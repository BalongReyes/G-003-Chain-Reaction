package ManagerSystem.Handlers.HandlerSystem;

import DataSystem.Interface.Tickable;
import MainSystem.Abstract.AbstractObject;
import MainSystem.Main.Main;
import java.awt.Point;
import java.util.ArrayList;

public class HandlerTick{

    private final java.util.List<Tickable> tickable = new ArrayList<>();
    private Tickable[] tickableCache = new Tickable[0];
    private boolean dirty = false;
    
    public boolean pause = false;
    public Point cursorLocation;

    public void add(AbstractObject o){
        if(o instanceof Tickable t){
            tickable.add(t);
            dirty = true;
        }
    }

    public void remove(AbstractObject o){
        if(o instanceof Tickable t){
            tickable.remove(t);
            dirty = true;
        }
    }

    public void set(AbstractObject oldO, AbstractObject newO){
        if(oldO instanceof Tickable oldT){
            if(newO instanceof Tickable newT){
                tickable.set(tickable.indexOf(oldT), newT);
                dirty = true;
            }
        }
    }

    public boolean check(AbstractObject o){
        return o instanceof Tickable;
    }

    public Tickable[] getArray(){
        if(dirty){
            tickableCache = tickable.toArray(Tickable[]::new);
            dirty = false;
        }
        return tickableCache;
    }

    public void tick(Main main){
        if(!pause){
            cursorLocation = main.window.getMouseLocation();
            for(Tickable t : getArray()){
                t.tick();
            }
            main.tick();
        }
    }
}
