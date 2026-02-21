package dev.keelbismark.shoophantom.data;

import org.bukkit.Location;

import java.util.List;
import java.util.UUID;

public interface Database {
    
    /**
     * Инициализация базы данных
     * @return true если успешно
     */
    boolean initialize();
    
    /**
     * Закрытие подключения
     */
    void close();
    
    /**
     * Сохранить оберег
     * @param ward оберег
     */
    void saveWard(Ward ward);
    
    /**
     * Загрузить оберег по ID
     * @param id UUID оберега
     * @return оберег или null
     */
    Ward loadWard(UUID id);
    
    /**
     * Загрузить оберег по локации
     * @param location локация
     * @return оберег или null
     */
    Ward loadWardByLocation(Location location);
    
    /**
     * Загрузить все обереги владельца
     * @param ownerUUID UUID владельца
     * @return список оберегов
     */
    List<Ward> loadWardsByOwner(UUID ownerUUID);
    
    /**
     * Загрузить все обереги в мире
     * @param worldName название мира
     * @return список оберегов
     */
    List<Ward> loadWardsByWorld(String worldName);
    
    /**
     * Загрузить все обереги
     * @return список всех оберегов
     */
    List<Ward> loadAllWards();
    
    /**
     * Удалить оберег
     * @param id UUID оберега
     */
    void deleteWard(UUID id);
    
    /**
     * Подсчитать обереги игрока
     * @param ownerUUID UUID владельца
     * @return количество оберегов
     */
    int countWardsByOwner(UUID ownerUUID);
}
