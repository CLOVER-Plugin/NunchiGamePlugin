package yd.kingdom.nunchiGamePlugin.config;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TeleportConfig {
    private final File file;
    private FileConfiguration conf;

    public enum Key { spawn, r1, r2, r3, r4, r5 }

    public TeleportConfig(org.bukkit.plugin.Plugin plugin) {
        this.file = new File(plugin.getDataFolder(), "teleports.yml");
        if (!file.exists()) {
            plugin.saveResource("teleports.yml", false);
        }
        this.conf = YamlConfiguration.loadConfiguration(file);
    }

    public void set(Key key, Location loc) {
        Map<String, Object> m = new HashMap<>();
        m.put("world", loc.getWorld().getName());
        m.put("x", loc.getX());
        m.put("y", loc.getY());
        m.put("z", loc.getZ());
        m.put("yaw", loc.getYaw());
        m.put("pitch", loc.getPitch());
        conf.set(key.name(), m);
        save();
    }

    public Location get(Key key) {
        if (!conf.isConfigurationSection(key.name())) return null;
        String w = conf.getString(key.name() + ".world", "world");
        World world = Bukkit.getWorld(w);
        if (world == null) world = Bukkit.getWorlds().get(0);
        double x = conf.getDouble(key.name() + ".x", 0.5);
        double y = conf.getDouble(key.name() + ".y", 64.0);
        double z = conf.getDouble(key.name() + ".z", 0.5);
        float yaw = (float) conf.getDouble(key.name() + ".yaw", 0.0);
        float pitch = (float) conf.getDouble(key.name() + ".pitch", 0.0);
        return new Location(world, x, y, z, yaw, pitch);
    }

    private void save() {
        try { conf.save(file); } catch (IOException ignored) {}
    }

    public void reload() {
        this.conf = YamlConfiguration.loadConfiguration(file);
    }
}