package fr.nocsy.mcpets.gui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import dev.triumphteam.gui.guis.GuiItem;

import gg.dropmc.survival.core.api.adventure.AdventureUtils;
import gg.dropmc.survival.core.api.gui.GuiItems;
import gg.dropmc.survival.core.api.gui.MenuBuilder;
import gg.dropmc.survival.core.api.gui.PageLayout;
import gg.dropmc.survival.core.api.gui.PaginatedMenu;
import gg.dropmc.survival.core.api.gui.PaginationStyle;
import gg.dropmc.survival.core.api.item.Rarity;
import gg.dropmc.survival.core.api.nexo.NexoGlyph;

import fr.nocsy.mcpets.data.Pet;
import fr.nocsy.mcpets.data.livingpets.PetLevel;
import fr.nocsy.mcpets.data.livingpets.PetStats;
import fr.nocsy.mcpets.data.sql.PlayerData;

/**
 * The {@code /pet} menu: every configured pet on one paginated grid, with the ones the owner may
 * summon listed first. There are no categories - a pet the owner does not have the node for is still
 * drawn, greyed out only by its lore, so the catalogue doubles as the list of what is still missing.
 *
 * <p>The viewer and the owner are separate because {@code /pet inspect} opens another player's
 * catalogue for a staff member: ownership and stats are read from {@code owner}, while the menu is
 * opened for {@code player}. Summoning is only wired up when the two are the same.</p>
 */
public class PetsMenu extends PaginatedMenu<PetsMenu.Entry> {

    /** Played when a click actually puts a pet in the world. Storing one answers it a fifth lower, from PetInteractionMenuListener#revoke. */
    private static final Sound SUMMON_SOUND = Sound.ENTITY_ENDERMAN_TELEPORT;

    private static final PageLayout LAYOUT = PageLayout.of(6, "9-44")
            .previous("45,46")
            .next("52,53");

    private final Player owner;

    private List<Entry> entries;

    /** One grid slot: a pet and whether {@link #owner} may summon it. */
    public record Entry(Pet pet, boolean owned) {
    }

    public PetsMenu(final @NotNull Player player) {
        this(player, player, 1);
    }

    public PetsMenu(final @NotNull Player player, final @NotNull Player owner, final int page) {
        super(player, page, LAYOUT);
        this.owner = owner;
    }

    @Override
    protected void decorate(final @NotNull MenuBuilder builder) {
        this.entries = loadEntries();
        page = Math.max(1, Math.min(page, totalPages()));

        builder.paginationTitle(NexoGlyph.PETS_MENU, page, totalPages(), PaginationStyle.ROWS_5);
    }

    @Override
    protected @NotNull List<Component> info() {
        if (!player.equals(owner)) {
            return List.of(
                    AdventureUtils.parse("<!i><gray>Catálogo de pets de <white>" + owner.getName() + "<gray>."),
                    AdventureUtils.parse("<!i><gray>Os pets liberados aparecem primeiro."));
        }

        return List.of(
                AdventureUtils.parse("<!i><gray>Clique num pet para invocá-lo."),
                AdventureUtils.parse("<!i><gray>Os pets que você já tem aparecem primeiro."),
                AdventureUtils.parse("<!i><gray>Use <white>/pet menu<gray> para as ações do pet ativo."));
    }

    @Override
    protected long totalCount() {
        return entries.size();
    }

    @Override
    protected @NotNull List<Entry> pageItems() {
        final int from = (int) offset();
        if (from >= entries.size()) return List.of();

        return entries.subList(from, Math.min(from + pageSize(), entries.size()));
    }

    @Override
    protected @NotNull GuiItem renderItem(final @NotNull Entry entry) {
        final Pet pet = entry.pet();
        final ItemStack icon = pet.getIcon();

        final GuiItems.Builder builder = GuiItems.of(icon)
                .name(name(icon, pet.getRarity()))
                .lore(lore(entry))
                .hideAttributes();

        if (!entry.owned() || !player.equals(owner)) {
            return builder.asGuiItem();
        }

        return builder.onClick(clicker -> {
            clicker.closeInventory();

            final Pet summoned = pet.copy();
            summoned.spawnWithMessage(clicker);
            // Only cheer when the pet actually made it out: spawnWithMessage swallows a cooldown, a
            // blacklisted world or the active pet cap into a message and leaves nothing summoned.
            if (summoned.isStillHere()) {
                clicker.playSound(clicker.getLocation(), SUMMON_SOUND, 0.7F, 1.2F);
            }
        });
    }

