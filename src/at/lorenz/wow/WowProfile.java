package at.lorenz.wow;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WowProfile {

    private final WowUser user;
    private final WoWClass clazz;

    private double health = 20;
    private int maxHealth = 20;

    public WowProfile(WowUser user, WoWClass clazz) {
        this.user = user;
        this.clazz = clazz;
    }
}
