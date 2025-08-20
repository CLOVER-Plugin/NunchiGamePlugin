package yd.kingdom.nunchiGamePlugin.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import yd.kingdom.nunchiGamePlugin.NunchiGamePlugin;

public class VoteStartCommand implements CommandExecutor {
    private final NunchiGamePlugin plugin;
    public VoteStartCommand(NunchiGamePlugin plugin){ this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        plugin.getVoteManager().start(30);
        return true;
    }
}


