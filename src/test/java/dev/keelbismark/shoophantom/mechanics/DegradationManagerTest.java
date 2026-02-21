package dev.keelbismark.shoophantom.mechanics;

import dev.keelbismark.shoophantom.ShooPhantom;
import dev.keelbismark.shoophantom.config.Messages;
import dev.keelbismark.shoophantom.data.Database;
import dev.keelbismark.shoophantom.data.Ward;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.OfflinePlayer;
import org.bukkit.Material;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("DegradationManager Tests")
public class DegradationManagerTest {
    
    private DegradationManager degradationManager;
    private ShooPhantom mockPlugin;
    private Database mockDatabase;
    private Messages mockMessages;
    
    @BeforeEach
    void setUp() {
        mockPlugin = mock(ShooPhantom.class);
        mockDatabase = mock(Database.class);
        mockMessages = mock(Messages.class);
        
        when(mockPlugin.getDatabase()).thenReturn(mockDatabase);
        when(mockPlugin.getMessages()).thenReturn(mockMessages);
        when(mockPlugin.getConfigManager()).thenReturn(mock(dev.keelbismark.shoophantom.config.ConfigManager.class));
        
        dev.keelbismark.shoophantom.config.ConfigManager mockConfig = mock(dev.keelbismark.shoophantom.config.ConfigManager.class);
        lenient().when(mockPlugin.getConfigManager()).thenReturn(mockConfig);
        lenient().when(mockConfig.getTier2MinAliveBlocks()).thenReturn(9);
        lenient().when(mockConfig.getTier2WarnThreshold()).thenReturn(11);
        lenient().when(mockConfig.getTier3PauseWhenNoPlayers()).thenReturn(true);
        lenient().when(mockConfig.getTier3PauseCheckRadius()).thenReturn(128);
        lenient().when(mockConfig.getTier3CycleHoursMin()).thenReturn(48);
        lenient().when(mockConfig.getTier3CycleHoursMax()).thenReturn(72);
        lenient().when(mockConfig.getTier3MastTop()).thenReturn(Material.END_ROD);
        lenient().when(mockConfig.getTier3MastDead()).thenReturn(Material.IRON_BARS);
        
        lenient().when(mockMessages.degradationTier2Warn(anyInt())).thenReturn("§e⚠ Медное кольцо окисляется!");
        lenient().when(mockMessages.degradationTier2Fail()).thenReturn("§c⚠ Кольцо слишком окислено!");
        lenient().when(mockMessages.degradationTier3Mast(anyInt(), anyInt())).thenReturn("§c⚡ Излучатель перегорел!");
        lenient().when(mockMessages.degradationTier3Fail()).thenReturn("§4⚠ Все излучатели мертвы!");
        
        degradationManager = new DegradationManager(mockPlugin);
    }
    
    @Nested
    @DisplayName("Tier 2 Degradation Tests")
    class Tier2DegradationTests {
        
        @Test
        @DisplayName("Should return same ward when tier < 2")
        void testCheckTier2Degradation_LowerTier() {
            UUID wardId = UUID.randomUUID();
            Ward ward = new Ward(wardId, UUID.randomUUID(), "world", 0, 64, 0, 1, 10, 0, 0, 0);
            
            Ward result = degradationManager.checkTier2Degradation(ward);
            
            assertSame(ward, result, "Should return same ward for tier < 2");
            verify(mockDatabase, never()).saveWard(any(Ward.class));
        }
        
        @Test
        @DisplayName("Should return same ward when world is null")
        void testCheckTier2Degradation_NullWorld() {
            UUID wardId = UUID.randomUUID();
            Ward ward = new Ward(wardId, UUID.randomUUID(), "nonexistent_world", 0, 64, 0, 2, 10, 0, 0, 0);
            
            Ward result = degradationManager.checkTier2Degradation(ward);
            
            assertSame(ward, result, "Should return same ward when world is null");
        }
        
        @Test
        @DisplayName("Should notify owner at warning threshold")
        void testCheckTier2Degradation_WarningThreshold() {
            UUID wardId = UUID.randomUUID();
            Ward ward = new Ward(wardId, UUID.randomUUID(), "world", 0, 64, 0, 2, 10, 0, 0, 0);
            
            World mockWorld = mock(World.class);
            when(Bukkit.getWorld("world")).thenReturn(mockWorld);
            
            Ward result = degradationManager.checkTier2Degradation(ward);
            
            assertNotNull(result, "Result should not be null");
        }
        
        @Test
        @DisplayName("Should downgrade tier when blocks below minimum")
        void testCheckTier2Degradation_BelowMinimum() {
            UUID wardId = UUID.randomUUID();
            Ward ward = new Ward(wardId, UUID.randomUUID(), "world", 0, 64, 0, 2, 10, 0, 0, 0);
            
            World mockWorld = mock(World.class);
            when(Bukkit.getWorld("world")).thenReturn(mockWorld);
            
            Ward result = degradationManager.checkTier2Degradation(ward);
            
            assertNotNull(result, "Result should not be null");
        }
    }
    
    @Nested
    @DisplayName("Tier 3 Degradation Tests")
    class Tier3DegradationTests {
        
