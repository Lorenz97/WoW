package at.lorenz.wow;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

@Getter
@Setter
public class Region {

    private Location a;
    private Location b;
    private String name;
    private final Map<MobType, Integer> possibleMobs = new HashMap<>();

    public Region() {
//        possibleMobs.put(MobType.SHEEP, 30);

//        possibleMobs.put(MobType.SHEEP, 10);
//        possibleMobs.put(MobType.BAD_SHEEP, 2);
//        possibleMobs.put(MobType.COW, 7);
//        possibleMobs.put(MobType.PIG, 10);

//        possibleMobs.put(MobType.ZOMBIE, 3);
    }

    public boolean equals(Region other) {
        return name.equals(other.name);
    }
}
