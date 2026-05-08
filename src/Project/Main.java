package Project;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;
import javax.swing.JOptionPane;

public class Main {

	// using arraylists because the csv file can change in size
	static ArrayList<String> fromStops = new ArrayList<String>();
	static ArrayList<String> toStops = new ArrayList<String>();
	static ArrayList<String> lineColours = new ArrayList<String>();
	static ArrayList<Double> journeyLength = new ArrayList<Double>();


	static ArrayList<String> stationNames = new ArrayList<String>();
	static ArrayList<String> stationLines = new ArrayList<String>();
	static ArrayList<Double> bestPossibleTime = new ArrayList<Double>();
	static ArrayList<Integer> totalChanges = new ArrayList<Integer>();
	static ArrayList<Boolean> checkedStations = new ArrayList<Boolean>();
	static ArrayList<Integer> previousStation = new ArrayList<Integer>();

	// a big number used before a proper route is found
	static final double LONG_TIME = 999999.0;
	static final int LOTS_OF_CHANGES = 999999;

	public static void loadCsvFile() {

	    // keeps track of the current colour of the line
	    String currentLineColour = "";

	    try {

	        // reads from the csv file
	        Scanner inputFile = new Scanner(new File("Metrolink_times_linecolour(in).csv"));
	        
	        // skips the first line because they are headings
	        if (inputFile.hasNextLine()) {
	            inputFile.nextLine();
	        }

	        // goes through the file line by line
	        while (inputFile.hasNextLine()) {

	            String row = inputFile.nextLine();

	            // splits the line into different parts
	            String[] bits = row.split(",");

	            if (bits.length >= 1) {

	                String firstValue = bits[0].trim();

	                // checks if the row is probably just a colour
	                if (bits.length < 3) {

	                	
	                    currentLineColour = firstValue;

	                } else {

	                    String stopA = bits[0].trim();
	                    String stopB = bits[1].trim();
	                    String minutesText = bits[2].trim();

	                    // some rows have the colour in the first column
	                    if (stopB.equals("") || minutesText.equals("")) {

	                        currentLineColour = stopA;

	                    } else {

	                        double mins = Double.parseDouble(minutesText);

	                        // stores the main connection
	                        fromStops.add(stopA);
	                        toStops.add(stopB);
	                        lineColours.add(currentLineColour);
	                        journeyLength.add(mins);

	                        // adds it backwards as trains can go both ways
	                        fromStops.add(stopB);
	                        toStops.add(stopA);
	                        lineColours.add(currentLineColour);
	                        journeyLength.add(mins);
	                    }
	                }
	            }
	        }

	        inputFile.close();

	    } catch (Exception e) {

	        // shows this if the file does not work
	        JOptionPane.showMessageDialog(null, "Could not read the CSV file.");
	        System.exit(0);
	    }
	}
	public static boolean stationExists(String stationName) {

	    // stops it breaking if the box is closed
	    if (stationName == null) {
	        return false;
	    }

	    // checks the first station list
	    for (int i = 0; i < fromStops.size(); i++) {
	        if (fromStops.get(i).equalsIgnoreCase(stationName)) {
	            return true;
	        }
	    }

	    // checks the other station list
	    for (int i = 0; i < toStops.size(); i++) {
	        if (toStops.get(i).equalsIgnoreCase(stationName)) {
	            return true;
	        }
	    }

	    return false;
	}

	public static int findStationLineOption(String stationName, String lineName) {

	    // finds a station with a certain line colour
	    for (int i = 0; i < stationNames.size(); i++) {

	        if (stationNames.get(i).equalsIgnoreCase(stationName)
	                && stationLines.get(i).equalsIgnoreCase(lineName)) {

	            return i;
	        }
	    }

	    return -1;
	}

	public static void addStationLineOption(String stationName, String lineName) {

	    // avoids adding the same station and line twice
	    if (findStationLineOption(stationName, lineName) == -1) {

	        stationNames.add(stationName);
	        stationLines.add(lineName);
	        bestPossibleTime.add(LONG_TIME);
	        totalChanges.add(LOTS_OF_CHANGES);
	        checkedStations.add(false);
	        previousStation.add(-1);
	    }
	}

	public static void setupStationOptions() {

	    // adds stations with their line colours
	    for (int i = 0; i < fromStops.size(); i++) {
	        addStationLineOption(fromStops.get(i), lineColours.get(i));
	        addStationLineOption(toStops.get(i), lineColours.get(i));
	    }
	}

