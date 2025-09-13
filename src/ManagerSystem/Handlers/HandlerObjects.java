package ManagerSystem.Handlers;

import MainSystem.Abstract.AbstractObject;
import ManagerSystem.Handlers.HandlerObject.HandlerCell;
import ManagerSystem.Handlers.HandlerSystem.HandlerClick;
import ManagerSystem.Handlers.HandlerSystem.HandlerRender;
import ManagerSystem.Handlers.HandlerSystem.HandlerTick;
import java.util.ArrayList;

public class HandlerObjects{

    public static ArrayList<AbstractObject> objects = new ArrayList();

    public static void add(AbstractObject o){
        if(o != null){
            objects.add(o);
            HandlerTick.add(o);
            HandlerRender.add(o);
            HandlerClick.add(o);
            HandlerCell.add(o);
        }
    }

    public static void remove(AbstractObject o){
        if(o != null){
            objects.remove(o);
            HandlerTick.remove(o);
            HandlerRender.remove(o);
            HandlerClick.remove(o);
            HandlerCell.remove(o);
        }
    }

    public static AbstractObject[] getArray(){
        return objects.toArray(AbstractObject[]::new);
    }

    public static void RemoveAll(){
        for(AbstractObject o : objects){
            remove(o);
        }
        objects.clear();
    }
}
