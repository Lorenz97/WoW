package at.lorenz.wow.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;


public class ItemBuilder {
    private final ItemStack stack;

    public ItemBuilder(Material material) {
        this.stack = new ItemStack(material);
    }

    public static ItemBuilder owner(String owner) {
        return material(Material.PLAYER_HEAD).editMeta((meta) -> {
            SkullMeta skullMeta = (SkullMeta)meta;
            skullMeta.setOwner(owner);
        });
    }

    public static ItemBuilder material(Material material) {
        return new ItemBuilder(material);
    }

    public ItemBuilder amount(int amount) {
        this.stack.setAmount(amount);
        return this;
    }

    public ItemBuilder name(String displayName) {
        return this.editMeta((meta) -> {
            meta.setDisplayName(displayName);
        });
    }

    public ItemBuilder lore(String... lore) {
        return this.lore(Arrays.asList(lore));
    }

    public ItemBuilder lore(List<String> lore) {
        return this.editMeta((meta) -> {
            meta.setLore(lore);
        });
    }

    public ItemBuilder enchantment(Enchantment enchantment, int level) {
        return this.editMeta((meta) -> {
            meta.addEnchant(enchantment, level, true);
        });
    }

    public ItemBuilder enchantment(HashMap<Enchantment, Integer> enchants) {
        enchants.forEach(this::enchantment);
        return this;
    }

    public ItemBuilder flags(ItemFlag... flags) {
        return this.editMeta((meta) -> {
            meta.addItemFlags(flags);
        });
    }

    public ItemBuilder glowing() {
        return this.enchantment(Enchantment.DAMAGE_UNDEAD, 1).flags(ItemFlag.HIDE_ENCHANTS);
    }

    public ItemBuilder glowing(boolean glowing) {
        if (glowing) {
            this.glowing();
        }

        return this;
    }

    public ItemBuilder leatherArmorColor(Color color) {
        return this.editMeta((meta) -> {
            LeatherArmorMeta armorMeta = (LeatherArmorMeta)meta;
            armorMeta.setColor(color);
        });
    }

    public ItemBuilder editMeta(Consumer<ItemMeta> consumer) {
        ItemMeta meta = this.stack.getItemMeta();
        consumer.accept(meta);
        this.stack.setItemMeta(meta);
        return this;
    }

//    public String getName() {
//        return ItemEnum.getName(this.stack);
//    }

    public ItemStack build() {
        return this.stack;
    }
}
