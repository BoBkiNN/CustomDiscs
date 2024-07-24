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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
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
                    Main.LOGGER.log(Level.WARNING, material.name(), e);
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
            if (Main.config.getBoolean("override-paper-no-permission-msg",false)){
                String noPermMsg = Main.config.getString("messages.no-permission","&cI'm sorry, but you do not have permission to perform this command.");
                return ChatColor.translateAlternateColorCodes('&',noPermMsg);
            } else {
                String noPermMsg = YamlConfiguration.loadConfiguration(new File("paper.yml")).getString("messages.no-permission");
                if (noPermMsg==null){
                    noPermMsg=Main.config.getString("messages.no-permission","&cI'm sorry, but you do not have permission to perform this command.");

                }
                return ChatColor.translateAlternateColorCodes('&',noPermMsg);
            }
        } else {
            String noPermMsg = Main.config.getString("messages.no-permission","&cI'm sorry, but you do not have permission to perform this command.");
            return ChatColor.translateAlternateColorCodes('&',noPermMsg);
        }
    }

    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> convertMap(Map<?, ?> map){
        try {
            return (Map<K, V>) map;
        } catch (ClassCastException e){
            return new HashMap<>();
        }
    }


    public static ArrayList<CustomDisc> makeDiscs(FileConfiguration configuration){
        ArrayList<CustomDisc> discs = new ArrayList<>();
        Logger logger = Main.plugin.getLogger();

        List<Map<?, ?>> raw = configuration.getMapList("discs");
        List<Map<String, Object>> section = new ArrayList<>();
        for (Map<?, ?> r : raw){
            Map<String, Object> converted = convertMap(r);
            if (!converted.isEmpty()) section.add(converted);
        }
        int i = 0;
        for (Map<String, Object> map : section){
            i++;
            try {
                String sound = (String) map.get("sound");
                String name = (String) map.get("name");
                String item = (String) map.get("item");
                Integer cmd = (Integer) map.get("cmd");
                Float volume = (Float) map.get("volume");
                if (sound == null){
                    logger.warning("Failed to load disc "+i+": 'sound' not found");
                    continue;
                }
                if (name == null){
                    logger.warning("Failed to load disc "+i+": 'name' not found");
                    continue;
                }
                if (item == null){
                    logger.warning("Failed to load disc "+i+": 'item' not found");
                    continue;
                }
                if (cmd == null){
                    logger.warning("Failed to load disc "+i+": 'cmd' not found");
                    continue;
                }
                if (cmd <= 0){
                    logger.warning("Failed to load disc "+i+": 'cmd' is not positive");
                    continue;
                }
                Material material = Material.matchMaterial(item);
                if (material == null){
                    logger.warning("Failed to load disc "+i+": unknown material");
                    continue;
                }
                CustomDisc disc = new CustomDisc(cmd, material, sound, name, volume);
                discs.add(disc);
            } catch (Exception e){
                logger.warning("Failed to load disc "+i+": "+e.getMessage());
            }
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
