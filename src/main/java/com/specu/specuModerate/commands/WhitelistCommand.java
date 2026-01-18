package com.specu.specuModerate.commands;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.specu.specuModerate.Main;
import com.specu.specuModerate.whitelist.WhitelistManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class WhitelistCommand implements CommandExecutor {
    private final Main main;
    private final WhitelistManager whitelistManager;

    public WhitelistCommand(Main main, WhitelistManager whitelistManager) {
        this.main = main;
        this.whitelistManager = whitelistManager;
    }

    private static final Cache<UUID, Long> cooldown = CacheBuilder.newBuilder().expireAfterWrite(300, TimeUnit.MILLISECONDS).build();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
        if (!main.hasPermission(sender, "whitelist")) return false;

        if (!main.hasCooldown(sender, cooldown)) return false;

        if (args.length == 0) {
            sender.sendMessage(main.getMessage("whitelist-usage", sender.getName(), null, null));
            return false;
        }

        switch (args[0].toLowerCase()) {
            case "add" -> {
                if (args.length != 2) {
                    sender.sendMessage(main.getMessage("whitelist-add-usage", sender.getName(), null, null));
                    return false;
                }

                OfflinePlayer player = Bukkit.getOfflinePlayer(args[1]);

                if (whitelistManager.isWhitelisted(player)) {
                    sender.sendMessage(main.getMessage("player-already-whitelisted", sender.getName(), player.getName(), null));
                    return false;
                }

                whitelistManager.addWhitelist(player);
                sender.sendMessage(main.getMessage("admin-whitelist-add-message", sender.getName(), player.getName(), null));
                System.out.println(main.getMessage("console-whitelist-add-message", sender.getName(), player.getName(), null));

                if (player.isOnline()) {
                    Player online =  player.getPlayer();
                    online.sendMessage(main.getMessage("player-whitelist-add-message", sender.getName(), online.getName(), null));
                }
                return true;
            }

            case "remove" -> {
                if (args.length != 2) {
                    sender.sendMessage(main.getMessage("whitelist-remove-usage", sender.getName(), null, null));
                    return false;
                }

                OfflinePlayer player = Bukkit.getOfflinePlayer(args[1]);

                if (!whitelistManager.isWhitelisted(player)) {
                    sender.sendMessage(main.getMessage("player-not-whitelisted", sender.getName(), player.getName(), null));
                    return false;
                }

                whitelistManager.removeWhitelist(player);
                sender.sendMessage(main.getMessage("admin-whitelist-remove-message", sender.getName(), player.getName(), null));
                System.out.println(main.getMessage("console-whitelist-remove-message", sender.getName(), player.getName(), null));

                if (player.isOnline()) {
                    Player online =  player.getPlayer();
                    online.sendMessage(main.getMessage("player-whitelist-remove-message", sender.getName(), online.getName(), null));

                    if (whitelistManager.isWhitelisted(online)) {
                        online.kickPlayer(main.getMessage("player-whitelist-remove-kick-message", sender.getName(), online.getName(), null));
                    }
                }

                return true;
            }

            case "clear" -> {
                if (whitelistManager.isWhitelistEnabled()) {
                    sender.sendMessage(main.getMessage("cannot-clear-whitelist", sender.getName(), null, null));
                    return false;
                }

                whitelistManager.clearWhitelist();
                sender.sendMessage(main.getMessage("whitelist-cleared", sender.getName(), null, null));
                return true;
            }

            case "on" -> {
                if (whitelistManager.isWhitelistEnabled()) {
                    sender.sendMessage(main.getMessage("whitelist-already-enabled", sender.getName(), null, null));
                    return false;
                }

                whitelistManager.setWhitelistEnabled(true);
                sender.sendMessage(main.getMessage("whitelist-enabled", sender.getName(), null, null));

                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!whitelistManager.isWhitelisted(player)) {
                        player.kickPlayer(main.getMessage("whitelist-enabled-kick", sender.getName(), player.getName(), null));
                    }
                }
                return true;
            }

            case "off" -> {
                if (!whitelistManager.isWhitelistEnabled()) {
                    sender.sendMessage(main.getMessage("whitelist-already-disabled", sender.getName(), null, null));
                    return false;
                }

                whitelistManager.setWhitelistEnabled(false);
                sender.sendMessage(main.getMessage("whitelist-disabled", sender.getName(), null, null));
                return true;
            }

            default -> sender.sendMessage(main.getMessage("whitelist-usage", sender.getName(), null, null));
        }
        return true;
    }
}
