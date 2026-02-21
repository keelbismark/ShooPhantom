package dev.keelbismark.shoophantom.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;

import dev.keelbismark.shoophantom.ShooPhantom;
import dev.keelbismark.shoophantom.data.Ward;
import dev.keelbismark.shoophantom.mechanics.TierCalculator;

public class BlockListener implements Listener {
    
    private final ShooPhantom plugin;
    private final TierCalculator tierCalculator;
    
    public BlockListener(ShooPhantom plugin) {
        this.plugin = plugin;
        this.tierCalculator = new TierCalculator(plugin);
    }
    
    /**
     * Обработка разрушения блока
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();
        
        // Проверка разрушения горшка-оберега
        if (block.getType() == Material.DECORATED_POT) {
            Ward ward = plugin.getWardManager().getWardByLocation(block.getLocation());
            
            if (ward != null) {
                handleWardDestruction(ward, player);
                return;
            }
        }
        
        // Проверка изменения структуры оберега
        checkNearbyWards(block.getLocation());
    }
    
    /**
     * Обработка установки блока
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        // Проверка изменения структуры оберега
        checkNearbyWards(event.getBlock().getLocation());
    }
    
    /**
     * Защита от взрыва (опционально)
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        if (!plugin.getConfigManager().isExplosionProtectionEnabled()) {
            return;
        }
        
        // Защита горшков-оберегов от взрыва
        event.blockList().removeIf(block -> {
            if (block.getType() == Material.DECORATED_POT) {
                Ward ward = plugin.getWardManager().getWardByLocation(block.getLocation());
                return ward != null; // Удаляем из списка взрыва, если это оберег
            }
            return false;
        });
    }
    
    /**
     * Защита от взрыва блоков
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        if (!plugin.getConfigManager().isExplosionProtectionEnabled()) {
            return;
        }
        
        // Защита горшков-оберегов от взрыва
        event.blockList().removeIf(block -> {
            if (block.getType() == Material.DECORATED_POT) {
                Ward ward = plugin.getWardManager().getWardByLocation(block.getLocation());
                return ward != null;
            }
            return false;
        });
    }
    
    /**
     * Обработка разрушения оберега
     */
    private void handleWardDestruction(Ward ward, Player player) {
        // Дроп топлива
        if (plugin.getConfigManager().dropFuelOnBreak() && ward.getFuel() > 0) {
            Location dropLoc = ward.getLocation(player.getWorld()).add(0.5, 0.5, 0.5);
            Material fuelMaterial = plugin.getConfigManager().getFuelMaterial();
            
            ItemStack fuelDrop = new ItemStack(fuelMaterial, ward.getFuel());
            dropLoc.getWorld().dropItemNaturally(dropLoc, fuelDrop);
        }
        
        // Удаление оберега
        plugin.getWardManager().removeWard(ward);
        
        // Уведомление
        player.sendMessage(plugin.getMessages().destroySuccess());
    }
    
    /**
     * Проверка оберегов рядом с изменённым блоком
     */
    private void checkNearbyWards(Location changedBlock) {
        // Ищем обереги в радиусе 5 блоков
        for (int x = -5; x <= 5; x++) {
            for (int y = -5; y <= 5; y++) {
                for (int z = -5; z <= 5; z++) {
                    Location checkLoc = changedBlock.clone().add(x, y, z);
                    
                    if (checkLoc.getBlock().getType() == Material.DECORATED_POT) {
                        Ward ward = plugin.getWardManager().getWardByLocation(checkLoc);

                        if (ward != null) {
                            // Пересчет Tier
                            int newTier = tierCalculator.calculateTier(checkLoc);

                            if (newTier != ward.getTier()) {
                                int oldTier = ward.getTier();
                                Ward updatedWard = plugin.getWardManager().updateWardTier(ward, newTier);

                                // Уведомление владельца
                                if (newTier > oldTier) {
                                    // Улучшение
                                    notifyOwner(updatedWard, "§a✨ Tier оберега повышен до " + newTier + "!");
                                } else if (newTier < oldTier) {
                                    // Ухудшение
                                    notifyOwner(updatedWard, "§c⚠ Tier оберега понижен до " + newTier + "!");
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Уведомление владельца
     */
    private void notifyOwner(Ward ward, String message) {
        Player owner = plugin.getServer().getPlayer(ward.getOwnerUUID());
        if (owner != null && owner.isOnline()) {
            owner.sendMessage(plugin.getMessages().get("prefix") + message);
        }
    }
}
