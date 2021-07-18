package com.starmediadev.plugins.data;

import com.starmediadev.data.StarData;
import org.bukkit.plugin.java.JavaPlugin;

public class StarDataPlugin extends JavaPlugin {
    
    private StarData starData;

    public void onEnable() {
        this.starData = new StarData(getLogger());
    }
}
