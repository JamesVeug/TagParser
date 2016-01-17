package editor;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import javax.swing.JFileChooser;

import parser.descriptions.DescriptionParser;
import parser.descriptions.DescriptionParserException;


public class TagParser extends Application{
	private TextArea code;
	private Pane results;
	private TextArea errorsTerminal;
	private TextField descriptionIDField;
	private Button inputHelpButton;
	private ScrollPane resultsScroll;
	private ScrollPane codeScroll;
	
	private TextField inputField;
	private Button compileButton;
	
	private final String programTitle = "Tag Editor";
	private final String localCodeFileName = "./LocalCode~.txt";
	private final String localOptionsFileName = "./LocalOptions~.txt";

	private String currentFile = null;
	
	// Example Code for when there are no local files supplied
	public static final String EXAMPLE_CODE = ""
			+ "// If we are given the equation 2x=6 \n"
			+ "// And we manage to get the correct answer, we will get the following description \n"
			+ "// <2x> is the same as <6> by replacing <x> with <3>. \n"
			+ "// \n"
			+ "// If we have 2xy=100 \n"
			+ "// Then x will be 25 and y will be 2 \n"
			+ "// Then the description will be \n"
			+ "// <2xy> is the same as <100> by replacing <<x with 25> and <y with 2>> \n"
			+ "//  \n"
			+ "6/ \n"
			+ "<1/> is the same as <2/> by replacing <LOOP x |<3[]/>|{<3[/x]/> with <4[/x]/><IF /x LESSTHAN |<3[]/>| { and }/>}/>. \n"
			+ "/6";
	

	public static final String EXAMPLE_DESCRIPTIONID = "6";
	public static final String EXAMPLE_INPUTS = "2xy:100:x,y:25,2";
	
	// Tooltips
	public static final List<Tooltip> tooltips = new ArrayList<Tooltip>();
	static{
		
		// Code
		tooltips.add(new Tooltip("Code Pane\nWrite your code for the descriptions here!"));
		
		// Compile Button
		tooltips.add(new Tooltip("Compiles the code and displays the output with the given inputs in the results Pane.\nSaves a backup version of the code into a local file."));
		
		// ID Field
		tooltips.add(new Tooltip("Description ID Field\nEnter a valid description ID from the Code Pane to be displayed in the Results Pane.\nOnly a single number greater than -1 can be entered."));
		
		// Input Field
		tooltips.add(new Tooltip("Input Field\nTest the selected description with certain Input to see the outcome. \nPress the '?' for more info."));
		
		// Help Button
		tooltips.add(new Tooltip("Get more help on the Input Field."));
		
		// Results
		tooltips.add(new Tooltip("Results Pane\nOutput from the code will be displayed here when the Compile button is pressed. The outcome is determined by the given Description ID and input field."));
		
		// Error Terminal
		tooltips.add(new Tooltip("Error Terminal\nWhen errors occur in the Code Pane, Description ID Field or Input Field. The Errors will be displayed here to help you solve the problem."));
	}
	
	public static String INPUT_HELP_DESCRIPTION = "";

	public boolean needsSaving = false;
	public boolean isCodeEdited = false;
	public boolean toolTipsEnabled = false;
	public Font CompileButtonNOTEditedFont = Font.font("Verdana", FontWeight.NORMAL, 10);
	public Font CompileButtonEditedFont = Font.font("Verdana", FontWeight.BOLD, 10);
	public Font errorTerminalFont = Font.font("Arial", FontWeight.BOLD, 10);
	private Stage primaryStage;
	
