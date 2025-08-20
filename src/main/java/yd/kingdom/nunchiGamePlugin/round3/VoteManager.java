package yd.kingdom.nunchiGamePlugin.round3;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class VoteManager {
    private final Plugin plugin;
    private boolean running = false;
    private final Map<UUID, UUID> votes = new HashMap<>(); // voter -> target
    private UUID correctAnswer; // 운영자 지정 시 사용

    public VoteManager(Plugin plugin){ this.plugin=plugin; }

    public void start(int seconds) {
        running = true; votes.clear(); correctAnswer = null;
        Bukkit.broadcastMessage("§d[투표] §f지금부터 " + seconds + "초간 투표를 시작합니다!");
        Bukkit.getScheduler().runTaskLater(plugin, this::stopAndAnnounce, seconds * 20L);
    }

    public void stopAndAnnounce() {
        if (!running) return;
        running = false;

        Map<UUID, Long> tally = votes.values().stream()
                .collect(Collectors.groupingBy(x -> x, Collectors.counting()));

        Bukkit.broadcastMessage("§6[투표 결과]");
        for (Map.Entry<UUID, Long> e : tally.entrySet()) {
            Player target = Bukkit.getPlayer(e.getKey());
            String name = (target!=null?target.getName():e.getKey().toString());
            List<String> voters = votes.entrySet().stream()
                    .filter(en -> en.getValue().equals(e.getKey()))
                    .map(en -> {
                        Player v = Bukkit.getPlayer(en.getKey());
                        return v!=null?v.getName():en.getKey().toString();
                    }).toList();
            Bukkit.broadcastMessage(" §f" + name + " §7(" + e.getValue() + ") : §f" + String.join(", ", voters));
        }
    }

    public boolean isRunning(){ return running; }

    public boolean hasVoted(UUID voterId) {
        return votes.containsKey(voterId);
    }

    public void castVote(Player voter, Player target) {
        if (!running) { voter.sendMessage("§c지금은 투표 시간이 아닙니다."); return; }
        if (votes.containsKey(voter.getUniqueId())) { voter.sendMessage("§c이미 투표를 완료했습니다."); return; }
        votes.put(voter.getUniqueId(), target.getUniqueId());
        voter.sendMessage("§a투표 완료: " + target.getName());
    }

    public void setCorrect(Player p) { this.correctAnswer = p!=null?p.getUniqueId():null; }
}