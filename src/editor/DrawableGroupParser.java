package editor;

import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Rectangle2D;
import javafx.geometry.Rectangle2DBuilder;
import javafx.scene.image.PixelFormat.Type;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;

/**
 * Parser to convert an equation to a drawableGroup
 * Created by James Veug on 9/08/2015.
 */
public class DrawableGroupParser {
    private static final int DEFAULT_DIVISION_STROKE_HEIGHT = 10;

    public static Color FONT_COLOR = Color.BLACK;
    public static Color AI_COLOR = Color.GREEN;
    public static Color EVALUATING_COLOR = Color.RED;
    public static Color DESCRIPTION_COLOR = Color.BLACK;
    public static Color OPERATION_COLOR = Color.BLACK;
    public static Color VARIABLE_COLOR = Color.BLACK;

    private static final float DEFAULT_SCREENWIDTH = 1024;
    private static final float DEFAULT_SCREENHEIGHT = 768;
    private static final int DEFAULT_FONTSIZE = 70;
    private static final int DEFAULT_NODELIP = 20;
    private static final int DEFAULT_LIP_X = 50; // Lip between steps
    private static final int DEFAULT_LIP_Y = 0; // Lip between steps
    //private static final Type DEFAULT_TYPEFACE = Type.create(Type., Type.NORMAL);

    private static final float DEFAULT_DIVISION_LIP = 30;
    private static float screenWidth = 1024;
    private static float screenHeight= 768;
    private static int divisionSize;
    private static int divisionLip;
    private static int nodeLip; // lip between numbers or operators... ( 2 + 5 )
    private static int fontSize;
    private static int xLipOffset;
    private static int yLipOffset;

    public static DrawableGroup getDrawableGroup(String equation) {
    	
    	char[] array = equation.toCharArray();
        DrawableGroup group = calculateXY(array, null);
        return group;
    }

    private static void reset(){
        // Reset everything in case we have we have a different sized screen!
        float sizeScalar = getScalar();
        fontSize = (int)(sizeScalar*DEFAULT_FONTSIZE);
        divisionLip = (int)(sizeScalar*DEFAULT_DIVISION_LIP);
        divisionSize = (int)(sizeScalar*DEFAULT_DIVISION_STROKE_HEIGHT);
        nodeLip = (int)(sizeScalar*DEFAULT_NODELIP);
        xLipOffset = (int)(sizeScalar*DEFAULT_LIP_X);
        yLipOffset = (int)(sizeScalar*DEFAULT_LIP_Y);
        System.out.println("==================");
        System.out.println("Font Size: " + fontSize);
        System.out.println("==================");
    }

    private static float getScalar(){
        float widthf  = screenWidth/DEFAULT_SCREENWIDTH;
        float heightf = screenHeight/DEFAULT_SCREENHEIGHT;
        return Math.min(widthf,heightf);
    }

    public static void setScreenDimensions(float w, float h){
        screenWidth = w;
        screenHeight = h;
        reset();
    }

    /**
     * Create a new DrawableGroup which contains all elements in entry.
     * Each of these elements contains an x,y for them to be drawn
     * @param entry List we want to create a DrawableGroup for
     * @param p painting properties on what to draw
     * @return new DrawableGroup object which contains all elements from entry with x,y coordinates to be drawn
     */
    private static DrawableGroup calculateXY(char[] entry, char[] nextEntry) {
        List<DrawableNode> drawableNodes = new ArrayList<DrawableNode>();


        // use the group to keep track of the side of the size of the drawing
        DrawableGroup group = new DrawableGroup(drawableNodes);
        group.setDrawingX(xLipOffset);
        group.setDrawingY(yLipOffset);
        for (int i = 0; i < entry.length; i=drawableNodes.size()) {

            // Recurse by create the next drawable node
            // Adds at least 1 to the drawableNodes list
            assignXY(i, entry, nextEntry, drawableNodes, group);
        }
        return group;
    }

    /**
     * Styles the drawable node with color, font, style and size for drawing
     * @param d Drawable object to assign the style to
     * @param index index in the entry list that the drawable node is from
     * @param entry List the drawable node is from
     * @param nextEntry Following list the DrawableNode is from
     */
    private static void assignStyle(DrawableNode d, int index, char[] entry, char[] nextEntry){

        // Assign color
        d.setColor(Color.BLACK);
    }

