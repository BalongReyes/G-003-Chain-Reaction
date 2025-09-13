
package MainSystem.Main;

import ManagerSystem.Listeners.KeyListener;
import ManagerSystem.Listeners.MouseListener;
import Settings.SettingsWindow;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import javax.swing.JFrame;

public class Window extends JFrame {

    public static Main main;
    public Dimension size;
    
    private Dimension frameSize;
    
    public Window(Dimension size, int onScreen){
        this.size = size;
        initComponents();
        setSize(size);
        setListeners();
        
        try{
            Thread.sleep(100);
        }catch(InterruptedException e){
        }
        
        setLocationByPlatform(true);
        getContentPane().setBackground(SettingsWindow.backgroundColor);
        jLabel1.setBackground(SettingsWindow.backgroundColor);
        jLabel1.setFocusable(false);
        canvas.setSize(size);
        canvas.setPreferredSize(size);
        pack();
        
        this.frameSize = getSize();
        setMinimumSize(frameSize);
        
        setVisible(true);
        showOnScreen(onScreen);
    }

// Main Methods ==============================================================================================

    private void showOnScreen(int screen){
        GraphicsDevice[] gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
        Rectangle bounds;
        if(screen > -1 && screen < gd.length){
            bounds = gd[screen].getDefaultConfiguration().getBounds();
            setLocation((bounds.x + (int)((bounds.width - size.width) / 2)), (bounds.y + (int)((bounds.height - size.height) / 2)));
        }else if(gd.length > 0){
            bounds = gd[0].getDefaultConfiguration().getBounds();
            setLocation((bounds.x + (int)((bounds.width - size.width) / 2)), (bounds.y + (int)((bounds.height - size.height) / 2)));
        }else{
            throw new RuntimeException("No Screens Found");
        }
    }
    
    private boolean fullscreen = false;
    
    public void toggleFullscreen(){
        fullscreen = !fullscreen;
        
        this.dispose();
        
        if(!this.fullscreen){
            this.setUndecorated(false);
            this.setExtendedState(0);
            this.setSize(frameSize);
        }else{
            this.setUndecorated(true);
            this.setExtendedState(6);
        }
        
        this.setLocationRelativeTo((Component)null);
        this.setVisible(true);

        repaint();
    }
    
    private KeyListener keyListener = new KeyListener();
    private MouseListener mouseListener = new MouseListener();
    
    private void setListeners(){
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher((KeyEvent evt) -> {
            if(Window.getKeyLock()) return false;
            switch(evt.getID()){
                case KeyEvent.KEY_PRESSED -> {
                    keyListener.keyPressed(evt);
                }
                case KeyEvent.KEY_RELEASED -> {
                    keyListener.keyReleased(evt);
                }
                case KeyEvent.KEY_TYPED -> {
                    keyListener.keyTyped(evt);
                }
            }
            return false;
        });
        canvas.addMouseListener(mouseListener);
    }
    
    public Point getMouseLocation() {
      return this.canvas.getMousePosition();
   }
    
// Generated =================================================================================================
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        canvas = new java.awt.Canvas();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Chain Reaction");
        addWindowFocusListener(new java.awt.event.WindowFocusListener() {
            public void windowGainedFocus(java.awt.event.WindowEvent evt) {
            }
            public void windowLostFocus(java.awt.event.WindowEvent evt) {
                formWindowLostFocus(evt);
            }
        });
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        canvas.setBackground(new java.awt.Color(20, 20, 20));

        jLabel1.setFont(new java.awt.Font("Arial", 0, 10)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("FPS: 0");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(canvas, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(canvas, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        main.stopThreads();
    }//GEN-LAST:event_formWindowClosing

    private void formWindowLostFocus(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowLostFocus
        KeyListener.shift = false;
    }//GEN-LAST:event_formWindowLostFocus

    // Variables declaration - do not modify//GEN-BEGIN:variables
    public java.awt.Canvas canvas;
    public javax.swing.JLabel jLabel1;
    // End of variables declaration//GEN-END:variables

// Static Methods ============================================================================================

    private static boolean keyLock = false;
    
    public static void setKeyLock(boolean keyLock){
        Window.keyLock = keyLock;
    }
    
    public static boolean getKeyLock(){
        return keyLock;
    }
    
}
