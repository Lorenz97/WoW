package at.lorenz.wow.manager;

import at.lorenz.wow.WoWPlugin;
import java.io.File;
import java.io.IOException;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

@Getter
@Setter
public class LocationManager {

    private Location select;
    private Location spawn;
    private final File configFile = new File("plugins/WoW/config.txt");

    private final WoWPlugin plugin;

    public LocationManager(WoWPlugin plugin) {
        this.plugin = plugin;
    }

    public void teleportToSpawn(Player player) {
        if (spawn != null) {
            player.teleport(spawn);
        } else {
            player.sendMessage("§4Spawn is null");
        }
    }

    public void teleportToSelect(Player player) {
        if (select != null) {
            player.teleport(select);
        } else {
            player.sendMessage("§4Select is null");
        }
    }

    public void loadConfig() {
        if (configFile.isFile()) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            select = (Location) config.get("select");
            spawn = (Location) config.get("spawn");
        }
    }

    public void saveConfig() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("select", select);
        config.set("spawn", spawn);
        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
