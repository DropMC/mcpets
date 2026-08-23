package fr.nocsy.mcpets.data.config;

import fr.nocsy.mcpets.MCPets;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.logging.Level;

public class LanguageConfig extends AbstractConfig {

    public static LanguageConfig instance;

    @Getter
    private final HashMap<String, String> map = new HashMap<>();

    public static LanguageConfig getInstance() {
        if (instance == null)
            instance = new LanguageConfig();

        return instance;
    }

    /**
     * Bumped whenever the shipped wording changes in a way every server should pick up. A file older than this is
     * rewritten from the defaults, because keys are otherwise only ever added and never refreshed: without it a
     * server that already had a language.yml would keep the old text forever.
     */
    private static final int LANGUAGE_VERSION = 3;

    private static final String VERSION_KEY = "language_version";

    public void init() {
        super.init("", "language.yml");

        if (getConfig().getInt(VERSION_KEY, 1) < LANGUAGE_VERSION) {
            rewriteFromDefaults();
        }

        for (final Language lang : Language.values()) {
            if (getConfig().get(lang.name().toLowerCase()) == null)
                getConfig().set(lang.name().toLowerCase(), lang.getMessage());
        }

        getConfig().set(VERSION_KEY, LANGUAGE_VERSION);
        save();
        reload();
    }

    /**
     * Drops every message key so the loop above refills them from the enum. A copy of what was there is written next
     * to it first, since an admin may have edited the wording by hand.
     */
    private void rewriteFromDefaults() {
        final File current = new File(getFullPath());
        final File backup = new File(current.getParentFile(),
                "language.yml.backup-" + System.currentTimeMillis());
        try {
            Files.copy(current.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
            MCPets.getLog().info("Backed up language.yml to " + backup.getName() + " before refreshing it.");
        } catch (final IOException ex) {
            MCPets.getLog().log(Level.WARNING, "Could not back up language.yml before refreshing it", ex);
        }

        for (final Language lang : Language.values()) {
            getConfig().set(lang.name().toLowerCase(), null);
        }
    }

    @Override
    public void save() {
        super.save();
    }

    @Override
    public void reload() {
        loadConfig();
        map.clear();

        for (final Language lang : Language.values()) {
            if (getConfig().get(lang.name().toLowerCase()) != null)
                map.put(lang.name().toLowerCase(), getConfig().getString(lang.name().toLowerCase()));

            lang.reload();
        }

        if (Language.PET_INVENTORY_TITLE.getMessage().equals(Language.INVENTORY_PETS_MENU_INTERACTIONS.getMessage())) {
            MCPets.getLog().severe("Interaction menu and prime menu have the same name, which might lead to unexpected behaviors. Please consider having different names for both menus.");
        }

        MCPets.getLog().info("Language file reloaded.");
    }
}
