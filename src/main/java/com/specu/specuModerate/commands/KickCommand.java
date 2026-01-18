package com.specu.specuModerate.commands;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.specu.specuModerate.Main;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class KickCommand implements CommandExecutor {
    private final Main main;
    public KickCommand(Main main) {
        this.main = main;
    }

    private static final Cache<UUID, Long> cooldown = CacheBuilder.newBuilder().expireAfterWrite(300, TimeUnit.MILLISECONDS).build();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!main.hasPermission(sender, "kick")) return false;

        if (!main.hasCooldown(sender, cooldown)) return false;

        if (args.length == 0) {
            sender.sendMessage(main.getMessage("kick-usage", sender.getName(), null, null));
            return false;
        }

        Player player = Bukkit.getPlayer(args[0]);
        if (player == null || !player.isOnline()) {
            sender.sendMessage(main.getMessage("not-found-kick", sender.getName(), args[0], null));
            return false;
        }

        String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        if (reason.isEmpty()) reason = null;

        main.handleReasonMessage(player, sender, player.getName(), "kick", reason, null);
        return true;
    }
}
