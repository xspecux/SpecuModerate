package com.specu.specuModerate.commands;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.specu.specuModerate.Main;
import com.specu.specuModerate.mute.MuteManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


public class MuteCommand implements CommandExecutor {
    private final Main main;
    private final MuteManager muteManager;
    public MuteCommand(Main main, MuteManager muteManager) {
        this.main = main;
        this.muteManager = new MuteManager(main);
    }

    private static final Cache<UUID, Long> cooldown = CacheBuilder.newBuilder().expireAfterWrite(300, TimeUnit.MILLISECONDS).build();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!main.hasPermission(sender, "mute")) return false;

        if (!main.hasCooldown(sender, cooldown)) return false;

        if (args.length < 3) {
            sender.sendMessage(main.getMessage("mute-usage", sender.getName(), null, null));
            return false;
        }

        OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);
        if (!player.hasPlayedBefore()) {
            sender.sendMessage(main.getMessage("not-found-mute", sender.getName(), player.getName(), null));
            return false;
        }

        int time;
        try {
            time = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage(main.getMessage("bad-mute-time", sender.getName(), player.getName(), null));
            return false;
        }

        if (time <= 0) {
            sender.sendMessage(main.getMessage("negative-mute-time", sender.getName(), player.getName(), null));
            return false;
        }

        if (args[2].length() != 1) {
            sender.sendMessage(main.getMessage("bad-mute-unit", sender.getName(), player.getName(), null));
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
                sender.sendMessage(main.getMessage("bad-mute-unit", sender.getName(), player.getName(), null));
                return false;
            }
        }

        String reason = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
        if (reason.isEmpty()) reason = null;

        String formattedDate = date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm:ss"));

        muteManager.mutePlayer(player, sender.getName(), reason, date);

        Player online = player.getPlayer();

        String title = main.getMessage("mute-title", sender.getName(), player.getName(), reason).replace("{time}", formattedDate);
        String subtitle = main.getMessage("mute-subtitle", sender.getName(), player.getName(), reason).replace("{time}", formattedDate);

        if (main.isMuteTitle()) {
            online.sendTitle(title, subtitle, 0, 200, 15);
            online.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 50, 998));
            online.playSound(online.getLocation(), Sound.ENTITY_ALLAY_DEATH, 2.5F, 0.6F);
        }

        main.handleReasonMessage(online, sender, player.getName(), "mute", reason, formattedDate);
        return false;

    }
}
