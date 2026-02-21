package dev.keelbismark.shoophantom.data;

import org.bukkit.Location;

import dev.keelbismark.shoophantom.ShooPhantom;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MySQLDatabase implements Database {
    
    private final ShooPhantom plugin;
    private Connection connection;
    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;
    
    public MySQLDatabase(ShooPhantom plugin) {
        this.plugin = plugin;
        this.host = plugin.getConfig().getString("database.mysql.host", "localhost");
        this.port = plugin.getConfig().getInt("database.mysql.port", 3306);
        this.database = plugin.getConfig().getString("database.mysql.database", "shoo_phantom");
        this.username = plugin.getConfig().getString("database.mysql.username", "root");
        this.password = plugin.getConfig().getString("database.mysql.password", "");
    }
    
    @Override
    public boolean initialize() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            String url = String.format("jdbc:mysql://%s:%d/%s?autoReconnect=true&useSSL=false", 
                    host, port, database);
            
            connection = DriverManager.getConnection(url, username, password);
            
            createTables();
            
            plugin.getLogger().info("MySQL база данных инициализирована: " + database);
            return true;
            
        } catch (Exception e) {
            plugin.getLogger().severe("Ошибка инициализации MySQL: " + e.getMessage());
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
                "UNIQUE KEY location_unique (world, x, y, z)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }
    
    @Override
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                plugin.getLogger().info("MySQL подключение закрыто");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Ошибка закрытия MySQL: " + e.getMessage());
        }
    }
    
    private void checkConnection() throws SQLException {
        if (connection == null || connection.isClosed() || !connection.isValid(5)) {
            initialize();
        }
    }
    
    @Override
    public void saveWard(Ward ward) {
        try {
            checkConnection();
            
            String sql = "INSERT INTO wards " +
                    "(id, owner_uuid, world, x, y, z, tier, fuel, burn_end_time, next_degrade_time, created_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE " +
                    "tier=VALUES(tier), fuel=VALUES(fuel), burn_end_time=VALUES(burn_end_time), " +
                    "next_degrade_time=VALUES(next_degrade_time)";
            
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
            }
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Ошибка сохранения оберега: " + e.getMessage());
        }
    }
    
    @Override
    public Ward loadWard(UUID id) {
        try {
            checkConnection();
            
            String sql = "SELECT * FROM wards WHERE id = ?";
            
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, id.toString());
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    return createWardFromResultSet(rs);
                }
            }
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Ошибка загрузки оберега: " + e.getMessage());
        }
        
        return null;
    }
    
    @Override
    public Ward loadWardByLocation(Location location) {
        try {
            checkConnection();
            
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
            }
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Ошибка загрузки оберега по локации: " + e.getMessage());
        }
        
        return null;
    }
    
    @Override
    public List<Ward> loadWardsByOwner(UUID ownerUUID) {
        List<Ward> wards = new ArrayList<>();
        
        try {
            checkConnection();
            
            String sql = "SELECT * FROM wards WHERE owner_uuid = ?";
            
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, ownerUUID.toString());
                ResultSet rs = stmt.executeQuery();
                
                while (rs.next()) {
                    wards.add(createWardFromResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Ошибка загрузки оберегов владельца: " + e.getMessage());
        }
        
        return wards;
    }
    
    @Override
    public List<Ward> loadWardsByWorld(String worldName) {
        List<Ward> wards = new ArrayList<>();
        
        try {
            checkConnection();
            
            String sql = "SELECT * FROM wards WHERE world = ?";
            
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, worldName);
                ResultSet rs = stmt.executeQuery();
                
                while (rs.next()) {
                    wards.add(createWardFromResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Ошибка загрузки оберегов мира: " + e.getMessage());
        }
        
        return wards;
    }
    
    @Override
    public List<Ward> loadAllWards() {
        List<Ward> wards = new ArrayList<>();
        
        try {
            checkConnection();
            
            String sql = "SELECT * FROM wards";
            
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                
                while (rs.next()) {
                    wards.add(createWardFromResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Ошибка загрузки всех оберегов: " + e.getMessage());
        }
        
        return wards;
    }
    
    @Override
    public void deleteWard(UUID id) {
        try {
            checkConnection();
            
            String sql = "DELETE FROM wards WHERE id = ?";
            
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, id.toString());
                stmt.executeUpdate();
            }
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Ошибка удаления оберега: " + e.getMessage());
        }
    }
    
    @Override
    public int countWardsByOwner(UUID ownerUUID) {
        try {
            checkConnection();
            
            String sql = "SELECT COUNT(*) FROM wards WHERE owner_uuid = ?";
            
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, ownerUUID.toString());
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Ошибка подсчета оберегов: " + e.getMessage());
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
