package com.starmediadev.plugins.data;

import com.starmediadev.data.StarData;
import com.starmediadev.data.manager.DatabaseManager;
import com.starmediadev.data.manager.MultidatabaseManager;
import org.bukkit.plugin.java.JavaPlugin;

public class StarDataPlugin extends JavaPlugin {
    
    private StarData starData;
    private DatabaseManager databaseManager;

    public void onEnable() {
        this.starData = new StarData(getLogger());
        databaseManager = new MultidatabaseManager(starData);
        starData.setDatabaseManager(databaseManager);

        databaseManager.setup();
    }
}
