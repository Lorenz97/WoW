package at.lorenz.wow.listener;

import at.lorenz.wow.WowUser;
import at.lorenz.wow.UserState;
import at.lorenz.wow.WoWPlugin;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;

public class GlobalListener implements Listener {

    private final WoWPlugin plugin;

    public GlobalListener(WoWPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOW)
    void onBlockBreak(BlockBreakEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW)
    void onBlockPlace(BlockPlaceEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    void onFoodLevelChange(FoodLevelChangeEvent event) {
        Player player = (Player) event.getEntity();
        WowUser user = plugin.getUserManager().getUser(player);
//        if (user.getUserState() == UserState.SELECT || user.getUserState() == UserState.CREATIVE) {
            event.setFoodLevel(20);
//        }
    }

    @EventHandler
    void onEntitySpawn(CreatureSpawnEvent event) {
        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.CUSTOM) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    void onEntitySpawn(EntityCombustEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (event.getEntity() instanceof Sheep) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    void onEntityDeath(EntityDeathEvent event) {
        event.setDroppedExp(0);
        event.getDrops().clear();
    }

    @EventHandler
    void onDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Player) {
            Player player = (Player) entity;
            WowUser user = plugin.getUserManager().getUser(player);
            if (user.getUserState() != UserState.INGAME) {
                event.setCancelled(true);
            }
        }
    }
}
