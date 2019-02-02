package at.lorenz.wow;

import at.lorenz.wow.command.ClassesCommand;
import at.lorenz.wow.command.PingCommand;
import at.lorenz.wow.command.SetSelectCommand;
import at.lorenz.wow.command.SetSpawnCommand;
import at.lorenz.wow.listener.GlobalListener;
import at.lorenz.wow.manager.*;
import at.lorenz.wow.utils.InventoryMenu;
import at.lorenz.wow.utils.ItemBuilder;
import at.lorenz.wow.utils.Utils;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class WoWPlugin extends JavaPlugin implements Listener {

    private MenuManager menuManager;
    private CreativeManager creativeManager;
    private SelectManager selectManager;
    private UserManager userManager;
    private LocationManager locationManager;
    private RegionManager regionManager;
    private MobManager mobManager;
    private InGameMamager inGameMamager;

    @Override
    public void onEnable() {

//        System.out.println("hallo");
//        System.out.println("hallo");
//        System.out.println("hallo");
//        System.out.println("hallo");
//        System.out.println("hallo");
//        System.out.println("hallo");
//        System.out.println("hallo");
//        System.out.println("hallo");
//        System.out.println("hallo");
//        System.out.println("hallo");
//        System.out.println("hallo");
//        System.out.println("hallo");
//        System.out.println("hallo");
//        System.out.println("hallo");
//        System.out.println("hallo");
//        System.out.println("hallo");
//        System.out.println("hallo");
//        System.out.println("hallo");
//        System.out.println("hallo");
//        System.out.println("hallo");
//        System.out.println("hallo");
        getCommand("ping").setExecutor(new PingCommand(this));
        getCommand("setselect").setExecutor(new SetSelectCommand(this));
        getCommand("setspawn").setExecutor(new SetSpawnCommand(this));
        getCommand("classes").setExecutor(new ClassesCommand(this));

        Bukkit.getPluginManager().registerEvents(new GlobalListener(this), this);
        Bukkit.getPluginManager().registerEvents(this, this);

        menuManager = new MenuManager(this);
        creativeManager = new CreativeManager(this);
        selectManager = new SelectManager(this);
        userManager = new UserManager(this);
        locationManager = new LocationManager(this);
        regionManager = new RegionManager(this);
        mobManager = new MobManager(this);
        inGameMamager = new InGameMamager(this);

        locationManager.loadConfig();
        mobManager.loadConfig();
        regionManager.loadConfig();

        for (Player player : Bukkit.getOnlinePlayers()) {
            userManager.handleJoin(player);
        }
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                int ping = Utils.getPing(player);
                player.setPlayerListName(player.getName() + " " + ping);
            }
        }, 20, 20);
    }

    @Override
    public void onDisable() {
        locationManager.saveConfig();
        regionManager.saveConfig();
        mobManager.saveConfig();
        for (Player player : Bukkit.getOnlinePlayers()) {
            userManager.handleQuit(player);
        }
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof Player) continue;
                entity.remove();
            }
        }
    }

    private Map<Player, Consumer<String>> chatStrings = new HashMap<>();

    @EventHandler
    void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (chatStrings.containsKey(player)) {
            chatStrings.get(player).accept(event.getMessage());
            event.setCancelled(true);
            chatStrings.remove(player);
        }
    }

    public void getStringFromChat(Player player, Consumer<String> consumer) {
        chatStrings.put(player, consumer);
    }

    public void openSelectMaterialInventory(String title, Player player, List<Material> materials, Consumer<Material> result) {
        player.closeInventory();
//        openSelectMaterialInventory(player, result, 0, MaterialSortType.NUMBER);
        openSelectMaterialInventory(title, player, materials, result, 0, null);
    }

    //    public void openSelectMaterialInventory(Player player, Consumer<Material> result, int page, MaterialSortType sortType) {
    public void openSelectMaterialInventory(String title, Player player, List<Material> materials, Consumer<Material> result, int page, String searchTerm) {
//        LinkedList<Material> materials = new LinkedList<>();
        int x = 0;
        ArrayList<Material> help = new ArrayList<>(materials);
        materials.clear();
        for (Material material : help) {
            if (material == Material.AIR) continue;
            if (material == Material.BARRIER) continue;

            if (material == Material.BEETROOTS) continue;
            if (material == Material.CARROTS) continue;
            if (material == Material.BUBBLE_COLUMN) continue;
            if (material == Material.CAVE_AIR) continue;
            if (material == Material.VOID_AIR) continue;
            if (material == Material.END_GATEWAY) continue;
            if (material == Material.END_PORTAL) continue;
            if (material == Material.FIRE) continue;
            if (material == Material.FROSTED_ICE) continue;
            if (material == Material.KELP_PLANT) continue;
            if (material == Material.COCOA) continue;
            if (material == Material.LIGHT_GRAY_STAINED_GLASS_PANE) continue;
            if (material == Material.LAVA) continue;
            if (material == Material.POTATOES) continue;
            if (material == Material.MOVING_PISTON) continue;
            if (material == Material.NETHER_PORTAL) continue;
            if (material == Material.PISTON_HEAD) continue;
            if (material == Material.REDSTONE_WIRE) continue;
            if (material == Material.TRIPWIRE) continue;
            if (material == Material.WATER) continue;
            if (material == Material.TALL_SEAGRASS) continue;
            if (material == Material.ATTACHED_PUMPKIN_STEM) continue;
            if (material == Material.ATTACHED_MELON_STEM) continue;
            if (material == Material.MELON_STEM) continue;
            if (material == Material.WALL_SIGN) continue;
            if (material.name().contains("POTTED_")) continue;
            if (material.name().contains("_WALL_BANNER")) continue;
            if (material.name().contains("_WALL_FAN")) continue;
            if (material.name().contains("WALL_TORCH")) continue;
            if (material.name().contains("_WALL_SKULL")) continue;
            if (material.name().contains("_WALL_HEAD")) continue;
            materials.add(material);
            x++;
        }

        if (searchTerm != null) {
            help = new ArrayList<>(materials);
            materials.clear();

            List<Material> exactMatches = new ArrayList<>();
            List<Material> normalMatches = new ArrayList<>();
            for (Material material : help) {
                String string = getName(material);
                if (string.startsWith(searchTerm)) {
                    exactMatches.add(material);
                } else if (string.contains(searchTerm)) {
                    normalMatches.add(material);
                }
            }

            materials.addAll(exactMatches);
            materials.addAll(normalMatches);

        }


//        if (sortType == MaterialSortType.NUMBER) {
//            LinkedHashMap<Material, Integer> map = new LinkedHashMap<>();
//            for (Material material : materials) {
//                int id = getMaterialId(material);
//                map.put(material, id);
//            }
//            materials.clear();
//            materials.addAll(Utils.sort(map).keySet());
//        }

//        if (page < 0) {
//            openSelectMaterialInventory(player, result, page + 1, searchTerm);
//            return;
//        }

        int itemsPerPage = 8 * 6;

        InventoryMenu menu = getMenuManager().create(player, 6);

        int maxPage = materials.size() / itemsPerPage;

        int end = itemsPerPage * (page + 1);
        if (end > materials.size()) {
            end = materials.size();
        } else {
            menu.addItem(8 + 9, ItemBuilder.material(Material.PAPER).name("§f->").lore(
                    "§eLeft click §8- §71 step forward", "" +
                            "§eRight click §8- §75 steps forward",
                    "§eShift + Click §8- §7To the end").build(), event -> {
                event.setCancelled(true);
                if (event.getAction() == InventoryAction.PICKUP_ALL) {
                    openSelectMaterialInventory(title, player, materials, result, page + 1, searchTerm);
                } else if (event.getAction() == InventoryAction.PICKUP_HALF) {
                    int newPage = page + 5;
                    if (newPage > maxPage) {
                        newPage = maxPage;
                    }
                    openSelectMaterialInventory(title, player, materials, result, newPage, searchTerm);
                } else if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                    openSelectMaterialInventory(title, player, materials, result, maxPage, searchTerm);
                }
            });
        }

        if (page > 0) {


            menu.addItem(8 + 9 * 2, ItemBuilder.material(Material.PAPER).name("§f<-").lore(
                    "§eLeft click §8- §71 step back", "" +
                            "§eRight click §8- §75 steps back",
                    "§eShift + Click §8- §7To the beginning").build(), event -> {
                event.setCancelled(true);
                if (event.getAction() == InventoryAction.PICKUP_ALL) {
                    openSelectMaterialInventory(title, player, materials, result, page - 1, searchTerm);
                } else if (event.getAction() == InventoryAction.PICKUP_HALF) {
                    int newPage = page - 5;
                    if (newPage < 0) {
                        newPage = 0;
                    }
                    openSelectMaterialInventory(title, player, materials, result, newPage, searchTerm);
                } else if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                    openSelectMaterialInventory(title, player, materials, result, 0, searchTerm);
//                } else {
//                    player.sendMessage(event.getAction() + "");
                }
            });