    /**
     * Creates a new DrawableNode
     * Gets the x,y coordinates of the given index elements in entry.
     * @param index index of element we want to create a DrawableNode for
     * @param entry list of Evaluations we want to get the element from
     * @param nextEntry following entry after entry
     * @param list List of DrawableNodes we have already created
     * @param group DrawableGroup which contains list
     * @param p painting properties on what to draw
     */
    private static void assignXY(int index, char[] entry, char[] nextEntry, List<DrawableNode> list, DrawableGroup group){
        DrawableNode d = new DrawableNode(entry[index]);
        assignStyle(d, index, entry, nextEntry);

        Rectangle2D bounds = getBounds(d.getNode(), p);
        if (d.getNode() instanceof Divide) {

            getDivisionGroup(d, index, entry, nextEntry, list, group, p);
        }
        else{
            d.setX(group.getDrawingX());
            d.setY(group.getDrawingY());
            d.setWidth(bounds.width());
            d.setHeight(bounds.height());

            // Set new X
            group.moveDrawingBy(bounds.width(), 0);

            if(group.height < d.getY()+d.getHeight()){
                group.height = d.getY()+d.getHeight();
            }
            list.add(d);
        }

        // Treat the exponent character as a normal node. '^'.
        // We want to assign new stuff to the exponents elements.

        if (d.getNode() instanceof Exponent) {
            group.moveDrawingBy(-bounds.width(),0);

            // Do it in another method
            getExponentGroup(d, index, entry, nextEntry, list, group, p);
        }

        // Move to the right
        float currentFontSize = fontSize;
        float expectedFontSize = (getScalar()*DEFAULT_FONTSIZE);
        float scaledFontSize = currentFontSize/expectedFontSize;

        int lipDistance = (int)(nodeLip*scaledFontSize);
        group.moveDrawingBy(lipDistance,0);
    }

    /**
     * Given the new DrawableNode d. We want to modify the current DrawableNodes, as well as create the denumerators of the fractions.
     * @param d Division's DrawableNode object to assign the fraction to.
     * @param index index of element we want to create a DrawableNode for
     * @param entry list of Evaluations we want to get the element from
     * @param nextEntry following entry after entry
     * @param list List of DrawableNodes we have already created
     * @param group DrawableGroup which contains list
     * @param p painting properties on what to draw
     */
    private static void getDivisionGroup(DrawableNode d, int index, List<Evaluation> entry, List<Evaluation> nextEntry, List<DrawableNode> list, DrawableGroup group, Paint p) {
        // Save our starting Y so when we have finished with the denominator
        //  We can start drawing in the correct place;
        int startDrawingY = group.getDrawingY();

        // 1/2 <-- just 2
        // 1/2*2/3 ( index 5) select 1/2*2
        // (1*2/2*3)/4
        // Get all elements to the right so we can start drawing under them

        // We have the index of the most left index to put above the fraction!
        int mostLeftX = getLeftDividingElements(index, entry);
        DrawableNode mostLeftNumerator = list.get(mostLeftX);

        int maxY = 0;
        for( int j = index-1; j>=mostLeftX; j-- ){
            DrawableNode temp = list.get(j);
            maxY = Math.max(temp.getY(), maxY);
        }

        // Assign the division line it's dimensions and position
        d.setWidth(group.getDrawingX() - mostLeftNumerator.getX() - mostLeftNumerator.getWidth());
        d.setHeight(divisionSize);
        group.setDrawingX(mostLeftNumerator.getX()); // Move by X
        group.setDrawingY(maxY + divisionLip);// Move by Y
        d.setX(group.getDrawingX());
        d.setY(group.getDrawingY());

        // Add division line!
        list.add(d);

        // Start drawing underneath the dividing line!
        group.moveDrawingBy(0, divisionLip);


        //
        // We want to convert the denominator before we align it.
        //


        // Get the denominator, and get the next division IF there is one
        int mostRightDenominatorIndex = getRightDividingElements(index, entry);

        // Create the DrawableNode for each of the denominators
        // Helps for stacked fractions!
        while(list.size() <= mostRightDenominatorIndex ){
            assignXY(list.size(), entry, nextEntry, list, group, p);
        }


        //
        // Put the denominator before the fraction line
        //


        // Get the height of the denominator
        int lowestDenominatorY = Integer.MAX_VALUE;
        int highestDenominatorY = Integer.MIN_VALUE;
        for(int i = index+1; i <= mostRightDenominatorIndex; i++){
            DrawableNode node = list.get(i);
            if( node.getNode() instanceof Divide ){
                // TODO
                // We need to allow divisions inside an exponent!
                break;
            }

            lowestDenominatorY = Math.min(lowestDenominatorY, node.getY()-node.getHeight());
            highestDenominatorY = Math.max(highestDenominatorY, node.getY());
        }
        int denominatorHeight = highestDenominatorY-lowestDenominatorY;

        // Assign new Y below the line
        for(int i = index+1; i <= mostRightDenominatorIndex; i++){
            DrawableNode node = list.get(i);
            node.setY(node.getY()+denominatorHeight);
        }



        group.moveDrawingBy(0, denominatorHeight);


        // If we used brackets as a denominator, we want to increase the length of the division line!
        DrawableNode leftNodeOfDivision = list.get(index - 1);
        DrawableNode rightNodeOfDenominator = list.get(mostRightDenominatorIndex);
        boolean leftIsWider = leftNodeOfDivision.getX()+leftNodeOfDivision.getWidth() > rightNodeOfDenominator.getX()+rightNodeOfDenominator.getWidth();
        int newWidth = (leftIsWider ? leftNodeOfDivision.getX()+leftNodeOfDivision.getWidth() : rightNodeOfDenominator.getX()+rightNodeOfDenominator.getWidth() ) - d.getX();
        d.setWidth(newWidth);


        //
        // Align which numerator or denominator depending which is smaller
        //


        float difference = Math.abs((leftNodeOfDivision.getX()+leftNodeOfDivision.getWidth()) - (rightNodeOfDenominator.getX()+rightNodeOfDenominator.getWidth()));
        int shiftAmount = (int)(difference/2);
        int startX = !leftIsWider ? mostLeftX : index+1;
        int endX = !leftIsWider ? index-1 : mostRightDenominatorIndex;
        for( int i = startX; i <= endX; i++ ){
            DrawableNode node = list.get(i);
            node.moveBy(shiftAmount,0);
        }

        // Start drawing up the top again
        group.setDrawingX(d.getX() + d.getWidth());
        group.setDrawingY(startDrawingY);
    }

