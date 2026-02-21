package dev.keelbismark.shoophantom.data;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import dev.keelbismark.shoophantom.ShooPhantom;
import dev.keelbismark.shoophantom.mechanics.TierCalculator;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages all wards in the plugin.
 * Handles creation, storage, retrieval, and querying of protection wards.
 */
public class WardManager {
    
    private final ShooPhantom plugin;
    private final Map<UUID, Ward> wards; // Кэш всех оберегов (ID -> Ward)
    private final Map<String, Set<UUID>> wardsByWorld; // Обереги по мирам (worldName -> Set<wardID>)
    private final TierCalculator tierCalculator;
    
    public WardManager(ShooPhantom plugin) {
        this.plugin = plugin;
        this.wards = new ConcurrentHashMap<>();
        this.wardsByWorld = new ConcurrentHashMap<>();
        this.tierCalculator = new TierCalculator(plugin);
        
        loadAllWards();
    }
    
    /**
     * Загрузка всех оберегов из БД
     */
    private void loadAllWards() {
        List<Ward> loadedWards = plugin.getDatabase().loadAllWards();
        
        for (Ward ward : loadedWards) {
            wards.put(ward.getId(), ward);
            
            wardsByWorld.computeIfAbsent(ward.getWorld(), k -> ConcurrentHashMap.newKeySet())
                    .add(ward.getId());
        }
        
        plugin.getLogger().info("Загружено " + wards.size() + " оберегов из базы данных");
    }
    
    /**
     * Creates a new ward for the specified player at the given location.
     * Checks player limits, calculates tier based on structure, and validates permissions.
     * @param player The player creating the ward
     * @param location The location where the ward is being created
     * @return The created ward, or null if creation failed (limit reached, invalid structure, etc.)
     */
    public Ward createWard(Player player, Location location) {
        // Проверка лимита
        int maxWards = plugin.getConfigManager().getMaxWardsPerPlayer();
        if (maxWards > 0 && !player.hasPermission("shoo.bypass")) {
            int currentWards = plugin.getDatabase().countWardsByOwner(player.getUniqueId());
            if (currentWards >= maxWards) {
                return null; // Лимит достигнут
            }
        }
        
        // Расчет Tier
        int tier = tierCalculator.calculateTier(location);
        
        if (tier == 0) {
            return null; // Структура невалидна
        }
        
        // Создание оберега
        Ward ward = new Ward(player.getUniqueId(), location, tier);
        
        // Инициализация таймера деградации для Tier 3
        if (tier == 3) {
            long cycleMin = plugin.getConfigManager().getTier3CycleHoursMin() * 3600000L;
            long cycleMax = plugin.getConfigManager().getTier3CycleHoursMax() * 3600000L;
            long nextDegrade = System.currentTimeMillis() +
                    cycleMin + new Random().nextLong(cycleMax - cycleMin);
            ward = ward.withNextDegradeTime(nextDegrade);
        }

        // Сохранение
        wards.put(ward.getId(), ward);
        wardsByWorld.computeIfAbsent(location.getWorld().getName(), k -> ConcurrentHashMap.newKeySet())
                .add(ward.getId());
        plugin.getDatabase().saveWard(ward);

        return ward;
    }
    
    /**
     * Получить оберег по локации
     */
    public Ward getWardByLocation(Location location) {
        String worldName = location.getWorld().getName();
        Set<UUID> worldWards = wardsByWorld.get(worldName);
        
        if (worldWards == null) {
            return null;
        }
        
        for (UUID id : worldWards) {
            Ward ward = wards.get(id);
            if (ward != null && 
                ward.getX() == location.getBlockX() &&
                ward.getY() == location.getBlockY() &&
                ward.getZ() == location.getBlockZ()) {
                return ward;
            }
        }
        
        return null;
    }
    
    /**
     * Получить оберег по ID
     */
    public Ward getWard(UUID id) {
        return wards.get(id);
    }
    
