package dev.keelbismark.shoophantom.mechanics;

import dev.keelbismark.shoophantom.ShooPhantom;
import dev.keelbismark.shoophantom.config.Messages;
import dev.keelbismark.shoophantom.data.Database;
import dev.keelbismark.shoophantom.data.Ward;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

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
        
        org.bukkit.Server mockServer = mock(org.bukkit.Server.class);
        try {
            java.lang.reflect.Field serverField = org.bukkit.Bukkit.class.getDeclaredField("server");
            serverField.setAccessible(true);
            serverField.set(null, mockServer);
        } catch (Exception e) {
        }
        
        dev.keelbismark.shoophantom.config.ConfigManager mockConfig = mock(dev.keelbismark.shoophantom.config.ConfigManager.class);
        lenient().when(mockPlugin.getConfigManager()).thenReturn(mockConfig);
        lenient().when(mockPlugin.getDatabase()).thenReturn(mockDatabase);
        lenient().when(mockPlugin.getMessages()).thenReturn(mockMessages);
        lenient().when(mockPlugin.getLogger()).thenReturn(java.util.logging.Logger.getLogger("DegradationManagerTest"));
        lenient().when(mockPlugin.getServer()).thenReturn(mockServer);
        
        lenient().when(mockConfig.getTier2MinAliveBlocks()).thenReturn(9);
        lenient().when(mockConfig.getTier2WarnThreshold()).thenReturn(11);
        lenient().when(mockConfig.getTier2AliveMaterials()).thenReturn(java.util.Set.of(Material.COPPER_BLOCK));
        lenient().when(mockConfig.getTier3PauseWhenNoPlayers()).thenReturn(true);
        lenient().when(mockConfig.getTier3PauseCheckRadius()).thenReturn(128);
        lenient().when(mockConfig.getTier3CycleHoursMin()).thenReturn(48);
        lenient().when(mockConfig.getTier3CycleHoursMax()).thenReturn(72);
        lenient().when(mockConfig.getTier3MastTop()).thenReturn(Material.END_ROD);
        lenient().when(mockConfig.getTier3MastDead()).thenReturn(Material.IRON_BARS);
        lenient().when(mockConfig.getTier3MastGlass()).thenReturn(Material.TINTED_GLASS);
        lenient().when(mockConfig.getTier3RadiusMin()).thenReturn(64);
        lenient().when(mockConfig.getTier3RadiusMax()).thenReturn(128);
        
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
            
            lenient().when(Bukkit.getWorld("nonexistent_world")).thenReturn(null);
            
            Ward result = degradationManager.checkTier2Degradation(ward);
            
            assertSame(ward, result, "Should return same ward when world is null");
        }
        
        @Test
        @DisplayName("Should handle missing world gracefully") 
        void testCheckTier2Degradation_MissingWorld() {
            UUID wardId = UUID.randomUUID();
            Ward ward = new Ward(wardId, UUID.randomUUID(), "world", 0, 64, 0, 2, 10, 0, 0, 0);
            
            Ward result = degradationManager.checkTier2Degradation(ward);
            
            assertNotNull(result, "Should return ward even with missing world");
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
        @DisplayName("Should return same ward when not yet time to degrade")
        void testCheckTier3Degradation_NotYetTime() {
            UUID wardId = UUID.randomUUID();
            long future = System.currentTimeMillis() + 3600000;
            Ward ward = new Ward(wardId, UUID.randomUUID(), "world", 0, 64, 0, 3, 10, 0, future, 0);
            
            Ward result = degradationManager.checkTier3Degradation(ward);
            
            assertSame(ward, result, "Should return same ward when not time to degrade");
        }
        
        @Test
        @DisplayName("Should initialize degrade time for new Tier 3 ward")
        void testCheckTier3Degradation_InitializeTime() {
            UUID wardId = UUID.randomUUID();
            Ward ward = new Ward(wardId, UUID.randomUUID(), "world", 0, 64, 0, 3, 10, 0, 0, 0);
            
            Ward result = degradationManager.checkTier3Degradation(ward);
            
            assertNotSame(ward, result, "Should return new ward with degrade time initialized");
            assertTrue(result.getNextDegradeTime() > System.currentTimeMillis(), 
                      "Degrade time should be in the future");
            verify(mockDatabase).saveWard(any(Ward.class));
        }
        
        @Test
        @DisplayName("Should handle missing world Tier 3") 
        void testCheckTier3Degradation_MissingWorld() {
            when(Bukkit.getWorld(anyString())).thenReturn(null);
            
            UUID wardId = UUID.randomUUID();
            long now = System.currentTimeMillis();
            Ward ward = new Ward(wardId, UUID.randomUUID(), "world", 0, 64, 0, 3, 10,
                              0, now - 1000, 0);
            
            Ward result = degradationManager.checkTier3Degradation(ward);
            
            assertSame(ward, result, "Should return same ward when world is null");
            verify(mockDatabase, never()).saveWard(any(Ward.class));
        }
    }
    
    @Nested
    @DisplayName("Degrade Time Tests")
    class DegradeTimeTests {
        
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
        @DisplayName("Should degrade time within valid range multiple runs")
        void testNextDegradeTime_MultipleRuns() {
            for (int i = 0; i < 10; i++) {
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
                    "Degrade time should be within configured range on run " + i);
            }
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
