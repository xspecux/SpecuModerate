package com.specu.specuModerate.ban;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.specu.specuModerate.Main;
import org.bukkit.OfflinePlayer;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class BanManager {
    private final Main main;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final ExecutorService saveExecutor = Executors.newSingleThreadExecutor();

    public BanManager(Main main) {
        this.main = main;
    }

    private Map<UUID, BanInfo> players = new HashMap<>();

    public void banPlayer(OfflinePlayer player, String reason, String admin, boolean ipBanned, String ip, String date, String bandate) {
        if (player.hasPlayedBefore()) {
            players.put(player.getUniqueId(), new BanInfo(player.getName(), reason, admin, ipBanned, ip, date, bandate));
            saveBansData();
        }
    }

    public void unBanPlayer(OfflinePlayer player) {
        if (player.hasPlayedBefore()) {
            players.remove(player.getUniqueId());
            saveBansData();
        }
    }

    public BanInfo getBanInfo(OfflinePlayer player) {
        return players.get(player.getUniqueId());
    }

    public boolean isPlayerBanned(OfflinePlayer player) {
        return players.containsKey(player.getUniqueId());
    }

    public void loadBansData() {
        File file = getBansFile();
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException("I can't create bans.json", e);
            }
        }

        try (FileReader reader = new FileReader(file)) {
            Type type = new TypeToken<HashMap<UUID, BanInfo>>() {}.getType();
            Map<UUID, BanInfo> loaded = gson.fromJson(reader, type);

            players = (loaded != null) ? loaded : new HashMap<>();

        } catch (IOException e) {
            e.printStackTrace();
            players = new HashMap<>();
        }
    }

    private void saveBansData() {
        Map<UUID, BanInfo> snapshot = new HashMap<>(players);

        saveExecutor.execute(() -> {
            File file = getBansFile();

            try (FileWriter writer = new FileWriter(file)) {
                gson.toJson(snapshot, writer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private File getBansFile() {
        return new File(main.getDataFolder(), "moderation/bans.json");
    }

    public void shutdown() {
        saveExecutor.shutdown();
        try {
            if (!saveExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                saveExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            saveExecutor.shutdownNow();
        }
    }
}
