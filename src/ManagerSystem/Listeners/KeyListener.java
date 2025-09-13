
package ManagerSystem.Listeners;

import MainSystem.Main.Main;
import ManagerSystem.Handlers.HandlerPlayers;
import ManagerSystem.Handlers.HandlerSystem.HandlerRender;
import ManagerSystem.Handlers.HandlerSystem.HandlerTick;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class KeyListener extends KeyAdapter{

    public static Main main;
    public static boolean shift = false;

    @Override
    public void keyPressed(KeyEvent e){
        int key = e.getKeyCode();
        switch(key){
            case KeyEvent.VK_SHIFT -> {
                shift = true;
            }
            case KeyEvent.VK_SPACE -> {
                HandlerTick.pause = !HandlerTick.pause;
            }
            case KeyEvent.VK_F1 -> {
                HandlerPlayers.nextPlayer();
            }
            case KeyEvent.VK_F2 -> {
                HandlerPlayers.nextPlayerForced();
            }
            case KeyEvent.VK_F3 -> {
                HandlerRender.showRenderLayer++;
                if(HandlerRender.showRenderLayer > 5){
                    HandlerRender.showRenderLayer = 0;
                }
            }
            case KeyEvent.VK_F4 -> {
                main.tProcess.toggleSlowmo = true;
            }
            case KeyEvent.VK_F6 -> {
                main.reset();
            }
            case KeyEvent.VK_F11 -> {
                main.tProcess.toggleFullscreen = true;
            }
        }

        if(e.isControlDown()) { 
            switch(key){
                case KeyEvent.VK_Z -> {
                    main.undoStates();
                }
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e){
        int key = e.getKeyCode();
        switch(key){
            case KeyEvent.VK_SHIFT -> {
                shift = false;
            }
        }
    }
    
}
