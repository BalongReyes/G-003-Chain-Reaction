package ManagerSystem.Handlers.HandlerSystem;

import DataSystem.Interface.Clickable;
import MainSystem.Abstract.AbstractObject;
import java.awt.Point;
import java.util.ArrayList;

public class HandlerClick{

    public static ArrayList<Clickable> clickable = new ArrayList();

    public static void add(AbstractObject o){
        if(o instanceof Clickable c){
            clickable.add(c);
        }
    }

    public static void remove(AbstractObject o){
        if(o instanceof Clickable c){
            clickable.remove(c);
        }
    }

    public static void set(AbstractObject oldO, AbstractObject newO){
        if(oldO instanceof Clickable oldT){
            if(newO instanceof Clickable newT){
                clickable.set(clickable.indexOf(oldT), newT);
            }
        }
    }

    public static boolean check(AbstractObject o){
        return o instanceof Clickable;
    }

    public static Clickable[] getArray(){
        return clickable.toArray(Clickable[]::new);
    }

    public static void clickLeftPressed(Point p){
        clickable.forEach((c) -> {
            if(c.getClickableBounds().contains(p)){
                c.clickLeftPressed();
            }
        });
    }

    public static void clickLeftReleased(){
        clickable.forEach((c) -> {
            if(c.isLeftPressed()){
                c.clickLeftReleased();
            }
        });
    }

    public static void clickRightPressed(Point p){
        clickable.forEach((c) -> {
            if(c.getClickableBounds().contains(p)){
                c.clickRightPressed();
            }
        });
    }

    public static void clickRightReleased(){
        clickable.forEach((c) -> {
            if(c.isRightPressed()){
                c.clickRightReleased();
            }
        });
    }
}
