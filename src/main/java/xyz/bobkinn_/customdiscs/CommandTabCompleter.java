package xyz.bobkinn_.customdiscs;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommandTabCompleter implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length==1){
            sender.sendMessage("1");
            return Arrays.asList("list","reload","del","add","get","play");
        }
        return Collections.singletonList("");
    }
}
