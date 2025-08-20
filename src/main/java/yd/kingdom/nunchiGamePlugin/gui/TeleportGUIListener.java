package yd.kingdom.nunchiGamePlugin.gui;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import yd.kingdom.nunchiGamePlugin.NunchiGamePlugin;
import yd.kingdom.nunchiGamePlugin.config.TeleportConfig;

public class TeleportGUIListener implements Listener {
    private final NunchiGamePlugin plugin;
    public TeleportGUIListener(NunchiGamePlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getView() == null || e.getView().getTitle() == null) return;
        if (!TeleportGUI.TITLE.equals(e.getView().getTitle())) return;
        e.setCancelled(true);

        ItemStack clicked = e.getCurrentItem();
        if (clicked == null) return;
        if (!(e.getWhoClicked() instanceof Player p)) return;

        Material m = clicked.getType();
        TeleportConfig cfg = plugin.getTeleportConfig();
        Location dest = null;

        switch (m) {
            case NETHER_STAR -> dest = cfg.get(TeleportConfig.Key.spawn);
            case HORN_CORAL -> dest = cfg.get(TeleportConfig.Key.r1);
            case BRAIN_CORAL -> dest = cfg.get(TeleportConfig.Key.r2);
            case TUBE_CORAL -> dest = cfg.get(TeleportConfig.Key.r3);
            case BUBBLE_CORAL -> dest = cfg.get(TeleportConfig.Key.r4);
            case FIRE_CORAL -> dest = cfg.get(TeleportConfig.Key.r5);
            default -> {}
        }
        if (dest != null) {
            p.closeInventory();
            p.teleportAsync(dest);
        }
    }
}