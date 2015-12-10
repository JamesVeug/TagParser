package editor;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class FXTest extends Application{
	
	
	public static void main(String[] args){
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setTitle("Title of window");
		
		
		
		GridPane layout = new GridPane();
		layout.setVgap(10);
		layout.setPadding(new Insets(0, 10, 0, 10));
	    
		TextArea code = new TextArea("CODEEEEEEEEEE");
		ScrollPane codeScroll = new ScrollPane(code);
		
		GridPane optionsLayout = new GridPane();
		Button compileButton = new Button("Compile");
		TextField descriptionIDField = new TextField("ID");
		TextField inputField = new TextField("3");
		Button inputHelpButton = new Button("?");
		optionsLayout.add(compileButton,0,0);
		optionsLayout.add(new Text("  ID:  "),1,0);
		optionsLayout.add(descriptionIDField,2,0);
		optionsLayout.add(new Text("  Inputs:  "),3,0);
		optionsLayout.add(inputField,4,0);
		optionsLayout.add(inputHelpButton,5,0);
		
		TextArea results = new TextArea("RESULTS");
		ScrollPane resultsScroll = new ScrollPane(results);
		results.setEditable(false);

		layout.addRow(0,codeScroll);
		layout.addRow(1,optionsLayout);
		layout.addRow(2,resultsScroll);
		
		Scene scene = new Scene(layout, 800, 600);
		primaryStage.setScene(scene);
		primaryStage.show();
		
	}

}
