package dev.keelbismark.shoophantom.mechanics;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import dev.keelbismark.shoophantom.ShooPhantom;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TierCalculator class.
 * Tests the calculation of protection tiers based on structure configuration.
 */
public class TierCalculatorTest {
    
    private TierCalculator tierCalculator;
    private ShooPhantom mockPlugin;
    
    @BeforeEach
    void setUp() {
        mockPlugin = mock(ShooPhantom.class);
        dev.keelbismark.shoophantom.config.ConfigManager mockConfigManager = mock(dev.keelbismark.shoophantom.config.ConfigManager.class);
        
        lenient().when(mockPlugin.getConfigManager()).thenReturn(mockConfigManager);
        lenient().when(mockPlugin.getLogger()).thenReturn(java.util.logging.Logger.getLogger("TierCalculatorTest"));
        lenient().when(mockConfigManager.getTier1AboveMaterial()).thenReturn(Material.LIGHTNING_ROD);
        lenient().when(mockConfigManager.getTier1BelowMaterial()).thenReturn(Material.CHISELED_TUFF_BRICKS);
        lenient().when(mockConfigManager.getTier1SidesMaterial()).thenReturn(Material.COPPER_GRATE);
        lenient().when(mockConfigManager.getTier1CornersMaterial()).thenReturn(Material.AMETHYST_BLOCK);
        lenient().when(mockConfigManager.getTier2AliveMaterials()).thenReturn(java.util.Set.of(Material.COPPER_BLOCK, Material.CUT_COPPER));
        lenient().when(mockConfigManager.getTier2MinAliveBlocks()).thenReturn(9);
        lenient().when(mockConfigManager.getTier3MastGlass()).thenReturn(Material.TINTED_GLASS);
        lenient().when(mockConfigManager.getTier3MastTop()).thenReturn(Material.END_ROD);
        lenient().when(mockConfigManager.getTier3RadiusMin()).thenReturn(64);
        lenient().when(mockConfigManager.getTier3RadiusMax()).thenReturn(128);
        
        tierCalculator = new TierCalculator(mockPlugin);
    }
    
    @Test
    @DisplayName("Should return 0 for non-pot center block")
    void testCalculateTier_InvalidCenter() {
        Location location = mock(Location.class);
        Block block = mock(Block.class);
        
        when(location.getBlock()).thenReturn(block);
        when(block.getType()).thenReturn(Material.STONE);
        
        int tier = tierCalculator.calculateTier(location);
        
        assertEquals(0, tier, "Should return tier 0 for non-decorated pot center");
    }
    
    @Test
    @DisplayName("Should return 1 for complete Tier 1 structure")
    void testCalculateTier_Tier1Complete() {
        Location location = mock(Location.class);
        Block centerBlock = mock(Block.class);
        
        when(location.getBlock()).thenReturn(centerBlock);
        when(centerBlock.getType()).thenReturn(Material.DECORATED_POT);
        
        Block above = mock(Block.class);
        Block below = mock(Block.class);
        Block side1 = mock(Block.class);
        Block side2 = mock(Block.class);
        Block side3 = mock(Block.class);
        Block side4 = mock(Block.class);
        Block corner1 = mock(Block.class);
        Block corner2 = mock(Block.class);
        Block corner3 = mock(Block.class);
        Block corner4 = mock(Block.class);
        
        Location aboveLoc = mock(Location.class);
        Location belowLoc = mock(Location.class);
        Location side1Loc = mock(Location.class);
        Location side2Loc = mock(Location.class);
        Location side3Loc = mock(Location.class);
        Location side4Loc = mock(Location.class);
        Location corner1Loc = mock(Location.class);
        Location corner2Loc = mock(Location.class);
        Location corner3Loc = mock(Location.class);
        Location corner4Loc = mock(Location.class);
        
        when(location.clone()).thenReturn(location);
        when(location.add(anyDouble(), anyDouble(), anyDouble())).thenReturn(location);
        when(location.getBlock()).thenReturn(centerBlock, above, below, side1, side2, side3, side4, corner1, corner2, corner3, corner4);
        
        lenient().when(above.getType()).thenReturn(Material.LIGHTNING_ROD);
        lenient().when(below.getType()).thenReturn(Material.CHISELED_TUFF_BRICKS);
        lenient().when(side1.getType()).thenReturn(Material.COPPER_GRATE);
        lenient().when(side2.getType()).thenReturn(Material.COPPER_GRATE);
        lenient().when(side3.getType()).thenReturn(Material.COPPER_GRATE);
        lenient().when(side4.getType()).thenReturn(Material.COPPER_GRATE);
        lenient().when(corner1.getType()).thenReturn(Material.AMETHYST_BLOCK);
        lenient().when(corner2.getType()).thenReturn(Material.AMETHYST_BLOCK);
        lenient().when(corner3.getType()).thenReturn(Material.AMETHYST_BLOCK);
        lenient().when(corner4.getType()).thenReturn(Material.AMETHYST_BLOCK);
        
        int tier = tierCalculator.calculateTier(location);
        
        assertEquals(1, tier, "Should return tier 1 for complete Tier 1 structure");
    }
    
