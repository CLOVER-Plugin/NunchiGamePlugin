package yd.kingdom.nunchiGamePlugin.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import yd.kingdom.nunchiGamePlugin.NunchiGamePlugin;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class TeleportGUI {

    public static final String TITLE = "§eTeleport Menu";
    private static final Set<Integer> PINK_FRAME = new HashSet<>(Arrays.asList(
            0,1,2,3,4,5,6,7,8,9,17,18,26,27,28,29,30,31,32,33,34,35
    ));

    public static Inventory build(NunchiGamePlugin plugin) {
        Inventory inv = Bukkit.createInventory(null, 36, TITLE);

        // 분홍색 유리판 프레임
        ItemStack pane = new ItemStack(Material.PINK_STAINED_GLASS_PANE);
        ItemMeta pm = pane.getItemMeta(); pm.setDisplayName(" ");
        pane.setItemMeta(pm);
        PINK_FRAME.forEach(s -> inv.setItem(s, pane));

        // 13: 네더의 별 → spawn
        inv.setItem(13, named(Material.NETHER_STAR, "§bSpawn"));
        // 20~24: 관/뇌/사방/거품/불 산호 → r1..r5
        inv.setItem(20, named(Material.HORN_CORAL, "§dRound 1"));
        inv.setItem(21, named(Material.BRAIN_CORAL, "§dRound 2"));
        inv.setItem(22, named(Material.TUBE_CORAL, "§dRound 3"));
        inv.setItem(23, named(Material.BUBBLE_CORAL, "§dRound 4"));
        inv.setItem(24, named(Material.FIRE_CORAL, "§dRound 5"));

        return inv;
    }

    private static ItemStack named(Material m, String name) {
        ItemStack it = new ItemStack(m);
        ItemMeta im = it.getItemMeta();
        im.setDisplayName(name);
        it.setItemMeta(im);
        return it;
    }
}