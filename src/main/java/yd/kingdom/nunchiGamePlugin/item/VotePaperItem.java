package yd.kingdom.nunchiGamePlugin.item;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;

public class VotePaperItem {
    public VotePaperItem(Plugin plugin){}

    public ItemStack build() {
        ItemStack it = new ItemStack(Material.PAPER);
        ItemMeta im = it.getItemMeta();
        im.setDisplayName("§d투표용지");
        im.setLore(Arrays.asList("§7투표용지를 우클릭하여 투표할 수 있습니다."));
        it.setItemMeta(im);
        return it;
    }

    public static boolean isVotePaper(ItemStack it) {
        return it != null && it.getType() == Material.PAPER
                && it.hasItemMeta() && "§d투표용지".equals(it.getItemMeta().getDisplayName());
    }
}