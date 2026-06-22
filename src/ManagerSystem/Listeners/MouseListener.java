package ManagerSystem.Listeners;

import ManagerSystem.Handlers.HandlerSystem.HandlerClick;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import MainSystem.Main.Main;

public class MouseListener implements java.awt.event.MouseListener, MouseMotionListener{

    private final Main main;
    private Point mouseLocation = null;

    public MouseListener(Main main) {
        this.main = main;
    }

    public Point getMouseLocation() {
        return mouseLocation;
    }

    @Override
    public void mouseClicked(MouseEvent e){
    }

    @Override
    public void mousePressed(MouseEvent e){
        int x = e.getX();
        int y = e.getY();
        Point p = new Point(x, y);
        switch(e.getButton()){
            case 1 -> main.handlerClick.clickLeftPressed(p);
            case 2 -> main.handlerClick.clickMiddlePressed(p);
            case 3 -> main.handlerClick.clickRightPressed(p);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e){
        int x = e.getX();
        int y = e.getY();
        Point p = new Point(x, y);
        switch(e.getButton()){
            case 1 -> main.handlerClick.clickLeftReleased(p);
            case 3 -> main.handlerClick.clickRightReleased(p);
        }
    }

    @Override
    public void mouseEntered(MouseEvent e){
        mouseLocation = e.getPoint();
    }

    @Override
    public void mouseExited(MouseEvent e){
        mouseLocation = null;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseLocation = e.getPoint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mouseLocation = e.getPoint();
    }

}
