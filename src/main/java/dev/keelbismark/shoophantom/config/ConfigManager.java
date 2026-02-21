package dev.keelbismark.shoophantom.config;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import dev.keelbismark.shoophantom.ShooPhantom;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Manages configuration values for the plugin.
 * Provides type-safe access to configuration settings with fallback values.
 */
public class ConfigManager {
    
    private final ShooPhantom plugin;
    private final FileConfiguration config;
    
    public ConfigManager(ShooPhantom plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }
    
    private Material parseMaterial(String configPath, Material fallback) {
        String matName = config.getString(configPath, fallback.name());
        try {
            return Material.valueOf(matName);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Материал '" + matName + "' не найден для " + configPath + ", используется fallback: " + fallback.name());
            return fallback;
        }
    }
    
    // General Settings
    
    /**
     * Returns the maximum number of wards allowed per player.
     * @return Maximum wards per player, or 0 for unlimited
     */
    public int getMaxWardsPerPlayer() {
        return config.getInt("general.max-wards-per-player", 5);
    }
    
    public boolean isExplosionProtectionEnabled() {
        return config.getBoolean("general.explosion-protection", true);
    }
    
    public boolean isDebugEnabled() {
        return config.getBoolean("general.debug", false);
    }
    
    // Tier 1
    public int getTier1Radius() {
        return config.getInt("tiers.tier1.radius", 48);
    }
    
    public int getTier1FuelMinutes() {
        return config.getInt("tiers.tier1.fuel-minutes", 60);
    }
    
    // Tier 2
    public int getTier2Radius() {
        return config.getInt("tiers.tier2.radius", 80);
    }
    
    public int getTier2FuelMinutes() {
        return config.getInt("tiers.tier2.fuel-minutes", 45);
    }
    
    public int getTier2MinAliveBlocks() {
        return config.getInt("structure.tier2.min-alive-blocks", 9);
    }
    
    public Set<Material> getTier2AliveMaterials() {
        List<String> materials = config.getStringList("structure.tier2.alive-materials");
        Set<Material> result = new HashSet<>();
        for (String mat : materials) {
            try {
                result.add(Material.valueOf(mat));
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Неизвестный материал в alive-materials: " + mat + ", используется fallback: COPPER_BLOCK");
                result.add(Material.COPPER_BLOCK);
            }
        }
        return result;
    }
    
    public Set<Material> getTier2DeadMaterials() {
        List<String> materials = config.getStringList("structure.tier2.dead-materials");
        Set<Material> result = new HashSet<>();
        for (String mat : materials) {
            try {
                result.add(Material.valueOf(mat));
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Неизвестный материал в dead-materials: " + mat + ", используется fallback: OXIDIZED_COPPER");
                result.add(Material.OXIDIZED_COPPER);
            }
        }
        return result;
    }
    
    public int getTier2WarnThreshold() {
        return config.getInt("degradation.tier2.warn-threshold", 11);
    }
    
    // Tier 3
    public int getTier3RadiusMax() {
        return config.getInt("tiers.tier3.radius-max", 128);
    }
    
    public int getTier3RadiusMin() {
        return config.getInt("tiers.tier3.radius-min", 80);
    }
    
    public int getTier3FuelMinutes() {
        return config.getInt("tiers.tier3.fuel-minutes", 30);
    }
    
    public double getTier3DamageMax() {
        return config.getDouble("tiers.tier3.effects.repel.damage-max", 2.0);
    }
    
    public double getTier3DamageMin() {
        return config.getDouble("tiers.tier3.effects.repel.damage-min", 0.5);
    }
    
    public int getTier3FireTicksMax() {
        return config.getInt("tiers.tier3.effects.repel.fire-ticks-max", 40);
    }
    
    public int getTier3FireTicksMin() {
        return config.getInt("tiers.tier3.effects.repel.fire-ticks-min", 10);
    }
    
    public double getTier3KnockbackMax() {
        return config.getDouble("tiers.tier3.effects.repel.knockback-max", 0.6);
    }
    
    public double getTier3KnockbackMin() {
        return config.getDouble("tiers.tier3.effects.repel.knockback-min", 0.15);
    }
    
    public int getTier3CycleHoursMin() {
        return config.getInt("degradation.tier3.cycle-hours-min", 48);
    }
    
    public int getTier3CycleHoursMax() {
        return config.getInt("degradation.tier3.cycle-hours-max", 72);
    }
    
    public boolean getTier3PauseWhenNoPlayers() {
        return config.getBoolean("degradation.tier3.pause-when-no-players", true);
    }
    
    public int getTier3PauseCheckRadius() {
        return config.getInt("degradation.tier3.pause-check-radius", 128);
    }
    
    // Структура
    /**
     * Returns the material placed above the flower pot.
     * @return Material for the above block
     */
    public Material getTier1AboveMaterial() {
        return parseMaterial("structure.tier1.above", Material.LIGHTNING_ROD);
    }
    
    public Material getTier1BelowMaterial() {
        return parseMaterial("structure.tier1.below", Material.CHISELED_TUFF_BRICKS);
    }
    
    public Material getTier1SidesMaterial() {
        return parseMaterial("structure.tier1.sides", Material.COPPER_GRATE);
    }
    
    public Material getTier1CornersMaterial() {
        return parseMaterial("structure.tier1.corners", Material.AMETHYST_BLOCK);
    }
    
    public Material getTier3MastGlass() {
        return parseMaterial("structure.tier3.mast-glass", Material.TINTED_GLASS);
    }
    
    public Material getTier3MastTop() {
        return parseMaterial("structure.tier3.mast-top", Material.END_ROD);
    }
    
    public Material getTier3MastDead() {
        return parseMaterial("structure.tier3.mast-dead", Material.IRON_BARS);
    }
    
    // Топливо
    public Material getFuelMaterial() {
        return parseMaterial("fuel.item", Material.PHANTOM_MEMBRANE);
    }
    
    public int getMaxFuel() {
        return config.getInt("fuel.max-stack", 64);
    }
    
    public boolean isHopperInputAllowed() {
        return config.getBoolean("fuel.allow-hopper-input", true);
    }
    
    public boolean isHopperOutputBlocked() {
        return config.getBoolean("fuel.block-hopper-output", true);
    }
    
    public boolean dropFuelOnBreak() {
        return config.getBoolean("fuel.drop-on-break", true);
    }
    
    // Активация
    public boolean requireSigil() {
        return config.getBoolean("activation.require-sigil", true);
    }
    
    public Material getSigilBaseMaterial() {
        return parseMaterial("activation.sigil-base-item", Material.ECHO_SHARD);
    }
    
    // Визуальные эффекты
    public boolean visualsEnabled() {
        return config.getBoolean("visuals.enabled", true);
    }
}
