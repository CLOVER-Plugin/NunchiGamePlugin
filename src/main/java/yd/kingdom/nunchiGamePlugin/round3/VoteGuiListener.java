package yd.kingdom.nunchiGamePlugin.round3;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;
import yd.kingdom.nunchiGamePlugin.NunchiGamePlugin;
import yd.kingdom.nunchiGamePlugin.item.VotePaperItem;

import java.util.List;

public class VoteGuiListener implements Listener {
    private static final String TITLE = "§0§l투표 대상 선택";

    private final Plugin plugin;
    public VoteGuiListener(Plugin plugin){ this.plugin=plugin; }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onUsePaper(PlayerInteractEvent e) {
        // 메인핸드만 처리 (오프핸드도 허용하려면 이 줄 제거)
        if (e.getHand() != EquipmentSlot.HAND) return;

        Action action = e.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

        Player p = e.getPlayer();

        // 허공 우클릭에서도 확실히 아이템을 가져오기
        ItemStack it = p.getInventory().getItem(e.getHand());
        if (!VotePaperItem.isVotePaper(it)) return;

        if (((NunchiGamePlugin) plugin).getVoteManager().hasVoted(p.getUniqueId())) {
            p.sendMessage("§c이미 투표를 완료했습니다.");
            return;
        }

        e.setCancelled(true);
        p.playSound(p.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
        openVoteGui(p);
    }

    private void openVoteGui(Player p) {
        // 관전자 제외
        List<Player> candidates = Bukkit.getOnlinePlayers().stream()
                .map(Player.class::cast)
                .filter(pl -> pl.getGameMode() != GameMode.SPECTATOR)
                .toList();

        // 항상 5줄(45슬롯) GUI 생성
        Inventory inv = Bukkit.createInventory(p, 45, TITLE);

        // 1번째 줄(슬롯 0-8)을 검은색 유리판으로 채우기
        for (int i = 0; i < 9; i++) {
            ItemStack blackGlass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
            inv.setItem(i, blackGlass);
        }

        // 5번째 줄(슬롯 36-44)을 검은색 유리판으로 채우기
        for (int i = 36; i < 45; i++) {
            ItemStack blackGlass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
            inv.setItem(i, blackGlass);
        }

        // 지정된 위치에 플레이어 헤드 배치 (11, 15, 19, 21, 23, 25, 29, 33)
        int[] headPositions = {11, 15, 19, 21, 23, 25, 29, 33};
        int candidateIndex = 0;
        
        for (int position : headPositions) {
            if (candidateIndex >= candidates.size()) break;
            
            Player c = candidates.get(candidateIndex);
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta sm = (SkullMeta) head.getItemMeta();
            sm.setOwningPlayer(c);
            sm.setDisplayName("§f" + c.getName());
            head.setItemMeta(sm);
            inv.setItem(position, head);
            candidateIndex++;
        }
        
        p.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getView()==null || e.getView().getTitle()==null) return;
        if (!TITLE.equals(e.getView().getTitle())) return;
        e.setCancelled(true);
        if (!(e.getWhoClicked() instanceof Player voter)) return;

        ItemStack it = e.getCurrentItem();
        if (it==null || it.getType()!=Material.PLAYER_HEAD) return;
        SkullMeta sm = (SkullMeta) it.getItemMeta();
        String name = sm.getOwningPlayer()!=null ? sm.getOwningPlayer().getName() : it.getItemMeta().getDisplayName();
        Player target = Bukkit.getPlayerExact(name);
        if (target == null) { voter.sendMessage("§c대상 오프라인."); return; }

        ((NunchiGamePlugin)plugin).getVoteManager().castVote(voter, target);
        voter.closeInventory();
    }
}