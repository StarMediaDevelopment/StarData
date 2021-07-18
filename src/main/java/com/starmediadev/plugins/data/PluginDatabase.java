package com.starmediadev.plugins.data;

import com.starmediadev.data.model.MysqlDatabase;
import com.starmediadev.data.properties.SqlProperties;
import com.starmediadev.data.registries.DataObjectRegistry;
import com.starmediadev.data.registries.TypeRegistry;
import org.bukkit.plugin.java.JavaPlugin;

public class PluginDatabase extends MysqlDatabase {
    
    private JavaPlugin plugin;
    private DataObjectRegistry dataObjectRegistry;
    
    public PluginDatabase(JavaPlugin plugin, SqlProperties properties, TypeRegistry typeRegistry, DataObjectRegistry dataObjectRegistry) {
        super(plugin.getLogger(), properties, typeRegistry);
        this.plugin = plugin;
        this.dataObjectRegistry = dataObjectRegistry;
    }
}
