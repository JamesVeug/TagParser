package editor;

import javafx.scene.paint.Color;

/**
 * Created by Dylan on 31/07/2015.
 */
public class DrawableNode{
    private String display;
    private double x = 0;
    private double y = 0;
    private double width = 0; // For division
    private double height;
    private Color color = Color.BLACK;
    private String font;
    private int fontSize;


    public DrawableNode(char node){
        display = String.valueOf(node);
    }

    public DrawableNode(String description){
        display = description;
    }
    
    public String getText(){
    	return display;
    }

    public double setX(double d){
        this.x = d;
        return this.x;
    }

    public double setY(double d){
        this.y = d;
        return y;
    }

    public void moveBy(int nx, int ny){
        this.x += nx;
        this.y += ny;
    }

    public double getX() { return this.x; }
    public double getY() { return this.y; }
    public String toString(){
        return display;
    }
    public double getWidth() {
        return width;
    }
    public void setWidth(double d) {
        this.width = d;
    }
    public void setColor(Color color) {
        this.color = color;
    }
    public Color getColor() {return color;}

    public int getFontSize() {
        return fontSize;
    }
    
    public void setFontSize(int size){
    	this.fontSize = size;
    }

    public void setHeight(double d) {
        this.height = d;
    }

    public double getHeight() {
        return height;
    }

	public String getFont() {
		return font;
	}

	public void setFont(String font) {
		this.font = font;
	}
}