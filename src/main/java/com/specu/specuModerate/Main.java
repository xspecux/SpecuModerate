package com.specu.specuModerate;

import com.google.common.cache.Cache;
import com.specu.specuModerate.Ban.*;
import com.specu.specuModerate.Listeners.BanListener;
import com.specu.specuModerate.Listeners.MuteListener;
import com.specu.specuModerate.Listeners.WhitelistListener;
import com.specu.specuModerate.ModCommands.*;
import com.specu.specuModerate.ModCommands.MuteCommand;
import com.specu.specuModerate.Mute.MuteManager;
import com.specu.specuModerate.ModCommands.UnMuteCommand;
import com.specu.specuModerate.Punish.PunishCommand;
import com.specu.specuModerate.Punish.PunishManager;
import com.specu.specuModerate.Whitelist.WhitelistManager;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public final class Main extends JavaPlugin {
    private Messages messages;
    private IpData ipData;
    private BanManager banManager;
    private MuteManager muteManager;
    private WhitelistManager whitelistManager;
    private BanCommand banCommand;
    private BanListener banListener;
    private IpBanCommand ipBanCommand;
    private TempBanCommand tempBanCommand;
    private UnBanCommand unbanCommand;
    private KickCommand kickCommand;
    private MuteCommand muteCommand;
    private MuteListener muteListener;
    private UnMuteCommand unmuteCommand;
    private WarnCommand warnCommand;
    private PunishManager punishManager;
    private PunishCommand punishCommand;
    private WhitelistCommand whitelistCommand;
    private WhitelistListener whitelistListener;

    private SMCommand smCommand;

    private boolean muteTitle;
    private boolean warnTitle;
    private boolean whitelistEnabled;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        muteTitle = getConfig().getBoolean("mutetitle");
        warnTitle = getConfig().getBoolean("warntitle");
        whitelistEnabled = getConfig().getBoolean("whitelistenabled");

        // ZAREJESTROWAC TU I PLUGIN.YML KOMENDY
        // PUNISHCOMMAND

        this.messages = new Messages(this);
        messages.loadMessages();

        this.ipData = new IpData(this);
        ipData.loadIpData();
        Bukkit.getPluginManager().registerEvents(ipData, this);

        this.banManager = new BanManager(this);
        banManager.loadBansData();

        this.muteManager = new MuteManager(this);
        muteManager.loadMutesData();

        this.whitelistManager = new WhitelistManager(this);
        whitelistManager.loadWhitelistData();

        this.banCommand = new BanCommand(this, banManager);
        getCommand("ban").setExecutor(banCommand);
        this.ipBanCommand = new IpBanCommand(this, banManager, ipData);
        getCommand("ipban").setExecutor(ipBanCommand);
        getCommand("ip-ban").setExecutor(ipBanCommand);
        getCommand("banip").setExecutor(ipBanCommand);
        getCommand("ban-ip").setExecutor(ipBanCommand);
        this.tempBanCommand = new TempBanCommand(this, banManager);
        getCommand("tempban").setExecutor(tempBanCommand);
        getCommand("tban").setExecutor(tempBanCommand);
        this.unbanCommand = new UnBanCommand(this, banManager);
        getCommand("unban").setExecutor(unbanCommand);
        this.kickCommand = new KickCommand(this);
        getCommand("kick").setExecutor(kickCommand);
        this.muteCommand = new MuteCommand(this, muteManager);
        getCommand("mute").setExecutor(muteCommand);
        this.unmuteCommand = new UnMuteCommand(this, muteManager);
        getCommand("unmute").setExecutor(unmuteCommand);
        this.warnCommand = new WarnCommand(this);
        getCommand("warn").setExecutor(warnCommand);
        this.whitelistCommand = new WhitelistCommand(this, whitelistManager);
        getCommand("whitelist").setExecutor(whitelistCommand);
        getCommand("wl").setExecutor(whitelistCommand);
        getCommand("wlist").setExecutor(whitelistCommand);
        getCommand("whitel").setExecutor(whitelistCommand);

        this.whitelistListener = new WhitelistListener(this, whitelistManager);
        Bukkit.getPluginManager().registerEvents(whitelistListener, this);

        this.muteListener = new MuteListener(this, muteManager);
        Bukkit.getPluginManager().registerEvents(muteListener, this);

        this.banListener = new BanListener(this, banManager);
        Bukkit.getPluginManager().registerEvents(banListener, this);

        this.punishManager = new PunishManager(this);
        this.punishCommand = new PunishCommand(this, punishManager);

        this.smCommand = new SMCommand(this, messages);

    }

    @Override
    public void onDisable() {
        whitelistManager.shutdown();
        muteManager.shutdown();
        banManager.shutdown();
        ipData.shutdown();
    }

    public boolean hasCooldown(CommandSender sender, Cache<UUID, Long> cooldown) {
        if (sender instanceof Player player) {
            Long cooldownend = cooldown.getIfPresent(player.getUniqueId());
            if (cooldownend != null) {
                player.sendMessage(getMessage("command-cooldown", player.getName(), null, null));
                return false;
            }
            cooldown.put(player.getUniqueId(), System.currentTimeMillis() + 500);
        }
        return true;
    }

    public boolean hasPermission(CommandSender sender, String permission) {
        if (!sender.hasPermission("specu.moderate." + permission)) {
            sender.sendMessage(getMessage("no-permission", sender.getName(), null, null));
            return false;
        }
        return true;
    }

    public void handleReasonMessage(Player target, CommandSender sender, String arg, String type, String reason, String temp) {
        String player = "player-" + type + "-message";
        String console = "console-" + type + "-message";
        String admin = "admin-" + type + "-message";

        if (reason != null) {
            player += "-reason";
            console += "-reason";
            admin += "-reason";
        }

        String kickmsg = getMessage(player, sender.getName(), arg, reason);
        String consolemsg = getMessage(console, sender.getName(), arg, reason);
        String adminmsg = getMessage(admin, sender.getName(), arg, reason);

        if (temp != null) {
            kickmsg = kickmsg.replace("{time}", temp);
            consolemsg = consolemsg.replace("{time}", temp);
            adminmsg = adminmsg.replace("{time}", temp);
        }

        if (target != null && target.isOnline()) target.kickPlayer(kickmsg);
        System.out.println(consolemsg);
        sender.sendMessage(adminmsg);
    }

    public String getMessage(String key, String admin, String player, String reason) {
        String msg = messages.getMessages().get(key);

        if (admin != null && !admin.isBlank()) msg = msg.replace("{admin}", admin);
        if (player != null && !player.isBlank()) msg = msg.replace("{player}", player);
        if (reason != null && !reason.isBlank()) msg = msg.replace("{reason}", reason);

        return msg;
    }

    public static String formatColor(String text) {
        if (text == null) return "";

        text = ChatColor.translateAlternateColorCodes('&', text);

        Pattern hexPattern = Pattern.compile("&#([A-Fa-f0-9]{6})");
        Matcher matcher = hexPattern.matcher(text);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String hex = "#" + matcher.group(1);
            matcher.appendReplacement(buffer, ChatColor.of(hex).toString());
        }
        matcher.appendTail(buffer);

        return buffer.toString();
    }

    public boolean isMuteTitle() { return muteTitle; }

    public boolean isWarnTitle() { return warnTitle; }

    public boolean isWhitelistEnabled() { return whitelistEnabled; }

    public void setMuteTitle(boolean muteTitle) { this.muteTitle = muteTitle; }

    public void setWarnTitle(boolean warnTitle) { this.warnTitle = warnTitle; }

    public void setWhitelistEnabled(boolean whitelistEnabled) { this.whitelistEnabled = whitelistEnabled; }
}
