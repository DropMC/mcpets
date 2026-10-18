package fr.nocsy.mcpets;

import fr.nocsy.mcpets.commands.CommandRegistry;

import java.util.Map;
import java.util.List;
import java.util.UUID;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.CompletableFuture;

import com.google.common.collect.Lists;

import lombok.Getter;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import net.luckperms.api.LuckPerms;

import com.sk89q.worldguard.WorldGuard;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.api.skills.placeholders.PlaceholderManager;
import io.lumine.mythic.core.skills.CustomComponentRegistry;
import io.lumine.mythic.core.utils.annotations.AnnotationUtil;
import io.lumine.mythic.core.utils.annotations.MythicPlaceholder;

import fr.nocsy.mcpets.data.Pet;
import fr.nocsy.mcpets.data.VanishedPets;
import fr.nocsy.mcpets.data.PetSkin;
import fr.nocsy.mcpets.data.sql.Databases;
import fr.nocsy.mcpets.data.sql.PlayerData;
import fr.nocsy.mcpets.data.config.PetConfig;
import fr.nocsy.mcpets.data.flags.FlagsManager;
import fr.nocsy.mcpets.modeler.AbstractModeler;
import fr.nocsy.mcpets.listeners.EventListener;
import fr.nocsy.mcpets.data.livingpets.PetStats;
import fr.nocsy.mcpets.data.config.GlobalConfig;
import fr.nocsy.mcpets.data.config.PetFoodConfig;
import fr.nocsy.mcpets.data.config.CategoryConfig;
import fr.nocsy.mcpets.data.config.LanguageConfig;
import fr.nocsy.mcpets.modeler.BetterModelModeler;
import fr.nocsy.mcpets.modeler.ModelEngineModeler;
import fr.nocsy.mcpets.data.config.AbstractConfig;
import fr.nocsy.mcpets.compat.PlaceholderAPICompat;
import fr.nocsy.mcpets.data.config.BlacklistConfig;
import fr.nocsy.mcpets.data.config.ItemsListConfig;
import fr.nocsy.mcpets.velocity.VelocitySyncManager;

import static fr.nocsy.mcpets.mythicmobs.MythicListener.*;

public class MCPets extends JavaPlugin {

    @Getter
    private static MCPets instance;

    private static MythicBukkit mythicMobs;
    private static LuckPerms luckPerms;
    private static boolean luckPermsNotFound = false;
    private static boolean nexoFound = false;
    private static boolean nexoChecked = false;

    @Getter
    private static CustomComponentRegistry componentRegistry;

    @Getter
    private static AbstractModeler modeler;

    @Getter
    private static PlaceholderAPICompat placeholderAPI;

    @Getter
    private static final String prefix = "§8[§»";

    public static void loadConfigs() {
        ItemsListConfig.getInstance().init();
        PetFoodConfig.getInstance().init();
        GlobalConfig.getInstance().init();
        LanguageConfig.getInstance().init();
        BlacklistConfig.getInstance().init();
        PetConfig.loadPets(AbstractConfig.getPath() + "Pets/", true);
        CategoryConfig.load(AbstractConfig.getPath() + "Categories/", true);

        // Run DB initialization asynchronously to avoid freezing the main thread.
        // Tasks that depend on isDatabaseSupport() being correctly set (autosave scheduler,
        // Velocity init) must run AFTER this completes — see scheduleDbDependentTasks().
        Bukkit.getAsyncScheduler().runNow(instance, t -> {
            Databases.init();
            PlayerData.initAll();
            // Hop back to the global region for tasks that must schedule from a tick thread
            Bukkit.getGlobalRegionScheduler().run(instance, t2 -> scheduleDbDependentTasks());
        });
    }

    /**
     * Tasks that depend on the DB connection state being known (isDatabaseSupport()).
     * Called from the async DB init path so that PetStats.saveStats() picks the correct
     * sync/async branch — otherwise a sync YAML autosave gets scheduled while DB init is
     * still pending, then runs heavy MySQL writes on the main thread once DB connects.
     */
    private static void scheduleDbDependentTasks() {
        PetStats.saveStats();
        if (GlobalConfig.getInstance().isVelocityEnabled()) {
            VelocitySyncManager.init();
            getLog().info("[MCPets] : Velocity sync enabled.");
        }
    }

