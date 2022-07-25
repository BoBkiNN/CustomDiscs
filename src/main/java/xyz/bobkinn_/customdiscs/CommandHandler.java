package xyz.bobkinn_.customdiscs;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandHandler implements CommandExecutor {


    public void helpCmd(CommandSender sender){
        sender.sendMessage("TODO help msg");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length==0){
            helpCmd(sender);
            return true;
        }
        if (args[0].equalsIgnoreCase("reload")){
            if (!sender.hasPermission("customdiscs.reload") || !sender.isOp()){
                sender.sendMessage(Utils.noPermMsg());
                return false;
            }
            Main.loadConfig();
            String rlMsg = Main.configuration.getString("messages.reload","&aReloaded!");
            rlMsg = ChatColor.translateAlternateColorCodes('&',rlMsg);
            sender.sendMessage(rlMsg);
            return true;
        }
        if (args[0].equalsIgnoreCase("list")){
            if (!sender.hasPermission("customdiscs.list") || !sender.isOp()){
                sender.sendMessage(Utils.noPermMsg());
                return false;
            }
            String listTop = Main.configuration.getString("messages.disc-list-top","&a----==== &bLoaded custom discs (%count%):");
            String listEntry = Main.configuration.getString("messages.disc-list-entry","&c№%index% &7- S: &c%sound%&7, N: &c%name%&7, CMD: &c%cmd%&7, M: &c%item%");
            String listBottom = Main.configuration.getString("messages.disc-list-bottom");
            listTop=ChatColor.translateAlternateColorCodes('&',listTop);
            listEntry=ChatColor.translateAlternateColorCodes('&',listEntry);
            if (listBottom!=null){
                listBottom=ChatColor.translateAlternateColorCodes('&',listBottom);
            }
            listTop=listTop.replace("%count%",Main.customDiscs.size()+"");
            StringBuilder msg = new StringBuilder();
            msg.append(listTop);
            msg.append('\n');
            for (CustomDisc disc : Main.customDiscs){
                String listEntryMsg = listEntry.replace("%index%",Main.customDiscs.indexOf(disc)+1+"");
                listEntryMsg=listEntryMsg.replace("%sound%", disc.getSound());
                listEntryMsg=listEntryMsg.replace("%name%", disc.getName());
                listEntryMsg=listEntryMsg.replace("%cmd%",disc.getCmd()+"");
                listEntryMsg=listEntryMsg.replace("%item%",disc.getMaterial().getKey().getKey());
                msg.append(listEntryMsg).append('\n');
            }
            if (listBottom!=null){
                msg.append(listBottom);
            }
            sender.sendMessage(msg.toString());
            return true;
        }
        return true;
    }
}
