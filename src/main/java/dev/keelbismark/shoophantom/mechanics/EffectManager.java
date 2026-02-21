package dev.keelbismark.shoophantom.mechanics;

import org.bukkit.Location;
import org.bukkit.entity.Phantom;
import org.bukkit.util.Vector;

import dev.keelbismark.shoophantom.ShooPhantom;
import dev.keelbismark.shoophantom.data.Ward;

public class EffectManager {
    
    private final ShooPhantom plugin;
    private final TierCalculator tierCalculator;
    
    public EffectManager(ShooPhantom plugin) {
        this.plugin = plugin;
        this.tierCalculator = new TierCalculator(plugin);
    }
    
    /**
     * Применить эффекты Tier 3 к фантому
     */
    public void applyTier3Effects(Ward ward, Phantom phantom, Location wardLocation) {
        // Рассчитываем мощность
        double power = tierCalculator.calculateTier3Power(wardLocation);
        
        if (power <= 0) {
            return; // Нет живых мачт
        }
        
        // Урон
        double damage = interpolate(
            plugin.getConfigManager().getTier3DamageMin(),
            plugin.getConfigManager().getTier3DamageMax(),
            power
        );
        phantom.damage(damage);
        
        // Поджог
        int fireTicks = (int) interpolate(
            plugin.getConfigManager().getTier3FireTicksMin(),
            plugin.getConfigManager().getTier3FireTicksMax(),
            power
        );
        phantom.setFireTicks(fireTicks);
        
        // Отталкивание
        double knockback = interpolate(
            plugin.getConfigManager().getTier3KnockbackMin(),
            plugin.getConfigManager().getTier3KnockbackMax(),
            power
        );
        applyKnockback(phantom, wardLocation, knockback);
    }
    
    /**
     * Интерполяция между min и max на основе мощности
     */
    private double interpolate(double min, double max, double power) {
        return min + (max - min) * power;
    }
    
    /**
     * Применить отталкивание
     */
    private void applyKnockback(Phantom phantom, Location wardLocation, double strength) {
        Location phantomLoc = phantom.getLocation();
        
        // Вектор от оберега к фантому
        Vector direction = phantomLoc.toVector().subtract(wardLocation.toVector()).normalize();
        
        // Применяем отталкивание
        phantom.setVelocity(direction.multiply(strength));
    }
}
