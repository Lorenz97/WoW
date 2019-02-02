package at.lorenz.wow.manager;

import at.lorenz.wow.*;
import at.lorenz.wow.utils.InventoryMenu;
import at.lorenz.wow.utils.ItemBuilder;
import at.lorenz.wow.utils.Utils;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class RegionManager {

    private final WoWPlugin plugin;

    private final File configFile = new File("plugins/WoW/regions.txt");

    private List<Region> regions = new ArrayList<>();

    public RegionManager(WoWPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean createRegion(Player player, Region region) {

        if (region.getA() == null || region.getB() == null) {
            player.sendMessage("§cFirst set both corners!");
            return false;
        }
        if (region.getName() == null) {
            player.sendMessage("§cFirst define a name!");
            return false;
        }
        Region oldRegion = getRegion(region.getName());
        if (oldRegion != null) {
            player.sendMessage("§cThere exists already a region '" + oldRegion.getName() + "'");
            return false;
        }

        regions.add(region);
        player.sendMessage("Region '" + region.getName() + "' created");
        return true;
    }

    public Region getRegion(String name) {
        for (Region region : regions) {
            if (region.getName().equalsIgnoreCase(name)) {
                return region;
            }
        }
        return null;
    }

    public List<Region> getRegionAt(Location location) {
        List<Region> list = new ArrayList<>();
        for (Region region : regions) {
            if (isInRegion(region, location)) {
                list.add(region);
            }
        }
        return list;
    }

    private boolean isInRegion(Region region, Location location) {
        return Utils.locationInAABBCC(location, region.getA(), region.getB());
    }

    public void loadConfig() {
        if (configFile.isFile()) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            for (String label : config.getKeys(false)) {
                Region region = new Region();
                region.setName(label);
                region.setA((Location) config.get(label + ".a"));
                region.setB((Location) config.get(label + ".b"));

                ConfigurationSection section = config.getConfigurationSection(label + ".possible-mobs");
                if (section != null) {
                    Set<String> keys = section.getKeys(false);
                    if (keys != null) {
                        for (String key : keys) {
                            int value = config.getInt(label + ".possible-mobs." + key);
                            MobType mobType = plugin.getMobManager().getMobTypebyId(Integer.valueOf(key));
                            region.getPossibleMobs().put(mobType, value);
                        }
                    }
                }

                regions.add(region);
            }
        }
    }

    public void saveConfig() {
        YamlConfiguration config = new YamlConfiguration();
        for (Region region : regions) {
            String name = region.getName();
            config.set(name + ".a", region.getA());
            config.set(name + ".b", region.getB());

            for (Map.Entry<MobType, Integer> entry : region.getPossibleMobs().entrySet()) {
                MobType mobType = entry.getKey();
                Integer amount = entry.getValue();
                config.set(name + ".possible-mobs." + mobType.getId(), amount);
            }
        }
        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void openRegionBook(Player player) {
        InventoryMenu menu = plugin.getMenuManager().create(player, 3);

        menu.addItem(4, ItemBuilder.material(Material.BOOK).name("Region Book").build());
        menu.addItem(8, WowItems.BACK, () -> {
            player.closeInventory();
        });

        int index = 9;
        for (Region region : regions) {
            ItemStack itemStack = ItemBuilder.material(Material.WHITE_WOOL).name(region.getName()).build();
            menu.addItem(index, itemStack, () -> {
                openRegionMenu(player, region);
            });
            index++;
        }

        menu.open("Region Book");
    }

    public void openRegionMenu(Player player, Region region) {
        InventoryMenu menu = plugin.getMenuManager().create(player, 3);

        menu.addItem(2, ItemBuilder.material(Material.NAME_TAG).name("§bRename region").build(), () -> {
            player.closeInventory();
            player.sendMessage("Write a new name for region '" + region.getName() + "' in the chat. Type 'cancel' to cancel");
            plugin.getStringFromChat(player, string -> {
                if (string.equals("cancel")) {
                    player.sendMessage("Cancelled rename");
                    openRegionMenu(player, region);
                } else {
                    player.sendMessage("renamed region '" + region.getName() + "' to '" + string + "'");
                    region.setName(string);
                    openRegionMenu(player, region);
                }
            });
        });
        menu.addItem(4, ItemBuilder.material(Material.WHITE_WOOL).name(region.getName()).build());
        menu.addItem(6, ItemBuilder.material(Material.LAVA_BUCKET).name("§4Delete region").build(), () -> {
            plugin.getMenuManager().confirmAction(player, "Delete region '" + region.getName() + "'?", "Yes, delete", "No, cancel", answer -> {
                if (answer) {
                    regions.remove(region);
                    player.sendMessage("Deleted region '" + region.getName() + "'");
                    openRegionBook(player);

                    for (MobType mobType : region.getPossibleMobs().keySet()) {
                        for (WowMob mob : plugin.getMobManager().getMobsInRegion(region, mobType)) {
                            mob.getEntity().remove();
                            mob.remove();
                        }
                    }
                } else {
                    openRegionMenu(player, region);
                }
            });
        });
        menu.addItem(8, WowItems.BACK, () -> {
            openRegionBook(player);
        });

        int index = 9;
        for (Map.Entry<MobType, Integer> entry : region.getPossibleMobs().entrySet()) {
            MobType mobType = entry.getKey();
            int amount = entry.getValue();
            Material material = mobType.getDisplayMaterial();
            String displayName = "§f" + amount + "x " + mobType.getDisplayName();
            ItemStack build = ItemBuilder.material(material).name(displayName).amount(amount).build();
            menu.addItem(index, build, () -> {
                player.closeInventory();
                player.sendMessage("Change the amount of " + mobType.getName() + " by typing a number, remove the mob by typing 'delete' or cancel by typing 'cancel");
                plugin.getStringFromChat(player, string -> {
                    if (string.equals("cancel")) {
                        player.sendMessage("Cancelled amount change");
                        return;
                    }
                    if (string.equals("delete")) {
                        region.getPossibleMobs().remove(mobType);
                        for (WowMob mob : plugin.getMobManager().getMobsInRegion(region, mobType)) {
                            mob.getEntity().remove();
                            mob.remove();
                        }
                        player.sendMessage("Removed " + mobType.getName() + " from region " + region.getName());
                        openRegionMenu(player, region);
                        return;
                    }
                    try {
                        int value = Integer.valueOf(string);

                        List<WowMob> list = plugin.getMobManager().getMobsInRegion(region, mobType);
                        int tooMuch = list.size() - value;
                        if (tooMuch > 0) {
                            for (int i = 0; i < tooMuch; i++) {
                                WowMob mob = Utils.randomElementFromList(list);
                                list.remove(mob);
                                mob.getEntity().remove();
                                mob.remove();
                            }
                        }

                        region.getPossibleMobs().put(mobType, value);
                        player.sendMessage("Changed mob amount from " + mobType.getName() + " in " + region.getName() + " to " + value);
                        openRegionMenu(player, region);
                    } catch (NumberFormatException e) {
                        player.sendMessage("Invalid input. Cancelled");
                    }
                });
            });
            index++;
        }
        menu.addItem(index, ItemBuilder.material(Material.GREEN_WOOL).name("Add mob").build(), () -> {
            String title = "Select mob for region " + region.getName();
            plugin.getMobManager().pickMobInventory(player, title, region.getPossibleMobs().keySet(), mobType -> {
                if (mobType == null) {
                    openRegionMenu(player, region);
                    return;
                }
                player.sendMessage("Type the amount for the mob " + mobType.getName() + " in the chat or type 'cancel' to cancel");
                player.closeInventory();
                plugin.getStringFromChat(player, string -> {
                    if (string.equals("cancel")) {
                        player.sendMessage("Cancelled amount change");
                        return;
                    }
                    try {
                        int value = Integer.valueOf(string);
                        region.getPossibleMobs().put(mobType, value);
                        player.sendMessage("Added " + value + "x " + mobType.getName() + " to " + region.getName());
                        openRegionMenu(player, region);
                    } catch (NumberFormatException e) {
                        player.sendMessage("Invalid input. Cancelled");
                    }
                });
            });
        });

        menu.open(region.getName());
    }

    public void removeFromAllRegions(MobType mobType) {
        for (Region region : regions) {
            region.getPossibleMobs().remove(mobType);
        }
    }
}
