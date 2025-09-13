package ManagerSystem.Handlers.HandlerSystem;

import DataSystem.Interface.Tickable;
import MainSystem.Abstract.AbstractObject;
import MainSystem.Main.Main;
import java.awt.Point;
import java.util.ArrayList;

public class HandlerTick{

    public static Main main;
    
    public static ArrayList<Tickable> tickable = new ArrayList();
    public static boolean pause = false;
    public static Point cursorLocation;

    public static void add(AbstractObject o){
        if(o instanceof Tickable t){
            tickable.add(t);
        }
    }

    public static void remove(AbstractObject o){
        if(o instanceof Tickable t){
            tickable.remove(t);
        }
    }

    public static void set(AbstractObject oldO, AbstractObject newO){
        if(oldO instanceof Tickable oldT){
            if(newO instanceof Tickable newT){
                tickable.set(tickable.indexOf(oldT), newT);
            }
        }
    }

    public static boolean check(AbstractObject o){
        return o instanceof Tickable;
    }

    public static Tickable[] getArray(){
        return tickable.toArray(Tickable[]::new);
    }

    public static void tick(){
        if(!pause){
            cursorLocation = main.window.getMouseLocation();
            tickable.forEach((t) -> {
                t.tick();
            });
            main.tick();
        }
    }
}
