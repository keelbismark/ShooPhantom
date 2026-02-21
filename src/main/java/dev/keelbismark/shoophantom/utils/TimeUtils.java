package dev.keelbismark.shoophantom.utils;

import java.util.concurrent.TimeUnit;

public class TimeUtils {
    
    /**
     * Форматирование времени в читаемый вид
     */
    public static String formatTime(long milliseconds) {
        if (milliseconds <= 0) {
            return "0с";
        }
        
        long seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds);
        long hours = TimeUnit.MILLISECONDS.toHours(milliseconds);
        long days = TimeUnit.MILLISECONDS.toDays(milliseconds);
        
        if (days > 0) {
            long remainingHours = hours % 24;
            return days + "д " + remainingHours + "ч";
        } else if (hours > 0) {
            long remainingMinutes = minutes % 60;
            return hours + "ч " + remainingMinutes + "м";
        } else if (minutes > 0) {
            long remainingSeconds = seconds % 60;
            return minutes + "м " + remainingSeconds + "с";
        } else {
            return seconds + "с";
        }
    }
    
    /**
     * Форматирование времени в часы и минуты
     */
    public static String formatHoursMinutes(long milliseconds) {
        long hours = TimeUnit.MILLISECONDS.toHours(milliseconds);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds) % 60;
        
        if (hours > 0) {
            return hours + "ч " + minutes + "м";
        } else {
            return minutes + "м";
        }
    }
    
    /**
     * Получить текущее время в миллисекундах
     */
    public static long now() {
        return System.currentTimeMillis();
    }
    
    /**
     * Проверка, истекло ли время
     */
    public static boolean isExpired(long timestamp) {
        return timestamp > 0 && timestamp < System.currentTimeMillis();
    }
}