	/**
	 * Saves a version for only the program
	 */
	private void saveLocal(){
		
		// CODE
		File localCodeFile = new File(localCodeFileName);
		try {
			PrintWriter writer = new PrintWriter(localCodeFile, "UTF-8");
			writer.print(code.getText());
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch ( UnsupportedEncodingException e ){
			e.printStackTrace();
		}
		
		// OPTIONS
		File localOptionsFile = new File(localOptionsFileName);
		try {
			PrintWriter writer = new PrintWriter(localOptionsFile, "UTF-8");
			writer.println(descriptionIDField.getText());
			writer.println(inputField.getText());
			writer.println(currentFile);
			writer.print(isCodeEdited);
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch ( UnsupportedEncodingException e ){
			e.printStackTrace();
		}
	}

	private int getDescriptionID(String options){
		
		try{
			int id = Integer.parseInt(descriptionIDField.getText().trim());
			return id;
			
		}catch(NumberFormatException e){
			e.printStackTrace();
		}
		
		return 0;
	}
	
	private String[][] getInputs(String inputString) {
		//System.out.println("Input: " + inputString);
		
		String[] splitInputs = inputString.split(":");
		/*System.out.print("Split: ");
		for(String s : splitInputs){
			System.out.print(s + " ");
		}
		System.out.println();*/
		

		List<String[]> doubleSplit = new ArrayList<String[]>();
		for(String s : splitInputs ){
			doubleSplit.add(s.split(","));
		}
		
		
		
		/*System.out.print("Double Split: ");
		for(String[] d : doubleSplit){
			System.out.print("(");
			for(String s : d){
				System.out.print(s + " ");
			}
			System.out.print(")");
		}
		System.out.println();*/
		
		
		String[][] temp = {};
		temp = doubleSplit.toArray(temp);
		
		System.out.print("Final Input: ");
		for(String[] d : temp){
			System.out.print("(");
			for(String s : d){
				System.out.print(s + " ");
			}
			System.out.print(")");
		}
		System.out.println();
		
		return temp;
	}
	
	/**
	 * Checks the inputs entered by the user to see if they have any problems iwth them
	 * @param inputs
	 * @return true if there are no errors
	 */
	private boolean checkInputsForErrors(String[][] inputs) {
		
		try{
			
			for(int i = 0; i < inputs.length; i++){
				String[] a = inputs[i];
				
				for( int j = 0; j < a.length; j++){
					String s = a[j];
					
					// Check for closing brackets (......)
					int openBracketIndex = s.indexOf("(");
					while( openBracketIndex != -1 ){
						int closingBracket = DrawableGroupParser.getClosingIndex(s, "(", ")", openBracketIndex);
						if( closingBracket == -1 ){
							displayInputError("Missing closing bracket for input " + (i+1) + " and item " + (j+1) + ".");
							System.out.println("Finished checking for errors");
							return false;
						}
						
						openBracketIndex = s.indexOf("(", openBracketIndex+1);
					}
					
	
					// Check for closing curly brackets {......}
					int openCurlyBracketIndex = s.indexOf("{");
					while( openCurlyBracketIndex != -1 ){
						int closingBracket = DrawableGroupParser.getClosingIndex(s, "{", "}", openCurlyBracketIndex);
						if( closingBracket == -1 ){
							displayInputError("Missing closing curly bracket for input " + (i+1) + " and item " + (j+1) + ".");
							System.out.println("Finished checking for errors");
							return false;
						}
						
						openCurlyBracketIndex = s.indexOf("(", openCurlyBracketIndex+1);
					}
				}
			}
		
		}catch(RuntimeException e){
			displayInputError(e.getMessage());
			System.out.println("Finished checking for errors");
			return false;
		}
		
		// No Errors
		System.out.println("Finished checking for errors");
		return true;
	}
	
	private void displayInputError(String error){
		//inputField.setBackground(inputErrorColor);
		errorsTerminal.setText("Input Error: \n" + error);
	}

	private void setupMenu(BorderPane mainLayout, Stage primaryStage) {
		MenuBar menuBar = new MenuBar();
		menuBar.prefWidthProperty().bind(primaryStage.widthProperty());
		Menu file = new Menu("File");
		MenuItem newFile = new MenuItem("New File");
		newFile.setOnAction(new NewFileListener());
		MenuItem loadFile = new MenuItem("Load File");
		loadFile.setOnAction(new LoadFileListener());
		MenuItem saveFile = new MenuItem("Save");
		saveFile.setOnAction(new SaveFileListener());
		MenuItem saveAs = new MenuItem("Save As");
		saveAs.setOnAction(new SaveAsListener());
		MenuItem toggleTooltips = new MenuItem("Toggle Tooltips");
		toggleTooltips.setOnAction(new ToggleTooltipsListener());
		MenuItem quitFile = new MenuItem("Quit");
		quitFile.setOnAction(new QuitListener());
		
		file.getItems().add(newFile);
		file.getItems().add(loadFile);
		file.getItems().add(saveFile);
		file.getItems().add(saveAs);
		file.getItems().add(toggleTooltips);
		file.getItems().add(quitFile);
		menuBar.getMenus().add(file);
		mainLayout.setTop(menuBar);
	}
	
	private String getTextFromFile(File file){
		try {
			String fileText = "";
			Scanner scan = new Scanner(file);
			while(scan.hasNextLine()){
				fileText += scan.nextLine();
				
				if( scan.hasNextLine() ){
					fileText += "\n";
				}
			}
			scan.close();
			return fileText;
		} catch (FileNotFoundException e) {	
			e.printStackTrace(); 		
		}
		
		return "Failed to load file";
	}
	
	private void saveFile(){
		
		// Make sure we have a file selected
		if( currentFile == null ){
			JFileChooser chooser = new JFileChooser(".");
			int selected = chooser.showSaveDialog(null);
			if( selected == JFileChooser.CANCEL_OPTION ){
				return;
			}
			
			File file = chooser.getSelectedFile();
			currentFile = file.getAbsolutePath();
		}
	
		
		try {
			
			// Save our current work to the local file
			saveLocal();
			
			// Only saves what we can see in the compile panel
			// Save to the file we want to use
			PrintWriter writer = new PrintWriter(currentFile);
			Scanner localScan = new Scanner(new File(localCodeFileName));
			while(localScan.hasNextLine()){
				writer.print(localScan.nextLine());
				if( localScan.hasNextLine() ){
					writer.println();
				}
			}
			localScan.close();
			writer.close();
			compileButton.setFont(CompileButtonNOTEditedFont);
			isCodeEdited = false;
			changeTitle();

			
			needsSaving = false;
			
			// Popup Window
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Saved");
			alert.setHeaderText("Your work has been saved Successfully.");
			alert.showAndWait();


		} catch (FileNotFoundException e) {
			e.printStackTrace();
			//JOptionPane.showMessageDialog(descriptionIDField, "Failed to Save!", "Message", 0);	
		}
	}
	
	private void compileCode() {
		// Save our current work to the local file
		saveLocal();
		
		// Tell descriptionParser to read our new files 
		File file = new File(localCodeFileName);
		try {
			String path = file.getCanonicalPath();
			System.out.println("Assigning Path: " + path);
			DescriptionParser.setFile(path);
			
			// Get the parser to, parse it.
			String[] options = getLocalOptions();
			String[][] inputs = getInputs(options[1]);
			int descriptionID = getDescriptionID(options[0]);
			
			// Check each input for errors
			boolean noerrors = checkInputsForErrors(inputs);
			if( !noerrors ){
				return;
			}
			
			try{
				String parsedCode = DescriptionParser.getDescription(descriptionID,inputs);
				
				// Display results
				drawResults(parsedCode);
				
				// Reset
				errorsTerminal.setText("");
				compileButton.setFont(CompileButtonNOTEditedFont);
				isCodeEdited = false;
			}catch(DescriptionParserException e){
				displayErrorMessage(e);
			}
		} catch (IOException e) {e.printStackTrace();}
	}
	
	/**
	 * Draws the outcome of the selected Description on the screen.
	 * @param parsedCode Description taken from the DescriptionParser and needs to be displayed on the screen.
	 */
	public void drawResults(String parsedCode){
		
		// GRAPHICS
		DrawableGroupParser.setScreenDimensions((int)results.getWidth(), (int)results.getHeight());
		results.getChildren().clear();
		
		DrawableGroup group = DrawableGroupParser.getDrawableGroup(parsedCode);
		for(DrawableNode node : group.list){
			if( node.getText().equals("^") ){
				continue;
			}
			else if( node.getText().equals("/") ){
				Line line = new Line();
				line.setStartX(node.getX());
				line.setStartY(node.getY()+group.height);
				line.setEndX(node.getX()+node.getWidth());
				line.setEndY(node.getY()+group.height);
				line.setFill(Color.BLACK);
				results.getChildren().add(line);
				
				continue;
			}
			
			Text text = new Text(node.getText());
			text.setX(node.getX());
			text.setY(node.getY()+group.height);
			text.setFont(Font.font(node.getFont(), node.getFontSize()));
			text.setFill(Color.BLACK);
			results.getChildren().add(text);
		}
	}

	private String[] getLocalOptions() {
		File file = new File(localOptionsFileName);
		String fileText = getTextFromFile(file);
		String[] options = divideLocalOptions(fileText);
		return options;
	}
	
	private boolean checkFileIsSaved(){
		
		if( !needsSaving ){
			return true;
		}
		
		Alert alert = new Alert(AlertType.WARNING);
		alert.setTitle("Unsaved");
		alert.setHeaderText("There is unsaved work. Do you want to save before continuing?");

		ButtonType buttonYes = new ButtonType("Yes");
		ButtonType buttonNo = new ButtonType("No");
		ButtonType buttonCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);

		alert.getButtonTypes().setAll(buttonYes, buttonNo, buttonCancel);

		Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == buttonYes){
		    saveFile();
			
		} else if (result.get() == buttonNo) {
		    // Do nothing
			
		} 
		else{
			return false;
		}
		
		// We can continue. No Errors
		return true;
	}

