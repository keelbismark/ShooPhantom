package dev.keelbismark.shoophantom.items;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ShapedRecipe;

import dev.keelbismark.shoophantom.ShooPhantom;

import org.bukkit.inventory.ItemStack;

public class CraftingRecipes {
    
    /**
     * Регистрация всех рецептов
     */
    public static void register(ShooPhantom plugin) {
        // Основной крафт (дорогой)
        if (plugin.getConfig().getBoolean("activation.craft.enabled", true)) {
            registerSigilRecipe(plugin);
        }
        
        // Альтернативный крафт (упрощенный)
        if (plugin.getConfig().getBoolean("sigil.simple-craft.enabled", true)) {
            registerSimpleSigilRecipe(plugin);
        }
    }
    
    /**
     * Регистрация рецепта Shoo Sigil (основной - дорогой)
     */
    private static void registerSigilRecipe(ShooPhantom plugin) {
        ItemStack result = ShooSigil.create(1);
        
        NamespacedKey key = new NamespacedKey(plugin, "shoo_sigil");
        ShapedRecipe recipe = new ShapedRecipe(key, result);
        
        // Форма рецепта
        recipe.shape("PEP", "ENE", "PEP");
        
        // Ингредиенты
        recipe.setIngredient('P', Material.PHANTOM_MEMBRANE);
        recipe.setIngredient('E', Material.ENDER_PEARL);
        recipe.setIngredient('N', Material.NETHER_STAR);
        
        // Регистрация
        plugin.getServer().addRecipe(recipe);
        
        plugin.getLogger().info("Рецепт Shoo Sigil (основной) зарегистрирован");
    }
    
    /**
     * Регистрация упрощенного рецепта Shoo Sigil
     */
    private static void registerSimpleSigilRecipe(ShooPhantom plugin) {
        ItemStack result = ShooSigil.create(1);
        
        NamespacedKey key = new NamespacedKey(plugin, "shoo_sigil_simple");
        ShapedRecipe recipe = new ShapedRecipe(key, result);
        
        // Форма рецепта (из конфига или дефолтная)
        recipe.shape("MPM", "PEP", "MPM");
        
        // Ингредиенты
        recipe.setIngredient('M', Material.PHANTOM_MEMBRANE);  // 4 мембраны
        recipe.setIngredient('P', Material.ENDER_PEARL);       // 2 жемчужины
        recipe.setIngredient('E', Material.ECHO_SHARD);        // 1 осколок эха
        
        // Регистрация
        plugin.getServer().addRecipe(recipe);
        
        plugin.getLogger().info("Рецепт Shoo Sigil (упрощенный) зарегистрирован");
    }
}
