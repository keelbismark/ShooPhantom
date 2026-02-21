package dev.keelbismark.shoophantom.commands;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;

import dev.keelbismark.shoophantom.ShooPhantom;
import dev.keelbismark.shoophantom.data.Ward;
import dev.keelbismark.shoophantom.items.ShooSigil;

public class ShooCommand implements CommandExecutor {
    
    private final ShooPhantom plugin;
    
    public ShooCommand(ShooPhantom plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "info":
                return handleInfo(sender);
            case "fuel":
                return handleFuel(sender);
            case "admin":
                return handleAdmin(sender, args);
            default:
                sendHelp(sender);
                return true;
        }
    }
    
    /**
     * Показать помощь
     */
    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§5═══ Shoo! Phantom Команды ═══");
        sender.sendMessage("§7/shoo info §f- Информация об обереге");
        sender.sendMessage("§7/shoo fuel §f- Проверить топливо");
        
        if (sender.hasPermission("shoo.admin")) {
            sender.sendMessage("§7/shoo admin reload §f- Перезагрузить конфиг");
            sender.sendMessage("§7/shoo admin give <игрок> sigil [кол-во] §f- Выдать Sigil");
            sender.sendMessage("§7/shoo admin remove §f- Удалить оберег");
            sender.sendMessage("§7/shoo admin list [радиус] §f- Список оберегов");
            sender.sendMessage("§7/shoo admin setfuel <кол-во> §f- Установить топливо");
        }
    }
    
    /**
      * Команда info - показать информацию об обереге
      */
    private boolean handleInfo(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cЭта команда только для игроков!");
            return true;
        }

        // Получаем блок, на который смотрит игрок
        Block target = getTargetBlock(player);
        
        if (target == null || target.getType() != Material.DECORATED_POT) {
            player.sendMessage(plugin.getMessages().adminNotLooking());
            return true;
        }
        
        Ward ward = plugin.getWardManager().getWardByLocation(target.getLocation());
        
        if (ward == null) {
            player.sendMessage("§c✖ Это не оберег!");
            return true;
        }
        
        // Показываем информацию
        showWardInfo(player, ward);
        
        return true;
    }
    
    /**
      * Команда fuel - проверить топливо
      */
    private boolean handleFuel(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cЭта команда только для игроков!");
            return true;
        }

        Block target = getTargetBlock(player);
        
        if (target == null || target.getType() != Material.DECORATED_POT) {
            player.sendMessage(plugin.getMessages().adminNotLooking());
            return true;
        }
        
        Ward ward = plugin.getWardManager().getWardByLocation(target.getLocation());
        
        if (ward == null) {
            player.sendMessage("§c✖ Это не оберег!");
            return true;
        }
        
        // Показываем топливо
        long remaining = plugin.getFuelManager().getRemainingTime(ward);
        String timeStr = plugin.getFuelManager().formatRemainingTime(remaining);
        
        player.sendMessage(plugin.getMessages().fuelStatus(ward.getFuel(), timeStr));
        
        return true;
    }
    
    /**
     * Команды администратора
     */
    private boolean handleAdmin(CommandSender sender, String[] args) {
        if (!sender.hasPermission("shoo.admin")) {
            sender.sendMessage("§cУ вас нет прав!");
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage("§cИспользование: /shoo admin <reload|give|remove|list|setfuel>");
            return true;
        }
        
        String adminCmd = args[1].toLowerCase();
        
        switch (adminCmd) {
            case "reload":
                return handleReload(sender);
            case "give":
                return handleGive(sender, args);
            case "remove":
                return handleRemove(sender);
            case "list":
                return handleList(sender, args);
            case "setfuel":
                return handleSetFuel(sender, args);
            default:
                sender.sendMessage("§cНеизвестная команда!");
                return true;
        }
    }
    
    /**
     * Админ: reload
     */
    private boolean handleReload(CommandSender sender) {
        plugin.reload();
        sender.sendMessage(plugin.getMessages().adminReload());
        return true;
    }
    
    /**
     * Админ: give sigil
     */
    private boolean handleGive(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage("§cИспользование: /shoo admin give <игрок> sigil [кол-во]");
            return true;
        }
        
        Player target = plugin.getServer().getPlayer(args[2]);
        
        if (target == null) {
            sender.sendMessage("§cИгрок не найден!");
            return true;
        }
        
        int amount = 1;
        if (args.length >= 5) {
            try {
                amount = Integer.parseInt(args[4]);
            } catch (NumberFormatException e) {
                sender.sendMessage("§cНеверное количество!");
                return true;
            }
        }
        
        target.getInventory().addItem(ShooSigil.create(amount));
        sender.sendMessage(plugin.getMessages().adminGiveSigil(target.getName(), amount));
        
        return true;
    }
    
    /**
      * Админ: remove
      */
    private boolean handleRemove(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cЭта команда только для игроков!");
            return true;
        }

        Block target = getTargetBlock(player);
        
        if (target == null || target.getType() != Material.DECORATED_POT) {
            player.sendMessage(plugin.getMessages().adminNotLooking());
            return true;
        }
        
        Ward ward = plugin.getWardManager().getWardByLocation(target.getLocation());
        
        if (ward == null) {
            player.sendMessage("§c✖ Это не оберег!");
            return true;
        }
        
        plugin.getWardManager().removeWard(ward);
        player.sendMessage(plugin.getMessages().adminRemoved());
        
        return true;
    }
    
    /**
      * Админ: list
      */
    private boolean handleList(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cЭта команда только для игроков!");
            return true;
        }
        int radius = 100;
        
        if (args.length >= 3) {
            try {
                radius = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                player.sendMessage("§cНеверный радиус!");
                return true;
            }
        }
        
        Location playerLoc = player.getLocation();
        int count = 0;
        double radiusSquared = radius * radius;
        
        player.sendMessage("§5═══ Обереги в радиусе " + radius + " блоков ═══");
        
        for (Ward ward : plugin.getWardManager().getWardsByWorld(player.getWorld().getName())) {
            Location wardLoc = ward.getLocation(player.getWorld());
            
            if (wardLoc.distanceSquared(playerLoc) <= radiusSquared) {
                String ownerName = plugin.getServer().getOfflinePlayer(ward.getOwnerUUID()).getName();
                if (ownerName == null) ownerName = "Неизвестен";
                
                player.sendMessage(String.format("§7- §fTier %d §7| §f%s §7| §f%d, %d, %d §7| Владелец: §f%s",
                    ward.getTier(),
                    ward.isActive() ? "§aАктивен" : "§cНеактивен",
                    ward.getX(), ward.getY(), ward.getZ(),
                    ownerName
                ));
                
                count++;
            }
        }
        
        player.sendMessage("§7Всего: §f" + count);
        
        return true;
    }
    
    /**
      * Админ: setfuel
      */
    private boolean handleSetFuel(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cЭта команда только для игроков!");
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage("§cИспользование: /shoo admin setfuel <кол-во>");
            return true;
        }

        int amount;
        try {
            amount = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§cНеверное количество!");
            return true;
        }
        Block target = getTargetBlock(player);
        
        if (target == null || target.getType() != Material.DECORATED_POT) {
            player.sendMessage(plugin.getMessages().adminNotLooking());
            return true;
        }
        
        Ward ward = plugin.getWardManager().getWardByLocation(target.getLocation());

        if (ward == null) {
            player.sendMessage("§c✖ Это не оберег!");
            return true;
        }

        Ward newWard = ward.withFuel(Math.max(0, Math.min(amount, plugin.getConfigManager().getMaxFuel())));
        plugin.getWardManager().updateWardReference(newWard);
        plugin.getDatabase().saveWard(newWard);

        player.sendMessage("§aТопливо установлено: " + newWard.getFuel());

        return true;
    }
    
    /**
     * Получить блок, на который смотрит игрок
     */
    private Block getTargetBlock(Player player) {
        RayTraceResult result = player.rayTraceBlocks(10);
        
        if (result != null && result.getHitBlock() != null) {
            return result.getHitBlock();
        }
        
        return null;
    }
    
    /**
     * Показать информацию об обереге
     */
    private void showWardInfo(Player player, Ward ward) {
        player.sendMessage(plugin.getMessages().infoHeader());
        
        String ownerName = plugin.getServer().getOfflinePlayer(ward.getOwnerUUID()).getName();
        if (ownerName == null) ownerName = "Неизвестен";
        
        player.sendMessage(plugin.getMessages().infoOwner(ownerName));
        player.sendMessage(plugin.getMessages().infoTier(ward.getTier()));
        player.sendMessage(plugin.getMessages().infoRadius(plugin.getWardManager().getRadius(ward.getTier())));
        player.sendMessage(plugin.getMessages().infoFuel(ward.getFuel()));
        
        long remaining = plugin.getFuelManager().getRemainingTime(ward);
        String timeStr = plugin.getFuelManager().formatRemainingTime(remaining);
        player.sendMessage(plugin.getMessages().infoTimeLeft(timeStr));
    }
}
