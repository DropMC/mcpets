package fr.nocsy.mcpets;

import gg.dropmc.survival.core.Brand;

import lombok.Getter;

/**
 * The permission nodes, in the house format. Annotations need them as compile-time constants, so
 * the nodes are declared as constants and the enum values only wrap them.
 *
 * <p>The node that grants ownership of one pet is the {@code Permission:} key of that pet's own
 * yml; only the prefix used to default it lives here.</p>
 */
public enum PPermission {

    USE(PPermission.USE_NODE),
    MOUNT(PPermission.MOUNT_NODE),
    COLOR(PPermission.COLOR_NODE),
    ADMIN_OTHERS(PPermission.ADMIN_OTHERS_NODE);

    public static final String USE_NODE = Brand.PERMISSION_PREFIX + ".commands.pet";
    public static final String MOUNT_NODE = Brand.PERMISSION_PREFIX + ".commands.montaria";

    // One node per player subcommand, so a rank can be given the menu without the rest, or the
    // other way round. The root node does not imply any of them.
    public static final String MENU_NODE = Brand.PERMISSION_PREFIX + ".commands.pet.menu";
    public static final String CATEGORY_NODE = Brand.PERMISSION_PREFIX + ".commands.pet.categoria";
    public static final String NAME_NODE = Brand.PERMISSION_PREFIX + ".commands.pet.nome";
    public static final String RIDE_NODE = Brand.PERMISSION_PREFIX + ".commands.pet.montar";
    public static final String DISMISS_NODE = Brand.PERMISSION_PREFIX + ".commands.pet.guardar";
    public static final String COLOR_NODE = Brand.PERMISSION_PREFIX + ".pet.color";

    /** Prefix of the node that grants ownership of one pet: this plus the pet's id. */
    public static final String PET_NODE_PREFIX = Brand.PERMISSION_PREFIX + ".pet.";

    // One node per staff verb, so the generated help lists only what the reader can actually run.
    public static final String ADMIN_RELOAD_NODE = Brand.PERMISSION_PREFIX + ".commands.pet.admin.reload";
    public static final String ADMIN_DEBUG_NODE = Brand.PERMISSION_PREFIX + ".commands.pet.admin.debug";
    public static final String ADMIN_INSPECT_NODE = Brand.PERMISSION_PREFIX + ".commands.pet.admin.inspect";
    public static final String ADMIN_SPAWN_NODE = Brand.PERMISSION_PREFIX + ".commands.pet.admin.spawn";
    public static final String ADMIN_RESET_NODE = Brand.PERMISSION_PREFIX + ".commands.pet.admin.reset";
    public static final String ADMIN_GIVE_NODE = Brand.PERMISSION_PREFIX + ".commands.pet.admin.give";
    public static final String ADMIN_STICK_NODE = Brand.PERMISSION_PREFIX + ".commands.pet.admin.stick";
    public static final String ADMIN_ITEM_NODE = Brand.PERMISSION_PREFIX + ".commands.pet.admin.item";

    /** Acting on someone else's pet, whether through a command or by clicking it in the world. */
    public static final String ADMIN_OTHERS_NODE = Brand.PERMISSION_PREFIX + ".commands.pet.admin.others";

    @Getter
    private final String permission;

    PPermission(String permission) {
        this.permission = permission;
    }
}
