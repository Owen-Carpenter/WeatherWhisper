package application;
	
import WeatherWhisper.WeatherDataAPI;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class Main extends Application {
	
	@Override
	public void start(Stage primaryStage) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("Main.fxml"));
			Parent root = loader.load();
			FXMLController controller = loader.getController();
			WeatherDataAPI weather = new WeatherDataAPI("Conway Ar");
			controller.initialize(weather);
			Scene scene = new Scene(root,1000,540);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm()); 
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}
