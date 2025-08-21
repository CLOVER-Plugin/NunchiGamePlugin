package yd.kingdom.nunchiGamePlugin.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import yd.kingdom.nunchiGamePlugin.NunchiGamePlugin;
import yd.kingdom.nunchiGamePlugin.round2.AreaSelectionManager;

public class AreaSetupCommand implements CommandExecutor {
    
    private final NunchiGamePlugin plugin;
    private final AreaSelectionManager areaSelectionManager;
    
    public AreaSetupCommand(NunchiGamePlugin plugin, AreaSelectionManager areaSelectionManager) {
        this.plugin = plugin;
        this.areaSelectionManager = areaSelectionManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c플레이어만 사용할 수 있는 명령어입니다.");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("nunchigame.areasetup")) {
            player.sendMessage("§c이 명령어를 사용할 권한이 없습니다.");
            return true;
        }
        
        if (args.length == 0) {
            showUsage(player);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "과녁1":
            case "과녁2":
            case "과녁3":
            case "방해1":
            case "방해2":
            case "방해3":
                if (areaSelectionManager.isSelecting(player)) {
                    player.sendMessage("§c이미 다른 영역을 선택하고 있습니다. 먼저 완료하거나 취소하세요.");
                    return true;
                }
                areaSelectionManager.startSelection(player, args[0]);
                break;
                
            case "취소":
                if (!areaSelectionManager.isSelecting(player)) {
                    player.sendMessage("§c선택 중인 영역이 없습니다.");
                    return true;
                }
                areaSelectionManager.cancelSelection(player);
                break;
                
            case "저장":
                if (args.length < 2) {
                    player.sendMessage("§c사용법: /영역설정 저장 <설정명>");
                    return true;
                }
                saveAreas(player, args[1]);
                break;
                
            case "불러오기":
                if (args.length < 2) {
                    player.sendMessage("§c사용법: /영역설정 불러오기 <설정명>");
                    return true;
                }
                loadAreas(player, args[1]);
                break;
                
            case "목록":
                listSavedAreas(player);
                break;
                
            case "삭제":
                if (args.length < 2) {
                    player.sendMessage("§c사용법: /영역설정 삭제 <설정명>");
                    return true;
                }
                deleteSavedAreas(player, args[1]);
                break;
                
            default:
                showUsage(player);
                break;
        }
        
        return true;
    }
    
    private void showUsage(Player player) {
        player.sendMessage("§e=== 영역 설정 명령어 ===");
        player.sendMessage("§f/영역설정 과녁1 §7- 과녁1 영역 선택 시작");
        player.sendMessage("§f/영역설정 과녁2 §7- 과녁2 영역 선택 시작");
        player.sendMessage("§f/영역설정 과녁3 §7- 과녁3 영역 선택 시작");
        player.sendMessage("§f/영역설정 방해1 §7- 방해1 영역 선택 시작");
        player.sendMessage("§f/영역설정 방해2 §7- 방해2 영역 선택 시작");
        player.sendMessage("§f/영역설정 방해3 §7- 방해3 영역 선택 시작");
        player.sendMessage("§f/영역설정 취소 §7- 현재 선택 취소");
        player.sendMessage("§f/영역설정 저장 <이름> §7- 현재 설정 저장");
        player.sendMessage("§f/영역설정 불러오기 <이름> §7- 저장된 설정 불러오기");
        player.sendMessage("§f/영역설정 목록 §7- 저장된 설정 목록");
        player.sendMessage("§f/영역설정 삭제 <이름> §7- 저장된 설정 삭제");
        player.sendMessage("§e=======================");
    }
    
    private void saveAreas(Player player, String name) {
        // 현재 설정된 영역 확인
        boolean hasAllAreas = true;
        for (int i = 1; i <= 3; i++) {
            String targetKey = "과녁" + i;
            String barrierKey = "방해" + i;
            
            if (!plugin.getArcherRoundManager().hasArea(targetKey) || 
                !plugin.getArcherRoundManager().hasArea(barrierKey)) {
                hasAllAreas = false;
                break;
            }
        }
        
        if (!hasAllAreas) {
            player.sendMessage("§c모든 영역이 설정되지 않았습니다!");
            player.sendMessage("§7과녁1~3, 방해1~3을 모두 설정한 후 저장하세요.");
            return;
        }
        
        // ArcherRoundManager에 설정 저장 요청
        plugin.getArcherRoundManager().saveConfig();
        player.sendMessage("§a설정이 config.yml에 저장되었습니다!");
        player.sendMessage("§7이제 /2라운드 명령어로 게임을 시작할 수 있습니다.");
    }
    
    private void loadAreas(Player player, String name) {
        // ArcherRoundManager에서 설정 다시 로드
        plugin.getArcherRoundManager().loadConfig();
        player.sendMessage("§a설정을 config.yml에서 불러왔습니다.");
    }
    
    private void listSavedAreas(Player player) {
        player.sendMessage("§e=== 저장된 영역 정보 ===");
        for (int i = 1; i <= 3; i++) {
            String targetKey = "과녁" + i;
            String barrierKey = "방해" + i;
            
            boolean targetSet = plugin.getArcherRoundManager().hasArea(targetKey);
            boolean barrierSet = plugin.getArcherRoundManager().hasArea(barrierKey);
            
            String targetStatus = targetSet ? "§a설정됨" : "§c미설정";
            String barrierStatus = barrierSet ? "§a설정됨" : "§c미설정";
            
            player.sendMessage("§7" + targetKey + ": " + targetStatus + " §7| " + barrierKey + ": " + barrierStatus);
        }
        player.sendMessage("§e=======================");
    }
    
    private void deleteSavedAreas(Player player, String name) {
        player.sendMessage("§c개별 영역 삭제는 지원하지 않습니다. 모든 영역을 다시 설정하세요.");
    }
}
