package com.specu.specuModerate.commands;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.specu.specuModerate.ban.BanManager;
import com.specu.specuModerate.ban.IpData;
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

public class IpBanCommand implements CommandExecutor {
    private final Main main;
    private final BanManager banManager;
    private final IpData ipData;
    public IpBanCommand(Main main, BanManager banManager, IpData ipData) {
        this.main = main;
        this.banManager = banManager;
        this.ipData = ipData;
    }

    private static final Cache<UUID, Long> cooldown = CacheBuilder.newBuilder().expireAfterWrite(300, TimeUnit.MILLISECONDS).build();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!main.hasPermission(sender, "ipban")) return false;

        if (!main.hasCooldown(sender, cooldown)) return false;

        if (args.length == 0) {
            sender.sendMessage(main.getMessage("ipban-usage", sender.getName(), null, null));
            return false;
        }

        OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);
        if (!player.hasPlayedBefore()) {
            sender.sendMessage(main.getMessage("not-found-ip", sender.getName(), player.getName(), null));
            return false;
        }

        String ip = ipData.getIps().get(player.getUniqueId());

        if (ip == null || ip.equals("unknown")) {
            sender.sendMessage(main.getMessage("not-found-ip", sender.getName(), player.getName(), null));
            return false;
        }

        String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        if (reason.isEmpty()) reason = null;

        banManager.banPlayer(player, reason, sender.getName(), true, ip, LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm:ss")), "perm");

        Player online = Bukkit.getPlayer(args[0]);

        main.handleReasonMessage(online, sender, player.getName(), "ipban", reason, null);
        return true;
    }
}
