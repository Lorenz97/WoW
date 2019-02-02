package at.lorenz.wow.utils.scoreboard;

import java.util.ArrayList;
import java.util.Collections;
import org.bukkit.entity.Player;

public class SidebarBuilder {

    protected final ArrayList<String> list = new ArrayList<>();
    protected String title;

    public SidebarBuilder setTitle(String title) {
        this.title = title;
        return this;
    }

    public SidebarBuilder add(String... lines) {
        Collections.addAll(list, lines);
        return this;
    }

    public void send(Player player) {
        SmartScoreboard.getUser(player).sidebarManager.accept(this);
    }
}
