package xyz.bobkinn_.customdiscs;

import net.md_5.bungee.api.ChatColor;
import org.apache.commons.io.FileUtils;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class CommandHandler implements CommandExecutor {

    public String getTranslate(String key, String def){
        return ChatColor.translateAlternateColorCodes('&', Main.config.getString(key,def));
    }

    public void helpCmd(CommandSender sender){
        String top = getTranslate("messages.help-cmd.top","&cHelp for CustomDiscs:");
        String bottom = getTranslate("messages.help-cmd.bottom","&c============");
        String help = getTranslate("messages.help-cmd.help","&e/cd help &b- sends a help message");
        String list = getTranslate("messages.help-cmd.list","&e/cd list &b- get a list of custom discs");
        String get = getTranslate("messages.help-cmd.get","&e/cd get <id> [player] &b- get a loaded disc");
        String del = getTranslate("messages.help-cmd.del","&e/cd del <id> &b- remove disc from config");
        String add = getTranslate("messages.help-cmd.add","&e/cd add <item> <sound> <cmd> <name> &b- add a new disc");
        ArrayList<Boolean> perms = new ArrayList<>();
        perms.add(Utils.hasPermForCmd(sender,"help"));
        perms.add(Utils.hasPermForCmd(sender, "list"));
        perms.add(Utils.hasPermForCmd(sender,"get"));
        perms.add(Utils.hasPermForCmd(sender,"del"));
        perms.add(Utils.hasPermForCmd(sender,"add"));
        if (perms.contains(Boolean.TRUE)) sender.sendMessage(top);
        if (Utils.hasPermForCmd(sender,"help")) sender.sendMessage(help);
        if (Utils.hasPermForCmd(sender,"list")) sender.sendMessage(list);
        if (Utils.hasPermForCmd(sender,"get")) sender.sendMessage(get);
        if (Utils.hasPermForCmd(sender,"del")) sender.sendMessage(del);
        if (Utils.hasPermForCmd(sender,"add")) sender.sendMessage(add);
        if (perms.contains(Boolean.TRUE)) sender.sendMessage(bottom);

    }

    public boolean checkNoPermission(String perm, CommandSender player){
        return !player.hasPermission(perm) && !player.isOp();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length==0){
            if (checkNoPermission("customdiscs.help", sender)){
                sender.sendMessage(Utils.noPermMsg());
                return true;
            }
            helpCmd(sender);
            return true;
        }
        if (args[0].equalsIgnoreCase("help")){
            if (checkNoPermission("customdiscs.help", sender)){
                sender.sendMessage(Utils.noPermMsg());
                return true;
            }
            helpCmd(sender);
            return true;
        }
        if (args[0].equalsIgnoreCase("get")){
            if (checkNoPermission("customdiscs.get", sender)){
                sender.sendMessage(Utils.noPermMsg());
                return true;
            }

            if (args.length==1){
                String msg = getTranslate("messages.get-cmd.use","&cUse &e/cd get <id> [player]");
                sender.sendMessage(msg);
                return true;
            }

            int id;
            try {
                id = Integer.parseInt(args[1]);
            } catch (NumberFormatException e){
                String msg = getTranslate("messages.id-not-given","&cPlease provide disc id");
                sender.sendMessage(msg);
                return true;
            }
            CustomDisc disc;
            try {
                disc = Main.customDiscs.get(id-1);
            } catch (IndexOutOfBoundsException e){
                String msg = getTranslate("messages.id-not-found","&cDisc with this id not found");
                sender.sendMessage(msg);
                return true;
            }
            ItemStack item = new ItemStack(disc.getMaterial());
            ItemMeta meta = item.getItemMeta();
            if (meta == null) return false;
            meta.setCustomModelData(disc.getCmd());
            if (item.getType().isRecord()){
                meta.addItemFlags(ItemFlag.values());
            }
            String fName=Utils.processDesc(disc.getName(),Main.config.getBoolean("use-colored-desc"));
            meta.setLore(Collections.singletonList(fName));
            item.setItemMeta(meta);
            if (args.length>=3){
                Player p = sender.getServer().getPlayerExact(args[2]);
                if (p==null){
                    String msg = getTranslate("messages.get-cmd.player-not-found","&cPlayer with this name not found");
                    sender.sendMessage(msg);
                } else {
                    p.getInventory().addItem(item);
                    String msg = Main.config.getString("messages.get-cmd.success","&cGiven disc &b№%id% &cto &e%player%");
                    msg = msg.replace("%id%", String.valueOf(id)).replace("%player%", p.getName());
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',msg));
                }
            } else {
                if (sender instanceof Player){
                    Player player = (Player) sender;
                    player.getInventory().addItem(item);
                    String msg = Main.config.getString("messages.get-cmd.success","&cGiven disc &b№%id% &cto &e%player%");
                    msg = msg.replace("%id%", String.valueOf(id)).replace("%player%", player.getName());
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',msg));
                } else {
                    String msg = getTranslate("messages.get-cmd.console-use","&cUse &e/cd get <id> [player]&c for console");
                    sender.sendMessage(msg);
                }
            }
            return true;

        }

        if (args[0].equalsIgnoreCase("reload")){
            if (checkNoPermission("customdiscs.reload", sender)){
                sender.sendMessage(Utils.noPermMsg());
                return true;
            }
            Main.loadConfig();
            String rlMsg = getTranslate("messages.reload","&aReloaded!");
            sender.sendMessage(rlMsg);
            return true;
        }

        if (args[0].equalsIgnoreCase("del")){
            if (checkNoPermission("customdiscs.del", sender)){
                sender.sendMessage(Utils.noPermMsg());
                return true;
            }

            if (!(args.length >=2)) {
                String msg = getTranslate("messages.del-cmd.use","&cUse: &e/cd del <id>");
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',msg));
                return true;
            }

            int id;
            try {
                id = Integer.parseInt(args[1]);
            } catch (NumberFormatException e){
                String msg = getTranslate("messages.id-not-given","&cPlease provide disc id");
                sender.sendMessage(msg);
                return true;
            }
            List<Map<?, ?>> raw = Main.config.getMapList("discs");
            if (id > raw.size()){
                Main.customDiscs.remove(id - 1);
            } else {
                String msg = getTranslate("messages.id-not-found","&cDisc with this id not found");
                sender.sendMessage(msg);
                return true;
            }
            raw.remove(id-1);
            Main.config.set("discs",raw);
            File configFile = new File(Main.plugin.getDataFolder(),"config.yml");
            File configBackup = new File(Main.plugin.getDataFolder(),"config-backup.yml");

            try {
                FileUtils.copyFile(configFile,configBackup);
                Main.config.save(new File(Main.plugin.getDataFolder(),"config.yml"));
                if (!Main.config.getBoolean("messages.disable-config-warning",false)){
                    String msg = Main.config.getString("messages.config-warning","&cWarning!&e config.yml&c file was rewritten, old copy of file was saved as&e config-backup.yml&c. You can disable this warning in config");
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',msg));
                }

                return true;
            } catch (IOException e) {
                Main.LOGGER.log(Level.SEVERE, "Failed to save config", e);
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("add")){
            if (checkNoPermission("customdiscs.add", sender)){
                sender.sendMessage(Utils.noPermMsg());
                return true;
            }

            if (args.length<5){
                String msg = getTranslate("messages.add-cmd.use","&cUse: &e/cd add <item> <sound> <cmd> <name>");
                String msg2 = getTranslate("messages.add-cmd.use-example","&cExample: &e/cd add minecraft:music_disc_cat minecraft:music.game 2005 &2Menu Music");
                sender.sendMessage(msg, msg2);
                return true;
            } //add1 i2 s3 c4 n5

            if (Material.matchMaterial(args[1])==null){
                String msg = Main.config.getString("messages.add-cmd.material-not-exists","&cMaterial &e%item%&c not found");
                msg=msg.replace("%item%",args[1]);
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',msg));
                return true;
            }
            int cmd;
            String msg = Main.config.getString("messages.add-cmd.not-int","&cCustomModelData must be positive integer");
            try {
                if (Integer.parseInt(args[3])<=0){
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',msg));
                    return true;
                } else {
                    cmd = Integer.parseInt(args[3]);
                }
            } catch (NumberFormatException e){
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',msg));
                return true;
            }

            StringBuilder name = new StringBuilder();
            int i = 0;
            for (String arg : args){
                if (i<4){
                    i++;
                } else {
                    name.append(arg);
                    name.append(" ");
                }
            }
            name.deleteCharAt(name.length()-1);
            CustomDisc disc = new CustomDisc(cmd,Material.matchMaterial(args[1]),args[2],name.toString(), null);
            List<Map<?, ?>> raw = Main.config.getMapList("discs");
            raw.add(disc.serialize());
            Main.config.set("discs", raw);
            Main.customDiscs.add(disc);
            File configFile = new File(Main.plugin.getDataFolder(),"config.yml");
            File configBackup = new File(Main.plugin.getDataFolder(),"config-backup.yml");

            try {
                FileUtils.copyFile(configFile,configBackup);
                Main.config.save(new File(Main.plugin.getDataFolder(),"config.yml"));
                if (!Main.config.getBoolean("messages.disable-config-warning",false)){
                    String msg3 = Main.config.getString("messages.config-warning","&cWarning!&e config.yml&c file was rewritten, old copy of file was saved as&e config-backup.yml&c. You can disable this warning in config");
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',msg3));
                }
                String msg1 = Main.config.getString("messages.add-cmd.success","&cNew disc &e№%id%&c created");
                msg1 = msg1.replace("%id%",String.valueOf(Main.customDiscs.indexOf(disc)+1));
                msg1 = ChatColor.translateAlternateColorCodes('&',msg1);
                sender.sendMessage(msg1);
            } catch (IOException e) {
                Main.LOGGER.log(Level.SEVERE, "Failed to save config", e);
            }

        }

        if (args[0].equalsIgnoreCase("list")){
            if (checkNoPermission("customdiscs.list", sender)){
                sender.sendMessage(Utils.noPermMsg());
                return true;
            }
            String listTop = Main.config.getString("messages.list-cmd.top","&a----==== &bLoaded custom discs (%count%):");
            String listEntry = Main.config.getString("messages.list-cmd.entry","&c№%index% &7- S: &c%sound%&7, N: &c%name%&7,\nCMD: &c%cmd%&7, M: &c%item%");
            String listBottom = Main.config.getString("messages.list-cmd.bottom","none");
            listTop=ChatColor.translateAlternateColorCodes('&',listTop);
            listEntry=ChatColor.translateAlternateColorCodes('&',listEntry);
            if (!listBottom.equalsIgnoreCase("none")){
                listBottom=ChatColor.translateAlternateColorCodes('&',listBottom);
            }
            listTop=listTop.replace("%count%", String.valueOf(Main.customDiscs.size()));
            StringBuilder msg = new StringBuilder();
            msg.append(listTop);
            msg.append('\n');
            for (CustomDisc disc : Main.customDiscs){
                String listEntryMsg = listEntry.replace("%index%", String.valueOf(Main.customDiscs.indexOf(disc)+1));
                listEntryMsg=listEntryMsg.replace("%sound%", disc.getSound());
                listEntryMsg=listEntryMsg.replace("%name%", disc.getName());
                listEntryMsg=listEntryMsg.replace("%cmd%", String.valueOf(disc.getCmd()));
                listEntryMsg=listEntryMsg.replace("%item%",disc.getMaterial().getKey().getKey());
                msg.append(listEntryMsg).append('\n');
            }
            if (!listBottom.equalsIgnoreCase("none")){
                msg.append(listBottom);
            }
            sender.sendMessage(msg.toString());
            return true;
        }
        return true;
    }
}
