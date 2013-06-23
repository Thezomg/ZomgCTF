package com.thezomg.zomgctf;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;


public class ZomgCTF extends JavaPlugin {

    public Configuration config;
    protected TeamManager tm;
    protected HashMap<String, String> flag_set = new HashMap<String, String>();
    
    @Override
    public void onEnable() {
        File config_file = new File(getDataFolder(), "config.yml");
        if (!config_file.exists()) {
            getConfig().options().copyDefaults(true);
            saveConfig();
        }
        this.config = new Configuration(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("g")) {
            globalChat(sender, util.join(args, " "));
            return true;
        }
        else if (command.getName().equalsIgnoreCase("teams")) {
            sendTeamList(sender);
            return true;
        }
        else if (command.getName().equalsIgnoreCase("score")) {
            sendScores(sender);
            return true;
        }
        else if (command.getName().equalsIgnoreCase("drop")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Only a player can drop a flag");
            }
            Team t = tm.getPlayerTeam(sender.getName());
            if (t == null) {
                sender.sendMessage("Only those on a team can play the game");
            }
            Team flag_team = tm.getTeamFromCarrier(sender.getName());
            if (flag_team == null) {
                sender.sendMessage("You can only drop the flag if you are a carrier.");
            }
            else {
                flag_team.dropflag();
            }
            return true;
        }
        else if (command.getName().equalsIgnoreCase("zomgctf")) {
            if (args[0].equalsIgnoreCase("addteam")) {
                // zomgctf addteam <name> <chatcolour> <blockid>
                if (args.length < 4) {
                    sender.sendMessage(ChatColor.RED + "Syntax: /" + label + " addteam <name> <chatcolour> <blockid>");
                }
                else {
                    handleCreateTeam(sender, args[1], args[2], args[3]);
                }
                return true;
            }
            else if (args[0].equalsIgnoreCase("setspawn")) {
                if (sender instanceof Player) {
                    // zomgctf setspawn <team>
                    if (args.length < 2) {
                        sender.sendMessage(ChatColor.RED + "Syntax: /" + label + " setspawn <teamname>");
                    }
                    else {
                        handleSetSpawn((Player)sender, args[1]);
                    }
                }
                else {
                    sender.sendMessage(ChatColor.RED + "Can't set team spawns from console.");
                }
            }
            else if (args[0].equalsIgnoreCase("setflag")) {
                if (sender instanceof Player) {
                    // zomgctf setflag <team>
                    if (args.length < 2) {
                        sender.sendMessage(ChatColor.RED + "Syntax: /" + label + " setflag <teamname>");
                    }
                    else {
                        handleSetFlag((Player)sender, args[1]);
                    }
                }
            }
        }
        return false;
    }
    
    private void handleSetFlag(Player sender, String teamname) {
        Team t = tm.getTeam(teamname);
        if (t == null) {
            sender.sendMessage(ChatColor.RED + "The team \"" + teamname + "\" doesn't exist.");
        }
        else {
            sender.sendMessage(ChatColor.GREEN + "Place any block at the location where you would like the flag.");
            flag_set.put(sender.getName().toLowerCase(), teamname);
        }
    }
    
    private void handleSetSpawn(Player sender, String teamname) {
        Team t = tm.getTeam(teamname);
        if (t == null) {
            sender.sendMessage(ChatColor.RED + "The team \"" + teamname + "\" doesn't exist.");
        }
        else {
            t.setSpawn(sender.getLocation());
        }
    }
    
    private void handleCreateTeam(CommandSender sender, String name, String chatcolour, String blockid) {
        try {
            ChatColor colour = ChatColor.valueOf(chatcolour);
            ItemStack block = util.stringToItemStack(blockid);
            tm.addTeam(name, colour, block);
            getConfig().set("teams", tm.getTeams());
            saveConfig();
        }
        catch (Exception ex) {
            sender.sendMessage(ChatColor.RED + "Invalid option for either chat colour or block id");
            getLogger().log(Level.WARNING, "Invalid options in for creating team " + name + " by " + sender.getName() + ".", ex);
        }
    }

    private void globalChat(CommandSender sender, String message) {
        StringBuilder builder = new StringBuilder();
        Team t = tm.getPlayerTeam(sender.getName());
        builder.append("[");
        builder.append(ChatColor.BLUE);
        builder.append("global");
        builder.append(ChatColor.WHITE);
        builder.append("] <");
        if (t != null)
            builder.append(t.getChatColour());
        builder.append(sender.getName());
        if (t != null)
            builder.append(ChatColor.WHITE);
        builder.append("> ");
        builder.append(message);
        
        getServer().broadcastMessage(builder.toString());
    }
    
    private void sendTeamList(CommandSender sender) {
        Collection<Team> teams = tm.getTeams();
        List<String> teamstrs = new ArrayList<String>();
        for (Team t : teams) {
            teamstrs.add(t.getChatColour() + t.getName() + ChatColor.WHITE);
        }
        
        sender.sendMessage(util.join(teamstrs, ", "));
    }
    
    private void sendScores(CommandSender sender) {
        Collection<Team> teams = tm.getTeams();
        List<String> teamstrs = new ArrayList<String>();
        for (Team t : teams) {
            teamstrs.add(t.getChatColour() + t.getName() + ChatColor.WHITE + ": " + t.getScore());
        }
        
        sender.sendMessage(teamstrs.toArray(new String[0]));
    }
}
