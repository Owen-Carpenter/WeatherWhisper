package application;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.event.ActionEvent;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.time.ZonedDateTime;
import WeatherWhisper.WeatherDataAPI;

public class FXMLController {
	
	private WeatherDataAPI calledWeather;
	private ZonedDateTime date;
	//private int currentHour;
	//private int viewHour;

	// Tags for injection
	@FXML ArrayList<Text> dayLabel;
	@FXML ArrayList<Text> hour;
	@FXML ArrayList<Text> dayBound;
	@FXML ArrayList<Text> hourFahrenheit;
	@FXML ArrayList<Text> hourCelsius;
	@FXML ArrayList<Text> hourWind;
	@FXML ArrayList<Text> hourPrecip;
	@FXML Label address;
	@FXML ImageView backgroundGif;
	@FXML ArrayList<ImageView> hourSymbolsF;
	@FXML ArrayList<ImageView> hourSymbolsC;
	@FXML Text UVIndex;
	@FXML Text currentTemp;
	@FXML Label weatherDesc;
	@FXML Text compassText;
	@FXML TextField searchBox;
	@FXML Button searchButton;
	@FXML Button hourLeft;
	@FXML Button hourRight;
	@FXML Text uvIndexDisplay;
	@FXML Image arrow = new Image(getClass().getResourceAsStream("compassArrow.png"));
	@FXML ImageView compassArrow = new ImageView(arrow);
	@FXML ImageView uvArrow = new ImageView(arrow);
	@FXML Text moonPhaseDisplay;
	@FXML ImageView moonImg;
	@FXML ArrayList<ImageView> dailySymbols;
	@FXML ArrayList<ImageView> arrows;
	@FXML Text sunsetT;
	@FXML Text sunriseT;
	
	Rotate compassRotation = new Rotate(0);
	Rotate uvRotation = new Rotate(0);
	
	// Initialize all weather data from a given WeatherDataAPI object representing the weather at a specific location and time
	@FXML public void initialize(WeatherDataAPI weather) {
		// Store WeatherDataAPI object and datetime information
		calledWeather = weather;
		date = calledWeather.getCurrentTime();
		//currentHour = calledWeather.getCurrentTime().getHour();
		//viewHour = currentHour;

		// Set actions to buttons 
		searchButton.setOnAction(this::searchHandler);
		searchBox.setOnMouseClicked(this::searchTextHandler);
		//hourLeft.setOnAction(this::shiftHourHandler);
		//hourRight.setOnAction(this::shiftHourHandler);
		
		// Set various graphics, plus sunset/sunrise display times
		setBackground();
		setHourlyWeatherSymbols(hourSymbolsF);
		setHourlyWeatherSymbols(hourSymbolsC);
		setDailySymbols();
		setMoonPhase();
		setWindArrows();
		setSunTimes();
	
		//initialize tags for header info
		address.setText(weather.getResolvedAddress().toString());
		currentTemp.setText(weather.getCurrentTemp().toString() + "°F");
		weatherDesc.setText(weather.getCurrentSkyConditions().toString());
		
		// Initialize tags for UV info, compass info, dates, hours, daily temperature bounds, hourly temperatures in C and F, 
		// hourly wind speeds/directions, and hourly precipitation percentages
		initializeUV();
		compassText.setText("Wind Direction: " + directionToString(weather.getCurrentWindDirection()) + " Speed: " + weather.getCurrentWindSpeed() + " MPH");
		initializeCompassArrow();
		initializeDates();
		initializeHours();
		initializeBounds();
		initializeHourlyFahrenheit();
		initializeHourlyCelsius();
		initializeHourlyWind();
		initializeHourlyPrecip();
	}
	
