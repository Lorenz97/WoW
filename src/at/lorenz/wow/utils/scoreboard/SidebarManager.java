package at.lorenz.wow.utils.scoreboard;

import java.text.DecimalFormat;
import java.util.ArrayList;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

class SidebarManager {

    private final Scoreboard scoreboard;
    private final Objective objective;
    private final ArrayList<String> content = new ArrayList<>();
    private String title;

    public SidebarManager(Scoreboard scoreboard, Player player) {
        objective = scoreboard.registerNewObjective(player.getName(), "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        this.scoreboard = scoreboard;
    }

    public void accept(SidebarBuilder builder) {
        if (builder.title != null) {
            title = builder.title;
        }
        if (title == null) {
            throw new InternalError("Title cannot be null");
        }
        objective.setDisplayName(title);
        ArrayList<String> newEntries = new ArrayList<>();
        int i = 0;
        for (String entry : builder.list) {
            newEntries.add(get(i) + entry);
            i++;
        }
        for (String entry : new ArrayList<>(content)) {
            if (!newEntries.contains(entry)) {
                scoreboard.resetScores(entry);
                content.remove(entry);
            }
        }
        for (String entry : newEntries) {
            if (content.contains(entry)) continue;
            objective.getScore(entry).setScore(0);
            content.add(entry);
        }
    }

    private String get(int i) {
        String s = new DecimalFormat("00").format(i);
//        String s = StringManager.numFormat("00", i);
        String a = s.substring(0, 1);
        String b = s.substring(1, 2);
        return "§" + a + "§" + b + "§f";
    }

}
