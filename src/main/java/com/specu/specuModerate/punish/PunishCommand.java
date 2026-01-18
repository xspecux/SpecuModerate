package com.specu.specuModerate.punish;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.specu.specuModerate.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PunishCommand implements CommandExecutor {
    private final Main main;
    private final PunishManager punishManager;
    public PunishCommand(Main main, PunishManager punishManager) {
        this.main = main;
        this.punishManager = punishManager;
    }

    private static final Cache<UUID, Long> cooldown = CacheBuilder.newBuilder().expireAfterWrite(300, TimeUnit.MILLISECONDS).build();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!main.hasPermission(sender, "punish")) return false;

        if (!main.hasCooldown(sender, cooldown)) return false;

        if (!(sender instanceof Player player)) return false;

        if (args.length == 0) {
            //player.sendMessage(main.getMessage());
            return false;
        }

        //modGuiManager.firstGui(player, args[0]);
        return true;
    }
}
