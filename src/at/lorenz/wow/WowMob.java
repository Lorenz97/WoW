package at.lorenz.wow;

import at.lorenz.wow.entity.WowLookAtPlayer;
import at.lorenz.wow.entity.WowMeeleAttack;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.v1_13_R2.*;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftMob;
import org.bukkit.entity.*;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

@Getter
public class WowMob {

    private final MobType mobType;
    private final Region region;
    private final WoWPlugin plugin;
    @Setter
    private boolean alive = false;
    private UUID uuid;
    private ArmorStand label1;
    private ArmorStand label2;
    private LivingEntity entity;

    private double health;

    private Player target;

    public WowMob(WoWPlugin plugin, MobType mobType, Region region) {
        this.plugin = plugin;
        this.mobType = mobType;
        this.region = region;
    }

    public void spawn(Location location) {
        alive = true;
        health = mobType.getHealth();

        createEntity(location);

        updateLabel();
    }

    private void createEntity(Location location) {
        entity = (LivingEntity) location.getWorld().spawnEntity(location, mobType.getEntityType());

        checkAbilities();

        entity.setSilent(true);

        if (entity instanceof Zombie) {
            Zombie zombie = (Zombie) this.entity;
            zombie.setBaby(false);
            zombie.getEquipment().setItemInMainHand(null);
        }

        if (entity instanceof Sheep) {
            Sheep sheep = (Sheep) entity;
            sheep.setColor(DyeColor.WHITE);
        }

        if (entity instanceof MagmaCube) {
            MagmaCube cube = (MagmaCube) this.entity;
            cube.setSize(3);
        }

        if (mobType.getEntityType() == EntityType.ZOMBIE) {
            Zombie zombie = (Zombie) this.entity;
            EntityEquipment equipment = zombie.getEquipment();

            Material optionalHelmet = mobType.getOptionalHelmet();

            if (optionalHelmet != null) {
                equipment.setHelmet(new ItemStack(optionalHelmet));
            }





            Material equipmentChestplate = mobType.getEquipmentChestplate();
            Material equipmentLeggins = mobType.getEquipmentLeggins();
            Material equipmentBoots = mobType.getEquipmentBoots();

//            if (equipmentHelmet != null) {
//                equipment.setHelmet(new ItemStack(equipmentHelmet));
//            }
            if (equipmentChestplate != null) {
                equipment.setChestplate(new ItemStack(equipmentChestplate));
            }
            if (equipmentLeggins != null) {
                equipment.setLeggings(new ItemStack(equipmentLeggins));
            }
            if (equipmentBoots != null) {
                equipment.setBoots(new ItemStack(equipmentBoots));
            }
        }


        uuid = entity.getUniqueId();


        label2 = (ArmorStand) location.getWorld().spawnEntity(location.clone().add(0, -1.25 + mobType.getLabelOffset(), 0), EntityType.ARMOR_STAND);
        label2.setCustomNameVisible(true);
        label2.setGravity(false);
        label2.setVisible(false);
    }

