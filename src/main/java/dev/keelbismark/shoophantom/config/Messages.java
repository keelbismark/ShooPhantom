package dev.keelbismark.shoophantom.config;

import org.bukkit.configuration.file.FileConfiguration;

import dev.keelbismark.shoophantom.ShooPhantom;

public class Messages {
    
    private final ShooPhantom plugin;
    private final FileConfiguration config;
    private final String prefix;
    
    public Messages(ShooPhantom plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        this.prefix = config.getString("messages.prefix", "§5[Shoo!] §r");
    }
    
    public String get(String path) {
        return prefix + config.getString("messages." + path, "§cСообщение не найдено: " + path);
    }
    
    public String get(String path, String... replacements) {
        String message = get(path);
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                message = message.replace("{" + replacements[i] + "}", replacements[i + 1]);
            }
        }
        return message;
    }
    
    // Активация
    public String activationSuccess(int tier) {
        return get("activation.success", "tier", String.valueOf(tier));
    }
    
    public String activationInvalidStructure() {
        return get("activation.invalid-structure");
    }
    
    public String activationNoSigil() {
        return get("activation.no-sigil");
    }
    
    public String activationLimitReached(int max) {
        return get("activation.limit-reached", "max", String.valueOf(max));
    }
    
    // Топливо
    public String fuelAdded(int amount, int total) {
        return get("fuel.added", "amount", String.valueOf(amount), "total", String.valueOf(total));
    }
    
    public String fuelFull() {
        return get("fuel.full");
    }
    
    public String fuelEmpty() {
        return get("fuel.empty");
    }
    
    public String fuelStatus(int fuel, String time) {
        return get("fuel.status", "fuel", String.valueOf(fuel), "time", time);
    }
    
    // Деградация
    public String degradationTier2Warn(int alive) {
        return get("degradation.tier2-warn", "alive", String.valueOf(alive));
    }
    
    public String degradationTier2Fail() {
        return get("degradation.tier2-fail");
    }
    
    public String degradationTier3Mast(int percent, int alive) {
        return get("degradation.tier3-mast", "percent", String.valueOf(percent), "alive", String.valueOf(alive));
    }
    
    public String degradationTier3Fail() {
        return get("degradation.tier3-fail");
    }
    
    // Ремонт
    public String repairTier2() {
        return get("repair.tier2");
    }
    
    public String repairTier3(int percent) {
        return get("repair.tier3", "percent", String.valueOf(percent));
    }
    
    // Информация
    public String infoHeader() {
        return get("info.header");
    }
    
    public String infoOwner(String owner) {
        return get("info.owner", "owner", owner);
    }
    
    public String infoTier(int tier) {
        return get("info.tier", "tier", String.valueOf(tier));
    }
    
    public String infoRadius(int radius) {
        return get("info.radius", "radius", String.valueOf(radius));
    }
    
    public String infoFuel(int fuel) {
        return get("info.fuel", "fuel", String.valueOf(fuel));
    }
    
    public String infoTimeLeft(String time) {
        return get("info.time-left", "time", time);
    }
    
    public String infoMasts(int alive, int percent) {
        return get("info.masts", "alive", String.valueOf(alive), "percent", String.valueOf(percent));
    }
    
    public String infoRing(int alive) {
        return get("info.ring", "alive", String.valueOf(alive));
    }
    
    // Разрушение
    public String destroySuccess() {
        return get("destroy.success");
    }
    
    // Админ
    public String adminReload() {
        return get("admin.reload");
    }
    
    public String adminGiveSigil(String player, int amount) {
        return get("admin.give-sigil", "player", player, "amount", String.valueOf(amount));
    }
    
    public String adminRemoved() {
        return get("admin.removed");
    }
    
    public String adminNotLooking() {
        return get("admin.not-looking");
    }
}
