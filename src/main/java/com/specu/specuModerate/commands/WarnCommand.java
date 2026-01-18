package com.specu.specuModerate.commands;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.specu.specuModerate.Main;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class WarnCommand implements CommandExecutor {
    private final Main main;
    public WarnCommand(Main main) {
        this.main = main;
    }

    private static final Cache<UUID, Long> cooldown = CacheBuilder.newBuilder().expireAfterWrite(300, TimeUnit.MILLISECONDS).build();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!main.hasPermission(sender, "warn")) return false;

        if (!main.hasCooldown(sender, cooldown)) return false;

        if (args.length == 0) {
            sender.sendMessage(main.getMessage("warn-usage", sender.getName(), null, null));
            return false;
        }

        Player player = Bukkit.getPlayer(args[0]);
        if (player == null || !player.isOnline()) {
            sender.sendMessage(main.getMessage("not-found-warn", sender.getName(), args[0], null));
            return false;
        }

        String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        if (reason.isEmpty()) reason = null;

        if (main.isWarnTitle()) {
            player.sendTitle(main.getMessage("warn-title", sender.getName(), args[0], reason), main.getMessage("warn-subtitle", sender.getName(), args[0], reason), 0, 200, 15);
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 50, 998));
            player.playSound(player.getLocation(), Sound.ENTITY_ALLAY_DEATH, 2.5F, 0.6F);
        }

        main.handleReasonMessage(player, sender, player.getName(), "warn", reason, null);
        return true;
    }
}

