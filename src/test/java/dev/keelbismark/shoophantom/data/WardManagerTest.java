package dev.keelbismark.shoophantom.data;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import dev.keelbismark.shoophantom.ShooPhantom;
import dev.keelbismark.shoophantom.config.ConfigManager;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("WardManager Tests")
public class WardManagerTest {
    
    private WardManager wardManager;
    private ShooPhantom mockPlugin;
    private Database mockDatabase;
    private ConfigManager mockConfigManager;
    private Player mockPlayer;
    private World mockWorld;
    private Location mockLocation;
    
    @BeforeEach
    void setUp() {
        mockPlugin = mock(ShooPhantom.class);
        mockDatabase = mock(Database.class);
        mockConfigManager = mock(ConfigManager.class);
        mockPlayer = mock(Player.class);
        mockWorld = mock(World.class);
        mockLocation = mock(Location.class);
        
        org.bukkit.Server mockServer = mock(org.bukkit.Server.class);
        lenient().when(mockPlugin.getServer()).thenReturn(mockServer);
        lenient().when(mockServer.getWorld("world")).thenReturn(mockWorld);
        
        when(mockPlugin.getDatabase()).thenReturn(mockDatabase);
        when(mockPlugin.getConfigManager()).thenReturn(mockConfigManager);
        lenient().when(mockPlugin.getLogger()).thenReturn(java.util.logging.Logger.getLogger("ShooPhantomTest"));
        
        UUID playerUUID = UUID.randomUUID();
        when(mockPlayer.getUniqueId()).thenReturn(playerUUID);
        when(mockPlayer.hasPermission(anyString())).thenReturn(false);
        
        when(mockLocation.getWorld()).thenReturn(mockWorld);
        when(mockLocation.getBlockX()).thenReturn(100);
        when(mockLocation.getBlockY()).thenReturn(64);
        when(mockLocation.getBlockZ()).thenReturn(200);
        when(mockLocation.clone()).thenReturn(mockLocation);
        lenient().when(mockLocation.add(anyDouble(), anyDouble(), anyDouble())).thenReturn(mockLocation);
        when(mockWorld.getName()).thenReturn("world");
        
        when(mockDatabase.loadAllWards()).thenReturn(List.of());
        when(mockDatabase.countWardsByOwner(any())).thenReturn(0);
        when(mockConfigManager.getMaxWardsPerPlayer()).thenReturn(5);
        when(mockConfigManager.getTier1Radius()).thenReturn(48);
        when(mockConfigManager.getTier2Radius()).thenReturn(80);
        when(mockConfigManager.getTier3RadiusMax()).thenReturn(128);
        
        wardManager = new WardManager(mockPlugin);
    }
    
    @Nested
    @DisplayName("Ward Creation Tests")
    class WardCreationTests {
        
        @Test
        @DisplayName("Should create ward when structure is valid and limits not reached")
        void testCreateWard_Success() {
            org.bukkit.block.Block mockBlock = mock(org.bukkit.block.Block.class);
            when(mockBlock.getType()).thenReturn(org.bukkit.Material.DECORATED_POT);
            when(mockLocation.getBlock()).thenReturn(mockBlock);
            when(mockLocation.clone()).thenReturn(mockLocation);
            lenient().when(mockLocation.add(anyDouble(), anyDouble(), anyDouble())).thenReturn(mockLocation);
            
            when(mockConfigManager.getTier1AboveMaterial()).thenReturn(org.bukkit.Material.LIGHTNING_ROD);
            when(mockConfigManager.getTier1BelowMaterial()).thenReturn(org.bukkit.Material.CHISELED_TUFF_BRICKS);
            when(mockConfigManager.getTier1SidesMaterial()).thenReturn(org.bukkit.Material.COPPER_GRATE);
            when(mockConfigManager.getTier1CornersMaterial()).thenReturn(org.bukkit.Material.AMETHYST_BLOCK);
            when(mockConfigManager.getTier2MinAliveBlocks()).thenReturn(9);
            when(mockConfigManager.getTier2AliveMaterials()).thenReturn(java.util.Set.of());
            when(mockConfigManager.getTier3MastGlass()).thenReturn(org.bukkit.Material.TINTED_GLASS);
            when(mockConfigManager.getTier3MastTop()).thenReturn(org.bukkit.Material.END_ROD);
            
            Ward result = wardManager.createWard(mockPlayer, mockLocation);
            
            assertNotNull(result, "Ward should be created successfully");
            assertTrue(result.getTier() >= 1, "Tier should be at least 1 for valid structure");
            assertEquals(mockPlayer.getUniqueId(), result.getOwnerUUID(), "Owner should match");
            assertEquals(100, result.getX(), "X coordinate should match");
            assertEquals(64, result.getY(), "Y coordinate should match");
            assertEquals(200, result.getZ(), "Z coordinate should match");
            
            verify(mockDatabase).saveWard(any(Ward.class));
        }
        
