package com.msgeng.engine;

import com.msgeng.core.Core;
import com.msgeng.message.Message;

public abstract class Engine {
	
	private static int idCounter = 0;
	
	private final String alias;
    private final int id;
    private final Core core;
    private final String[] otherWantedEngines;
    private final EngineManager em;
    
    
    public Engine(String alias, Core core, String[] otherWantedEngines, EngineManager em) {
        this.alias = alias;
        this.core = core;
        this.otherWantedEngines = otherWantedEngines;
        this.em = em;
        this.id = getNextId();
    }
    
    private static int getNextId() {
    	idCounter++;
    	return idCounter;
    }
     
    public abstract void init();
    
    public void sendMessage(Message newMessage) {
        core.getMessageBus().addMessage(newMessage);
    }
    
    public void updateWithCurrentMessage(Message m, double delta) {
        if(m.getSenderId() == id || onList(m.getSenderId()))
            update(m, delta);
    }
    
    private boolean onList(int id) {
        if(otherWantedEngines == null)
            return false;
        if(otherWantedEngines[0].equalsIgnoreCase("all"))
            return true;
        for(String s : otherWantedEngines) {
            if(em.getEngineId(s) == id) return true;
        }
        return false;
    }
    
    public abstract void update(Message m, double delta);
    
    public abstract void dispose();

    public final void setRunning(boolean running) {
        core.setRunning(running);
    }
    
    protected void disposalLine() {
        System.out.println("Cleaned up the " + getAlias() + " system!");
    }
    
    public final void cleanUp() {
        dispose();
        disposalLine();
    }

    public final String getAlias() {
        return alias;
    }

    public final int getId() {
        return id;
    }

    public final Core getCore() {
        return core;
    }
}
