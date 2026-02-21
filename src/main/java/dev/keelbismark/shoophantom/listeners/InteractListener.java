package dev.keelbismark.shoophantom.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import dev.keelbismark.shoophantom.ShooPhantom;
import dev.keelbismark.shoophantom.data.Ward;
import dev.keelbismark.shoophantom.items.ShooSigil;
import dev.keelbismark.shoophantom.mechanics.TierCalculator;

public class InteractListener implements Listener {
    
    private final ShooPhantom plugin;
    private final TierCalculator tierCalculator;
    
    public InteractListener(ShooPhantom plugin) {
        this.plugin = plugin;
        this.tierCalculator = new TierCalculator(plugin);
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        
        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.DECORATED_POT) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        // Проверяем, есть ли уже оберег на этой локации
        Ward ward = plugin.getWardManager().getWardByLocation(block.getLocation());

        if (ward == null) {
            // Нет оберега - попытка активации
            handleActivation(player, block, item, event);
        } else {
            // Есть оберег - попытка заправки или информация
            Location potLocation = block.getLocation();
            handleFueling(player, potLocation, ward, item, event);
        }
    }
    
    /**
     * Обработка активации оберега
     */
    private void handleActivation(Player player, Block block, ItemStack item, PlayerInteractEvent event) {
        // Проверка прав
        if (!player.hasPermission("shoo.create")) {
            player.sendMessage(plugin.getMessages().get("activation.no-permission"));
            return;
        }
        
        // Проверка Sigil
        if (!plugin.getConfigManager().requireSigil()) {
            // Sigil не требуется - прямая активация
            createWard(player, block);
            return;
        }
        
        if (item == null || !ShooSigil.isSigil(item)) {
            player.sendMessage(plugin.getMessages().activationNoSigil());
            return;
        }
        
        // Расчет Tier
        int tier = tierCalculator.calculateTier(block.getLocation());
        
        if (tier == 0) {
            player.sendMessage(plugin.getMessages().activationInvalidStructure());
            return;
        }
        
        // Проверка прав на конкретный Tier
        if (!player.hasPermission("shoo.create.tier" + tier)) {
            player.sendMessage(plugin.getMessages().get("activation.no-tier-permission"));
            return;
        }
        
        // Проверка лимита
        int maxWards = plugin.getConfigManager().getMaxWardsPerPlayer();
        if (maxWards > 0 && !player.hasPermission("shoo.bypass")) {
            int currentWards = plugin.getDatabase().countWardsByOwner(player.getUniqueId());
            if (currentWards >= maxWards) {
                player.sendMessage(plugin.getMessages().activationLimitReached(maxWards));
                return;
            }
        }
        
        // Создание оберега
        Ward newWard = plugin.getWardManager().createWard(player, block.getLocation());
        
        if (newWard == null) {
            player.sendMessage(plugin.getMessages().activationInvalidStructure());
            return;
        }
        
        // Расход Sigil
        item.setAmount(item.getAmount() - 1);
        
        // Уведомление
        player.sendMessage(plugin.getMessages().activationSuccess(newWard.getTier()));
        
        // Визуальный эффект
        block.getWorld().spawnParticle(
            org.bukkit.Particle.TOTEM_OF_UNDYING,
            block.getLocation().add(0.5, 1, 0.5),
            30, 0.3, 0.5, 0.3, 0.1
        );
        block.getWorld().playSound(
            block.getLocation(),
            org.bukkit.Sound.BLOCK_BEACON_ACTIVATE,
            1.0f, 1.5f
        );
        
        event.setCancelled(true);
    }
    
    /**
     * Создание оберега без Sigil
     */
    private void createWard(Player player, Block block) {
        Ward newWard = plugin.getWardManager().createWard(player, block.getLocation());
        
        if (newWard == null) {
            player.sendMessage(plugin.getMessages().activationInvalidStructure());
            return;
        }
        
        player.sendMessage(plugin.getMessages().activationSuccess(newWard.getTier()));
    }
    
    /**
      * Обработка заправки оберега
      */
    private void handleFueling(Player player, Location potLocation, Ward ward, ItemStack item, PlayerInteractEvent event) {
        // Проверка топлива
        Material fuelMaterial = plugin.getConfigManager().getFuelMaterial();

        if (item != null && item.getType() == fuelMaterial) {
            // Попытка заправки
            int maxFuel = plugin.getConfigManager().getMaxFuel();

            if (ward.getFuel() >= maxFuel) {
                player.sendMessage(plugin.getMessages().fuelFull());
                return;
            }

            // Добавляем топливо
            int toAdd = item.getAmount();
            int added = Math.min(toAdd, maxFuel - ward.getFuel());

            Ward newWard = plugin.getFuelManager().addFuel(ward, added);
            if (newWard != null) {
                plugin.getWardManager().updateWardReference(newWard);
                item.setAmount(item.getAmount() - added);
                player.sendMessage(plugin.getMessages().fuelAdded(added, newWard.getFuel()));
            }

            event.setCancelled(true);
        } else {
            // Показать информацию об обереге
            if (player.isSneaking()) {
                // Получить актуальный ward после обновлений
                Ward currentWard = plugin.getWardManager().getWardByLocation(potLocation);
                showWardInfo(player, currentWard != null ? currentWard : ward);
                event.setCancelled(true);
            }
        }
    }
    
    /**
     * Показать информацию об обереге
     */
    private void showWardInfo(Player player, Ward ward) {
        player.sendMessage(plugin.getMessages().infoHeader());
        
        String ownerName = plugin.getServer().getOfflinePlayer(ward.getOwnerUUID()).getName();
        if (ownerName == null) ownerName = "Неизвестен";
        
        player.sendMessage(plugin.getMessages().infoOwner(ownerName));
        player.sendMessage(plugin.getMessages().infoTier(ward.getTier()));
        player.sendMessage(plugin.getMessages().infoRadius(plugin.getWardManager().getRadius(ward.getTier())));
        player.sendMessage(plugin.getMessages().infoFuel(ward.getFuel()));
        
        long remaining = plugin.getFuelManager().getRemainingTime(ward);
        String timeStr = plugin.getFuelManager().formatRemainingTime(remaining);
        player.sendMessage(plugin.getMessages().infoTimeLeft(timeStr));
        
        // Дополнительная информация для Tier 2+
        if (ward.getTier() >= 2) {
            int aliveBlocks = tierCalculator.countAliveCopperBlocks(
                ward.getLocation(player.getWorld())
            );
            player.sendMessage(plugin.getMessages().infoRing(aliveBlocks));
        }
        
        // Дополнительная информация для Tier 3
        if (ward.getTier() >= 3) {
            int aliveMasts = tierCalculator.countAliveMasts(
                ward.getLocation(player.getWorld())
            );
            int percent = (aliveMasts * 100) / 4;
            player.sendMessage(plugin.getMessages().infoMasts(aliveMasts, percent));
        }
    }
}
