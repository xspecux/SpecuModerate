package com.specu.specuModerate.ModCommands;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.specu.specuModerate.Ban.BanManager;
import com.specu.specuModerate.Main;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class TempBanCommand implements CommandExecutor {
    private final Main main;
    private final BanManager banManager;
    public TempBanCommand(Main main, BanManager banManager) {
        this.main = main;
        this.banManager = banManager;
    }

    private static final Cache<UUID, Long> cooldown = CacheBuilder.newBuilder().expireAfterWrite(300, TimeUnit.MILLISECONDS).build();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!main.hasPermission(sender, "tempban")) return false;

        if (!main.hasCooldown(sender, cooldown)) return false;

        if (args.length < 3) {
            sender.sendMessage(main.getMessage("tempban-usage", sender.getName(), null, null));
            return false;
        }

        OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);

        int time;
        try {
            time = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage(main.getMessage("bad-tempban-time", sender.getName(), player.getName(), null));
            return false;
        }

        if (time <= 0) {
            sender.sendMessage(main.getMessage("negative-tempban-time", sender.getName(), player.getName(), null));
            return false;
        }

        if (args[2].length() != 1) {
            sender.sendMessage(main.getMessage("bad-tempban-unit", sender.getName(), player.getName(), null));
            return false;
        }

        char unit = args[2].charAt(0);
        LocalDateTime date;
        LocalDateTime now = LocalDateTime.now();

        switch (unit) {
            case 'd' -> {
                date = now.plusDays(time);
            }
            case 'h' -> {
                date = now.plusHours(time);
            }
            case 'm' -> {
                date = now.plusMinutes(time);
            }
            case 's' -> {
                date = now.plusSeconds(time);
            }
            default -> {
                System.out.println("Unknown time unit: " + unit);
                sender.sendMessage(main.getMessage("bad-tempban-unit", sender.getName(), player.getName(), null));
                return false;
            }
        }

        String reason = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
        if (reason.isEmpty()) reason = null;

        String formattedDate = date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm:ss"));

        banManager.banPlayer(player, reason, sender.getName(), false, null, now.format(DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm:ss")), formattedDate);

        Player online = Bukkit.getPlayer(args[0]);

        main.handleReasonMessage(online, sender, player.getName(), "unban", reason, formattedDate);
        return true;
    }
}
