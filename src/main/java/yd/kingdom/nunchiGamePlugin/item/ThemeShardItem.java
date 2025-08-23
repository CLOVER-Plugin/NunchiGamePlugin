package yd.kingdom.nunchiGamePlugin.item;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import yd.kingdom.nunchiGamePlugin.NunchiGamePlugin;

import java.util.Arrays;

public class ThemeShardItem {
    private final NunchiGamePlugin plugin;
    private final NamespacedKey key;

    public ThemeShardItem(NunchiGamePlugin plugin) {
        this.plugin = plugin;
        this.key = new NamespacedKey(plugin, "round4_theme_shard");
    }

    public ItemStack build() {
        ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§5주제 뽑기");
        meta.setLore(Arrays.asList(
                "§7우클릭: §f3초 카운트다운 후",
                "§f§l주제 표시 + 5초 타이머(밀리초)"
        ));
        meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 1);
        item.setItemMeta(meta);
        return item;
    }

    public void give(Player p) {
        p.getInventory().addItem(build());
    }
}