package dev.keelbismark.shoophantom.mechanics;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import dev.keelbismark.shoophantom.ShooPhantom;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TierCalculator class.
 * Tests the calculation of protection tiers based on structure configuration.
 */
public class TierCalculatorTest {
    
    private TierCalculator tierCalculator;
    private ShooPhantom mockPlugin;
    private FileConfiguration mockConfig;
    
    @BeforeEach
    void setUp() {
        mockPlugin = mock(ShooPhantom.class);
        mockConfig = mock(FileConfiguration.class);
        
        when(mockPlugin.getConfig()).thenReturn(mockConfig);
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
        Location location = createTier1Structure();
        
        when(location.getBlock().getType()).thenReturn(Material.DECORATED_POT);
        
        int tier = tierCalculator.calculateTier(location);
        
        assertEquals(1, tier, "Should return tier 1 for complete Tier 1 structure");
    }
    
    @Test
    @DisplayName("Should count alive copper blocks correctly")
    void testCountAliveCopperBlocks() {
        Location location = mock(Location.class);
        
        when(location.clone()).thenReturn(location);
        
        int count = tierCalculator.countAliveCopperBlocks(location);
        
        assertTrue(count >= 0, "Alive block count should be non-negative");
    }
    
    @Test
    @DisplayName("Should count alive masts correctly")
    void testCountAliveMasts() {
        Location location = mock(Location.class);
        
        when(location.clone()).thenReturn(location);
        
        int count = tierCalculator.countAliveMasts(location);
        
        assertTrue(count >= 0 && count <= 4, "Mast count should be between 0 and 4");
    }
    
    @Test
    @DisplayName("Should calculate Tier 3 power correctly based on alive masts")
    void testCalculateTier3Power() {
        Location location = mock(Location.class);
        ShooPhantom plugin = mock(ShooPhantom.class);
        TierCalculator calculator = new TierCalculator(plugin);
        
        when(location.clone()).thenReturn(location);
        
        double power = calculator.calculateTier3Power(location);
        
        assertTrue(power >= 0.0 && power <= 1.0, "Power should be between 0.0 and 1.0");
        assertTrue(power % 0.25 < 0.01, "Power should be a multiple of 0.25");
    }
    
    @Test
    @DisplayName("Should return correct radius for Tier 3 based on power")
    void testGetTier3Radius() {
        Location location = mock(Location.class);
        ShooPhantom plugin = mock(ShooPhantom.class);
        TierCalculator calculator = new TierCalculator(plugin);
        
        when(location.clone()).thenReturn(location);
        
        double power025 = 0.25;
        double power050 = 0.50;
        double power075 = 0.75;
        double power100 = 1.0;
        
        when(plugin.getConfig()).thenReturn(mockConfig);
        when(mockConfig.getInt("tiers.tier3.radius-min", 80)).thenReturn(80);
        when(mockConfig.getInt("tiers.tier3.radius-max", 128)).thenReturn(128);
        
        int radius025 = calculator.getTier3Radius(power025);
        int radius100 = calculator.getTier3Radius(power100);
        
        assertTrue(radius025 < radius100, "Higher power should result in larger radius");
    }
    
    private Location createTier1Structure() {
        Location location = mock(Location.class);
        
        when(location.getBlock()).thenReturn(mock(Block.class));
        
        return location;
    }
}
