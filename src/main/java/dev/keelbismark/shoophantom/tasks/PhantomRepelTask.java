package dev.keelbismark.shoophantom.tasks;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Phantom;
import org.bukkit.scheduler.BukkitRunnable;

import dev.keelbismark.shoophantom.ShooPhantom;
import dev.keelbismark.shoophantom.data.Ward;
import dev.keelbismark.shoophantom.mechanics.EffectManager;
import dev.keelbismark.shoophantom.mechanics.TierCalculator;

import java.util.List;

public class PhantomRepelTask extends BukkitRunnable {
    
    private final ShooPhantom plugin;
    private final EffectManager effectManager;
    private final TierCalculator tierCalculator;
    
    public PhantomRepelTask(ShooPhantom plugin) {
        this.plugin = plugin;
        this.effectManager = new EffectManager(plugin);
        this.tierCalculator = new TierCalculator(plugin);
    }
    
    @Override
    public void run() {
        // Обработка всех активных Tier 3 оберегов
        for (Ward ward : plugin.getWardManager().getAllWards()) {
            if (ward.getTier() != 3 || !ward.isActive()) {
                continue;
            }
            
            World world = plugin.getServer().getWorld(ward.getWorld());
            if (world == null) {
                continue;
            }
            
            Location wardLoc = ward.getLocation(world);
            
            // Получаем радиус с учетом мощности
            double power = tierCalculator.calculateTier3Power(wardLoc);
            int radius = tierCalculator.getTier3Radius(power);
            
            // Находим всех фантомов в радиусе
            List<Entity> entities = wardLoc.getNearbyEntities(radius, radius, radius)
                    .stream()
                    .filter(e -> e instanceof Phantom)
                    .toList();
            
            // Применяем эффекты к каждому фантому
            for (Entity entity : entities) {
                Phantom phantom = (Phantom) entity;
                effectManager.applyTier3Effects(ward, phantom, wardLoc);
            }
        }
    }
}
