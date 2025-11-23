
package MainSystem.Abstract;

import DataSystem.ID.IDObject;
import MainSystem.Main.Main;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;

public abstract class AbstractObject{
    
    public static Main main;
    
    public double x, y;
    public Dimension size;
    public IDObject idObject;

// Constructor ===============================================================================================

    public AbstractObject(double x, double y, int w, int h, IDObject idObject){
        this(x, y, new Dimension(w, h), idObject);
    }

    public AbstractObject(double x, double y, int size, IDObject idObject){
        this(x, y, new Dimension(size, size), idObject);
    }
    
    public AbstractObject(double x, double y, Dimension size, IDObject idObject){
        this.x = x;
        this.y = y;
        this.size = size;
        this.idObject = idObject;
    }
    
// Getters ===================================================================================================
    
    public IDObject getIDObject(){
        return idObject;
    }
    
    public int getX(){
        return (int)x;
    }

    public int getY(){
        return (int)y;
    }
    
    public int getX(int addX){
        return (int)x + addX;
    }

    public int getY(int addY){
        return (int)y + addY;
    }
    
    public double getDoubleX(double addX){
        return x + addX;
    }

    public double getDoubleY(double addY){
        return y + addY;
    }
    
    public int getW(){
        return size.width;
    }
    
    public int getH(){
        return size.height;
    }
    
    public int getW(int addW){
        return this.size.width + addW;
    }

    public int getH(int addH){
        return this.size.height + addH;
    }
    
    public int getXW(){
        return (int)x + size.width;
    }

    public int getYH(){
        return (int)y + size.height;
    }
    
    public int getXW(int addX){
        return (int)x + size.width + addX;
    }

    public int getYH(int addY){
        return (int)y + size.height + addY;
    }
    
    public int getXWhalf(){
        return (int)x + (size.width / 2);
    }

    public int getYHhalf(){
        return (int)y + (size.height / 2);
    }
    
    public int getXWhalf(int addX){
        return (int)x + (size.width / 2) + addX;
    }

    public int getYHhalf(int addY){
        return (int)y + (size.height / 2) + addY;
    }
    
    public Rectangle getBounds(){
        return new Rectangle(new Point((int)x, (int)y), size);
    }
    
    public Rectangle getBounds(double vx, double vy){
        return new Rectangle(new Point((int)(x + vx), (int)(y + vy)), size);
    }
    
    public void moveObject(double addX, double addY){
        x += addX;
        y += addY;
    }
    
    public void setPosition(double x, double y){
        this.x = x;
        this.y = y;
    }
    
// Main Methods ==============================================================================================

    public void gDrawRect(Graphics2D g, Color c){
        g.setColor(c);
        gDrawRect(g);
    }
    
    public void gDrawRect(Graphics2D g){
        g.drawRect(getX(), getY(), getW(), getH());
    }

    public void gDrawRect(Graphics2D g, Color c, int addX, int addY, int addW, int addH){
        g.setColor(c);
        gDrawRect(g, addX, addY, addW, addH);
    }
    
    public void gDrawRect(Graphics2D g, int addX, int addY, int addW, int addH){
        g.drawRect(getX(addX), getY(addY), getW(addW), getH(addH));
    }

// -----------------------------------------------------------------------------------------------------------
    
    public void gFillRect(Graphics2D g, Color c){
        g.setColor(c);
        gFillRect(g);
    }
    
    public void gFillRect(Graphics2D g){
        g.fillRect(getX(), getY(), getW(), getH());
    }

    public void gFillRect(Graphics2D g, Color c, int addX, int addY, int addW, int addH){
        g.setColor(c);
        gFillRect(g, addX, addY, addW, addH);
    }
    
    public void gFillRect(Graphics2D g, int addX, int addY, int addW, int addH){
        g.fillRect(getX(addX), getY(addY), getW(addW), getH(addH));
    }
    
// -----------------------------------------------------------------------------------------------------------
    
    public void gEllipse(Graphics2D g, Color c, double x, double y, double s){
        g.setColor(c);
        gEllipse(g, x, y, s);
    }
    
    public void gEllipse(Graphics2D g, double x, double y, double s){
        g.fill(new Ellipse2D.Double(x, y, s, s));
    }
    
// Abstract Methods ==========================================================================================

    public abstract void reset();
    
}
