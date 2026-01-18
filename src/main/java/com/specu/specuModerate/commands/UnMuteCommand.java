package com.specu.specuModerate.commands;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.specu.specuModerate.Main;
import com.specu.specuModerate.mute.MuteManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class UnMuteCommand implements CommandExecutor {
    private final Main main;
    private final MuteManager muteManager;
    public UnMuteCommand(Main main, MuteManager muteManager) {
        this.main = main;
        this.muteManager = muteManager;
    }

    private static final Cache<UUID, Long> cooldown = CacheBuilder.newBuilder().expireAfterWrite(300, TimeUnit.MILLISECONDS).build();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!main.hasPermission(sender, "unmute")) return false;

        if (!main.hasCooldown(sender, cooldown)) return false;

        if (args.length == 0) {
            sender.sendMessage(main.getMessage("unmute-usage", sender.getName(), null, null));
            return false;
        }

        OfflinePlayer player =  Bukkit.getOfflinePlayer(args[0]);
        if (!player.hasPlayedBefore()) {
            sender.sendMessage(main.getMessage("not-found-unmute", sender.getName(), player.getName(), null));
            return false;
        }

        if (!muteManager.isPlayerMuted(player)) {
            sender.sendMessage(main.getMessage("not-found-unmute", sender.getName(), player.getName(), null));
            return false;
        }

        muteManager.unMutePlayer(player);
        sender.sendMessage(main.getMessage("admin-unmute-message", sender.getName(), player.getName(), null));
        System.out.println(main.getMessage("console-unmute-message", sender.getName(), player.getName(), null));
        if (player.isOnline()) {
            Player online = player.getPlayer();
            online.sendMessage(main.getMessage("player-unmute-message", sender.getName(), player.getName(), null));
        }
        return true;
    }
}
