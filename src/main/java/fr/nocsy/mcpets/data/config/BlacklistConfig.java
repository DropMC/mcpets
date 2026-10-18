package fr.nocsy.mcpets.data.config;

import fr.nocsy.mcpets.MCPets;
import lombok.Getter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class BlacklistConfig extends AbstractConfig {

    public static BlacklistConfig instance;

    @Getter
    private final List<String> blackListedWords = new CopyOnWriteArrayList<>();

    public static BlacklistConfig getInstance() {
        if (instance == null)
            instance = new BlacklistConfig();

        return instance;
    }

    public void init() {
        super.init("", "blacklist.yml");
        save();
        reload();
    }

    @Override
    public void save() {
        super.save();
    }

    @Override
    public void reload() {
        loadConfig();

        blackListedWords.clear();
        blackListedWords.addAll(getConfig().getStringList("Blacklist"));

        MCPets.getLog().info("Blacklist file reloaded.");
    }
}
