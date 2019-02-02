package at.lorenz.wow.command;

import at.lorenz.wow.WowUser;
import at.lorenz.wow.UserState;
import at.lorenz.wow.WoWPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ClassesCommand implements CommandExecutor {

    private final WoWPlugin plugin;

    public ClassesCommand(WoWPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            WowUser user = plugin.getUserManager().getUser(player);
            if (user.getUserState() != UserState.SELECT) {
                plugin.getSelectManager().putIntoSelect(player);
            }
        }
        return true;
    }
}
