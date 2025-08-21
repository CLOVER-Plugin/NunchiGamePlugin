package yd.kingdom.nunchiGamePlugin.round2;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {
    
    private final ArcherRoundManager archerRoundManager;
    
    public PlayerJoinListener(ArcherRoundManager archerRoundManager) {
        this.archerRoundManager = archerRoundManager;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // 2라운드가 진행 중이면 플레이어 등록
        if (archerRoundManager.isRunning()) {
            archerRoundManager.registerPlayer(player);
        }
    }
}
