package at.lorenz.wow;

import at.lorenz.wow.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class WowItems {
    public static final ItemStack SELECT_COMPASS = ItemBuilder.material(Material.COMPASS).name("§6Select").build();

    public static final ItemStack CREATIVE_EXIT = ItemBuilder.material(Material.OAK_DOOR).name("Exit Creative").build();
    public static final ItemStack CREATIVE_REGION = ItemBuilder.material(Material.SAND).name("Regions").build();
    public static final ItemStack CREATIVE_BUILD = ItemBuilder.material(Material.IRON_SHOVEL).name("Build").build();
    public static final ItemStack CREATIVE_MOBS = ItemBuilder.material(Material.SHEEP_SPAWN_EGG).name("Mobs").build();
    public static final ItemStack CREATIVE_REGION_CREATE_NEW = ItemBuilder.material(Material.GRASS_BLOCK).name("Create new Region").build();
    public static final ItemStack CREATIVE = ItemBuilder.material(Material.BEDROCK).name("Creative").build();

    public static final ItemStack CREATIVE_REGION_SET_1 = ItemBuilder.material(Material.RED_WOOL).name("Pos 1").build();
    public static final ItemStack CREATIVE_REGION_SET_2 = ItemBuilder.material(Material.BLUE_WOOL).name("Pos 2").build();
    public static final ItemStack CREATIVE_REGION_NAME = ItemBuilder.material(Material.PAPER).name("Name of the Region").build();
    public static final ItemStack CREATIVE_REGION_CONFIRM_NEW = ItemBuilder.material(Material.GREEN_WOOL).name("Create region").build();
    public static final ItemStack CREATIVE_REGION_BOOK = ItemBuilder.material(Material.BOOK).name("Region Book").build();
    public static final ItemStack BACK = ItemBuilder.material(Material.OAK_DOOR).name("§cBack").build();
}
