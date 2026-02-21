package dev.keelbismark.shoophantom.data;

import org.bukkit.Location;

import dev.keelbismark.shoophantom.ShooPhantom;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SQLiteDatabase implements Database {
    
    private final ShooPhantom plugin;
    private Connection connection;
    
    public SQLiteDatabase(ShooPhantom plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean initialize() {
        try {
            File dataFolder = plugin.getDataFolder();
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }
            
            String filename = plugin.getConfig().getString("database.sqlite.file", "wards.db");
            File dbFile = new File(dataFolder, filename);
            
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
            
            createTables();
            
            plugin.getLogger().info("SQLite база данных инициализирована: " + dbFile.getName());
            return true;
            
        } catch (ClassNotFoundException e) {
            plugin.getLogger().severe("SQLite JDBC драйвер не найден. Убедитесь, что Paper включает SQLite.");
            e.printStackTrace();
            return false;
        } catch (SQLException e) {
            plugin.getLogger().severe("Ошибка SQL при инициализации SQLite: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            plugin.getLogger().severe("Неожиданная ошибка при инициализации SQLite: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    private void createTables() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS wards (" +
                "id VARCHAR(36) PRIMARY KEY," +
                "owner_uuid VARCHAR(36) NOT NULL," +
                "world VARCHAR(64) NOT NULL," +
                "x INT NOT NULL," +
                "y INT NOT NULL," +
                "z INT NOT NULL," +
                "tier INT DEFAULT 1," +
                "fuel INT DEFAULT 0," +
                "burn_end_time BIGINT DEFAULT 0," +
                "next_degrade_time BIGINT DEFAULT 0," +
                "created_at BIGINT NOT NULL," +
                "UNIQUE(world, x, y, z)" +
                ")";
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            plugin.getLogger().info("Таблица 'wards' проверена/создана");
        }
    }
    
    @Override
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                plugin.getLogger().info("SQLite подключение к базе данных успешно закрыто");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Ошибка закрытия подключения SQLite: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public void saveWard(Ward ward) {
        String sql = "INSERT OR REPLACE INTO wards " +
                "(id, owner_uuid, world, x, y, z, tier, fuel, burn_end_time, next_degrade_time, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, ward.getId().toString());
            stmt.setString(2, ward.getOwnerUUID().toString());
            stmt.setString(3, ward.getWorld());
            stmt.setInt(4, ward.getX());
            stmt.setInt(5, ward.getY());
            stmt.setInt(6, ward.getZ());
            stmt.setInt(7, ward.getTier());
            stmt.setInt(8, ward.getFuel());
            stmt.setLong(9, ward.getBurnEndTime());
            stmt.setLong(10, ward.getNextDegradeTime());
            stmt.setLong(11, ward.getCreatedAt());
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Ошибка сохранения оберега " + ward.getId() + " в базу данных: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public Ward loadWard(UUID id) {
        String sql = "SELECT * FROM wards WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, id.toString());
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return createWardFromResultSet(rs);
            }
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Ошибка загрузки оберега с ID " + id + " из базы данных: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    @Override
    public Ward loadWardByLocation(Location location) {
        String sql = "SELECT * FROM wards WHERE world = ? AND x = ? AND y = ? AND z = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, location.getWorld().getName());
            stmt.setInt(2, location.getBlockX());
            stmt.setInt(3, location.getBlockY());
            stmt.setInt(4, location.getBlockZ());
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return createWardFromResultSet(rs);
            }
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Ошибка загрузки оберега по локации " + 
                location.getWorld().getName() + "," + location.getBlockX() + "," + 
                location.getBlockY() + "," + location.getBlockZ() + ": " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    @Override
    public List<Ward> loadWardsByOwner(UUID ownerUUID) {
        List<Ward> wards = new ArrayList<>();
        String sql = "SELECT * FROM wards WHERE owner_uuid = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, ownerUUID.toString());
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                wards.add(createWardFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Ошибка загрузки оберегов владельца " + ownerUUID + ": " + e.getMessage());
            e.printStackTrace();
        }
        
        return wards;
    }
    
    @Override
    public List<Ward> loadWardsByWorld(String worldName) {
        List<Ward> wards = new ArrayList<>();
        String sql = "SELECT * FROM wards WHERE world = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, worldName);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                wards.add(createWardFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Ошибка загрузки оберегов мира '" + worldName + "': " + e.getMessage());
            e.printStackTrace();
        }
        
        return wards;
    }
    
    @Override
    public List<Ward> loadAllWards() {
        List<Ward> wards = new ArrayList<>();
        String sql = "SELECT * FROM wards";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                wards.add(createWardFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Ошибка загрузки всех оберегов из базы данных: " + e.getMessage());
            e.printStackTrace();
        }
        
        return wards;
    }
    
    @Override
    public void deleteWard(UUID id) {
        String sql = "DELETE FROM wards WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, id.toString());
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Ошибка удаления оберега с ID " + id + " из базы данных: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public int countWardsByOwner(UUID ownerUUID) {
        String sql = "SELECT COUNT(*) FROM wards WHERE owner_uuid = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, ownerUUID.toString());
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Ошибка подсчета оберегов владельца " + ownerUUID + ": " + e.getMessage());
            e.printStackTrace();
        }
        
        return 0;
    }
    
    private Ward createWardFromResultSet(ResultSet rs) throws SQLException {
        return new Ward(
            UUID.fromString(rs.getString("id")),
            UUID.fromString(rs.getString("owner_uuid")),
            rs.getString("world"),
            rs.getInt("x"),
            rs.getInt("y"),
            rs.getInt("z"),
            rs.getInt("tier"),
            rs.getInt("fuel"),
            rs.getLong("burn_end_time"),
            rs.getLong("next_degrade_time"),
            rs.getLong("created_at")
        );
    }
}
