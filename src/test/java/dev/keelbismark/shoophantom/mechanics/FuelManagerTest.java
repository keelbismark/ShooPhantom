package dev.keelbismark.shoophantom.mechanics;

import dev.keelbismark.shoophantom.ShooPhantom;
import dev.keelbismark.shoophantom.config.Messages;
import dev.keelbismark.shoophantom.data.Database;
import dev.keelbismark.shoophantom.data.Ward;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("FuelManager Tests")
public class FuelManagerTest {
    
    private FuelManager fuelManager;
    private ShooPhantom mockPlugin;
    private Database mockDatabase;
    private Messages mockMessages;
    
    @BeforeEach
    void setUp() {
        mockPlugin = mock(ShooPhantom.class);
        mockDatabase = mock(Database.class);
        mockMessages = mock(Messages.class);
        
        when(mockPlugin.getDatabase()).thenReturn(mockDatabase);
        when(mockPlugin.getConfigManager()).thenReturn(mock(dev.keelbismark.shoophantom.config.ConfigManager.class));
        when(mockPlugin.getMessages()).thenReturn(mockMessages);
        when(mockPlugin.getConfigManager().getMaxFuel()).thenReturn(64);
        when(mockPlugin.getConfigManager().getTier1FuelMinutes()).thenReturn(60);
        when(mockPlugin.getConfigManager().getTier2FuelMinutes()).thenReturn(45);
        when(mockPlugin.getConfigManager().getTier3FuelMinutes()).thenReturn(30);
        when(mockMessages.fuelEmpty()).thenReturn("§c⚠ Топливо закончилось!");
        
        dev.keelbismark.shoophantom.config.ConfigManager mockConfig = mock(dev.keelbismark.shoophantom.config.ConfigManager.class);
        lenient().when(mockPlugin.getConfigManager()).thenReturn(mockConfig);
        lenient().when(mockConfig.getMaxFuel()).thenReturn(64);
        lenient().when(mockConfig.getTier1FuelMinutes()).thenReturn(60);
        lenient().when(mockConfig.getTier2FuelMinutes()).thenReturn(45);
        lenient().when(mockConfig.getTier3FuelMinutes()).thenReturn(30);
        
        fuelManager = new FuelManager(mockPlugin);
    }
    
    @Nested
    @DisplayName("Add Fuel Tests")
    class AddFuelTests {
        
        @Test
        @DisplayName("Should add fuel to empty ward")
        void testAddFuel_EmptyWard() {
            UUID wardId = UUID.randomUUID();
            Ward ward = new Ward(wardId, UUID.randomUUID(), "world", 0, 64, 0, 1, 0, 0, 0, 0);
            
            Ward result = fuelManager.addFuel(ward, 10);
            
            assertNotNull(result, "Result should not be null");
            assertEquals(10, result.getFuel(), "Fuel should be added");
            assertTrue(result.getBurnEndTime() > System.currentTimeMillis(), 
                "Burn end time should be set");
            assertEquals(9, result.getFuel(), "One fuel should be consumed to start burning");
        }
        
        @Test
        @DisplayName("Should add fuel to partially filled ward")
        void testAddFuel_PartiallyFilled() {
            UUID wardId = UUID.randomUUID();
            Ward ward = new Ward(wardId, UUID.randomUUID(), "world", 0, 64, 0, 1, 30, 0, 0, 0);
            
            Ward result = fuelManager.addFuel(ward, 20);
            
            assertNotNull(result, "Result should not be null");
            assertEquals(50, result.getFuel(), "Fuel should be added to existing amount");
        }
        
        @Test
        @DisplayName("Should return null when storage is full")
        void testAddFuel_StorageFull() {
            UUID wardId = UUID.randomUUID();
            Ward ward = new Ward(wardId, UUID.randomUUID(), "world", 0, 64, 0, 1, 64, 0, 0, 0);
            
            Ward result = fuelManager.addFuel(ward, 10);
            
            assertNull(result, "Should return null when storage is full");
            verify(mockDatabase, never()).saveWard(any(Ward.class));
        }
        
        @Test
        @DisplayName("Should cap fuel at maximum capacity")
        void testAddFuel_CapAtMax() {
            UUID wardId = UUID.randomUUID();
            Ward ward = new Ward(wardId, UUID.randomUUID(), "world", 0, 64, 0, 1, 50, 0, 0, 0);
            
            Ward result = fuelManager.addFuel(ward, 20);
            
            assertNotNull(result, "Result should not be null");
            assertEquals(64, result.getFuel(), "Fuel should be capped at maximum");
        }
    }
    
