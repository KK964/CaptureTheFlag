package net.justminecraft.net.minigames.capturetheflag;

import net.justminecraft.minigames.minigamecore.Game;
import net.justminecraft.minigames.minigamecore.MG;
import net.justminecraft.minigames.minigamecore.Minigame;
import net.justminecraft.minigames.minigamecore.worldbuffer.WorldBuffer;
import org.bukkit.*;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Team;

import java.io.File;
import java.util.*;

public class CaptureTheFlag extends Minigame implements Listener {
    private static CaptureTheFlag captureTheFlag;
    private static File DATA_FOLDER;
    private static File SCHEMATIC_FOLDER;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        captureTheFlag = this;
        DATA_FOLDER = getDataFolder();
        SCHEMATIC_FOLDER = new File(DATA_FOLDER.getPath() + System.getProperty("file.separator") + "schematics");
        SCHEMATIC_FOLDER.mkdir();

        MG.core().registerMinigame(this);
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("Enabled");
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabled");
    }

    @Override
    public int getMaxPlayers() {
        return 8;
    }
    @Override
    public int getMinPlayers() {
        return 2;
    }
    @Override
    public String getMinigameName() {
        return "CaptureTheFlag";
    }
    @Override
    public Game newGame() {
        return new CaptureTheFlagGame(this);
    }

    @Override
    public void startGame(Game game) {
        HashMap<Player, Integer> playerScore = new HashMap<>();
        CaptureTheFlagGame g = (CaptureTheFlagGame) game;
        Random random = new Random();

        Team red = g.scoreboard.registerNewTeam("red");
        Team blue = g.scoreboard.registerNewTeam("blue");

        Block redBanner = g.world.getBlockAt(g.redFlag);
        redBanner.setType(Material.STANDING_BANNER);
        Banner rB = (Banner) redBanner.getState();
        rB.setBaseColor(DyeColor.RED);
        rB.update();
        Block blueBanner = g.world.getBlockAt(g.blueFlag);
        blueBanner.setType(Material.STANDING_BANNER);
        Banner bB = (Banner) blueBanner.getState();
        bB.setBaseColor(DyeColor.BLUE);
        bB.update();

        Objective display = g.scoreboard.registerNewObjective("display", "dummy");

        red.setAllowFriendlyFire(false);
        red.setCanSeeFriendlyInvisibles(true);
        blue.setAllowFriendlyFire(false);
        blue.setCanSeeFriendlyInvisibles(true);

        display.setDisplaySlot(DisplaySlot.SIDEBAR);

        display.setDisplayName(ChatColor.YELLOW + ChatColor.BOLD.toString() + "CAPTURE THE FLAG");
        display.getScore(" ").setScore(5);
        display.getScore(ChatColor.RED + "Red: " + 5).setScore(4);
        display.getScore(ChatColor.BLUE + "Blue: " + 5).setScore(3);
        display.getScore(" ").setScore(2);
        display.getScore(ChatColor.YELLOW + "justminecraft.net").setScore(1);

        for(Player p : game.players) {
            PlayerScores playerScores = new PlayerScores(p);
            int games = playerScores.getGames();
            int wins = playerScores.getWins();
            int captures = playerScores.getCaptures();
            int losses = games - wins;
            int score = (games + (wins * 10) + (captures * 2)) - losses;
            g.games.put(p, games);
            g.wins.put(p, wins);
            g.captures.put(p, captures);
            p.setScoreboard(g.scoreboard);
            playerScore.put(p, score);
        }

        HashMap<Player, Integer> sortedMap = new HashMap<>(sortByValue(playerScore));

        ArrayList<Location> redLoc = new ArrayList<>(g.redSpawns);
        ArrayList<Location> blueLoc = new ArrayList<>(g.blueSpawns);

        int flip = 0;
        for(Player p : sortedMap.keySet()) {
            if(flip == 0) {
                Location location = redLoc.get(random.nextInt(redLoc.size()));
                location.setWorld(g.world);
                red.addEntry(p.getName());
                p.teleport(location);
                redLoc.remove(location);
                g.redTeam.add(p);
            } else {
                Location location = blueLoc.get(random.nextInt(blueLoc.size()));
                location.setWorld(g.world);
                blue.addEntry(p.getName());
                p.teleport(location);
                blueLoc.remove(location);
                g.blueTeam.add(p);
            }
            flip = flip == 1 ? 0 : 1;
        }
    }

    @Override
    public void generateWorld(Game game, WorldBuffer w) {
        CaptureTheFlagGame g = (CaptureTheFlagGame) game;
        String key = g.getMap();
        g.disablePvP = false;
        g.moneyPerDeath = 10;
        g.moneyPerWin = 20;
        g.disableBlockBreaking = true;
        g.disableBlockPlacing = true;
        g.disableHunger = true;

        Map m = new Map();
        Location l = new Location(g.world, 0, 64, 0);
        m.placeSchematic(w,l,key,g);
    }

    @EventHandler
    public void breakFlag(BlockBreakEvent e) {
        Player p = e.getPlayer();
        Game g = MG.core().getGame(p);
        if(g == null || g.minigame != this) return;
        CaptureTheFlagGame game = (CaptureTheFlagGame) g;
        boolean deleteBlock = false;
        String flagStolen = null;
        Location redFlag = game.redFlag;
        redFlag.setWorld(g.world);
        Location blueFlag = game.blueFlag;
        blueFlag.setWorld(g.world);
        Location blockBroken = e.getBlock().getLocation();
        if(game.redTeam.contains(p) && blockBroken.equals(blueFlag)) {
            ItemStack headBanner = new ItemStack(Material.BANNER, 1, (short) 4);
            p.getInventory().setHelmet(headBanner);
            deleteBlock = true;
            flagStolen = "Blue";
        }
        if(game.blueTeam.contains(p) && blockBroken.equals(redFlag)) {
            ItemStack headBanner = new ItemStack(Material.BANNER, 1, (short) 1);
            p.getInventory().setHelmet(headBanner);
            deleteBlock = true;
            flagStolen = "Red";
        }

        if(deleteBlock) {
            e.getBlock().setType(Material.AIR);
            game.flagCarrier.add(p);
            p.sendMessage(ChatColor.GREEN + "You stole " + flagStolen + "'s Flag! Make it back to your spawn to capture the flag!");
            p.playSound(p.getLocation(), Sound.LEVEL_UP, 1, 1);
            for(Player player : g.players) {
                if(player != p) {
                    if((game.redTeam.contains(player) && flagStolen.equals("Red")) || (game.blueTeam.contains(player) && flagStolen.equals("Blue"))) {
                        player.sendMessage(ChatColor.RED + "Your flag was stolen by " + p.getName() + "! Go get your flag back!");
                        player.playSound(player.getLocation(), Sound.BURP, 1, 1);
                    } else {
                        player.sendMessage(ChatColor.GREEN + p.getName() + " stole " + flagStolen + "'s flag, protect them!");
                        player.playSound(player.getLocation(), Sound.LEVEL_UP, 1, 1);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onMoveEvent(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        Game g = MG.core().getGame(p);
        if(g == null || g.minigame != this) return;
        CaptureTheFlagGame game = (CaptureTheFlagGame) g;
        if(!game.flagCarrier.contains(p)) return;
        Location to = e.getTo();
        Location l = to.getBlock().getLocation();
        l.setWorld(null);
        l.setY(l.getY() - 1);
        boolean capturedFlag = false;
        if(game.blueTeam.contains(p)) {
            if(game.blueSpawns.contains(l)) {
                game.redLives = game.redLives - 1;
                game.captures.replace(p, game.captures.get(p) + 1);
                capturedFlag = true;
            }
        }
        if(game.redTeam.contains(p)) {
            if(game.redSpawns.contains(l)) {
                game.blueLives = game.blueLives - 1;
                game.captures.replace(p, game.captures.get(p) + 1);
                capturedFlag = true;
            }
        }
        if(capturedFlag) {
            p.getInventory().setHelmet(new ItemStack(Material.AIR));
            game.flagCarrier.remove(p);
            for(Player player : game.redTeam) {
                if(game.redTeam.contains(p)) {
                    player.sendMessage(ChatColor.GREEN + p.getName() + " Captured Blue teams flag!");
                } else {
                    player.sendMessage(ChatColor.RED + p.getName() + " Captured Your teams flag!");
                }
            }
            for(Player player : game.blueTeam) {
                if(game.blueTeam.contains(p)) {
                    player.sendMessage(ChatColor.GREEN + p.getName() + " Captured Red teams flag!");
                } else {
                    player.sendMessage(ChatColor.RED + p.getName() + " Captured Your teams flag!");
                }
            }
        }
    }


    public static CaptureTheFlag getPlugin() {
        return captureTheFlag;
    }

    public static File getSchematicFolder() {
        return SCHEMATIC_FOLDER;
    }

    public static HashMap<Player, Integer> sortByValue(HashMap<Player, Integer> hm)
    {
        List<java.util.Map.Entry<Player, Integer>> list =
                new LinkedList<>(hm.entrySet());

        Collections.sort(list, new Comparator<java.util.Map.Entry<Player, Integer> >() {
            public int compare(java.util.Map.Entry<Player, Integer> o1,
                               java.util.Map.Entry<Player, Integer> o2)
            {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });

        HashMap<Player, Integer> temp = new LinkedHashMap<>();
        for (java.util.Map.Entry<Player, Integer> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;
    }
}
