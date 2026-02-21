package dev.keelbismark.shoophantom.mechanics;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import dev.keelbismark.shoophantom.ShooPhantom;

import java.util.Set;

public class TierCalculator {
    
    private final ShooPhantom plugin;
    
    public TierCalculator(ShooPhantom plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Рассчитать Tier оберега на основе структуры
     */
    public int calculateTier(Location potLocation) {
        // Проверка центрального блока
        if (potLocation.getBlock().getType() != Material.DECORATED_POT) {
            return 0;
        }
        
        // Проверка Tier 1
        if (!checkTier1Structure(potLocation)) {
            return 0;
        }
        
        // Проверка Tier 2
        if (!checkTier2Structure(potLocation)) {
            return 1; // Tier 1 валиден, но Tier 2 нет
        }
        
        // Проверка Tier 3
        if (!checkTier3Structure(potLocation)) {
            return 2; // Tier 2 валиден, но Tier 3 нет
        }
        
        return 3; // Все структуры валидны
    }
    
    /**
     * Проверка структуры Tier 1
     */
    private boolean checkTier1Structure(Location pot) {
        Block above = pot.clone().add(0, 1, 0).getBlock();
        Block below = pot.clone().add(0, -1, 0).getBlock();
        
        Block sideEast = pot.clone().add(1, 0, 0).getBlock();
        Block sideWest = pot.clone().add(-1, 0, 0).getBlock();
        Block sideSouth = pot.clone().add(0, 0, 1).getBlock();
        Block sideNorth = pot.clone().add(0, 0, -1).getBlock();
        
        Block cornerNE = pot.clone().add(1, 0, -1).getBlock();
        Block cornerNW = pot.clone().add(-1, 0, -1).getBlock();
        Block cornerSE = pot.clone().add(1, 0, 1).getBlock();
        Block cornerSW = pot.clone().add(-1, 0, 1).getBlock();
        
        Material expectedAbove = plugin.getConfigManager().getTier1AboveMaterial();
        Material expectedBelow = plugin.getConfigManager().getTier1BelowMaterial();
        Material expectedSides = plugin.getConfigManager().getTier1SidesMaterial();
        Material expectedCorners = plugin.getConfigManager().getTier1CornersMaterial();
        
        return above.getType() == expectedAbove &&
               below.getType() == expectedBelow &&
               sideEast.getType() == expectedSides &&
               sideWest.getType() == expectedSides &&
               sideSouth.getType() == expectedSides &&
               sideNorth.getType() == expectedSides &&
               cornerNE.getType() == expectedCorners &&
               cornerNW.getType() == expectedCorners &&
               cornerSE.getType() == expectedCorners &&
               cornerSW.getType() == expectedCorners;
    }
    
    /**
     * Проверка структуры Tier 2
     */
    private boolean checkTier2Structure(Location pot) {
        // Проверка кольца 5x5 на уровне Y-1
        int aliveBlocks = countAliveCopperBlocks(pot);
        int minAlive = plugin.getConfigManager().getTier2MinAliveBlocks();
        
        return aliveBlocks >= minAlive;
    }
    
    /**
     * Подсчет живых медных блоков в кольце
     */
    public int countAliveCopperBlocks(Location pot) {
        int count = 0;
        Set<Material> aliveMaterials = plugin.getConfigManager().getTier2AliveMaterials();
        
        // Периметр 5x5 на уровне Y-1
        int y = -1;
        
        // Северная сторона
        for (int x = -2; x <= 2; x++) {
            Block block = pot.clone().add(x, y, -2).getBlock();
            if (aliveMaterials.contains(block.getType())) {
                count++;
            }
        }
        
        // Южная сторона
        for (int x = -2; x <= 2; x++) {
            Block block = pot.clone().add(x, y, 2).getBlock();
            if (aliveMaterials.contains(block.getType())) {
                count++;
            }
        }
        
        // Западная сторона (без углов)
        for (int z = -1; z <= 1; z++) {
            Block block = pot.clone().add(-2, y, z).getBlock();
            if (aliveMaterials.contains(block.getType())) {
                count++;
            }
        }
        
        // Восточная сторона (без углов)
        for (int z = -1; z <= 1; z++) {
            Block block = pot.clone().add(2, y, z).getBlock();
            if (aliveMaterials.contains(block.getType())) {
                count++;
            }
        }
        
        return count;
    }
    
    /**
     * Проверка структуры Tier 3
     */
    private boolean checkTier3Structure(Location pot) {
        // Проверка 4 мачт
        int aliveMasts = countAliveMasts(pot);
        
        // Хотя бы одна мачта должна быть живой для Tier 3
        return aliveMasts > 0;
    }
    
    /**
     * Подсчет живых мачт
     */
    public int countAliveMasts(Location pot) {
        int count = 0;
        
        int[][] mastPositions = {
            {2, 0, 2},
            {2, 0, -2},
            {-2, 0, 2},
            {-2, 0, -2}
        };
        
        Material glass = plugin.getConfigManager().getTier3MastGlass();
        Material top = plugin.getConfigManager().getTier3MastTop();
        
        for (int[] pos : mastPositions) {
            if (checkMast(pot, pos[0], pos[2], glass, top)) {
                count++;
            }
        }
        
        return count;
    }
    
    /**
     * Проверка одной мачты
     */
    private boolean checkMast(Location pot, int x, int z, Material glass, Material top) {
        // Проверка стеклянных блоков (y = 0, 1, 2)
        for (int y = 0; y <= 2; y++) {
            Block block = pot.clone().add(x, y, z).getBlock();
            if (block.getType() != glass) {
                return false;
            }
        }
        
        // Проверка верхушки (y = 3)
        Block topBlock = pot.clone().add(x, 3, z).getBlock();
        return topBlock.getType() == top;
    }
    
    /**
     * Рассчитать мощность Tier 3 (0.0 - 1.0)
     */
    public double calculateTier3Power(Location pot) {
        int aliveMasts = countAliveMasts(pot);
        return aliveMasts / 4.0; // 0.25, 0.5, 0.75 или 1.0
    }
    
    /**
     * Получить радиус Tier 3 с учетом мощности
     */
    public int getTier3Radius(double power) {
        int minRadius = plugin.getConfigManager().getTier3RadiusMin();
        int maxRadius = plugin.getConfigManager().getTier3RadiusMax();
        
        return (int) (minRadius + (maxRadius - minRadius) * power);
    }
}
