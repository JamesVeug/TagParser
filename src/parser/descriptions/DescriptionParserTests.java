package parser.descriptions;

import static org.junit.Assert.fail;

import org.junit.Test;

public class DescriptionParserTests {
	
	@Test
	public void testDescriptions(){
		compareStrings(getDescription(1), "Solve Using BEDMAS");
		compareStrings(getDescription(2), "Simplify before solving");
	}	
	
	@Test
	public void testDescriptionsInput(){

		String parsed = getDescription(11,"x","(3/2)");
		compareStrings(parsed, "Replace x with (3/2) in the first equation.");
		
		parsed = getDescription(9,"x","*","55");
		compareStrings(parsed, "Find substitution for x by multiplying both sides by 55.");
	}
	
	@Test
	public void testDescriptionsLoops(){
		String parsed = getDescription(6,toArray("2xy"),toArray("100"),toArray("x","y"),toArray("25","2"));
		compareStrings(parsed, "MATH{2xy} is the same as MATH{100} by replacing MATH{x} with MATH{25} and MATH{y} with MATH{2}");

		 parsed = getDescription(12);
		compareStrings(parsed, "TEST 1 2 3 4 5 ");
		
		parsed = getDescription(15);
		compareStrings(parsed, "TEST 1");

		parsed = getDescription(6,toArray("2x"),toArray("100"),toArray("x"),toArray("50"));
		compareStrings(parsed, "MATH{2x} is the same as MATH{100} by replacing MATH{x} with MATH{50}");
		
		
	}
	
	@Test
	public void testDescriptionsIF(){

		String parsed = getDescription(13);
		compareStrings(parsed, "TEST 1 is less than two yes so we can be happy.");
		
		parsed = getDescription(14);
		compareStrings(parsed, "TEST so we should be happy.");
	}
	
	@Test
	public void testDescriptionsDefine(){

		String parsed = getDescription(16);
		compareStrings(parsed, "TEST Words");
		
		parsed = getDescription(17);
		compareStrings(parsed, "TEST Hello World");
	}
	
	@Test
	public void testDescriptionsNewLine(){

		String parsed = getDescription(18);
		compareStrings(parsed, "TEST ONE\nTEST TWO\n\nTEST THREE");
	}

	@Test
	public void getClosingIndextest() {
		testClosingIndexEquals("<LOOP i |<3[]/>|{<3[i]/> with <4[i]/><IF i LESSTHAN |<3[]/>| { and }/>}/>","<","/>",0,71);

		//<>
		testClosingIndexEquals("<>", "<", ">", 0,1);
		testClosingIndexEquals("<<>>", "<", ">", 0,3);
		testClosingIndexEquals("<<>>", "<", ">", 1,2);
		
		testClosingIndexEquals("<stuff>", "<", ">", 0,6);
		testClosingIndexEquals("<<stuff>>", "<", ">", 0,8);
		testClosingIndexEquals("<<stuff>>", "<", ">", 1,7);
		testClosingIndexEquals("<stuff<forgot>>", "<", ">", 0,14);
		testClosingIndexEquals("<stuff<forgot>>", "<", ">", 6,13);
		
		//</>
		testClosingIndexEquals("</>", "<", "/>", 0,1);
		testClosingIndexEquals("<</>/>", "<", "/>", 0,4);
		testClosingIndexEquals("<</>/>", "<", "/>", 1,2);
		
		testClosingIndexEquals("</></>", "<", "/>", 0,1);
		testClosingIndexEquals("</></>", "<", "/>", 3,4);
		
		testClosingIndexEquals("<stuff/>", "<", "/>", 0,6);
		testClosingIndexEquals("<stuff/><and/>", "<", "/>", 0,6);
		testClosingIndexEquals("<stuff/><and/>", "<", "/>", 8,12);
		
		testClosingIndexEquals("<stuff<and/>/>", "<", "/>", 0,12);
		testClosingIndexEquals("<stuff<and/>/>", "<", "/>", 6,10);

		//Complex stuff
		testClosingIndexEquals("<LOOP i |<3[]/>|/>","<","/>",0,16);
		
		
		// FAILED TESTS
		testClosingIndexFailed("<>", "<", ">", 1);
		testClosingIndexFailed("<<>>", "<", ">", 2);
		testClosingIndexFailed("<<>>", "<", ">", 3);
		
		testClosingIndexFailed("<stuff>", "<", ">", 1);
		testClosingIndexFailed("<<stuff>>", "<", ">", 2);
		testClosingIndexFailed("<<stuff>>", "<", ">", 7);
		testClosingIndexFailed("<stuff<forgot>>", "<", ">", 1);
		testClosingIndexFailed("<stuff<forgot>>", "<", ">", 5);
	}


	/*@Test
	public void testNoUnpassedDescriptions(){
		
		// All descriptions start with <<<< now
		String prefix = "<<<<";
		DescriptionParser.setDescriptionPrefix(prefix);
		
		float maxRuns = 10;
		
		int run = 0;
		while( ++run <= maxRuns ){
		    String foo = EquationFactory.getRandomAlgebraEquation();
		    System.out.println("Random Equation: " + foo);
		    
		    Equation equation = null;
		    try{
		    	equation = Parser.solveEquation(EquationFactory.getEquation(foo));
		    }catch(Exception e){ run--; continue; }
		    
		    for(EquationEntry e : equation.getPostFixElements()){
		    	String description = e.getDescription();
		    	if(!description.startsWith(prefix)){
		    		String error = "\tGiven Equation: '" + foo + "'\n"
		    				+ "\tCurrent Equation: '" + e.toString() + "'\n"
		    				+ "\tDescription: '" + description + "'. \n"
		    				+ "\tEquation: " + equation.toString() + "\n";
		    		fail("Found description without prefix after " + run + " runs: \n" + error);
		    	}
		    }
		}
		
		if(maxRuns == 10){
			fail("WARNING: Need to test on more than 10 runs!");
		}
	}*/
	
	
	
	
	private void compareStrings(String parsed, String string) {
		if( !parsed.equals(string) ){			
			fail("Descriptions do not match: \nReceived: '" + parsed + "'\nExpected: '" + string + "'");
		}
		
	}

	private void testClosingIndexFailed(String equation, String opening, String closing, int startIndex) {
		int closingIndex = DescriptionParser.getClosingIndex(equation, opening, closing, startIndex);
		if( closingIndex != -1){
			fail("Expected closing index '-1' but instead got '" + closingIndex + "' for equation " + equation);
		}
	}

	private void testClosingIndexEquals(String equation, String opening, String closing, int startIndex, int expectedClosingIndex) {
		int closingIndex = DescriptionParser.getClosingIndex(equation, opening, closing, startIndex);
		if( closingIndex != expectedClosingIndex){
			fail("Expected closing index '" + expectedClosingIndex + "' but instead got '" + closingIndex + "' for equation " + equation);
		}
		
	}
	

	private static String[] toArray(String... strings) {
		return strings;
	}

	
	public String getDescription(int ID){
		return DescriptionParser.getDescription(ID).replaceFirst(DescriptionParser.getDescriptionPrefix(), "");
	}
	
	public String getDescription(int ID, String[]... array){
		return DescriptionParser.getDescription(ID,array).replaceFirst(DescriptionParser.getDescriptionPrefix(), "");
	}
	
	public String getDescription(int ID, String... array){
		return DescriptionParser.getDescription(ID,array).replaceFirst(DescriptionParser.getDescriptionPrefix(), "");
	}
}
