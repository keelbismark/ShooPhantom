package dev.keelbismark.shoophantom.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ShooTabCompleter implements TabCompleter {
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // Основные команды
            List<String> baseCommands = Arrays.asList("info", "fuel");
            
            if (sender.hasPermission("shoo.admin")) {
                baseCommands = new ArrayList<>(baseCommands);
                baseCommands.add("admin");
            }
            
            for (String cmd : baseCommands) {
                if (cmd.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(cmd);
                }
            }
            
        } else if (args.length == 2 && args[0].equalsIgnoreCase("admin")) {
            // Админ команды
            if (sender.hasPermission("shoo.admin")) {
                List<String> adminCommands = Arrays.asList("reload", "give", "remove", "list", "setfuel");
                
                for (String cmd : adminCommands) {
                    if (cmd.toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(cmd);
                    }
                }
            }
            
        } else if (args.length == 3 && args[0].equalsIgnoreCase("admin") && args[1].equalsIgnoreCase("give")) {
            // Список игроков для команды give
            return null; // Bukkit автоматически подставит онлайн игроков
            
        } else if (args.length == 4 && args[0].equalsIgnoreCase("admin") && args[1].equalsIgnoreCase("give")) {
            // Тип предмета
            completions.add("sigil");
        }
        
        return completions;
    }
}
