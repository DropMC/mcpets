package fr.nocsy.mcpets.gui;

import java.util.ArrayList;
import java.util.List;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import gg.dropmc.survival.core.api.adventure.AdventureUtils;
import gg.dropmc.survival.core.api.gui.AbstractMenu;
import gg.dropmc.survival.core.api.gui.GuiItems;
import gg.dropmc.survival.core.api.gui.Menu;
import gg.dropmc.survival.core.api.gui.MenuBuilder;
import gg.dropmc.survival.core.api.nexo.NexoGlyph;

import fr.nocsy.mcpets.data.Pet;
import fr.nocsy.mcpets.data.config.FormatArg;
import fr.nocsy.mcpets.data.config.GlobalConfig;
import fr.nocsy.mcpets.data.config.Language;
import fr.nocsy.mcpets.listeners.PetInteractionMenuListener;

/**
 * What you can do with the pet you have out: two rows, and the pet itself as the only button. Left
 * click stores it, right click asks for a new name.
 *
 * <p>This replaces the two nine-slot inventories the fork opened for the same pet - one from the
 * command, one from clicking the pet - which differed only in which of the same buttons they drew.
 * The actions that used to have their own button are commands of their own now, and the signal stick
 * button is gone entirely: any stick in hand commands the pet while it is out.</p>
 */
public class PetActionMenu extends AbstractMenu {

    private static final int BUTTON_SLOT = 13;

    private final Pet pet;

    /**
     * Opens the actions for {@code pet}, unless it is still being tamed. A half-tamed pet does not
     * answer to its owner yet, so there is nothing here for it to do.
     *
     * @param player the player to open the menu for
     * @param pet    the pet the menu acts on
     */
    public static void openFor(final @NotNull Player player, final @NotNull Pet pet) {
        if (pet.getTamingProgress() < 1) return;

        new PetActionMenu(player, pet).open();
    }

    public PetActionMenu(final @NotNull Player player, final @NotNull Pet pet) {
        super(player);
        this.pet = pet;
    }

    @Override
    protected int rows() {
        return 2;
    }

    @Override
    protected @NotNull List<Component> info() {
        return List.of(
                AdventureUtils.parse("<!i><gray>Ações do pet que você tem ativo."),
                AdventureUtils.parse("<!i><gray>Com ele fora, segure um <white>graveto<gray> para"),
                AdventureUtils.parse("<!i><gray>dar ordens: <white>direito<gray> usa a habilidade,"),
                AdventureUtils.parse("<!i><gray>e <white>esquerdo<gray> troca a ordem ativa."));
    }

    @Override
    protected void decorate(final @NotNull MenuBuilder builder) {
        builder.background(NexoGlyph.PETS_ACTION_MENU);
    }

    @Override
    protected void content(final @NotNull Menu menu) {
        final ItemStack icon = pet.getIcon();

        menu.setItem(BUTTON_SLOT, GuiItems.of(icon)
                .name(PetIcons.name(icon, pet.getRarity()))
                .lore(lore())
                .hideAttributes()
                .onClick((clicker, event) -> {
                    if (event.isRightClick()) {
                        rename(clicker);
                        return;
                    }

                    clicker.closeInventory();
                    PetInteractionMenuListener.revoke(clicker, pet);
                }));
    }

    private void rename(final Player clicker) {
        if (!GlobalConfig.getInstance().isNameable()) return;

        // The chat prompt reads the pet back from whoever the player last interacted with, so the
        // click has to register as that interaction before the prompt goes out.
        pet.setLastInteractedWith(clicker);
        clicker.closeInventory();
        PetInteractionMenuListener.changeName(clicker);
    }

    /**
     * The full sheet for the pet: its tier, its ability, the nickname it answers to, and the stat block
     * the icon carried before this menu existed - status, health, the modifiers and the experience bar,
     * straight from {@code language.yml} so it stays in step with what {@code /pet} shows.
     */
    private List<Component> lore() {
        final List<Component> lore = new ArrayList<>();

        if (pet.getRarity() != null) {
            lore.add(pet.getRarity().loreLine());
            lore.add(Component.empty());
        }

        final List<Component> description = PetIcons.description(pet.getIcon());
        if (!description.isEmpty()) {
            lore.addAll(description);
            lore.add(Component.empty());
        }

        if (pet.getCurrentName() != null) {
            lore.add(Language.NICKNAME.getComponentFormatted(new FormatArg("%nickname%", pet.getCurrentName()))
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.empty());
        }

        final List<Component> stats = pet.statsLore();
        if (!stats.isEmpty()) {
            // The numbers read as a continuation of what is above them, so whatever section came last
            // gives up its trailing blank line rather than opening a gap in the middle of the tooltip.
            if (!lore.isEmpty() && lore.getLast().equals(Component.empty())) lore.removeLast();

            stats.stream().map(line -> line.decoration(TextDecoration.ITALIC, false)).forEach(lore::add);
            lore.add(Component.empty());
        }

        lore.add(PetIcons.action(NexoGlyph.MOUSE_LEFT, "Guardar"));
        if (GlobalConfig.getInstance().isNameable()) {
            lore.add(PetIcons.action(NexoGlyph.MOUSE_RIGHT, "Renomear"));
        }

        return lore;
    }
}