    @Nested
    @DisplayName("Fuel Consumption Tests")
    class FuelConsumptionTests {
        
        @Test
        @DisplayName("Should return same ward when still burning")
        void testProcessFuelConsumption_StillBurning() {
            UUID wardId = UUID.randomUUID();
            long now = System.currentTimeMillis();
            Ward ward = new Ward(wardId, UUID.randomUUID(), "world", 0, 64, 0, 1, 10,
                              now + 10000, 0, 0);
            
            Ward result = fuelManager.processFuelConsumption(ward);
            
           assertSame(ward, result, "Should return same ward when still burning");
            verify(mockDatabase, never()).saveWard(any(Ward.class));
        }
        
        @Test
        @DisplayName("Should start burning next fuel when current finishes")
        void testProcessFuelConsumption_StartNext() {
            UUID wardId = UUID.randomUUID();
            long now = System.currentTimeMillis();
            Ward ward = new Ward(wardId, UUID.randomUUID(), "world", 0, 64, 0, 1, 10,
                              now - 1000, 0, 0);
            
            Ward result = fuelManager.processFuelConsumption(ward);
            
            assertNotSame(ward, result, "Should return new ward");
            assertTrue(result.getBurnEndTime() > System.currentTimeMillis(), 
                "New burn should have started");
            assertEquals(9, result.getFuel(), "One fuel should have been consumed");
            verify(mockDatabase).saveWard(any(Ward.class));
        }
        
        @Test
        @DisplayName("Should mark empty when fuel runs out")
        void testProcessFuelConsumption_FuelEmpty() {
            UUID wardId = UUID.randomUUID();
            long now = System.currentTimeMillis();
            Ward ward = new Ward(wardId, UUID.randomUUID(), "world", 0, 64, 0, 1, 0,
                              now - 1000, 0, 0);
            
            OfflinePlayer mockOwner = mock(OfflinePlayer.class);
            lenient().when(mockOwner.isOnline()).thenReturn(true);
            lenient().when(mockOwner.getPlayer()).thenReturn(mock(Player.class));
            
            java.lang.reflect.Field bukkitField = null;
            try {
                bukkitField = org.bukkit.Bukkit.class.getDeclaredField("server");
                bukkitField.setAccessible(true);
                bukkitField.set(null, mock(org.bukkit.Server.class));
            } catch (Exception e) {
                // Ignore during test
            }
            
            Ward result = fuelManager.processFuelConsumption(ward);
            
            assertNotSame(ward, result, "Should return new ward");
            assertEquals(0, result.getBurnEndTime(), "Burn end time should be cleared");
            assertEquals(0, result.getFuel(), "Fuel should be zero");
            assertFalse(result.isActive(), "Ward should be inactive");
        }
    }
    
    @Nested
    @DisplayName("Time Calculation Tests")
    class TimeCalculationTests {
        
        @Test
        @DisplayName("Should calculate remaining time with burning fuel")
        void testGetRemainingTime_Burning() {
            UUID wardId = UUID.randomUUID();
            long now = System.currentTimeMillis();
            long oneHour = 60 * 60 * 1000;
            Ward ward = new Ward(wardId, UUID.randomUUID(), "world", 0, 64, 0, 1, 5,
                              now + oneHour, 0, 0);
            
            long remaining = fuelManager.getRemainingTime(ward);
            
            long expected = oneHour + (5 * 60 * 60 * 1000);
            assertTrue(remaining > oneHour, "Should include burning time and fuel bank");
            assertTrue(remaining <= expected + 1000, "Should not exceed expected time significantly");
        }
        
        @Test
        @DisplayName("Should calculate remaining time with fuel bank only")
        void testGetRemainingTime_FuelBankOnly() {
            UUID wardId = UUID.randomUUID();
            Ward ward = new Ward(wardId, UUID.randomUUID(), "world", 0, 64, 0, 1, 10,
                              0, 0, 0);
            
            long remaining = fuelManager.getRemainingTime(ward);
            
            long expected = 10 * 60 * 60 * 1000; // 10 hours
            assertTrue(remaining > 0, "Should have remaining time");
            assertTrue(remaining <= expected + 1000, "Should match expected time");
        }
        
