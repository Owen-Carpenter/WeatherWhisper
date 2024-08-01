package WeatherWhisper;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import org.json.*;

// Stores all of the Weather Data (used in the WeatherWhisper application) for a single location
public class WeatherDataAPI {
	
	private String inputLocation;
	private JSONObject weatherDataJSON;
	private Boolean validAddress;
	private Object resolvedAddress;
	private ZonedDateTime currentTime;
	private ZonedDateTime sunriseTime;
	private ZonedDateTime sunsetTime;
	private String moonPhase;
	private Object currentTemp;
	private Object currentSkyConditions;
	private Object currentWindSpeed;
	private Object currentWindDirection;
	private Object currentUVindex;
	private ArrayList<Object> hourlyTemps;
	private ArrayList<Object> hourlySkyConditions;
	private ArrayList<Object> hourlyWindSpeeds;
	private ArrayList<Object> hourlyWindDirections;
	private ArrayList<Object> hourlyPrecipProbs;
	private ArrayList<Object> hourlyHumidities;
	private ArrayList<Object> dailyMaxTemps;
	private ArrayList<Object> dailyMinTemps;
	private ArrayList<Object> dailySkyConditions;
	
	// Constructor that takes String containing location name (i.e., API has not already been called -> constructor must call API)
	public WeatherDataAPI (String inputLocation) {
		
		this.inputLocation = inputLocation;
		
		// Initialize ArrayList fields
		hourlyTemps = new ArrayList<Object>();
		hourlySkyConditions = new ArrayList<Object>();
		hourlyWindSpeeds = new ArrayList<Object>();
		hourlyWindDirections = new ArrayList<Object>();
		hourlyPrecipProbs = new ArrayList<Object>();
		hourlyHumidities = new ArrayList<Object>();
		dailyMaxTemps = new ArrayList<Object>();
		dailyMinTemps = new ArrayList<Object>();
		dailySkyConditions = new ArrayList<Object>();
		
		// Make initial API call to fetch latest JSON weather data and extract desired weather measurements
		updateWeatherData();
	}
	
