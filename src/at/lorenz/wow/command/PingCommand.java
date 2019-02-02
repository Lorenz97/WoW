package at.lorenz.wow.command;

import at.lorenz.wow.WoWPlugin;
import at.lorenz.wow.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class PingCommand implements CommandExecutor {

    private final WoWPlugin plugin;

    public PingCommand(WoWPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            int ping = Utils.getPing(player);
            player.sendMessage("ping: " + ping);

        }
        return true;
    }

}
