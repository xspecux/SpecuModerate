package com.specu.specuModerate;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class SMCommand implements CommandExecutor {
    private final Main main;
    private final Messages messages;

    public SMCommand(Main main, Messages messages) {
        this.main = main;
        this.messages = messages;
    }

    private static final Cache<UUID, Long> cooldown = CacheBuilder.newBuilder().expireAfterWrite(300, TimeUnit.MILLISECONDS).build();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
        if (!main.hasPermission(sender, "sm")) return false;

        if (!main.hasCooldown(sender, cooldown)) return false;

        switch(args[0].toLowerCase()) {
            case "reload" -> {
                messages.loadMessages();
                main.reloadConfig();
                main.setMuteTitle(main.getConfig().getBoolean("mute-title"));
                main.setWarnTitle(main.getConfig().getBoolean("warn-title"));
                main.setWhitelistEnabled(main.getConfig().getBoolean("whitelistenabled"));
                sender.sendMessage(main.getMessage("reload", sender.getName(), null, null));
                return true;
            }
            case "help" -> {
                sender.sendMessage(main.getMessage("help", sender.getName(), null, null));
                return true;
            }
            default -> {
                sender.sendMessage(main.getMessage("sm-bad-usage", sender.getName(), null, null));
                return false;
            }
        }
    }
}
