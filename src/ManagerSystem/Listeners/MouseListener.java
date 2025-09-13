
package ManagerSystem.Listeners;

import ManagerSystem.Handlers.HandlerSystem.HandlerClick;
import java.awt.Point;
import java.awt.event.MouseEvent;

public class MouseListener implements java.awt.event.MouseListener{

    @Override
    public void mouseClicked(MouseEvent e){
    }

    @Override
    public void mousePressed(MouseEvent e){
        int x = e.getX();
        int y = e.getY();
        Point p = new Point(x, y);
        switch(e.getButton()){
            case 1 -> HandlerClick.clickLeftPressed(p);
            case 3 -> HandlerClick.clickRightPressed(p);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e){
        switch(e.getButton()){
            case 1 -> HandlerClick.clickLeftReleased();
            case 3 -> HandlerClick.clickRightReleased();
        }
    }

    @Override
    public void mouseEntered(MouseEvent e){
    }

    @Override
    public void mouseExited(MouseEvent e){
    }

}
