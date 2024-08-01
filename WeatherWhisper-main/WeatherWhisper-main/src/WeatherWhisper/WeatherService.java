package WeatherWhisper;

import java.io.IOException;
import java.net.*;
import java.net.http.*;
import org.json.*;

// Interacts with Visual Crossing Weather API to retrieve weather data
public class WeatherService {
	
	private final static String API_KEY = "MXA6LAQYMEUM2DN23ZZ6GNH8U";
	
	// Takes as input a String containing a location name, and returns JSONObject containing weather data for that city
	public static JSONObject fetchWeatherData(String inputLocation) throws IOException, InterruptedException {
		
		// Convert inputLocation into a valid string to be placed inside URI
		String URILocation = "";
		// Replace spaces and commas in inputLocation with appropriate formatting for API call
		for(int ch = 0; ch < inputLocation.length(); ch++) {
			if(inputLocation.charAt(ch) == ' ')			// Replace spaces with "%20"
				URILocation += "%20";
			else if(inputLocation.charAt(ch) == ',')	// Replace commas with "%2C"
				URILocation += "%2C";
			else										// All other characters remain the same
				URILocation += inputLocation.charAt(ch);
		}
		
		// Build URI to represent the API endpoint / location of web resource
		String identifier = "https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline/"
				+ URILocation + "?unitGroup=us&key=" + API_KEY + "&contentType=json";
	
		// Call Visual Crossing API via HttpRequest (based on code snippet provide by Visual Crossing)
		HttpRequest request = HttpRequest.newBuilder()
			.uri(URI.create(identifier))
			.method("GET", HttpRequest.BodyPublishers.noBody()).build();
		HttpResponse<?> response = HttpClient.newHttpClient()
			.send(request, HttpResponse.BodyHandlers.ofString());
		String responseText = response.body().toString();
		
		//Return results as a JSONObject
		JSONObject weatherData;
		
		// Create a validAddress key based on whether input location generated Bad API Request or not
		if(responseText.equals("Bad API Request:Invalid location parameter value.") || responseText.equals("Bad API Request:A location must be specified")) {
			weatherData = new JSONObject();
			weatherData.put("validAddress", false);
		}
		else {
			weatherData = new JSONObject(responseText);
			weatherData.put("validAddress", true);
		}
		
		return weatherData;
		
	}
	
}
