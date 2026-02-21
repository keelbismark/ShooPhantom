package dev.keelbismark.shoophantom.listeners;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;

import dev.keelbismark.shoophantom.ShooPhantom;

import java.util.List;

public class PhantomListener implements Listener {
    
    private final ShooPhantom plugin;
    
    public PhantomListener(ShooPhantom plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Блокировка спавна фантомов (Tier 1+)
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPhantomSpawn(CreatureSpawnEvent event) {
        if (event.getEntityType() != EntityType.PHANTOM) {
            return;
        }
        
        // Проверка причин спавна из конфига
        List<String> blockedReasons = plugin.getConfig().getStringList("phantoms.blocked-spawn-reasons");
        
        if (!blockedReasons.contains(event.getSpawnReason().name())) {
            return; // Эта причина спавна не блокируется
        }
        
        // Проверка, находится ли точка спавна в зоне защиты
        if (plugin.getWardManager().isProtected(event.getLocation(), 1)) {
            event.setCancelled(true);
            
            if (plugin.getConfigManager().isDebugEnabled()) {
                plugin.getLogger().info("Заблокирован спавн фантома в " + 
                    event.getLocation().getBlockX() + ", " +
                    event.getLocation().getBlockY() + ", " +
                    event.getLocation().getBlockZ());
            }
        }
    }
    
    /**
      * Сброс таргета фантомов (Tier 2+)
      */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPhantomTarget(EntityTargetLivingEntityEvent event) {
        if (!(event.getEntity() instanceof Phantom)) {
            return;
        }

        LivingEntity target = event.getTarget();
        if (!(target instanceof Player player)) {
            return;
        }

        // Проверка, находится ли игрок в зоне защиты Tier 2+
        if (plugin.getWardManager().isProtected(player.getLocation(), 2)) {
            event.setCancelled(true);

            if (plugin.getConfigManager().isDebugEnabled()) {
                plugin.getLogger().info("Сброшен таргет фантома на игрока " + player.getName());
            }
        }
    }
}
