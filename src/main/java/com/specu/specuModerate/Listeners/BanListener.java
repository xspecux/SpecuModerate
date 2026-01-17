package com.specu.specuModerate.Listeners;

import com.specu.specuModerate.Ban.BanInfo;
import com.specu.specuModerate.Ban.BanManager;
import com.specu.specuModerate.Main;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class BanListener implements Listener {
    private final Main main;
    private final BanManager banManager;

    public BanListener(Main main, BanManager banManager) {
        this.main = main;
        this.banManager = banManager;
    }

    @EventHandler
    public void onLogin(AsyncPlayerPreLoginEvent e) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(e.getUniqueId());
        BanInfo baninfo = banManager.getBanInfo(player);

        if (baninfo == null) return;

        String type = "ban";

        boolean ipBanned = baninfo.getIp() != null && baninfo.getIp().equals(e.getAddress().getHostAddress());
        if (ipBanned) type = "ipban";

        String bandate = baninfo.getBandate();
        if (bandate != null && !bandate.equalsIgnoreCase("perm")) type = "tempban";

        String admin = baninfo.getAdmin() != null ? baninfo.getAdmin() : "Brak";

        String reason = baninfo.getReason();
        if (reason.isEmpty()) reason = null;

        String message = main.getMessage("player-" + type + "-join-message", admin, player.getName(), reason);

        if (type.equalsIgnoreCase("tempban")) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm:ss");
            try {
                LocalDateTime date = LocalDateTime.parse(bandate, formatter);
                LocalDateTime now = LocalDateTime.now();

                if (date.isBefore(now)) {
                    if (!bandate.equalsIgnoreCase("perm")) banManager.unBanPlayer(player);
                    return;
                }

                message = message.replace("{time}", bandate);
            } catch (DateTimeParseException ex) {
                message += main.getMessage("ban-date-error", admin, player.getName(), reason);
            }
        }

        e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, message);
    }
}
