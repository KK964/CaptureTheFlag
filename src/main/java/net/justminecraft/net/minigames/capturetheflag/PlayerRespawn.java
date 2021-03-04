package net.justminecraft.net.minigames.capturetheflag;

import net.justminecraft.minigames.minigamecore.Game;
import net.justminecraft.minigames.minigamecore.MG;
import net.justminecraft.minigames.titleapi.TitleAPI;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Random;

public class PlayerRespawn implements Runnable{
    private final Player player;
    private int seconds = 5;

    public PlayerRespawn(Player player) {
        this.player = player;
        run();
    }

    @Override
    public void run() {
        Random rand = new Random();
        Game g = MG.core().getGame(player);
        CaptureTheFlagGame game = (CaptureTheFlagGame) g;
        if(g == null || g.minigame != CaptureTheFlag.getPlugin()) return;
        if(seconds == 0) {
            Location spawnLocation = null;
            ArrayList<Location> redSpawnLoc = new ArrayList<>(game.redSpawns);
            ArrayList<Location> blueSpawnLoc = new ArrayList<>(game.blueSpawns);
            if(game.redTeam.contains(player)) {
                spawnLocation = redSpawnLoc.get(rand.nextInt(redSpawnLoc.size()));
            } else {
                spawnLocation = blueSpawnLoc.get(rand.nextInt(blueSpawnLoc.size()));
            }
            spawnLocation.setWorld(g.world);
            spawnLocation.setY(spawnLocation.getY() + 1);
            player.teleport(spawnLocation);
            player.setGameMode(GameMode.SURVIVAL);
            game.respawnInvulnerability.put(player, 3);
            new PlayerRespawnInvulnerability(player);
            CaptureTheFlag.getPlugin().setArmor(player);
            return;
        }

        TitleAPI.sendTitle(player, 0, seconds > 1 ? 30 : 20, 0, ChatColor.GREEN + "Respawning in " + seconds + " second" + (seconds == 1 ? "" : "s") + "...","");
        seconds--;
        CaptureTheFlag.getPlugin().getServer().getScheduler().runTaskLater(CaptureTheFlag.getPlugin(), this, 20);
    }
}
