package dev.keelbismark.shoophantom.items;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import dev.keelbismark.shoophantom.ShooPhantom;

import java.util.Arrays;

public class ShooSigil {
    
    private static final NamespacedKey SIGIL_KEY = new NamespacedKey(ShooPhantom.getInstance(), "shoo_sigil");
    
    /**
     * Создать Shoo Sigil
     */
    public static ItemStack create(int amount) {
        ShooPhantom plugin = ShooPhantom.getInstance();
        Material baseMaterial = plugin.getConfigManager().getSigilBaseMaterial();
        
        ItemStack sigil = new ItemStack(baseMaterial, amount);
        ItemMeta meta = sigil.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName("§5Shoo Sigil");
            meta.setLore(Arrays.asList(
                "§7Активирует Резонансный оберег",
                "§7ПКМ по украшенному горшку"
            ));
            
            // Добавляем PDC метку
            meta.getPersistentDataContainer().set(
                SIGIL_KEY,
                PersistentDataType.BYTE,
                (byte) 1
            );
            
            sigil.setItemMeta(meta);
        }
        
        return sigil;
    }
    
    /**
     * Проверить, является ли предмет Shoo Sigil
     */
    public static boolean isSigil(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        
        ItemMeta meta = item.getItemMeta();
        
        return meta.getPersistentDataContainer().has(SIGIL_KEY, PersistentDataType.BYTE);
    }
}
