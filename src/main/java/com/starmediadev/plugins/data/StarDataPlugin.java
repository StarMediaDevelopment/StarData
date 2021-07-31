package com.starmediadev.plugins.data;

import com.starmediadev.data.StarData;
import com.starmediadev.data.manager.DatabaseManager;
import com.starmediadev.data.manager.MultidatabaseManager;
import com.starmediadev.data.properties.SqlProperties;
import com.starmediadev.utils.Code;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

public class StarDataPlugin extends JavaPlugin {
    
    private StarData starData;
    private DatabaseManager databaseManager;

    public void onEnable() {
        this.starData = new StarData(getLogger());
        databaseManager = new MultidatabaseManager(starData);
        starData.setDatabaseManager(databaseManager);
        //Databases

        SqlProperties sqlProperties = new SqlProperties().setHost("localhost").setUsername("root").setPassword("niles3408").setPort(3306);
        
        databaseManager.setupDatabase(sqlProperties.clone().setDatabase("test"));
        databaseManager.setupDatabase(sqlProperties.clone().setDatabase("test2"));
        databaseManager.setupDatabase(sqlProperties.clone().setDatabase("test3"));
        
        databaseManager.registerObjectAsTable(TestOne.class, "test");
        databaseManager.registerObjectAsTable(TestTwo.class, "test2");
        databaseManager.registerObjectAsTable(TestThree.class, "test3");
        
        databaseManager.setup();
        
        var testOneData = databaseManager.getAllData(TestOne.class, null, null);
        var testThreeData = databaseManager.getAllData(TestThree.class, null, null);

        for (TestOne tod : testOneData) {
            getLogger().info(tod.toString());
        }

        for (TestThree ttd : testThreeData) {
            getLogger().info(ttd.toString());
        }
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!cmd.getName().equalsIgnoreCase("testdatabase")) {
            return true;
        }

        Random random = new Random();
        
        for (int i = 0; i < random.nextInt(9) + 1; i++) {
            TestOne testOne = new TestOne(new Code(8).toString());
            for (int j = 0; j < random.nextInt(4) + 1; j++) {
                testOne.generateTestTwo();
            }
            databaseManager.saveData(testOne);
        }
        
        for (int i = 0; i < random.nextInt(5) + 1; i++) {
            TestThree testThree = new TestThree(new Code(10).toString());
            databaseManager.saveData(testThree);
        }
        
        return true;
    }
}