    private void checkAbilities() {
        CraftLivingEntity livingEntity = (CraftLivingEntity) this.entity;

        EntityLiving entityHandle = livingEntity.getHandle();

        EntityInsentient entityInsentient = (EntityInsentient) entityHandle;

        PathfinderGoalSelector goalSelector = entityInsentient.goalSelector;
        PathfinderGoalSelector targetSelector = entityInsentient.targetSelector;

        try {
            Field field = PathfinderGoalSelector.class.getDeclaredField("b");
            field.setAccessible(true);
            Set o = (Set<?>) field.get(goalSelector);
            handleList("goalSelector b", o);

            if (entity.getType() == EntityType.ZOMBIE) {
                if (mobType.getAggressionType() == MobAggressionType.HOSTILE) {
                    addMeeleAttack();
                }
                goalSelector.a(8, new WowLookAtPlayer(plugin, entityInsentient, EntityHuman.class, 8.0F));
            } else if (entity.getType() == EntityType.SPIDER) {
                goalSelector.a(6, new WowLookAtPlayer(plugin, entityInsentient, EntityHuman.class, 8.0F));
            }


            field = PathfinderGoalSelector.class.getDeclaredField("c");
            field.setAccessible(true);
            o = (Set<?>) field.get(goalSelector);
            handleList("goalSelector c", o);


            field = PathfinderGoalSelector.class.getDeclaredField("b");
            field.setAccessible(true);
            o = (Set<?>) field.get(targetSelector);
            handleList("targetSelector b", o);

            field = PathfinderGoalSelector.class.getDeclaredField("c");
            field.setAccessible(true);
            o = (Set<?>) field.get(targetSelector);
            handleList("targetSelector c", o);
            System.out.println("");


        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void addMeeleAttack() {
        EntityType type = entity.getType();

        if (type == EntityType.ZOMBIE) {
            EntityLiving entityHandle = ((CraftLivingEntity) this.entity).getHandle();
            PathfinderGoalSelector goalSelector = ((EntityInsentient) entityHandle).goalSelector;

            if (entityHandle instanceof EntityCreature) { // check in properties
                EntityCreature entityCreature = (EntityCreature) entityHandle;
                Double movementSpeed = mobType.getMovementSpeed();
                WowMeeleAttack meleeAttack = new WowMeeleAttack(entityCreature, movementSpeed);
                goalSelector.a(2, meleeAttack);
            }
        } else {
            throw new RuntimeException("Meele Attack not implemented for " + type);
        }
    }

    private void handleList(String type, Set<?> list) {
        System.out.println("type: " + type);

        List<String> removeGoal = new ArrayList<>();

        removeGoal.add("PathfinderGoalRandomLookaround");
        removeGoal.add("PathfinderGoalMoveThroughVillage");
        removeGoal.add("PathfinderGoalRandomStrollLand");
        removeGoal.add("PathfinderGoalZombieAttack");

        removeGoal.add("PathfinderGoalLookAtPlayer");
        try {
            Class<?> clazz = Class.forName("net.minecraft.server.v1_13_R2.PathfinderGoalSelector$PathfinderGoalSelectorItem");
            for (Object object : new ArrayList<>(list)) {
                if (object.getClass() == clazz) {

                    Field field = clazz.getDeclaredField("a");
                    field.setAccessible(true);
                    PathfinderGoal goal = (PathfinderGoal) field.get(object);
                    String simpleName = goal.getClass().getSimpleName();
                    if (removeGoal.contains(simpleName)) {
                        list.remove(object);
                    } else {
                        System.out.println("goal: " + simpleName);
                    }

                }
            }
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void updateLocation() {
        Location location = entity.getLocation();
        if (label1 != null) {
            label1.teleport(location.clone().add(0, -1 + mobType.getLabelOffset(), 0));
        }
        if (label2 != null) {
            label2.teleport(location.clone().add(0, -1.25 + mobType.getLabelOffset(), 0));
        }
    }

    public void updateLabel() {
        if (health == mobType.getHealth()) {
            setLabelAmount(1);
            label2.setCustomName(mobType.getDisplayName());
        } else {
            setLabelAmount(2);

            label1.setCustomName(mobType.getDisplayName());
            int h = (int) health;
            label2.setCustomName("§4[§c|||||§4" + h + "§c|||||§4]");
        }

    }

    private void setLabelAmount(int i) {
        if (i == 2) {
            if (label1 == null) {
                Location location = entity.getLocation();
                label1 = (ArmorStand) location.getWorld().spawnEntity(location.clone().add(0, -1 + mobType.getLabelOffset(), 0), EntityType.ARMOR_STAND);
                label1.setCustomNameVisible(true);
                label1.setGravity(false);
                label1.setVisible(false);
            }
        } else if (i == 1) {
            if (label1 != null) {
                label1.remove();
                label1 = null;
            }
        }
    }

    public void damage(int damage) {
        this.health -= damage;
        if (health <= 0) {
            die();
        } else {
            updateLabel();
        }
    }

    public void die() {
        entity.setHealth(0);
        remove();
    }

    public void remove() {
        alive = false;
        if (label1 != null) {
            label1.remove();
        }
        label2.remove();
    }

    public void setTarget(Player player) {
        this.target = player;
//        if (target != null) {

        addMeeleAttack();

        CraftEntity craftEntity = (CraftEntity) entity;
        if (craftEntity instanceof CraftMob) {
            CraftMob craftMob = (CraftMob) craftEntity;
            CraftLivingEntity target = craftMob.getTarget();
            if (target != null) {
                if (target.getUniqueId().equals(player.getUniqueId())) {
                    craftMob.setTarget(player);
                }
            }
        }
//        }
    }
}
