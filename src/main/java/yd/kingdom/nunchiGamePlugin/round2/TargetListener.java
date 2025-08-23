package yd.kingdom.nunchiGamePlugin.round2;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;


public class TargetListener implements Listener {
    
    private final ArcherRoundManager archerRoundManager;
    
    public TargetListener(ArcherRoundManager archerRoundManager) {
        this.archerRoundManager = archerRoundManager;
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent e) {
        if (!(e.getEntity() instanceof Arrow)) return;
        if (!(e.getEntity().getShooter() instanceof Player player)) return;
        if (archerRoundManager == null || !archerRoundManager.isRunning()) return;

        
        // 맞은 블럭이 과녁 블럭인지 확인
        Block hitBlock = e.getHitBlock();
        if (hitBlock == null) return;
        
        Location hitLocation = hitBlock.getLocation();
        
        // 해당 위치가 과녁에 속하는지 확인
        if (!archerRoundManager.isTargetOpen(hitLocation)) return;
        
        // 화살 제거
        e.getEntity().remove();
        
        // 과녁 맞춤 처리
        archerRoundManager.handleTargetHit(hitLocation, player);
        
        // 플레이어에게 피드백
        player.sendMessage("§a과녁을 맞췄습니다!");
    }
}
