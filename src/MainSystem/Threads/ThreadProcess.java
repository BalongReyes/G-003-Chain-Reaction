package MainSystem.Threads;

import MainSystem.Main.Console;
import MainSystem.Main.Main;
import ManagerSystem.Handlers.HandlerPlayers;
import ManagerSystem.Handlers.HandlerSystem.HandlerRender;
import ManagerSystem.Handlers.HandlerSystem.HandlerTick;
import ManagerSystem.Listeners.KeyListener;
import Settings.SettingsSystem;
import Settings.SettingsWindow;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferStrategy;

public class ThreadProcess implements Runnable{

    public static Main main;
    
    private static final double SECOND_TO_NANO = 1.0E9D;
    private static final float NINETY_PERCENT = 0.9F;
    private static final float TEN_PERCENT = 0.1F;
    
    public boolean toggleFullscreen = false;
    public boolean toggleSlowmo = false;
    private static final RenderingHints rh;
    public Thread threadProcess;
    public boolean running = false;
    public boolean exit = false;

    @Override
    public void run(){
        int w = main.canvasSize.width;
        int h = main.canvasSize.height;
        int lastTick = -1;
        int renderedFrames = 0;
        int fps = 0;
        boolean naiveTiming = true;
        double time = (double) System.nanoTime() / 1.0E9D;
        double now = time;
        double averagePassedTime = 0.0D;

        while(this.running){
            if(this.toggleSlowmo){
                if(SettingsSystem.ticks == 1000){
                    SettingsSystem.ticks = 100;
                }else{
                    SettingsSystem.ticks = 1000;
                }

                lastTick = -1;
                renderedFrames = 0;
                fps = 0;
                this.toggleSlowmo = false;
            }

            if(this.toggleFullscreen){
                main.window.toggleFullscreen();
                this.toggleFullscreen = false;
            }else{
                BufferStrategy bs;
                do{
                    bs = main.window.canvas.getBufferStrategy();
                    if(bs == null){
                        main.window.canvas.createBufferStrategy(2);
                    }
                }while(bs == null);

                Graphics g = bs.getDrawGraphics();
                double lastTime = time;
                time = (double) System.nanoTime() / 1.0E9D;
                double passedTime = time - lastTime;
                if(passedTime < 0.0D){
                    naiveTiming = false;
                }

                averagePassedTime = averagePassedTime * 0.8999999761581421D + passedTime * 0.10000000149011612D;
                if(naiveTiming){
                    now = time;
                }else{
                    now += averagePassedTime;
                }

                int tick = (int) (now * (double) SettingsSystem.ticks);
                if(lastTick == -1){
                    lastTick = tick;
                }

                while(lastTick < tick){
                    HandlerTick.tick();
                    ++lastTick;
                    if(lastTick % SettingsSystem.ticks == 0){
                        fps = renderedFrames;
                        renderedFrames = 0;
                    }
                }

                g.setColor(SettingsWindow.backgroundColor);
                g.fillRect(0, 0, w, h);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHints(rh);
                HandlerRender.render(g2);
                main.window.jLabel1.setText("FPS: " + fps +
                    (main.isSimulating() ? "   (Simulating)" : "") + 
                    (main.isSimulating() ? "   (" + main.getSimulatePhase() + ")" : "") + 
                    (main.isCellFocused() ? "   (Focused)" : "") + 
                    (main.isCellHideHint() ? "   (Hide Hint)" : "") + 
                    (HandlerPlayers.getPlayerMoves() > 0 ? "   (PlayerMoves: " + HandlerPlayers.getPlayerMoves() + ")" : "") +
                    (KeyListener.shift ? "   (Shift)" : "") + (HandlerTick.pause ? "   (Paused)" : "") +
                    (SettingsSystem.ticks != 1000 ? "   (Tick: " + SettingsSystem.ticks + ")" : "") +
                    (HandlerRender.showRenderLayer != 0 ? "   (Showing Layer: " + HandlerRender.showRenderLayer + ")" : "")
                );
                bs.show();
                g.dispose();
                ++renderedFrames;
                if(this.exit){
                    break;
                }
            }
        }

        this.running = false;
    }

    public synchronized void start(){
        if(!this.running){
            Console.out("ThreadProcess\tStarted", "\u001b[0;32m");
            this.threadProcess = new Thread(this, "ThreadProcess");
            this.threadProcess.start();
            this.running = true;
        }
    }

    public synchronized void stop(){
        Console.out("ThreadProcess\tClosing", "\u001b[0;31m");
        this.exit = true;

        while(this.running){
            try{
                this.wait(10L);
            }catch(InterruptedException var2){
            }
        }

        Console.out("ThreadProcess\tClosed", "\u001b[0;31m");
    }

    static{
        rh = new RenderingHints(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    }
}
