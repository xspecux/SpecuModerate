package com.specu.specuModerate.listeners;

import com.specu.specuModerate.Main;
import com.specu.specuModerate.whitelist.WhitelistManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

public class WhitelistListener implements Listener {
    private final Main main;
    private final WhitelistManager whitelistManager;

    public WhitelistListener(Main main, WhitelistManager whitelistManager) {
        this.main = main;
        this.whitelistManager = whitelistManager;
    }

    @EventHandler
    public void onLogin(AsyncPlayerPreLoginEvent e) {
        if (!whitelistManager.isWhitelistEnabled()) return;

        OfflinePlayer player = Bukkit.getOfflinePlayer(e.getUniqueId());

        if (whitelistManager.isWhitelisted(player)) return;

        e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, main.getMessage("whitelist-disallow", null, player.getName(), null));
    }
}
