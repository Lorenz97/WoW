package at.lorenz.wow.utils.scoreboard;

import java.util.ArrayList;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

public class SmartScoreboard {

    public static final ArrayList<ScoreboardUser> users = new ArrayList<>();

    public static ScoreboardUser getUser(Player player, boolean create) {
        for (ScoreboardUser user : users) {
            if (user.player.getUniqueId().equals(player.getUniqueId())) {
                return user;
            }
        }
        if (!create) {
            return null;
        }
        ScoreboardUser user = new ScoreboardUser(player);
        users.add(user);
        return user;
    }

    public static ScoreboardUser getUser(Player player) {
        return getUser(player, true);
    }

    public static void removeUser(Player player) {
        ScoreboardUser user = getUser(player, false);
        if (user == null) return;
        users.remove(user);
        for (ScoreboardUser all : users) {
            Team team = user.getTeam(all);
            if (team != null) {
                team.unregister();
            }
        }
    }
}
