package dev.keelbismark.shoophantom.tasks;

import org.bukkit.scheduler.BukkitRunnable;

import dev.keelbismark.shoophantom.ShooPhantom;
import dev.keelbismark.shoophantom.data.Ward;

public class FuelConsumptionTask extends BukkitRunnable {
    
    private final ShooPhantom plugin;
    
    public FuelConsumptionTask(ShooPhantom plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void run() {
        for (Ward ward : plugin.getWardManager().getAllWards()) {
            plugin.getFuelManager().processFuelConsumption(ward);
        }
    }
}
