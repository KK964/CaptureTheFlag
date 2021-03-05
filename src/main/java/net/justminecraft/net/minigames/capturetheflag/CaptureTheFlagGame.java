package net.justminecraft.net.minigames.capturetheflag;

import net.justminecraft.minigames.minigamecore.Game;
import net.justminecraft.minigames.minigamecore.Minigame;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class CaptureTheFlagGame extends Game {
    private final CaptureTheFlag captureTheFlag;
    public int redLives;
    public int blueLives;
    public List<Player> redTeam = new ArrayList<>();
    public List<Player> blueTeam = new ArrayList<>();
    public List<Player> flagCarrier = new ArrayList<>();
    public List<Player> flagReturnCarrier = new ArrayList<>();
    public List<Location> redSpawns = new ArrayList<>();
    public List<Location> blueSpawns = new ArrayList<>();
    public Location redFlag;
    public Location blueFlag;
    public Location redDroppedFlag;
    public Location blueDroppedFlag;
    public HashMap<Player, Integer> wins = new HashMap<>();
    public HashMap<Player, Integer> games = new HashMap<>();
    public HashMap<Player, Integer> captures = new HashMap<>();
    public HashMap<Player, Integer> flagReturns = new HashMap<>();
    public HashMap<Player, Integer> respawnInvulnerability = new HashMap<>();

    Scoreboard scoreboard;

    public CaptureTheFlagGame(Minigame mg) {
        super(mg, false);
        captureTheFlag = (CaptureTheFlag) mg;
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
    }

    public String getMap() {
        try {
            ArrayList<String> maps = new ArrayList<>();
            for(File file : CaptureTheFlag.getSchematicFolder().listFiles()) {
                if(file.isFile())
                    maps.add(file.getName());
            }
            if(maps.size() == 0) new IOException("Schematic File is missing, please add maps.");
            Random rand = new Random();
            return maps.get(rand.nextInt(maps.size()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean isGameOver() {
        if(players.size() < 2) return true;
        if(redLives == 0 || blueLives == 0) return true;
        return false;
    }

    public void isOver() {
        if(isGameOver()) {
            String winningTeam = getWinningTeamName();
            for(Player p : players) {
                String team = redTeam.contains(p) ? "Red" : "Blue";
                if(team.equals(winningTeam))
                    wins.replace(p, wins.get(p) + 1);
                PlayerScores playerScores = new PlayerScores(p);
                playerScores.save(games.get(p) + 1, wins.get(p), captures.get(p), flagReturns.get(p));
            }
            finishGame();
        }
    }

    @Override
    public String getWinningTeamName() {
        if(redLives > 0 && blueLives > 0) return "Unknown";
        if(redLives > 0) return "Red";
        if(blueLives > 0) return "Blue";
        return "Unknown";
    }

    @Override
    public void onPlayerDeath(Player p) {
        isOver();
        p.setVelocity(new Vector(0,0,0));
        p.setHealth(20);
        p.setFallDistance(0);
        p.setGameMode(GameMode.SPECTATOR);
        String team = redTeam.contains(p) ? "Red" : "Blue";
        if(flagCarrier.contains(p)) {
            flagCarrier.remove(p);
            if(team.equals("Red")) {
                captureTheFlag.setFlagBlock(blueFlag, DyeColor.BLUE);
            } else {
                captureTheFlag.setFlagBlock(redFlag, DyeColor.RED);
            }
        }
        if(flagReturnCarrier.contains(p)) {
            flagReturnCarrier.remove(p);
            if(team.equals("Red")) {
                captureTheFlag.setFlagBlock(redFlag, DyeColor.RED);
            } else {
                captureTheFlag.setFlagBlock(blueFlag, DyeColor.BLUE);
            }
        }
        if(p.getLocation().getY() < 30)
            p.teleport(new Location(p.getWorld(), 0, 90, 0));
        if(!isGameOver()) new PlayerRespawn(p);
    }

    public void updateScoreBoard() {
        scoreboard.resetScores(ChatColor.RED + "Red: " + (redLives + 1));
        scoreboard.resetScores(ChatColor.BLUE + "Blue: " + (blueLives + 1));
        scoreboard.resetScores(ChatColor.RED + "Red: " + redLives);
        scoreboard.resetScores(ChatColor.BLUE + "Blue: " + blueLives);
        scoreboard.getObjective(DisplaySlot.SIDEBAR).getScore(ChatColor.RED + "Red: " + redLives).setScore(4);
        scoreboard.getObjective(DisplaySlot.SIDEBAR).getScore(ChatColor.BLUE + "Blue: " + blueLives).setScore(3);
        isOver();
    }
}
