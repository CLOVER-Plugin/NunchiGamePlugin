package yd.kingdom.nunchiGamePlugin.round2;

import org.bukkit.plugin.Plugin;

public class ArcherRoundManager {
    private final Plugin plugin;
    private boolean running = false;
    // 타깃 관리 맵/스케줄…

    public ArcherRoundManager(Plugin plugin){ this.plugin=plugin; }

    public void start() {
        if (running) return;
        running = true;
        // 3분 타이머 시작 & 주기적 오픈/클로즈 스케줄
    }
    public void stop() {
        running = false;
        // 정리 & 점수 집계 출력
    }
    public boolean isRunning(){ return running; }

    public void shutdown(){ stop(); }
}