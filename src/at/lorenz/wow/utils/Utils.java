package at.lorenz.wow.utils;

import at.lorenz.wow.WoWPlugin;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftMob;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

public class Utils {

    public static void sendActionBar(Player player, String text) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(text));
    }

    public static String getCurrentTime() {
        long time = System.currentTimeMillis();
        return new SimpleDateFormat("HH:mm", Locale.ENGLISH).format(new Time(time));
    }

    public static int getPing(Player player) {
        return ((CraftPlayer) player).getHandle().ping;
    }

    public static String locationToString(Location location) {
        if (location == null) {
            return "§cno location";
        }
        int x = (int) location.getX();
        int y = (int) location.getY();
        int z = (int) location.getZ();
        return x + ";" + y + ";" + z;
    }

    public static boolean locationInAABBCC(Location location, Location a, Location b) {
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();

        double minX = Math.min(a.getX(), b.getX());
        double minY = Math.min(a.getY(), b.getY());
        double minZ = Math.min(a.getZ(), b.getZ());

        double maxX = Math.max(a.getX(), b.getX()) + 1;
        double maxY = Math.max(a.getY(), b.getY()) + 1;
        double maxZ = Math.max(a.getZ(), b.getZ()) + 1;

        return x > minX && x <= maxX && y > minY && y <= maxY && z > minZ && z <= maxZ;
    }

    public static Location randomLocationInCircle(Location center, double radius) {
        Location location = center.clone();
        ThreadLocalRandom random = ThreadLocalRandom.current();
        double a = random.nextDouble(radius * 2);
        double b = random.nextDouble(radius * 2);
        location.add(new Vector(a - radius, 0, b - radius));
        if (center.distance(location) < radius) {
            return location;
        }
        return randomLocationInCircle(center, radius);
    }

    public static Location randomLocationInCircleWithHole(Location center, double radius, double hole) {
        Location location = randomLocationInCircle(center, radius);
        if (location.distance(center) > hole) {
            return location;
        }
        return randomLocationInCircleWithHole(center, radius, hole);
    }

    public static <T> T randomElementFromList(List<T> list) {
        if (list.isEmpty()) {
            throw new RuntimeException("List is empty");
        }
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int index = random.nextInt(list.size());
        return list.get(index);
    }

    public static boolean isSimilar(ItemStack a, ItemStack b) {
        if (a.getType() != b.getType()) {
            return false;
        }
        ItemMeta aMeta = a.getItemMeta();
        ItemMeta bMeta = b.getItemMeta();
        if (aMeta == null) return false;
        if (bMeta == null) return false;
        return Objects.equals(aMeta.getDisplayName(), bMeta.getDisplayName());
    }

//    private static Map<Player, List<String>> map = new HashMap<>();
//
//    public static void setSidebar(Player player, String text) {
//        ScoreboardManager manager = Bukkit.getScoreboardManager();
//        Scoreboard scoreboard;
//        Objective objective;
//        if (manager.getMainScoreboard() != player.getScoreboard()) {
//            scoreboard = player.getScoreboard();
//            objective = scoreboard.getObjective("text");
//            for (String s : map.get(player)) {
//                scoreboard.resetScores(s);
//            }
//        } else {
//            scoreboard = manager.getNewScoreboard();
//            player.setScoreboard(scoreboard);
//            objective = scoreboard.registerNewObjective("test", "dummy");
//            objective.setDisplaySlot(DisplaySlot.SIDEBAR);
//        }
//
//        objective.setDisplayName("abc");
//
//        objective.getScore(text).setScore(0);
//
//        ArrayList<String> list = new ArrayList<>();
//        list.add(text);
//        map.put(player, list);
//    }

    public static <K, N extends Number> Map<K, N> sort(LinkedHashMap<K, N> map) {
        return sort(map, false);
    }

    public static <K, N extends Number> Map<K, N> sort(LinkedHashMap<K, N> map, boolean ascending) {
        LinkedHashMap<K, N> help = new LinkedHashMap<>(map);
        map.clear();
        while (!help.isEmpty()) {
            K key = null;
            N maxNum = null;
            for (Map.Entry<K, N> entry : help.entrySet()) {
                N num = entry.getValue();
                if (maxNum == null || ascending == num.doubleValue() > maxNum.doubleValue()) {
                    maxNum = num;
                    key = entry.getKey();
                }
            }
            help.remove(key);
            map.put(key, maxNum);
        }
        return map;
    }

    public static void clear(Player player) {
        player.getInventory().clear();
        player.setFoodLevel(20);
        player.setFlying(false);
        player.setAllowFlight(false);
        player.setHealth(20);
        removeTarget(player);

        player.setVelocity(new Vector(0, 0, 0));
//        Bukkit.getScheduler().runTaskLater(WoWPlugin.getPlugin(WoWPlugin.class), () -> {
//            player.setVelocity(new Vector(0, 0, 0));
//        }, 1);
    }

    private static void removeTarget(Player player) {
        //        System.out.println("start worlds");
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {

                CraftEntity craftEntity = (CraftEntity) entity;
                if (craftEntity instanceof CraftMob) {
                    CraftMob craftMob = (CraftMob) craftEntity;
//                    System.out.println("target");

                    CraftLivingEntity target = craftMob.getTarget();
                    if (target != null) {
//                        System.out.println("target not null");
                        if (target.getUniqueId().equals(player.getUniqueId())) {
                            craftMob.setTarget(null);
//                            System.out.println("removed target");
                        }
                    }
                }
            }
        }
    }
}
