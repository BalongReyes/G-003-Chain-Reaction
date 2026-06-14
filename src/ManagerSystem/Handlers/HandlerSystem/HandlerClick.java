package ManagerSystem.Handlers.HandlerSystem;

import DataSystem.Interface.Clickable;
import MainSystem.Abstract.AbstractObject;
import MainSystem.Object.Cell;
import java.awt.Point;
import java.util.ArrayList;

public class HandlerClick{

    private final java.util.List<Clickable> clickable = new ArrayList<>();
    private Clickable[] clickableCache = new Clickable[0];
    private boolean dirty = false;

    public void add(AbstractObject o){
        if(o instanceof Clickable c){
            clickable.add(c);
            dirty = true;
        }
    }

    public void remove(AbstractObject o){
        if(o instanceof Clickable c){
            clickable.remove(c);
            dirty = true;
        }
    }

    public void set(AbstractObject oldO, AbstractObject newO){
        if(oldO instanceof Clickable oldT){
            if(newO instanceof Clickable newT){
                clickable.set(clickable.indexOf(oldT), newT);
                dirty = true;
            }
        }
    }

    public boolean check(AbstractObject o){
        return o instanceof Clickable;
    }

    public Clickable[] getArray(){
        if(dirty){
            clickableCache = clickable.toArray(Clickable[]::new);
            dirty = false;
        }
        return clickableCache;
    }

    public boolean leftPressed = false;
    
    public void clickLeftPressed(Point p){
        for(Clickable c : getArray()){
            if(c.getClickableBounds().contains(p)){
                c.clickLeftPressed();
            }
        }
    }

    public void clickLeftReleased(Point p){
        for(Clickable c : getArray()){
            if(c.getClickableBounds().contains(p)){
                c.clickLeftReleased();
            }
        }
        leftPressed = false;
        Cell.cellLeftPressed = null;
    }

    public boolean rightPressed = false;
    
    public void clickRightPressed(Point p){
        for(Clickable c : getArray()){
            if(c.getClickableBounds().contains(p)){
                c.clickRightPressed();
            }
        }
    }

    public void clickRightReleased(Point p){
        for(Clickable c : getArray()){
            if(c.getClickableBounds().contains(p)){
                c.clickRightReleased();
            }
        }
        rightPressed = false;
        Cell.cellRightPressed = null;
    }
    
    public void clickMiddlePressed(Point p){
        for(Clickable c : getArray()){
            if(c.getClickableBounds().contains(p)){
                c.clickMiddlePressed();
            }
        }
    }
}
