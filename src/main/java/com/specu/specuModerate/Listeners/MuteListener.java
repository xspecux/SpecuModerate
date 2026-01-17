package com.specu.specuModerate.Listeners;

import com.specu.specuModerate.Main;
import com.specu.specuModerate.Mute.MuteInfo;
import com.specu.specuModerate.Mute.MuteManager;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MuteListener implements Listener {
    private final Main main;
    private final MuteManager muteManager;

    public MuteListener(Main main, MuteManager muteManager) {
        this.main = main;
        this.muteManager = muteManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncChatEvent e) {
        Player player = e.getPlayer();

        if (!muteManager.isPlayerMuted(player)) return;

        MuteInfo muteInfo = muteManager.getMuteInfo(player.getUniqueId());
        LocalDateTime muteDate = muteInfo.getDate();
        LocalDateTime now = LocalDateTime.now();

        if (muteDate == null || muteDate.isBefore(now)) muteManager.unMutePlayer(player);

        e.setCancelled(true);
        String date = muteDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm:ss"));
        String reason = muteInfo.getReason();
        String admin = muteInfo.getAdmin();

        String message = main.getMessage("you-are-muted", admin, player.getName(), reason).replace("{time}", date);
        player.sendMessage(message);
    }
}
