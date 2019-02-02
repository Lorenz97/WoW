package at.lorenz.wow;

import at.lorenz.wow.utils.ItemBuilder;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

@Getter
public enum WoWClass {
    TEST("test", ItemBuilder.material(Material.WHITE_WOOL).name("Test").build());

    private final String name;
    private final ItemStack itemStack;

    WoWClass(String name, ItemStack itemStack) {
        this.name = name;
        this.itemStack = itemStack;
    }
}
