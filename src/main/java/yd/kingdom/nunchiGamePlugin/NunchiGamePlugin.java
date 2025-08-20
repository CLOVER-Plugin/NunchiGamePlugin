package yd.kingdom.nunchiGamePlugin;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import yd.kingdom.nunchiGamePlugin.command.ItemCommand;
import yd.kingdom.nunchiGamePlugin.command.PointSelectCommand;
import yd.kingdom.nunchiGamePlugin.command.VoteStartCommand;
import yd.kingdom.nunchiGamePlugin.config.TeleportConfig;
import yd.kingdom.nunchiGamePlugin.gui.TeleportGUIListener;
import yd.kingdom.nunchiGamePlugin.item.BreezeRodOpenListener;
import yd.kingdom.nunchiGamePlugin.item.CountdownItem;
import yd.kingdom.nunchiGamePlugin.item.CountdownUseListener;
import yd.kingdom.nunchiGamePlugin.item.VotePaperItem;
import yd.kingdom.nunchiGamePlugin.round1.CountdownService;
import yd.kingdom.nunchiGamePlugin.round2.ArcherRoundManager;
import yd.kingdom.nunchiGamePlugin.round2.TargetListener;
import yd.kingdom.nunchiGamePlugin.round3.VoteGuiListener;
import yd.kingdom.nunchiGamePlugin.round3.VoteManager;
import yd.kingdom.nunchiGamePlugin.round4.PointerListener;
import yd.kingdom.nunchiGamePlugin.round4.PointerRoundManager;

public class NunchiGamePlugin extends JavaPlugin {

    private TeleportConfig teleportConfig;
    private CountdownService countdownService;
    private VoteManager voteManager;
    private ArcherRoundManager archerRoundManager;
    private PointerRoundManager pointerRoundManager;

    @Override
    public void onEnable() {
        this.teleportConfig = new TeleportConfig(this);
        this.countdownService = new CountdownService(this);
        this.voteManager = new VoteManager(this);
        this.archerRoundManager = new ArcherRoundManager(this);
        this.pointerRoundManager = new PointerRoundManager(this);

        // 리스너
        Bukkit.getPluginManager().registerEvents(new BreezeRodOpenListener(this), this);
        Bukkit.getPluginManager().registerEvents(new CountdownUseListener(this), this);
        Bukkit.getPluginManager().registerEvents(new TeleportGUIListener(this), this);
        Bukkit.getPluginManager().registerEvents(new VoteGuiListener(this), this);
        Bukkit.getPluginManager().registerEvents(new TargetListener(), this);
        Bukkit.getPluginManager().registerEvents(new PointerListener(), this);

        // 커맨드
        if (getCommand("아이템") != null) {
            getCommand("아이템").setExecutor(new ItemCommand(this));
        }
        if (getCommand("투표시작") != null) {
            getCommand("투표시작").setExecutor(new VoteStartCommand(this));
        }
        if (getCommand("지목") != null) {
            getCommand("지목").setExecutor(new PointSelectCommand(this));
        }

        getLogger().info("NunchiGame enabled.");
    }

    @Override
    public void onDisable() {
        archerRoundManager.shutdown();
        pointerRoundManager.shutdown();
    }

    public TeleportConfig getTeleportConfig() { return teleportConfig; }
    public CountdownService getCountdownService() { return countdownService; }
    public VoteManager getVoteManager() { return voteManager; }
    public ArcherRoundManager getArcherRoundManager() { return archerRoundManager; }
    public PointerRoundManager getPointerRoundManager() { return pointerRoundManager; }

    // 헬퍼: 공용 아이템 팩토리 노출(슬라임볼/투표용지)
    public CountdownItem getCountdownItem() { return new CountdownItem(this); }
    public VotePaperItem getVotePaperItem() { return new VotePaperItem(this); }
}