package MainSystem.Object;

import DataSystem.Data.Player;
import DataSystem.ID.IDObject;
import DataSystem.Interface.Clickable;
import DataSystem.Interface.Renderable;
import DataSystem.Interface.Tickable;
import MainSystem.Abstract.AbstractObject;
import MainSystem.Methods.MethodsColor;
import ManagerSystem.Handlers.HandlerPlayers;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Ellipse2D.Double;

public class PlayerIndicate extends AbstractObject implements Renderable, Tickable, Clickable{

    public Player player;
    public String name;
    private double angle = 0.0D;
    private double currentRadius = 0.0D;
    private double currentLeaderOffset = 0.0D;
    private double[] currentSize = new double[]{12.0, 0.0, 0.0, 0.0};
    private boolean initialized = false;

    private double targetRadius = 0.0D;
    private double targetLeaderOffset = 0.0D;
    private double[] targetSize = new double[4];
    
    private boolean leftPressed = false;
    private boolean rightPressed = false;

    public PlayerIndicate(double x, double y, Player player){
        super(x, y, new Dimension(0, 0), IDObject.PlayerIndicate);
        this.player = player;
        if(player.name != null){
            this.name = player.name;
        }else{
            this.name = "Player" + player.ordinal();
        }
    }

    @Override
    public void reset(){
    }

    private boolean hasMostAtoms() {
        if (this.player.atoms <= 0) return false;
        for (Player p : Player.values()) {
            if (p != Player.Dead && p != this.player) {
                if (p.atoms >= this.player.atoms) {
                    return false;
                }
            }
        }
        return true;
    }

    private void updateTargets() {
        if (main.handlerPlayers.checkPlayer(player)) {
            // State 1: Active Turn (4 atoms orbiting)
            targetRadius = 10.0D;
            targetLeaderOffset = 0.0D;
            targetSize[0] = 12.0D;
            targetSize[1] = 12.0D;
            targetSize[2] = 12.0D;
            targetSize[3] = 12.0D;
        } else {
            // Inactive
            if (hasMostAtoms()) {
                // State 2: Inactive Turn, Leading (2 atoms)
                targetRadius = 0.0D;
                targetLeaderOffset = 6.0D;
                targetSize[0] = 12.0D;
                targetSize[1] = 12.0D;
                targetSize[2] = 0.0D;
                targetSize[3] = 0.0D;
            } else {
                // State 3: Inactive Turn, Normal (1 atom)
                targetRadius = 0.0D;
                targetLeaderOffset = 0.0D;
                targetSize[0] = 12.0D;
                targetSize[1] = 0.0D;
                targetSize[2] = 0.0D;
                targetSize[3] = 0.0D;
            }
        }
    }

    @Override
    public void tick(){
        this.angle += 0.6D;
        if(this.angle >= 360.0D){
            this.angle -= 360.0D;
        }

        if (!initialized) {
            initialized = true;
            updateTargets();
            currentRadius = targetRadius;
            currentLeaderOffset = targetLeaderOffset;
            for (int i = 0; i < 4; i++) {
                currentSize[i] = targetSize[i];
            }
        } else {
            updateTargets();
            double lerpFactor = 0.010D; // Smooth transition over ~230ms
            currentRadius += (targetRadius - currentRadius) * lerpFactor;
            currentLeaderOffset += (targetLeaderOffset - currentLeaderOffset) * lerpFactor;
            for (int i = 0; i < 4; i++) {
                currentSize[i] += (targetSize[i] - currentSize[i]) * lerpFactor;
            }
        }
    }

// Renderable ================================================================================================
    
    @Override
    public boolean isRenderReady(){
        return true;
    }

    @Override
    public void renderLayer1(Graphics2D g){
        if(player.atoms == 0){
            g.setColor(Color.gray);
        }else{
            g.setColor(this.player.color);
        }

        double[][] positions = new double[4][2];

        // Atom 0: left offset + orbit 0
        double radians0 = this.angle / 180.0D * Math.PI;
        positions[0][0] = -currentLeaderOffset + currentRadius * Math.cos(-radians0);
        positions[0][1] = currentRadius * Math.sin(-radians0);

        // Atom 1: right offset + orbit 90
        double radians1 = (this.angle + 90.0D) / 180.0D * Math.PI;
        positions[1][0] = currentLeaderOffset + currentRadius * Math.cos(-radians1);
        positions[1][1] = currentRadius * Math.sin(-radians1);

        // Atom 2: orbit 180
        double radians2 = (this.angle + 180.0D) / 180.0D * Math.PI;
        positions[2][0] = currentRadius * Math.cos(-radians2);
        positions[2][1] = currentRadius * Math.sin(-radians2);

        // Atom 3: orbit 270
        double radians3 = (this.angle + 270.0D) / 180.0D * Math.PI;
        positions[3][0] = currentRadius * Math.cos(-radians3);
        positions[3][1] = currentRadius * Math.sin(-radians3);

        for (int i = 0; i < 4; i++) {
            if (currentSize[i] > 0.1D) {
                double drawX = this.x + positions[i][0] + (12.0D - currentSize[i]) / 2.0D;
                double drawY = this.y + positions[i][1] + (12.0D - currentSize[i]) / 2.0D;
                g.fill(new Double(drawX, drawY, currentSize[i], currentSize[i]));
            }
        }

        g.setColor(Color.white);
        g.drawString(this.name, this.getX() + 28, this.getY());
        if(this.player.atoms == -1){
            g.drawString("0", this.getX() + 28, this.getY() + 19);
        }else{
            g.drawString(String.valueOf(this.player.atoms), this.getX() + 28, this.getY() + 19);
        }
    }

    @Override
    public void renderLayer2(Graphics2D g){
    }

    @Override
    public void renderLayer3(Graphics2D g){
    }

    @Override
    public void renderLayer4(Graphics2D g){
    }
    
    @Override
    public void renderLayer5(Graphics2D g){
    }
    
// ===========================================================================================================
    
    @Override
    public void clickLeftPressed(){
        if(!this.leftPressed){
            boolean valid = true;
            Color newColor = this.player.color;

            do{
                valid = true;
                newColor = MethodsColor.getNextAvailableColor(newColor);
                Player[] var3 = Player.values();
                int var4 = var3.length;

                for(int var5 = 0; var5 < var4; ++var5){
                    Player p = var3[var5];
                    if(p.color == newColor){
                        valid = false;
                        break;
                    }
                }
            }while(!valid);

            this.player.color = newColor;
        }

        this.leftPressed = true;
    }

    @Override
    public void clickLeftReleased(){
        this.leftPressed = false;
    }

    @Override
    public void clickRightPressed(){
        if(!this.rightPressed){
            player.hintEnabled = !player.hintEnabled;
        }
        this.rightPressed = true;
    }

    @Override
    public void clickRightReleased(){
        this.rightPressed = false;
    }

    @Override
    public void clickMiddlePressed(){
    }

    @Override
    public Rectangle getClickableBounds(){
        return new Rectangle(this.getX(), this.getY(), 12, 12);
    }

}
