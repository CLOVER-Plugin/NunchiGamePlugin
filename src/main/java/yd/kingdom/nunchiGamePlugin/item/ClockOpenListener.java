package yd.kingdom.nunchiGamePlugin.item;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import yd.kingdom.nunchiGamePlugin.NunchiGamePlugin;
import yd.kingdom.nunchiGamePlugin.gui.TeleportGUI;

public class ClockOpenListener implements Listener {
    private final NunchiGamePlugin plugin;
    public ClockOpenListener(NunchiGamePlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;
        Action action = e.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

        Player p = e.getPlayer();

        // 허공 우클릭에서도 확실히 아이템을 가져오기
        ItemStack it = p.getInventory().getItem(e.getHand());
        if (it == null || it.getType() != Material.BREEZE_ROD) return;

        e.setCancelled(true);
        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.0f);
        p.openInventory(TeleportGUI.build(plugin));
    }
}