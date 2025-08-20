package yd.kingdom.nunchiGamePlugin.round2;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;

public class TargetListener implements Listener {

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent e) {
        if (!(e.getEntity().getShooter() instanceof Player p)) return;
        // 열린 타깃 범위에 맞았는지 판단 → 타깃별 적중 플레이어 목록에 기록
    }

}
