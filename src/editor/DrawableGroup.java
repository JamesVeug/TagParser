package editor;

import java.util.List;

/**
 * Created by Dylan on 31/07/2015.
 */
public class DrawableGroup{
    private static final String TAG = DrawableGroup.class.getSimpleName();

    final List<DrawableNode> list;
    double x;
    double y;
    double width;
    double height;
    double drawingX;
    double drawingY;

    public DrawableGroup(List<DrawableNode> list){
        this.list = list;

        this.x = 0;
        this.y = 0;
        this.width = 0;
        this.height = 0;
        this.drawingX = 0;
        this.drawingY = 0;
    }

    public void moveDrawingBy(double d, double y) {
        setDrawingX(drawingX + d);
        setDrawingY(drawingY + y);
    }
    
    public void moveBy(double dX, double dY) {
        x += dX;
        y += dY;
        
        for(int i = 0; i < list.size(); i++){
        	list.get(i).moveBy(dX, dY);
        }
    }

    public double getDrawingX() {
        return drawingX;
    }
    public double getDrawingY() {
        return drawingY;
    }

    public void setDrawingX(double d) {
        this.drawingX = d;
        width = Math.max(width,this.drawingX);
        x = Math.min(x, this.drawingX);
    }

    public void setDrawingY(double d) {
        this.drawingY = d;
        height = Math.max(height,this.drawingY);
        y = Math.min(y, this.drawingY);
    }

    public void union(DrawableGroup otherGroup) {
        List<DrawableNode> otherList = otherGroup.list;

        for( int i = 0; i < otherList.size(); i++ ){
            DrawableNode node = otherList.get(i);
            node.setX(node.getX() + getDrawingX());
            node.setY(node.getY() + getDrawingY());

            list.add(node);

            moveDrawingBy(node.getWidth(), 0);

            double nodeBottomY = node.getY()+node.getHeight();

            if( nodeBottomY > height ){
                moveDrawingBy(0, nodeBottomY);
            }
        }
    }
}