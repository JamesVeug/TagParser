package editor;

import java.util.Arrays;
import java.util.List;


public class EquationScanner2 {
	private static Character[] bracket_array = {'(', ')'};
	private static Character[] operator_array = {'+','*','-','/','%', '^', '='};
	private static String[] divider_array = {"TRUE","FALSE", "SQRT", "LOG"};
	private static List<Character> brackets = Arrays.asList(bracket_array);
	private static List<Character> operators = Arrays.asList(operator_array);
	private static List<String> dividers = Arrays.asList(divider_array);
	
	private final String givenEquation;
	
	private int currentIndex = 0;
	private final String[] splitEquation;
	
	public EquationScanner2(String equation){
		givenEquation = equation;
		splitEquation = divideEquation(equation);
	}
	
	/**
	 * Gets the next element
	 * @return String of the next element
	 */
	public String next(){
		return splitEquation[currentIndex++];
	}
	
	/**
	 * Returns the next string in the equation without moving the cursor.
	 * @return String of the next element
	 */
	public String peek(){
		return splitEquation[currentIndex];
	}
	
	/**
	 * Checks if there is another element to receive from the scanner
	 * @return true if there is another valid next call.
	 */
	public boolean hasNext(){
		return currentIndex < size();
	}
	
	/**
	 * Gets the total amount of elements in the equation
	 * @return element size
	 */
	public int size(){
		return splitEquation.length;
	}
	
	/**
	 * Returns the equation that was given to this scanner
	 * @return string of the equation
	 */
	public String givenEquation(){
		return givenEquation;
	}

	private static String[] divideEquation(String equation){
		System.out.println("Equation: " + equation);
		
		// Remove all spaces
		equation = equation.replaceAll(" ", "");
		
		// Build spacedEquation
		String spacedEquation = equation.length() == 1 ? equation : "";
		
		//Add spaces
		for(int i =0; i < equation.length()-1; i++){
			char a = equation.charAt(i);
			char b = equation.charAt(i+1);
			
			// Add a
			spacedEquation += a;
			
			//System.out.println("A: " + a + " B: " + b);
			if( (Character.isDigit(a) || a == '.') && (!Character.isDigit(b) && b != '.')){
				spacedEquation += " ";
			}
			else if( (Character.isDigit(b) || b == '.') && (!Character.isDigit(a) && a != '.') ){
				spacedEquation += " ";
			}
			else if( Character.isLetter(a) && Character.isLetter(b) ){
				
				// Check for words
				boolean found = false;
				for(String s : dividers){
					String sub = equation.substring(i,i+2);
					if( (i+s.length()-1) < equation.length() && s.startsWith(String.valueOf(sub.toUpperCase())) ){
						String subWord = equation.substring(i, i+s.length());
						if( subWord.equalsIgnoreCase(s) ){
							spacedEquation = spacedEquation.substring(0,spacedEquation.length()-1) + s;
							i+= s.length()-1;
							found = true;
							break;
						}
					}
				}

				spacedEquation += " ";
				if( found ){
					continue;
				}				
			}
			else if( Character.isLetter(a) && !Character.isLetter(b) ){
				spacedEquation += " ";				
			}
			else if( operators.contains(a)){
				spacedEquation += " ";
			}
			else if( brackets.contains(a)){
				spacedEquation += " ";
			}
			
			// Add last element
			if( (i+2) >= equation.length() ){
				spacedEquation += b;
			}
		}
		
		
		System.out.println("Spaced: " + spacedEquation);
		
		String[] splitEquation = spacedEquation.split(" ");
		System.out.println("Split: ");
		for(String s : splitEquation){
			System.out.println(s);
		}
		return splitEquation;
	}
}