	public static boolean isBetterRoute(String routeChoice, double newTime, int newChanges, int testPosition) {

	    // compares by time if that was the option selected
	    if (routeChoice.equals("time")) {

	        if (newTime < bestPossibleTime.get(testPosition)) {
	            return true;
	        }

	    } else {

	        // compares by changes first
	        if (newChanges < totalChanges.get(testPosition)) {
	            return true;
	        }

	        // if changes are the same then time is used
	        if (newChanges == totalChanges.get(testPosition)
	                && newTime < bestPossibleTime.get(testPosition)) {

	            return true;
	        }
	    }

	    return false;
	}

	public static boolean isBetterEnding(String routeChoice, int newPosition, int previousPosition) {

	    // checks which ending option is better
	    if (routeChoice.equals("time")) {

	        if (bestPossibleTime.get(newPosition) < bestPossibleTime.get(previousPosition)) {
	            return true;
	        }

	    } else {

	        if (totalChanges.get(newPosition) < totalChanges.get(previousPosition)) {
	            return true;
	        }

	        if (totalChanges.get(newPosition).equals(totalChanges.get(previousPosition))
	                && bestPossibleTime.get(newPosition) < bestPossibleTime.get(previousPosition)) {

	            return true;
	        }
	    }

	    return false;
	}

	public static int getNextStationToCheck(String routeChoice) {

	    int bestSoFar = -1;
	    
	    // looks for the next option to check
	    for (int i = 0; i < stationNames.size(); i++) {

	        if (checkedStations.get(i) == false) {

	            if (bestSoFar == -1) {
	                bestSoFar = i;

	            } else {

	                // uses time if the shortest time was chosen
	                if (routeChoice.equals("time")) {

	                   if (bestPossibleTime.get(i) < bestPossibleTime.get(bestSoFar)) {
	                        bestSoFar = i;
	                    }

	                } else {

	                    // uses changes if fewest changes was chosen
	                    if (totalChanges.get(i) < totalChanges.get(bestSoFar)) {
	                        bestSoFar = i;
	                    }

	                    if (totalChanges.get(i).equals(totalChanges.get(bestSoFar))
	                            && bestPossibleTime.get(i) < bestPossibleTime.get(bestSoFar)) {

	                        bestSoFar = i;
	                    }
	                }
	            }
	        }
	    }

	    // means there is no useful route left
	    if (bestSoFar != -1 && bestPossibleTime.get(bestSoFar) == LONG_TIME) {
	        return -1;
	    }

	    return bestSoFar;
	}

	public static void displayRoute(int endPosition, String routeChoice) {

	    // stores the route backwards first
	    ArrayList<Integer> backwardsRoute = new ArrayList<Integer>();

	    int currentPlace = endPosition;

	    // follows the previous stops back to the start
	    while (currentPlace != -1) {
	        backwardsRoute.add(currentPlace);
	        currentPlace = previousStation.get(currentPlace);
	    }

	    String output = "";

	    // adds the title for the route
	    if (routeChoice.equals("time")) {
	        output = output + "*** Route with Shortest Time ***\n";
	    } else {
	        output = output + "*** Route with Fewest Changes ***\n";
	    }

	    // gets the first station because the route is backwards
	    int startPosition = backwardsRoute.get(backwardsRoute.size() - 1);

	    // tells the user which line to get on first
	    output = output + "You need to get on the "
	            + stationLines.get(startPosition)
	            + " line at "
	            + stationNames.get(startPosition)
	            + "\n\n";

	    // prints the route the right way round
	    for (int i = backwardsRoute.size() - 1; i >= 0; i--) {

	        int routePosition = backwardsRoute.get(i);

	        output = output + stationNames.get(routePosition) + " on "
	                + stationLines.get(routePosition) + " line\n";

	        if (i > 0) {

	            int nextRoutePosition = backwardsRoute.get(i - 1);

	            String thisLine = stationLines.get(routePosition);
	            String nextLine = stationLines.get(nextRoutePosition);

	            // checks if there is a line change
	            if (!thisLine.equalsIgnoreCase(nextLine)) {

	                output = output + "\n*** Change Line to "
	                        + nextLine
	                        + " line ***\n";
	            }
	        }
	    }

	    // adds final time and number of changes
	    output = output + "\nTime (mins): " + bestPossibleTime.get(endPosition) + "\n";
	    output = output + "Total Changes: " + totalChanges.get(endPosition);

	    JOptionPane.showMessageDialog(null, output);
	    // closes after the final box
	    System.exit(0);
	}

