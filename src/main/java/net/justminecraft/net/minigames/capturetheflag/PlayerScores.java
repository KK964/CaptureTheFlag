package net.justminecraft.net.minigames.capturetheflag;

import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerScores {
    private static final String SCORES_CSV = "scores.csv";
    private final UUID uuid;

    public PlayerScores(Player p) {
        File file = new File(CaptureTheFlag.getPlugin().getDataFolder(), SCORES_CSV);
        try {
            if(file.createNewFile()) {
                CaptureTheFlag.getPlugin().getLogger().info("Made new PlayerScore File");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.uuid = p.getUniqueId();
    }

    public int getGames() {
        String part = getLine();
        if(part == null) return 0;
        String[] parts = part.split(",");
        return Integer.parseInt(parts[1]);
    }

    public int getWins() {
        String part = getLine();
        if(part == null) return 0;
        String[] parts = part.split(",");
        return Integer.parseInt(parts[2]);
    }

    public int getCaptures() {
        String part = getLine();
        if(part == null) return 0;
        String[] parts = part.split(",");
        return Integer.parseInt(parts[3]);
    }

    public int getFlagReturns() {
        String part = getLine();
        if(part == null) return 0;
        String[] parts = part.split(",");
        return Integer.parseInt(parts[4]);
    }

    private String getLine() {
        List<String> lines = getLines();
        if(lines.isEmpty()) return null;
        for(String l : lines) {
            String[] parts = l.split(",");
            if(parts[0].equals(uuid.toString())) {
                return l;
            }
        }
        return null;
    }

    private List getLines() {
        File file = new File(CaptureTheFlag.getPlugin().getDataFolder(), SCORES_CSV);
        if(file.isFile()) {
            try {
                return Files.readAllLines(file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void save(int games, int wins, int captures, int returns) {
        List<String> lines = getLines();
        List<String> line = new ArrayList<>();
        boolean isInFile = false;
        String newValue = uuid.toString() + "," + games + "," + wins + "," + captures + "," + returns;
        if(lines.isEmpty()) {
            isInFile = true;
            line.add(newValue);
        } else {
            for(String l : lines) {
                String[] part = l.split(",");
                if(part[0].equals(uuid.toString())) {
                    isInFile = true;
                    line.add(newValue);
                } else {
                    line.add(l);
                }
            }
        }
        if(!isInFile) {
            line.add(newValue);
        }
        try {
            Files.write(new File(CaptureTheFlag.getPlugin().getDataFolder(), SCORES_CSV).toPath(), line);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
