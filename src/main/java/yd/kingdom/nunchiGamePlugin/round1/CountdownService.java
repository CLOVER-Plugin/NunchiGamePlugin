package yd.kingdom.nunchiGamePlugin.round1;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class CountdownService {
    private final Plugin plugin;
    public CountdownService(Plugin plugin){ this.plugin=plugin; }

    public void startAll(int seconds, Runnable onEnd) {
        for (Player p : Bukkit.getOnlinePlayers()) show(p, seconds, onEnd);
    }
    public void show(Player p, int seconds, Runnable onEnd) {
        final int[] t = {seconds};
        Bukkit.getScheduler().runTaskTimer(plugin, task -> {
            if (t[0] <= 0) {
                p.sendTitle("§aGo!", "", 0, 20, 10);
                task.cancel();
                if (onEnd != null) onEnd.run();
                return;
            }
            p.sendTitle("§e" + t[0], "", 0, 20, 0);
            p.playSound(p.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.5f);
            t[0]--;
        }, 0L, 20L);
    }
}