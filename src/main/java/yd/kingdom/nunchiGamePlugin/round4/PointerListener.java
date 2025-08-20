package yd.kingdom.nunchiGamePlugin.round4;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import yd.kingdom.nunchiGamePlugin.NunchiGamePlugin;

public class PointerListener implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent e){
        if (e.getHand() != EquipmentSlot.HAND) return;
        Action action = e.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;
        Player shooter = e.getPlayer();
        ItemStack item = e.getItem();
        if (item == null || item.getType() != Material.STICK) return;

        PointerRoundManager manager = ((NunchiGamePlugin) Bukkit.getPluginManager().getPlugin("NunchiGamePlugin")).getPointerRoundManager();
        if (!manager.isAccepting()) return;

        Vector origin = shooter.getEyeLocation().toVector();
        Vector direction = shooter.getEyeLocation().getDirection();
        RayTraceResult rt = shooter.getWorld().rayTraceEntities(shooter.getEyeLocation(), direction, 30.0, entity -> entity instanceof Player && entity.getUniqueId() != shooter.getUniqueId());
        if (rt == null || rt.getHitEntity() == null || !(rt.getHitEntity() instanceof Player target)) return;

        manager.setLink(shooter, target);
        e.setCancelled(true);

        // 지목 카운트가 예약되어 있는 경우 실행
        PointerRoundManager.Pending pending = manager.consumePending(shooter.getUniqueId());
        if (pending != null) {
            manager.countAndHighlight(pending.startId(), pending.steps());
        }
    }

}
