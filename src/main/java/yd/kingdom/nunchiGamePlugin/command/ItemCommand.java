package yd.kingdom.nunchiGamePlugin.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import yd.kingdom.nunchiGamePlugin.NunchiGamePlugin;

public class ItemCommand implements CommandExecutor {
    private final NunchiGamePlugin plugin;
    public ItemCommand(NunchiGamePlugin plugin){ this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage("플레이어만 사용 가능합니다."); return true; }
        if (args.length != 1) { player.sendMessage("/아이템 <카운트|투표>"); return true; }

        String sub = args[0];
        switch (sub) {
            case "카운트" -> {
                plugin.getCountdownItem().give(player);
                player.sendMessage("카운트 아이템 지급 완료");
            }
            case "투표" -> {
                player.getInventory().addItem(plugin.getVotePaperItem().build());
                player.sendMessage("투표용지 지급 완료");
            }
            default -> player.sendMessage("/아이템 <카운트|투표>");
        }
        return true;
    }
}