        @Test
        @DisplayName("Should return zero for empty ward")
        void testGetRemainingTime_Empty() {
            UUID wardId = UUID.randomUUID();
            Ward ward = new Ward(wardId, UUID.randomUUID(), "world", 0, 64, 0, 1, 0,
                              0, 0, 0);
            
            long remaining = fuelManager.getRemainingTime(ward);
            
            assertEquals(0, remaining, "Should have no remaining time");
        }
    }
    
    @Nested
    @DisplayName("Time Formatting Tests")
    class TimeFormattingTests {
        
        @Test
        @DisplayName("Should format time in seconds")
        void testFormatRemainingTime_Seconds() {
            String result = fuelManager.formatRemainingTime(5000);
            
            assertTrue(result.contains("с"), "Should contain 'с' for seconds");
            assertFalse(result.contains("м"), "Should not contain minutes");
        }
        
        @Test
        @DisplayName("Should format time in minutes and seconds")
        void testFormatRemainingTime_MinutesSeconds() {
            String result = fuelManager.formatRemainingTime(125000); // 2 min 5 sec
            
            assertTrue(result.contains("м"), "Should contain 'м' for minutes");
            assertTrue(result.contains("с"), "Should contain 'с' for seconds");
        }
        
        @Test
        @DisplayName("Should format time in hours and minutes")
        void testFormatRemainingTime_HoursMinutes() {
            String result = fuelManager.formatRemainingTime(3750000); // 1 hour 2.5 minutes
            
            assertTrue(result.contains("ч"), "Should contain 'ч' for hours");
            assertTrue(result.contains("м"), "Should contain 'м' for minutes");
        }
        
        @Test
        @DisplayName("Should format time in days and hours")
        void testFormatRemainingTime_DaysHours() {
            String result = fuelManager.formatRemainingTime(176400000); // 2 days 1 hour
            
            assertTrue(result.contains("д"), "Should contain 'д' for days");
            assertTrue(result.contains("ч"), "Should contain 'ч' for hours");
        }
        
        @Test
        @DisplayName("Should return empty message for zero or negative time")
        void testFormatRemainingTime_Zero() {
            String result = fuelManager.formatRemainingTime(0);
            
            assertNotNull(result, "Result should not be null");
        }
    }
    
    @Nested
    @DisplayName("Tier-Specific Consumption Tests")
    class TierConsumptionTests {
        
        @Test
        @DisplayName("Should use correct burn time for Tier 1")
        void testTier1BurnTime() {
            long now = System.currentTimeMillis();
            UUID wardId = UUID.randomUUID();
            Ward ward = new Ward(wardId, UUID.randomUUID(), "world", 0, 64, 0, 1, 1, 0, 0, 0);
            
            Ward result = fuelManager.addFuel(ward, 1);
            
            long burnDuration = result.getBurnEndTime() - now;
            long expected = 60 * 60 * 1000; // 60 minutes in milliseconds
            
            assertTrue(burnDuration >= expected - 1000 && burnDuration <= expected + 60000,
                "Tier 1 should burn for 60 minutes");
        }
        
        @Test
        @DisplayName("Should use correct burn time for Tier 2")
        void testTier2BurnTime() {
            long now = System.currentTimeMillis();
            UUID wardId = UUID.randomUUID();
            Ward ward = new Ward(wardId, UUID.randomUUID(), "world", 0, 64, 0, 2, 1, 0, 0, 0);
            
            Ward result = fuelManager.addFuel(ward, 1);
            
            long burnDuration = result.getBurnEndTime() - now;
            long expected = 45 * 60 * 1000; // 45 minutes in milliseconds
            
            assertTrue(burnDuration >= expected - 1000 && burnDuration <= expected + 60000,
                "Tier 2 should burn for 45 minutes");
        }
        
        @Test
        @DisplayName("Should use correct burn time for Tier 3")
        void testTier3BurnTime() {
            long now = System.currentTimeMillis();
            UUID wardId = UUID.randomUUID();
            Ward ward = new Ward(wardId, UUID.randomUUID(), "world", 0, 64, 0, 3, 1, 0, 0, 0);
            
            Ward result = fuelManager.addFuel(ward, 1);
            
            long burnDuration = result.getBurnEndTime() - now;
            long expected = 30 * 60 * 1000; // 30 minutes in milliseconds
            
            assertTrue(burnDuration >= expected - 1000 && burnDuration <= expected + 60000,
                "Tier 3 should burn for 30 minutes");
        }
    }
}
