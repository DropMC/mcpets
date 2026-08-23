package fr.nocsy.mcpets.gui;

import java.util.ArrayList;
import java.util.List;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import gg.dropmc.survival.core.api.item.Rarity;

import fr.nocsy.mcpets.data.livingpets.PetLevel;
import fr.nocsy.mcpets.data.livingpets.PetStats;

/**
 * The pieces of a pet tooltip that the catalogue and the action menu both draw: the configured icon
 * name, its ability text, the owner's progress on it, and the action hints at the bottom.
 */
final class PetIcons {

    private PetIcons() {}

    /**
     * The icon's configured name. A pet that picks its own color keeps it - the dragons are colored by
     * element, and painting all of them in the tier color would trade that identity for information the
     * badge line already carries. Only an uncolored name falls back to the rarity color.
     */
    static Component name(final ItemStack icon, final Rarity rarity) {
        Component name = displayName(icon);
        if (name == null) name = Component.text("???");
        if (rarity != null && name.color() == null) name = name.color(rarity.color());

        return name.decoration(TextDecoration.ITALIC, false);
    }

    static Component displayName(final ItemStack icon) {
        final ItemMeta meta = icon.getItemMeta();
        return meta == null ? null : meta.displayName();
    }

    /** The ability text the pet's config writes on its icon, de-italicized for the menu. */
    static List<Component> description(final ItemStack icon) {
        final ItemMeta meta = icon.getItemMeta();
        if (meta == null || !meta.hasLore() || meta.lore() == null) return List.of();

        return meta.lore().stream()
                .map(line -> line.decoration(TextDecoration.ITALIC, false))
                .toList();
    }

    /** The level and experience block, empty when the pet has no stats recorded yet. */
    static List<Component> progress(final PetStats stats) {
        if (stats == null || stats.getCurrentLevel() == null) return List.of();

        final List<Component> block = new ArrayList<>();
        block.add(label("Nível: ", stats.getCurrentLevel().getLevelName()));

        final PetLevel next = stats.getNextLevel();
        if (next == null || next.equals(stats.getCurrentLevel())) {
            block.add(label("Experiência: ", "máxima"));
        } else {
            block.add(label("Experiência: ",
                    (int) stats.getExperience() + "/" + (int) next.getExpThreshold()));
        }

        block.add(Component.empty());
        return block;
    }

    static Component label(final String label, final String value) {
        return Component.text(label, NamedTextColor.GRAY)
                .append(Component.text(value, NamedTextColor.WHITE))
                .decoration(TextDecoration.ITALIC, false);
    }

    /** One "click this way to do that" line: the mouse glyph, a green separator, and the verb. */
    static Component action(final Component mouse, final String verb) {
        return mouse.appendSpace()
                .append(Component.text("|", NamedTextColor.GREEN))
                .appendSpace()
                .append(Component.text(verb, NamedTextColor.WHITE))
                .decoration(TextDecoration.ITALIC, false);
    }
}
