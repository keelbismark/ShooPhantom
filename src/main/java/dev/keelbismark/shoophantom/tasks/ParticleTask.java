package dev.keelbismark.shoophantom.tasks;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import dev.keelbismark.shoophantom.ShooPhantom;
import dev.keelbismark.shoophantom.data.Ward;

public class ParticleTask extends BukkitRunnable {
    
    private final ShooPhantom plugin;
    private int tickCounter = 0;
    
    public ParticleTask(ShooPhantom plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void run() {
        tickCounter++;

        for (Ward ward : plugin.getWardManager().getAllWards()) {
            if (!ward.isActive()) {
                continue;
            }

            World world = plugin.getServer().getWorld(ward.getWorld());
            if (world == null) {
                continue;
            }

            Location wardLoc = ward.getLocation(world);

            // Эффекты в зависимости от Tier
            int tickMod = switch (ward.getTier()) {
                case 1 -> tickCounter % 100;
                case 2 -> tickCounter % 80;
                case 3 -> tickCounter % 60;
                default -> Integer.MAX_VALUE; // Play nothing
            };

            if (tickMod == 0) {
                switch (ward.getTier()) {
                    case 1 -> playTier1Effect(wardLoc);
                    case 2 -> playTier2Effect(wardLoc);
                    case 3 -> playTier3Effect(wardLoc);
                }
            }
        }
    }
    
    /**
     * Эффект Tier 1 - искры на громоотводе
     */
    private void playTier1Effect(Location wardLoc) {
        Location lightning = wardLoc.clone().add(0, 1, 0);
        lightning.getWorld().spawnParticle(
            Particle.END_ROD, 
            lightning, 
            3, 
            0.1, 0.3, 0.1, 
            0.01
        );
    }
    
    /**
     * Эффект Tier 2 - волна по кольцу
     */
    private void playTier2Effect(Location wardLoc) {
        World world = wardLoc.getWorld();
        int y = -1;
        
        // Периметр 5x5 на уровне Y-1
        // Северная сторона
        for (int x = -2; x <= 2; x++) {
            Location loc = wardLoc.clone().add(x + 0.5, y + 1, -2 + 0.5);
            world.spawnParticle(Particle.WAX_ON, loc, 2, 0.2, 0.2, 0.2, 0.01);
        }
        
        // Южная сторона
        for (int x = -2; x <= 2; x++) {
            Location loc = wardLoc.clone().add(x + 0.5, y + 1, 2 + 0.5);
            world.spawnParticle(Particle.WAX_ON, loc, 2, 0.2, 0.2, 0.2, 0.01);
        }
        
        // Западная сторона (без углов)
        for (int z = -1; z <= 1; z++) {
            Location loc = wardLoc.clone().add(-2 + 0.5, y + 1, z + 0.5);
            world.spawnParticle(Particle.WAX_ON, loc, 2, 0.2, 0.2, 0.2, 0.01);
        }
        
        // Восточная сторона (без углов)
        for (int z = -1; z <= 1; z++) {
            Location loc = wardLoc.clone().add(2 + 0.5, y + 1, z + 0.5);
            world.spawnParticle(Particle.WAX_ON, loc, 2, 0.2, 0.2, 0.2, 0.01);
        }
    }
    
    /**
     * Эффект Tier 3 - лучи от мачт к центру
     */
    private void playTier3Effect(Location wardLoc) {
        World world = wardLoc.getWorld();
        
        int[][] mastPositions = {
            {2, 0, 2},
            {2, 0, -2},
            {-2, 0, 2},
            {-2, 0, -2}
        };
        
        // Цвета для градиента
        Color fromColor = Color.fromRGB(128, 0, 255); // Фиолетовый
        Color toColor = Color.fromRGB(0, 128, 255);   // Синий
        
        for (int[] pos : mastPositions) {
            Location mastTop = wardLoc.clone().add(pos[0], 3, pos[2]);
            
            // Проверяем, жива ли мачта (есть ли END_ROD)
            if (mastTop.getBlock().getType() != plugin.getConfigManager().getTier3MastTop()) {
                continue; // Мачта мертва
            }
            
            // Рисуем луч от мачты к центру
            drawBeam(mastTop, wardLoc.clone().add(0, 1, 0), fromColor, toColor);
        }
    }
    
    /**
     * Рисование луча между двумя точками
     */
    private void drawBeam(Location from, Location to, Color fromColor, Color toColor) {
        World world = from.getWorld();
        double distance = from.distance(to);
        int points = (int) (distance * 2); // Плотность частиц
        
        for (int i = 0; i < points; i++) {
            double ratio = (double) i / points;
            
            Location particleLoc = from.clone().add(
                (to.getX() - from.getX()) * ratio,
                (to.getY() - from.getY()) * ratio,
                (to.getZ() - from.getZ()) * ratio
            );
            
            // Интерполяция цвета
            int r = (int) (fromColor.getRed() + (toColor.getRed() - fromColor.getRed()) * ratio);
            int g = (int) (fromColor.getGreen() + (toColor.getGreen() - fromColor.getGreen()) * ratio);
            int b = (int) (fromColor.getBlue() + (toColor.getBlue() - fromColor.getBlue()) * ratio);
            
            Particle.DustTransition dustTransition = new Particle.DustTransition(
                fromColor, toColor, 0.5f
            );
            
            world.spawnParticle(Particle.DUST_COLOR_TRANSITION, particleLoc, 1, 0, 0, 0, 0, dustTransition);
        }
    }
}
