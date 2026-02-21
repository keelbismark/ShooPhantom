package dev.keelbismark.shoophantom.mechanics;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import dev.keelbismark.shoophantom.ShooPhantom;
import dev.keelbismark.shoophantom.data.Ward;

public class FuelManager {

    private final ShooPhantom plugin;

    public FuelManager(ShooPhantom plugin) {
        this.plugin = plugin;
    }

    /**
     * Добавить топливо в оберег
     * @return New Ward instance with updated fuel, or null if fuel storage is full
     */
    public Ward addFuel(Ward ward, int amount) {
        int maxFuel = plugin.getConfigManager().getMaxFuel();
        int currentFuel = ward.getFuel();

        if (currentFuel >= maxFuel) {
            return null; // Хранилище полное
        }

        int toAdd = Math.min(amount, maxFuel - currentFuel);
        Ward newWard = ward.withAddedFuel(toAdd);

        // Если оберег был неактивен, запускаем горение
        if (!newWard.isActive()) {
            newWard = startBurning(newWard);
        }

        plugin.getDatabase().saveWard(newWard);
        return newWard;
    }

    /**
     * Запустить горение топлива
     * @return New Ward instance with burn time started, or current if no fuel
     */
    private Ward startBurning(Ward ward) {
        if (ward.getFuel() <= 0) {
            return ward;
        }

        // Вычисляем время горения одной мембраны
        int minutes = getFuelMinutes(ward.getTier());
        long burnTime = minutes * 60 * 1000L; // Миллисекунды

        return ward
            .withBurnEndTime(System.currentTimeMillis() + burnTime)
            .withFuel(ward.getFuel() - 1); // Убираем одну мембрану из банка
    }

    /**
     * Получить время горения одной мембраны в минутах
     */
    private int getFuelMinutes(int tier) {
        return switch (tier) {
            case 1 -> plugin.getConfigManager().getTier1FuelMinutes();
            case 2 -> plugin.getConfigManager().getTier2FuelMinutes();
            case 3 -> plugin.getConfigManager().getTier3FuelMinutes();
            default -> 60;
        };
    }

    /**
     * Обработка расхода топлива (вызывается каждую секунду)
     * @return New Ward instance if state changed, or current if no change
     */
    public Ward processFuelConsumption(Ward ward) {
        long now = System.currentTimeMillis();

        // Если топливо горит
        if (ward.getBurnEndTime() > now) {
            return ward; // Еще горит
        }

        // Топливо закончилось
        if (ward.getFuel() > 0) {
            // Есть топливо в банке - начинаем сжигать следующее
            Ward newWard = startBurning(ward);
            plugin.getDatabase().saveWard(newWard);
            return newWard;
        } else {
            // Топливо полностью закончилось
            Ward newWard = ward.withBurnEndTime(0);
            plugin.getDatabase().saveWard(newWard);

            // Уведомление владельца
            notifyOwner(newWard, plugin.getMessages().fuelEmpty());
            return newWard;
        }
    }

    /**
     * Получить оставшееся время работы оберега
     */
    public long getRemainingTime(Ward ward) {
        long now = System.currentTimeMillis();
        long remaining = 0;

        // Время текущего горения
        if (ward.getBurnEndTime() > now) {
            remaining += ward.getBurnEndTime() - now;
        }

        // Время остального топлива в банке
        int fuelMinutes = getFuelMinutes(ward.getTier());
        remaining += ward.getFuel() * fuelMinutes * 60 * 1000L;

        return remaining;
    }

    /**
     * Форматирование оставшегося времени
     */
    public String formatRemainingTime(long milliseconds) {
        if (milliseconds <= 0) {
            return "§cНет топлива";
        }

        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return days + "д " + (hours % 24) + "ч";
        } else if (hours > 0) {
            return hours + "ч " + (minutes % 60) + "м";
        } else if (minutes > 0) {
            return minutes + "м " + (seconds % 60) + "с";
        } else {
            return seconds + "с";
        }
    }

    /**
     * Уведомление владельца
     */
    private void notifyOwner(Ward ward, String message) {
        OfflinePlayer owner = Bukkit.getOfflinePlayer(ward.getOwnerUUID());
        if (owner.isOnline()) {
            owner.getPlayer().sendMessage(message);
        }
    }
}
