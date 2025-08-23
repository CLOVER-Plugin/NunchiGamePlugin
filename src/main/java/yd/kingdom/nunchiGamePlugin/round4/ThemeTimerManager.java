package yd.kingdom.nunchiGamePlugin.round4;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.text.DecimalFormat;
import java.util.*;

public class ThemeTimerManager {

    private final Plugin plugin;
    private final Random random = new Random();
    private final List<String> topics = new ArrayList<>();

    public ThemeTimerManager(Plugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        topics.clear();
        ConfigurationSection sec = plugin.getConfig().getConfigurationSection("round4");
        if (sec != null) {
            List<String> list = sec.getStringList("topics");
            if (list != null && !list.isEmpty()) topics.addAll(list);
        }
        if (topics.isEmpty()) {
            topics.addAll(Arrays.asList("마인크래프트", "동물", "인물"));
        }
    }

    // ====== 전역 세션 ======
    private static class Session {
        String topic;
        boolean running;
        BukkitTask countdownTask;  // 3초 카운트다운(1초 간격)
        BukkitTask tickTask;       // 5초 타이머(매 틱)
        BukkitTask clearTask;      // (현재 미사용) 남겨둠
        long stopwatchStartMs;
        double lastRemaining;
        String starterName;
    }

    private Session session; // 동시에 하나만

    private boolean active() {
        return session != null && session.running;
    }

    private Collection<Player> recipients() {
        List<Player> list = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getGameMode() != GameMode.SPECTATOR) list.add(p);
        }
        return list;
    }

    // ====== 시작 (전역) ======
    public void startGlobal(Player starter) {
        if (active()) {
            starter.sendMessage("§c이미 타이머가 진행 중입니다.");
            return;
        }
        if (topics.isEmpty()) {
            starter.sendMessage("§c주제 목록이 비어 있습니다. config.yml을 확인하세요.");
            return;
        }

        session = new Session();
        session.topic = topics.get(random.nextInt(topics.size()));
        session.running = true;
        session.starterName = starter.getName();

        // 3초 카운트다운: Title=흰 숫자, SubTitle=주제
        session.countdownTask = new BukkitRunnable() {
            int left = 3;
            @Override public void run() {
                if (!session.running) { cancel(); return; }
                if (left <= 0) {
                    // 타이틀 비우고 5초 타이머 시작
                    for (Player p : recipients()) p.sendTitle("", "§f", 0, 5, 5);
                    startStopwatch();
                    cancel();
                    return;
                }
                for (Player p : recipients()) {
                    p.sendTitle("§f" + left, "§7주제: §a" + session.topic, 0, 25, 0);
                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.0f);
                }
                left--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void startStopwatch() {
        if (session == null) return;
        session.stopwatchStartMs = System.currentTimeMillis();
        DecimalFormat df = new DecimalFormat("0.00"); // ⬅ 2자리 고정

        session.tickTask = new BukkitRunnable() {
            @Override public void run() {
                if (!session.running) { cancel(); return; }
                double elapsed = (System.currentTimeMillis() - session.stopwatchStartMs) / 1000.0;
                double remaining = 5.0 - elapsed;
                session.lastRemaining = remaining;

                if (remaining <= 0) {
                    // 끝나면 실패 문구 없이 조용히 종료 + 서브타이틀 비우기
                    for (Player p : recipients()) p.sendTitle("", "§f", 0, 5, 5);
                    endNow();
                    cancel();
                    return;
                }
                String sub = "§e" + df.format(remaining); // ⬅ 's' 제거
                for (Player p : recipients()) {
                    p.sendTitle("", sub, 0, 10, 0);
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void endNow() {
        if (session == null) return;
        if (session.countdownTask != null) session.countdownTask.cancel();
        if (session.tickTask != null) session.tickTask.cancel();
        if (session.clearTask != null) session.clearTask.cancel();
        session.countdownTask = null;
        session.tickTask = null;
        session.clearTask = null;
        session.running = false;
        session = null;
    }

    // 강제 취소(플러그인 disable 등)
    public void cancelAll() {
        endNow();
    }
}