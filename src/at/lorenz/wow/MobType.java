package at.lorenz.wow;

import java.lang.reflect.Field;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

@Getter
@Setter
public class MobType {
//    SHEEP(Material.SHEEP_SPAWN_EGG, "Schaf", EntityType.SHEEP, 0.5, 3, 5, MobAggressionType.PEACEFUL),
//    PIG(Material.PIG_SPAWN_EGG, "Schwein", EntityType.PIG, 0, 2, 2, MobAggressionType.PEACEFUL),
//    BAD_SHEEP(Material.SHEEP_SPAWN_EGG, "Untotes Schaf", EntityType.SHEEP, 0.5, 7, 5, MobAggressionType.HOSTILE),
//    COW(Material.COW_SPAWN_EGG, "Kuh", EntityType.COW, 0.6, 5, 1, MobAggressionType.PASSIVE),
//    ZOMBIE(Material.ZOMBIE_HEAD, "Zombie", EntityType.ZOMBIE, 1.1, 6, 3, MobAggressionType.HOSTILE);

    private int id;
    private String name;
    private Material displayMaterial;
    private EntityType entityType;
    private MobAggressionType aggressionType;
    private Double labelOffset;
    private Integer health;
    private Integer level;
    private Double movementSpeed;
    private Double jumpHeigth;
    private Double damage;
    private Integer gainExp;
    private Double triggerRange;

    private Material equipmentChestplate;
    private Material equipmentLeggins;
    private Material equipmentBoots;

    private Material optionalHelmet;

//    public MobType(Material displayMaterial, String name, EntityType entityType, double labelOffset, int health, int level, MobAggressionType aggressionType, double movementSpeed, double damage, int gainExp, double jumpHeigth, double triggerRange,
//                   Material equipmentHelmet, Material equipmentChestplate, Material equipmentLeggins, Material equipmentBoots) {
//
//        this.name = name;
//        this.entityType = entityType;
//        this.labelOffset = labelOffset;
//        this.health = health;
//        this.level = level;
//        this.aggressionType = aggressionType;
//        this.displayMaterial = displayMaterial;
//        this.movementSpeed = movementSpeed;
//        this.damage = damage;
//        this.gainExp = gainExp;
//        this.jumpHeigth = jumpHeigth;
//        this.triggerRange = triggerRange;
//
//        this.equipmentHelmet = equipmentHelmet;
//        this.equipmentChestplate = equipmentChestplate;
//        this.equipmentLeggins = equipmentLeggins;
//        this.equipmentBoots = equipmentBoots;
//    }

//    public MobType() {
////        this.name = null;
////        this.entityType = null;
////        this.labelOffset = null;
////        this.health = null;
////        this.level = null;
////        this.aggressionType = null;
////        this.displayMaterial = null;
////        this.movementSpeed = null;
////        this.damage = null;
////        this.gainExp = null;
//    }

    public String getDisplayName() {
        return aggressionType.getColor() + name + " §6[Level " + level + "]";
    }

    public MobType duplicate() {
        MobType duplicate = new MobType();
        for (Field field : getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object o = field.get(this);
                field.set(duplicate, o);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return duplicate;
    }
}