	public void displayErrorMessage(DescriptionParserException e) {
		errorsTerminal.setText(e.getMessage() + "\n\n\n");
		for(StackTraceElement i : e.getStackTrace()){
			errorsTerminal.appendText(i.toString() + "\n");
		}
		e.printStackTrace();
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setWidth(1024);
		primaryStage.setHeight(800);		
		
		
		BorderPane mainLayout = new BorderPane();
		setupMenu(mainLayout, primaryStage);
		
		GridPane layout = new GridPane();
		layout.setVgap(10);
		layout.setPadding(new Insets(0, 10, 0, 10));
		
		// Code Area
		code = new TextArea();
		code.setPrefWidth(1000000);
		code.setPrefHeight(600);
		code.textProperty().addListener(new CodeListener());
		codeScroll = new ScrollPane(code);
		//BorderedTitledPane codePane = new BorderedTitledPane("Code Pane", code);
		//codePane.setStyle("titled-address");
		codeScroll.setFitToWidth(true);

		
		// Options Area
		GridPane optionsLayout = new GridPane();
		compileButton = new Button("Compile");
		compileButton.setOnAction(new CompileListener());
		
		descriptionIDField = new TextField(EXAMPLE_DESCRIPTIONID);
		descriptionIDField.textProperty().addListener(new IDListener());
		inputField = new TextField();
		inputHelpButton = new Button("?");
		inputHelpButton.setOnAction(new InputHelpButtonListener());
		optionsLayout.add(compileButton,0,0);
		optionsLayout.add(new Text("  ID:  "),1,0);
		optionsLayout.add(descriptionIDField,2,0);
		optionsLayout.add(new Text("  Inputs:  "),3,0);
		optionsLayout.add(inputField,4,0);
		optionsLayout.add(inputHelpButton,5,0);
		
		// Results Area
		results = new Pane();
		results.setPrefHeight(100);
		resultsScroll = new ScrollPane(results);
		resultsScroll.setFitToWidth(true);

		layout.addRow(0,code);
		layout.addRow(1,optionsLayout);
		layout.addRow(2,resultsScroll);
		
		mainLayout.setCenter(layout);
		
		
		//
		// Terminal
		Pane terminal = setupTerminal();	
		mainLayout.setRight(terminal);
		
		
		// Scene
		Scene scene = new Scene(mainLayout);
		primaryStage.setScene(scene);
		primaryStage.show();
		this.primaryStage = primaryStage;
		
		// Assign text to code
		String[] startingValues = getStartingText();
		code.setText(startingValues[2]);
		descriptionIDField.setText(startingValues[0]);
		inputField.setText(startingValues[1]);
		changeTitle();
		compileButton.setFont(isCodeEdited ? CompileButtonNOTEditedFont : CompileButtonEditedFont);
		toggleToolTips();
		importInputHelp();
	}