    private static void getExponentGroup(DrawableNode d, int exponentIndex, List<Evaluation> entry, List<Evaluation> nextEntry, List<DrawableNode> list, DrawableGroup group, Paint p){
        System.out.println( "Exponent Equation: " + entry);

        // We need to raise (5*2)^3... up and make it half the size.
        int startIndex = exponentIndex+1;
        int endIndex = getRightExponentElements(exponentIndex, entry);

        System.out.println( "Start Exponent: " + startIndex);
        System.out.println( "End Exponent: " + endIndex);

        // Shrink font!
        int oldFontSize = fontSize;
        int newFontSize = (int)Math.floor(fontSize/2);
        fontSize = newFontSize;
        p.setTextSize(newFontSize);


        // Assign exponent values recursively.
        for(int i = startIndex; i <=endIndex; i=list.size()){
            System.out.println( "Exponent Before Size: " + list.size());
            assignXY(i, entry, nextEntry, list, group, p);
            System.out.println( "Exponent After Size: " + list.size());
        }

        // Reset the font size
        fontSize = oldFontSize;
        p.setTextSize(oldFontSize);

        // Get height of the exponent
        int exponentHeight = 0;
        for( int i = startIndex; i <= endIndex; i++){
            DrawableNode node = list.get(i);
            System.out.println( "Size: " + node.getY());
            exponentHeight = Math.max(exponentHeight, node.getY()+node.getHeight()-d.getY()+d.getHeight());
        }

        // Move all the values up by half of the exponent node's height, and by half the total height of the exponents.
        int changeInHeight = d.getHeight()/2+exponentHeight/2;
        System.out.println( "Change : " + changeInHeight);
        for( int i = startIndex; i <= endIndex; i++){
            DrawableNode node = list.get(i);
            node.setY(node.getY()-changeInHeight);
        }
    }

