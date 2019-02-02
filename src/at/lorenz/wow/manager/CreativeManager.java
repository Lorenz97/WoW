package at.lorenz.wow.manager;

import at.lorenz.wow.*;
import at.lorenz.wow.utils.ItemBuilder;
import at.lorenz.wow.utils.Utils;
import at.lorenz.wow.utils.scoreboard.SidebarBuilder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class CreativeManager implements Listener {

    private final WoWPlugin plugin;
    private Map<Player, CreativeType> creativeTypes = new HashMap<>();
    private Map<Player, Region> newRegion = new HashMap<>();

    public CreativeManager(WoWPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);

//        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
//            for (Player player : Bukkit.getOnlinePlayers()) {
//                if (plugin.getUserManager().getUser(player).getUserState() == UserState.CREATIVE) {
////                    updateActionBar(player);
//                }
//            }
//        }, 40, 40);
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (plugin.getUserManager().getUser(player).getUserState() == UserState.CREATIVE) {
                    SidebarBuilder builder = new SidebarBuilder();
                    CreativeType creativeType = creativeTypes.get(player);
                    builder.setTitle(getCreativeName(creativeType));
                    if (creativeType == CreativeType.REGION) {
                        builder.add("");
                        List<Region> regions = plugin.getRegionManager().getRegionAt(player.getLocation());
                        if (regions.isEmpty()) {
                            builder.add("No Region");
                            builder.add("");
                            player.getInventory().setItem(3, null);
                            player.getInventory().setItem(4, null);
                            player.getInventory().setItem(5, null);
                            player.getInventory().setItem(6, null);
                            player.getInventory().setItem(7, null);
                        } else {
                            int i = 0;
                            for (Region region : regions) {
                                ItemStack itemStack = ItemBuilder.material(Material.WHITE_WOOL).name(region.getName()).build();
                                player.getInventory().setItem(3 + i, itemStack);
                                i++;

                                builder.add(region.getName());
                                region.getPossibleMobs().forEach((mobType, integer) -> {
                                    builder.add(integer + "x " + mobType.getDisplayName());
                                });
                                builder.add("");
                            }
                        }
                    } else if (creativeType == CreativeType.REGION_NEW) {
                        Region region = newRegion.get(player);
                        builder.add("");
                        builder.add("= New region =");
                        builder.add("Name: " + (region.getName() == null ? "§cno name" : region.getName()));
                        builder.add("A: " + Utils.locationToString(region.getA()));
                        builder.add("B: " + Utils.locationToString(region.getB()));
                        builder.add("");
                    }
                    builder.add("                                  ");
                    builder.add(Utils.getCurrentTime());
                    builder.send(player);
                }
            }
        }, 2, 2);
    }

    private void updateActionBar(Player player) {
        CreativeType creativeType = creativeTypes.get(player);
        Utils.sendActionBar(player, getCreativeName(creativeType));
    }

    private String getCreativeName(CreativeType creativeType) {
        switch (creativeType) {
            case NONE:
                return "null";
            case DEFAULT:
                return "CREATIVE";
            case BUILD:
                return "CREATIVE > Build";
            case REGION:
                return "CREATIVE > Region";
            case REGION_NEW:
                return "CREATIVE > Region > Create new";
        }
        throw new RuntimeException("impossible");
    }

    enum CreativeType {
        NONE, DEFAULT, REGION, BUILD, REGION_NEW;
    }

    @EventHandler
    void onPlayerInteract(PlayerInteractEvent event) {
//        System.out.println(event.getTarget() + " - " + event.getAction() + " " + event.getClickedBlock());
        Player player = event.getPlayer();
        WowUser user = plugin.getUserManager().getUser(player);
        if (user.getUserState() != UserState.CREATIVE) return;
        CreativeType creativeType = creativeTypes.get(player);
        Action action = event.getAction();
//        event.setCancelled(true);

        ItemStack itemStack = player.getItemInHand();
        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            if (Utils.isSimilar(itemStack, WowItems.CREATIVE_EXIT)) {
                exitCreative(player);
            }
            if (creativeType == CreativeType.DEFAULT) {
                if (Utils.isSimilar(itemStack, WowItems.CREATIVE_REGION)) {
                    selectRegion(player);
                    event.setCancelled(true);
                }
                if (Utils.isSimilar(itemStack, WowItems.CREATIVE_BUILD)) {
                    selectBuild(player);
                    event.setCancelled(true);
                }
                if (Utils.isSimilar(itemStack, WowItems.CREATIVE_MOBS)) {
                    InventoryView openInventory = player.getOpenInventory();
                    event.setCancelled(true);
                    if (openInventory != null) {
                        if (openInventory.getTitle().startsWith("Edit mob ")) {
                            return;
                        }
                    }
                    selectMobs(player);
                    return;
                }
            }
            if (creativeType == CreativeType.BUILD) {
                if (Utils.isSimilar(itemStack, WowItems.CREATIVE)) {
                    enterCreative(player);
                    event.setCancelled(true);
                }
            }
            if (creativeType == CreativeType.REGION) {
                if (Utils.isSimilar(itemStack, WowItems.CREATIVE)) {
                    enterCreative(player);
                    event.setCancelled(true);
                }
                if (Utils.isSimilar(itemStack, WowItems.CREATIVE_REGION_CREATE_NEW)) {
                    createNewRegion(player);
                    event.setCancelled(true);
                }
                if (Utils.isSimilar(itemStack, WowItems.CREATIVE_REGION_BOOK)) {
                    plugin.getRegionManager().openRegionBook(player);
                    event.setCancelled(true);
                }
                if (itemStack.getType() == Material.WHITE_WOOL) {
                    String name = itemStack.getItemMeta().getDisplayName();
                    Region region = plugin.getRegionManager().getRegion(name);
                    plugin.getRegionManager().openRegionMenu(player, region);
                }
            }
            if (creativeType == CreativeType.REGION_NEW) {
                if (Utils.isSimilar(itemStack, WowItems.CREATIVE_REGION)) {
                    selectRegion(player);
                    event.setCancelled(true);
                }
            }
        }
        if (creativeType == CreativeType.REGION_NEW) {
            Region region = newRegion.get(player);
            if (Utils.isSimilar(itemStack, WowItems.CREATIVE_REGION_SET_1)) {
                if (action == Action.LEFT_CLICK_BLOCK) {
                    region.setA(event.getClickedBlock().getLocation());
                    player.sendMessage("1 set on block");
                    event.setCancelled(true);
                } else if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
                    player.sendMessage("1 set on location");
                    region.setA(player.getLocation().getBlock().getLocation());
                    event.setCancelled(true);
                }
            }
            if (Utils.isSimilar(itemStack, WowItems.CREATIVE_REGION_SET_2)) {
                if (action == Action.LEFT_CLICK_BLOCK) {
                    region.setB(event.getClickedBlock().getLocation());
                    player.sendMessage("2 set on block");
                    event.setCancelled(true);
                } else if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
                    player.sendMessage("2 set on location");
                    region.setB(player.getLocation().getBlock().getLocation());
                    event.setCancelled(true);
                }
            }
            if (Utils.isSimilar(itemStack, WowItems.CREATIVE_REGION_CONFIRM_NEW)) {
                if (action != Action.PHYSICAL) {
                    event.setCancelled(true);
                    if (plugin.getRegionManager().createRegion(player, region)) {
                        newRegion.remove(player);
                        selectRegion(player);
                    }
                }
            }
            if (Utils.isSimilar(itemStack, WowItems.CREATIVE_REGION_NAME)) {
                player.sendMessage("Type the name of the Region in the chat");
                plugin.getStringFromChat(player, name -> {
                    region.setName(name);
                    player.sendMessage("Region name set to '" + name + "'");
                });
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (!event.getHand().equals(EquipmentSlot.HAND)) return;

        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();
        WowUser user = plugin.getUserManager().getUser(player);
        if (user.getUserState() == UserState.CREATIVE) {
            CreativeType creativeType = creativeTypes.get(player);
            if (creativeType == CreativeType.DEFAULT) {
                if (Utils.isSimilar(player.getItemInHand(), WowItems.CREATIVE_MOBS)) {
                    WowMob mob = plugin.getMobManager().getMobFromEntity(entity);
                    if (mob != null) {
                        plugin.getMobManager().editMobType(player, mob.getMobType(), false);
                        event.setCancelled(true);
                    }
                }
            }
        }

    }

    private void selectMobs(Player player) {
        plugin.getMobManager().openMobInventory(player);
    }

    private void createNewRegion(Player player) {
        setMode(player, CreativeType.REGION_NEW);
        player.setGameMode(GameMode.CREATIVE);

        PlayerInventory inventory = player.getInventory();
        inventory.clear();
        inventory.setItem(8, WowItems.CREATIVE_REGION);
        inventory.setItem(1, WowItems.CREATIVE_REGION_SET_1);
        inventory.setItem(2, WowItems.CREATIVE_REGION_SET_2);
        inventory.setItem(3, WowItems.CREATIVE_REGION_NAME);
        inventory.setItem(4, WowItems.CREATIVE_REGION_CONFIRM_NEW);
        player.getInventory().setHeldItemSlot(0);

        newRegion.put(player, new Region());
    }

    private void selectBuild(Player player) {
        player.setGameMode(GameMode.CREATIVE);
        setMode(player, CreativeType.BUILD);
        PlayerInventory inventory = player.getInventory();
        inventory.clear();
        inventory.setItem(8, WowItems.CREATIVE);
        player.getInventory().setHeldItemSlot(0);
    }

    private void selectRegion(Player player) {
        setMode(player, CreativeType.REGION);
        player.setGameMode(GameMode.ADVENTURE);
        nakeFly(player);
        PlayerInventory inventory = player.getInventory();
        inventory.clear();

        inventory.setItem(1, WowItems.CREATIVE_REGION_CREATE_NEW);
        inventory.setItem(2, WowItems.CREATIVE_REGION_BOOK);

        inventory.setItem(8, WowItems.CREATIVE);
        player.getInventory().setHeldItemSlot(0);
    }

    public void enterCreative(Player player) {
        player.setGameMode(GameMode.ADVENTURE);
        nakeFly(player);

        WowUser user = plugin.getUserManager().getUser(player);
        user.setUserState(UserState.CREATIVE);
        PlayerInventory inventory = player.getInventory();
        player.setFlying(true);

        inventory.clear();
        inventory.setItem(8, WowItems.CREATIVE_EXIT);

        inventory.setItem(1, WowItems.CREATIVE_BUILD);
        inventory.setItem(2, WowItems.CREATIVE_REGION);
        inventory.setItem(3, WowItems.CREATIVE_MOBS);
        player.getInventory().setHeldItemSlot(0);

        setMode(player, CreativeType.DEFAULT);
    }

    private void nakeFly(Player player) {
        player.setAllowFlight(true);
        if (!player.isOnGround()) {
            player.setFlying(true);
        }
    }

    private void setMode(Player player, CreativeType creativeType) {
        creativeTypes.put(player, creativeType);
//        updateActionBar(player);
    }

    private void exitCreative(Player player) {
        player.setFlying(false);
        player.setAllowFlight(false);
        creativeTypes.remove(player);
        Utils.sendActionBar(player, "exit creative");
        WowUser user = plugin.getUserManager().getUser(player);

        user.setUserState(UserState.SELECT);
        plugin.getSelectManager().putIntoSelect(player);
        player.getInventory().setHeldItemSlot(0);
    }

    @EventHandler
    void onBlockBreak(BlockBreakEvent event) {
        if (creativeTypes.getOrDefault(event.getPlayer(), CreativeType.NONE) == CreativeType.BUILD) {
            event.setCancelled(false);
        }
    }

    @EventHandler
    void onBlockPlace(BlockPlaceEvent event) {
        if (creativeTypes.getOrDefault(event.getPlayer(), CreativeType.NONE) == CreativeType.BUILD) {
            event.setCancelled(false);
        }
    }

//    @EventHandler
//    void onSetTarget(EntityTargetEvent event) {
//        Entity entity = event.getEntity();
//        Entity target = event.getTarget();
//        EntityTargetEvent.TargetReason reason = event.getReason();
//        Bukkit.broadcastMessage("EntityTargetEvent: " + entity.getName() + " targets " + target + " bc of " + reason);
//    }

    @EventHandler
    void onSetTarget(EntityTargetLivingEntityEvent event) {
        Entity entity = event.getEntity();
        WowMob mob = plugin.getMobManager().getMobFromEntity(entity);
        if (mob == null) return;
        Entity target = event.getTarget();
        if (target instanceof Player) {
            Player player = (Player) target;
            if (plugin.getUserManager().getUser(player).getUserState() == UserState.INGAME) {
                if (player.getLocation().distance(entity.getLocation()) > mob.getMobType().getTriggerRange()) {
                    event.setCancelled(true);
                    return;
                }
            } else {
                event.setCancelled(true);
                return;
            }
//            if (mob.getMobType().getAggressionType() == MobAggressionType.PASSIVE) {
//                event.setCancelled(true);
//            }
        }
    }
}
