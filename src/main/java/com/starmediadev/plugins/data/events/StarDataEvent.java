package com.starmediadev.plugins.data.events;

import com.starmediadev.data.StarData;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class StarDataEvent extends Event {
    
    private static HandlerList handlerList = new HandlerList();
    
    private StarData starData;

    public StarDataEvent(StarData starData) {
        this.starData = starData;
    }

    public StarData getStarData() {
        return starData;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }
    
    public HandlerList getHandlers() {
        return handlerList;
    }
}
