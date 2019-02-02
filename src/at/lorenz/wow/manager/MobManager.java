package at.lorenz.wow.manager;

import at.lorenz.wow.*;
import at.lorenz.wow.utils.InventoryMenu;
import at.lorenz.wow.utils.ItemBuilder;
import at.lorenz.wow.utils.Utils;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

public class MobManager implements Listener {

    private final WoWPlugin plugin;
    private final List<WowMob> mobs = new ArrayList<>();
    private final List<MobType> mobTypes = new ArrayList<>();

    private final File configFile = new File("plugins/WoW/mobs.txt");
    private final int MIN_DISTANCE = 9;
    private final int MAX_DISTANCE = 35;

    public MobManager(WoWPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);

        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (plugin.getUserManager().getUser(player).getUserState() != UserState.SELECT) {
                    spawnMobs(player);
                }
            }
        }, 10, 10);

        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (WowMob mob : mobs) {
                mob.updateLocation();
            }
        }, 1, 1);
    }

    private void spawnMobs(Player player) {
        List<Location> locations = new ArrayList<>();
        Map<Location, List<Region>> regions = new HashMap<>();
        for (int i = 0; i < 10_000; i++) {
            Location location;
            if (plugin.getUserManager().getUser(player).getUserState() == UserState.INGAME) {
                location = Utils.randomLocationInCircleWithHole(player.getLocation(), MAX_DISTANCE, MIN_DISTANCE);
            } else {
                location = Utils.randomLocationInCircleWithHole(player.getLocation(), MAX_DISTANCE, 3);

            }
            if (!canSpawnOtherPlayers(location)) continue;

            List<Region> regs = plugin.getRegionManager().getRegionAt(location);
            if (!regs.isEmpty()) {
                locations.add(location);
                regions.put(location, regs);
            }
        }
        if (locations.isEmpty()) return;
        Location location = Utils.randomElementFromList(locations);
        List<Region> list = regions.get(location);
        Region r = Utils.randomElementFromList(list);
//        Bukkit.broadcastMessage("distance: " + player.getLocation().distance(location));
        spawnMobInRegion(r, location);
    }

    private boolean canSpawnOtherPlayers(Location location) {
        for (Player all : Bukkit.getOnlinePlayers()) {
            if (plugin.getUserManager().getUser(all).getUserState() == UserState.INGAME) {
                if (all.getLocation().distance(location) < MIN_DISTANCE) {
                    return false;
                }
            }
        }
        return true;
    }

    @EventHandler(priority = EventPriority.LOW)
    void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        WowMob mob = getMobFromEntity(entity);
        if (mob != null) {
            Entity damager = event.getDamager();
            if (damager instanceof Player) {
                Player player = (Player) damager;

                if (plugin.getUserManager().getUser(player).getUserState() == UserState.CREATIVE) {
                    event.setCancelled(true);
                    entity.remove();
                    mob.remove();
                } else {
                    if (mob.getMobType().getAggressionType() == MobAggressionType.PASSIVE) {
                        if (mob.getTarget() == null) {
                            mob.setTarget(player);
                        }
                    }
                    event.setDamage(0);
                    mob.damage(1);
                }
            }
        }
    }

    public WowMob getMobFromEntity(Entity entity) {
        for (WowMob mob : mobs) {
            if (mob.getUuid().equals(entity.getUniqueId())) {
                return mob;
            }
        }
        return null;
    }

    private void spawnMobInRegion(Region region, Location location) {
        List<MobType> mobTypes = new ArrayList<>();
        for (Map.Entry<MobType, Integer> entry : region.getPossibleMobs().entrySet()) {
            MobType mobType = entry.getKey();
            int limit = entry.getValue();
            List<WowMob> mobsInRegion = getMobsInRegion(region, mobType);
            if (mobsInRegion.size() < limit) {
                mobTypes.add(mobType);
            }
        }
        if (mobTypes.isEmpty()) return;

        location = fixLocation(location).getBlock().getLocation();
        location.add(0.5, 0, 0.5);

        if (!plugin.getRegionManager().getRegionAt(location).contains(region)) {
            return;
        }

        MobType mobType = Utils.randomElementFromList(mobTypes);
        WowMob mob = new WowMob(plugin, mobType, region);
        mobs.add(mob);

        mob.spawn(location);
    }

    private Location fixLocation(Location location) {
        Block block = location.getBlock();
//        return down(location);
        if (block.getType() == Material.AIR) {
            return up(down(location));
        }
        return up(location);
    }

    private Location up(Location location) {
        Block block = location.getBlock();
        if (block.getType() == Material.AIR) {
            return location;
        }
        return up(location.add(0, 1, 0));
    }

    private Location down(Location location) {
        Block block = location.getBlock();
        if (block.getType() == Material.AIR) {
            return down(location.add(0, -1, 0));
        }
        return location;
    }

    public List<WowMob> getMobsInRegion(Region region, MobType mobType) {
        List<WowMob> list = new ArrayList<>();
        for (WowMob mob : mobs) {
            if (mob.isAlive()) {
                if (mob.getRegion() == region) {
                    if (mob.getMobType() == mobType) {
                        list.add(mob);
                    }
                }
            }
        }
        return list;
    }

    public void pickMobInventory(Player player, String title, Set<MobType> except, Consumer<MobType> result) {
        InventoryMenu menu = plugin.getMenuManager().create(player, 3);

        menu.addItem(8, WowItems.BACK, () -> {
            result.accept(null);
        });

        int index = 0;
        for (MobType mobType : mobTypes) {
            if (except.contains(mobType)) continue;
            ItemStack itemStack = ItemBuilder.material(mobType.getDisplayMaterial()).name(mobType.getDisplayName()).build();
            menu.addItem(index, itemStack, () -> {
                result.accept(mobType);
            });
            index++;
            if (index % 9 == 8) {
                index++;
            }
        }
        menu.open(title);
    }

    public void openMobInventory(Player player) {
        InventoryMenu menu = plugin.getMenuManager().create(player, 3);

        menu.addItem(8, WowItems.BACK, () -> {
            player.closeInventory();
        });
        menu.addItem(17, ItemBuilder.material(Material.GREEN_WOOL).name("Create new mob").build(), () -> {
            startCreateNewMob(player);
        });

        int index = 0;
        for (MobType mobType : mobTypes) {
            Material material = mobType.getDisplayMaterial();
            if (material == null) {
                mobTypes.remove(mobType);
                return;
            }
            System.out.println("material " + material);
            ItemStack itemStack = ItemBuilder.material(material).name(mobType.getDisplayName()).build();
            menu.addItem(index, itemStack, () -> {
                editMobType(player, mobType, false);
            });
            index++;
            if (index % 9 == 8) {
                index++;
            }
        }

        menu.open("Mobs");

    }

    public void editMobType(Player player, MobType mobType, boolean newMob) {
        List<MobProperty> properties = new LinkedList<>();

        properties.add(new MobProperty("name", "Name", Material.PAPER, "The name of the mob. Must be unique"));
        properties.add(new MobProperty("level", "Level", Material.WHITE_WOOL, "A level."));
        properties.add(null);
        properties.add(new MobProperty("displayMaterial", "Display material", Material.STONE, "Only visible in configuration for admins"));
        properties.add(new MobProperty("entityType", "Entity type", Material.EGG, "The minecraft entity type"));
        properties.add(new MobProperty("labelOffset", "Label offset", Material.LEAD, "The distance between the entity and the nametag above it"));
        properties.add(new MobProperty("health", "Health", Material.POTION, "Amount of health the mob will spawn with"));
        properties.add(new MobProperty("aggressionType", "Aggression type", Material.ZOMBIE_HEAD, "How should the mob behave itself?"));
        properties.add(new MobProperty("movementSpeed", "Movement speed", Material.FEATHER, "The vertical speed of the mob"));
        properties.add(new MobProperty("jumpHeigth", "Jump heigth", Material.RABBIT_FOOT, "The horizontal at wich the entity can jump"));
        properties.add(new MobProperty("damage", "Damage", Material.DIAMOND_SWORD, "How much damage does the mob apply?"));
        properties.add(new MobProperty("gainExp", "Gain experience", Material.EXPERIENCE_BOTTLE, "The amount of experience the killer will get"));
        properties.add(new MobProperty("triggerRange", "Trigger range", Material.ENDER_EYE, "the max range at when the mob can 'see' a player"));

        checkCustomProperties(mobType, properties);

        InventoryMenu menu = plugin.getMenuManager().create(player, 6);

        menu.addItem(8, WowItems.BACK, () -> {
            openMobInventory(player);
        });

        int index = 0;

        for (MobProperty property : properties) {

            if (property != null) {

                ItemBuilder builder = ItemBuilder.material(property.material);
                builder.name("§f" + property.displayName);
                builder.flags(ItemFlag.HIDE_POTION_EFFECTS);
                builder.flags(ItemFlag.HIDE_ATTRIBUTES);
                builder.lore("§7" + property.description);
                ItemStack itemStack = builder.build();

                Runnable runnable = () -> {
                    Class clazz = getClass(mobType, property);
                    if (clazz.equals(Material.class)) {
                        List<Material> materials = new ArrayList<>();
                        for (Material material : Material.values()) {
                            if (material == Material.BARRIER) continue;
                            materials.add(material);
                        }
                        plugin.openSelectMaterialInventory("Pick a material", player, materials, material -> {
                            setValue(mobType, property.id, material);
                            editMobType(player, mobType, newMob);
                        });
                    } else if (clazz.equals(EntityType.class)) {
                        List<Material> materials = new ArrayList<>();
                        for (Material material : Material.values()) {
                            if (!material.name().contains("SPAWN_EGG")) continue;
                            materials.add(material);
                        }
                        plugin.openSelectMaterialInventory("Pick a entity", player, materials, material -> {
                            if (material == null) {
                                editMobType(player, mobType, newMob);
                                return;
                            }
                            String name = material.name();
                            System.out.println("name " + name);

                            EntityType entityType = getEntityType(material);
                            System.out.println("entityType " + entityType);

                            setValue(mobType, property.id, entityType);
                            editMobType(player, mobType, newMob);
                        });
                    } else if (clazz.equals(MobAggressionType.class)) {
                        selectAggresionType(player, aggressionType -> {
                            if (aggressionType == null) {
                                editMobType(player, mobType, newMob);
                                return;
                            }
                            setValue(mobType, property.id, aggressionType);
                            editMobType(player, mobType, newMob);
                        });
                    } else {
                        player.sendMessage("Set a value for property '" + property.displayName + "':");
                        player.closeInventory();
                        plugin.getStringFromChat(player, string -> {
                            if (clazz.equals(Integer.class)) {
                                try {
                                    int val = Integer.valueOf(string);
                                    setValue(mobType, property.id, val);
                                    editMobType(player, mobType, newMob);
                                    player.sendMessage(property.displayName + " set to " + val);
                                } catch (NumberFormatException e) {
                                    player.sendMessage("'" + string + "' is not a integer");
                                    editMobType(player, mobType, newMob);
                                }
                            } else if (clazz.equals(Double.class)) {
                                try {
                                    double val = Double.valueOf(string);
                                    setValue(mobType, property.id, val);
                                    editMobType(player, mobType, newMob);
                                    player.sendMessage(property.displayName + " set to " + val);
                                } catch (NumberFormatException e) {
                                    player.sendMessage("'" + string + "' is not a double");
                                    editMobType(player, mobType, newMob);
                                }
                            } else if (clazz.equals(String.class)) {
                                setValue(mobType, property.id, string);
                                editMobType(player, mobType, newMob);
                            } else {
                                throw new RuntimeException("Invalid class: " + clazz.getName());
                            }
                        });
                    }
                };
                menu.addItem(index, itemStack, runnable);

                String id = property.id;

                Object value = getValue(mobType, property.id);
                if (value == null) {
                    menu.addItem(index + 9, ItemBuilder.material(Material.BARRIER).name("§cNo value set").build(), runnable);
                } else {
                    ItemStack stack;
                    if (getClass(mobType, property).equals(MobAggressionType.class)) {
                        MobAggressionType aggressionType = (MobAggressionType) getValue(mobType, id);
                        stack = getItem(aggressionType);
                    } else {
                        Material material;
                        if (getClass(mobType, property).equals(Material.class)) {
                            material = (Material) getValue(mobType, id);
                        } else if (getClass(mobType, property).equals(EntityType.class)) {
                            material = getMaterial((EntityType) getValue(mobType, id));
                        } else {
                            material = Material.GREEN_WOOL;
                        }
                        stack = ItemBuilder.material(material).name("§a" + value).build();
                    }

                    menu.addItem(index + 9, stack, runnable);
                }
            }


            index++;
            if (index % 9 == 7) {
                index += 9 + 2;
            }
        }


        if (newMob) {
            if (isReady(mobType, properties)) {
                menu.addItem(44, ItemBuilder.material(Material.GREEN_WOOL).name("§aCreate mob").build(), () -> {
                    createMobType(player, mobType);
                });
            } else {
                menu.addItem(44, ItemBuilder.material(Material.GREEN_WOOL).name("§7Create mob").lore("§cProperties missing").build());
            }
        } else {
            menu.addItem(26, ItemBuilder.material(Material.LAVA_BUCKET).name("§cDelete mob").build(), () -> {
                plugin.getMenuManager().confirmAction(player, "Delete mob '" + mobType.getName() + "'?", "Yes, delete", "No, cancel", answer -> {
                    if (answer) {
                        deleteMobType(mobType);
                        player.sendMessage("Mob " + mobType.getName() + " deleted");
                        openMobInventory(player);
                    } else {
                        editMobType(player, mobType, newMob);
                    }
                });
            });
            menu.addItem(26 + 9, ItemBuilder.material(Material.SPONGE).name("§6Duplicate mob").build(), () -> {
                MobType duplicate = mobType.duplicate();
                duplicate.setName(null);
                duplicate.setLevel(null);
                editMobType(player, duplicate, true);
            });
            menu.addItem(26 + 9 * 2, ItemBuilder.material(Material.STICK).name("§3Create preview").build(), () -> {
                WowMob mob = new WowMob(plugin, mobType, null);
                mobs.add(mob);


                Location location = player.getLocation();
                location = fixLocation(location).getBlock().getLocation();
                location.add(0.5, 0, 0.5);
                mob.spawn(location);
            });
        }

        ItemStack borderItem = ItemBuilder.material(Material.GRAY_STAINED_GLASS_PANE).name(" ").build();
        index = 7;
        for (int i = 0; i < 6; i++) {
            menu.addItem(index, borderItem);
            index += 9;
        }
//        for (int i : new int[]{0, 1, 2, 3, 4, 5, 6, 7,  16, 25, 34, 43, 52}) {
//            menu.addItem(i, borderItem);
//        }
        if (newMob) {
            menu.open("Create new mob");
        } else {
            menu.open("Edit mob " + mobType.getName());
        }
    }

    private void checkCustomProperties(MobType mobType, List<MobProperty> properties) {
        EntityType entityType = mobType.getEntityType();
        if (entityType == EntityType.ZOMBIE) {
            properties.add(null);
            properties.add(new MobProperty("optionalHelmet", "Helmet", Material.LEATHER_HELMET, "The helmet to wear"));
//            properties.add(new MobProperty("equipmentChestplate", "Chestplate", Material.LEATHER_CHESTPLATE, "The chestplate to wear"));
//            properties.add(new MobProperty("equipmentLeggins", "Leggins", Material.LEATHER_LEGGINGS, "The leggins to wear"));
//            properties.add(new MobProperty("equipmentBoots", "Boots", Material.LEATHER_BOOTS, "The boots to wear"));
        }
    }

    private void checkCustomProperties() {

    }

    private void updateMobs(MobType mobType) {
        System.out.println("updateMobs");
        for (WowMob mob : mobs) {
            if (mob.getMobType() == mobType) {
                mob.getEntity().remove();
                mob.remove();
            }
        }
    }

    private void deleteMobType(MobType mobType) {
        mobTypes.remove(mobType);
        plugin.getRegionManager().removeFromAllRegions(mobType);

        for (WowMob mob : new ArrayList<>(mobs)) {
            if (mob.getMobType() == mobType) {
                mob.getEntity().remove();
                mob.remove();
                mobs.remove(mob);
            }
        }
    }

    private void startCreateNewMob(Player player) {
        editMobType(player, new MobType(), true);
    }

    private void createMobType(Player player, MobType mobType) {
        mobTypes.add(mobType);
        mobType.setId(mobTypes.size());
        player.sendMessage("Mob type successfully created");
        openMobInventory(player);
    }

    private boolean isReady(MobType mobType, List<MobProperty> properties) {
        return true;
//        for (MobProperty property : properties) {
//            if (property == null) continue;
//            if (property.id.startsWith("equipment")) continue;
//            if (getValue(mobType, property.id) == null) {
//                return false;
//            }
//        }
//        return true;
    }

    private void selectAggresionType(Player player, Consumer<MobAggressionType> result) {
        InventoryMenu menu = plugin.getMenuManager().create(player, 3);

        menu.addItem(8, WowItems.BACK, () -> {
            result.accept(null);
        });

        int i = 11;
        for (MobAggressionType aggressionType : MobAggressionType.values()) {
            menu.addItem(i, getItem(aggressionType), () -> {
                result.accept(aggressionType);
            });
            i += 2;
        }

        menu.open("§fSelect aggresion type");

    }

    private ItemStack getItem(MobAggressionType aggressionType) {
        return ItemBuilder.material(aggressionType.getMaterial()).name(aggressionType.getDisplayName()).build();
    }

    private EntityType getEntityType(Material material) {
        if (material == Material.ZOMBIE_PIGMAN_SPAWN_EGG) {
            return EntityType.PIG_ZOMBIE;
        }
        String name = material.name();
        String s = name.substring(0, name.length() - "_SPAWN_EGG".length());
        for (EntityType entityType : EntityType.values()) {
            if (entityType.name().equals(s)) {
                return entityType;
            }
        }
        return null;
    }

    private Material getMaterial(EntityType entityType) {
        if (entityType == EntityType.PIG_ZOMBIE) {
            return Material.ZOMBIE_PIGMAN_SPAWN_EGG;
        }
        String name = entityType.name();
        return Material.valueOf(name + "_SPAWN_EGG");
    }

    private void setValue(MobType mobType, String id, Object object) {
        try {
            Field field = mobType.getClass().getDeclaredField(id);
            field.setAccessible(true);
            field.set(mobType, object);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("reflection error", e);
        }

        updateMobs(mobType);
    }

    private Object getValue(MobType mobType, String id) {
        try {
            Field field = mobType.getClass().getDeclaredField(id);
            field.setAccessible(true);
            return field.get(mobType);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("reflection error", e);
        }
    }

    private Class getClass(MobType mobType, MobProperty property) {
        try {
            Field field = mobType.getClass().getDeclaredField(property.id);
            return field.getType();
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("reflection error", e);
        }
    }

    public void saveConfig() {
        System.out.println("save");
        YamlConfiguration config = new YamlConfiguration();

        int i = 0;
        for (MobType mobType : mobTypes) {
            for (Field field : mobType.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                String name = field.getName();
                try {
                    Object o = field.get(mobType);
                    if (o != null) {
                        System.out.println("set");
                        if (o instanceof Number) {
                            config.set(i + "." + name, o);
                        } else {
                            config.set(i + "." + name, o.toString());
                        }
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            i++;
        }

        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadConfig() {
        if (configFile.isFile()) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            for (String id : config.getKeys(false)) {
//                Material displayMaterial = Material.valueOf(config.getString(id + ".displayMaterial"));
//                String name = config.getString(id + ".name");
//                EntityType entityType = EntityType.valueOf(config.getString(id + ".entityType"));
//                double labelOffset = config.getDouble(id + ".labelOffset");
//                int health = config.getInt(id + ".health");
//                int level = config.getInt(id + ".level");
//                MobAggressionType aggressionType = MobAggressionType.valueOf(config.getString(id + ".aggressionType"));
//                double movementSpeed = config.getDouble(id + ".movementSpeed");
//                double damage = config.getDouble(id + ".damage");
//                int gainExp = config.getInt(id + ".gainExp");
//                double jumpHeigth = config.getDouble(id + ".jumpHeigth");
//                double triggerRange = config.getDouble(id + ".triggerRange");
//
//                Material equipmentHelmet = null;
//                Material equipmentChestplate = null;
//                Material equipmentLeggins = null;
//                Material equipmentBoots = null;

//                if (entityType == EntityType.ZOMBIE) {
//                    String a = config.getString("equipmentHelmet");
//                    String b = config.getString("equipmentChestplate");
//                    String c = config.getString("equipmentLeggins");
//                    String d = config.getString("equipmentBoots");
//
//                    if (a != null) {
//                        equipmentHelmet = Material.valueOf(a);
//                    }
//                    if (b != null) {
//                        equipmentChestplate = Material.valueOf(b);
//                    }
//                    if (c != null) {
//                        equipmentLeggins = Material.valueOf(c);
//                    }
//                    if (d != null) {
//                        equipmentBoots = Material.valueOf(d);
//                    }
//                }

//                MobType mobType = new MobType(displayMaterial, name, entityType, labelOffset, health, level, aggressionType, movementSpeed, damage, gainExp, jumpHeigth, triggerRange, equipmentHelmet, equipmentChestplate, equipmentLeggins, equipmentBoots);
                MobType mobType = new MobType();

                for (Field field : mobType.getClass().getDeclaredFields()) {
                    field.setAccessible(true);
                    String fieldName = field.getName();
                    Object o = config.get(id + "." + fieldName);
                    System.out.println("field " + fieldName);
                    if (o != null) {
                        System.out.println("not null " + fieldName);
                        try {
                            Class<?> clazz = field.getType();
                            if (clazz == Material.class) {
                                Material material = Material.valueOf(config.getString(id + "." + fieldName));
                                System.out.println("load " + material);
                                field.set(mobType, material);
                            } else if (clazz == String.class) {
                                String string = config.getString(id + "." + fieldName);
                                field.set(mobType, string);
                            } else if (clazz == EntityType.class) {
                                EntityType type = EntityType.valueOf(config.getString(id + "." + fieldName));
                                field.set(mobType, type);
                            } else if (clazz == MobAggressionType.class) {
                                MobAggressionType type = MobAggressionType.valueOf(config.getString(id + "." + fieldName));
                                field.set(mobType, type);
                            } else if (clazz == int.class) {
                                System.out.println("");
                                System.out.println("");
                                System.out.println("debug");
                                System.out.println("id " + id);
                                System.out.println("fieldName " + fieldName);
                                int anInt = config.getInt(id + "." + fieldName);
                                System.out.println("anInt " + anInt);
                                System.out.println("clazz " + clazz.getName());
                                System.out.println("");
                                System.out.println("");
                                field.set(mobType, anInt);
                            } else {
                                throw new RuntimeException("Invalid clazz type for '" + fieldName + "' = " + clazz.getName());
                            }
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    } else {
                        System.out.println("null " + fieldName);
                    }
                }

                mobTypes.add(mobType);
                mobType.setId(Integer.valueOf(id));
            }
        }
    }

//    public MobType getMobType(String name) {
//        for (MobType mobType : mobTypes) {
//            if (mobType.getName().equals(name)) {
//                return mobType;
//            }
//        }
//        throw new RuntimeException("mob type '" + name + "' not found");
//    }

    public MobType getMobTypebyId(int id) {
        for (MobType mobType : mobTypes) {
            if (mobType.getId() == id) {
                return mobType;
            }
        }
        throw new RuntimeException("Mob type with id " + id + " not found");
    }

//    public void resetTarget(Player player) {
//        for (WowMob mob : mobs) {
//            Player target = mob.getTarget();
//            if (target != null) {
//                if (target.getUniqueId().equals(player.getUniqueId())) {
//                    mob.setTarget(null);
//                }
//            }
//        }
//    }

    private class MobProperty {
        private final String displayName;
        private final Material material;
        private final String description;
        private final String id;

        private MobProperty(String id, String displayName, Material material, String description) {
            this.displayName = displayName;
            this.material = material;
            this.description = description;
            this.id = id;
        }
    }
}