    /**
     * Gets the most left element that will be needed for solving before the index (division) in entry.
     * @param index index of the division in entry that we want to fidn the most left element of
     * @param entry list of evaluations to find the most left element from
     * @return index of the most left element, always >= 0
     */
    private static int getLeftDividingElements(int index, List<Evaluation> entry){
        System.out.println( "Finding Left Dividing at index " + index + " on entry " + entry.toString());
        int mostLeftX = index-1;
        while( mostLeftX >= 0 ){
            Evaluation current = entry.get(mostLeftX);
            System.out.println("\tCurrent: " + current + " at " + mostLeftX);
            if( current instanceof Operation){
                if( current instanceof Divide || current instanceof Multiply || current instanceof Exponent ){
                    mostLeftX--;
                }
                else if( current instanceof Subtract && entry.get(mostLeftX+1) instanceof Variable ){ // Subtract a variable
                    break;
                }
                else{
                    mostLeftX++;
                    break;
                }
            }
            else if( current instanceof Bracket ){
                if( ((Bracket)current).isOpeningBracket() ){
                    mostLeftX++;
                    System.out.println(" \t\tOpening Bracket at index " + mostLeftX);
                    break;
                }
                else{
                    System.out.println(" \t\tClosing Bracket at index " + mostLeftX);
                    // Skip past all brackets
                    int bracketsHit = 1; // Increase if we hit a closing bracket ((()))
                    while( mostLeftX > 0 ){
                        mostLeftX--;
                        if( entry.get(mostLeftX) instanceof Bracket ){
                            Bracket bracket = (Bracket)entry.get(mostLeftX);
                            bracketsHit += bracket.isOpeningBracket() ? -1 : +1;
                            if( bracketsHit == 0 ){
                                mostLeftX--;
                                break;
                            }
                        }
                    }
                    System.out.println(" \t\tFinished Looking for opening at Bracket at index " + mostLeftX);
                }
            }
            else if( current instanceof Number && ((Number)current).isNegative()){
                //mostLeftX++;
                break;
            }
            else{
                mostLeftX--;
            }
        }

        if( mostLeftX == -1 ){
            mostLeftX++;
        }
        System.out.println( "\tReturning index " + mostLeftX);
        return mostLeftX;
    }

    /**
     * Gets the most left element that will be needed for solving before the index (division) in entry.
     * @param index index of the division in entry that we want to fidn the most left element of
     * @param entry list of evaluations to find the most left element from
     * @return index of the most left element, always >= 0
     */
    private static int getRightDividingElements(int index, List<Evaluation> entry){
        System.out.println( "Finding Right Dividing at index " + index + " on entry " + entry.toString());
        int startingIndex = index+1;
        int mostLeftX = startingIndex;
        while( mostLeftX < entry.size() ){
            Evaluation current = entry.get(mostLeftX);
            Evaluation previous = mostLeftX-1 < 0 ? null : entry.get(mostLeftX-1);
            //System.out.println("\tCurrent: " + current + " at " + mostLeftX);
            if( current instanceof Operation){
                if( current instanceof Divide || current instanceof Exponent ){
                    mostLeftX++;
                }
                else if( previous instanceof Divide && current instanceof Subtract){
                    mostLeftX++;
                }
                else{ // Subtract or add with a variable
                    mostLeftX--;
                    break;
                }
            }
            else if( current instanceof Bracket ){
                if( ((Bracket)current).isClosingBracket() ){
                    mostLeftX--;
                    System.out.println(" \t\tClosing Bracket at index " + mostLeftX);
                    break;
                }
                else if( ((Bracket)current).isOpeningBracket() && (previous instanceof Divide || previous instanceof Exponent) ){
                    System.out.println(" \t\tOpening Bracket at index " + mostLeftX);
                    // Skip past all brackets
                    int bracketsHit = 1; // Increase if we hit a closing bracket ((()))
                    while( mostLeftX < entry.size() && bracketsHit > 0 ){
                        mostLeftX++;
                        System.out.println(" \t\tLook " + entry.get(mostLeftX) + " at index " + mostLeftX + " brackets " + bracketsHit);
                        if( entry.get(mostLeftX) instanceof Bracket ){
                            Bracket bracket = (Bracket)entry.get(mostLeftX);
                            bracketsHit += bracket.isClosingBracket() ? -1 : 1;
                        }
                    }
                    mostLeftX++;
                    System.out.println(" \t\tFinished Looking for opening at Bracket at index " + mostLeftX);
                }
                else{
                    mostLeftX--;
                    System.out.println(" \t\tBreaking Bracket at index " + mostLeftX);
                    break;
                }
            }
            else if( current instanceof Number && ((Number)current).isNegative() && mostLeftX > index+1 ){
                mostLeftX--;
                break;
            }
            else if( current instanceof Variable ){
                if( mostLeftX > startingIndex && !( mostLeftX == startingIndex+1 && previous instanceof Subtract )){
                    mostLeftX--;
                }
                break;
            }
            else if( current instanceof NullEvaluation && !(previous instanceof Exponent || previous instanceof Divide)){
                if( mostLeftX > startingIndex+1 ){
                    mostLeftX--;
                }
                break;
            }
            else{
                mostLeftX++;
            }
        }

        if( mostLeftX == entry.size() ){
            mostLeftX--;
        }

        System.out.println( "\tReturning index " + mostLeftX);
        return mostLeftX;
    }

