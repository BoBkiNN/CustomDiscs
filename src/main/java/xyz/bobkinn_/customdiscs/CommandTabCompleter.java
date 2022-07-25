package xyz.bobkinn_.customdiscs;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.Arrays;
import java.util.List;

public class CommandTabCompleter implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        sender.sendMessage(args.length+"");
        if (args.length==0){
            sender.sendMessage("0");
            return Arrays.asList("list","reload","del","add");
        }
        return null;
    }
}
