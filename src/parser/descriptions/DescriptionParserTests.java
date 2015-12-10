package parser.descriptions;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class DescriptionParserTests {
	
	@Test
	public void testDescriptions(){
		compareStrings(DescriptionParser.getDescription(1), "Solve Using BEDMAS.");
		compareStrings(DescriptionParser.getDescription(2), "Simplify before solving");
	}	
	
	@Test
	public void testDescriptionsInput(){

		String parsed = DescriptionParser.getDescription(11,"x","(3/2)");
		compareStrings(parsed, "Replace x with (3/2) in the first equation.");
		
		parsed = DescriptionParser.getDescription(9,"x","*","55");
		compareStrings(parsed, "Find substitution for x by multiplying 55 on both sides.");
	}
	
	@Test
	public void testDescriptionsLoops(){
		String parsed = DescriptionParser.getDescription(6,toArray("2xy"),toArray("100"),toArray("x","y"),toArray("25","2"));
		compareStrings(parsed, "2xy is the same as 100 by replacing x with 25 and y with 2.");

		 parsed = DescriptionParser.getDescription(12);
		compareStrings(parsed, "TEST 1 2 3 4 5 ");
		
		parsed = DescriptionParser.getDescription(15);
		compareStrings(parsed, "TEST 1 ");

		parsed = DescriptionParser.getDescription(6,toArray("2x"),toArray("100"),toArray("x"),toArray("50"));
		compareStrings(parsed, "2x is the same as 100 by replacing x with 50.");
		
		
	}
	
	@Test
	public void testDescriptionsIF(){

		String parsed = DescriptionParser.getDescription(13);
		compareStrings(parsed, "TEST 1 is less than two yes so we can be happy.");
		
		parsed = DescriptionParser.getDescription(14);
		compareStrings(parsed, "TEST so we should NOT be happy.");
	}
	
	@Test
	public void testDescriptionsDefine(){

		String parsed = DescriptionParser.getDescription(16);
		compareStrings(parsed, "TEST Words");
		
		parsed = DescriptionParser.getDescription(17);
		compareStrings(parsed, "TEST Hello World");
	}
	
	@Test
	public void testDescriptionsNewLine(){

		String parsed = DescriptionParser.getDescription(18);
		compareStrings(parsed, "TEST ONE\nTEST TWO\nTEST THREE");
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

}
