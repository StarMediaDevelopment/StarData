package com.starmediadev.plugins.data;

import com.starmediadev.data.StarData;
import com.starmediadev.data.handlers.DataTypeHandler;
import com.starmediadev.data.manager.DatabaseManager;
import com.starmediadev.data.manager.MultidatabaseManager;
import com.starmediadev.data.model.IDataObject;
import com.starmediadev.data.properties.SqlProperties;
import com.starmediadev.plugins.data.events.DatabaseRegisterEvent;
import com.starmediadev.plugins.data.events.HandlerRegisterEvent;
import com.starmediadev.plugins.data.events.PostDataSetupEvent;
import com.starmediadev.plugins.data.events.TypeRegisterEvent;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class StarDataPlugin extends JavaPlugin {
    
    private StarData starData;
    private DatabaseManager databaseManager;

    public void onEnable() {
        this.starData = new StarData(getLogger());
        starData.setDatabaseManager(databaseManager = new MultidatabaseManager(starData));

        PluginManager pluginManager = Bukkit.getServer().getPluginManager();
        getServer().getScheduler().runTaskLater(this, () -> {
            DatabaseRegisterEvent databaseEvent = new DatabaseRegisterEvent(starData);
            pluginManager.callEvent(databaseEvent);
            
            HandlerRegisterEvent handlerEvent = new HandlerRegisterEvent(starData);
            pluginManager.callEvent(handlerEvent);

            TypeRegisterEvent typeEvent = new TypeRegisterEvent(starData);
            pluginManager.callEvent(typeEvent);

            for (SqlProperties details : databaseEvent.getDatabaseProperties()) {
                databaseManager.setupDatabase(details);
            }
            
            for (DataTypeHandler<?> typeHandler : handlerEvent.getTypeHandlers()) {
                databaseManager.registerTypeHandler(typeHandler);
            }

            for (Class<? extends IDataObject> dataType : typeEvent.getDataTypes()) {
                List<String> databases = new ArrayList<>();
                if (typeEvent.getTypeToDatabases().containsKey(dataType.getName())) {
                    databases.addAll(typeEvent.getTypeToDatabases().get(dataType.getName()));
                }
                
                databaseManager.registerObjectAsTable(dataType, databases.toArray(new String[0])); //TODO This may need to be changed
            }
            
            databaseManager.setup();
            
            pluginManager.callEvent(new PostDataSetupEvent(starData));
        }, 1L);
    }
}
