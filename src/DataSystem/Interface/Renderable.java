package DataSystem.Interface;

import java.awt.Graphics2D;

public interface Renderable{
    
    boolean isRenderReady();

    void renderLayer1(Graphics2D g);
    void renderLayer2(Graphics2D g);
    void renderLayer3(Graphics2D g);
    void renderLayer4(Graphics2D g);
    void renderLayer5(Graphics2D g);
    
}
