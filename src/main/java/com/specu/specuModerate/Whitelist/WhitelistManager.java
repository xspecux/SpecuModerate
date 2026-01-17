package com.specu.specuModerate.Whitelist;

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
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class WhitelistManager {
    private final Main main;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final ExecutorService saveExecutor = Executors.newSingleThreadExecutor();

    public  WhitelistManager(Main main) {
        this.main = main;
    }

    private Map<UUID, String> whitelisted = new HashMap<>();

    public void addWhitelist(OfflinePlayer player) {
        if (player.hasPlayedBefore()) {
            whitelisted.put(player.getUniqueId(), player.getName());
            saveWhitelistData();
        }
    }

    public void removeWhitelist(OfflinePlayer player) {
        if (player.hasPlayedBefore()) {
            whitelisted.remove(player.getUniqueId());
            saveWhitelistData();
        }
    }

    public void clearWhitelist() {
        whitelisted.clear();
        saveWhitelistData();
    }

    public boolean isWhitelisted(OfflinePlayer player) {
        if (player.hasPlayedBefore()) {
            return whitelisted.containsKey(player.getUniqueId());
        }
        return false;
    }

    public boolean isWhitelistEnabled() {
        return main.isWhitelistEnabled();
    }

    public void setWhitelistEnabled(boolean enabled) {
        main.setWhitelistEnabled(enabled);
        main.getConfig().set("whitelistenabled", enabled);
        main.saveConfig();
    }

    public void loadWhitelistData() {
        File file = getWhitelistFile();
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException("I can't create whitelist.json", e);
            }
        }

        try (FileReader reader = new FileReader(file)) {
            Type type = new TypeToken<HashMap<UUID, String>>() {}.getType();
            Map<UUID, String> loaded = gson.fromJson(reader, type);

            whitelisted = (loaded != null) ? loaded : new HashMap<>();

        } catch (IOException e) {
            e.printStackTrace();
            whitelisted = new HashMap<>();
        }
    }

    private void saveWhitelistData() {
        Map<UUID, String> snapshot = new HashMap<>(whitelisted);

        saveExecutor.execute(() -> {
            File file = getWhitelistFile();

            try (FileWriter writer = new FileWriter(file)) {
                gson.toJson(snapshot, writer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private File getWhitelistFile() {
        return new File(main.getDataFolder(), "moderation/whitelist.json");
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
