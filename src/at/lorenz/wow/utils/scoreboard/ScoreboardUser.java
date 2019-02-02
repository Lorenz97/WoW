package at.lorenz.wow.utils.scoreboard;

import java.text.DecimalFormat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class ScoreboardUser {

    protected final Player player;
    protected final SidebarManager sidebarManager;
    private final Scoreboard scoreboard;
    private String prefix = "";
    private String suffix = "";
    private int order;

    protected ScoreboardUser(Player player) {
        this.player = player;
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        order = 0;

//        BukkitMethods.addPlayer(registerTeam(this), player.toPlayer());
        registerTeam(this).addPlayer(player);

        player.setScoreboard(scoreboard);
        sidebarManager = new SidebarManager(scoreboard, player);

        register();
        for (ScoreboardUser user : SmartScoreboard.users) {
            Team otherTeam = user.registerTeam(this);
            otherTeam.setPrefix(user.prefix);
            otherTeam.setSuffix(user.suffix);

//            BukkitMethods.addPlayer(otherTeam, user.player.toPlayer());
            otherTeam.addPlayer(user.player);
        }
    }

    private void register() {
        for (ScoreboardUser user : SmartScoreboard.users) {
            Team team = registerTeam(user);

//            BukkitMethods.addPlayer(team, player.toPlayer());
            team.addPlayer(player);

            team.setPrefix(prefix);
            team.setSuffix(suffix);
        }
    }

    protected String getTeamName() {
        String s = new DecimalFormat("000").format(order) + player.getName();
//        String s = StringManager.numFormat("000", order) + player;
        if (s.length() > 16) {
            s = s.substring(0, 16);
        }
        return s;
    }

    public void setPrefix(String prefix) {
        if (prefix.length() > 16) {
//            throw new InternalError("prefix is too big: " + prefix.length() + "(" + ColorString.removeColor(prefix) + ")");
            throw new InternalError("prefix is too big: " + prefix.length() + "(" + prefix + ")");
        }
        if (this.prefix.equals(prefix)) return;
        this.prefix = prefix;
        for (ScoreboardUser user : SmartScoreboard.users) {
            Team team = getTeam(user);
            team.setPrefix(prefix);
        }
    }

    public void setSuffix(String suffix) {
        if (suffix.length() > 16) {
//            throw new InternalError("suffix is too big: " + suffix.length() + "(" + ColorString.removeColor(suffix) + ")");
            throw new InternalError("suffix is too big: " + suffix.length() + "(" + suffix + ")");
        }
        if (this.suffix.equals(suffix)) return;
        this.suffix = suffix;
        for (ScoreboardUser user : SmartScoreboard.users) {
            Team team = getTeam(user);
            if (team == null) {
                team = registerTeam(user);
            }
            team.setSuffix(suffix);
        }
    }

    public void setOrder(int order) {
        if (this.order == order) return;
        if (order < 0 || order > 99) {
            throw new InternalError("Scoreboard order must be between 0 and 99!");
        }
        for (ScoreboardUser user : SmartScoreboard.users) {
            getTeam(user).unregister();
        }
        this.order = order;
        register();
    }

    private SidebarManager getTablistManager() {
        return sidebarManager;
    }

    protected Team getTeam(ScoreboardUser user) {
        return user.scoreboard.getTeam(getTeamName());
    }

    private Team registerTeam(ScoreboardUser user) {
        return user.scoreboard.registerNewTeam(getTeamName());
    }

    public void checkScoreboard() {
//        Player player = this.player.toPlayer();
        Player player = this.player;
        if (player.getScoreboard() != scoreboard) {
            Bukkit.broadcast("§cLost smart scoreboard: " + this.player.getName(), "op");
//            SmartBukkit.notifyOp("§cLost smart scoreboard: " + this.player);
            player.setScoreboard(scoreboard);
        }
    }
}
