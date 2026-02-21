package dev.keelbismark.shoophantom.tasks;

import org.bukkit.scheduler.BukkitRunnable;

import dev.keelbismark.shoophantom.ShooPhantom;
import dev.keelbismark.shoophantom.data.Ward;

public class DegradationTask extends BukkitRunnable {
    
    private final ShooPhantom plugin;
    
    public DegradationTask(ShooPhantom plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void run() {
        for (Ward ward : plugin.getWardManager().getAllWards()) {
            // Проверка деградации Tier 2
            Ward updatedWard = plugin.getDegradationManager().checkTier2Degradation(ward);
            if (updatedWard != ward) {
                plugin.getWardManager().updateWardReference(updatedWard);
                ward = updatedWard;
            }

            // Проверка деградации Tier 3
            updatedWard = plugin.getDegradationManager().checkTier3Degradation(ward);
            if (updatedWard != ward) {
                plugin.getWardManager().updateWardReference(updatedWard);
            }
        }
    }
}
