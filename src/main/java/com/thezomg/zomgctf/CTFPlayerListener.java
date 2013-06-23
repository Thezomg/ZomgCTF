package com.thezomg.zomgctf;

import org.bukkit.ChatColor;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class CTFPlayerListener implements Listener {
    private ZomgCTF plugin;

    public CTFPlayerListener(ZomgCTF plugin) {
        this.plugin = plugin;
    }

    public void onBlockPlace(BlockPlaceEvent event) {
        if (plugin.flag_set.containsKey(event.getPlayer().getName().toLowerCase())) {
            Team t = plugin.tm.getTeam(plugin.flag_set.get(event.getPlayer().getName().toLowerCase()));
            if (t == null) {
                // Something went rather wrong here.
                event.getPlayer().sendMessage(ChatColor.RED + "An error occurred while setting the flag, see the console for more details.");
                return;
            }
            t.setFlagHome(event.getBlock().getLocation());
            plugin.flag_set.remove(event.getPlayer().getName().toLowerCase());
            event.getPlayer().sendMessage(ChatColor.GREEN + "Successfully set the " + t.getName() + " flag location.");
        }
    }
}
