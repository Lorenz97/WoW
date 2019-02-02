package at.lorenz.wow.manager;

import at.lorenz.wow.WowUser;
import at.lorenz.wow.UserState;
import at.lorenz.wow.WoWPlugin;
import at.lorenz.wow.utils.Utils;
import at.lorenz.wow.utils.scoreboard.SidebarBuilder;
import at.lorenz.wow.utils.scoreboard.SmartScoreboard;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class UserManager implements Listener {
    private final WoWPlugin plugin;

    private List<WowUser> users = new ArrayList<>();

    public UserManager(WoWPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);

        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (plugin.getUserManager().getUser(player).getUserState() == UserState.INGAME) {
                    SidebarBuilder builder = new SidebarBuilder();
                    builder.setTitle("");
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
    void onJoin(PlayerJoinEvent event) {
        handleJoin(event.getPlayer());
    }

    @EventHandler
    void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        handleQuit(player);
        SmartScoreboard.removeUser(player);
    }

    public void handleQuit(Player player) {
        WowUser user = getUser(player);
        users.remove(user);
    }

    public void handleJoin(Player player) {
        WowUser user = new WowUser(player);
        users.add(user);
        plugin.getSelectManager().putIntoSelect(player);
    }

    public WowUser getUser(Player player) {
        for (WowUser user : users) {
            if (user.getUuid().equals(player.getUniqueId())) {
                return user;
            }
        }
        throw new RuntimeException("player " + player.getName() + " has no user");
    }
}
