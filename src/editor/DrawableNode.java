package editor;

import javafx.scene.paint.Color;

/**
 * Created by Dylan on 31/07/2015.
 */
public class DrawableNode{
    private String display;
    private int x = 0;
    private int y = 0;
    private int width = 0; // For division
    private Color color = Color.BLACK;
    private int fontSize;
    private int height;


    public DrawableNode(char node){
        display = String.valueOf(node);
    }

    public DrawableNode(String description){
        display = description;
    }

    public int setX(int nx){
        this.x = nx;
        return this.x;
    }

    public int setY(int ny){
        this.y = ny;
        return y;
    }

    public void moveBy(int nx, int ny){
        this.x += nx;
        this.y += ny;
    }

    public int getX() {
        return this.x;
    }
    public int getY() { return this.y; }
    public String toString(){
        return display;
    }
    public int getWidth() {
        return width;
    }
    public void setWidth(int width) {
        this.width = width;
    }
    public void setColor(Color color) {
        this.color = color;
    }
    public Color getColor() {return color;}

    public int getFontSize() {
        return fontSize;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getHeight() {
        return height;
    }
}