        @Test
        @DisplayName("Should return null when ward limit is reached")
        void testCreateWard_LimitReached() {
            when(mockDatabase.countWardsByOwner(mockPlayer.getUniqueId())).thenReturn(5);
            when(mockConfigManager.getMaxWardsPerPlayer()).thenReturn(5);
            
            Ward result = wardManager.createWard(mockPlayer, mockLocation);
            
            assertNull(result, "Ward should not be created when limit reached");
            verify(mockDatabase, never()).saveWard(any(Ward.class));
        }
        
        @Test
        @DisplayName("Should allow bypassing limits with permission")
        void testCreateWard_BypassLimit() {
            when(mockPlayer.hasPermission("shoo.bypass")).thenReturn(true);
            when(mockDatabase.countWardsByOwner(mockPlayer.getUniqueId())).thenReturn(10);
            when(mockConfigManager.getMaxWardsPerPlayer()).thenReturn(5);
            
            org.bukkit.block.Block mockBlock = mock(org.bukkit.block.Block.class);
            when(mockBlock.getType()).thenReturn(org.bukkit.Material.DECORATED_POT);
            when(mockLocation.getBlock()).thenReturn(mockBlock);
            when(mockLocation.clone()).thenReturn(mockLocation);
            lenient().when(mockLocation.add(anyDouble(), anyDouble(), anyDouble())).thenReturn(mockLocation);
            
            when(mockConfigManager.getTier1AboveMaterial()).thenReturn(org.bukkit.Material.LIGHTNING_ROD);
            when(mockConfigManager.getTier1BelowMaterial()).thenReturn(org.bukkit.Material.CHISELED_TUFF_BRICKS);
            when(mockConfigManager.getTier1SidesMaterial()).thenReturn(org.bukkit.Material.COPPER_GRATE);
            when(mockConfigManager.getTier1CornersMaterial()).thenReturn(org.bukkit.Material.AMETHYST_BLOCK);
            when(mockConfigManager.getTier2MinAliveBlocks()).thenReturn(9);
            when(mockConfigManager.getTier2AliveMaterials()).thenReturn(java.util.Set.of());
            when(mockConfigManager.getTier3MastGlass()).thenReturn(org.bukkit.Material.TINTED_GLASS);
            when(mockConfigManager.getTier3MastTop()).thenReturn(org.bukkit.Material.END_ROD);
            
            Ward result = wardManager.createWard(mockPlayer, mockLocation);
            
            assertNotNull(result, "Ward should be created with bypass permission");
            verify(mockDatabase).saveWard(any(Ward.class));
        }
    }
    
    @Nested
    @DisplayName("Ward Retrieval Tests")
    class WardRetrievalTests {
        
        @Test
        @DisplayName("Should retrieve ward by location")
        void testGetWardByLocation_Found() {
            UUID wardId = UUID.randomUUID();
            UUID ownerUUID = UUID.randomUUID();
            Ward ward = new Ward(wardId, ownerUUID, "world", 100, 64, 200, 1, 50, 0, 0, System.currentTimeMillis());
            
            when(mockDatabase.loadAllWards()).thenReturn(List.of(ward));
            
            Location location = mock(Location.class);
            when(location.getWorld()).thenReturn(mockWorld);
            when(location.getWorld().getName()).thenReturn("world");
            when(location.getBlockX()).thenReturn(100);
            when(location.getBlockY()).thenReturn(64);
            when(location.getBlockZ()).thenReturn(200);
            
            Ward result = wardManager.getWardByLocation(location);
            
            assertNotNull(result, "Ward should be found");
            assertEquals(wardId, result.getId(), "Ward ID should match");
        }
        
        @Test
        @DisplayName("Should return null when ward not found at location")
        void testGetWardByLocation_NotFound() {
            when(mockDatabase.loadAllWards()).thenReturn(List.of());
            
            Ward result = wardManager.getWardByLocation(mockLocation);
            
            assertNull(result, "Ward should not be found");
        }
        
