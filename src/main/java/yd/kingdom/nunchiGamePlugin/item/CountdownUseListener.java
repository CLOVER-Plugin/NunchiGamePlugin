package yd.kingdom.nunchiGamePlugin.item;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import yd.kingdom.nunchiGamePlugin.NunchiGamePlugin;

public class CountdownUseListener implements Listener {
    private final NunchiGamePlugin plugin;
    public CountdownUseListener(NunchiGamePlugin plugin){ this.plugin = plugin; }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
    public void onInteract(PlayerInteractEvent e) {
        Action a = e.getAction();
        if (a != Action.RIGHT_CLICK_AIR && a != Action.RIGHT_CLICK_BLOCK) return;

        // 허공 우클릭에서도 확실히 아이템을 가져오도록
        ItemStack item = e.getPlayer().getInventory().getItem(e.getHand());
        if (item == null || item.getType() != Material.SLIME_BALL) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        // 색코드 차이 방지: 색 제거 후 비교
        String name = org.bukkit.ChatColor.stripColor(meta.getDisplayName());
        if (!"게임 시작 카운트".equals(name)) return;

        // 더블트리거 방지: 메인핸드만 처리 (오프핸드도 허용하려면 이 줄 삭제)
        if (e.getHand() != EquipmentSlot.HAND) return;

        e.setCancelled(true);
        plugin.getCountdownService().startAll(3, null);
    }
}