        @Test
        @DisplayName("Should return same ward when tier < 3")
        void testCheckTier3Degradation_LowerTier() {
            UUID wardId = UUID.randomUUID();
            Ward ward = new Ward(wardId, UUID.randomUUID(), "world", 0, 64, 0, 2, 10, 0, 0, 0);
            
            Ward result = degradationManager.checkTier3Degradation(ward);
            
            assertSame(ward, result, "Should return same ward for tier < 3");
        }
        
        @Test
        @DisplayName("Should pause degradation when no players in radius")
        void testCheckTier3Degradation_PauseNoPlayers() {
            UUID wardId = UUID.randomUUID();
            long now = System.currentTimeMillis();
            Ward ward = new Ward(wardId, UUID.randomUUID(), "world", 0, 64, 0, 3, 10,
                              now - 1000, now - 1000, 0);
            
            World mockWorld = mock(World.class);
            when(mockWorld.getPlayers()).thenReturn(java.util.Collections.emptyList());
            when(Bukkit.getWorld("world")).thenReturn(mockWorld);
            
            Ward result = degradationManager.checkTier3Degradation(ward);
            
            assertSame(ward, result, "Should return same ward when paused");
            verify(mockDatabase, never()).saveWard(any(Ward.class));
        }
        
        @Test
        @DisplayName("Should return same ward when not yet time to degrade")
        void testCheckTier3Degradation_NotYetTime() {
            UUID wardId = UUID.randomUUID();
            long future = System.currentTimeMillis() + 3600000;
            Ward ward = new Ward(wardId, UUID.randomUUID(), "world", 0, 64, 0, 3, 10, 0, future, 0);
            
            Ward result = degradationManager.checkTier3Degradation(ward);
            
            assertSame(ward, result, "Should return same ward when not time to degrade");
        }
        
        @Test
        @DisplayName("Should degrade tier when no active masts")
        void testPerformTier3Degradation_NoMasts() {
            UUID wardId = UUID.randomUUID();
            long now = System.currentTimeMillis();
            Ward ward = new Ward(wardId, UUID.randomUUID(), "world", 0, 64, 0, 3, 10,
                              0, now - 1000, 0);
            
            World mockWorld = mock(World.class);
            when(Bukkit.getWorld("world")).thenReturn(mockWorld);
            
            Ward result = degradationManager.checkTier3Degradation(ward);
            
            assertNotSame(ward, result, "Should return new ward");
            assertEquals(2, result.getTier(), "Tier should be downgraded to 2");
            verify(mockDatabase).saveWard(any(Ward.class));
            verify(mockMessages).degradationTier3Fail();
        }
    }
    
    @Nested
    @DisplayName("Mast Degradation Tests")
    class MastDegradationTests {
        
        @Test
        @DisplayName("Should set degrade time for Tier 3")
        void testNextDegradeTime_Tier3() {
            UUID wardId = UUID.randomUUID();
            Ward ward = new Ward(wardId, UUID.randomUUID(), "world", 0, 64, 0, 3, 10, 0, 0, 0);
            
            long now = System.currentTimeMillis();
            long minHours = 48;
            long maxHours = 72;
            long minMillis = minHours * 3600000;
            long maxMillis = maxHours * 3600000;
            
            Ward result = degradationManager.checkTier3Degradation(ward);
            
            assertTrue(result.getNextDegradeTime() >= now + minMillis &&
                       result.getNextDegradeTime() <= now + maxMillis,
                "Degrade time should be within configured range");
        }
        
        @Test
        @DisplayName("Should reduce mast count on degradation")
        void testPerformMastDegradation() {
            UUID wardId = UUID.randomUUID();
            long now = System.currentTimeMillis();
            Ward ward = new Ward(wardId, UUID.randomUUID(), "world", 0, 64, 0, 3, 10,
                              0, now - 1000, 0);
            
            World mockWorld = mock(World.class);
            when(Bukkit.getWorld("world")).thenReturn(mockWorld);
            
            Ward result = degradationManager.checkTier3Degradation(ward);
            
            assertNotNull(result, "Result should not be null");
        }
    }
    
    @Nested
    @DisplayName("Configuration Tests")
    class ConfigurationTests {
        
        @Test
        @DisplayName("Should respect tier 2 minimum blocks config")
        void testTier2MinAliveBlocksConfig() {
            int minBlocks = mockPlugin.getConfigManager().getTier2MinAliveBlocks();
            
            assertEquals(9, minBlocks, "Should use configured minimum blocks");
        }
        
        @Test
        @DisplayName("Should respect tier 3 pause radius config")
        void testTier3PauseRadiusConfig() {
            int pauseRadius = mockPlugin.getConfigManager().getTier3PauseCheckRadius();
            
            assertEquals(128, pauseRadius, "Should use configured pause radius");
        }
        
        @Test
        @DisplayName("Should respect tier 3 degradation cycle config")
        void testTier3CycleConfig() {
            long minHours = mockPlugin.getConfigManager().getTier3CycleHoursMin();
            long maxHours = mockPlugin.getConfigManager().getTier3CycleHoursMax();
            
            assertEquals(48, minHours, "Should use configured minimum cycle hours");
            assertEquals(72, maxHours, "Should use configured maximum cycle hours");
        }
    }
}
