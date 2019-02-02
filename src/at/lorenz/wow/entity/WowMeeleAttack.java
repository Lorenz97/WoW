package at.lorenz.wow.entity;

import net.minecraft.server.v1_13_R2.EntityCreature;
import net.minecraft.server.v1_13_R2.PathfinderGoalMeleeAttack;

public class WowMeeleAttack extends PathfinderGoalMeleeAttack {
    public WowMeeleAttack(EntityCreature entityCreature, double damage) {
        super(entityCreature, damage, false);
    }
}