    @Override
    public void onLoad() {
        instance = this;

        // Reset static flags for PlugMan reload support
        nexoFound = false;
        nexoChecked = false;
        luckPermsNotFound = false;
        modeler = null;

        if (!checkMythicMobs()) {
            getLog().severe("MCPets could not be loaded : MythicMobs could not be found or this version is not compatible with the plugin.");
            return;
        }

        if (!checkModeler()) {
            getLog().severe("MCPets could not be loaded : Neither ModelEngine nor BetterModel could be found.");
            return;
        }

        checkWorldGuard();
        checkLuckPerms();
        checkPlaceholderApi();
        if (!checkNexo()) {
            getLog().info("Nexo was not found. Custom items features won't be available.");
        }

        try {
            if (GlobalConfig.getInstance().isWorldGuardSupport()) {
                FlagsManager.init(this);
            }
        } catch (final Exception ex) {
            getLog().log(Level.SEVERE, "Flag manager has raised an exception", ex);
        }
    }

    @Override
    public void onEnable() {
        CommandRegistry.register(this);
        EventListener.init(this);
        modeler.registerListeners(this);
        registerMythicComponents();

        Bukkit.getGlobalRegionScheduler().run(this, t -> {
            loadConfigs();
            // PetStats.saveStats() and VelocitySyncManager.init() are scheduled inside
            // loadConfigs() once async DB init completes — see scheduleDbDependentTasks()

            getLog().info("-=-=-=-= MCPets loaded =-=-=-=-");
            getLog().info("      Plugin made by Nocsy     ");
            getLog().info("-=-=-=-= -=-=-=-=-=-=- =-=-=-=-");

            FlagsManager.launchFlags();
            VanishedPets.start(this);
        });
    }

    /**
     * Hands this plugin's mechanics, conditions, targeters and placeholders to MythicMobs before it builds its skills.
     * <p>
     * MythicMobs reads its packs while it enables, which is before this plugin, but it only builds each skill's
     * mechanics on the first tick, asking every listener for the names it does not know; registering here, still
     * inside the enable phase, answers those requests. Placeholders are different: an expression such as
     * {@code cooldown=(30-(<pet.power>*0.2))} was tokenized while the packs were read, with the tag as plain text, so the
     * placeholders are registered directly and MythicMobs re-tokenizes every expression it has. That replaces the
     * full /mm reload this used to run, which held the tick for several seconds on every start.
     */
    private void registerMythicComponents() {
        componentRegistry = new CustomComponentRegistry(instance, Lists.newArrayList());
        componentRegistry.registerCustomComponent(CustomComponentRegistry.MythicComponentType.PLACEHOLDER, PLACEHOLDER_PACKAGE)
                .registerCustomComponent(CustomComponentRegistry.MythicComponentType.CONDITION, CONDITION_PACKAGE)
                .registerCustomComponent(CustomComponentRegistry.MythicComponentType.TARGETER, TARGETER_PACKAGE)
                .registerCustomComponent(CustomComponentRegistry.MythicComponentType.MECHANIC, MECHANIC_PACKAGE);

        final PlaceholderManager placeholders = getMythicMobs().getPlaceholderManager();
        for (final Class<?> placeholder : AnnotationUtil.getAnnotatedClasses(this, PLACEHOLDER_PACKAGE, MythicPlaceholder.class)) {
            placeholders.register(placeholder);
        }
        placeholders.recheckForPlaceholders();
    }

    @Override
    public void onDisable() {
        getLog().info("-=-=-=-= MCPets disabled =-=-=-=-");
        getLog().info("          See you soon           ");
        getLog().info("-=-=-=-= -=-=-=-=-=-=-=- =-=-=-=-");

        CommandRegistry.unregister(this);

        if (modeler != null) {
            modeler.unregisterListeners();
        }

        // Run all DB saves on a separate thread to avoid freezing the main thread
        final CompletableFuture<Void> saveFuture = CompletableFuture.runAsync(() -> {
            PetStats.saveAll();

            // Save all active pets to DB before clearing them so that a server restart
            // does not wipe the mcpets_active_pet records — players rejoin with their pet intact.
            if (GlobalConfig.getInstance().isVelocityEnabled()
                    && GlobalConfig.getInstance().isDatabaseSupport()) {
                for (Map.Entry<UUID, List<Pet>> entry : Pet.getActivePets().entrySet()) {
                    List<Pet> activePets = entry.getValue();
                    if (activePets == null || activePets.isEmpty()) {
                        continue;
                    }

                    List<String> ids = new ArrayList<>();
                    Map<String, String> skinIds = new HashMap<>();
                    for (Pet pet : activePets) {
                        if (pet == null) continue;

                        ids.add(pet.getId());
                        final PetSkin skin = pet.getActiveSkin();
                        if (skin != null) {
                            skinIds.put(pet.getId(), skin.getPathId());
                        }
                    }

                    if (ids.isEmpty()) continue;

                    Databases.saveActivePet(entry.getKey(), ids, skinIds);
                }
            }
        });

        FlagsManager.stopFlags();
        VelocitySyncManager.shutdown();

        // Wait for DB saves to complete before cleaning up
        try {
            saveFuture.join();
        } catch (final Exception e) {
            getLog().log(Level.SEVERE, "Error saving data on disable", e);
        }

        Pet.clearPets();
        Databases.closeConnection();

        Bukkit.getGlobalRegionScheduler().cancelTasks(this);
        Bukkit.getAsyncScheduler().cancelTasks(this);
    }

