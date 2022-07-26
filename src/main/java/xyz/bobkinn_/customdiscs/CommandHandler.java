package xyz.bobkinn_.customdiscs;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;

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
        if (args[0].equalsIgnoreCase("get")){
            if (!sender.hasPermission("customdiscs.get") || !sender.isOp()){
                sender.sendMessage(Utils.noPermMsg());
                return true;
            }

            int id;
            try {
                id = Integer.parseInt(args[1]);
            } catch (NumberFormatException e){
                String msg = Main.configuration.getString("messages.get-cmd.id-not-given","&cPlease provide disc id");
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',msg));
                return true;
            }
            CustomDisc disc;
            try {
                disc = Main.customDiscs.get(id-1);
            } catch (IndexOutOfBoundsException e){
                String msg = Main.configuration.getString("messages.get-cmd.id-not-found","&cDisc with this id not found");
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',msg));
                return true;
            }
            ItemStack item = new ItemStack(disc.getMaterial());
            ItemMeta meta = item.getItemMeta();
            meta.setCustomModelData(disc.getCmd());
            if (item.getType().isRecord()){
                meta.addItemFlags(ItemFlag.values());
            }
            String fName=Utils.processDesc(disc.getName(),Main.configuration.getBoolean("use-colored-desc"));
            meta.setLore(Collections.singletonList(fName));
            item.setItemMeta(meta);
            if (args.length>=3){
                Player p = sender.getServer().getPlayerExact(args[2]);
                if (p==null){
                    String msg = Main.configuration.getString("messages.get-cmd.player-not-found","&cPlayer with this name not found");
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',msg));
                    return true;
                } else {
                    p.getInventory().addItem(item);
                    String msg = Main.configuration.getString("messages.get-cmd.success","&cGiven disc &b№%id% &cto &e%player%");
                    msg = msg.replace("%id%",id+"").replace("%player%", p.getName());
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',msg));
                    return true;
                }
            } else {
                if (sender instanceof Player){
                    Player player = (Player) sender;
                    player.getInventory().addItem(item);
                    String msg = Main.configuration.getString("messages.get-cmd.success","&cGiven disc &b№%id% &cto &e%player%");
                    msg = msg.replace("%id%",id+"").replace("%player%", player.getName());
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',msg));
                    return true;
                } else {
                    String msg = Main.configuration.getString("messages.get-cmd.console-use","&cUse &e/cd get <id> [player]&c for console");
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',msg));
                    return true;
                }
            }

        }

        if (args[0].equalsIgnoreCase("reload")){
            if (!sender.hasPermission("customdiscs.reload") || !sender.isOp()){
                sender.sendMessage(Utils.noPermMsg());
                return true;
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
                return true;
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
