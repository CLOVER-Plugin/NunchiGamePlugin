package yd.kingdom.nunchiGamePlugin.round2;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import java.util.*;

public class ArcherRoundManager {
    private final Plugin plugin;
    private boolean running = false;

    // 과녁과 방해 블럭 정보
    private final List<TargetInfo> targets = new ArrayList<>();

    // 선택된 영역들을 임시로 저장
    private final Map<String, Location> selectedPos1 = new HashMap<>();
    private final Map<String, Location> selectedPos2 = new HashMap<>();

    // 게임 설정
    private int roundDuration;
    private int barrierOpenDuration;   // 초
    private int minOpenTargets;
    private int maxOpenTargets;
    private int totalTargetChanges;    // 총 과녁 변경 횟수

    // 게임 상태
    private BukkitTask gameTask;
    private BukkitTask barrierTask;                // "열린 창" 종료(일괄 닫기+점수) 타이머
    private final Map<Player, Integer> playerScores = new HashMap<>();
    private final Map<TargetInfo, Set<Player>> targetHits = new HashMap<>();
    private final Set<TargetInfo> openTargets = new HashSet<>();
    private boolean windowOpen = false;            // 현재 2초 오픈 윈도우 상태

    // 참여자 관리 및 스코어보드
    private final List<Player> participants = new ArrayList<>();
    private Scoreboard scoreboard;
    private Objective objective;

    // 과녁 변경 관리
    private int targetChangeCount = 0;  // 현재까지 과녁 변경 횟수

    // 과녁 정보를 담는 내부 클래스
    private static class TargetInfo {
        final Location center;
        final List<Location> blocks;
        final Location barrierPos1;
        final Location barrierPos2;

        TargetInfo(Location center, List<Location> blocks, Location barrierPos1, Location barrierPos2) {
            this.center = center;
            this.blocks = blocks;
            this.barrierPos1 = barrierPos1;
            this.barrierPos2 = barrierPos2;
        }

        boolean containsLocation(Location location) {
            return blocks.stream().anyMatch(block ->
                    block.getWorld().equals(location.getWorld()) &&
                            block.getBlockX() == location.getBlockX() &&
                            block.getBlockY() == location.getBlockY() &&
                            block.getBlockZ() == location.getBlockZ()
            );
        }

