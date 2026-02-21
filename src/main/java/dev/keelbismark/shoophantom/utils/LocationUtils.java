package dev.keelbismark.shoophantom.utils;

import org.bukkit.Location;

public class LocationUtils {
    
    /**
     * Проверка, находится ли локация в кубической области
     */
    public static boolean isInCube(Location center, Location check, int radius) {
        if (!center.getWorld().equals(check.getWorld())) {
            return false;
        }
        
        return Math.abs(center.getBlockX() - check.getBlockX()) <= radius &&
               Math.abs(center.getBlockY() - check.getBlockY()) <= radius &&
               Math.abs(center.getBlockZ() - check.getBlockZ()) <= radius;
    }
    
    /**
     * Форматирование локации для вывода
     */
    public static String format(Location location) {
        return String.format("%s: %d, %d, %d",
            location.getWorld().getName(),
            location.getBlockX(),
            location.getBlockY(),
            location.getBlockZ()
        );
    }
    
    /**
     * Получить расстояние между двумя локациями (2D, без учета Y)
     */
    public static double distance2D(Location loc1, Location loc2) {
        if (!loc1.getWorld().equals(loc2.getWorld())) {
            return Double.MAX_VALUE;
        }
        
        double dx = loc1.getX() - loc2.getX();
        double dz = loc1.getZ() - loc2.getZ();
        
        return Math.sqrt(dx * dx + dz * dz);
    }
}
