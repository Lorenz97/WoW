package at.lorenz.wow.manager;

import at.lorenz.wow.*;
import at.lorenz.wow.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class InGameMamager implements Listener {

    private final WoWPlugin plugin;

    public InGameMamager(WoWPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);

        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (plugin.getUserManager().getUser(player).getUserState() == UserState.INGAME) {
                    updateActionBar(player);
                }
            }
        }, 2, 2);
    }

    private void updateActionBar(Player player) {
        WowProfile profile = getProfile(player);
//        if (profile == null) return; //TODO remove
        int health = (int) profile.getHealth();
        int maxHealth = profile.getMaxHealth();

        String textHealth = "§c" + health + "/" + maxHealth;

        Utils.sendActionBar(player, textHealth);
    }

    public void join(Player player, WoWClass clazz) {
        WowUser user = plugin.getUserManager().getUser(player);

        player.getInventory().clear();

        WowProfile profile = new WowProfile(user, clazz);

        user.setActiveProfile(profile);

        user.setUserState(UserState.INGAME);
        plugin.getLocationManager().teleportToSpawn(player);
    }

    @EventHandler(ignoreCancelled = true)
    void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        Entity damager = event.getDamager();
        WowMob mob = plugin.getMobManager().getMobFromEntity(damager);
        if (mob == null) return;
        if (entity instanceof Player) {
            Player player = (Player) entity;
            WowProfile profile = getProfile(player);
//            if (profile == null) return; //TODO remove

            double damage = mob.getMobType().getDamage();

            double health = profile.getHealth();

            double result = health - damage;

            if (result < 0) {
                respawn(player);
                event.setCancelled(true);
            } else {
                event.setDamage(0);
                profile.setHealth(result);
            }
        }

    }

    private void respawn(Player player) {
        player.sendMessage("Du bist gestorben");
        player.teleport(plugin.getLocationManager().getSpawn());

        Utils.clear(player);

        WowProfile profile = getProfile(player);
        profile.setHealth(profile.getMaxHealth());

//        plugin.getMobManager().resetTarget(player);
    }

    public WowProfile getProfile(Player player) {
        return plugin.getUserManager().getUser(player).getActiveProfile();
    }
}
