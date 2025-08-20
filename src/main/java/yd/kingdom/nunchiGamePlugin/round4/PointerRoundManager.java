package yd.kingdom.nunchiGamePlugin.round4;

import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PointerRoundManager {
    private final Plugin plugin;
    private boolean accepting = false;
    private final Map<UUID, UUID> link = new HashMap<>();
    private final Map<UUID, Pending> pendingByOwner = new HashMap<>();
    private int particleTaskId = -1;

    public PointerRoundManager(Plugin plugin){
        this.plugin = plugin;
        startParticleTask();
    }

    public void beginAccepting(){ accepting = true; link.clear(); }
    public void endAndScore(){
        accepting = false;
        // 마지막 대상/지목자 계산 후 점수 반영
    }
    public boolean isAccepting(){ return accepting; }
    public void setLink(Player from, Player to){ if (accepting) link.put(from.getUniqueId(), to.getUniqueId()); }
    public void shutdown(){
        accepting=false; link.clear();
        if (particleTaskId != -1) {
            Bukkit.getScheduler().cancelTask(particleTaskId);
            particleTaskId = -1;
        }
        pendingByOwner.clear();
    }

    public void setPending(UUID owner, UUID start, int steps){ pendingByOwner.put(owner, new Pending(start, steps)); }
    public Pending consumePending(UUID owner){ return pendingByOwner.remove(owner); }

    public void countAndHighlight(UUID startId, int steps){
        // 시작 플레이어부터 매 초 현재 지목된 플레이어만 발광, 이전은 발광 해제. 종료 시 마지막 플레이어는 5초 유지.
        final UUID[] current = { startId };
        final UUID[] lastGlowed = { null };
        final int[] i = { 0 };
        Bukkit.getScheduler().runTaskTimer(plugin, task -> {
            // 액션바 숫자와 소리
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                        new net.md_5.bungee.api.chat.TextComponent("§e" + i[0]));
                p.playSound(p.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.5f);
            }

            // 현재 대상 발광 처리, 이전 발광 해제
            Player cur = Bukkit.getPlayer(current[0]);
            if (cur != null) {
                if (lastGlowed[0] != null && !lastGlowed[0].equals(cur.getUniqueId())) {
                    Player prev = Bukkit.getPlayer(lastGlowed[0]);
                    if (prev != null) {
                        prev.setGlowing(false);
                        prev.removePotionEffect(PotionEffectType.GLOWING);
                    }
                }
                cur.setGlowing(true);
                lastGlowed[0] = cur.getUniqueId();
            }

            // 종료 조건: 지정한 스텝까지 도달
            if (i[0] >= steps) {
                if (cur != null) {
                    // 마지막 플레이어는 5초간 유지 후 해제
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        Player again = Bukkit.getPlayer(cur.getUniqueId());
                        if (again != null) again.setGlowing(false);
                    }, 5 * 20L);
                }
                task.cancel();
                return;
            }

            // 다음 대상 계산
            UUID next = link.get(current[0]);
            if (next == null) {
                // 더 이상 연결이 없으면 현재에서 종료 처리
                if (cur != null) {
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        Player again = Bukkit.getPlayer(cur.getUniqueId());
                        if (again != null) again.setGlowing(false);
                    }, 5 * 20L);
                }
                task.cancel();
                return;
            }
            current[0] = next;
            i[0]++;
        }, 0L, 20L);
    }

    private void startParticleTask(){
        if (particleTaskId != -1) return;
        particleTaskId = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Map.Entry<UUID, UUID> e : link.entrySet()) {
                Player from = Bukkit.getPlayer(e.getKey());
                Player to = Bukkit.getPlayer(e.getValue());
                if (from == null || to == null) continue;
                if (from.getWorld() != to.getWorld()) continue;
                drawParticleLine(from, to);
            }
        }, 0L, 5L).getTaskId();
    }

    private void drawParticleLine(Player from, Player to){
        Vector a = from.getEyeLocation().toVector();
        Vector b = to.getEyeLocation().toVector();
        Vector dir = b.clone().subtract(a);
        double dist = dir.length();
        int points = Math.max(8, (int)(dist * 6));
        Vector step = dir.multiply(1.0 / points);
        for (int i=0;i<=points;i++){
            Vector pos = a.clone().add(step.clone().multiply(i));
            from.getWorld().spawnParticle(Particle.FLAME, pos.getX(), pos.getY(), pos.getZ(), 1, 0, 0, 0, 0);
        }
    }

    public record Pending(UUID startId, int steps) {}
}