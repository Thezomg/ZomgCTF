package com.thezomg.zomgctf;

import com.thezomg.zomgctf.Team.TeamDoesNotExistException;
import java.util.Collection;
import java.util.HashMap;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

public class TeamManager {
    
    private HashMap<String, Team> teams;
    ZomgCTF plugin;
    
    public TeamManager(ZomgCTF plugin) {
        this.plugin = plugin;
        teams = new HashMap<String, Team>();
    }
    
    public void addTeam(String name, ChatColor chatColour, ItemStack block) {
        if (!teams.containsKey(name.toLowerCase())) {
            teams.put(name, new Team(plugin, name, chatColour, block));
        }
    }
    
    public void loadTeam(String name) throws TeamDoesNotExistException {
        if (!teams.containsKey(name.toLowerCase())) {
            teams.put(name, new Team(plugin, name));
        }
    }

    public Collection<Team> getTeams() {
        return teams.values();
    }
    
    public Team getTeam(String name) {
        if (teams.containsKey(name.toLowerCase())) {
            return teams.get(name.toLowerCase());
        }
        return null;
    }
    
    public Team getPlayerTeam(String player) {
        Team t = null;
        for (Team _t : teams.values()) {
            if (_t.isOnTeam(player)) {
                t = _t;
                break;
            }
        }
        return t;
    }
    
    public Team getTeamFromCarrier(String player) {
        Team t = null;
        for (Team _t : teams.values()) {
            if (_t.isCarrier(player)) {
                t = _t;
                break;
            }
        }
        return t;
    }
}
