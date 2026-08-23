package fr.nocsy.mcpets.commands;

import java.util.List;

import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.description.Description;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.flag.Flag;
import dev.rollczi.litecommands.annotations.join.Join;
import dev.rollczi.litecommands.annotations.optional.OptionalArg;
import dev.rollczi.litecommands.annotations.permission.Permission;

import fr.nocsy.mcpets.MCPets;
import fr.nocsy.mcpets.PPermission;
import fr.nocsy.mcpets.data.Category;
import fr.nocsy.mcpets.data.Items;
import fr.nocsy.mcpets.data.Pet;
import fr.nocsy.mcpets.data.config.FormatArg;
import fr.nocsy.mcpets.data.config.GlobalConfig;
import fr.nocsy.mcpets.data.config.ItemsListConfig;
import fr.nocsy.mcpets.data.config.Language;
import fr.nocsy.mcpets.data.inventories.PetActionMenu;
import fr.nocsy.mcpets.data.inventories.PetInventory;
import fr.nocsy.mcpets.data.inventories.PetMenu;
import fr.nocsy.mcpets.data.livingpets.PetFood;
import fr.nocsy.mcpets.data.livingpets.PetStats;
import fr.nocsy.mcpets.data.sql.PlayerData;
import fr.nocsy.mcpets.listeners.PetInteractionMenuListener;
import fr.nocsy.mcpets.utils.Utils;
import fr.nocsy.mcpets.utils.debug.Debugger;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@Command(name = "pet", aliases = {"pets", "bicho"})
public class PetCommand {

    @Execute
    @Permission(PPermission.USE_NODE)
    @Description("Abre o menu dos seus pets.")
    public void menu(final @Context Player player) {
        new PetMenu(player, 0).open(player);
    }

    @Execute(name = "menu")
    @Permission(PPermission.MENU_NODE)
    @Description("Abre as ações do seu pet ativo.")
    public void actions(final @Context Player player, final @OptionalArg("pet") Pet pet) {
        final Pet active = resolveActive(player, pet, false);
        if (active == null) {
            return;
        }

        PetActionMenu.open(player, active, false);
    }

    @Execute(name = "categoria")
    @Permission(PPermission.CATEGORY_NODE)
    @Description("Abre uma categoria de pets.")
    public void category(final @Context CommandSender sender,
                         final @Arg("categoria") Category category,
                         final @OptionalArg("jogador") Player target) {
        // Opening someone else's menu is the staff form of the same command.
        if (target != null && !sender.hasPermission(PPermission.ADMIN_OTHERS.getPermission())) {
            Language.NO_PERM.sendMessage(sender);
            return;
        }

        final Player viewer = target != null ? target : asPlayer(sender);
        if (viewer == null) {
            return;
        }

        category.openInventory(viewer, 0);
    }

    @Execute(name = "nome")
    @Permission(PPermission.NAME_NODE)
    @Description("Pede no chat um novo nome para o seu pet ativo.")
    public void renamePrompt(final @Context Player player) {
        final Pet pet = renameable(player);
        if (pet == null) {
            return;
        }

        pet.setLastInteractedWith(player);
        PetInteractionMenuListener.changeName(player);
    }

    @Execute(name = "nome")
    @Permission(PPermission.NAME_NODE)
    @Description("Renomeia o seu pet ativo.")
    public void rename(final @Context Player player, final @Join("nome") String name) {
        final Pet pet = renameable(player);
        if (pet == null) {
            return;
        }

        final String cleaned = name.replace("'", "").replace(";;", ";").replace(";;;", ";");
        final String blacklisted = Utils.isInBlackList(cleaned);
        if (blacklisted != null) {
            Language.BLACKLISTED_WORD.sendMessageFormatted(player, new FormatArg("%word%", blacklisted));
            return;
        }

        pet.setDisplayName(cleaned, true, !player.hasPermission(PPermission.COLOR.getPermission()));
        Language.NICKNAME_CHANGED_SUCCESSFULY.sendMessage(player);
    }

    /** @return the pet to rename, or null once the reason it cannot be renamed has been sent. */
    private static Pet renameable(final Player player) {
        if (!GlobalConfig.getInstance().isNameable()) {
            Language.NO_PERM.sendMessage(player);
            return null;
        }

        return resolveActive(player, null, false);
    }

    @Execute(name = "montar")
    @Permission(PPermission.RIDE_NODE)
    @Description("Monta no seu pet ativo.")
    public void mount(final @Context Player player) {
        final Pet pet = resolveActive(player, null, false);
        if (pet == null) {
            return;
        }

        PetInteractionMenuListener.mount(player, pet);
    }

    @Execute(name = "guardar")
    @Permission(PPermission.DISMISS_NODE)
    @Description("Guarda o seu pet ativo.")
    public void dismiss(final @Context CommandSender sender, final @OptionalArg("jogador") Player target) {
        if (target != null && !sender.hasPermission(PPermission.ADMIN_OTHERS.getPermission())) {
            Language.NO_PERM.sendMessage(sender);
            return;
        }

        final Player owner = target != null ? target : asPlayer(sender);
        if (owner == null) {
            return;
        }

        final Pet pet = Pet.fromOwner(owner.getUniqueId());
        if (pet == null) {
            Language.NO_ACTIVE_PET.sendMessage(sender);
            return;
        }

        PetInteractionMenuListener.revoke(owner, pet);
    }

