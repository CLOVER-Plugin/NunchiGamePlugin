package yd.kingdom.nunchiGamePlugin.round2;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.HashMap;
import java.util.Map;

public class AreaSelectionManager implements Listener {

    private final Map<Player, SelectionState> playerSelections = new HashMap<>();
    private final ArcherRoundManager archerRoundManager;

    public AreaSelectionManager(ArcherRoundManager archerRoundManager) {
        this.archerRoundManager = archerRoundManager;
    }

    // 선택 상태
    static class SelectionState {
        String command;
        Location pos1;
        Location pos2;
        SelectionState(String command) { this.command = command; }
        boolean isComplete() { return pos1 != null && pos2 != null; }
    }

    public void startSelection(Player player, String command) {
        playerSelections.put(player, new SelectionState(command));
        player.sendMessage("§a" + command + " 영역을 선택하세요!");
        player.sendMessage("§7첫 번째 좌클릭 → pos1, 두 번째 좌클릭 → pos2");
    }

    public boolean isSelecting(Player p) {
        return playerSelections.containsKey(p);
    }

    public void cancelSelection(Player player) {
        playerSelections.remove(player);
        player.sendMessage("§c영역 선택이 취소되었습니다.");
    }

    public SelectionState getSelectionState(Player player) {
        return playerSelections.get(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        SelectionState state = playerSelections.get(player);
        if (state == null) return;

        // 메인핸드만 처리 (보조핸드 이중처리 방지)
        if (event.getHand() != EquipmentSlot.HAND) return;

        // 좌클릭 블럭만 pos1/pos2로 사용
        if (event.getAction() != Action.LEFT_CLICK_BLOCK) return;

        // 상호작용/파괴 모두 차단
        event.setCancelled(true);
        event.setUseInteractedBlock(Event.Result.DENY);
        event.setUseItemInHand(Event.Result.DENY);

        Location clickedLocation = event.getClickedBlock().getLocation();

        if (state.pos1 == null) {
            state.pos1 = clickedLocation;
            player.sendMessage("§a첫 번째 위치(pos1): §7("
                    + clickedLocation.getBlockX() + ", "
                    + clickedLocation.getBlockY() + ", "
                    + clickedLocation.getBlockZ() + ")");
            player.sendMessage("§7이제 두 번째 위치를 클릭하세요.");
            return;
        }

        // pos2 설정
        state.pos2 = clickedLocation;
        player.sendMessage("§a두 번째 위치(pos2): §7("
                + clickedLocation.getBlockX() + ", "
                + clickedLocation.getBlockY() + ", "
                + clickedLocation.getBlockZ() + ")");

        int minX = Math.min(state.pos1.getBlockX(), state.pos2.getBlockX());
        int maxX = Math.max(state.pos1.getBlockX(), state.pos2.getBlockX());
        int minY = Math.min(state.pos1.getBlockY(), state.pos2.getBlockY());
        int maxY = Math.max(state.pos1.getBlockY(), state.pos2.getBlockY());
        int minZ = Math.min(state.pos1.getBlockZ(), state.pos2.getBlockZ());
        int maxZ = Math.max(state.pos1.getBlockZ(), state.pos2.getBlockZ());

        int width = maxX - minX + 1;
        int height = maxY - minY + 1;
        int depth = maxZ - minZ + 1;

        player.sendMessage("§6=== " + state.command + " 영역 설정 완료 ===");
        player.sendMessage("§7Pos1: (" + state.pos1.getBlockX() + ", " + state.pos1.getBlockY() + ", " + state.pos1.getBlockZ() + ")");
        player.sendMessage("§7Pos2: (" + state.pos2.getBlockX() + ", " + state.pos2.getBlockY() + ", " + state.pos2.getBlockZ() + ")");
        player.sendMessage("§7크기: " + width + "x" + height + "x" + depth + " 블럭");
        player.sendMessage("§7총 블럭 수: " + (width * height * depth) + "개");
        player.sendMessage("§6================================");

        // 매니저에 전달
        archerRoundManager.setSelectedArea(state.command, state.pos1, state.pos2);

        // 종료 및 상태 표시
        playerSelections.remove(player);
        showCurrentAreaStatus(player);
        checkAllAreasSet(player);
    }

    // 블럭 금 가는 단계 자체를 차단
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onBlockDamage(BlockDamageEvent event) {
        if (isSelecting(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    // 실제 파괴 단계도 최우선으로 차단
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onBlockBreak(BlockBreakEvent event) {
        if (isSelecting(event.getPlayer())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§c영역 선택 중에는 블럭을 파괴할 수 없습니다!");
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        playerSelections.remove(event.getPlayer());
    }

    private void checkAllAreasSet(Player player) {
        boolean allSet = true;
        for (int i = 1; i <= 3; i++) {
            if (!archerRoundManager.hasArea("과녁" + i) || !archerRoundManager.hasArea("방해" + i)) {
                allSet = false; break;
            }
        }
        if (allSet) {
            player.sendMessage("§a§l모든 영역이 설정되었습니다!");
            player.sendMessage("§7이제 /영역설정 저장 <이름> 으로 저장하거나 /2라운드 로 시작하세요.");
        } else {
            player.sendMessage("§e아직 설정되지 않은 영역이 있습니다. 계속 설정하세요.");
        }
    }

    private void showCurrentAreaStatus(Player player) {
        player.sendMessage("§e=== 현재 설정된 영역 상태 ===");
        for (int i = 1; i <= 3; i++) {
            String targetKey = "과녁" + i;
            String barrierKey = "방해" + i;
            String targetStatus = archerRoundManager.hasArea(targetKey) ? "§a설정됨" : "§c미설정";
            String barrierStatus = archerRoundManager.hasArea(barrierKey) ? "§a설정됨" : "§c미설정";
            player.sendMessage("§7" + targetKey + ": " + targetStatus + " §7| " + barrierKey + ": " + barrierStatus);
        }
        player.sendMessage("§e================================");
    }
}