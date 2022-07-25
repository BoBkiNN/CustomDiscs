package xyz.bobkinn_.customdiscs;

import org.bukkit.Material;

public class CustomDisc {
    private final int cmd;
    private final Material material;
    private final String sound;
    private final String name;

    public CustomDisc(int cmd, Material material, String sound, String name) {
        this.cmd = cmd;
        this.material = material;
        this.sound = sound;
        this.name = name;
    }

    public int getCmd() {
        return cmd;
    }

    public Material getMaterial() {
        return material;
    }

    public String getSound() {
        return sound;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString(){
        return "{\"name\":\""+this.name+"\",\"sound\":\""+this.sound+"\",\"cmd\":\""+this.cmd+"\",\"material\":\""+this.material+"\"}";
    }
}
