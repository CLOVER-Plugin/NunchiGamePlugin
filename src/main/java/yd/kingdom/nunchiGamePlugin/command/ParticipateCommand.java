package yd.kingdom.nunchiGamePlugin.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import yd.kingdom.nunchiGamePlugin.round2.ArcherRoundManager;

public class ParticipateCommand implements CommandExecutor {
    private final ArcherRoundManager archerRoundManager;

    public ParticipateCommand(ArcherRoundManager archerRoundManager) {
        this.archerRoundManager = archerRoundManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c플레이어만 사용할 수 있는 명령어입니다.");
            return true;
        }

        Player player = (Player) sender;
        
        if (archerRoundManager.isRunning()) {
            player.sendMessage("§c2라운드가 진행 중입니다. 게임이 끝난 후 참여해주세요.");
            return true;
        }

        boolean added = archerRoundManager.addParticipant(player);
        if (added) {
            player.sendMessage("§a2라운드 참여자로 등록되었습니다!");
            archerRoundManager.updateScoreboard();
        } else {
            player.sendMessage("§c이미 참여자로 등록되어 있습니다.");
        }

        return true;
    }
}
