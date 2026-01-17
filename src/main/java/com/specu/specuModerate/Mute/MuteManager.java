package com.specu.specuModerate.Mute;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.specu.specuModerate.Main;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MuteManager {
    private final Main main;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final ExecutorService saveExecutor = Executors.newSingleThreadExecutor();

    public MuteManager(Main main) { this.main = main; }

    private Map<UUID, MuteInfo> mutes = new HashMap<>();

    public void mutePlayer(OfflinePlayer player, String admin, String reason, LocalDateTime time) {
        if (player.hasPlayedBefore()) {
            mutes.put(player.getUniqueId(), new MuteInfo(player.getName(), admin, reason, time));
            saveMutes();
        }
    }

    public void unMutePlayer(OfflinePlayer player) {
        if (player.hasPlayedBefore()) {
            mutes.remove(player.getUniqueId());
            saveMutes();
        }
    }

    public MuteInfo getMuteInfo(UUID uuid) {
        return mutes.get(uuid);
    }

    public boolean isPlayerMuted(OfflinePlayer player) {
        return mutes.containsKey(player.getUniqueId());
    }

    private void saveMutes() {
        Map<UUID, MuteInfo> snapshot = new HashMap<>(mutes);

        saveExecutor.execute(() -> {
            File file = getMuteFile();

            try (FileWriter writer = new FileWriter(file)) {
                gson.toJson(snapshot, writer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void loadMutesData() {
        File file = getMuteFile();
        File parent = file.getParentFile();

        if (!parent.exists()) {
            parent.mkdirs();
        }

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException("I can't create mutes.json", e);
            }
        }

        try (FileReader reader = new FileReader(file)) {
            Type type = new TypeToken<HashMap<UUID, MuteInfo>>() {}.getType();
            Map<UUID, MuteInfo> loaded = gson.fromJson(reader, type);

            mutes = (loaded != null) ? loaded : new HashMap<>();

        } catch (IOException e) {
            e.printStackTrace();
            mutes = new HashMap<>();
        }
    }

    private File getMuteFile() {
        return new File(main.getDataFolder(), "moderation/mutes.json");
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
