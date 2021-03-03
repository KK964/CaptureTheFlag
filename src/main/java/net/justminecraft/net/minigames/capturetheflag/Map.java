package net.justminecraft.net.minigames.capturetheflag;

import net.justminecraft.minigames.minigamecore.worldbuffer.WorldBuffer;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class Map {

    private Material redSpawnMaterial = Material.SPONGE;
    private Material blueSpawnMaterial = Material.JUKEBOX;
    private Material redFlagMaterial = Material.RED_MUSHROOM;
    private Material blueFlagMaterial = Material.BROWN_MUSHROOM;

    public void placeSchematic(WorldBuffer w, Location l, String key, CaptureTheFlagGame game) {
        try {
            File schematic = new File(CaptureTheFlag.getSchematicFolder(), key);
            if(schematic.isFile()) {
                Random random = new Random();
                HashMap<Material, ArrayList<Location>> spawnLocations = w.placeSchematic(l, schematic, redSpawnMaterial, blueSpawnMaterial, redFlagMaterial, blueFlagMaterial);
                ArrayList<Location> redSpawn = spawnLocations.get(redSpawnMaterial);
                ArrayList<Location> redFlag = spawnLocations.get(redFlagMaterial);
                ArrayList<Location> blueFlag = spawnLocations.get(blueFlagMaterial);
                ArrayList<Location> blueSpawn = spawnLocations.get(blueSpawnMaterial);
                for(Location loc : redSpawn) {
                    game.redSpawns.add(loc);
                    w.setBlockAt(loc, Material.WOOL, DyeColor.RED.getData());
                }
                for(Location loc : blueSpawn) {
                    game.blueSpawns.add(loc);
                    w.setBlockAt(loc, Material.WOOL, DyeColor.BLUE.getData());
                }
                game.redFlag = redFlag.get(random.nextInt(redFlag.size()));
                game.blueFlag = blueFlag.get(random.nextInt(blueFlag.size()));
                for(Location loc : redFlag) {
                    w.setBlockAt(loc, Material.AIR);
                }
                for(Location loc : blueFlag) {
                    w.setBlockAt(loc, Material.AIR);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
