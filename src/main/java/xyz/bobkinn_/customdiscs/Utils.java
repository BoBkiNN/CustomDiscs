package xyz.bobkinn_.customdiscs;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;

public class Utils {
    public static ArrayList<String> vanillaDiscs = null;

    public static void stopSound(Block sourceBlock, String sound){
        if (sourceBlock == null) return;
        for (Player player : sourceBlock.getWorld().getPlayers()){
            player.stopSound(sound, SoundCategory.RECORDS);
        }

    }

    public static boolean hasPermForCmd(CommandSender sender, String cmdName){
        return sender.hasPermission("customdiscs."+cmdName) || sender.isOp();
    }

    public static ArrayList<String> getMaterialList(){
        ArrayList<String> materials = new ArrayList<>();
        for (Material material : Material.values()){
            materials.add(material.getKey().getNamespace()+":"+material.getKey().getKey());
        }
        return materials;
    }

    public static ArrayList<String> getVanillaDiscsList(){
        if (vanillaDiscs != null) return vanillaDiscs;
        ArrayList<String> discs = new ArrayList<>();
        for (Material material : Material.values()){
            if (material.name().startsWith("LEGACY_")) {
                continue; // Skip legacy materials
            }
            if (material.name().startsWith("MUSIC_DISC_")) {
//                Main.logger.warning(material.toString());
                NamespacedKey key;
                try {
                    key = material.getKey();
                } catch (IllegalArgumentException e) {
                    Main.logger.warning("e" + material);
                    continue;
                }
                discs.add(key.toString());
            }
        }
        vanillaDiscs = discs;
        return vanillaDiscs;
    }
    public static ArrayList<String> getSoundsList(){
        ArrayList<String> sounds = new ArrayList<>();
        for (Sound sound : Sound.values()){
            sounds.add(sound.getKey().getNamespace()+":"+sound.getKey().getKey());
        }
        return sounds;
    }

    public static ArrayList<String> getIDsList(){
        ArrayList<String> ids = new ArrayList<>();
        int c = 0;
        for (CustomDisc ignored : Main.customDiscs){
            c++;
            ids.add(String.valueOf(c));
        }
        return ids;
    }

    public static String getNameBySound(String sound, FileConfiguration configuration){
        ArrayList<String> names = new ArrayList<>(configuration.getStringList("names"));
        Logger logger = Main.plugin.getLogger();

        for (String name : names){
            ArrayList<String> nameP = new ArrayList<>(Arrays.asList(name.split("=")));
            if (nameP.size()!=2){
                logger.warning("Disc name at position "+names.indexOf(name)+" incorrect");
                if (names.indexOf(name) == names.size()-1){
                    return sound;
                }
            }
            if (nameP.get(0).equals(sound)){
                return nameP.get(1);
            }
        }
        logger.warning("Name for sound \""+sound+"\" not found");
        return sound;
    }

    public static boolean isPaper() {
        try {
            Class.forName("com.destroystokyo.paper.ParticleBuilder");
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    public static String noPermMsg(){
        if (isPaper()){
            if (Main.configuration.getBoolean("override-paper-no-permission-msg",false)){
                String noPermMsg = Main.configuration.getString("messages.no-permission","&cI'm sorry, but you do not have permission to perform this command.");
                return ChatColor.translateAlternateColorCodes('&',noPermMsg);
            } else {
                String noPermMsg = YamlConfiguration.loadConfiguration(new File("paper.yml")).getString("messages.no-permission");
                if (noPermMsg==null){
                    noPermMsg=Main.configuration.getString("messages.no-permission","&cI'm sorry, but you do not have permission to perform this command.");

                }
                return ChatColor.translateAlternateColorCodes('&',noPermMsg);
            }
        } else {
            String noPermMsg = Main.configuration.getString("messages.no-permission","&cI'm sorry, but you do not have permission to perform this command.");
            return ChatColor.translateAlternateColorCodes('&',noPermMsg);
        }
    }


    public static ArrayList<CustomDisc> makeDiscs(FileConfiguration configuration){
        ArrayList<CustomDisc> discs = new ArrayList<>();
        Logger logger = Main.plugin.getLogger();

        ArrayList<String> rawDiscs = new ArrayList<>(configuration.getStringList("discs"));

        //checking
        for (String disc : rawDiscs){
            ArrayList<String> discP = new ArrayList<>(Arrays.asList(disc.split("=")));
            if (discP.size()!=3){
                logger.warning("Disc at position "+rawDiscs.indexOf(disc)+" incorrect, skipping");
                continue;
            }
            if (Material.matchMaterial(discP.get(0)) == null){
                logger.warning("Material of disc at position "+rawDiscs.indexOf(disc)+" not found, skipping");
                continue;
            }
            try {
                int cmd = Integer.parseInt(discP.get(2));
                if (cmd<=0){
                    logger.warning("CustomModelData of disc at position "+rawDiscs.indexOf(disc)+" not positive, skipping");
                    continue;
                }
            } catch (NumberFormatException e){
                logger.warning("CustomModelData of disc at position "+rawDiscs.indexOf(disc)+" not int, skipping");
                continue;
            }

            Material material = Material.matchMaterial(discP.get(0));
            int cmd = Integer.parseInt(discP.get(2));
            String name = getNameBySound(discP.get(1),configuration);
            discs.add(new CustomDisc(cmd,material,discP.get(1),name));
        }

        return discs;
    }

    public static String processDesc(String fName,boolean colored){
        if (colored){
            return ChatColor.translateAlternateColorCodes('&',fName);
        } else {
            return ChatColor.GRAY+ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&',fName));
        }
    }

    public static CustomDisc getDiscBySound(String sound, ArrayList<CustomDisc> discs){
        for (CustomDisc disc : discs){
            if (disc.getSound().equals(sound)){
                return disc;
            }
        }
        return null;
    }

}
