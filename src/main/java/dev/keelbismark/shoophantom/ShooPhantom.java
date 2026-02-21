package dev.keelbismark.shoophantom;

import org.bukkit.plugin.java.JavaPlugin;

import dev.keelbismark.shoophantom.commands.ShooCommand;
import dev.keelbismark.shoophantom.commands.ShooTabCompleter;
import dev.keelbismark.shoophantom.config.ConfigManager;
import dev.keelbismark.shoophantom.config.Messages;
import dev.keelbismark.shoophantom.data.Database;
import dev.keelbismark.shoophantom.data.MySQLDatabase;
import dev.keelbismark.shoophantom.data.SQLiteDatabase;
import dev.keelbismark.shoophantom.data.WardManager;
import dev.keelbismark.shoophantom.items.CraftingRecipes;
import dev.keelbismark.shoophantom.listeners.*;
import dev.keelbismark.shoophantom.mechanics.DegradationManager;
import dev.keelbismark.shoophantom.mechanics.FuelManager;
import dev.keelbismark.shoophantom.tasks.*;

public class ShooPhantom extends JavaPlugin {
    
    private static ShooPhantom instance;
    private ConfigManager configManager;
    private Messages messages;
    private Database database;
    private WardManager wardManager;
    private FuelManager fuelManager;
    private DegradationManager degradationManager;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // Загрузка конфигурации
        saveDefaultConfig();
        configManager = new ConfigManager(this);
        messages = new Messages(this);
        
        // Инициализация базы данных
        String dbType = getConfig().getString("database.type", "SQLITE");
        if (dbType.equalsIgnoreCase("MYSQL")) {
            database = new MySQLDatabase(this);
        } else {
            database = new SQLiteDatabase(this);
        }
        
        if (!database.initialize()) {
            getLogger().severe("Не удалось инициализировать базу данных! Плагин отключается.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        // Инициализация менеджеров
        wardManager = new WardManager(this);
        fuelManager = new FuelManager(this);
        degradationManager = new DegradationManager(this);
        
        // Регистрация команд
        ShooCommand shooCommand = new ShooCommand(this);
        getCommand("shoo").setExecutor(shooCommand);
        getCommand("shoo").setTabCompleter(new ShooTabCompleter());
        
        // Регистрация слушателей
        getServer().getPluginManager().registerEvents(new PhantomListener(this), this);
        getServer().getPluginManager().registerEvents(new InteractListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockListener(this), this);
        getServer().getPluginManager().registerEvents(new HopperListener(this), this);
        getServer().getPluginManager().registerEvents(new PhantomDeathListener(this), this);
        
        // Регистрация рецептов
        if (getConfig().getBoolean("activation.craft.enabled", true)) {
            CraftingRecipes.register(this);
        }
        
        // Запуск задач
        startTasks();
        
        getLogger().info("§aShoo! Phantom v1.0 включен!");
    }
    
    @Override
    public void onDisable() {
        // Остановка задач
        getServer().getScheduler().cancelTasks(this);
        
        // Сохранение всех оберегов
        if (wardManager != null) {
            wardManager.saveAll();
        }
        
        // Закрытие базы данных
        if (database != null) {
            database.close();
        }
        
        getLogger().info("§cShoo! Phantom v1.0 выключен!");
    }
    
    private void startTasks() {
        // Расход топлива (каждые 20 тиков = 1 секунда)
        new FuelConsumptionTask(this).runTaskTimer(this, 20L, 20L);
        
        // Проверка деградации (каждые 5 минут)
        long degradationInterval = getConfig().getInt("degradation.tier2.check-interval-minutes", 5) * 60 * 20L;
        new DegradationTask(this).runTaskTimer(this, degradationInterval, degradationInterval);
        
        // Отталкивание фантомов Tier 3 (каждые 40 тиков)
        int repelInterval = getConfig().getInt("tiers.tier3.effects.repel.tick-interval", 40);
        new PhantomRepelTask(this).runTaskTimer(this, repelInterval, repelInterval);
        
        // Визуальные эффекты
        if (getConfig().getBoolean("visuals.enabled", true)) {
            new ParticleTask(this).runTaskTimer(this, 20L, 20L);
        }
        
        // Автосохранение (каждые N минут)
        long saveInterval = getConfig().getInt("database.auto-save-minutes", 5) * 60 * 20L;
        getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            wardManager.saveAll();
        }, saveInterval, saveInterval);
    }
    
    public void reload() {
        reloadConfig();
        configManager = new ConfigManager(this);
        messages = new Messages(this);
    }
    
    // Геттеры
    public static ShooPhantom getInstance() {
        return instance;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public Messages getMessages() {
        return messages;
    }
    
    public Database getDatabase() {
        return database;
    }
    
    public WardManager getWardManager() {
        return wardManager;
    }
    
    public FuelManager getFuelManager() {
        return fuelManager;
    }
    
    public DegradationManager getDegradationManager() {
        return degradationManager;
    }
}
