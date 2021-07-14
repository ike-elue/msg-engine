package com.msgeng.engine;

import java.util.ArrayList;
import java.util.List;

public class EngineManager {
	   private final List<Engine> engines;
	    
	    public EngineManager() {
	        engines = new ArrayList<>();
	    }

	    public final void init() {
	        engines.stream().forEach((e) -> {
	            e.init();
	        });
	    }

	    
	    public final void addEngine(Engine engine) {
	        engines.add(engine);
	    }

	    public final Engine findEngine(int id) {
	        for (Engine e : engines) {
	            if (e.getId() == id) return e;
	        }
	        return null;
	    }
	    
	    public final Engine findEngine(String alias) {
	        for (Engine e : engines) {
	            if (e.getAlias().equalsIgnoreCase(alias)) return e;
	        }
	        return null;
	    }
	    
	    public final int getEngineId(String alias) {
	        Engine e = findEngine(alias);
	        if(e == null) return -1;
	        return e.getId();
	    }

	    public final void removeEngine(String alias) {
	        Engine ee = null;
	        for (Engine e : engines) {
	            if (e.getAlias().equalsIgnoreCase(alias)) ee = e;
	        }

	        if (ee == null) {
	            return;
	        }

	        engines.remove(ee);
	    }
	    
	    public final void disposeEngines() {
	        engines.stream().forEach((e) -> {
	            e.cleanUp();
	        });
	    }

	    public final List<Engine> getEngines() {
	        return engines;
	    }
}
