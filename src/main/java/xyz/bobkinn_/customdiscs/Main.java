package xyz.bobkinn_.customdiscs;

import com.jeff_media.customblockdata.CustomBlockData;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.apache.commons.io.IOUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Hopper;
import org.bukkit.block.data.type.Jukebox;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.BlockInventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Main extends JavaPlugin implements Listener {
    public final NamespacedKey customDiscKey = new NamespacedKey(this,"custom_disc");
    public static ArrayList<CustomDisc> customDiscs;
    public static Plugin plugin;
    public static FileConfiguration config;
    public static Logger LOGGER;
    public static float DEFAULT_SOUND_VOLUME = 4f;

    @Override
    public void onEnable() {
        LOGGER = getLogger();
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(this,this);
        plugin=this;
        config = getConfig();
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
                LOGGER.log(Level.WARNING, "Failed to copy config from plugin jar", e);
            }
        }
        config = YamlConfiguration.loadConfiguration(configFile);
        DEFAULT_SOUND_VOLUME = (float) config.getDouble("sound-volume", 4f);

        customDiscs=Utils.makeDiscs(config);
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

    public PersistentDataContainer getPdc(Block block){
        return new CustomBlockData(block, this);
    }

    public CustomDisc findDiscByItem(ItemStack stack){
        for (CustomDisc disc : customDiscs){
            if (!stack.getType().equals(disc.getMaterial())) continue;
            if (stack.getItemMeta() == null) continue;
            if (stack.getItemMeta().getCustomModelData() != disc.getCmd()) continue;
            return disc;
        }
        return null;
    }

    public List<Player> collectPlayers(Block block, float volume){
        Location loc = block.getLocation();
        World world = block.getWorld();
        float distance = 4*volume*16;
        float distanceSquared = distance*distance;
        List<Player> ret = new ArrayList<>();
        for (Player player : world.getPlayers()){
            if (player.getLocation().distanceSquared(loc) <= distanceSquared){
                ret.add(player);
            }
        }
        return ret;
    }

    @EventHandler
    public void onHopperMove(InventoryMoveItemEvent e){
        if (!(e.getSource().getHolder() instanceof Hopper)) return;
        if (!(e.getDestination().getHolder() instanceof BlockInventoryHolder)) return;
        BlockInventoryHolder holder = (BlockInventoryHolder) e.getDestination().getHolder();
        Block block = holder.getBlock();
        if (!block.getType().equals(Material.JUKEBOX)) return;
        PersistentDataContainer pdc = getPdc(block);
        if (pdc.has(customDiscKey, PersistentDataType.STRING)) {
            e.setCancelled(true);
            return;
        }
        CustomDisc disc = findDiscByItem(e.getItem());
        if (disc == null) return;
        pdc.set(customDiscKey, PersistentDataType.STRING,disc.getSound());
        block.getWorld().playSound(block.getLocation(),disc.getSound(),
                SoundCategory.RECORDS, disc.getVolumeOrDef(),1f);

        if (config.getBoolean("enable-playing_msg",true)){
            String fName = ChatColor.translateAlternateColorCodes('&',disc.getName());
            if (!config.getBoolean("use-colored-name-in-msg",false)){
                fName = ChatColor.stripColor(fName);
            }
            List<Player> players = collectPlayers(block, disc.getVolumeOrDef());
            TranslatableComponent actBarTr = new TranslatableComponent("record.nowPlaying", fName);
            new PlayingMsgThread(players, actBarTr).start();
        }
        e.setCancelled(true);
        e.getSource().remove(e.getItem());
    }

    @EventHandler
    public void onJukeBreak(BlockBreakEvent e){
        if (e.isCancelled()) return;
        Block brokenBlock = e.getBlock();
        if (!brokenBlock.getType().equals(Material.JUKEBOX)) return;
        PersistentDataContainer jukeStore = getPdc(brokenBlock);
        if (jukeStore.has(customDiscKey, PersistentDataType.STRING)) {
            String sound = jukeStore.get(customDiscKey, PersistentDataType.STRING);
            CustomDisc disc = Utils.getDiscBySound(sound,customDiscs);
            if (disc==null){
                e.getPlayer().sendMessage("TODO This disc does not exists");
                return;
            }

            jukeStore.remove(customDiscKey);
            Utils.stopSound(brokenBlock, disc.getSound());

            ItemStack drop = new ItemStack(disc.getMaterial());
            ItemMeta dropMeta = drop.getItemMeta();
            if (dropMeta == null) return;

            dropMeta.setCustomModelData(disc.getCmd());
            if (drop.getType().isRecord()){
                dropMeta.addItemFlags(ItemFlag.values());
            }

            String fName=Utils.processDesc(disc.getName(), config.getBoolean("use-colored-desc"));
            dropMeta.setLore(Collections.singletonList(fName));
            drop.setItemMeta(dropMeta);
            Item entity = brokenBlock.getWorld().dropItemNaturally(brokenBlock.getLocation(), drop);
            entity.setPickupDelay(10);
        }
    }

    @EventHandler
    public void onDiscInsert(PlayerInteractEvent e){
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK){
            return;
        }
        if (e.getClickedBlock() == null) return;
        Block clickedBlock = e.getClickedBlock();
        if (!clickedBlock.getBlockData().getMaterial().equals(Material.JUKEBOX)) return;
        if (clickedBlock.getBlockData() instanceof Jukebox) {
            Jukebox jukebox = (Jukebox) clickedBlock.getBlockData();
            if (jukebox.hasRecord()) return;
        }

        GameMode gameMode = e.getPlayer().getGameMode();

        if (!config.getBoolean("allow-spectator-use",false) && gameMode.equals(GameMode.SPECTATOR)){
            return;
        }

        if (gameMode.equals(GameMode.ADVENTURE)){
            return;
        }

        //drop if it has disc
        PersistentDataContainer jukeStore = getPdc(clickedBlock);
        if (jukeStore.has(customDiscKey, PersistentDataType.STRING)){
            String sound = jukeStore.get(customDiscKey,PersistentDataType.STRING);
//            e.getPlayer().sendMessage("Jukebox has disc "+sound);
            jukeStore.remove(customDiscKey);
            Utils.stopSound(clickedBlock,sound);
            CustomDisc disc = Utils.getDiscBySound(sound,customDiscs);
            if (disc == null){
//                e.getPlayer().sendMessage("TODO This disc does not exists");
                e.setCancelled(true);
                return;
            }

            ItemStack drop = new ItemStack(disc.getMaterial());
            ItemMeta dropMeta = drop.getItemMeta();
            if (dropMeta == null) return;

            dropMeta.setCustomModelData(disc.getCmd());

            String fName=Utils.processDesc(disc.getName(), config.getBoolean("use-colored-desc"));
            dropMeta.setLore(Collections.singletonList(fName));

            if (drop.getType().isRecord()){
                dropMeta.addItemFlags(ItemFlag.values());
            }
            drop.setItemMeta(dropMeta);

            clickedBlock.getWorld().dropItemNaturally(clickedBlock.getLocation().add(0,1.0,0), drop);
            e.setCancelled(true);
            e.getPlayer().swingMainHand();
            
        } else {
            ItemStack heldItem = e.getPlayer().getInventory().getItemInMainHand();
            if (!heldItem.hasItemMeta()) return;
            if (heldItem.getItemMeta() == null) return;
            if (!heldItem.getItemMeta().hasCustomModelData()){
                return;
            }
            if (!heldItem.getType().isRecord()) return;
            CustomDisc disc = findDiscByItem(heldItem);
            if (disc == null) return;
//            e.getPlayer().sendMessage(ChatColor.GREEN+"Correct disc: "+disc);
            PersistentDataContainer jukeEmpty = getPdc(clickedBlock);
            jukeEmpty.set(customDiscKey, PersistentDataType.STRING,disc.getSound());
            clickedBlock.getWorld().playSound(clickedBlock.getLocation(),disc.getSound(),
                    SoundCategory.RECORDS, disc.getVolumeOrDef(),1f);

            if (config.getBoolean("remove-item-in-creative",false) && gameMode.equals(GameMode.CREATIVE)){
                e.getPlayer().getInventory().setItemInMainHand(new ItemStack(Material.AIR));
            } else if (gameMode.equals(GameMode.SURVIVAL)){
                e.getPlayer().getInventory().setItemInMainHand(new ItemStack(Material.AIR));
            }

            if (config.getBoolean("enable-playing_msg",true)){
                String fName = ChatColor.translateAlternateColorCodes('&',disc.getName());
                if (!config.getBoolean("use-colored-name-in-msg",false)){
                    fName = ChatColor.stripColor(fName);
                }
                List<Player> players = collectPlayers(clickedBlock, disc.getVolumeOrDef());
                TranslatableComponent actBarTr = new TranslatableComponent("record.nowPlaying", fName);
                new PlayingMsgThread(players, actBarTr).start();
            }
            e.setCancelled(true);
        }
    }
}