	/**
	 * Load the help information for the Input Field from a file.
	 */
	private void importInputHelp() {
		
		try {
			Scanner scan = new Scanner(new File("inputhelp.txt"));
			
			INPUT_HELP_DESCRIPTION = "";
			while(scan.hasNextLine()){
				INPUT_HELP_DESCRIPTION += scan.nextLine() + "\n";
				
			}
			
			scan.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	private Pane setupTerminal() {
		BorderPane terminalLayout = new BorderPane();		
		
		// Text Area
		errorsTerminal = new TextArea();
		errorsTerminal.setPrefHeight(100000);
		errorsTerminal.setEditable(false);
		errorsTerminal.setFont(errorTerminalFont);
		ScrollPane errorsScroll = new ScrollPane(errorsTerminal);
		terminalLayout.setCenter(errorsScroll);
		
		// Menu
		MenuBar menubar = new MenuBar();
		Menu options = new Menu("Options");
		MenuItem clear = new MenuItem("Clear");
		clear.setOnAction(new EventHandler<ActionEvent>(){
			public void handle(ActionEvent arg0) { errorsTerminal.setText(""); };
		});
		options.getItems().add(clear);
		menubar.getMenus().add(options);
		terminalLayout.setTop(menubar);
		
		return terminalLayout;
	}

	private String[] getStartingText() {
		String[] start = new String[3]; // Code, ID, Inputs
		
		// If we have local files.
		// Then there are 2 files
		// first contains CODE
		// second contains information about the code PER line
		// 1. DescriptionIE
		// 2. Inputs
		// 3. File destination ( will be the local file if it has not been saved yet )
		
		
		//
		// Check if our local CODE exists
		File localCode = new File(localCodeFileName);
		if( localCode.exists() ){
			start[2] = getTextFromFile(localCode);
		}
		else{
			start[2] = EXAMPLE_CODE; 
		}

		//
		// Local Options
		File localOptions = new File(localOptionsFileName);
		if( localOptions.exists() ){
			String text = getTextFromFile(localOptions);
			String[] options = divideLocalOptions(text);
			
			start[0] = options[0];
			start[1] = options[1];
			currentFile = options[2];
			isCodeEdited = "TRUE".equalsIgnoreCase(options[3]);
		}
		else{
			// Could not file the local options
			start[0] = EXAMPLE_DESCRIPTIONID;
			start[1] = EXAMPLE_INPUTS;
			
			try {
				currentFile = localOptions.getCanonicalPath();
			} catch (IOException e) {e.printStackTrace();}
		}

		return start;
	}

	private void changeTitle() {

		String editedSuffix = (isCodeEdited ? " - *" : "");
		if( currentFile == null ){
			 editedSuffix = (isCodeEdited ? " - *" : "");
		}
		else if( currentFile.equalsIgnoreCase("null") ){
			editedSuffix = (isCodeEdited ? "*" : "");
		}
		else{
			editedSuffix = " - " + currentFile + (isCodeEdited ? "*" : "");
		}

		primaryStage.setTitle(programTitle + editedSuffix);
	}

	/**
	 * Options are seperated by lines
	 * First line is the description
	 * Second line is Inputs
	 * @param text
	 * @return
	 */
	private String[] divideLocalOptions(String text) {
		String[] options = new String[4];
		
		Scanner scan = new Scanner(text);
		while(scan.hasNextLine()){
			String line = scan.nextLine();
			if(line.isEmpty()){
				continue;
			}
			
			if(options[0] == null){
				options[0] = line.equalsIgnoreCase("null") || line.isEmpty() ? "1" : line;
			}
			else if(options[1] == null){
				options[1] = line.equalsIgnoreCase("null") || line.isEmpty() ? "" : line;
			}
			else if(options[2] == null){
				options[2] = line.equalsIgnoreCase("null") || line.isEmpty() ? "" : line;
			}
			else if(options[3] == null){
				options[3] = line.equalsIgnoreCase("null") || line.isEmpty() ? "" : line;
			}
			else{
				scan.close();
				throw new DescriptionParserException("Corrupted Local Options file!");
			}
		}
		scan.close();
		return options;
	}
	
	public void toggleToolTips(){
		for(int i = 0; i < tooltips.size(); i++){			
			Tooltip tooltip = toolTipsEnabled ? tooltips.get(i) : null;
			
			// 0 Code
			// 1 Compile Button
			// 2 ID Field
			// 3 Input Field
			// 4 Help Button
			// 5 Results
			// 6 Error Terminal
			switch(i){
				case 0: code.setTooltip(tooltip); break;
				case 1: compileButton.setTooltip(tooltip); break;
				case 2: descriptionIDField.setTooltip(tooltip); break;
				case 3: inputField.setTooltip(tooltip); break;
				case 4: inputHelpButton.setTooltip(tooltip); break;
				case 5: resultsScroll.setTooltip(tooltip); break;
				case 6: errorsTerminal.setTooltip(tooltip); break;
			}
		}
	}
	
	private class SaveFileListener implements EventHandler<ActionEvent>{
		public void handle(ActionEvent arg0) {
			saveFile();
		}
	}
	
	private class NewFileListener implements EventHandler<ActionEvent>{
		public void handle(ActionEvent arg0) {
			
			if( currentFile == null ){
				saveFile();
			}
			else if( !checkFileIsSaved() ){
				return;
			}
			
			// Nohing being displayed
			results.getChildren().clear();
			
			// No code
			code.setText("");
			
			// Saved file
			currentFile = null;
			needsSaving = false;
			isCodeEdited = false;
			
			// No Errors
			errorsTerminal.clear();
			
			// Chance Title
			changeTitle();
		}		
	}
	
	private class QuitListener implements EventHandler<ActionEvent>{

		public void handle(ActionEvent arg0) {
			if( checkFileIsSaved() ){
				System.exit(0);
			}
		}		
	}
	
	private class CompileListener implements EventHandler<ActionEvent>{

		public void handle(ActionEvent arg0) {
			compileCode();
		}		
	}

	
	private class SaveAsListener implements EventHandler<ActionEvent>{
		public void handle(ActionEvent arg0) {
			JFileChooser chooser = new JFileChooser(".");
			int selected = chooser.showSaveDialog(null);
			if( selected == JFileChooser.CANCEL_OPTION ){
				return;
			}
			
			File file = chooser.getSelectedFile();
			currentFile = file.getAbsolutePath();
			changeTitle();
			saveFile();
		}
	}
	
	private class ToggleTooltipsListener implements EventHandler<ActionEvent>{
		public void handle(ActionEvent arg0) {
			toolTipsEnabled = !toolTipsEnabled;
			toggleToolTips();
		}
	}
	
	private class LoadFileListener implements EventHandler<ActionEvent>{
		public void handle(ActionEvent arg0) {
			JFileChooser chooser = new JFileChooser(".");
			int selected = chooser.showOpenDialog(null);
			if( selected == JFileChooser.CANCEL_OPTION ){
				return;
			}
			
			File file = chooser.getSelectedFile();
			if(file.exists()){
				code.setText(getTextFromFile(file));
				try {
					currentFile = file.getCanonicalPath();
					changeTitle();
				} catch (IOException e) {e.printStackTrace();}
			}
		}
		
	}
	
	private class InputHelpButtonListener implements EventHandler<ActionEvent>{
		public void handle(ActionEvent arg0) {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Input Field Help");
			alert.setHeaderText(" ");
			
			TextArea textArea = new TextArea(INPUT_HELP_DESCRIPTION);
			textArea.setEditable(false);
			textArea.setWrapText(true);
			alert.getDialogPane().setContent(textArea);

			alert.showAndWait();
		}
	}
	
	public class IDListener implements ChangeListener<String> {
		public void changed(ObservableValue<? extends String> arg0,
				String arg1, String arg2) {
			int difference = arg2.length() - arg1.length();
			if( difference > 0 ){
				String newExtension = "";
				
				// Check if we need to edit
				for(int i = arg1.length(); i < arg2.length(); i++){
					char c = arg2.charAt(i);
					if(Character.isDigit(c)){
						newExtension += c;
					}
				}
				
				descriptionIDField.setText(arg1+newExtension);
			}
		}

	}

	
	public class CodeListener implements ChangeListener<String> {

		public void changed(ObservableValue<? extends String> arg0,
				String arg1, String arg2) {
			if( Math.abs(arg1.length()-arg2.length()) > 2 ){
				return;
			}
			
			compileButton.setFont(CompileButtonEditedFont);
			isCodeEdited = true;
			needsSaving = true;
			changeTitle();
		}

	}

	public static void main(String[] args){
		launch(args);
	}
}













