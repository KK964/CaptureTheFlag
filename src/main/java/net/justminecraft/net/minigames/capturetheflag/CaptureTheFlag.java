package net.justminecraft.net.minigames.capturetheflag;

import net.justminecraft.minigames.minigamecore.ActionBar;
import net.justminecraft.minigames.minigamecore.Game;
import net.justminecraft.minigames.minigamecore.MG;
import net.justminecraft.minigames.minigamecore.Minigame;
import net.justminecraft.minigames.minigamecore.worldbuffer.WorldBuffer;
import org.bukkit.*;
import org.bukkit.block.Banner;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
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

        g.redFlag.setWorld(g.world);
        g.blueFlag.setWorld(g.world);
        g.redLives = 5;
        g.blueLives = 5;

        setFlagBlock(g.redFlag, DyeColor.RED);
        setFlagBlock(g.blueFlag, DyeColor.BLUE);

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
            int flagReturns = playerScores.getFlagReturns();
            int losses = games - wins;
            int score = (games + (wins * 10) + (captures * 2) + (flagReturns * 2)) - losses;
            g.games.put(p, games);
            g.wins.put(p, wins);
            g.captures.put(p, captures);
            g.flagReturns.put(p, flagReturns);
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
            setArmor(p);
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

    public void setFlagBlock(Location location, DyeColor color) {
        location.getBlock().setType(Material.STANDING_BANNER);
        Banner b = (Banner) location.getBlock().getState();
        b.setBaseColor(color);
        b.update();
    }

    public void setArmor(Player p) {
        Game g = MG.core().getGame(p);
        CaptureTheFlagGame game = (CaptureTheFlagGame) g;
        String team = game.redTeam.contains(p) ? "Red" : "Blue";
        ItemStack c = new ItemStack(Material.LEATHER_CHESTPLATE);
        LeatherArmorMeta cM = (LeatherArmorMeta) c.getItemMeta();
        ItemStack l = new ItemStack(Material.LEATHER_LEGGINGS);
        LeatherArmorMeta lM = (LeatherArmorMeta) l.getItemMeta();
        ItemStack b = new ItemStack(Material.LEATHER_BOOTS);
        LeatherArmorMeta bM = (LeatherArmorMeta) b.getItemMeta();
        if(team.equals("Red")) {
            cM.setColor(Color.RED);
            lM.setColor(Color.RED);
            bM.setColor(Color.RED);
        } else {
            cM.setColor(Color.BLUE);
            lM.setColor(Color.BLUE);
            bM.setColor(Color.BLUE);
        }
        c.setItemMeta(cM);
        l.setItemMeta(lM);
        b.setItemMeta(bM);
        p.getInventory().setChestplate(c);
        p.getInventory().setLeggings(l);
        p.getInventory().setBoots(b);
    }

    @EventHandler
    public void breakFlag(BlockBreakEvent e) {
        Player p = e.getPlayer();
        Game g = MG.core().getGame(p);
        if(g == null || g.minigame != this) return;
        CaptureTheFlagGame game = (CaptureTheFlagGame) g;
        boolean deleteBlock = false;
        boolean pickUpFlag = false;
        boolean isOtherPickup = false;
        String flagStolen = null;
        Location redFlag = game.redFlag;
        redFlag.setWorld(g.world);
        Location blueFlag = game.blueFlag;
        blueFlag.setWorld(g.world);
        Location blockBroken = e.getBlock().getLocation();
        if(game.redTeam.contains(p) && (blockBroken.equals(blueFlag) || blockBroken.equals(game.blueDroppedFlag))) {
            if(blockBroken.equals(game.blueDroppedFlag))
                isOtherPickup = true;
            ItemStack headBanner = new ItemStack(Material.BANNER, 1, (short) 4);
            p.getInventory().setHelmet(headBanner);
            deleteBlock = true;
            flagStolen = "Blue";
        }
        if(game.blueTeam.contains(p) && (blockBroken.equals(redFlag) || blockBroken.equals(game.redDroppedFlag))) {
            if(blockBroken.equals(game.redDroppedFlag))
                isOtherPickup = true;
            ItemStack headBanner = new ItemStack(Material.BANNER, 1, (short) 1);
            p.getInventory().setHelmet(headBanner);
            deleteBlock = true;
            flagStolen = "Red";
        }

        if(game.redTeam.contains(p) && blockBroken.equals(game.redDroppedFlag)) {
            ItemStack headBanner = new ItemStack(Material.BANNER, 1, (short) 1);
            p.getInventory().setHelmet(headBanner);
            deleteBlock = true;
            pickUpFlag = true;
        }
        if(game.blueTeam.contains(p) && blockBroken.equals(game.blueDroppedFlag)) {
            ItemStack headBanner = new ItemStack(Material.BANNER, 1, (short) 4);
            p.getInventory().setHelmet(headBanner);
            deleteBlock = true;
            pickUpFlag = true;
        }

        if(deleteBlock) {
            e.getBlock().setType(Material.AIR);
            p.playSound(p.getLocation(), Sound.LEVEL_UP, 1, 1);
            if(!pickUpFlag) {
                game.flagCarrier.add(p);
                if(!isOtherPickup) {
                    p.sendMessage(ChatColor.GREEN + "You stole " + flagStolen + "'s Flag! Make it back to your spawn to capture the flag!");
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
                } else {
                    p.sendMessage(ChatColor.GREEN + "You picked up " + flagStolen + "'s Flag!");
                }
            } else {
                game.flagReturnCarrier.add(p);
                p.sendMessage(ChatColor.GREEN + "You picked up your flag! Bring it back to your spawn!");
            }
        }
    }

    @EventHandler
    public void onMoveEvent(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        Game g = MG.core().getGame(p);
        if(g == null || g.minigame != this) return;
        CaptureTheFlagGame game = (CaptureTheFlagGame) g;
        if(!game.flagCarrier.contains(p) && !game.flagReturnCarrier.contains(p)) return;
        Location to = e.getTo();
        Location l = to.getBlock().getLocation();
        l.setY(l.getY() - 1);
        boolean capturedFlag = false;
        boolean returnedFlag = false;
        String capturedFlagString = null;
        if(game.blueTeam.contains(p)) {
            if(game.blueSpawns.contains(l)) {
                if(game.flagCarrier.contains(p)) {
                    game.redLives = game.redLives - 1;
                    game.captures.replace(p, game.captures.get(p) + 1);
                    capturedFlag = true;
                    capturedFlagString = "Red";
                }
                if(game.flagReturnCarrier.contains(p)) {
                    returnedFlag = true;
                }
            }
        }
        if(game.redTeam.contains(p)) {
            if(game.redSpawns.contains(l)) {
                if(game.flagCarrier.contains(p)) {
                    game.blueLives = game.blueLives - 1;
                    game.captures.replace(p, game.captures.get(p) + 1);
                    capturedFlag = true;
                    capturedFlagString = "Blue";
                } else {
                    returnedFlag = true;
                }
            }
        }
        if(capturedFlag || returnedFlag) {
            p.getInventory().setHelmet(new ItemStack(Material.AIR));
            game.updateScoreBoard();
            if(capturedFlag) {
                game.flagCarrier.remove(p);
                if(capturedFlagString.equals("Red")) {
                    setFlagBlock(game.redFlag, DyeColor.RED);
                } else {
                    setFlagBlock(game.blueFlag, DyeColor.BLUE);
                }

                for(Player player : game.redTeam) {
                    if(capturedFlagString.equals("Blue")) {
                        player.sendMessage(ChatColor.GREEN + p.getName() + " Captured Blue teams flag!");
                    } else {
                        player.sendMessage(ChatColor.RED + p.getName() + " Captured Your teams flag!");
                    }
                }
                for(Player player : game.blueTeam) {
                    if(capturedFlagString.equals("Red")) {
                        player.sendMessage(ChatColor.GREEN + p.getName() + " Captured Red teams flag!");
                    } else {
                        player.sendMessage(ChatColor.RED + p.getName() + " Captured Your teams flag!");
                    }
                }
            }
            if(returnedFlag) {
                game.flagReturnCarrier.remove(p);
                game.flagReturns.replace(p, game.flagReturns.get(p) + 1);
                if(game.redTeam.contains(p)) {
                    setFlagBlock(game.redFlag, DyeColor.RED);
                    for(Player player : game.redTeam) {
                        player.sendMessage(ChatColor.GREEN + p.getName() + " returned your flag!");
                    }
                } else {
                    setFlagBlock(game.blueFlag, DyeColor.BLUE);
                    for(Player player : game.blueTeam) {
                        player.sendMessage(ChatColor.GREEN + p.getName() + " returned your flag!");
                    }
                }
            }
        }
    }

    @EventHandler
    public void playerHurt(EntityDamageByEntityEvent e) {
        if(e.getDamager() instanceof Player && e.getEntity() instanceof Player) {
            Player damager = (Player) e.getDamager();
            Player hurt = (Player) e.getEntity();
            Game g = MG.core().getGame(hurt);
            if(g == null || g.minigame != this) return;
            CaptureTheFlagGame game = (CaptureTheFlagGame) g;
            if(game.respawnInvulnerability.containsKey(hurt)) {
                ActionBar actionBar = new ActionBar(ChatColor.RED + "That player is Invulnerable!");
                actionBar.send(damager);
                e.setCancelled(true);
                return;
            }
            if(game.flagCarrier.contains(hurt)) {
                game.flagCarrier.remove(hurt);
                hurt.sendMessage(ChatColor.RED + "You lost the flag!");
                hurt.getInventory().setHelmet(new ItemStack(Material.AIR));
                if(game.redTeam.contains(hurt)) {
                    setFlagBlock(hurt.getLocation(), DyeColor.BLUE);
                    game.blueDroppedFlag = hurt.getLocation().getBlock().getLocation();
                } else {
                    setFlagBlock(hurt.getLocation(), DyeColor.RED);
                    game.redDroppedFlag = hurt.getLocation().getBlock().getLocation();
                }
            }
            if(game.flagReturnCarrier.contains(hurt)) {
                game.flagReturnCarrier.remove(hurt);
                hurt.sendMessage(ChatColor.RED + "You lost the flag!");
                hurt.getInventory().setHelmet(new ItemStack(Material.AIR));
                if(game.redTeam.contains(hurt)) {
                    setFlagBlock(hurt.getLocation(), DyeColor.RED);
                    game.redDroppedFlag = hurt.getLocation().getBlock().getLocation();
                } else {
                    setFlagBlock(hurt.getLocation(), DyeColor.BLUE);
                    game.blueDroppedFlag = hurt.getLocation().getBlock().getLocation();
                }
            }
            e.setDamage(5);
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
