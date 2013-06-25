package com.thezomg.zomgctf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Team {
    private String name;
    private ChatColor chatColour;
    private ItemStack block;
    private HashMap<String, Integer> players = new HashMap<String, Integer>();
    private int score = 0;
    private int kills = 0;
    private int captures = 0;
    private Location spawn;
    private Location flagHome = null;
    private Location flagLocation = null;
    private YamlConfiguration config;
    private File config_file;
    private Player carrier;
    private boolean dirty = false;
    private boolean spawn_removed = false;
    
    private ZomgCTF plugin;
    
    public class TeamDoesNotExistException extends Exception {
        public TeamDoesNotExistException(String message) {
            super(message);
        }
    }

    public Team(ZomgCTF plugin, String name) throws TeamDoesNotExistException {
        this.plugin = plugin;
        this.name = name;
        config_file = new File(plugin.getDataFolder(), name + ".yml");
        if (!config_file.exists()) {
            throw new TeamDoesNotExistException("The team " + name + " doesn't exist");
        }
        load();
    }
    
    public Team(ZomgCTF plugin, String name, ChatColor chatColour, ItemStack block) {
        this.plugin = plugin;
        this.name = name;
        this.chatColour = chatColour;
        this.block = block;
        config_file = new File(plugin.getDataFolder(), name + ".yml");
        if (!config_file.exists()) {
            config = new YamlConfiguration();
            save();
        }
        load();
    }
    
    public String getName() {
        return name;
    }

    public boolean isOnTeam(String player) {
        return players.containsKey(player.toLowerCase());
    }
    
    public boolean isCarrier(String player) {
        return carrier==null?null:carrier.getName().equalsIgnoreCase(player);
    }
    
    public int getScore() {
        return score;
    }
    
    public int getKills() {
        return kills;
    }
    
    public ItemStack getBlock() {
        return block;
    }
    
    public ChatColor getChatColour() {
        return chatColour;
    }
    
    public Block getBlockHome() {
        return flagHome.getWorld().getBlockAt(flagHome);
    }
    
    public boolean isFlagHome() {
        return (util.inSameLocation(flagHome, flagLocation) && (carrier == null));
    }
    
    protected void capture() {
        this.captures++;
        this.score += plugin.config.flag_capture_score;
        dirty = true;
    }
    
    protected void kill() {
        this.kills++;
        this.score += plugin.config.kill_score;
    }
    
    public Player getCarrier() {
        return carrier;
    }
    
    public void setCarrier(Player player) {
        this.carrier = player;
    }
    
    public void dropflag() {
        if (carrier != null) {
            Location loc = carrier.getLocation();
            Block b = loc.getWorld().getBlockAt(loc);
            int count = 0;
            while (b.getType() != Material.AIR && count < plugin.config.furthest_drop) {
                b = loc.getWorld().getBlockAt(b.getX(), b.getY() + 1, b.getZ());
                count++;
            }
            if (count >= plugin.config.furthest_drop) {
                respawnFlag();
            }
            else {
                b.setTypeIdAndData(block.getTypeId(), block.getData().getData(), false);
                setFlagLocation(b.getLocation());
            }
            carrier = null;
        }
    }
    
    public void respawnFlag() {
        flagLocation.getBlock().setType(Material.AIR);
        flagHome.getWorld().getBlockAt(flagHome).setTypeIdAndData(block.getTypeId(), block.getData().getData(), false);
        setFlagLocation(flagHome);
    }
    
    public void setFlagLocation(Location loc) {
        flagLocation = loc;
        dirty = true;
    }
    
    public void setFlagHome(Location loc) {
        flagHome = loc;
        flagHome.getWorld().getBlockAt(flagHome).setTypeIdAndData(block.getTypeId(), block.getData().getData(), false);
        dirty = true;
    }
    
    public void addPlayer(String player) {
        addPlayer(player, 0);
    }
    
    private void addPlayer(String player, Integer score) {
        players.put(player.toLowerCase(), score);
        dirty = true;
    }
    
    private void setChatColour(String chatColour) {
        this.chatColour = ChatColor.valueOf(name);
        dirty = true;
    }
    
    private void setBlock(String block) {
        this.block = util.stringToItemStack(block);
        dirty = true;
    }
    
    public void setSpawn(Location loc) {
        spawn = loc;
        dirty = true;
    }
    
    public void save() {
        if (dirty) {
            config.set("chatcolour", chatColour.name());
            config.set("block", block.getTypeId() + ":" + block.getDurability());
            config.set("score", score);
            config.set("kills", kills);
            util.setLocationInConfig(config, "flag.home", flagHome);
            util.setLocationInConfig(config, "flag.location", flagLocation);
            util.setLocationInConfig(config, "spawn", spawn);
            config.set("members", players);
            
            try {
                config.save(config_file);
                dirty = false;
            } catch (IOException ex) {
                plugin.getLogger().log(Level.SEVERE, "Could not save team config to " + config_file, ex);
            }
        }
    }
    
    private void load() {
        try {
            config = new YamlConfiguration();
            config.load(config_file);
            
            setChatColour(config.getString("chatcolour"));
            setBlock(config.getString("block"));
            score = config.getInt("score");
            kills = config.getInt("kills");
            flagHome = util.getLocationFromConfig(config, "flag.home");
            flagLocation = util.getLocationFromConfig(config, "flag.location");
            players = (HashMap<String, Integer>) config.getMapList("members");
            dirty = false;
        } catch (FileNotFoundException ex) {
            plugin.getLogger().log(Level.SEVERE, "Could not load team config from " + config_file, ex);
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "Could not read team config from " + config_file, ex);
        } catch (InvalidConfigurationException ex) {
            plugin.getLogger().log(Level.SEVERE, "Invalid team config from " + config_file, ex);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof Team)) return false;
        
        return this.name.equalsIgnoreCase(((Team)o).getName());
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 41 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }
}
