package dev.keelbismark.shoophantom.listeners;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Phantom;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import dev.keelbismark.shoophantom.ShooPhantom;
import dev.keelbismark.shoophantom.items.ShooSigil;

import java.util.Random;

public class PhantomDeathListener implements Listener {
    
    private final ShooPhantom plugin;
    private final Random random;
    
    public PhantomDeathListener(ShooPhantom plugin) {
        this.plugin = plugin;
        this.random = new Random();
    }
    
    /**
     * Дроп Shoo Sigil при убийстве фантома
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPhantomDeath(EntityDeathEvent event) {
        if (event.getEntityType() != EntityType.PHANTOM) {
            return;
        }
        
        // Проверка, включен ли дроп
        if (!plugin.getConfig().getBoolean("sigil.drop.enabled", true)) {
            return;
        }
        
        Phantom phantom = (Phantom) event.getEntity();
        
        // Проверка, убит ли игроком
        boolean requirePlayer = plugin.getConfig().getBoolean("sigil.drop.require-player-kill", true);
        if (requirePlayer && phantom.getKiller() == null) {
            return;
        }
        
        // Получаем шанс дропа
        double baseChance = plugin.getConfig().getDouble("sigil.drop.base-chance", 0.05); // 5% базовый
        
        // Бонус от Looting
        int lootingLevel = 0;
        if (phantom.getKiller() != null) {
            ItemStack weapon = phantom.getKiller().getInventory().getItemInMainHand();
            if (weapon != null && weapon.hasItemMeta()) {
                lootingLevel = weapon.getEnchantmentLevel(org.bukkit.enchantments.Enchantment.LOOTING);
            }
        }
        
        double lootingBonus = plugin.getConfig().getDouble("sigil.drop.looting-bonus", 0.01); // +1% за уровень
        double finalChance = baseChance + (lootingLevel * lootingBonus);
        
        // Проверка шанса
        if (random.nextDouble() < finalChance) {
            // Дроп Sigil
            ItemStack sigil = ShooSigil.create(1);
            event.getDrops().add(sigil);
            
            // Сообщение убийце
            if (phantom.getKiller() != null) {
                String message = plugin.getConfig().getString("messages.sigil.dropped", 
                    "§5✨ Вы получили Shoo Sigil!");
                phantom.getKiller().sendMessage(plugin.getMessages().get("prefix") + message);
            }
            
            if (plugin.getConfigManager().isDebugEnabled()) {
                plugin.getLogger().info(String.format(
                    "Sigil dropped from phantom (chance: %.2f%%, looting: %d)",
                    finalChance * 100, lootingLevel
                ));
            }
        }
    }
}