//            menu.addItem(8 + 9 * 2, ItemBuilder.material(Material.PAPER).name("§f<-").build(), () -> {
////                openSelectMaterialInventory(player, result, page - 1, sortType);
//                openSelectMaterialInventory(player, result, page - 1, searchTerm);
//            });
        }

//        String sortName = "Sort: " + sortType.name().toLowerCase();
//        menu.addItem(8 + 9 * 4, ItemBuilder.material(Material.BOOK).name(sortName).build(), () -> {
//            if (sortType == MaterialSortType.NAME) {
//                openSelectMaterialInventory(player, result, 0, MaterialSortType.NUMBER);
//            } else {
//                openSelectMaterialInventory(player, result, 0, MaterialSortType.NAME);
//            }
//        });
        String filterName = "§fName filter";
        if (searchTerm != null) {
            filterName = filterName + ": §e" + searchTerm;
        }
        menu.addItem(8 + 9 * 4, ItemBuilder.material(Material.COMPASS).name(filterName).build(), () -> {
            player.sendMessage("Type search term in the chat or type 'cancel' to cancel search");
            player.closeInventory();
            getStringFromChat(player, term -> {
                if (term.equalsIgnoreCase("cancel")) {
                    openSelectMaterialInventory(title, player, materials, result, page, searchTerm);
                } else {
                    openSelectMaterialInventory(title, player, materials, result, 0, term);
                }
            });
        });
        if (searchTerm != null) {
            menu.addItem(8 + 9 * 5, ItemBuilder.material(Material.REDSTONE_BLOCK).name("§cRemove filter").build(), () -> {
                openSelectMaterialInventory(title, player, materials, result, 0, null);
            });
            if (materials.isEmpty()) {
                menu.addItem(4 + 9 * 2, ItemBuilder.material(Material.BUCKET).name("§cNo material found.").lore("§7Try to remove the filter").build());
            }
        }


        menu.addItem(8, WowItems.BACK, () -> {
            result.accept(null);
        });


        int index = 0;
        for (int i = page * itemsPerPage; i < end; i++) {
            Material material = materials.get(i);
//            String displayName = material.name();
            String displayName = getName(material);
            if (searchTerm != null) {
                displayName = displayName.replaceAll(Pattern.quote(searchTerm), "§e" + searchTerm + "§f");
            }
//            System.out.println(displayName);
            ItemBuilder builder = ItemBuilder.material(material).name("§f" + displayName);
//            if (i != 0) {
//                builder.lore("§fbefore: " + materials.get(i - 1).name());
//            }
            menu.addItem(index, builder.build(), () -> {
                result.accept(material);
            });
//            System.out.println("index " + index);
            index++;
            if (index % 9 == 8) {
                index++;
            }
        }
//        if (index == 0) {
//            openSelectMaterialInventory(player, result, page - 1, searchTerm);
//            return;
//        }
        menu.open(title + "  | page " + (page + 1) + "/" + (maxPage + 1));
    }

    private String getName(Material material) {
        String string = material.name().toLowerCase();
        string = string.replaceAll(Pattern.quote("_"), " ");
        return string;
    }

    private int getMaterialId(Material material) {
        int i = 0;
        for (Material value : Material.values()) {
            if (value == material) {
                return i;
            }
            i++;
        }
        throw new RuntimeException("Impossible");
    }

//    enum MaterialSortType {
//        NUMBER, NAME
//    }
}
