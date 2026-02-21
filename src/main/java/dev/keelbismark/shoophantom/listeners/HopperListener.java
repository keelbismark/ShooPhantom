package dev.keelbismark.shoophantom.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.ItemStack;

import dev.keelbismark.shoophantom.ShooPhantom;
import dev.keelbismark.shoophantom.data.Ward;

public class HopperListener implements Listener {
    
    private final ShooPhantom plugin;
    
    public HopperListener(ShooPhantom plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Обработка перемещения предметов через воронку
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryMoveItem(InventoryMoveItemEvent event) {
        // Проверка, что назначение - декорированный горшок
        if (event.getDestination().getHolder() == null) {
            return;
        }
        
        Block destBlock = null;
        
        if (event.getDestination().getLocation() != null) {
            destBlock = event.getDestination().getLocation().getBlock();
        }
        
        if (destBlock == null || destBlock.getType() != Material.DECORATED_POT) {
            // Проверка извлечения из горшка
            checkExtraction(event);
            return;
        }
        
        // Проверка, является ли горшок оберегом
        Ward ward = plugin.getWardManager().getWardByLocation(destBlock.getLocation());
        
        if (ward == null) {
            return; // Не оберег
        }
        
        // Проверка, разрешен ли ввод через воронку
        if (!plugin.getConfigManager().isHopperInputAllowed()) {
            event.setCancelled(true);
            return;
        }
        
        // Проверка, что перемещается топливо
        ItemStack item = event.getItem();
        Material fuelMaterial = plugin.getConfigManager().getFuelMaterial();
        
        if (item.getType() != fuelMaterial) {
            event.setCancelled(true); // Разрешено только топливо
            return;
        }
        
        // Проверка заполнения
        int maxFuel = plugin.getConfigManager().getMaxFuel();

        if (ward.getFuel() >= maxFuel) {
            event.setCancelled(true); // Хранилище полное
            return;
        }

        // Добавление топлива
        int toAdd = Math.min(item.getAmount(), maxFuel - ward.getFuel());

        Ward newWard = plugin.getFuelManager().addFuel(ward, toAdd);
        if (newWard != null) {
            plugin.getWardManager().updateWardReference(newWard);
        }

        // Отмена стандартного перемещения
        event.setCancelled(true);

        // Убираем предметы из источника
        event.getSource().removeItem(new ItemStack(fuelMaterial, toAdd));
    }
    
    /**
     * Проверка извлечения из оберега
     */
    private void checkExtraction(InventoryMoveItemEvent event) {
        if (event.getSource().getLocation() == null) {
            return;
        }
        
        Block sourceBlock = event.getSource().getLocation().getBlock();
        
        if (sourceBlock.getType() != Material.DECORATED_POT) {
            return;
        }
        
        Ward ward = plugin.getWardManager().getWardByLocation(sourceBlock.getLocation());
        
        if (ward == null) {
            return; // Не оберег
        }
        
        // Блокировка извлечения через воронку
        if (plugin.getConfigManager().isHopperOutputBlocked()) {
            event.setCancelled(true);
        }
    }
}
