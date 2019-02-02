package at.lorenz.wow.manager;

import at.lorenz.wow.WoWPlugin;
import at.lorenz.wow.utils.InventoryMenu;
import at.lorenz.wow.utils.ItemBuilder;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class MenuManager implements Listener {

    private final WoWPlugin plugin;

    private final List<InventoryMenu> menus = new ArrayList<>();

    public MenuManager(WoWPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public InventoryMenu create(Player player, int size) {
        InventoryMenu menu = new InventoryMenu(plugin, player, size);
        menus.add(menu);
        return menu;
    }

    @EventHandler
    void onInventoryClick(InventoryClickEvent event) {
        for (InventoryMenu menu : new ArrayList<>(menus)) {
            menu.checkClick(event);
        }
    }

    @EventHandler
    void onInventoryClose(InventoryCloseEvent event) {
        for (InventoryMenu menu : new ArrayList<>(menus)) {
            if (menu.checkClose(event)) {
                menus.remove(menu);
                return;
            }
        }
    }

    public void confirmAction(Player player, String question, String yes, String no, BooleanConsumer answer) {
        InventoryMenu menu = create(player, 1);
        menu.addItem(3, ItemBuilder.material(Material.GREEN_WOOL).name("§a" + yes).build(), () -> {
            answer.accept(true);
        });
        menu.addItem(5, ItemBuilder.material(Material.RED_WOOL).name("§c" + no).build(), () -> {
            answer.accept(false);
        });
        menu.open("§e" + question);
    }
}
