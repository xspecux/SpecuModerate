package com.specu.specuModerate;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Messages {
    private final Main main;
    public Messages(Main main) {
        this.main = main;
    }

    private Map<String, String> messages = new HashMap<>();
    public Map<String, String> getMessages() { return messages;}

    public void loadMessages() {
        Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
            String[] names = {
                    "pl_PL", "en_US", "custom"
            };
            for (String name : names) {
                File newfile = new File(main.getDataFolder(), "languages/" + name + ".yml");
                if (!newfile.exists()) {
                    main.saveResource("languages/" + name + ".yml", false);
                }
            }

            String lang = main.getConfig().getString("messagesfile");
            File file = new File(main.getDataFolder(), "languages/" + lang + ".yml");

            if (!file.exists()) {
                main.getLogger().warning("Language file " + lang + " doesn't exist!");
            }

            messages.clear();

            YamlConfiguration conf = YamlConfiguration.loadConfiguration(file);

            for (String key : conf.getKeys(true)) {

                if (key != null) {
                    String message;
                    String prefix = conf.getString("prefix");

                    message = conf.getString(key);

                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("{prefix}", prefix);

                    for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                        String keyPlaceholder = entry.getKey();
                        String value = entry.getValue() == null ? "" : entry.getValue();
                        message = message.replace(keyPlaceholder, value);
                    }
                    messages.put(key, Main.formatColor(message));
                }
            }
            Bukkit.getScheduler().runTask(main, () -> {
                main.getLogger().info("Loaded " + messages.size() + " messages from " + lang + ".yml!");
            });
        });
    }
}