    /**
     * Every configured pet, ordered the way the menu reads: the ones the owner may summon first, then
     * rarest first, then alphabetically. The owned ones come from {@link Pet#getAvailablePets(Player)},
     * which already hands back copies bound to the owner with their stats loaded; the rest stay the
     * shared config instances and are only read from.
     */
    private List<Entry> loadEntries() {
        PlayerData.get(owner.getUniqueId());

        final Map<String, Pet> available = new HashMap<>();
        for (final Pet pet : Pet.getAvailablePets(owner)) {
            available.put(pet.getId(), pet);
        }

        final List<Entry> loaded = new ArrayList<>();
        for (final Pet pet : Pet.getObjectPets()) {
            final Pet owned = available.get(pet.getId());
            loaded.add(owned == null ? new Entry(pet, false) : new Entry(owned, true));
        }

        loaded.sort(Comparator.comparing((Entry entry) -> !entry.owned())
                .thenComparing(Comparator.comparingInt(PetsMenu::rarityRank).reversed())
                .thenComparing(PetsMenu::plainName, String.CASE_INSENSITIVE_ORDER));
        return loaded;
    }

    /** Sort key for the rarity column: unranked pets sort below every ranked one. */
    private static int rarityRank(final Entry entry) {
        final Rarity rarity = entry.pet().getRarity();
        return rarity == null ? -1 : rarity.ordinal();
    }

    private static String plainName(final Entry entry) {
        final Component name = displayName(entry.pet().getIcon());
        return name == null ? entry.pet().getId() : PlainTextComponentSerializer.plainText().serialize(name);
    }

    /**
     * The icon's configured name. A pet that picks its own color keeps it - the dragons are colored by
     * element, and painting all of them in the tier color would trade that identity for information the
     * badge line already carries. Only an uncolored name falls back to the rarity color.
     */
    private static Component name(final ItemStack icon, final Rarity rarity) {
        Component name = displayName(icon);
        if (name == null) name = Component.text("???");
        if (rarity != null && name.color() == null) name = name.color(rarity.color());

        return name.decoration(TextDecoration.ITALIC, false);
    }

    /**
     * The pet's tooltip: the rarity badge, the ability text configured on the icon, the owner's
     * progress on it, and the action hint - or, for a pet the owner has no node for, the line saying
     * so in place of that hint.
     */
    private List<Component> lore(final Entry entry) {
        final List<Component> lore = new ArrayList<>();

        final Rarity rarity = entry.pet().getRarity();
        if (rarity != null) {
            lore.add(rarity.loreLine());
            lore.add(Component.empty());
        }

        final List<Component> description = description(entry.pet().getIcon());
        if (!description.isEmpty()) {
            lore.addAll(description);
            lore.add(Component.empty());
        }

        if (entry.owned()) {
            lore.addAll(progress(entry.pet().getPetStats()));
        }

        if (!entry.owned()) {
            lore.add(NexoGlyph.EXCLAMATION_RED.appendSpace()
                    .append(Component.text("Você não possui esse pet.", NamedTextColor.RED))
                    .decoration(TextDecoration.ITALIC, false));
        } else if (player.equals(owner)) {
            // A staff member inspecting someone else's catalogue can look but not summon, so that
            // branch deliberately leaves the tooltip without an action hint.
            lore.add(NexoGlyph.MOUSE_LEFT.appendSpace()
                    .append(Component.text("|", NamedTextColor.GREEN))
                    .appendSpace()
                    .append(Component.text("Invocar", NamedTextColor.WHITE))
                    .decoration(TextDecoration.ITALIC, false));
        }

        return lore;
    }

    /** The level and experience block, empty when the pet has no stats recorded yet. */
    private static List<Component> progress(final PetStats stats) {
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

    private static Component label(final String label, final String value) {
        return Component.text(label, NamedTextColor.GRAY)
                .append(Component.text(value, NamedTextColor.WHITE))
                .decoration(TextDecoration.ITALIC, false);
    }

    private static Component displayName(final ItemStack icon) {
        final ItemMeta meta = icon.getItemMeta();
        return meta == null ? null : meta.displayName();
    }

    private static List<Component> description(final ItemStack icon) {
        final ItemMeta meta = icon.getItemMeta();
        if (meta == null || !meta.hasLore() || meta.lore() == null) return List.of();

        return meta.lore().stream()
                .map(line -> line.decoration(TextDecoration.ITALIC, false))
                .toList();
    }
}
