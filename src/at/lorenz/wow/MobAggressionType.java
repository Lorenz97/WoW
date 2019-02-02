package at.lorenz.wow;

import lombok.Getter;
import org.bukkit.Material;

@Getter
public enum MobAggressionType {
    PEACEFUL("Peaceful", "§a", Material.GREEN_WOOL),
    PASSIVE("Passive", "§e", Material.YELLOW_WOOL),
    HOSTILE("Hostile", "§c", Material.RED_WOOL);

    private final String name;
    private final String color;
    private final Material material;

    MobAggressionType(String name, String color, Material material) {
        this.name = name;
        this.color = color;
        this.material = material;
    }

    public String getDisplayName() {
        return color + name;
    }
}
