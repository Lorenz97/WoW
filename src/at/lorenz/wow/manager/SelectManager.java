package at.lorenz.wow.manager;

import at.lorenz.wow.*;
import at.lorenz.wow.utils.InventoryMenu;
import at.lorenz.wow.utils.Utils;
import at.lorenz.wow.utils.scoreboard.SidebarBuilder;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class SelectManager implements Listener {

    private final WoWPlugin plugin;

    public SelectManager(WoWPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);

        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (plugin.getUserManager().getUser(player).getUserState() == UserState.SELECT) {
                    SidebarBuilder builder = new SidebarBuilder();
                    builder.setTitle("Willkommen " + player.getName() + "!");
                    builder.add("");
                    int size = Bukkit.getOnlinePlayers().size();
                    builder.add("Es sind " + size + " Spieler online");
                    builder.add("                                  ");
                    builder.add(Utils.getCurrentTime());
                    builder.send(player);
                }
            }
        }, 2, 2);
    }

    @EventHandler
    void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        WowUser user = plugin.getUserManager().getUser(player);
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Material material = player.getItemInHand().getType();
        if (user.getUserState() == UserState.SELECT) {
            if (material == WowItems.SELECT_COMPASS.getType()) {
                openSelectInventory(player);
                event.setCancelled(true);
            }
        }
    }

    public void openSelectInventory(Player player) {
        InventoryMenu menu = plugin.getMenuManager().create(player, 3);
        int index = 0;
        for (WoWClass clazz: WoWClass.values()) {
            menu.addItem(index, clazz.getItemStack(), () -> {
                plugin.getInGameMamager().join(player, clazz);
                player.closeInventory();
            });
            index++;
        }
        if (player.isOp()) {
            menu.addItem(index, WowItems.CREATIVE, () -> {
                plugin.getCreativeManager().enterCreative(player);
                plugin.getLocationManager().teleportToSpawn(player);
                player.closeInventory();
            });
        }
        menu.open("Select class");
    }

    public void putIntoSelect(Player player) {
        player.setGameMode(GameMode.ADVENTURE);
        plugin.getLocationManager().teleportToSelect(player);
        player.sendMessage("Willkommen!");

        Utils.clear(player);

        WowUser user = plugin.getUserManager().getUser(player);

        player.getInventory().addItem(WowItems.SELECT_COMPASS);
        user.setUserState(UserState.SELECT);
        player.getInventory().setHeldItemSlot(0);
    }
}
