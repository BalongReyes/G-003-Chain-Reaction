package DataSystem.Interface;

import java.awt.Rectangle;

public interface Clickable{

    void clickLeftPressed();
    void clickLeftReleased();
    void clickRightPressed();
    void clickRightReleased();
    Rectangle getClickableBounds();
    
}
