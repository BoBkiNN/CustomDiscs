package xyz.bobkinn_.customdiscs;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommandTabCompleter implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length==1){
            return Arrays.asList("list","reload","del","add","get");
        }
        if (args.length==2 && args[0].equalsIgnoreCase("get")){
            ArrayList<String> ids = new ArrayList<>();
            int c = 0;
            for (CustomDisc ignored : Main.customDiscs){
                c++;
                ids.add(String.valueOf(c));
            }
            return ids;
        }
        if (args.length==3 && args[0].equalsIgnoreCase("get")){
            ArrayList<String> players = new ArrayList<>();
            for (Player p : sender.getServer().getOnlinePlayers()){
                players.add(p.getName());
            }
            return players;
        }
        return Collections.singletonList("");
    }
}