    /**
     * Gets the most left element that will be needed for solving before the index (division) in entry.
     * @param index index of the division in entry that we want to find the most left element of
     * @param entry list of evaluations to find the most left element from
     * @return index of the most left element, always >= 0
     */
    private static int getRightExponentElements(int index, List<Evaluation> entry){
        System.out.println( "Finding Right Exponent at index " + index + " on entry " + entry.toString());
        int mostLeftX = index+1;
        while( mostLeftX < entry.size() ){
            Evaluation current = entry.get(mostLeftX);
            System.out.println("\tCurrent: " + current + " at " + mostLeftX);
            if( current instanceof Operation){
                if( current instanceof Exponent ){
                    mostLeftX++;
                }
                else{
                    mostLeftX--;
                    break;
                }
            }
            else if( current instanceof Bracket ){
                if( ((Bracket)current).isClosingBracket() ){
                    mostLeftX--;
                    System.out.println(" \t\tClosing Bracket at index " + mostLeftX);
                    break;
                }
                else{
                    System.out.println(" \t\tOpening Bracket at index " + mostLeftX);
                    // Skip past all brackets
                    int bracketsHit = 1; // Increase if we hit a closing bracket ((()))
                    while( mostLeftX < entry.size() && bracketsHit > 0 ){
                        mostLeftX++;
                        System.out.println(" \t\tLook " + entry.get(mostLeftX) + " at index " + mostLeftX + " brackets " + bracketsHit);
                        if( entry.get(mostLeftX) instanceof Bracket ){
                            Bracket bracket = (Bracket)entry.get(mostLeftX);
                            bracketsHit += bracket.isClosingBracket() ? -1 : 1;
                        }
                    }
                    mostLeftX++;
                    System.out.println(" \t\tFinished Looking for opening at Bracket at index " + mostLeftX);
                }
            }
            else if( current instanceof Number && ((Number)current).isNegative()){
                mostLeftX--;
                break;
            }
            else{
                mostLeftX++;
            }
        }

        if( mostLeftX == entry.size() ){
            mostLeftX--;
        }
        else if( mostLeftX < (index+1) ){
            mostLeftX = (index+1);
        }

        System.out.println( "\tReturning right Exponent index " + mostLeftX);
        return mostLeftX;
    }

    /**
     * Get the bounds(x,y,w,h) of the given node for when we want to draw
     * @param node evaluation we want to get the bounds of
     * @param p Paint object that contains the font information
     * @return new Rectangle object that contains the x,y,w,h of the given node.
     */
    private static Rectangle2D getBounds(Evaluation node,Paint p){
        String string = node.toString();
        if( node instanceof parser.leafs.Number && ((Number)node).isNegative() ){
            string += ".";
        }
        return getBounds(string, p);
    }

    /**
     * Get the bounds(x,y,w,h) of the given node for when we want to draw
     * @param string string we want to get the bounds of
     * @param p Paint object that contains the font information
     * @return new Rectangle object that contains the x,y,w,h of the given string.
     */
    private static Rectangle2D getBounds(String string, Paint p){
    	Rectangle2D bounds = Rectangle2DBuilder.create();
        if( string.endsWith(" ") ){
            string += ".";
        }

        p.getTextBounds(string,0,string.length(),bounds);
        return bounds;
    }
}