    /**
      * Получить все обереги игрока
      */
    public List<Ward> getWardsByPlayer(UUID playerUUID) {
        List<Ward> result = new ArrayList<>();
        
        for (Ward ward : wards.values()) {
            if (ward.getOwnerUUID().equals(playerUUID)) {
                result.add(ward);
            }
        }
        
        return result;
    }
    
    /**
      * Получить все обереги в мире
      */
    public List<Ward> getWardsByWorld(String worldName) {
        return getFilteredWardsByWorld(worldName, null);
    }
    
    /**
      * Получить все активные обереги в мире
      */
    public List<Ward> getActiveWardsByWorld(String worldName) {
        return getFilteredWardsByWorld(worldName, Ward::isActive);
    }
    
    /**
      * Общий метод для фильтрации оберегов по миру
      * @param worldName название мира
      * @param filter фильтр (null = без фильтра)
      * @return список оберегов
      */
    private List<Ward> getFilteredWardsByWorld(String worldName, java.util.function.Predicate<Ward> filter) {
        List<Ward> result = new ArrayList<>();
        Set<UUID> worldWards = wardsByWorld.get(worldName);
        
        if (worldWards != null) {
            for (UUID id : worldWards) {
                Ward ward = wards.get(id);
                if (ward != null && (filter == null || filter.test(ward))) {
                    result.add(ward);
                }
            }
        }
        
        return result;
    }
    
    /**
     * Checks if a location is within the protection zone of any active ward.
     * @param location The location to check
     * @param minTier Minimum tier level required for protection (1-3)
     * @return true if protected, false otherwise
     */
    public boolean isProtected(Location location, int minTier) {
        String worldName = location.getWorld().getName();
        Set<UUID> worldWards = wardsByWorld.get(worldName);
        
        if (worldWards == null) {
            return false;
        }
        
        for (UUID id : worldWards) {
            Ward ward = wards.get(id);
            if (ward != null && ward.isActive() && ward.getTier() >= minTier) {
                World world = plugin.getServer().getWorld(ward.getWorld());
                if (world != null) {
                    Location wardLoc = ward.getLocation(world);
                    int radius = getRadius(ward.getTier());
                    double radiusSquared = radius * radius;
                    
                    if (wardLoc.distanceSquared(location) <= radiusSquared) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    /**
     * Получить радиус оберега по Tier
     */
    public int getRadius(int tier) {
        switch (tier) {
            case 1: return plugin.getConfigManager().getTier1Radius();
            case 2: return plugin.getConfigManager().getTier2Radius();
            case 3: return plugin.getConfigManager().getTier3RadiusMax();
            default: return 0;
        }
    }
    
    /**
      * Обновить tier оберега
      * @param ward The ward to update (immutable pattern - returns new instance)
      * @param newTier The new tier value
      */
    public Ward updateWardTier(Ward ward, int newTier) {
        Ward newWard = ward.withTier(newTier);
        wards.put(newWard.getId(), newWard);
        plugin.getDatabase().saveWard(newWard);
        return newWard;
    }

    /**
      * Обновить ссылку на оберег в кэше (для immutable pattern)
      * @param updatedWard The new ward instance to store
      */
    public void updateWardReference(Ward updatedWard) {
        wards.put(updatedWard.getId(), updatedWard);
    }
    
    /**
     * Удалить оберег
     */
    public void removeWard(Ward ward) {
        wards.remove(ward.getId());
        
        Set<UUID> worldWards = wardsByWorld.get(ward.getWorld());
        if (worldWards != null) {
            worldWards.remove(ward.getId());
        }
        
        plugin.getDatabase().deleteWard(ward.getId());
    }
    
    /**
     * Сохранить все обереги
     */
    public void saveAll() {
        for (Ward ward : wards.values()) {
            plugin.getDatabase().saveWard(ward);
        }
        
        if (plugin.getConfigManager().isDebugEnabled()) {
            plugin.getLogger().info("Сохранено " + wards.size() + " оберегов");
        }
    }
    
    /**
     * Получить все обереги
     */
    public Collection<Ward> getAllWards() {
        return wards.values();
    }
}
