package xyz.bobkinn_.customdiscs;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlayingMsgThread extends Thread{
    PlayerInteractEvent e;
    TranslatableComponent actBarTr;
    PlayingMsgThread(PlayerInteractEvent e, TranslatableComponent msg){
        this.e=e;
        this.actBarTr=msg;
    }

    @Override
    public void run() {
        try {
            actBarTr.setColor(ChatColor.GREEN);
            e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, actBarTr);
            sleep(600);
            actBarTr.setColor(ChatColor.DARK_PURPLE);
            e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, actBarTr);
            sleep(400);
            actBarTr.setColor(ChatColor.DARK_BLUE);
            e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, actBarTr);
            sleep(400);
            actBarTr.setColor(ChatColor.DARK_GREEN);
            e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, actBarTr);
            sleep(300);
            actBarTr.setColor(ChatColor.DARK_AQUA);
            e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, actBarTr);
            sleep(50);
            actBarTr.setColor(ChatColor.GRAY);
            e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, actBarTr);
            interrupt();
        } catch (InterruptedException e){
            e.printStackTrace();
        }
    }
}
