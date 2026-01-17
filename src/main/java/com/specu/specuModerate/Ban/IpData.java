package com.specu.specuModerate.Ban;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.specu.specuModerate.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

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

public class IpData implements Listener {
    private final Main main;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final ExecutorService saveExecutor = Executors.newSingleThreadExecutor();

    public IpData(Main main) { this.main = main; }

    private Map<UUID, String> ips = new HashMap<>();
    public Map<UUID, String> getIps() { return ips; }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        saveIp(e.getPlayer());
    }

    private void saveIp(Player player) {
        String ip = player.getAddress() != null ? player.getAddress().getHostString() : "Unknown";
        ips.put(player.getUniqueId(), ip);

        Map<UUID, String> snapshot = new HashMap<>(ips);

        saveExecutor.execute(() -> {
            File file = getIpFile();

            try (FileWriter writer = new FileWriter(file)) {
                gson.toJson(snapshot, writer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void loadIpData() {
        File file = getIpFile();
        File parent = file.getParentFile();

        if (!parent.exists()) {
            parent.mkdirs();
        }

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException("I can't create ips.json", e);
            }
        }

        try (FileReader reader = new FileReader(file)) {
            Type type = new TypeToken<HashMap<UUID, String>>() {}.getType();
            Map<UUID, String> loaded = gson.fromJson(reader, type);

            ips = (loaded != null) ? loaded : new HashMap<>();

        } catch (IOException e) {
            e.printStackTrace();
            ips = new HashMap<>();
        }
    }

    private File getIpFile() {
        return new File(main.getDataFolder(), "data/ips.json");
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