        @Test
        @DisplayName("Should retrieve ward by ID")
        void testGetWardById_Found() {
            UUID wardId = UUID.randomUUID();
            Ward ward = new Ward(wardId, UUID.randomUUID(), "world", 100, 64, 200, 1, 50, 0, 0, System.currentTimeMillis());
            
            when(mockDatabase.loadAllWards()).thenReturn(List.of(ward));
            
            Ward result = wardManager.getWard(wardId);
            
            assertNotNull(result, "Ward should be found");
            assertEquals(wardId, result.getId(), "Ward ID should match");
        }
        
        @Test
        @DisplayName("Should retrieve all wards for a player")
        void testGetWardsByPlayer() {
            UUID playerUUID = UUID.randomUUID();
            UUID wardId1 = UUID.randomUUID();
            UUID wardId2 = UUID.randomUUID();
            
            Ward ward1 = new Ward(wardId1, playerUUID, "world", 100, 64, 200, 1, 50, 0, 0, 0);
            Ward ward2 = new Ward(wardId2, playerUUID, "world", 150, 70, 250, 2, 30, 0, 0, 0);
            
            when(mockDatabase.loadAllWards()).thenReturn(List.of(ward1, ward2));
            
            List<Ward> result = wardManager.getWardsByPlayer(playerUUID);
            
            assertEquals(2, result.size(), "Should return 2 wards for the player");
            assertTrue(result.contains(ward1), "Should contain ward1");
            assertTrue(result.contains(ward2), "Should contain ward2");
        }
    }
    
    @Nested
    @DisplayName("Protection Tests")
    class ProtectionTests {
        
        @Test
        @DisplayName("Should detect location is protected")
        void testIsProtected_True() {
            UUID wardId = UUID.randomUUID();
            long now = System.currentTimeMillis();
            Ward ward = new Ward(wardId, UUID.randomUUID(), "world", 0, 64, 0, 2, 50,
                              now + 100000, 0, now);
            
            Location wardLocation = mock(Location.class);
            when(wardLocation.getWorld()).thenReturn(mockWorld);
            lenient().when(wardLocation.distanceSquared(any(Location.class))).thenReturn(1600.0);
            
            when(mockDatabase.loadAllWards()).thenReturn(List.of(ward));
            when(mockConfigManager.getTier2Radius()).thenReturn(80);
            when(mockPlugin.getServer().getWorld("world")).thenReturn(mockWorld);
            lenient().when(mockLocation.getWorld().getName()).thenReturn("world");
            
            boolean result = wardManager.isProtected(mockLocation, 1);
            
            assertTrue(result, "Location should be protected");
        }
        
        @Test
        @DisplayName("Should detect location is not protected")
        void testIsProtected_False() {
            when(mockDatabase.loadAllWards()).thenReturn(List.of());
            
            boolean result = wardManager.isProtected(mockLocation, 1);
            
            assertFalse(result, "Location should not be protected without wards");
        }
        
        @Test
        @DisplayName("Should not protect from disabled ward")
        void testIsProtected_InactiveWard() {
            UUID wardId = UUID.randomUUID();
            Ward ward = new Ward(wardId, UUID.randomUUID(), "world", 0, 64, 0, 2, 0, 0, 0, 0);
            
            when(mockDatabase.loadAllWards()).thenReturn(List.of(ward));
            
            boolean result = wardManager.isProtected(mockLocation, 1);
            
            assertFalse(result, "Inactive ward should not protect");
        }
    }
    
    @Nested
    @DisplayName("Radius Tests")
    class RadiusTests {
        
        @Test
        @DisplayName("Should return correct radius for Tier 1")
        void testGetRadius_Tier1() {
            when(mockConfigManager.getTier1Radius()).thenReturn(48);
            
            int radius = wardManager.getRadius(1);
            
            assertEquals(48, radius, "Tier 1 radius should be 48");
        }
        
        @Test
        @DisplayName("Should return correct radius for Tier 2")
        void testGetRadius_Tier2() {
            when(mockConfigManager.getTier2Radius()).thenReturn(80);
            
            int radius = wardManager.getRadius(2);
            
            assertEquals(80, radius, "Tier 2 radius should be 80");
        }
        
        @Test
        @DisplayName("Should return correct radius for Tier 3")
        void testGetRadius_Tier3() {
            when(mockConfigManager.getTier3RadiusMax()).thenReturn(128);
            
            int radius = wardManager.getRadius(3);
            
            assertEquals(128, radius, "Tier 3 radius should be 128");
        }
        
        @Test
        @DisplayName("Should return 0 for invalid tier")
        void testGetRadius_InvalidTier() {
            int radius = wardManager.getRadius(0);
            
            assertEquals(0, radius, "Invalid tier should return 0 radius");
        }
    }
}
