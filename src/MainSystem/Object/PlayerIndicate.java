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
    private double radius = 0.0D;
    private int radiusTick = 0;
    
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

    @Override
    public void tick(){
        if(this.radiusTick > 0){
            --this.radiusTick;
        }

        this.angle += this.radius != 0.0D && this.radius != 8.0D ? 0.7D : 0.4D;
        if(this.angle >= 360.0D){
            this.angle = 0.0D;
        }

        if(HandlerPlayers.checkPlayer(player)){
            if(this.radius < 8.0D && this.radiusTick <= 0){
                this.radius += 0.5D;
                this.radiusTick = 20;
            }
        }else if(this.radius > 0.0D && this.radiusTick <= 0){
            this.radius -= 0.5D;
            this.radiusTick = 40;
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

        if(this.radius > 0.0D){
            for(int i = 0; i <= 2; ++i){
                double radians = (this.angle + (double) (i * 120)) / 180.0D * Math.PI;
                double x1 = this.radius * Math.cos(-radians);
                double y1 = this.radius * Math.sin(-radians);
                g.fill(new Double(this.x + x1, this.y + y1, 12.0D, 12.0D));
            }
        }else{
            g.fill(new Double(this.x, this.y, 12.0D, 12.0D));
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
    public boolean isLeftPressed(){
        return this.leftPressed;
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
    public boolean isRightPressed(){
        return this.rightPressed;
    }

    @Override
    public Rectangle getClickableBounds(){
        return new Rectangle(this.getX(), this.getY(), 12, 12);
    }

}