    /**
     * Check and initialize LuckPerms instance
     */
    private static void checkLuckPerms() {
        try {
            final RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
            if (provider != null) {
                luckPerms = provider.getProvider();
            }
        } catch (final NoClassDefFoundError error) {
            if (!luckPermsNotFound) {
                luckPermsNotFound = true;
                getLog().warning("LuckPerms could not be found. Some features relating to giving permissions won't be available.");
            }
        }
    }

    public static boolean checkNexo() {
        if (nexoFound) return true;
        if (nexoChecked) return false;

        try {
            Class.forName("com.nexomc.nexo.api.NexoItems");
            nexoFound = true;
            if (!nexoChecked) {
                getLog().info("Nexo found. Nexo Custom items features are available.");
            }
        } catch (final ClassNotFoundException e) {
            nexoFound = false;
        } catch (final Exception e) {
            // Handle cases like zip file closed during plugin reload
            nexoFound = false;
            if (!nexoChecked) {
                getLog().warning("Could not check for Nexo (" + e.getClass().getSimpleName() + "). Nexo Custom items features won't be available.");
            }
        } finally {
            nexoChecked = true;
        }

        return nexoFound;
    }

    /**
     * Check and initialize WorldGuard instance
     */
    private static void checkWorldGuard() {
        try {
            final WorldGuard wg = WorldGuard.getInstance();
            if (wg != null)
                GlobalConfig.getInstance().setWorldGuardSupport(true);
        } catch (final NoClassDefFoundError error) {
            GlobalConfig.getInstance().setWorldGuardSupport(false);
            getLog().warning("WorldGuard could not be found. Flags won't be available.");
        }
    }

    /**
     * Check and initialize MythicMobs instance
     */
    private static boolean checkMythicMobs() {
        if (mythicMobs != null) return true;

        try {
            final MythicBukkit inst = MythicBukkit.inst();
            if (inst != null) {
                mythicMobs = inst;
                return true;
            }
        } catch (final NoClassDefFoundError error) {
            getLog().warning("MythicMobs could not be found.");
        }

        return false;
    }

    /**
     * Check and initialize the modeler (BetterModel or ModelEngine)
     */
    private static boolean checkModeler() {
        if (modeler != null) return true;

        // Try BetterModel first
        try {
            Class.forName("kr.toxicity.model.api.BetterModel");
            modeler = new BetterModelModeler();
            getLog().info("BetterModel found. Using BetterModel as modeler.");
            return true;
        } catch (final ClassNotFoundException ignored) {}

        // Fallback to ModelEngine
        try {
            Class.forName("com.ticxo.modelengine.api.ModelEngineAPI");
            modeler = new ModelEngineModeler();
            getLog().info("ModelEngine found. Using ModelEngine as modeler.");
            return true;
        } catch (final ClassNotFoundException ignored) {}

        return false;
    }

    private static boolean checkPlaceholderApi() {
        if (placeholderAPI != null) {
            return true;
        }

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            placeholderAPI = new PlaceholderAPICompat();
            placeholderAPI.register();
            return true;
        }

        return false;
    }

    /**
     * Return MythicMobs instance
     */
    public static MythicBukkit getMythicMobs() {
        if (mythicMobs == null) checkMythicMobs();

        return mythicMobs;
    }

    /**
     * Return LuckPerms instance
     */
    public static LuckPerms getLuckPerms() {
        if (luckPerms == null) checkLuckPerms();

        return luckPerms;
    }

    public static Logger getLog() {
        final MCPets plugin = getInstance();
        if (plugin != null) return plugin.getLogger();
        return Bukkit.getLogger();
    }

}
