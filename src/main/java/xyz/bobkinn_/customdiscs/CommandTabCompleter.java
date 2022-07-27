package xyz.bobkinn_.customdiscs;

import net.md_5.bungee.api.ChatColor;
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
            return Utils.getIDsList();
        }
        if (args.length==3 && args[0].equalsIgnoreCase("get")){
            ArrayList<String> players = new ArrayList<>();
            for (Player p : sender.getServer().getOnlinePlayers()){
                players.add(p.getName());
            }
            return players;
        }
        if (args.length==2 && args[0].equalsIgnoreCase("del")){
            return Utils.getIDsList();
        }
        if (args.length==2 && args[0].equalsIgnoreCase("add")){
            return Utils.getMaterialList();
        }
        if (args.length==3 && args[0].equalsIgnoreCase("add")){
            return Utils.getSoundsList();
        }
        if (args.length==4 && args[0].equalsIgnoreCase("add")){
            String cmdCo = Main.configuration.getString("messages.add-cmd.cmd-tab-complete","<CustomModelData-int>");
            cmdCo = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&',cmdCo));
            return Collections.singletonList(cmdCo);
        }
        if (args.length==5 && args[0].equalsIgnoreCase("add")){
            String nCo = Main.configuration.getString("messages.add-cmd.name-tab-complete","<displayName>");
            nCo = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&',nCo));
            return Collections.singletonList(nCo);
        }
        return Collections.singletonList("");
    }
}