	// Initialize UV value & UV Arrow
	private void initializeUV() {
		String uvString = calledWeather.getCurrentUVindex().toString();
		uvIndexDisplay.setText(uvString);
		Double uv = Double.parseDouble(uvString);
		double angle = 0;
		
		// Use UV value to determine angle of arrow for display
		if (uv >= 11) angle = 75;
		else if (uv >7 && uv < 11) angle = 40;
		else if (uv >= 6 && uv <= 7) angle = 0;
		else if (uv >2 && uv < 6) angle = -40;
		else if (uv > 0 && uv <= 2) angle = -75;
		else if (uv ==0) angle = -90;
		
		// Create arrow direction
		uvRotation.setPivotX(uvArrow.getBoundsInLocal().getWidth()/2);
		uvRotation.setPivotY(uvArrow.getBoundsInLocal().getHeight());
		uvRotation.setAngle(angle);
		uvArrow.getTransforms().clear();
		uvArrow.getTransforms().add(uvRotation);
	}
	
	// Initializes direction/rotation of arrow
	private void initializeCompassArrow() {
		compassRotation.setPivotX(compassArrow.getBoundsInLocal().getWidth()/2);
		compassRotation.setPivotY(compassArrow.getBoundsInLocal().getHeight());
		compassRotation.setAngle(Double.parseDouble((calledWeather.getCurrentWindDirection()).toString()));
		compassArrow.getTransforms().clear();
		compassArrow.getTransforms().add(compassRotation);
	}
	
	// Generate string based on weather direction
	private String directionToString(Object direction) {
		var dir = Double.parseDouble(direction.toString());
		
		if (dir >= 75 && dir <=105)
			return "E";
		else if (dir > 105 && dir < 165)
			return "SE";
		else if (dir >=165 && dir <= 195)
			return "S";
		else if (dir > 195 && dir < 255)
			return "SW";
		else if (dir >= 255 && dir <= 285)
			return "W";
		else if (dir > 285 && dir < 345)
			return "NW";
		else if ((dir >= 345 && dir <=360) || dir >= 0 && dir <= 15)
			return "N";
		else if (dir > 15 && dir < 75)
			return "NE";
		else return "Problem designating direction...";
	}
	
	// Initializes the dates 
	private void initializeDates() {
		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("MM/dd");
		int iterator = 1;
		
		for (Text text : dayLabel) {
			text.setText(date.plusDays(iterator).getDayOfWeek() + " " + dateFormat.format(date.plusDays(iterator)));
			iterator++;
		}
	}
	
	// Initialize hours
	private void initializeHours() {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h a");
		int iterator = 1;
		
		for (Text text : hour)
			text.setText(date.plusHours(iterator++).format(formatter));
	}
	
	//initialize daily bounds
	private void initializeBounds() {
		int iterator = 0;
		
		for (Text text : dayBound) {
			text.setText((calledWeather.getDailyMaxTemps().get(iterator) + "°F/" + calledWeather.getDailyMinTemps().get(iterator) + "°F"));
			iterator++;
		}
	}
	
	//initialize hourly farenheit temps
	private void initializeHourlyFahrenheit() {
		int iterator = 0;
		
		for (Text text : hourFahrenheit) {
			text.setText((calledWeather.getHourlyTemps().get(iterator++)).toString()+"F");
		}
	}
	
	//initialize hourly celsius temps
	private void initializeHourlyCelsius() {
		int iterator = 0;
		
		for (Text text : hourCelsius)
			text.setText(toCelsius(calledWeather.getHourlyTemps().get(iterator++)));
	}
	
	//initalize hourly wind speeds
	private void initializeHourlyWind() {
		int iterator = 0;
		
		for (Text text : hourWind)
			text.setText(calledWeather.getHourlyWindSpeeds().get(iterator++) + "MPH");
	}
	
	//initalize hourly precipitation chance
	private void initializeHourlyPrecip() {
		int iterator = 0;
		
		for (Text text : hourPrecip)
			text.setText(calledWeather.getHourlyPrecipProbs().get(iterator++) + "%");
	}
	
	//convert farenheit to celsius
	private String toCelsius(Object fahrenheit) {
		DecimalFormat dFormat = new DecimalFormat("#.#");
		return (dFormat.format((Double.parseDouble(fahrenheit.toString())-32)*5/9))+"°C";
	}
	
	// Reloads hours when time is shifted 
	// Unimplemented, ended up being unnecessary. Remains commented out for future, post-semester experimentation.
	/*
	private void shiftHourHandler(ActionEvent e) {
		if (e.getSource().equals(hourLeft)) {
			if (!(viewHour == currentHour)){viewHour--;}
		}
		else if (e.getSource().equals(hourRight)) {
			if (!(viewHour == currentHour+7)) {viewHour++;}
		}
	}*/
	
