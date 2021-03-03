package net.justminecraft.net.minigames.capturetheflag;

import net.justminecraft.minigames.minigamecore.Game;
import net.justminecraft.minigames.minigamecore.Minigame;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class CaptureTheFlagGame extends Game {
    private final CaptureTheFlag captureTheFlag;
    public List<Location> redSpawns = new ArrayList<>();
    public List<Location> blueSpawns = new ArrayList<>();
    public Location redFlag;
    public Location blueFlag;
    public HashMap<Player, Integer> wins = new HashMap<>();
    public HashMap<Player, Integer> games = new HashMap<>();
    public HashMap<Player, Integer> captures = new HashMap<>();

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

}
