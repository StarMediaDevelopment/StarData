package com.starmediadev.plugins.data;

import com.starmediadev.data.StarData;
import com.starmediadev.data.manager.MultidatabaseManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class StarDataPlugin extends JavaPlugin {
    
    private StarData starData;

    public void onEnable() {
        this.starData = new StarData(getLogger());
        starData.setDatabaseManager(new MultidatabaseManager(starData));
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        
        
        
        return true;
    }
}
