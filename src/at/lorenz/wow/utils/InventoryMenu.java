package at.lorenz.wow.utils;

import at.lorenz.wow.WoWPlugin;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InventoryMenu {

    private final Player player;
    private final int size;
    private final WoWPlugin plugin;
    private Map<Integer, ItemStack> items = new HashMap<>();
    private Map<Integer, Consumer<InventoryClickEvent>> events = new HashMap<>();
    private boolean open = false;

    public InventoryMenu(WoWPlugin plugin, Player player, int size) {
        this.player = player;
        this.size = size;
        this.plugin = plugin;
    }

    public void addItem(int i, ItemStack build) {
        addItem(i, build, () -> {
        });
    }

    public void addItem(int index, ItemStack itemStack, Runnable runnable) {
        addItem(index, itemStack, event -> {
            if (event.getAction() == InventoryAction.PICKUP_ALL) {
                runnable.run();
            }
            event.setCancelled(true);
        });
    }

    public void addItem(int index, ItemStack itemStack, Consumer<InventoryClickEvent> consumer) {
        if (index >= size * 9) {
            throw new RuntimeException("menu item out of range: " + index + " (max " + size * 9 + ")");
        }
        items.put(index, itemStack);
        events.put(index, consumer);
    }

    public void open(String title) {
        if (open) {
            throw new RuntimeException("menu is already open");
        }
        open = true;
        Inventory inventory = Bukkit.createInventory(null, 9 * size, title);
        items.forEach(inventory::setItem);
        player.openInventory(inventory);
    }

    public void checkClick(InventoryClickEvent event) {
        if (event.getWhoClicked().getUniqueId().equals(player.getUniqueId())) {
            Inventory inventory = event.getClickedInventory();
            if (inventory != null && inventory.getType() == InventoryType.CHEST) {
                events.getOrDefault(event.getSlot(), e -> e.setCancelled(true)).accept(event);
            } else {
                event.setCancelled(true);
            }
        }
    }

    public boolean checkClose(InventoryCloseEvent event) {
        return event.getPlayer().getUniqueId().equals(player.getUniqueId());
    }
}
