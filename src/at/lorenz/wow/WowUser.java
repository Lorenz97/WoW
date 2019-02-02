package at.lorenz.wow;

import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

@Getter
@Setter
public class WowUser {

    private final UUID uuid;
    private final String name;
    private UserState userState;
    private WowProfile activeProfile;

    public WowUser(Player player) {
        uuid = player.getUniqueId();
        name = player.getName();
    }
}
