package com.thezomg.zomgctf;

public class Configuration {
    
    private ZomgCTF plugin;

    public Configuration(ZomgCTF plugin) {
        this.plugin = plugin;
    }
    
    public int furthest_drop = plugin.getConfig().getInt("furthest_drop", 0);

}