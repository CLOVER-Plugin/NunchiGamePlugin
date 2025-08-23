package yd.kingdom.nunchiGamePlugin;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import yd.kingdom.nunchiGamePlugin.command.*;
import yd.kingdom.nunchiGamePlugin.config.TeleportConfig;
import yd.kingdom.nunchiGamePlugin.gui.TeleportGUIListener;
import yd.kingdom.nunchiGamePlugin.item.*;
import yd.kingdom.nunchiGamePlugin.round1.CountdownService;
import yd.kingdom.nunchiGamePlugin.round2.ArcherRoundManager;
import yd.kingdom.nunchiGamePlugin.round2.AreaSelectionManager;
import yd.kingdom.nunchiGamePlugin.round2.PlayerJoinListener;
import yd.kingdom.nunchiGamePlugin.round2.TargetListener;
import yd.kingdom.nunchiGamePlugin.round3.VoteGuiListener;
import yd.kingdom.nunchiGamePlugin.round3.VoteManager;
import yd.kingdom.nunchiGamePlugin.round4.ThemeTimerListener;
import yd.kingdom.nunchiGamePlugin.round4.ThemeTimerManager;

public class NunchiGamePlugin extends JavaPlugin {

    private TeleportConfig teleportConfig;
    private CountdownService countdownService;
    private VoteManager voteManager;
    private ArcherRoundManager archerRoundManager;
    private AreaSelectionManager areaSelectionManager;
    private ThemeTimerManager themeTimerManager;
    private ThemeShardItem themeShardItem;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.teleportConfig = new TeleportConfig(this);
        this.countdownService = new CountdownService(this);
        this.voteManager = new VoteManager(this);
        this.archerRoundManager = new ArcherRoundManager(this);
        this.areaSelectionManager = new AreaSelectionManager(this.archerRoundManager);
        this.themeTimerManager = new ThemeTimerManager(this);
        this.themeShardItem = new ThemeShardItem(this);

        // 리스너
        Bukkit.getPluginManager().registerEvents(new ClockOpenListener(this), this);
        Bukkit.getPluginManager().registerEvents(new CountdownUseListener(this), this);
        Bukkit.getPluginManager().registerEvents(new TeleportGUIListener(this), this);
        Bukkit.getPluginManager().registerEvents(new VoteGuiListener(this), this);
        Bukkit.getPluginManager().registerEvents(new TargetListener(archerRoundManager), this);
        Bukkit.getPluginManager().registerEvents(areaSelectionManager, this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(archerRoundManager), this);
        Bukkit.getPluginManager().registerEvents(new ThemeTimerListener(this), this);

        // 커맨드
        if (getCommand("아이템") != null) {
            getCommand("아이템").setExecutor(new ItemCommand(this));
        }
        if (getCommand("투표시작") != null) {
            getCommand("투표시작").setExecutor(new VoteStartCommand(this));
        }

        if (getCommand("2라운드") != null) {
            getCommand("2라운드").setExecutor(new Round2Command(this));
        }
        if (getCommand("영역설정") != null) {
            getCommand("영역설정").setExecutor(new AreaSetupCommand(this, areaSelectionManager));
        }
        if (getCommand("참여") != null) {
            getCommand("참여").setExecutor(new ParticipateCommand(archerRoundManager));
        }

        getLogger().info("NunchiGame enabled.");
    }

    @Override
    public void onDisable() {
        archerRoundManager.shutdown();
    }

    public TeleportConfig getTeleportConfig() { return teleportConfig; }
    public CountdownService getCountdownService() { return countdownService; }
    public VoteManager getVoteManager() { return voteManager; }
    public ArcherRoundManager getArcherRoundManager() { return archerRoundManager; }
    public AreaSelectionManager getAreaSelectionManager() { return areaSelectionManager; }

    // 헬퍼: 공용 아이템 팩토리 노출(슬라임볼/투표용지)
    public CountdownItem getCountdownItem() { return new CountdownItem(this); }
    public VotePaperItem getVotePaperItem() { return new VotePaperItem(this); }

    public ThemeTimerManager getThemeTimerManager() { return themeTimerManager; }
    public ThemeShardItem getThemeShardItem() { return themeShardItem; }
}