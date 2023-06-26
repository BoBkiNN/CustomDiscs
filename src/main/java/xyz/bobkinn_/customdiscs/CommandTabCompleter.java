package xyz.bobkinn_.customdiscs;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandTabCompleter implements TabCompleter {
    public boolean checkPermission(String perm, CommandSender player){
        return player.hasPermission(perm) || player.isOp();
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length==1){
            ArrayList<String> toDisplay = new ArrayList<>();
            if (checkPermission("customdiscs.list", sender)){
                toDisplay.add("list");
            }
            if (checkPermission("customdiscs.reload", sender)){
                toDisplay.add("reload");
            }
            if (checkPermission("customdiscs.del", sender)){
                toDisplay.add("del");
            }
            if (checkPermission("customdiscs.add", sender)){
                toDisplay.add("add");
            }
            if (checkPermission("customdiscs.get", sender)){
                toDisplay.add("get");
            }
            if (checkPermission("customdiscs.help", sender)){
                toDisplay.add("help");
            }
            return toDisplay;
        }
        if (args.length==2 && args[0].equalsIgnoreCase("get")){
            if (!checkPermission("customdiscs.get", sender)){
                return Collections.singletonList("");
            }
            return Utils.getIDsList();

        }
        if (args.length==3 && args[0].equalsIgnoreCase("get")){
            if (!checkPermission("customdiscs.get", sender)){
                return Collections.singletonList("");
            }
            ArrayList<String> players = new ArrayList<>();
            for (Player p : sender.getServer().getOnlinePlayers()){
                players.add(p.getName());
            }
            return players;
        }
        if (args.length==2 && args[0].equalsIgnoreCase("del")){
            if (!checkPermission("customdiscs.del", sender)){
                return Collections.singletonList("");
            }
            return Utils.getIDsList();
        }
        if (args.length==2 && args[0].equalsIgnoreCase("add")){
            if (!checkPermission("customdiscs.add", sender)){
                return Collections.singletonList("");
            }
//            return Utils.getMaterialList(); //currently disabled due to unknown bug sorry
            return Utils.getVanillaDiscsList();
        }
        if (args.length==3 && args[0].equalsIgnoreCase("add")){
            if (!checkPermission("customdiscs.add", sender)){
                return Collections.singletonList("");
            }
            return Utils.getSoundsList();
        }
        if (args.length==4 && args[0].equalsIgnoreCase("add")){
            if (!checkPermission("customdiscs.add", sender)){
                return Collections.singletonList("");
            }
            String cmdCo = Main.configuration.getString("messages.add-cmd.cmd-tab-complete","<CustomModelData-int>");
            cmdCo = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&',cmdCo));
            return Collections.singletonList(cmdCo);
        }
        if (args.length==5 && args[0].equalsIgnoreCase("add")){
            if (!checkPermission("customdiscs.add", sender)){
                return Collections.singletonList("");
            }
            String nCo = Main.configuration.getString("messages.add-cmd.name-tab-complete","<displayName>");
            nCo = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&',nCo));
            return Collections.singletonList(nCo);
        }
        return Collections.singletonList("");
    }
}
