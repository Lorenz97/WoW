package at.lorenz.wow.command;

import at.lorenz.wow.WoWPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetSpawnCommand implements CommandExecutor {

    private final WoWPlugin plugin;

    public SetSpawnCommand(WoWPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            if (sender.isOp()) {
                Player player = (Player) sender;
                plugin.getLocationManager().setSpawn(player.getLocation());
                player.sendMessage("Spawn set");
            }
        }
        return true;
    }
}
