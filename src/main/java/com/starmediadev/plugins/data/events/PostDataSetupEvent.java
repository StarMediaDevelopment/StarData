package com.starmediadev.plugins.data.events;

import com.starmediadev.data.StarData;
import com.starmediadev.data.manager.DatabaseManager;

public class PostDataSetupEvent extends StarDataEvent {
    public PostDataSetupEvent(StarData starData) {
        super(starData);
    }
    
    public DatabaseManager getDatabaseManager() {
        return getStarData().getDatabaseManager();
    }
}
