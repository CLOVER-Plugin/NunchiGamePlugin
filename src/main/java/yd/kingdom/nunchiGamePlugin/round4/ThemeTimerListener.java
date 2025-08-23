package yd.kingdom.nunchiGamePlugin.round4;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import yd.kingdom.nunchiGamePlugin.NunchiGamePlugin;

public class ThemeTimerListener implements Listener {

    private final NunchiGamePlugin plugin;
    private final NamespacedKey key;

    public ThemeTimerListener(NunchiGamePlugin plugin) {
        this.plugin = plugin;
        this.key = new NamespacedKey(plugin, "round4_theme_shard");
    }

    private boolean isThemeShard(ItemStack item) {
        if (item == null || item.getType() != Material.AMETHYST_SHARD) return false;
        if (!item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        Integer flag = meta.getPersistentDataContainer().get(key, PersistentDataType.INTEGER);
        return flag != null && flag == 1;
    }

    @EventHandler
    public void onUse(PlayerInteractEvent e) {
        Action action = e.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return; // 허공/블럭 우클릭만

        Player p = e.getPlayer();
        ItemStack main = p.getInventory().getItemInMainHand();
        ItemStack off  = p.getInventory().getItemInOffHand();

        boolean mainShard = isThemeShard(main);
        boolean offShard  = isThemeShard(off);

        if (!mainShard && !offShard) return;

        // 양손에 들려 있으면 메인핸드 이벤트만 처리(중복 방지)
        if (e.getHand() == EquipmentSlot.OFF_HAND && mainShard) return;

        // 블럭 우클릭이면 기본 상호작용(문/상자 열림 등) 방지
        if (action == Action.RIGHT_CLICK_BLOCK) {
            e.setCancelled(true);
        }

        plugin.getThemeTimerManager().startGlobal(p);
    }
}