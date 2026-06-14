package ManagerSystem.Handlers.HandlerSystem;

import DataSystem.Interface.Renderable;
import MainSystem.Abstract.AbstractObject;
import MainSystem.Main.Console;
import MainSystem.Main.Main;
import java.awt.Graphics2D;
import java.util.ArrayList;

public class HandlerRender{

    private final java.util.List<Renderable> renderable = new ArrayList<>();
    private Renderable[] renderableCache = new Renderable[0];
    private boolean dirty = false;
    
    public boolean pause = false;
    public boolean rendering = false;

    public void add(AbstractObject o){
        if(o instanceof Renderable r){
            renderable.add(r);
            dirty = true;
        }
    }

    public void remove(AbstractObject o){
        if(o instanceof Renderable r){
            renderable.remove(r);
            dirty = true;
        }
    }

    public void set(AbstractObject oldO, AbstractObject newO){
        if(oldO instanceof Renderable oldR){
            if(newO instanceof Renderable newR){
                renderable.set(renderable.indexOf(oldR), newR);
                dirty = true;
            }
        }
    }

    public boolean check(AbstractObject o){
        return o instanceof Renderable;
    }

    public Renderable[] getArray(){
        if(dirty){
            renderableCache = renderable.toArray(Renderable[]::new);
            dirty = false;
        }
        return renderableCache;
    }

    public int showRenderLayer = 0;
    
    public void render(Graphics2D g, Main main){
        if(!pause){
            rendering = true;
            main.preRender(g);
            try{
                if(showRenderLayer == 0 || showRenderLayer == 1) for(Renderable r : getArray()){
                    if(r.isRenderReady()) r.renderLayer1(g);
                }
                if(showRenderLayer == 0 || showRenderLayer == 2) for(Renderable r : getArray()){
                    if(r.isRenderReady()) r.renderLayer2(g);
                }
                if(showRenderLayer == 0 || showRenderLayer == 3) for(Renderable r : getArray()){
                    if(r.isRenderReady()) r.renderLayer3(g);
                }
                if(showRenderLayer == 0 || showRenderLayer == 4) for(Renderable r : getArray()){
                    if(r.isRenderReady()) r.renderLayer4(g);
                }
                if(showRenderLayer == 0 || showRenderLayer == 5) for(Renderable r : getArray()){
                    if(r.isRenderReady()) r.renderLayer5(g);
                }
            }catch(Exception var2){
                Console.out("\nRender Error:", true);
                Console.out(var2.getMessage(), true);
            }
            rendering = false;
        }
    }
}
