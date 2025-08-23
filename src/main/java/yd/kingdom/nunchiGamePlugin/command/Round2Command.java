package yd.kingdom.nunchiGamePlugin.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import yd.kingdom.nunchiGamePlugin.NunchiGamePlugin;

public class Round2Command implements CommandExecutor {
    
    private final NunchiGamePlugin plugin;
    
    public Round2Command(NunchiGamePlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c플레이어만 사용할 수 있는 명령어입니다.");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("nunchigame.round2")) {
            player.sendMessage("§c이 명령어를 사용할 권한이 없습니다.");
            return true;
        }
        
        if (args.length == 0) {
            // 2라운드 시작
            if (plugin.getArcherRoundManager().isRunning()) {
                player.sendMessage("§c2라운드가 이미 진행 중입니다!");
                return true;
            }
            
            plugin.getArcherRoundManager().start();
            return true;
        }
        
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("중단") || args[0].equalsIgnoreCase("stop")) {
                if (!plugin.getArcherRoundManager().isRunning()) {
                    player.sendMessage("§c2라운드가 진행 중이 아닙니다!");
                    return true;
                }
                
                plugin.getArcherRoundManager().stop();
                player.sendMessage("§a2라운드가 중단되었습니다.");
                return true;
            }
        }
        
        player.sendMessage("§e사용법:");
        player.sendMessage("§f/2라운드 §7- 2라운드 시작");
        player.sendMessage("§f/2라운드 중단 §7- 2라운드 중단");
        return true;
    }
}
