package parser.descriptions;

import java.util.ArrayList;
import java.util.List;

public class DescriptionNode {
	private List<String[]> strings = new ArrayList<String[]>();
	private List<String> substrings = new ArrayList<String>();

	public DescriptionNode(){
	}
	
	public DescriptionNode(String[]... arrays){
		System.out.println("Creating list of arrays of strings");
		//containsArrays = true;
		
		// Check none are null
		
		for(String[] a : arrays){
			if( a != null && !(a.length == 1 && (a[0].equalsIgnoreCase("null") || a[0].isEmpty()))){
				strings.add(a);
			}
		}
	}
	
	public DescriptionNode(String... strings){
		System.out.println("Creating list of strings from array");
		for(String a : strings){
			if( a != null && !a.equalsIgnoreCase("null") && !a.isEmpty()){
				substrings.add(a);
			}
		}
	}
	
	public DescriptionNode(List<String> strings){
		System.out.println("Creating list of strings from list");
		for(String a : strings){
			if( a != null && !a.equalsIgnoreCase("null") && !a.isEmpty()){
				substrings.add(a);
			}
		}
	}
	
	
	public String getString(int index){
		return containsArrays() ? strings.get(index-1)[0] : substrings.get(index-1);
	}
	
	public String getSub(int index, int subindex){
		if( index-1 >= strings.size() ){
			throw new DescriptionParserException("Could not obtain Array.\n\tIndex " + index + " exceeded max string input size " + strings.size() + "\n\t" + toString());
		}
		else if( subindex-1 >= strings.get(index-1).length ){
			throw new DescriptionParserException("Could not obtain String from array.\n\tIndex " + subindex + " exceeded max string input size " + strings.get(index-1).length + "\n\t" + toString());
		}
		
		return strings.get(index-1)[subindex-1];
	}
	
	public String[] getArray(int index){
		if( index-1 >= strings.size() ){
			throw new DescriptionParserException("Could not obtain Array.\n\tIndex " + index + " exceeded max string input size " + strings.size() + "\n\t" + toString());
		}
		
		return strings.get(index-1);
	}

	public boolean containsArrays(){
		return substrings.isEmpty();
	}
	
	@Override
	public String toString(){
		String to = "";
		if( containsArrays() ){
			for(String[] s : strings){
				to += "(" + arrayToString(s) + ") ";
			}
		}
		else{
			Object []temp2 = substrings.toArray();
			to = arrayToString(temp2);
		}
		
		return to; 
	}
	
	private static String arrayToString(Object[] array){
		
		int x = 0;
		String s = "";
		for(Object a : array){
			s += a.toString() + ( (++x) < (array.length-1) ? " " : "" );
		}
		return s;
	}

	public int size() {
		return !containsArrays() ? substrings.size() : strings.size();
	}

	public boolean isEmpty() {
		System.out.println(toString());
		System.out.println("\tContains Arrays: " + containsArrays());
		System.out.println("\tIs Empty: " + (size() == 0) + " " + size());
		return size() == 0;
	}
}