        List<Location> getBarrierBlocks() {
            List<Location> barrierBlocks = new ArrayList<>();
            int minX = Math.min(barrierPos1.getBlockX(), barrierPos2.getBlockX());
            int maxX = Math.max(barrierPos1.getBlockX(), barrierPos2.getBlockX());
            int minY = Math.min(barrierPos1.getBlockY(), barrierPos2.getBlockY());
            int maxY = Math.max(barrierPos1.getBlockY(), barrierPos2.getBlockY());
            int minZ = Math.min(barrierPos1.getBlockZ(), barrierPos2.getBlockZ());
            int maxZ = Math.max(barrierPos1.getBlockZ(), barrierPos2.getBlockZ());

            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        barrierBlocks.add(new Location(barrierPos1.getWorld(), x, y, z));
                    }
                }
            }
            return barrierBlocks;
        }
    }

    public ArcherRoundManager(Plugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    // 선택된 영역 저장
    public void setSelectedArea(String areaName, Location pos1, Location pos2) {
        selectedPos1.put(areaName, pos1);
        selectedPos2.put(areaName, pos2);

        if (areaName.startsWith("과녁") || areaName.startsWith("방해")) {
            updateTargetsFromSelection();
        }
    }

    // 선택된 영역으로 과녁 정보 업데이트
    private void updateTargetsFromSelection() {
        targets.clear();
        targetHits.clear();

        for (int i = 1; i <= 3; i++) {
            String targetKey = "과녁" + i;
            String barrierKey = "방해" + i;

            Location targetPos1 = selectedPos1.get(targetKey);
            Location targetPos2 = selectedPos2.get(targetKey);
            Location barrierPos1 = selectedPos1.get(barrierKey);
            Location barrierPos2 = selectedPos2.get(barrierKey);

            if (targetPos1 != null && targetPos2 != null && barrierPos1 != null && barrierPos2 != null) {
                List<Location> targetBlocks = calculateAreaBlocks(targetPos1, targetPos2);
                Location center = new Location(
                        targetPos1.getWorld(),
                        (targetPos1.getX() + targetPos2.getX()) / 2,
                        (targetPos1.getY() + targetPos2.getY()) / 2,
                        (targetPos1.getZ() + targetPos2.getZ()) / 2
                );

                TargetInfo targetInfo = new TargetInfo(center, targetBlocks, barrierPos1, barrierPos2);
                targets.add(targetInfo);
                targetHits.put(targetInfo, new HashSet<>());
            }
        }

        plugin.getLogger().info("과녁 정보가 선택된 영역으로 업데이트되었습니다: " + targets.size() + "개 과녁");
    }

    // 영역의 모든 블럭 위치 계산
    private List<Location> calculateAreaBlocks(Location pos1, Location pos2) {
        List<Location> blocks = new ArrayList<>();
        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    blocks.add(new Location(pos1.getWorld(), x, y, z));
                }
            }
        }
        return blocks;
    }

    // 설정 저장
    public void saveConfig() {
        plugin.getLogger().info("설정 저장 시작...");

        plugin.getConfig().set("round2", null);

        plugin.getConfig().createSection("round2");
        plugin.getConfig().createSection("round2.targets");
        plugin.getConfig().createSection("round2.barriers");
        plugin.getConfig().createSection("round2.settings");

        for (int i = 1; i <= 3; i++) {
            String targetKey = "과녁" + i;
            String barrierKey = "방해" + i;

            Location targetPos1 = selectedPos1.get(targetKey);
            Location targetPos2 = selectedPos2.get(targetKey);
            Location barrierPos1 = selectedPos1.get(barrierKey);
            Location barrierPos2 = selectedPos2.get(barrierKey);

            plugin.getLogger().info(targetKey + " 설정 상태: " + (targetPos1 != null && targetPos2 != null));
            plugin.getLogger().info(barrierKey + " 설정 상태: " + (barrierPos1 != null && barrierPos2 != null));

            if (targetPos1 != null && targetPos2 != null) {
                String targetPath = "round2.targets.target" + i;
                plugin.getConfig().set(targetPath + ".center.world", targetPos1.getWorld().getName());
                plugin.getConfig().set(targetPath + ".center.x", (targetPos1.getX() + targetPos2.getX()) / 2);
                plugin.getConfig().set(targetPath + ".center.y", (targetPos1.getY() + targetPos2.getY()) / 2);
                plugin.getConfig().set(targetPath + ".center.z", (targetPos1.getZ() + targetPos2.getZ()) / 2);

                List<Location> targetBlocks = calculateAreaBlocks(targetPos1, targetPos2);
                List<Map<String, Object>> blocksData = new ArrayList<>();
                for (Location block : targetBlocks) {
                    Map<String, Object> blockData = new HashMap<>();
                    blockData.put("world", block.getWorld().getName());
                    blockData.put("x", block.getX());
                    blockData.put("y", block.getY());
                    blockData.put("z", block.getZ());
                    blocksData.add(blockData);
                }
                plugin.getConfig().set(targetPath + ".blocks", blocksData);
                plugin.getLogger().info(targetPath + " 저장 완료");
            }

            if (barrierPos1 != null && barrierPos2 != null) {
                String barrierPath = "round2.barriers.barrier" + i;
                plugin.getConfig().set(barrierPath + ".pos1.world", barrierPos1.getWorld().getName());
                plugin.getConfig().set(barrierPath + ".pos1.x", barrierPos1.getX());
                plugin.getConfig().set(barrierPath + ".pos1.y", barrierPos1.getY());
                plugin.getConfig().set(barrierPath + ".pos1.z", barrierPos1.getZ());
                plugin.getConfig().set(barrierPath + ".pos2.world", barrierPos2.getWorld().getName());
                plugin.getConfig().set(barrierPath + ".pos2.x", barrierPos2.getX());
                plugin.getConfig().set(barrierPath + ".pos2.y", barrierPos2.getY());
                plugin.getConfig().set(barrierPath + ".pos2.z", barrierPos2.getZ());
                plugin.getLogger().info(barrierPath + " 저장 완료");
            }
        }

        plugin.getConfig().set("round2.settings.round_duration", 180);
        plugin.getConfig().set("round2.settings.barrier_open_duration", 2); // 기본 2초
        plugin.getConfig().set("round2.settings.min_open_targets", 1);
        plugin.getConfig().set("round2.settings.max_open_targets", 3);
        plugin.getConfig().set("round2.settings.total_target_changes", 12);

        plugin.saveConfig();
        plugin.getLogger().info("2라운드 설정이 저장되었습니다. 총 " + targets.size() + "개 과녁 설정됨");
    }

    public void loadConfig() {
        plugin.reloadConfig();
        ConfigurationSection round2Section = plugin.getConfig().getConfigurationSection("round2");
        if (round2Section == null) {
            plugin.getLogger().warning("2라운드 설정을 찾을 수 없습니다!");
            return;
        }

        // 초기화
        targets.clear();
        targetHits.clear();
        openTargets.clear();
        windowOpen = false;
        if (barrierTask != null) {
            barrierTask.cancel();
            barrierTask = null;
        }

        // 과녁 정보 로드
        ConfigurationSection targetsSection = round2Section.getConfigurationSection("targets");
        if (targetsSection != null) {
            for (String key : targetsSection.getKeys(false)) {
                ConfigurationSection targetSection = targetsSection.getConfigurationSection(key);
                if (targetSection != null) {
                    ConfigurationSection centerSection = targetSection.getConfigurationSection("center");
                    Location center = null;
                    if (centerSection != null) {
                        center = new Location(
                                Bukkit.getWorld(centerSection.getString("world", "world")),
                                centerSection.getDouble("x"),
                                centerSection.getDouble("y"),
                                centerSection.getDouble("z")
                        );
                    }

                    List<Location> blocks = new ArrayList<>();
                    List<?> blocksList = targetSection.getList("blocks", new ArrayList<>());
                    for (Object blockObj : blocksList) {
                        if (blockObj instanceof Map) {
                            Map<?, ?> blockMap = (Map<?, ?>) blockObj;
                            if (blockMap.containsKey("world") && blockMap.containsKey("x") &&
                                    blockMap.containsKey("y") && blockMap.containsKey("z")) {
                                Location blockLoc = new Location(
                                        Bukkit.getWorld((String) blockMap.get("world")),
                                        ((Number) blockMap.get("x")).doubleValue(),
                                        ((Number) blockMap.get("y")).doubleValue(),
                                        ((Number) blockMap.get("z")).doubleValue()
                                );
                                blocks.add(blockLoc);
                            }
                        }
                    }

                    ConfigurationSection barriersSection = round2Section.getConfigurationSection("barriers");
                    Location barrierPos1 = null;
                    Location barrierPos2 = null;

                    if (barriersSection != null) {
                        String barrierKey = "barrier" + key.substring(6); // target1 -> barrier1
                        ConfigurationSection barrierSection = barriersSection.getConfigurationSection(barrierKey);
                        if (barrierSection != null) {
                            ConfigurationSection pos1Section = barrierSection.getConfigurationSection("pos1");
                            ConfigurationSection pos2Section = barrierSection.getConfigurationSection("pos2");

                            if (pos1Section != null) {
                                barrierPos1 = new Location(
                                        Bukkit.getWorld(pos1Section.getString("world", "world")),
                                        pos1Section.getDouble("x"),
                                        pos1Section.getDouble("y"),
                                        pos1Section.getDouble("z")
                                );
                            }

                            if (pos2Section != null) {
                                barrierPos2 = new Location(
                                        Bukkit.getWorld(pos2Section.getString("world", "world")),
                                        pos2Section.getDouble("x"),
                                        pos2Section.getDouble("y"),
                                        pos2Section.getDouble("z")
                                );
                            }
                        }
                    }

                    if (center != null && !blocks.isEmpty() && barrierPos1 != null && barrierPos2 != null) {
                        TargetInfo targetInfo = new TargetInfo(center, blocks, barrierPos1, barrierPos2);
                        targets.add(targetInfo);
                        targetHits.put(targetInfo, new HashSet<>());
                    }
                }
            }
        }

        // 게임 설정 로드
        ConfigurationSection settingsSection = round2Section.getConfigurationSection("settings");
        if (settingsSection != null) {
            roundDuration = settingsSection.getInt("round_duration", 180);
            barrierOpenDuration = settingsSection.getInt("barrier_open_duration", 2);
            minOpenTargets = settingsSection.getInt("min_open_targets", 1);
            maxOpenTargets = settingsSection.getInt("max_open_targets", 3);
            totalTargetChanges = settingsSection.getInt("total_target_changes", 12);
        }

        plugin.getLogger().info("2라운드 설정 로드 완료: " + targets.size() + "개 과녁");
    }

    public void start() {
        if (running) {
            Bukkit.broadcastMessage("§c2라운드가 이미 진행 중입니다!");
            return;
        }

        if (targets.isEmpty()) {
            Bukkit.broadcastMessage("§c과녁이 설정되지 않았습니다! /영역설정 명령어로 먼저 설정하세요.");
            return;
        }

        if (participants.isEmpty()) {
            Bukkit.broadcastMessage("§c참여자가 없습니다! /참여 명령어로 참여자를 등록해주세요.");
            return;
        }

        running = true;
        playerScores.clear();
        targetHits.clear();
        openTargets.clear();
        windowOpen = false;
        targetChangeCount = 0;

        // 참여자만 초기 점수 설정
        for (Player player : participants) {
            playerScores.put(player, 0);
        }

        // 모든 방해 블럭을 검은색 콘크리트로 설정
        for (TargetInfo target : targets) {
            for (Location barrierLoc : target.getBarrierBlocks()) {
                barrierLoc.getBlock().setType(Material.BLACK_CONCRETE);
            }
        }

        Bukkit.broadcastMessage("§a§l2라운드 시작! §e" + roundDuration + "초 동안 과녁을 맞춰보세요!");
        Bukkit.broadcastMessage("§7과녁이 총 " + totalTargetChanges + "번 랜덤하게 열리고, 열린 지 " + barrierOpenDuration + "초 후 자동으로 닫힙니다.");
        Bukkit.broadcastMessage("§7참여자: " + participants.stream().map(Player::getName).reduce((a, b) -> a + ", " + b).orElse("없음"));

        // 스코어보드 표시
        updateScoreboard();

        // 게임 타이머 (랜덤하게 과녁 상태 변경)
        gameTask = new BukkitRunnable() {
            private int timeLeft = roundDuration;
            private int nextChangeTime = roundDuration - 15; // 첫 번째 변경은 15초 후

            @Override
            public void run() {
                if (timeLeft <= 0) {
                    endRound();
                    return;
                }

                // 총 변경횟수만큼, 최소 간격 15초 보장하며 랜덤 분배
                if (targetChangeCount < totalTargetChanges && timeLeft <= nextChangeTime) {
                    int remainingTime = timeLeft;
                    int remainingChanges = totalTargetChanges - targetChangeCount;

                    int averageInterval = Math.max(remainingTime / remainingChanges, 15); // 최소 15초
                    int randomInterval = Math.max((int) (averageInterval * (0.7 + Math.random() * 0.6)), 15);

                    changeTargets();

                    nextChangeTime = timeLeft - randomInterval;
                }

                // 요청: 180초 -> 90초 -> 30초 -> 5,4,3,2,1 만 출력
                if (timeLeft == 180 || timeLeft == 90 || timeLeft == 30 || (timeLeft <= 5 && timeLeft > 0)) {
                    Bukkit.broadcastMessage("§e남은 시간: " + timeLeft + "초");
                }

                timeLeft--;
            }
        }.runTaskTimer(plugin, 0L, 20L);

        // 초기 과녁 오픈 (첫 번째 변경)
        changeTargets();
    }

    private void changeTargets() {
        // 혹시 이전 윈도우가 열려있다면 강제 종료(일괄 점수+닫기)
        if (windowOpen) {
            closeOpenTargetsWithScoring();
        }

        // 과녁 변경 횟수 증가 + 디버그
        targetChangeCount++;
        //Bukkit.broadcastMessage("§a[DEBUG] 과녁 오픈 #" + targetChangeCount + "/" + totalTargetChanges);

        // 새로 열기 준비
        openTargets.clear();
        int openCount = Math.min(
                new Random().nextInt(maxOpenTargets - minOpenTargets + 1) + minOpenTargets,
                targets.size()
        );
        List<TargetInfo> available = new ArrayList<>(targets);
        Collections.shuffle(available);

        int opened = 0;
        for (TargetInfo t : available) {
            openTargets.add(t);

            // 방해블럭 제거(오픈)
            for (Location barrierLoc : t.getBarrierBlocks()) {
                barrierLoc.getBlock().setType(Material.AIR);
            }

            // 오픈 시점에 명중 기록 초기화
            targetHits.put(t, new HashSet<>());

            opened++;
            if (opened >= openCount) break;
        }

        // 윈도우 시작
        windowOpen = true;

        // 기존 타이머 있으면 취소 후, 새 2초 윈도우 타이머 설정
        if (barrierTask != null) {
            barrierTask.cancel();
            barrierTask = null;
        }
        barrierTask = new BukkitRunnable() {
            @Override
            public void run() {
                closeOpenTargetsWithScoring();
            }
        }.runTaskLater(plugin, barrierOpenDuration * 20L);
    }

    // 윈도우 종료: 모든 열린 과녁을 동시에 닫고, 같은 시점에 점수 계산
    private void closeOpenTargetsWithScoring() {
        if (!windowOpen) return;

        // 점수 계산
        for (TargetInfo target : new ArrayList<>(openTargets)) {
            Set<Player> hitters = targetHits.getOrDefault(target, Collections.emptySet());
            if (hitters.size() == 1) {
                Player solo = hitters.iterator().next();
                updatePlayerScore(solo, playerScores.getOrDefault(solo, 0) + 1);
                solo.sendMessage("§a과녁을 혼자 맞췄습니다! +1점");
            } else if (hitters.size() >= 2) {
                for (Player p : hitters) {
                    updatePlayerScore(p, playerScores.getOrDefault(p, 0) - 1);
                    p.sendMessage("§c다른 사람과 같은 과녁을 맞췄습니다! -1점");
                }
            }
        }

        // 일괄 닫기
        for (TargetInfo target : openTargets) {
            for (Location barrierLoc : target.getBarrierBlocks()) {
                barrierLoc.getBlock().setType(Material.BLACK_CONCRETE);
            }
        }

        // 상태 정리
        openTargets.clear();
        targetHits.clear();
        windowOpen = false;

        if (barrierTask != null) {
            barrierTask.cancel();
            barrierTask = null;
        }

        // 스코어보드 갱신
        updateScoreboard();
    }

    public void handleTargetHit(Location targetLocation, Player player) {
        if (!running) return;
        if (!windowOpen) return; // 윈도우가 아니면 무시

        final TargetInfo hitTarget = findHitTarget(targetLocation);
        if (hitTarget == null) return; // 열린 과녁 안이 아님

        playerScores.putIfAbsent(player, 0);

        // 히트 기록(동일 윈도우에서만 집계됨)
        final Set<Player> playersWhoHit = targetHits.computeIfAbsent(hitTarget, t -> new HashSet<>());
        playersWhoHit.add(player);
        // 즉시 점수 계산/닫기 없음 — 윈도우 종료 시 일괄 처리
    }

    // 맞은 과녁 찾기 (현재 열린 과녁 중에서만)
    private TargetInfo findHitTarget(Location targetLocation) {
        for (TargetInfo target : openTargets) {
            if (target.containsLocation(targetLocation)) {
                return target;
            }
        }
        return null;
    }

    private void endRound() {
        running = false;

        // 남아있는 윈도우가 있으면 마무리
        if (windowOpen) {
            closeOpenTargetsWithScoring();
        }

        if (gameTask != null) {
            gameTask.cancel();
            gameTask = null;
        }
        if (barrierTask != null) {
            barrierTask.cancel();
            barrierTask = null;
        }

        // 모든 방해 블럭 복원
        for (TargetInfo target : targets) {
            for (Location barrierLoc : target.getBarrierBlocks()) {
                barrierLoc.getBlock().setType(Material.BLACK_CONCRETE);
            }
        }

        // 최종 점수 발표
        Bukkit.broadcastMessage("§6§l ==== 2라운드 종료 ====");

        if (participants.isEmpty()) {
            Bukkit.broadcastMessage("§7참여한 플레이어가 없습니다.");
        } else {
            List<Map.Entry<Player, Integer>> participantScores = new ArrayList<>();
            for (Player participant : participants) {
                int score = playerScores.getOrDefault(participant, 0);
                participantScores.add(new AbstractMap.SimpleEntry<>(participant, score));
            }

            if (participantScores.isEmpty()) {
                Bukkit.broadcastMessage("§7참여한 플레이어가 없습니다.");
            } else {
                participantScores.sort((a, b) -> b.getValue().compareTo(a.getValue()));

                Bukkit.broadcastMessage("§e§l최종 순위:");
                for (int i = 0; i < participantScores.size(); i++) {
                    Map.Entry<Player, Integer> entry = participantScores.get(i);
                    Player player = entry.getKey();
                    int score = entry.getValue();
                    String rank = (i + 1) + "위";
                    Bukkit.broadcastMessage("§" + (i == 0 ? "6" : i == 1 ? "7" : "8") + rank + " §f" + player.getName() + " §e" + score + "점");
                }
            }
        }

        Bukkit.broadcastMessage("§6§l==================");

        // 스코어보드 제거 및 참여자 리셋
        removeScoreboard();
        participants.clear();
    }

    public void stop() {
        if (!running) return;

        running = false;

        // 남아있는 윈도우 종료
        if (windowOpen) {
            closeOpenTargetsWithScoring();
        }

        if (gameTask != null) {
            gameTask.cancel();
            gameTask = null;
        }
        if (barrierTask != null) {
            barrierTask.cancel();
            barrierTask = null;
        }

        for (TargetInfo target : targets) {
            for (Location barrierLoc : target.getBarrierBlocks()) {
                barrierLoc.getBlock().setType(Material.BLACK_CONCRETE);
            }
        }

        Bukkit.broadcastMessage("§c2라운드가 중단되었습니다.");
    }

    public boolean isRunning() {
        return running;
    }

    public void shutdown() {
        stop();
    }

    public boolean isTargetOpen(Location location) {
        if (!windowOpen) return false;
        for (TargetInfo target : openTargets) {
            if (target.containsLocation(location)) {
                return true;
            }
        }
        return false;
    }

    public List<Location> getTargetLocations() {
        List<Location> allLocations = new ArrayList<>();
        for (TargetInfo target : targets) {
            allLocations.addAll(target.blocks);
        }
        return allLocations;
    }

    // 특정 영역이 설정되었는지 확인
    public boolean hasArea(String areaName) {
        return selectedPos1.containsKey(areaName) && selectedPos2.containsKey(areaName);
    }

    // 설정된 영역 정보 가져오기
    public Location getAreaPos1(String areaName) {
        return selectedPos1.get(areaName);
    }

    public Location getAreaPos2(String areaName) {
        return selectedPos2.get(areaName);
    }

    // 게임 중 새로 접속한 플레이어 등록
    public void registerPlayer(Player player) {
        if (running && player.getGameMode() != org.bukkit.GameMode.SPECTATOR) {
            playerScores.putIfAbsent(player, 0);
        }
    }

    // 참여자 추가 (최대 4명)
    public boolean addParticipant(Player player) {
        if (participants.contains(player)) {
            return false; // 이미 참여자
        }

        if (participants.size() >= 4) {
            // 최근 4명만 유지
            participants.remove(0);
        }

        participants.add(player);
        return true;
    }

    // 스코어보드 생성 및 표시
    public void updateScoreboard() {
        if (scoreboard != null) {
            scoreboard.clearSlot(DisplaySlot.SIDEBAR);
        }

        ScoreboardManager manager = Bukkit.getScoreboardManager();
        scoreboard = manager.getNewScoreboard();
        objective = scoreboard.registerNewObjective("round2", "dummy", "§6§l점수");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        // 참여자들의 점수 표시
        for (Player player : participants) {
            int score = playerScores.getOrDefault(player, 0);
            Score playerScore = objective.getScore(player.getName());
            playerScore.setScore(score);
        }

        // 모든 플레이어에게 스코어보드 표시
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setScoreboard(scoreboard);
        }
    }

    // 스코어보드 제거
    public void removeScoreboard() {
        if (scoreboard != null) {
            scoreboard.clearSlot(DisplaySlot.SIDEBAR);
            scoreboard = null;
            objective = null;
        }

        // 모든 플레이어의 스코어보드 제거
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        }
    }

    // 점수 업데이트 시 스코어보드도 업데이트
    private void updatePlayerScore(Player player, int newScore) {
        playerScores.put(player, newScore);
        if (running && scoreboard != null && objective != null) {
            Score score = objective.getScore(player.getName());
            score.setScore(newScore);
        }
    }
}