package com.specu.specuModerate.commands;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.specu.specuModerate.ban.BanManager;
import com.specu.specuModerate.Main;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


public class BanCommand implements CommandExecutor {
    private final Main main;
    private final BanManager banManager;

    public BanCommand(Main main, BanManager banManager) {
        this.main = main;
        this.banManager = banManager;
    }

    private static final Cache<UUID, Long> cooldown = CacheBuilder.newBuilder().expireAfterWrite(300, TimeUnit.MILLISECONDS).build();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!main.hasPermission(sender, "ban")) return false;

        if (!main.hasCooldown(sender, cooldown)) return false;

        if (args.length == 0) {
            sender.sendMessage(main.getMessage("ban-usage", sender.getName(), null, null));
            return false;
        }

        OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);

        String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        if (reason.isEmpty()) reason = null;

        banManager.banPlayer(player, reason, sender.getName(), false, null, LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm:ss")), "perm");

        Player online = player.getPlayer();

        main.handleReasonMessage(online, sender, player.getName(), "ban", reason, null);
        return true;
    }
}
