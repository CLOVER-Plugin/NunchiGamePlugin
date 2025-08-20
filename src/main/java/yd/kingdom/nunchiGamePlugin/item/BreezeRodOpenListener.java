package yd.kingdom.nunchiGamePlugin.item;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import yd.kingdom.nunchiGamePlugin.NunchiGamePlugin;
import yd.kingdom.nunchiGamePlugin.gui.TeleportGUI;

public class BreezeRodOpenListener implements Listener {
    private final NunchiGamePlugin plugin;
    public BreezeRodOpenListener(NunchiGamePlugin plugin) { this.plugin = plugin; }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;
        if (e.getItem() == null || e.getItem().getType() != Material.BREEZE_ROD) return;
        Player p = e.getPlayer();
        p.openInventory(TeleportGUI.build(plugin));
    }
}