    @Execute(name = "reload")
    @Permission(PPermission.ADMIN_RELOAD_NODE)
    @Description("Recarrega as configurações dos pets.")
    public void reload(final @Context CommandSender sender) {
        PlayerData.saveDB();
        MCPets.loadConfigs();
        Language.RELOAD_SUCCESS.sendMessage(sender);
        Language.HOW_MANY_PETS_LOADED.sendMessageFormatted(sender,
                new FormatArg("%numberofpets%", Integer.toString(Pet.getObjectPets().size())));
    }

    @Execute(name = "debug")
    @Permission(PPermission.ADMIN_DEBUG_NODE)
    @Description("Liga ou desliga as mensagens de depuração.")
    public void debug(final @Context Player player) {
        if (Debugger.isListening(player.getUniqueId())) {
            Debugger.leave(player.getUniqueId());
            Language.DEBUGGER_LEAVE.sendMessage(player);
            return;
        }

        Debugger.join(player.getUniqueId());
        Language.DEBUGGER_JOINING.sendMessage(player);
    }

    @Execute(name = "inspect")
    @Permission(PPermission.ADMIN_INSPECT_NODE)
    @Description("Abre os pets de um jogador, ou o inventário de um deles.")
    public void inspect(final @Context Player staff,
                        final @Arg("jogador") OfflinePlayer target,
                        final @OptionalArg("pet") Pet pet) {
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            Language.PLAYER_OR_PET_DOESNT_EXIST.sendMessage(staff);
            return;
        }

        if (pet == null) {
            final Player online = target.getPlayer();
            if (online == null) {
                Language.PLAYER_NOT_CONNECTED.sendMessageFormatted(staff,
                        new FormatArg("%player%", String.valueOf(target.getName())));
                return;
            }

            new PetMenu(online, 0).open(staff);
            return;
        }

        final PetInventory inventory = PetInventory.get(target.getUniqueId(), pet.getId());
        if (inventory == null) {
            Language.PET_INVENTORY_COULDNOT_OPEN.sendMessage(staff);
            return;
        }

