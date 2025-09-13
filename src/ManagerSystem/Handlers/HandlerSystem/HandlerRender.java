package ManagerSystem.Handlers.HandlerSystem;

import DataSystem.Interface.Renderable;
import MainSystem.Abstract.AbstractObject;
import MainSystem.Main.Console;
import MainSystem.Main.Main;
import java.awt.Graphics2D;
import java.util.ArrayList;

public class HandlerRender{

    public static Main main;
    
    public static ArrayList<Renderable> renderable = new ArrayList();
    
    public static boolean pause = false;
    public static boolean rendering = false;

    public static void add(AbstractObject o){
        if(o instanceof Renderable r){
            renderable.add(r);
        }
    }

    public static void remove(AbstractObject o){
        if(o instanceof Renderable r){
            renderable.remove(r);
        }
    }

    public static void set(AbstractObject oldO, AbstractObject newO){
        if(oldO instanceof Renderable oldR){
            if(newO instanceof Renderable newR){
                renderable.set(renderable.indexOf(oldR), newR);
            }
        }
    }

    public static boolean check(AbstractObject o){
        return o instanceof Renderable;
    }

    public static Renderable[] getArray(){
        return renderable.toArray(Renderable[]::new);
    }

    public static int showRenderLayer = 0;
    
    public static void render(Graphics2D g){
        if(!pause){
            rendering = true;
            main.preRender(g);
            try{
                if(showRenderLayer == 0 || showRenderLayer == 1) renderable.forEach((r) -> {
                    if(r.isRenderReady()){
                        r.renderLayer1(g);
                    }
                });
                if(showRenderLayer == 0 || showRenderLayer == 2) renderable.forEach((r) -> {
                    if(r.isRenderReady()){
                        r.renderLayer2(g);
                    }
                });
                if(showRenderLayer == 0 || showRenderLayer == 3) renderable.forEach((r) -> {
                    if(r.isRenderReady()){
                        r.renderLayer3(g);
                    }
                });
                if(showRenderLayer == 0 || showRenderLayer == 4) renderable.forEach((r) -> {
                    if(r.isRenderReady()){
                        r.renderLayer4(g);
                    }
                });
                if(showRenderLayer == 0 || showRenderLayer == 5) renderable.forEach((r) -> {
                    if(r.isRenderReady()){
                        r.renderLayer5(g);
                    }
                });
            }catch(Exception var2){
                Console.out("\nRender Error:", true);
                Console.out(var2.getMessage(), true);
            }
            rendering = false;
        }
    }
}
