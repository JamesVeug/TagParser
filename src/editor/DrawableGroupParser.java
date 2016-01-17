package editor;

import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Bounds;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import parser.descriptions.DescriptionParserException;

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
    private static final float DEFAULT_SCREENHEIGHT = 400;
    private static final int DEFAULT_FONTSIZE = 70;
    private static final int DEFAULT_NODELIP = 0;
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
    	// This is a description where MATH{2x}=MATH{5}
    	
    	// [This is a description where ],[MATH 2x],[=],[MATH 5]
    	List<String> array = splitEquation(equation);
    	
    	
        DrawableGroup group = calculateXY(array, null);
        return group;
    }

    /**
     * Create a new DrawableGroup which contains all elements in entry.
     * Each of these elements contains an x,y for them to be drawn
     * @param array List we want to create a DrawableGroup for
     * @param p painting properties on what to draw
     * @return new DrawableGroup object which contains all elements from entry with x,y coordinates to be drawn
     */
    private static DrawableGroup calculateXY(List<String> array, List<String> nextEntry) {
        List<DrawableNode> drawableNodes = new ArrayList<DrawableNode>();


        // use the group to keep track of the side of the size of the drawing
        DrawableGroup group = new DrawableGroup(drawableNodes);
        group.setDrawingX(xLipOffset);
        group.setDrawingY(yLipOffset);
        for (int i = 0; i < array.size(); i=drawableNodes.size()) {

            // Recurse by create the next drawable node
            // Adds at least 1 to the drawableNodes list
            assignXY(i, array, nextEntry, drawableNodes, group);
        }
        return group;
    }

    /**
     * Divides the string into substrings so we can display the text either as standared text. Or as Math Text
     * @param equation
     * @return
     */
    private static List<String> splitEquation(String equation) {
    	List<String> list = new ArrayList<String>();
    	
    	int currentIndex = 0;
    	int index = equation.indexOf("MATH");
    	while(index != -1){
    		
    		// .....MATH.....
    		String beforeMATH = equation.substring(0,index);
    		list.add(beforeMATH);
    		
    		// MATH{
    		int functionOpen = equation.indexOf("{", index);
    		if( functionOpen == -1 ){
    			throw new DrawableGroupParserException("No open syntax for function at index " + (index+currentIndex) + " '" + equation + "'");
    		}
    		
    		// MATH{...}
    		int functionClose = getClosingIndex(equation, "{", "}", functionOpen);
    		if( functionClose == -1 ){
    			throw new DrawableGroupParserException("No close syntax for function at index " + (index+currentIndex));
    		}
    		
    		// MATH ...
    		String math = equation.substring(functionOpen+1,functionClose).trim();
    		list.add("MATH");
    		list.add("{");
    		
    		EquationScanner2 scan = new EquationScanner2(math);
    		while(scan.hasNext()){
    			list.add(scan.next());
    		}
    		
    		list.add("}");
    		
    		equation = equation.substring(functionClose+1);
    		
    		// Save how far we have substringed the equation for error referencing.
    		currentIndex += functionClose;
    		
    		index = equation.indexOf("MATH");
    	}
    	
    	equation = equation.trim();
    	if( equation != null && !equation.isEmpty() ){
    		list.add(equation);
    	}
    	
    	if( list.get(0).isEmpty() ){
    		list.remove(0);
    	}
    	
    	System.out.println("Split Equation: " + list);
		return list;
	}
    
    /**
	 * Returns an index of the opening bracket for the given closing bracket' index
	 * @param list List containing the closing and opening brackets
	 * @param i Index of the closing bracket
	 * @return
	 */
    protected static int getClosingIndex(String substring, String openingSyntax, String closingSyntax, int index) {	
		//System.out.println("Getting closed index for '" + substring + "'\n\t o( " + openingSyntax + " ) c( " + closingSyntax + " ) at " + index);
		if( !substring.substring(index).startsWith(openingSyntax) ){
			System.err.println("Was not given opening syntax at index '" + index + "' for equation " + substring);
			return -1;
		}
		
		int opening = index;
		int closing = substring.indexOf(closingSyntax, index);
		if( closing == -1 ){
			throw new DescriptionParserException("Can not find closing bracket for '" + substring + "' at index " + index + " with Syntax: '" + openingSyntax + "'" + ", '" + closingSyntax + "'");
		}
		

		// How many in between this and the next
		//System.out.println("view: '" + substring.substring(opening, closing+2));
		while(true){
			//System.out.println("\topening: " + opening);
			//System.out.println("\tclosing: " + closing);

			int innerOpens = 0;
			int insideIndex1 = substring.indexOf(openingSyntax, opening+1);
			while(insideIndex1 != -1 && insideIndex1 < closing){
				innerOpens++;
				insideIndex1 = substring.indexOf(openingSyntax, insideIndex1+1);
			}
			
			int innerCloses = 0;
			int insideIndex2 = substring.indexOf(closingSyntax, opening+1);
			while(insideIndex2 != -1 && insideIndex2 < closing){
				innerCloses++;
				insideIndex2 = substring.indexOf(closingSyntax, insideIndex2+1);
			}
			
			if( innerOpens == innerCloses ){
				break;
			}
			else{
				
				int difference = Math.abs(innerCloses-innerOpens);
				while( difference-- > 0 ){
					closing = substring.indexOf(closingSyntax, closing+1);
				}
			}
		}
		
		
		return closing;
	}
    
    /**
	 * Returns an index of the opening bracket for the given closing bracket' index
	 * @param list List containing the closing and opening brackets
	 * @param i Index of the closing bracket
	 * @return
	 */
	protected static int getClosingBracketIndex(
			List<String> list, int index) {
		System.out.println("Getting closing Bracket index in '" + list.subList(index, list.size()) + "' + starting at index " + index);

		
		String closingType = list.get(index).equals("(") ? ")" : list.get(index).equals("{") ? "}" : "NULL";
		System.out.println("openType: " + list.get(index) + " closingType: " + closingType);

		// (((10/10)*10)/10) where i == 1 and looking for closing 9
		String start = list.get(index).trim(); 
		if( closingType.equals("NULL") ){
			throw new RuntimeException("Index not linked to OpeningBracket: " + index + " is linked to " + start);
		}


		int bracketCount = 1;
		int i = index;
		String e = list.get(i).trim();
		while( bracketCount != 0 && (i+1) < list.size()){
			
			// Start to the right of the opening bracket
			i++;
			e = list.get(i).trim();
			
			// Increment if we are looking at a bracket
			if( e.equals(start) || e.equals(closingType) ){
				bracketCount += e.equals(start) ? 1 : -1;
			}			
		}
		//System.out.println( i + " " + bracketCount + " " + e + " !!");
		//System.out.println( "Final: " + list.subList(index, i+1));


		return i;
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
     * Styles the drawable node with color, font, style and size for drawing
     * @param d Drawable object to assign the style to
     * @param index index in the entry list that the drawable node is from
     * @param array List the drawable node is from
     * @param nextEntry Following list the DrawableNode is from
     */
    private static void assignStyle(DrawableNode d, int index, List<String> array, List<String> nextEntry){

        // Assign color
        d.setColor(Color.BLACK);
        d.setFontSize(fontSize);
        d.setFont("Arial");
    }

    private static void assignMathXY(int index, List<String> array, List<String> nextEntry, List<DrawableNode> list, DrawableGroup group){
    	DrawableNode d = new DrawableNode(array.get(index));
        assignStyle(d, index, array, nextEntry);

        Bounds bounds = getBounds(d);
    	if ( isDivide(d) ){

            getDivisionGroup(d, index, array, nextEntry, list, group);
        }
        else{
            d.setX(group.getDrawingX());
            d.setY(group.getDrawingY());
            d.setWidth(bounds.getWidth());
            d.setHeight(bounds.getHeight());

            // Set new X
            group.moveDrawingBy(bounds.getWidth(), 0);

            if(group.height < d.getY()+d.getHeight()){
                group.height = d.getY()+d.getHeight();
            }
            list.add(d);
        }

        // Treat the exponent character as a normal node. '^'.
        // We want to assign new stuff to the exponents elements.

        if ( isExponent(d) ) {
            group.moveDrawingBy(-bounds.getWidth(),0);

            // Do it in another method
            getExponentGroup(d, index, array, nextEntry, list, group);
        }
        
        // Move to the right
        float currentFontSize = fontSize;
        float expectedFontSize = (getScalar()*DEFAULT_FONTSIZE);
        float scaledFontSize = currentFontSize/expectedFontSize;

        int lipDistance = (int)(nodeLip*scaledFontSize);
        group.moveDrawingBy(lipDistance,0);
    }
    
    /**
     * Creates a new DrawableNode
     * Gets the x,y coordinates of the given index elements in entry.
     * @param index index of element we want to create a DrawableNode for
     * @param array list of Strings we want to get the element from
     * @param nextEntry following entry after entry
     * @param list List of DrawableNodes we have already created
     * @param group DrawableGroup which contains list
     * @param p painting properties on what to draw
     */
    private static void assignXY(int index, List<String> array, List<String> nextEntry, List<DrawableNode> list, DrawableGroup group){
        DrawableNode d = new DrawableNode(array.get(index));
        assignStyle(d, index, array, nextEntry);

        Bounds bounds = getBounds(d);
        if( isMath(d) ){
        	
        	int start = index;
        	int end = getClosingBracketIndex(array, index+1);

        	array.remove(index); // MATH
        	array.remove(index); // {
        	
        	List<String> subList = array.subList(start, end-2);
        	System.out.println("Assigning Math XY: " + subList);
        	
        	
        	List<DrawableNode> mathNodes = new ArrayList<DrawableNode>();
        	DrawableGroup mathGroup = new DrawableGroup(mathNodes);
        	while(mathNodes.size() < subList.size()){
        		assignMathXY(mathNodes.size(), subList, nextEntry, mathNodes, mathGroup);
        	}
        	
        	// Move new group into the same position
        	mathGroup.moveBy(group.getDrawingX(), group.getDrawingY());
        	
        	// Add nodes to our list
        	for(int i = 0; i < mathNodes.size(); i++){
        		list.add(mathNodes.get(i));
        	}
        	
        	// Move drawing position to the end of the box
        	group.moveDrawingBy(mathGroup.getDrawingX(), mathGroup.getDrawingY());
        	
        	array.remove(end-2); // }
        }
        else{
        	d.setX(group.getDrawingX());
            d.setY(group.getDrawingY());
            d.setWidth(bounds.getWidth());
            d.setHeight(bounds.getHeight());

            // Set new X
            group.moveDrawingBy(bounds.getWidth(), 0);

            if(group.height < d.getY()+d.getHeight()){
                group.height = d.getY()+d.getHeight();
            }
            list.add(d);
            
            // Move to the right
            float currentFontSize = fontSize;
            float expectedFontSize = (getScalar()*DEFAULT_FONTSIZE);
            float scaledFontSize = currentFontSize/expectedFontSize;

            int lipDistance = (int)(nodeLip*scaledFontSize);
            group.moveDrawingBy(lipDistance,0);
        }

    }

	/**
     * Given the new DrawableNode d. We want to modify the current DrawableNodes, as well as create the denumerators of the fractions.
     * @param d Division's DrawableNode object to assign the fraction to.
     * @param index index of element we want to create a DrawableNode for
     * @param entry list of Strings we want to get the element from
     * @param nextEntry following entry after entry
     * @param list List of DrawableNodes we have already created
     * @param group DrawableGroup which contains list
     * @param p painting properties on what to draw
     */
    private static void getDivisionGroup(DrawableNode d, int index, List<String> entry, List<String> nextEntry, List<DrawableNode> list, DrawableGroup group) {
        // Save our starting Y so when we have finished with the denominator
        //  We can start drawing in the correct place;
        double startDrawingY = group.getDrawingY();

        // 1/2 <-- just 2
        // 1/2*2/3 ( index 5) select 1/2*2
        // (1*2/2*3)/4
        // Get all elements to the right so we can start drawing under them

        // We have the index of the most left index to put above the fraction!
        int mostLeftX = getLeftDividingElements(index, entry);
        DrawableNode mostLeftNumerator = list.get(mostLeftX);

        double maxY = 0;
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
        	assignMathXY(list.size(), entry, nextEntry, list, group);
        }


        //
        // Put the denominator before the fraction line
        //


        // Get the height of the denominator
        double lowestDenominatorY = Integer.MAX_VALUE;
        double highestDenominatorY = Integer.MIN_VALUE;
        for(int i = index+1; i <= mostRightDenominatorIndex; i++){
            DrawableNode node = list.get(i);
            if( isDivide(node) ){
                // TODO
                // We need to allow divisions inside an exponent!
                break;
            }

            lowestDenominatorY = Math.min(lowestDenominatorY, node.getY()-node.getHeight());
            highestDenominatorY = Math.max(highestDenominatorY, node.getY());
        }
        double denominatorHeight = highestDenominatorY-lowestDenominatorY;

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
        double newWidth = (leftIsWider ? leftNodeOfDivision.getX()+leftNodeOfDivision.getWidth() : rightNodeOfDenominator.getX()+rightNodeOfDenominator.getWidth() ) - d.getX();
        d.setWidth(newWidth);


        //
        // Align which numerator or denominator depending which is smaller
        //


        double difference = Math.abs((leftNodeOfDivision.getX()+leftNodeOfDivision.getWidth()) - (rightNodeOfDenominator.getX()+rightNodeOfDenominator.getWidth()));
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

	private static void getExponentGroup(DrawableNode d, int exponentIndex, List<String> entry, List<String> nextEntry, List<DrawableNode> list, DrawableGroup group){
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
        d.setFontSize(newFontSize);


        // Assign exponent values recursively.
        for(int i = startIndex; i <=endIndex; i=list.size()){
            System.out.println( "Exponent Before Size: " + list.size());
            assignMathXY(i, entry, nextEntry, list, group);
            System.out.println( "Exponent After Size: " + list.size());
        }

        // Reset the font size
        fontSize = oldFontSize;
        d.setFontSize(oldFontSize);

        // Get height of the exponent
        double exponentHeight = 0;
        for( int i = startIndex; i <= endIndex; i++){
            DrawableNode node = list.get(i);
            System.out.println( "Size: " + node.getY());
            exponentHeight = Math.max(exponentHeight, node.getY()+node.getHeight()-d.getY()+d.getHeight());
        }

        // Move all the values up by half of the exponent node's height, and by half the total height of the exponents.
        double changeInHeight = d.getHeight()/2+exponentHeight/2;
        System.out.println( "Change : " + changeInHeight);
        for( int i = startIndex; i <= endIndex; i++){
            DrawableNode node = list.get(i);
            node.setY(node.getY()-changeInHeight);
        }
    }

    /**
     * Gets the most left element that will be needed for solving before the index (division) in entry.
     * @param index index of the division in entry that we want to fidn the most left element of
     * @param entry list of Strings to find the most left element from
     * @return index of the most left element, always >= 0
     */
    private static int getLeftDividingElements(int index, List<String> entry){
        System.out.println( "Finding Left Dividing at index " + index + " on entry " + entry.toString());
        int mostLeftX = index-1;
        while( mostLeftX >= 0 ){
            String current = entry.get(mostLeftX);
            System.out.println("\tCurrent: " + current + " at " + mostLeftX);
            if( isOperation(current) ){
                if( isDivide(current) || isMultiply(current) || isExponent(current) ){
                    mostLeftX--;
                }
                else if( isSubtract(current) && isVariable(entry.get(mostLeftX+1)) ){ // Subtract a variable
                    break;
                }
                else{
                    mostLeftX++;
                    break;
                }
            }
            else if( isBracket(current) ){
                if( isOpeningBracket(current) ){
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
                        if( isBracket(entry.get(mostLeftX)) ){
                            String bracket = entry.get(mostLeftX);
                            bracketsHit += isOpeningBracket(bracket) ? -1 : +1;
                            if( bracketsHit == 0 ){
                                mostLeftX--;
                                break;
                            }
                        }
                    }
                    System.out.println(" \t\tFinished Looking for opening at Bracket at index " + mostLeftX);
                }
            }
            else if( isNegative(current) ){
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


	private static boolean isMath(DrawableNode d) {
		return d.getText().equalsIgnoreCase("Math");
	}
	
	private static boolean isNull(String current) {
		return current.equals("?");
	}

    private static boolean isDivide(DrawableNode node) {
		return node.getText().equals("/");
	}

    private static boolean isExponent(DrawableNode d) {
		return d.getText().equals("^");
	}

    private static boolean isOpeningBracket(String current) {
		return current.equals("(") || current.equals("{");
	}

    private static boolean isClosingBracket(String current) {
		return current.equals(")") || current.equals("}");
	}

	private static boolean isBracket(String current) {
		return isOpeningBracket(current) || isClosingBracket(current);
	}

	private static boolean isVariable(String string) {
		return string.length() == 1 && Character.isLetter(string.charAt(0));
	}

	private static boolean isSubtract(String current) {
		return current.equals("-");
	}

	private static boolean isExponent(String current) {
		return current.equals("^");
	}

	private static boolean isMultiply(String current) {
		return current.equals("*");
	}
	
	private static boolean isAdd(String current) {
		return current.equals("+");
	}

	private static boolean isDivide(String current) {
		return current.equals("/");
	}

	private static boolean isOperation(String current) {
		return isAdd(current) || isSubtract(current) || isMultiply(current) || isDivide(current) || isExponent(current);
	}

	/**
     * Gets the most left element that will be needed for solving before the index (division) in entry.
     * @param index index of the division in entry that we want to fidn the most left element of
     * @param entry list of Strings to find the most left element from
     * @return index of the most left element, always >= 0
     */
    private static int getRightDividingElements(int index, List<String> entry){
        System.out.println( "Finding Right Dividing at index " + index + " on entry " + entry.toString());
        int startingIndex = index+1;
        int mostLeftX = startingIndex;
        while( mostLeftX < entry.size() ){
            String current = entry.get(mostLeftX);
            String previous = mostLeftX-1 < 0 ? null : entry.get(mostLeftX-1);
            //System.out.println("\tCurrent: " + current + " at " + mostLeftX);
            if( isOperation(current) ){
                if( isDivide(current) || isExponent(current) ){
                    mostLeftX++;
                }
                else if( isDivide(previous) && isSubtract(current) ){
                    mostLeftX++;
                }
                else{ // Subtract or add with a variable
                    mostLeftX--;
                    break;
                }
            }
            else if( isBracket(current) ){
                if( isClosingBracket(current) ){
                    mostLeftX--;
                    System.out.println(" \t\tClosing Bracket at index " + mostLeftX);
                    break;
                }
                else if( isOpeningBracket(current) && (isDivide(previous) || isExponent(previous)) ){
                    System.out.println(" \t\tOpening Bracket at index " + mostLeftX);
                    // Skip past all brackets
                    int bracketsHit = 1; // Increase if we hit a closing bracket ((()))
                    while( mostLeftX < entry.size() && bracketsHit > 0 ){
                        mostLeftX++;
                        System.out.println(" \t\tLook " + entry.get(mostLeftX) + " at index " + mostLeftX + " brackets " + bracketsHit);
                        if( isBracket(entry.get(mostLeftX)) ){
                            String bracket = entry.get(mostLeftX);
                            bracketsHit += isClosingBracket(bracket) ? -1 : 1;
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
            else if( isNegative(current) && mostLeftX > index+1 ){
                mostLeftX--;
                break;
            }
            else if( isVariable(current) ){
                if( mostLeftX > startingIndex && !( mostLeftX == startingIndex+1 && isSubtract(previous) )){
                    mostLeftX--;
                }
                break;
            }
            else if( isNull(current) && !isExponent(previous) && isDivide(previous) ){
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
     * @param entry list of Strings to find the most left element from
     * @return index of the most left element, always >= 0
     */
    private static int getRightExponentElements(int index, List<String> entry){
        System.out.println( "Finding Right Exponent at index " + index + " on entry " + entry.toString());
        int mostLeftX = index+1;
        while( mostLeftX < entry.size() ){
            String current = entry.get(mostLeftX);
            System.out.println("\tCurrent: " + current + " at " + mostLeftX);
            if( isOperation(current) ){
                if( isExponent(current) ){
                    mostLeftX++;
                }
                else{
                    mostLeftX--;
                    break;
                }
            }
            else if( isBracket(current) ){
                if( isClosingBracket(current) ){
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
                        if( isBracket(entry.get(mostLeftX)) ){
                            String bracket = entry.get(mostLeftX);
                            bracketsHit += isClosingBracket(bracket) ? -1 : 1;
                        }
                    }
                    mostLeftX++;
                    System.out.println(" \t\tFinished Looking for opening at Bracket at index " + mostLeftX);
                }
            }
            else if( isNegative(current) ){
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
     * @param node String we want to get the bounds of
     * @param p Paint object that contains the font information
     * @return new Rectangle object that contains the x,y,w,h of the given node.
     */
    /*private static Bounds getBounds(String node,Paint p){
        String string = node.toString();
        if( isNegative(node) ){
            string += ".";
        }
        return getBounds(string, p);
    }*/

    private static boolean isNegative(String node) {
		return node.startsWith("-");
	}

	/**
     * Get the bounds(x,y,w,h) of the given node for when we want to draw
     * @param string string we want to get the bounds of
     * @param p Paint object that contains the font information
     * @return new Rectangle object that contains the x,y,w,h of the given string.
     */
    private static Bounds getBounds(DrawableNode node){
    	String string = node.getText();
    	if( string.startsWith("-") ){
            //string += ".";
        }

    	/*if( string.startsWith(" ") ){
            string = "." + string;
        }
    	
        if( string.endsWith(" ") ){
            string += ".";
        }*/
        
    	Text text = new Text(string);
    	text.setFont(Font.font(node.getFont(), node.getFontSize()));
        return text.getBoundsInLocal();
    }
}