        inventory.open(staff);
    }

    @Execute(name = "spawn")
    @Permission(PPermission.ADMIN_SPAWN_NODE)
    @Description("Invoca um pet para um jogador.")
    public void spawn(final @Context CommandSender sender,
                      final @Arg("jogador") Player target,
                      final @Arg("pet") Pet pet,
                      final @OptionalArg("checarPermissao") Boolean checkPermission,
                      final @Flag("-s") boolean silent) {
        final boolean check = checkPermission == null || checkPermission;
        if (check && !target.hasPermission(pet.getPermission())) {
            Language.NOT_ALLOWED.sendMessage(sender);
            return;
        }

        final Pet copy = pet.copy();
        copy.setCheckPermission(check);
        if (silent) {
            copy.spawn(target, target.getLocation());
            return;
        }

        copy.spawnWithMessage(target);
    }

    @Execute(name = "reset")
    @Permission(PPermission.ADMIN_RESET_NODE)
    @Description("Apaga o progresso dos pets de um jogador.")
    public void reset(final @Context CommandSender sender,
                      final @Arg("jogador") OfflinePlayer target,
                      final @OptionalArg("pet") Pet pet) {
        final PlayerData data = PlayerData.get(target.getUniqueId());

        if (pet == null) {
            PetStats.remove(target.getUniqueId());
            Language.STATS_CLEARED.sendMessage(sender);
            data.save();
            return;
        }

        PetStats.remove(pet.getId(), target.getUniqueId());
        Language.STATS_CLEARED_FOR_PET_FOR_PLAYER.sendMessageFormatted(sender,
                new FormatArg("%petId%", pet.getId()),
                new FormatArg("%player%", String.valueOf(target.getName())));
        data.save();
    }

    @Execute(name = "give")
    @Permission(PPermission.ADMIN_GIVE_NODE)
    @Description("Dá comida de pet a um jogador.")
    public void give(final @Context CommandSender sender,
                     final @Arg("jogador") Player target,
                     final @Arg("comida") PetFood food,
                     final @OptionalArg("quantidade") Integer amount) {
        int left = amount == null ? 1 : amount;
        if (left < 1) {
            Language.INVALID_AMOUNT.sendMessage(sender);
            return;
        }

        final ItemStack item = food.getItemStack();
        while (left > 0) {
            final ItemStack stack = item.clone();
            stack.setAmount(Math.min(left, stack.getMaxStackSize()));
            target.getInventory().addItem(stack);
            left -= stack.getAmount();
        }
    }

    @Execute(name = "stick set")
    @Permission(PPermission.ADMIN_STICK_NODE)
    @Description("Transforma o item na sua mão em bastão de sinal.")
    public void stickSet(final @Context Player staff, final @Arg("pet") Pet pet) {
        final ItemStack held = staff.getInventory().getItemInMainHand();
        if (held.getType().isAir()) {
            Language.REQUIRES_ITEM_IN_HAND.sendMessage(staff);
            return;
        }

        staff.getInventory().setItemInMainHand(Items.turnIntoSignalStick(held, pet));
    }

    @Execute(name = "stick give")
    @Permission(PPermission.ADMIN_STICK_NODE)
    @Description("Dá o bastão de sinal de um pet a um jogador.")
    public void stickGive(final @Arg("jogador") Player target, final @Arg("pet") Pet pet) {
        target.getInventory().addItem(pet.getSignalStick());
    }

    @Execute(name = "item list")
    @Permission(PPermission.ADMIN_ITEM_NODE)
    @Description("Lista as chaves de itens configuradas.")
    public void itemList(final @Context CommandSender sender) {
        Language.KEY_LIST.sendMessage(sender);
        for (final String key : ItemsListConfig.getInstance().listKeys()) {
            sender.sendMessage(Utils.toComponent("§8- §7" + key));
        }
    }

    @Execute(name = "item add")
    @Permission(PPermission.ADMIN_ITEM_NODE)
    @Description("Cria uma chave de item com o item na sua mão.")
    public void itemAdd(final @Context Player staff, final @Arg("chave") String key) {
        if (ItemsListConfig.getInstance().getItemStack(key) != null) {
            Language.KEY_ALREADY_EXISTS.sendMessage(staff);
            return;
        }

        final ItemStack held = staff.getInventory().getItemInMainHand();
        if (held.getType().isAir()) {
            Language.REQUIRES_ITEM_IN_HAND.sendMessage(staff);
            return;
        }

        ItemsListConfig.getInstance().setItemStack(key, held);
        Language.KEY_ADDED.sendMessage(staff);
    }

    @Execute(name = "item set")
    @Permission(PPermission.ADMIN_ITEM_NODE)
    @Description("Substitui uma chave de item pelo item na sua mão.")
    public void itemSet(final @Context Player staff, final @Arg("chave") String key) {
        if (ItemsListConfig.getInstance().getItemStack(key) == null) {
            Language.ITEM_DOESNT_EXIST.sendMessageFormatted(staff, new FormatArg("%key%", key));
            return;
        }

        final ItemStack held = staff.getInventory().getItemInMainHand();
        if (held.getType().isAir()) {
            Language.REQUIRES_ITEM_IN_HAND.sendMessage(staff);
            return;
        }

        ItemsListConfig.getInstance().setItemStack(key, held);
        Language.ITEM_UPDATED.sendMessageFormatted(staff, new FormatArg("%key%", key));
    }

    @Execute(name = "item remove")
    @Permission(PPermission.ADMIN_ITEM_NODE)
    @Description("Apaga uma chave de item.")
    public void itemRemove(final @Context CommandSender sender, final @Arg("chave") String key) {
        if (ItemsListConfig.getInstance().getItemStack(key) == null) {
            Language.KEY_DOESNT_EXIST.sendMessage(sender);
            return;
        }

        ItemsListConfig.getInstance().removeItemStack(key);
        Language.KEY_REMOVED.sendMessage(sender);
    }

    @Execute(name = "item give")
    @Permission(PPermission.ADMIN_ITEM_NODE)
    @Description("Dá a você o item de uma chave configurada.")
    public void itemGive(final @Context Player staff, final @Arg("chave") String key) {
        final ItemStack item = ItemsListConfig.getInstance().getItemStack(key);
        if (item == null) {
            Language.KEY_DOESNT_EXIST.sendMessage(staff);
            return;
        }

        staff.getInventory().addItem(item);
    }

    /**
     * Resolves which of the player's active pets a command is about, answering with the usual
     * message when there is none or when the player has to say which one.
     */
    static Pet resolveActive(final Player player, final Pet requested, final boolean mounts) {
        final List<Pet> active = Pet.getActivePetsForOwner(player.getUniqueId())
                .stream()
                .filter(pet -> pet.isMountable() == mounts)
                .toList();

        if (active.isEmpty()) {
            Language.NO_ACTIVE_PET.sendMessage(player);
            return null;
        }

        if (requested != null) {
            final Pet match = active.stream()
                    .filter(pet -> pet.getId().equalsIgnoreCase(requested.getId()))
                    .findFirst()
                    .orElse(null);
            if (match == null) {
                Language.NO_ACTIVE_PET.sendMessage(player);
            }
            return match;
        }

        if (active.size() == 1) {
            return active.getFirst();
        }

        Language.SPECIFY_PET.sendMessageFormatted(player,
                new FormatArg("%pets%", active.stream().map(Pet::getId).reduce((a, b) -> a + ", " + b).orElse("")));
        return null;
    }

    private static Player asPlayer(final CommandSender sender) {
        if (sender instanceof Player player) {
            return player;
        }

        Language.NO_PERM.sendMessage(sender);
        return null;
    }
}
