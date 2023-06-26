package xyz.bobkinn_.customdiscs;

import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class CustomDisc implements ConfigurationSerializable {
    private final int cmd;
    private final Material material;
    private final String sound;
    private final String name;
    private final Float volume;

    public CustomDisc(int cmd, Material material, String sound, String name,@Nullable Float volume) {
        this.cmd = cmd;
        this.material = material;
        this.sound = sound;
        this.name = name;
        this.volume = volume;
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

    public @Nullable Float getVolume() {
        return volume;
    }

    @Override
    public String toString() {
        return "CustomDisc{" +
                "cmd=" + cmd +
                ", material=" + material +
                ", sound='" + sound + '\'' +
                ", name='" + name + '\'' +
                (volume != null ? ", volume=" + volume : "")
                 +
                '}';
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> ret = new HashMap<>();
        ret.put("sound", sound);
        ret.put("name", name);
        ret.put("item", material.getKey().toString());
        ret.put("cmd", cmd);
        if (volume != null){
            ret.put("volume", volume);
        }
        return ret;
    }
}