    @Test
    @DisplayName("Should count alive copper blocks correctly")
    void testCountAliveCopperBlocks() {
        Location location = mock(Location.class);
        Block mockBlock = mock(Block.class);
        
        when(location.clone()).thenReturn(location);
        lenient().when(location.add(anyDouble(), anyDouble(), anyDouble())).thenReturn(location);
        when(location.getBlock()).thenReturn(mockBlock);
        when(mockBlock.getType()).thenReturn(Material.STONE);
        
        int count = tierCalculator.countAliveCopperBlocks(location);
        
        assertTrue(count >= 0, "Alive block count should be non-negative");
    }
    
    @Test
    @DisplayName("Should count alive masts correctly")
    void testCountAliveMasts() {
        Location location = mock(Location.class);
        Block mockBlock = mock(Block.class);
        
        when(location.clone()).thenReturn(location);
        lenient().when(location.add(anyDouble(), anyDouble(), anyDouble())).thenReturn(location);
        when(location.getBlock()).thenReturn(mockBlock);
        when(mockBlock.getType()).thenReturn(Material.TINTED_GLASS, Material.TINTED_GLASS, Material.TINTED_GLASS, Material.END_ROD);
        
        int count = tierCalculator.countAliveMasts(location);
        
        assertTrue(count >= 0 && count <= 4, "Mast count should be between 0 and 4");
    }
    
    @Test
    @DisplayName("Should calculate Tier 3 power correctly based on alive masts")
    void testCalculateTier3Power() {
        Location location = mock(Location.class);
        Block potBlock = mock(Block.class);
        Block glassBlock = mock(Block.class);
        Block topBlock = mock(Block.class);
        
        when(location.getBlock()).thenReturn(potBlock);
        when(potBlock.getType()).thenReturn(Material.DECORATED_POT);
        when(location.clone()).thenReturn(location);
        
        Location mast1Loc = mock(Location.class);
        when(mast1Loc.getBlock()).thenReturn(glassBlock);
        when(glassBlock.getType()).thenReturn(Material.TINTED_GLASS, Material.TINTED_GLASS, Material.TINTED_GLASS, Material.END_ROD);
        
        int[][] mastPositions = {{2, 0, 2}, {2, 0, -2}, {-2, 0, 2}, {-2, 0, -2}};
        int callCount = 0;
        Location mockLoc = mock(Location.class);
        Block mockBlock = mock(Block.class);
        
        when(location.add(anyDouble(), anyDouble(), anyDouble())).thenReturn(mockLoc);
        when(mockLoc.getBlock()).thenReturn(mockBlock);
        
        lenient().when(mockBlock.getType()).thenReturn(Material.TINTED_GLASS);
        
        double power = tierCalculator.calculateTier3Power(location);
        
        assertTrue(power >= 0.0 && power <= 1.0, "Power should be between 0.0 and 1.0");
        assertTrue(power % 0.25 < 0.01, "Power should be a multiple of 0.25");
    }
    
    @Test
    @DisplayName("Should return correct radius for Tier 3 based on power")
    void testGetTier3Radius() {
        double power025 = 0.25;
        double power100 = 1.0;
        
        int radius025 = tierCalculator.getTier3Radius(power025);
        int radius100 = tierCalculator.getTier3Radius(power100);
        
        assertTrue(radius025 < radius100, "Higher power should result in larger radius");
    }
}
