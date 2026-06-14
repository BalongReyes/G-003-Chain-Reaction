package ManagerSystem.Handlers;

import MainSystem.Abstract.AbstractObject;
import MainSystem.Main.Main;
import ManagerSystem.Handlers.HandlerObject.HandlerCell;
import ManagerSystem.Handlers.HandlerSystem.HandlerClick;
import ManagerSystem.Handlers.HandlerSystem.HandlerRender;
import ManagerSystem.Handlers.HandlerSystem.HandlerTick;
import java.util.ArrayList;

public class HandlerObjects{

    private final java.util.List<AbstractObject> objects = new ArrayList<>();
    private AbstractObject[] objectsCache = new AbstractObject[0];
    private boolean dirty = false;

    public void add(AbstractObject o, Main main){
        if(o != null){
            objects.add(o);
            dirty = true;
            main.handlerTick.add(o);
            main.handlerRender.add(o);
            main.handlerClick.add(o);
            main.handlerCell.add(o);
        }
    }

    public void remove(AbstractObject o, Main main){
        if(o != null){
            objects.remove(o);
            dirty = true;
            main.handlerTick.remove(o);
            main.handlerRender.remove(o);
            main.handlerClick.remove(o);
            main.handlerCell.remove(o);
        }
    }

    public AbstractObject[] getArray(){
        if(dirty){
            objectsCache = objects.toArray(AbstractObject[]::new);
            dirty = false;
        }
        return objectsCache;
    }

    public void RemoveAll(Main main){
        for(AbstractObject o : getArray()){
            remove(o, main);
        }
        objects.clear();
        dirty = true;
    }
}
