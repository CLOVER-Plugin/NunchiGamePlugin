package yd.kingdom.nunchiGamePlugin.item;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import yd.kingdom.nunchiGamePlugin.NunchiGamePlugin;

public class CountdownItem {
    private final NunchiGamePlugin plugin;
    public CountdownItem(NunchiGamePlugin plugin){this.plugin=plugin;}

    public ItemStack build() {
        ItemStack it = new ItemStack(Material.TUBE_CORAL_FAN);
        ItemMeta im = it.getItemMeta();
        im.setDisplayName("§a게임 시작 카운트");
        it.setItemMeta(im);
        return it;
    }

    public void give(Player p){
        p.getInventory().addItem(build());
    }
}