	//Clears textbox when clicked
	private void searchTextHandler(MouseEvent e) {
		searchBox.setText("");
	}
	
	//generates new JFX based on search action
	private void searchHandler(ActionEvent e) {
		String loc = searchBox.getText();
		WeatherDataAPI searchLoc = new WeatherDataAPI(loc);
		
		// Only initialize location searched by user if API recognized location and returned valid JSON
		if(searchLoc.isValid())
			initialize(searchLoc);
		// else indicate invalid location to user somehow perhaps?
		else searchBox.setText("Invalid Location, Try again");
	}
	
	//Set the background to the correct correlating sky condition
	private void setBackground() {
	    Object currConditions = calledWeather.getCurrentSkyConditions();

	    if (currConditions != null) {
	        String condition = currConditions.toString();

	        //Map of sky condition output --> correlating gif/jpg
	        Map<String, String> conditionImageMap = Map.of(
	                "Clear", "sunny.jpg",
	                "Rain", "rainy.gif",
	                "Partially cloudy", "partlyCloudy.gif",
	                "Overcast", "cloudy.gif",
	                "Rain, Partially cloudy", "rainy.gif",
	                "Rain, Overcast", "rainy.gif",
	                "Snow, Overcast", "snowy.gif",
	                "Snow, Partially cloudy", "snowy.gif",
	                "Snow", "snowy.gif"
	        );
	        
	        Map<String,String> conditionNightMap = Map.of(
	        		
	        		"Clear", "nightClear.gif",
	        		"Overcast", "cloudyNight.gif",
	        		"Partially cloudy", "partlyCloudyNight.gif",	
	        		"Snow", "snowyNight.gif"
    		);
	        
	        ZonedDateTime sunsetTime = calledWeather.getSunsetTime();
	        ZonedDateTime sunriseTime = calledWeather.getSunriseTime();
	        ZonedDateTime currentTime = calledWeather.getCurrentTime();
	        
	        if (conditionNightMap.containsKey(condition) && ((currentTime.isAfter(sunsetTime) || currentTime.isBefore(sunriseTime)))) {
		        String imagePath = getClass().getResource(conditionNightMap.get(condition)).toExternalForm();
	            Image image = new Image(imagePath);
	            backgroundGif.setImage(image);
	    	}
	        // Check if a key is true and set the correct background image
	        else if (conditionImageMap.containsKey(condition)) {
	            String imagePath = getClass().getResource(conditionImageMap.get(condition)).toExternalForm();
	            Image image = new Image(imagePath);
	            backgroundGif.setImage(image);
	            
	        } 
	        
	        else System.out.println("Condition Not Found Error!");
	    }
	    
	}	
	
	private void setHourlyWeatherSymbols(List<ImageView> hourSymbols) {
		//Map of sky condition output --> correlating symbols
	    Map<String, String> conditionImageMap = Map.of(
	            "Clear", "Sunny.png",
	            "Rain", "Raining.png",
	            "Partially cloudy", "PartlyCloudy.png",
	            "Overcast", "Cloudy.png",
	            "Rain, Partially cloudy", "Raining.png",
	            "Rain, Overcast", "Raining.png",
	            "Snow, Overcast", "snowy.png",
	            "Snow, Partially cloudy", "snowy.png",
	            "Snow", "snowy.png"
	    );
	    Map<String, String> nightImageMap = Map.of(
	    		"Clear", "Night.png",
	    		"Partially cloudy", "partlyCloudyNight.png"
	    );
	    
	    ZonedDateTime sunsetTime = calledWeather.getSunsetTime();
        ZonedDateTime sunriseTime = calledWeather.getSunriseTime();
        ZonedDateTime currentTime = calledWeather.getCurrentTime();
        
	    int iterator = 0;
	    //Loop for setting the images
	    for (ImageView iv : hourSymbols) {
	        Object current = calledWeather.getHourlySkyConditions().get(iterator++);
	        String condition = current.toString();
	        // Check if a key is true and set the correct symbol
	        if(nightImageMap.containsKey(condition) && ((currentTime.isAfter(sunsetTime) || currentTime.isBefore(sunriseTime)))) {
	        	String imagePath = getClass().getResource(nightImageMap.get(condition)).toExternalForm();
	            Image image = new Image(imagePath);
	            iv.setImage(image);
	        }
	        else if (conditionImageMap.containsKey(condition)) {
	            String imagePath = getClass().getResource(conditionImageMap.get(condition)).toExternalForm();
	            Image image = new Image(imagePath);
	            iv.setImage(image);
	        }
	    }
	}
	
