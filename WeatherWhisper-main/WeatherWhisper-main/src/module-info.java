module WeatherWhisper {
	requires java.desktop;
	requires java.net.http;
	requires org.json;
	requires javafx.controls;
	requires javafx.fxml;
	requires javafx.graphics;
	
	opens application to javafx.graphics, javafx.fxml;
}
