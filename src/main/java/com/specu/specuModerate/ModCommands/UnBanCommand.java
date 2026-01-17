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

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class UnBanCommand implements CommandExecutor {
    private final Main main;
    private final BanManager banManager;
    public UnBanCommand(Main main, BanManager banManager) {
        this.main = main;
        this.banManager = banManager;
    }

    private static final Cache<UUID, Long> cooldown = CacheBuilder.newBuilder().expireAfterWrite(300, TimeUnit.MILLISECONDS).build();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!main.hasPermission(sender, "unban")) return false;

        if (!main.hasCooldown(sender, cooldown)) return false;

        if (args.length == 0) {
            sender.sendMessage(main.getMessage("unban-usage", sender.getName(), null, null));
            return false;
        }

        OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);
        if (!player.hasPlayedBefore()) {
            sender.sendMessage(main.getMessage("not-found-unban", sender.getName(), player.getName(), null));
            return false;
        }

        if (!banManager.isPlayerBanned(player)) {
            sender.sendMessage(main.getMessage("not-found-unban", sender.getName(), player.getName(), null));
            return false;
        }

        banManager.unBanPlayer(player);
        sender.sendMessage(main.getMessage("admin-unban-message", sender.getName(), player.getName(), null));
        System.out.println(main.getMessage("console-unban-message", sender.getName(), player.getName(), null));
        return true;
    }
}