	private void setMoonPhase() {
		Object currConditions = calledWeather.getMoonPhase();
		
		if (currConditions != null) {
	        String condition = currConditions.toString();

	        //Map of sky condition output --> correlating gif/jpg
	        Map<String, String> conditionImageMap = Map.of(
	                "New Moon", "newMoon.png",
	                "Waxing Crescent", "waxingCrescent.png",
	                "First Quarter", "firstQuarter.png",
	                "Waxing Gibbous", "waxingGibbous.png",
	                "Full Moon", "fullMoon.png",
	                "Waning Gibbous", "waningGibbous.png",
	                "Last Quarter", "thirdQuarter.png",
	                "Waning Crescent", "waningCrescent.png"
	        );

	        // Check if a key is true and set the correct background image
	        if (conditionImageMap.containsKey(condition)) {
	            String imagePath = getClass().getResource(conditionImageMap.get(condition)).toExternalForm();
	            Image image = new Image(imagePath);
	            moonImg.setImage(image);
	            moonPhaseDisplay.setText(calledWeather.getMoonPhase().toString());
	        } else {
	            System.out.println("Condition Not Found Error!");
	        }
		}
	}
	
	private void setDailySymbols() {
		//Map of sky condition output --> correlating symbols
	    Map<String, String> conditionImageMap = Map.of(
	            "Clear", "Sunny.png",
	            "Rain", "Raining.png",
	            "Partially cloudy", "PartlyCloudy.png",
	            "Overcast", "Cloudy.png",
	            "Rain, Partially cloudy", "Raining.png",
	            "Rain, Overcast", "Raining.png",
	            "Snow, Overcast", "snowy.png",
	            "Snow, Partially cloudy", "snowy.png",
	            "Snow", "snowy.png"
	    );
	    
	    int iterator = 0;
	    
	    for(ImageView iv : dailySymbols) {
	    	Object current = calledWeather.getDailySkyConditions().get(iterator++);
	        String condition = current.toString();
	        
	        if (conditionImageMap.containsKey(condition)) {
	            String imagePath = getClass().getResource(conditionImageMap.get(condition)).toExternalForm();
	            Image image = new Image(imagePath);
	            iv.setImage(image);
	        }
	    }
	}
	
	private void setWindArrows() {
		List<Object> windDirectionsList = calledWeather.getHourlyWindDirections();

		//Get the image arrow.png
		String imagePath = getClass().getResource("arrow.png").toExternalForm();
		Image arrowImage = new Image(imagePath);

		int iterator = 0;

		for (Object windDirectionObject : windDirectionsList) {
		    
			//Parse for wind direction angle
		    double windDirection = Double.parseDouble(windDirectionObject.toString());
		    
		    //Set the image and i++
		    if (iterator < arrows.size()) {
		        ImageView iv = arrows.get(iterator);
		        iv.setImage(arrowImage);
		        iv.setRotate(windDirection); // Set the rotation angle based on the wind direction

		        iterator++;
		    }
		}
	}
	
	private void setSunTimes() {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm a");
		
		// Set display for sunset time
		ZonedDateTime sunsetTime = calledWeather.getSunsetTime();
		String formattedSunsetTime = sunsetTime.format(formatter);
		sunsetT.setText(formattedSunsetTime);
		
		// Set display for sunrise time
		ZonedDateTime sunriseTime = calledWeather.getSunriseTime();
		String formattedSunriseTime = sunriseTime.format(formatter);
		sunriseT.setText(formattedSunriseTime);
	}
	
}
