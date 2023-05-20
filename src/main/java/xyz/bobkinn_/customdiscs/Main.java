package xyz.bobkinn_.customdiscs;

import com.jeff_media.customblockdata.CustomBlockData;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.apache.commons.io.IOUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Logger;

public final class Main extends JavaPlugin implements Listener {
    NamespacedKey namespacedKey = new NamespacedKey(this,"custom_disc");
    static ArrayList<CustomDisc> customDiscs;
    public static Plugin plugin;
    static FileConfiguration configuration;
    public static Logger logger;

    @Override
    public void onEnable() {
        logger = getLogger();
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(this,this);
        plugin=this;
        configuration = getConfig();
        loadConfig();
        PluginCommand cmd = getCommand("customdiscs");
        if (cmd != null){
            cmd.setExecutor(new CommandHandler());
            cmd.setTabCompleter(new CommandTabCompleter());
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static void loadConfig(){
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            if (!dataFolder.mkdirs()){
                plugin.getLogger().severe("Failed to create data folder");
            }
        }
        File configFile = new File(plugin.getDataFolder(),"config.yml");
        if (!configFile.exists()) {
            try {
                boolean c = configFile.createNewFile();
                if (!c) throw new IOException("Failed to create config file");
                InputStream jarCfg = plugin.getResource("config.yml");
                if (jarCfg == null) throw new IOException("Failed to find config file");
                OutputStream outputStream = Files.newOutputStream(configFile.toPath());
                IOUtils.copy(jarCfg,outputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        configuration = YamlConfiguration.loadConfiguration(configFile);

        customDiscs=Utils.makeDiscs(configuration);
//        plugin.getLogger().info("Disc debug:");
//        for (CustomDisc disc : customDiscs){
//            plugin.getLogger().info(disc.toString());
//        }
    }

//   @EventHandler
//   public void onJukeExplode(BlockExplodeEvent e){
//       if (e.isCancelled()) return;
//       // not work
//   }

    @EventHandler
    public void onJukeBreak(BlockBreakEvent e){
        if (e.isCancelled()) return;
        Block brokenBlock = e.getBlock();
        if (!brokenBlock.getBlockData().getMaterial().equals(Material.JUKEBOX)){return;}
        PersistentDataContainer jukeStore = new CustomBlockData(brokenBlock,this);
        if (jukeStore.has(namespacedKey, PersistentDataType.STRING)) {
            String sound = jukeStore.get(namespacedKey, PersistentDataType.STRING);
            CustomDisc disc = Utils.getDiscBySound(sound,customDiscs);
            if (disc==null){
                e.getPlayer().sendMessage("TODO This disc does not exists");
                return;
            }

            jukeStore.remove(namespacedKey);
            Utils.stopSound(brokenBlock, disc.getSound());


            ItemStack drop = new ItemStack(disc.getMaterial());
            ItemMeta dropMeta = drop.getItemMeta();
            if (dropMeta == null) return;

            dropMeta.setCustomModelData(disc.getCmd());
            if (drop.getType().isRecord()){
                dropMeta.addItemFlags(ItemFlag.values());
            }


            String fName=Utils.processDesc(disc.getName(),configuration.getBoolean("use-colored-desc"));
            dropMeta.setLore(Collections.singletonList(fName));
            drop.setItemMeta(dropMeta);
            brokenBlock.getWorld().dropItemNaturally(brokenBlock.getLocation(), drop);
        }
    }

    @EventHandler
    public void onDiscInsert(PlayerInteractEvent e){
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK){
            return;
        }
        if (e.getClickedBlock() == null){return;}
        Block clickedBlock = e.getClickedBlock();
        if (!clickedBlock.getBlockData().getMaterial().equals(Material.JUKEBOX)){return;}

        if (clickedBlock.getBlockData().getAsString(false).contains("has_record=true")){
            return;
        }

        GameMode pGm = e.getPlayer().getGameMode();

        if (!configuration.getBoolean("allow-spectator-use",false) && pGm.equals(GameMode.SPECTATOR)){
            return;
        }

        if (pGm.equals(GameMode.ADVENTURE)){
            return;
        }

        //drop if it has disc
        PersistentDataContainer jukeStore = new CustomBlockData(clickedBlock,this);
        if (jukeStore.has(namespacedKey, PersistentDataType.STRING)){
            String sound = jukeStore.get(namespacedKey,PersistentDataType.STRING);
//            e.getPlayer().sendMessage("Jukebox has disc "+sound);
            jukeStore.remove(namespacedKey);
            Utils.stopSound(clickedBlock,sound);
            CustomDisc disc = Utils.getDiscBySound(sound,customDiscs);
            if (disc == null){
                e.getPlayer().sendMessage("TODO This disc does not exists");
                e.setCancelled(true);
                return;
            }

            ItemStack drop = new ItemStack(disc.getMaterial());
            ItemMeta dropMeta = drop.getItemMeta();
            if (dropMeta == null) return;

            dropMeta.setCustomModelData(disc.getCmd());

            String fName=Utils.processDesc(disc.getName(),configuration.getBoolean("use-colored-desc"));
            dropMeta.setLore(Collections.singletonList(fName));

            if (drop.getType().isRecord()){
                dropMeta.addItemFlags(ItemFlag.values());
            }
            drop.setItemMeta(dropMeta);

            clickedBlock.getWorld().dropItemNaturally(clickedBlock.getLocation().add(0,1.0,0), drop);
            e.setCancelled(true);
            
        } else {
            ItemStack heldItem = e.getPlayer().getInventory().getItemInMainHand();
            if (!heldItem.hasItemMeta()) return;
            if (heldItem.getItemMeta() == null) return;
            if (!heldItem.getItemMeta().hasCustomModelData()){
                return;
            }
            for (CustomDisc disc : customDiscs){

                // check is disc correct
                boolean correctDisc = false;
                if (heldItem.getType().equals(disc.getMaterial())){
                    if (heldItem.getItemMeta().getCustomModelData() == disc.getCmd()) {
                        correctDisc=true;
                    }
                }

                if (correctDisc){
//                    e.getPlayer().sendMessage(ChatColor.GREEN+"Correct disc");
//                    e.getPlayer().sendMessage(ChatColor.YELLOW+disc.toString());
                    if (!e.getPlayer().getInventory().getItemInMainHand().getType().isRecord()){
                        return;
                    }

                    PersistentDataContainer jukeEmpty = new CustomBlockData(clickedBlock,this);
                    jukeEmpty.set(namespacedKey, PersistentDataType.STRING,disc.getSound());
                    // old 3.5f
                    clickedBlock.getWorld().playSound(clickedBlock.getLocation(),disc.getSound(), SoundCategory.RECORDS,1.0f,1f);

                    if (configuration.getBoolean("remove-item-in-creative",false) && pGm.equals(GameMode.CREATIVE)){
                        e.getPlayer().getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                    } else if (pGm.equals(GameMode.SURVIVAL)){
                        e.getPlayer().getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                    }

                    if (configuration.getBoolean("enable-playing_msg",true)){
                        String fName = ChatColor.translateAlternateColorCodes('&',disc.getName());
                        if (!configuration.getBoolean("use-colored-name-in-msg",false)){
                            fName = ChatColor.stripColor(fName);
                        }
                        TranslatableComponent actBarTr = new TranslatableComponent("record.nowPlaying", fName);
                        new PlayingMsgThread(e,actBarTr).start();
                    }
                    e.setCancelled(true);
                    break;
                }
            }
        }
    }
}
