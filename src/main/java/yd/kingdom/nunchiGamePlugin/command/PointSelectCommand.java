package yd.kingdom.nunchiGamePlugin.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import yd.kingdom.nunchiGamePlugin.NunchiGamePlugin;
import yd.kingdom.nunchiGamePlugin.round4.PointerRoundManager;

public class PointSelectCommand implements CommandExecutor {
    private final NunchiGamePlugin plugin;
    public PointSelectCommand(NunchiGamePlugin plugin){ this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player owner)) { sender.sendMessage("플레이어만 사용 가능합니다."); return true; }
        if (args.length != 2) { owner.sendMessage("/지목 <플레이어이름> <숫자>"); return true; }

        Player start = Bukkit.getPlayerExact(args[0]);
        if (start == null) { owner.sendMessage("대상 플레이어를 찾을 수 없습니다."); return true; }
        int steps;
        try { steps = Integer.parseInt(args[1]); }
        catch (NumberFormatException e){ owner.sendMessage("숫자를 올바르게 입력하세요."); return true; }
        if (steps < 0) { owner.sendMessage("숫자는 0 이상이어야 합니다."); return true; }

        PointerRoundManager manager = plugin.getPointerRoundManager();
        manager.setPending(owner.getUniqueId(), start.getUniqueId(), steps);
        manager.beginAccepting();
        owner.sendMessage("지정 완료. 스틱으로 대상 플레이어를 우클릭해 연결을 시작하세요.");
        return true;
    }
}


