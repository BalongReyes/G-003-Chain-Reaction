package DataSystem.Interface;

import java.awt.Rectangle;

public interface Clickable{

    void clickLeftPressed();
    void clickLeftReleased();
    boolean isLeftPressed();
    void clickRightPressed();
    void clickRightReleased();
    boolean isRightPressed();
    Rectangle getClickableBounds();
    
}