	// Re-calls the Visual Crossing API w/ the same inputLocation String to update weather data (can also be used for initial API call)
	public void updateWeatherData() {
		try {
			// Retrieve new JSONObject by calling API with original input for location
			weatherDataJSON = WeatherService.fetchWeatherData(inputLocation);
			
			// Extract all desired data from JSONObject
			setValidity();
			if(isValid()) {
				setResolvedAddress();
				setCurrentTime();
				setSunriseTime();
				setSunsetTime();
				setMoonPhase();
				setCurrentTemp();
				setCurrentSkyConditions();
				setCurrentWindSpeed();
				setCurrentWindDirection();
				setCurrentUVindex();
				setHourlyTemps();
				setHourlySkyConditions();
				setHourlyWindSpeeds();
				setHourlyWindDirections();
				setHourlyPrecipProbs();
				setHourlyHumidities();
				setDailyMaxTemps();
				setDailyMinTemps();
				setDailySkyConditions();
			}
			
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	// Determine if JSONObject represents valid location (i.e., that the API request wasn't bad) based on value at "validAddress" key
	// Note: validAddress is a key we create in WeatherService.java and is not in the original JSON sent by the Visual Crossing API
	private void setValidity() {
		validAddress = (Boolean) weatherDataJSON.get("validAddress");
	}
	
	public Boolean isValid() {
		return validAddress;
	}
	
	// Store resolvedAddress determined by Visual Crossing API (e.g., API itself resolves "Conway,,, Arkansas" to "Conway, AR, United States")
	private void setResolvedAddress() {
		resolvedAddress = weatherDataJSON.get("resolvedAddress");
	}
	
	public Object getResolvedAddress() {
		return resolvedAddress;
	}
	
	// Stores the datetime at the resolvedAddress by converting from the system time to the address' time via timezones. We do not use
	// time values provided by API because they are only precise to 15 minutes (and are not even accurate in that regard; it could be 
	// 3:08pm and the latest JSON might still store 2:45pm, for example).
	private void setCurrentTime() {
		// Determine the timezone for the resolvedAddress location
		ZoneId locationZone = ZoneId.of(weatherDataJSON.get("timezone").toString());
		
		// Determine system's local time and timezone
		LocalDateTime systemTime = LocalDateTime.now();
		ZoneId systemZone = ZoneId.systemDefault();
		
		// Knowing both location's timezones, convert the system's local time to the resolvedAddress' local time
		currentTime = ZonedDateTime.of(systemTime, systemZone).withZoneSameInstant(locationZone);
	}

	public ZonedDateTime getCurrentTime() {
		return currentTime;
	}
	
	// For either a sunrise or a sunset (specified by keyInJSON), generates a ZonedDateTime object for the time of that twilight at the location.
	// Used as a helper function to standardize sunSunriseTime() and setSunSetTime() -- "twilight" used to refer to either a sunset or a sunrise
	private ZonedDateTime twilightHelper(String keyInJSON) {
		
		String locTimeZone = weatherDataJSON.get("timezone").toString();
		
		// Create string containing twilight datetime information in format readable by ZonedDateTime parser
		String twilightStr = weatherDataJSON.getJSONArray("days").getJSONObject(0).get("datetime").toString(); 		//e.g., =  "2023-12-11" (date)
		twilightStr += "T" + weatherDataJSON.getJSONObject("currentConditions").getString(keyInJSON).toString(); 	//e.g., += "T07:07:50"	(time)
		twilightStr += ZoneId.of(locTimeZone).getRules().getOffset(LocalDateTime.now());							//e.g., += "-06:00" (UTC offset)
		twilightStr += "[" + locTimeZone + "]";																		//e.g., += "[America/Chicago] ([timezone])
		
		// Return sunset/sunrise time by parsing twilightTime w/ ZonedDateTime parser
		return ZonedDateTime.parse(twilightStr);
	}
	
	// Sets the sunriseTime field according to the value for the "sunrise" key in the JSON
	private void setSunriseTime() {
		sunriseTime = twilightHelper("sunrise");
	}
	
	public ZonedDateTime getSunriseTime() {
		return sunriseTime;
	}
	
	// Sets the sunsetTime field according to the value for the "sunrise" key in the JSON
	private void setSunsetTime() {
		sunsetTime = twilightHelper("sunset");
	}
	
	public ZonedDateTime getSunsetTime() {
		return sunsetTime;
	}
	
	// Sets today's moon phase according to the value for the "moonphase" key in the "currentConditions" JSONObject
	private void setMoonPhase() {
		BigDecimal moonPhaseJSON = (BigDecimal) weatherDataJSON.getJSONObject("currentConditions").get("moonphase");
		double moonPhaseValue = moonPhaseJSON.doubleValue();
		
		// Derive name for current moon phase from decimal value stored at "moonphase" in jJSON
		if(moonPhaseValue < 0 || moonPhaseValue > 1)
			moonPhase = "invalid moonphase value";
		else if(moonPhaseValue == 0)
			moonPhase = "New Moon";
		else if(moonPhaseValue < 0.25)
			moonPhase = "Waxing Crescent";
		else if(moonPhaseValue == 0.25)
			moonPhase = "First Quarter";
		else if(moonPhaseValue < 0.5)
			moonPhase = "Waxing Gibbous";
		else if(moonPhaseValue == 0.5)
			moonPhase = "Full Moon";
		else if(moonPhaseValue < 0.75)
			moonPhase = "Waning Gibbous";
		else if(moonPhaseValue == 0.75)
			moonPhase = "Last Quarter";
		else if(moonPhaseValue <= 1)
			moonPhase = "Waning Crescent";
		else
			moonPhase = "invalid moonphase value"; //should be unreachable
	}
	
	public String getMoonPhase() {
		return moonPhase;
	}
	
	// Sets currentTemp according to the value for the "temp" key in the "currentConditions" JSONObject
	private void setCurrentTemp() {
		currentTemp = weatherDataJSON.getJSONObject("currentConditions").get("temp");
	}
	
	public Object getCurrentTemp() {
		return currentTemp;
	}
	
	// Sets currentSkyConditions according to the value for the "conditions" key in the "currentConditions" JSONObject
	private void setCurrentSkyConditions() {
		currentSkyConditions = weatherDataJSON.getJSONObject("currentConditions").get("conditions");
	}
	
	public Object getCurrentSkyConditions() {
		return currentSkyConditions;
	}
	
	// Sets currentWindSpeed according to the value for the "windspeed" key in the "currentConditions" JSONObject
	private void setCurrentWindSpeed() {
		currentWindSpeed = weatherDataJSON.getJSONObject("currentConditions").get("windspeed");
	}
	
	public Object getCurrentWindSpeed() {
		return currentWindSpeed;
	}
	
	// Sets currentWindDirection according to the value for the "winddir" key in the "currentConditions" JSONObject
	private void setCurrentWindDirection() {
		currentWindDirection = weatherDataJSON.getJSONObject("currentConditions").get("winddir");
	}
	
	public Object getCurrentWindDirection() {
		return currentWindDirection;
	}
	
	// Sets currentUVindex according to the value for the "uvindex" key in the "currentConditions" JSONObject
	private void setCurrentUVindex() {
		currentUVindex = weatherDataJSON.getJSONObject("currentConditions").get("uvindex");
	}
	
	public Object getCurrentUVindex() {
		return currentUVindex;
	}
	
	// For a given weather measurement (specified by keyInJSON), generates an ArrayList of hourly values for that weather measurement
	// for the next 24 hours. Index 0 of the ArrayList represents the next hour (i.e., the weather measurement at 5pm if it is 4:12pm). 
	// Used as a helper function to standardize setHourly_() methods.
	private ArrayList<Object> hourlyHelper(String keyInJSON) {
		
		ArrayList<Object> hourlyData = new ArrayList<Object>();
		
		// Starting at currentHour + 1, append today's (i.e., day 0) remaining hourly data values (i.e., until !(hour < 24)) for the given keyInJSON
		int currentHour = getCurrentTime().getHour();
		for(int hour = currentHour + 1; hour < 24; hour++) {
			hourlyData.add(weatherDataJSON.getJSONArray("days").getJSONObject(0).getJSONArray("hours").getJSONObject(hour).get(keyInJSON));
		}
		
		// Starting at hour 0 (i.e. midnight), append tomorrow's (i.e., day 1) hourly data values until 24 total values have been stored
		for(int hour = 0; hour <= currentHour; hour++) {
			hourlyData.add(weatherDataJSON.getJSONArray("days").getJSONObject(1).getJSONArray("hours").getJSONObject(hour).get(keyInJSON));
		}
		
		return hourlyData;
	}
	
	// Stores the next 24 hours of hourlyTemps according to the hourly values for the "temp" key in the JSON
	private void setHourlyTemps() {
		hourlyTemps = hourlyHelper("temp");
	}
	
	public ArrayList<Object> getHourlyTemps() {
		return hourlyTemps;
	}
	
	// Stores the next 24 hours of hourly sky conditions according to the hourly values for the "conditions" key in the JSON
	private void setHourlySkyConditions() {
		hourlySkyConditions = hourlyHelper("conditions");
	}
	
	public ArrayList<Object> getHourlySkyConditions() {
		return hourlySkyConditions;
	}	
	
	// Stores the next 24 hours of hourlyWindSpeeds according to the hourly values for the "windspeed" key in the JSON
	private void setHourlyWindSpeeds() {
		hourlyWindSpeeds = hourlyHelper("windspeed");
	}
	
	public ArrayList<Object> getHourlyWindSpeeds() {
		return hourlyWindSpeeds;
	}
	
	// Stores the next 24 hours of hourlyWindDirections according to the hourly values for the "winddir" key in the JSON
	private void setHourlyWindDirections() {
		hourlyWindDirections = hourlyHelper("winddir");
	}
	
	public ArrayList<Object> getHourlyWindDirections() {
		return hourlyWindDirections;
	}
	
	// Stores the next 24 hours of hourly precipitation probabilities according to the hourly values for the "precipprob" key in the JSON
	private void setHourlyPrecipProbs() {
		hourlyPrecipProbs = hourlyHelper("precipprob");
	}
	
	public ArrayList<Object> getHourlyPrecipProbs() {
		return hourlyPrecipProbs;
	}
	
	// Stores the next 24 hours of hourly humidities according to the hourly values for the "humidity" key in the JSON
	private void setHourlyHumidities() {
		hourlyHumidities = hourlyHelper("humidity");
	}
	
	public ArrayList<Object> getHourlyHumidities() {
		return hourlyHumidities;
	}
	
	// For a given weather measurement (specified by keyInJSON), generates an ArrayList of daily values for that weather measurement
	// for the next 15 days. Index 0 of the ArrayList represents today's value for that weather measurement. 
	// Used as a helper function to standardize setDaily_() methods.
	private ArrayList<Object> dailyHelper(String keyInJSON) {
		
		ArrayList<Object> dailyData = new ArrayList<Object>();
		
		for(int day = 0; day < 15; day++) {
			dailyData.add(weatherDataJSON.getJSONArray("days").getJSONObject(day).get(keyInJSON));
		}
		
		return dailyData;
	}
	
	// Stores 15 days (including today at index 0) of daily max/high temperatures according to the daily values for the "tempmax" key in the JSON
	private void setDailyMaxTemps() {
		dailyMaxTemps = dailyHelper("tempmax");
	}
	
	public ArrayList<Object> getDailyMaxTemps() {
		return dailyMaxTemps;
	}
	
	// Stores 15 days (including today at index 0) of daily min/low temperatures according to the daily values for the "tempmin" key in the JSON
	private void setDailyMinTemps() {
		dailyMinTemps = dailyHelper("tempmin");
	}
	
	public ArrayList<Object> getDailyMinTemps() {
		return dailyMinTemps;
	}
	
	// Stores 15 days (including today at index 0) of daily sky conditions according to the daily values for the "conditions" key in the JSON
	private void setDailySkyConditions() {
		dailySkyConditions = dailyHelper("conditions");
	}
	
	public ArrayList<Object> getDailySkyConditions() {
		return dailySkyConditions;
	}
	
}