	public static void findRoute(String beginningStation, String endingStation, String routeChoice) {

	    // sets up the station and line options
	    setupStationOptions();

	    // resets the route value
	    for (int i = 0; i < stationNames.size(); i++) {

	    	bestPossibleTime.set(i, LONG_TIME);
	        totalChanges.set(i, LOTS_OF_CHANGES);
	        checkedStations.set(i, false);
	        previousStation.set(i, -1);
	    }

	    // start station begins with a 0
	    for (int i = 0; i < stationNames.size(); i++) {

	        if (stationNames.get(i).equalsIgnoreCase(beginningStation)) {
	        	bestPossibleTime.set(i, 0.0);
	            totalChanges.set(i, 0);
	        }
	    }

	    // main route checking loop
	    for (int counter = 0; counter < stationNames.size(); counter++) {
	        int currentPosition = getNextStationToCheck(routeChoice);

	        if (currentPosition == -1) {
	            break;
	        }

	        checkedStations.set(currentPosition, true);

	        String station = stationNames.get(currentPosition);
	        String currentLine = stationLines.get(currentPosition);

	        // checks each connection from the file
	        for (int i = 0; i < fromStops.size(); i++) {

	            if (fromStops.get(i).equalsIgnoreCase(station)) {

	                String nextStation = toStops.get(i);
	                String nextLine = lineColours.get(i);

	                double extraTime = journeyLength.get(i);
	                int addedChange = 0;

	                // adds 2 minutes if changing line
	                if (!currentLine.equalsIgnoreCase(nextLine)) {

	                    extraTime = extraTime + 2.0;
	                    addedChange = 1;
	                }

	                int nextPosition = findStationLineOption(nextStation, nextLine);

	                if (nextPosition != -1 && checkedStations.get(nextPosition) == false) {

	                    double possibleTime = bestPossibleTime.get(currentPosition) + extraTime;
	                    int possibleChanges = totalChanges.get(currentPosition) + addedChange;

	                    // updates it if this way is a better
	                    if (isBetterRoute(routeChoice, possibleTime, possibleChanges, nextPosition)) {

	                    	bestPossibleTime.set(nextPosition, possibleTime);
	                        totalChanges.set(nextPosition, possibleChanges);
	                        previousStation.set(nextPosition, currentPosition);
	                    }
	                }
	            }
	        }
	    }

	    int bestEndPosition = -1;

	    // finds the best end station version
	    for (int i = 0; i < stationNames.size(); i++) {

	        if (stationNames.get(i).equalsIgnoreCase(endingStation)) {

	            if (bestEndPosition == -1) {

	                bestEndPosition = i;

	            } else if (isBetterEnding(routeChoice, i, bestEndPosition)) {

	                bestEndPosition = i;
	            }
	        }
	    }

	    // checks if a route was found
	    if (bestEndPosition == -1 || bestPossibleTime.get(bestEndPosition) == LONG_TIME) {

	        JOptionPane.showMessageDialog(null, "No route found");
	        System.exit(0);

	    } else {
	        displayRoute(bestEndPosition, routeChoice);
	    }
	}

	public static void main(String[] args) {

	    // loads the spreadsheet data
	    loadCsvFile();

	    // gets the starting station
	    String beginningStation = JOptionPane.showInputDialog("Enter start station:");

	    // closes if cancel or the x button is pressed
	    if (beginningStation == null) {
	        System.exit(0);
	    }

	    // keeps asking until a proper station is entered
	    while (!stationExists(beginningStation)) {

	        beginningStation = JOptionPane.showInputDialog("Station not found. Enter a valid station:");

	        if (beginningStation == null) {
	            System.exit(0);
	        }
	    }

	    // gets the ending station
	    String endingStation = JOptionPane.showInputDialog("Enter end station:");
	    // closes if cancel or x is pressed
	    if (endingStation == null) {
	        System.exit(0);
	    }

	    // keeps asking again until a proper station is entered
	    while (!stationExists(endingStation)) {

	        endingStation = JOptionPane.showInputDialog("Station not found. Enter a valid station:");

	        if (endingStation == null) {
	            System.exit(0);
	        }
	    }

	    // options for the journey type
	    String[] journeyOptions = {"Shortest Time", "Fewest Changes"};

	    // shows the option box
	    int optionPicked = JOptionPane.showOptionDialog(
	            null,
	            "Choose route option:",
	            "Metrolink Route Planner",
	            JOptionPane.DEFAULT_OPTION,
	            JOptionPane.QUESTION_MESSAGE,
	            null,
	            journeyOptions,
	            journeyOptions[0]
	    );

	    // closes if cancel or x button is pressed
	    if (optionPicked == -1) {
	        System.exit(0);
	    }

	    // runs the chosen route type
	    if (optionPicked == 0) {
	        findRoute(beginningStation, endingStation, "time");
	    } else {
	        findRoute(beginningStation, endingStation, "changes");
	    }

	    System.exit(0);
	
	}
	    
	}
