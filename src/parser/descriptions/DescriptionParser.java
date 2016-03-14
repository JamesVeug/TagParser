package parser.descriptions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class DescriptionParser {
	private static String filename = "/parser/descriptions/english_descriptions";
	
	private final static String PLACEHOLDER = "PLACE HOLDER";
	private static List<String> decriptions = null;
	
	// Prefix
	private static String prefix = "";
	
	// Loading variables 
	private static boolean isSetup = false;
	private static long lastModified = 0; // Current files last modified time
	
	private static Map<String,String> savedVariables = new HashMap<String, String>();
	
	public static boolean isSetup(){
		return isSetup;
	}
	
	public static void setup(){
		
		// If we have already set up
		// Make sure the file has not change.
		// If it has, reread the file
		if( isSetup && !fileHasChanged()){
			return;				
		}
		load();
		isSetup = true;
	}

	public static String getDescription(int ID){
		System.out.println("Getting Description " + ID);
		return getDescription(ID, new DescriptionNode());
	}
	
	public static String getDescription(int ID, String... subs){
		System.out.println("Getting Description " + ID + ", subs: " + subs);
		return getDescription(ID, new DescriptionNode(subs));
	}

	public static String getDescription(int ID, String[]... arrays) {
		System.out.println("Getting Description " + ID + ", arrays: " + arrayToString(arrays));
		return getDescription(ID, new DescriptionNode(arrays));
	}
	
	private static String getDescription(int ID, DescriptionNode node){
		setup();
		
		// Reset
		savedVariables.clear();

		String description = getDescriptionFromList(ID);
		String subbed = substituteVariables(description, node, ID);

		// Remove all "'s
		subbed = subbed.replaceAll("\"", "");
		
		// Add prefix
		subbed = prefix + subbed;
		
		System.out.println("Finished Getting Description '" + subbed + "'");
		return subbed;
	}
	
	private static String getDescriptionFromList(int index) {
		if( index-1 >= decriptions.size() || index < 1 ){
			throw new DescriptionParserException("Description with ID " + index + " does not exist! (Min: 1, Max: " + decriptions.size() + ")");
		}
		else if( decriptions.get(index-1).endsWith(PLACEHOLDER) ){
			throw new DescriptionParserException("Description with ID " + index + " has not been defined!");
		}
		
		return decriptions.get(index-1);
	}

	public static String substituteVariables(String description, DescriptionNode subs, int ID) {
		System.out.println("Substituting Variables '" + description + "'");
		String subbed = description;
		
		// Remove new lines
		subbed = subbed.replaceAll("\n", "");
		
		int index = subbed.indexOf("<");
		while(index != -1){
			int closingIndex = getClosingIndex(subbed, "<", "/>", index);
			if( closingIndex == -1 ){
				throw new DescriptionParserException("Missing closing bracket for variable on '" + subbed.substring(index) + "'\n\ton description " + ID);
			}
			
			String substringed = subbed.substring(index+1,closingIndex).trim();
			if( !substringed.isEmpty() && !substringed.equals(" ")){
				DescriptionNode parsed = parse(substringed, subs, index, ID);
				subbed = (subbed.substring(0,index) + parsed.toString() + subbed.substring(closingIndex+2)).trim();
			}
			else{
				System.err.println("Empty Strings '" + substringed + "'");
			}
			
			index = subbed.indexOf("<");
		}
		
		System.out.println("Finished Substituting Variables '" + subbed + "'");
		
		
		return subbed;
	}

	private static DescriptionNode parse(String string, DescriptionNode subs, int index, int ID) {
		System.out.println("===== Parsing at index " + index + " '" + string + "' =====");
		if( string.startsWith("LOOP") ){
			return parseLoop(string, subs, index, ID);
		}
		else if( string.startsWith("IF") ){
			DescriptionNode node = parseIf(string, subs, index, ID); 
			return node == null ? new DescriptionNode() : node;
		}
		else if( string.startsWith("DEFINE") ){
			return parseDefine(string, subs, index, ID);
		}
		else if( string.startsWith("NEWLINE") ){
			return parseNewLine(string, subs, index, ID);
		}
		else if( string.startsWith("MATH") ){
			return parseMath(string, subs, index, ID);
		}
		else if( Character.isDigit(string.charAt(0)) ){
			return parseInput(string, subs, index, ID);
		}
		else if( string.charAt(0) == '/' ){ // /x
			return parseVariable(string, subs, index, ID);
		}
		
		throw new DescriptionParserException("Unknown keyword '" + string + "' for description " + ID);
	}

	private static DescriptionNode parseMath(String string,
			DescriptionNode subs, int index, int iD) {
		System.out.println("===== Parsing MATH at index " + index + " '" + string + "' =====");
		
		int openBracket = string.indexOf("{");
		int closeBracket = getClosingIndex(string, "{", "}", openBracket);
		if( closeBracket == -1 ){
			throw new DescriptionParserException("No closing bracket for syntax at index " + openBracket);
		}
		
		String function = substituteVariables(string.substring(openBracket+1,closeBracket), subs, iD);
		
		String parsed = string.substring(0,openBracket+1) + function + string.substring(closeBracket); 
		
		return new DescriptionNode(parsed);
	}

	private static DescriptionNode parseNewLine(String string,
			DescriptionNode subs, int index, int iD) {
		return new DescriptionNode("\n");
	}

	private static DescriptionNode parseVariable(String string,
			DescriptionNode subs, int index, int ID) {
		System.out.println("===== Parsing Variable at index " + index + " '" + string + "' =====");
		
		// /x
		int nextSpace = getNextSpaceIndex(string, 0);
		String name = nextSpace != -1 ? string.substring(1,nextSpace) : string.substring(1);
		
		if( !savedVariables.containsKey(name) ){
			throw new DescriptionParserException("Variable '" + name + "' has not been defined for description " + ID);
		}
		
		String var = savedVariables.get(name);

		System.out.println("===== Finished Parsing Variable '" + var + "' =====");
		return new DescriptionNode(var);
	}

	private static DescriptionNode parseDefine(String string,
			DescriptionNode subs, int index, int ID) {
		System.out.println("===== Parsing DEFINE at index " + index + " '" + string + "' =====");

		// <DEFINE x {Something}/>
		
		// Skip DEFINE
		string = string.substring(6).trim();
		
		if( Character.isDigit(string.charAt(0)) ){
			throw new DescriptionParserException("Can not define new variable with numbers! '" + string.charAt(0) + "'");
		}
		
		// </DEFINE
		int endLetterIndex = getNextSpaceIndex(string, 0);
		
		String name = string.substring(0, endLetterIndex);
		
		// </DEFINE x
		
		int openBracket = string.indexOf("{");
		int closeBracket = getClosingIndex(string, "{", "}", openBracket);
		DescriptionNode function = new DescriptionNode(substituteVariables(string.substring(openBracket+1,closeBracket), subs, ID));
		
		// </DEFINE x {Something}\>		
		savedVariables.put(name, function.toString());	
		System.out.println("Defined: '" + name + "' as '" + savedVariables.get(name) + "'");

		System.out.println("===== Finished Parsing DEFINE '" + savedVariables.get(name) + "'=====");
		
		// Do not save anything
		System.out.println("'" + new DescriptionNode().toString() + "'");
		return new DescriptionNode();
	}
	
	private static String getNextToken(String string, DescriptionNode subs, int index, int ID){
		System.out.println("===== Getting NextToken in '" + string + "' ======");
		
		String leftSide = "";
		if( string.charAt(0) == '<' ){
			int closingBracket = getClosingIndex(string, "<", "/>", 0);
			if( closingBracket == -1 ){
				throw new DescriptionParserException("No closing bracket for syntax at index " + index);
			}
			
			String computation = parse(string.substring(1,closingBracket), subs, index, ID).toString();
			string = string.substring(closingBracket+1).trim();
			leftSide = computation;
		}
		else if( string.charAt(0) == '|' ){
			int closingAbsolute = string.indexOf("|",1);
			String substringAbsolute = string.substring(2,closingAbsolute);
			String parsedAbsolute = parseAbsolute(substringAbsolute, subs, index, ID);
			string = string.substring(closingAbsolute).trim();
			leftSide = parsedAbsolute;
		}
		else{
			int end = getNextSpaceIndex(string, 0);
			if( end > 0 ){
				leftSide = string.substring(0,end);
			}
			else{
				leftSide = "";
			}
			
			
		}
		
		return leftSide;
	}
	
	private static String getNextComparator(String string){
		String comparator = "";
		if( string.startsWith("LESSTHAN") ){
			comparator = "LESSTHAN";
			string = string.substring(8).trim();
		}
		else if( string.startsWith("GREATERTHAN") ){
			comparator = "GREATERTHAN";
			string = string.substring(11).trim();
		}
		else if( string.startsWith("EQUALS") ){
			comparator = "EQUALS";
			string = string.substring(6).trim();
		}
		else{
			throw new DescriptionParserException("Unrecognized comparator for IF statement! '" + string + "'");
		}
		return comparator;
	}

	private static String skipNextToken(String string, DescriptionNode subs, int index, int iD) {
		System.out.println("===== Skipping NextToken in '" + string + "' ======");
		
		if( string.charAt(0) == '<' ){
			int closingBracket = getClosingIndex(string, "<", "/>", 0);
			if( closingBracket == -1 ){
				throw new DescriptionParserException("No closing bracket for syntax at index " + index);
			}
			
			return string.substring(closingBracket+2).trim();
		}
		else if( string.charAt(0) == '|' ){
			int closingAbsolute = string.indexOf("|",1);
			if( closingAbsolute == -1 ){
				throw new DescriptionParserException("No closing absolute syntax at index " + index);
			}
			
			return string.substring(closingAbsolute+1).trim();
		}
		
		int end = getNextSpaceIndex(string, 0);
		if( end > 0 ){
			return string.substring(end+1);
		}
		
		return "";		
	}

	private static String skip(String string, String leftSide) {
		return string.substring(leftSide.length()).trim();
	}

	private static DescriptionNode parseIf(String string, DescriptionNode subs, int index, int ID) {
		System.out.println("=========================================");
		System.out.println("=========================================");
		System.out.println("=========================================");
		System.out.println("=========================================");
		System.out.println("===== Parsing IF at index " + index + " '" + string + "' =====");
		System.out.println("=========================================");
		System.out.println("=========================================");
		System.out.println("=========================================");
		System.out.println("=========================================");
		
		// <IF i <5> Hello_ <IF/>
		string = skip(string,"IF");

		// Left Side
		String leftSide = getNextToken(string, subs, index, ID);
		string = skipNextToken(string, subs, index, ID);
		System.out.println("left: '" + leftSide + "'");
		
		
		// MIDDLE
		String comparator = getNextComparator(string);
		System.out.println("Comparator: '" + comparator + "'");
		string = skipNextToken(string, subs, index, ID);
		
		
		// Right side
		String rightSide = getNextToken(string, subs, index, ID);
		string = skipNextToken(string, subs, index, ID);
		System.out.println("right: '" + rightSide + "'");
		System.out.println("Remainaing String: '" + string + "'");
		
		
		// Get function
		int openingfunction = string.indexOf("{");
		if( openingfunction == -1 ){
			throw new DescriptionParserException("No function syntax found for IF statement at index " + index + " on description " + ID + " '" + string + "'");
		}
		
		int closingFunction = getClosingIndex(string, "{", "}", openingfunction);
		if( closingFunction == -1 ){
			throw new DescriptionParserException("No closing syntax found for function in IF statement at index " + index + " on description " + ID + " '" + string + "'");
		}
		
		boolean compared = compare(leftSide, comparator, rightSide);
		System.out.println("IF '" + leftSide + "' " + comparator + " '" + rightSide + "' RESULTS: " + compared);
		if( compared ){
			String function = substituteVariables(string.substring(openingfunction+1,closingFunction), subs, ID).trim();
			System.out.println("===== Finished Parsing Primary IF at index " + index + " '" + function + "' =====");
			return new DescriptionNode(function);
		}
		string = string.substring(closingFunction+1).trim();
		
		// <IF i EQUALS 5 {}
		while( string.startsWith("ELSE") ){
			string = skip(string, "ELSE");
			
			if( !string.startsWith("IF") ){
				// ALWAYS be true!
				string = "IF 1 EQUALS 1 " + string;
			}
			
			System.out.println("SUB IF '" + string + "'");
			DescriptionNode subIF = parseIf(string, subs, index, ID);
			if( subIF != null ){
				// If the ELSE IF is true.
				// Return what it contained
				System.out.println("===== Finished Parsing SUB IF with TRUE at index " + index + " '" + subIF + "' =====");
				return subIF;
			}
			else{
				int subOpeningfunction = string.indexOf("{");
				int subClosingFunction = getClosingIndex(string, "{", "}", subOpeningfunction);
				string = string.substring(subClosingFunction+1).trim();
			}
			
			
			
		}

		System.out.println("===== Finished Parsing IF with FALSE at index " + index + " '" + new DescriptionNode() + "' =====");
		return null;
	}

	private static String parseAbsolute(String string, DescriptionNode subs, int index, int ID) {
		System.out.println("=== Parsing Absolute (" + string + ") ===");

		if( string.startsWith("<") ){
			int closing = getClosingIndex(string, "<", "/>", 0);
			if( closing == -1 ){
				throw new DescriptionParserException("No closing bracket for syntax at " + index);
			}
			
			string = string.substring(1, closing);
		}
		DescriptionNode node = parse(string, subs, index, ID);//parse(string, subs, 0, ID);
		System.out.println("Node: " + node.toString() + " is array " + node.size() + " " + node.containsArrays());
		if( node.containsArrays() ){
			
		}
		else{
			for(int i = 1; i <= node.size(); i++){
				System.out.println("\t" + i + " " + node.getString(i));
			}
		}
		
		String value = String.valueOf(node.size());
		/*if( node.containsArrays() ){
			System.err.println("CONTAINS ARRAYS: " + value);
			System.out.print("\t'");
			for(String s : node.getArray(0) ){
				System.out.print(s + " ");
			}
			System.out.println("'");
			value = String.valueOf(node.size());O
			
		}
		else{
			value = String.valueOf(node.getString(0).length());
			System.err.println("DOESN'T CONTAINS ARRAYS: " + value);
		}*/
		

		System.out.println("=== Finished Parsing Absolute (" + value + ") ===");
		return value;
	}

	private static boolean compare(String leftSide, String comparator,
			String rightSide) {		
		
		if( !isNumber(leftSide) || !isNumber(rightSide)){
			if( comparator.equalsIgnoreCase("EQUALS")){
				return leftSide.equals(rightSide);
			}
			else{
				throw new DescriptionParserException("Unsupported operation for comparing non digits " + leftSide + " " + comparator + " " + rightSide);
			}
		}
		
		int left = isNumber(leftSide) ? Integer.parseInt(leftSide) : -1;
		int right = isNumber(rightSide) ? Integer.parseInt(rightSide) : -1;
		
		if( comparator.equalsIgnoreCase("LESSTHAN")){
			return left < right;
		}
		else if( comparator.equalsIgnoreCase("GREATERTHAN")){
			return left > right;
		}
		else if( comparator.equalsIgnoreCase("EQUALS")){
			return left == right;
		}
		
		throw new DescriptionParserException("Unknown comparator '" + leftSide + " " + comparator + " " + rightSide);
	}

	private static DescriptionNode parseInput(String string, DescriptionNode subs, int index, int ID) {
		System.out.println("=== Parsing Input (" + string + ") ===");
		
		// We do have this variable in the description
		int endNumber = getEndNumberIndex(string,0);
		
		int subIndex = Integer.parseInt(string.substring(0,endNumber+1));
		
		//<11???

		
		String nextKeyWords = string.substring(endNumber+1).trim();
		System.out.println("nextkeywords '" + nextKeyWords + "'");
		if( nextKeyWords.isEmpty() ){
			
			// Check the input exists
			if(subs.size() < subIndex){
				return new DescriptionNode("?");
			}
			// <11/> <-- get element from subs
			return new DescriptionNode(subs.getString(subIndex));
		}
		else if( nextKeyWords.startsWith("[") ){
			//<11[???
			char nextChar = nextKeyWords.charAt(1);
			
			// We want the array
			if( nextChar == ']' ){
				//<11[]???
				// Return array
				DescriptionNode node = new DescriptionNode(subs.getArray(subIndex));
				System.out.println("Returning array (" + node + ") " + node.size() + " containsArrays: " + node.containsArrays());
				return node;
			}
			else if( Character.isDigit(nextChar) ){
				//<11[2???
				// Get element FROM array
				int endArrayIndex = getEndNumberIndex(nextKeyWords, 1);
				if( nextKeyWords.charAt(endArrayIndex+1) == ']'){
					int index1 = Integer.parseInt(nextKeyWords.substring(1,endArrayIndex+1));
					// Return element from array
					return new DescriptionNode(subs.getSub(subIndex, index1));
				}
				else{
					throw new DescriptionParserException("Unknown keyword at index " + (endArrayIndex+index));
				}
			}
			
			throw new DescriptionParserException("Unknown keyword in array at " + (endNumber+2) + " for description " + ID);
		}
		
		throw new DescriptionParserException("Unknown keyword '" + nextKeyWords + "' in variable at " + (endNumber+index) + " for description " + ID);
	}

	private static int getNextSpaceIndex(String string, int startIndex) {
		int endNumber = startIndex+1;
		while( endNumber+1 < string.length() && (string.charAt(endNumber)) != ' ' ){
			endNumber++;
		}
		
		// -1 if we do not have a space
		if( string.charAt(endNumber) != ' ' ){
			return -1;
		}
		return endNumber;
	}
	
	private static int getEndNumberIndex(String string, int startIndex) {
		int endNumber = startIndex;
		while( endNumber+1 < string.length() && Character.isDigit(string.charAt(endNumber+1)) ){
			endNumber++;
		}
		return endNumber;
	}

	private static DescriptionNode parseOpeningLoop(String string, DescriptionNode subs, int index, int ID) {
		System.out.println("=== Parsing Opening Loop '" + string + "' ===");
		
		//checkSpace(string, index, ID);
		
		// LOOP_
		int loopLookIndex = 0;
		char savedVariable = string.charAt(loopLookIndex);
		String letter = pickNextLoopVariable(index,ID);
		if( Character.isLetter(savedVariable) ){
			letter = String.valueOf(savedVariable);
			
			// LOOP_i
			if( string.charAt(1) != ' '){
				throw new DescriptionParserException("Must contain a space after LOOPs variable at " + (index+5) + " for description " + ID);
			}
			
			loopLookIndex += 2;
			savedVariable = string.charAt(loopLookIndex);
		}
		
		
		
		
		// LOOP_i_
		int maxNumber = 20;
		if( Character.isDigit(savedVariable) ){
			int endNumber = loopLookIndex;
			while( endNumber+1 < string.length() && Character.isDigit(string.charAt(endNumber+1)) ){
				endNumber++;
			}
			maxNumber = Integer.parseInt(string.substring(loopLookIndex,endNumber+1));
			System.out.println("Loop max number: " + maxNumber);
		}
		else if( savedVariable == '|'){
			System.out.println("Needs absolute");
			// Get size of array
			int closingAbsolute = string.indexOf("|",loopLookIndex+1);
			String substringAbsolute = string.substring(loopLookIndex+1,closingAbsolute);
			String parsedAbsolute = parseAbsolute(substringAbsolute, subs, index, ID);
			maxNumber = Integer.parseInt(parsedAbsolute);
			//string = parsedAbsolute + string.substring(closingAbsolute).trim();
			System.out.println("Loop max number: " + maxNumber);
		}
		else{
			throw new DescriptionParserException("Must add max counter for loop at " + (index) + " for description " + ID);
		}
		
		// <LOOP_i_5
		
		int functionIndex = string.indexOf("{");
		if( functionIndex == -1 ){
			throw new DescriptionParserException("No function defined for loop at index " + index);
		}
		
		int closingFunctionIndex = getClosingIndex(string, "{", "}", functionIndex);
		if( closingFunctionIndex == -1 ){
			throw new DescriptionParserException("Closing bracket for function not defined for loop at index " + index);
		}
		
		String function = string.substring(functionIndex+1,closingFunctionIndex);
		
		// <LOOP_i_5 { ... } />
		
		// Do Loop
		String loopString = "";
		for(int i = 1; i <= maxNumber; i++){
			String subbedFunction = function.replaceAll("/"+letter, String.valueOf(i));
			System.out.println("=== CURRENTLY LOOPING " + subbedFunction + " ===");
			String parsed = substituteVariables(subbedFunction, subs, ID);
			loopString += parsed;
		}
		
		System.out.println("=== Finished Parsing Opening Loop '" + loopString + "' ===");
		return new DescriptionNode(loopString);
	}
	
	private static DescriptionNode parseLoop(String string, DescriptionNode subs, int index, int ID) {
		System.out.println("=== Parsing Loop '" + string + "' ===");
		
		// Skip LOOP
		string = string.substring(4).trim();

		DescriptionNode open = parseOpeningLoop(string, subs, index, ID);
		System.out.println("=== Finished Parsing Loop '" + string + "' ===");
		return open;		
	}

	private static String pickNextLoopVariable(int index, int ID) {
		String letter = "a";
		for(int i=0; i<10; i++){
			if( !savedVariables.containsKey(letter+i)){
				return (letter+i);
			}
		}
		
		throw new DescriptionParserException("Too many variables for description " + ID);
	}

	protected static int getClosingIndex(String substring, String openingSyntax, String closingSyntax, int index) {	
		//System.out.println("Getting closed index for '" + substring + "'\n\t o( " + openingSyntax + " ) c( " + closingSyntax + " ) at " + index);
		if( !substring.substring(index).startsWith(openingSyntax) ){
			System.err.println("Was not given opening syntax at index '" + index + "' for equation " + substring);
			return -1;
		}
		
		int opening = index;
		int closing = substring.indexOf(closingSyntax, index);
		if( closing == -1 ){
			throw new DescriptionParserException("Can not find closing bracket for " + substring + " at " + index + "  with O: '" + openingSyntax + "'" + " C: '" + closingSyntax + "'");
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
			
			
			//System.out.println("opens: " + innerOpens);
			//System.out.println("view: '" + substring.substring(opening, closing+1));
		}
		
		
		return closing;
	}
	
	protected static int getOpeningIndex(String string, String openingSyntax, String closingSyntax, int closingIndex) {	
		System.out.println("Getting opening index for '" + string + "' o( " + openingSyntax + " ) c( " + closingSyntax + " ) at " + closingIndex);
		if( !string.substring(closingIndex).startsWith(closingSyntax) ){
			System.err.println("Was not given closing syntax at index '" + closingIndex + "' for equation '" + string + "'");
			return -1;
		}
		
		
		// Make the string as small as possible as the opening will be to the left of the the index
		//string = string.substring(0, closingIndex+closingSyntax.length());

		int opening = string.indexOf(openingSyntax);
		while( opening != -1 ){
			int expectedClosing = getClosingIndex(string, openingSyntax, closingSyntax, opening);
			if( expectedClosing == closingIndex ){
				return opening;
			}
			else{
				opening = string.indexOf(openingSyntax, opening+1);
			}
		}		
		
		System.err.println("Could not find opening bracket!");
		return -1;
	}
	
	
	/**
	 * Loads the descriptions
	 */
	public static void load(){
		System.out.println("Loading Descriptions: '" + filename + "'");
		
		
		// Scans file
		Scanner scan = null;
		if( !filename.contains(":")) {
			InputStream file = DescriptionParser.class.getResourceAsStream(filename);
			if( file == null ){
				throw new DescriptionParserException("Could not find file: '" + filename + "'");
			}
			scan = new Scanner(file);
		}
		else{
			try {
				scan = new Scanner(new File(filename));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new DescriptionParserException("Could not find file: '" + filename + "'");
			}
		}
		
		List<String> descriptions = new ArrayList<String>();
		String currentDescription = PLACEHOLDER;
		int currentNumber = -1;
		int lineNumber = 0;
		while(scan.hasNext()){
			String line = scan.nextLine().trim();
			lineNumber++;
			
			// Ignore comments
			if( line.startsWith("//")){
				continue;
			}
			
			// Check for comments in the line
			int index = line.indexOf("//");
			if( index != -1 ){
				line = line.substring(0, index);
			}

			
			int tokenNumber = 0;
			Scanner lineScanner = new Scanner(line);
			while(lineScanner.hasNextLine()){
				tokenNumber++;
				
				// We can assume there are no comments
				
				
				
				
				if( currentNumber == -1){ // Start Description
					String nextToken = lineScanner.next();
					if( nextToken.endsWith("/") && isNumber(nextToken.substring(0, nextToken.length()-1)) ){ // 5/
						currentNumber = Integer.parseInt(nextToken.substring(0, nextToken.length()-1));
						System.out.println("Starting description " + currentNumber);
					}
					else{
						lineScanner.close();
						scan.close();
						throw new DescriptionParserException("Missing Description ID on line " + lineNumber + " at token " + tokenNumber + " (" + nextToken + ")");
					}
				}
				else if( line.startsWith("/") && isNumber(line.substring(1))){ // Finishing Description /5
					
					int closingNumber = Integer.parseInt(line.substring(1)); 
					if( closingNumber == currentNumber ){
						// We have the corrent closing 
						
						addDescription(currentDescription, descriptions, closingNumber);
						System.out.println("Saved description " + currentNumber);
						
						// Reset information
						currentNumber = -1;
						currentDescription = PLACEHOLDER;
						break;
					}
					else{ // Closing WRONG description
						lineScanner.close();
						scan.close();
						throw new DescriptionParserException("Expecting closing syntax for description " + currentNumber + " but instead got " + closingNumber + "!");
					}
				}
				else{
					
					
					// Added to description
					if( !currentDescription.equals(PLACEHOLDER) ){
						currentDescription += "\n";
					}
					else{
						currentDescription = "";
					}
					
					currentDescription += line;
					break;
				}
			}
			
			
			lineScanner.close();
		}
		
		DescriptionParser.decriptions = descriptions;
		scan.close();
		

		
		// Didn't close brackets!
		if( currentNumber != -1 ){
			throw new DescriptionParserException("No closing syntax for description " + currentNumber + "!");
		}
		
		// Record when the file was l;ast modified
		lastModified = new File(filename).lastModified();
		
		System.out.println("Successfully loaded Descriptions.");
		
	}
	
	public static void setDescriptionPrefix(String newPrefix){
		
		// Error check
		if( newPrefix == null || !( newPrefix instanceof String) ){
			throw new DescriptionParserException("Prefix' must be of type String " + newPrefix);
		}
		
		// Check if they are the same
		if( prefix.equals(newPrefix) ){
			return;
		}
		
		prefix = newPrefix;
		
		// Reload all the data
		// This avoids stacked prefix'
		setup();
	}
	
	public void clearDescriptionPrefix(){
		setDescriptionPrefix("");;
	}
	
	public static String getDescriptionPrefix(){
		return prefix;
	}
	
	private static String removeTags(String line) {
		System.err.println("Removing Tags '" + line + "'");
		
		String noTags = line;
		
		int closingTag = noTags.indexOf("/>");
		while( closingTag != -1 ){
			int openingTag = getOpeningIndex(noTags, "<", "/>", closingTag);
			
			String left = openingTag > 0 ? noTags.substring(0,openingTag) : "";
			String right = noTags.substring(closingTag+2);
			noTags = left+right;
			System.err.println("\t" + noTags);
			
			closingTag = noTags.indexOf("/>");
		}
		
		return noTags;
	}

	private static void addDescription(String description, List<String> list, int index){
		// Descriptions are listed via numbers, not array index's
		
		while( list.size() < index ){
			list.add(PLACEHOLDER);
		}
		
		// Check we haven't added to this index already
		if( !list.get(index-1).equals(PLACEHOLDER) ){
			throw new DescriptionParserException("Description at index " + index + " already exists!");
		}
		
		// Add to the list
		list.set(index-1, description);
		
		System.out.println("===============");
		System.out.println("Saved Description: " + index);
		System.out.println("===============");
		Scanner scan = new Scanner(description);
		while( scan.hasNextLine() ){
			System.err.println("\t" + scan.nextLine());
		}
		System.out.println();
		scan.close();
	}
	
	public static void main(String[] args){
		load();
		
		System.out.println("Printing Descriptions: ");
		if( decriptions != null ){
			for(int i = 0; i < decriptions.size(); i++){
				System.out.println(i + ": " +  decriptions.get(i));
			}
		}
		
		/*System.out.println("Testing descriptions:");
		System.out.println("== one ==");
		System.out.println(getDescription(7));
		
		System.out.println("== two ==");
		System.out.println(getDescription(6, "2x", "100", "x", "(3/2)"));
		System.out.println(getDescription(6, toArray("2x"), toArray("100"), toArray("x","y"), toArray("(3/2)")));
		*/
	}

	private static boolean fileHasChanged() {
		
		File currentFile = new File(filename);
		long currentModified = currentFile.lastModified();
		
		return currentModified != lastModified;
	}
	
	private static String[] toArray(String... strings) {
		return strings;
	}

	private static String arrayToString(String[] array){
		String s = "";
		for(String a : array){
			s+= a + " ";
		}
		return s;
	}
	
	private static String arrayToString(String[][] array){
		String s = "";
		for(String[] a : array){
			s+= "(" + arrayToString(a) + ") ";
		}
		return s;
	}
	
	public static boolean isNumber(String value){
		
		try{
			Integer.parseInt(value);
			return true;
		}
		catch( NumberFormatException e ){
			return false;			
		}		
	}

	public static void setFile(String path) {
		filename = path;
	}

	/**
	 * Returns how many descriptions have been loaded.
	 * @return Integer between -1 and Integer.Max_Value() 
	 */
	public static int size() {
		setup();
		return decriptions.size();
	}
}

