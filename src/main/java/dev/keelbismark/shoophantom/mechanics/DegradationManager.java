package dev.keelbismark.shoophantom.mechanics;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import dev.keelbismark.shoophantom.ShooPhantom;
import dev.keelbismark.shoophantom.data.Ward;

import java.util.Random;

public class DegradationManager {
    
    private final ShooPhantom plugin;
    private final TierCalculator tierCalculator;
    private final Random random;
    
    public DegradationManager(ShooPhantom plugin) {
        this.plugin = plugin;
        this.tierCalculator = new TierCalculator(plugin);
        this.random = new Random();
    }
    
    /**
      * Проверка деградации Tier 2 (окисление меди)
      * @return New Ward instance if tier changed, or current ward otherwise
      */
    public Ward checkTier2Degradation(Ward ward) {
        if (ward.getTier() < 2) {
            return ward; // Нет Tier 2
        }

        World world = Bukkit.getWorld(ward.getWorld());
        if (world == null) {
            return ward;
        }
        
        Location potLoc = ward.getLocation(world);
        int aliveBlocks = tierCalculator.countAliveCopperBlocks(potLoc);
        int minAlive = plugin.getConfigManager().getTier2MinAliveBlocks();
        int warnThreshold = plugin.getConfigManager().getTier2WarnThreshold();
        
        // Проверка предупреждения
        if (aliveBlocks == warnThreshold) {
            notifyOwner(ward, plugin.getMessages().degradationTier2Warn(aliveBlocks));
        }
        
        // Проверка отказа
        if (aliveBlocks < minAlive) {
            // Понижение Tier
            Ward newWard = ward.withTier(1);
            plugin.getDatabase().saveWard(newWard);
            notifyOwner(ward, plugin.getMessages().degradationTier2Fail());
            return newWard;
        }

        return ward;
    }

    /**
      * Проверка деградации Tier 3 (выгорание мачт)
      */
    public Ward checkTier3Degradation(Ward ward) {
        if (ward.getTier() < 3) {
            return ward; // Нет Tier 3
        }

        long now = System.currentTimeMillis();

        // Первичная настройка: установим время деградации если не установлено
        if (ward.getNextDegradeTime() == 0) {
            Ward newWard = setNextDegradeTime(ward);
            plugin.getDatabase().saveWard(newWard);
            ward = newWard;
        }

        // Проверка времени деградации
        if (ward.getNextDegradeTime() > now) {
            // Еще не время - проверяем паузу при отсутствии игроков
            if (plugin.getConfigManager().getTier3PauseWhenNoPlayers()) {
                World world = Bukkit.getWorld(ward.getWorld());
                if (world != null) {
                    Location wardLoc = ward.getLocation(world);
                    int pauseRadius = plugin.getConfigManager().getTier3PauseCheckRadius();

                    boolean hasPlayers = false;
                    for (Player player : world.getPlayers()) {
                        if (wardLoc.distance(player.getLocation()) <= pauseRadius) {
                            hasPlayers = true;
                            break;
                        }
                    }

                    if (!hasPlayers) {
                        return ward; // Пауза деградации
                    }
                }
            }
            return ward; // Еще не время
        }

        // Время наступило - выполняем деградацию без проверки паузы
        return performTier3Degradation(ward);
    }
    
    /**
      * Выполнение деградации Tier 3
      * @return New Ward instance if tier changed, or current ward otherwise
      */
    private Ward performTier3Degradation(Ward ward) {
        World world = Bukkit.getWorld(ward.getWorld());
        if (world == null) {
            return ward;
        }

        Location potLoc = ward.getLocation(world);

        // Найти случайную живую мачту и "перегореть" её
        int[][] mastPositions = {
            {2, 0, 2},
            {2, 0, -2},
            {-2, 0, 2},
            {-2, 0, -2}
        };

        Material topMaterial = plugin.getConfigManager().getTier3MastTop();
        Material deadMaterial = plugin.getConfigManager().getTier3MastDead();

        // Найти все живые мачты
        Location[] aliveMasts = new Location[4];
        int aliveCount = 0;

        for (int[] pos : mastPositions) {
            Location topLoc = potLoc.clone().add(pos[0], 3, pos[2]);
            if (topLoc.getBlock().getType() == topMaterial) {
                aliveMasts[aliveCount++] = topLoc;
            }
        }

        if (aliveCount == 0) {
            // Нет живых мачт - понижение Tier
            Ward newWard = ward.withTier(2);
            plugin.getDatabase().saveWard(newWard);
            notifyOwner(ward, plugin.getMessages().degradationTier3Fail());
            return newWard;
        }
        
        // Выбираем случайную живую мачту
        Location mastToBurn = aliveMasts[random.nextInt(aliveCount)];
        
        // Заменяем END_ROD на IRON_BARS
        Block mastBlock = mastToBurn.getBlock();
        mastBlock.setType(deadMaterial);
        
        // Визуальные эффекты
        playDegradationEffects(mastToBurn);
        
        // Пересчитываем живые мачты
        int newAliveCount = aliveCount - 1;
        int percent = (newAliveCount * 100) / 4;
        
        // Уведомление
        notifyOwner(ward, plugin.getMessages().degradationTier3Mast(percent, newAliveCount));
        
        // Проверка на полное выгорание
        if (newAliveCount == 0) {
            Ward newWard = ward.withTier(2);
            plugin.getDatabase().saveWard(newWard);
            notifyOwner(ward, plugin.getMessages().degradationTier3Fail());
            return newWard;
        } else {
            // Установка нового таймера
            Ward newWard = setNextDegradeTime(ward);
            plugin.getDatabase().saveWard(newWard);
            return newWard;
        }
    }

    /**
      * Установка следующего времени деградации
      * @return New Ward instance with updated degrade time
      */
    private Ward setNextDegradeTime(Ward ward) {
        long cycleMin = plugin.getConfigManager().getTier3CycleHoursMin() * 3600000L;
        long cycleMax = plugin.getConfigManager().getTier3CycleHoursMax() * 3600000L;

        double randomFactor = random.nextDouble(); // 0.0 - 1.0
        long cycleDuration = cycleMin + (long)(randomFactor * (cycleMax - cycleMin));

        long nextTime = System.currentTimeMillis() + cycleDuration;

        return ward.withNextDegradeTime(nextTime);
    }

    /**
      * Визуальные эффекты деградации
      */
    private void playDegradationEffects(Location location) {
        World world = location.getWorld();
        if (world == null) {
            return;
        }
        
        // Дым
        world.spawnParticle(Particle.SMOKE, location, 20, 0.3, 0.3, 0.3, 0.02);
        
        // Искры
        world.spawnParticle(Particle.LAVA, location, 10, 0.2, 0.2, 0.2, 0.01);
        
        // Звук
        world.playSound(location, Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1.0f, 1.0f);
    }
    
    /**
     * Уведомление владельца
     */
    private void notifyOwner(Ward ward, String message) {
        OfflinePlayer owner = Bukkit.getOfflinePlayer(ward.getOwnerUUID());
        if (owner.isOnline()) {
            owner.getPlayer().sendMessage(message);
        }
    }
}
