package xyz.bobkinn_.customdiscs;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.logging.Level;

public class PlayingMsgThread extends Thread {
    List<Player> players;
    TranslatableComponent actBarTr;
    PlayingMsgThread(List<Player> players, TranslatableComponent msg){
        this.players = players;
        this.actBarTr=msg;
    }

    private void send(ChatColor color){
        actBarTr.setColor(color);
        players.forEach(p -> p.spigot().sendMessage(ChatMessageType.ACTION_BAR, actBarTr));
    }

    @Override
    public void run() {
        try {
            send(ChatColor.GREEN);
            sleep(600);
            send(ChatColor.DARK_PURPLE);
            sleep(400);
            send(ChatColor.DARK_BLUE);
            sleep(400);
            send(ChatColor.DARK_GREEN);
            sleep(300);
            send(ChatColor.DARK_AQUA);
            sleep(50);
            send(ChatColor.GRAY);
        } catch (InterruptedException e){
            Main.LOGGER.log(Level.SEVERE, "Unexpected exception in PlayingMsgThread", e);
        }
    }
}
