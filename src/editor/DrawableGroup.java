package editor;

import java.util.List;

/**
 * Created by Dylan on 31/07/2015.
 */
public class DrawableGroup{
    private static final String TAG = DrawableGroup.class.getSimpleName();

    final List<DrawableNode> list;
    int x;
    int y;
    int width;
    int height;
    int drawingX;
    int drawingY;

    public DrawableGroup(List<DrawableNode> list){
        this.list = list;

        this.x = 0;
        this.y = 0;
        this.width = 0;
        this.height = 0;
        this.drawingX = 0;
        this.drawingY = 0;
    }

    public void moveDrawingBy(int x, int y) {
        setDrawingX(drawingX + x);
        setDrawingY(drawingY + y);
    }

    public int getDrawingX() {
        return drawingX;
    }
    public int getDrawingY() {
        return drawingY;
    }

    public void setDrawingX(int drawingX) {
        this.drawingX = drawingX;
        width = Math.max(width,this.drawingX);
        x = Math.min(x, this.drawingX);
    }

    public void setDrawingY(int drawingY) {
        this.drawingY = drawingY;
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

            int nodeBottomY = node.getY()+node.getHeight();

            if( nodeBottomY > height ){
                moveDrawingBy(0, nodeBottomY);
            }
        }
    }
}