

import java.io.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class FollowingSet {

    // class constants
    public final static int MAX_TOKENS = 2;
    public final static int FOLLOWING_LIMIT = 2000;
    public final static int HANDLE_CUTOFF = 4;
    public final static String FOLDER_ONE = "./V0.1";
    public final static String FOLDER_TWO = "./V0.2";
    public final static String V1 = "V1.txt";
    public final static String HANDLES_FILE = "handles_party.txt";
    public final static char DEMOCRAT = 'D';
    public final static char REPUBLICAN = 'R';

    // variables for storing information
    private static Set<String> idsWithinSet = new HashSet<>();
    private static Set<String> idsExceedSet = new HashSet<>();

    private static HashMap<String, String> memberParties = new HashMap<>(); // (members = party)
    private static HashMap<String, Integer> exceedsMembers = new HashMap<>(); // (members = numFollowing)
    private static HashMap<String, Integer> withinMembers = new HashMap<>(); // (members = numFollowing)

    // result variables
    private static int numWithinDem = 0;
    private static int numWithinRep = 0;
    private static int numExceedDem = 0;
    private static int numExceedRep = 0;
    private static int totalDemocrats = 0;
    private static int totalRepublicans = 0;

    public static void main(String[] args) throws IOException {

        System.out.println("\nBeginning of Processing........\n");

        readHandles();
        readCongressMembers(FOLDER_ONE);
        readCongressMembers(FOLDER_TWO);
        writeFile();
        printResults();

        System.out.println("\n\nEnd of processing\n");

    }

    private static void parseJsonArray(String handle, String content) {

        JSONParser parser = new JSONParser();
        JSONObject root = null;

        try {
            root = (JSONObject) parser.parse(content);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
        }


        assert (root != null);

        // get the "following" array
        JSONArray following = (JSONArray) root.get("following");
        Iterator<JSONObject> iterator = following.iterator();
        JSONObject object;

        if (following.size() <= FOLLOWING_LIMIT) {
            //add to corresponding set and
            //it's party counter

            withinMembers.put(handle, following.size());

            while (iterator.hasNext()) {
                object = iterator.next();

                // add the "id" field of each object in the array
                // to the set of all following accounts within our limit
                idsWithinSet.add((String) object.get("id"));

            }

            // increment the party count for members within our limit
            if (memberParties.get(handle).equals(Character.toString(DEMOCRAT))) {
                numWithinDem++;
            } else {
                numWithinRep++;
            }

        } else { // exceeds our limit

            exceedsMembers.put(handle, following.size());

            while (iterator.hasNext()) {
                object = iterator.next();

                // add the "id" field of each object in the array
                // to the set of all following accounts that exceed our limit
                idsExceedSet.add((String) object.get("id"));

            }

            // increment the party count for members that exceed our limit
            if (memberParties.get(handle).equals(Character.toString(DEMOCRAT))) {
                numExceedDem++;
            } else {
                numExceedRep++;
            }

        }

    }


    private static void processFollowingFile(File file) throws IOException {

        System.out.print(".");

        BufferedReader inputStream = null;
        String line;
        StringBuilder fileContent = new StringBuilder();
        String fileName = file.getName();
        String handle = file.getName().substring(0, fileName.length() - HANDLE_CUTOFF);

        try {

            inputStream = new BufferedReader(new FileReader(file));

            // add the contents of the file to a String containing a JSON array
            while ((line = inputStream.readLine()) != null)
                fileContent.append(line.trim());

        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }

        // Count total members in each party
        totalInfo(handle);

        // parse the JSON array stored as a string
        parseJsonArray(handle, fileContent.toString());

    }

    private static void readCongressMembers(String fileName) {

        System.out.println("\nReading files containing the following list of Congress Members from " + fileName);

        File dir = new File(fileName);
        File[] files = dir.listFiles();

        try {
            if (files == null) {
                throw new FileNotFoundException(fileName);
            }

            // Fetching all the files
            for (File file : files) {
                processFollowingFile(file);
            }

        } catch (FileNotFoundException e) {
            System.out.println(e + " not found!");

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

    }

    private static void readHandles() {

        try {

            FileReader myFile = new FileReader(HANDLES_FILE);
            BufferedReader file = new BufferedReader(myFile);

            String line;
            String[] tokens;

            while ((line = file.readLine()) != null) {

                tokens = line.split(" ");

                // ignore empty lines - use only valid lines (exactly 2 tokens)
                if (tokens.length == MAX_TOKENS)
                    memberParties.put(tokens[0], tokens[1]);

            }

            file.close();

        } catch (FileNotFoundException e) {
            System.out.println("FileNotFoundException: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }

    }

    // Count total members in each party
    private static void totalInfo(String handle) {

        if (memberParties.get(handle).equals(Character.toString(REPUBLICAN))) {
            totalRepublicans++;
        } else {
            totalDemocrats++;
        }

    }

    private static void writeFile() throws IOException {

        File file = new File(V1);
        FileWriter fw = new FileWriter(file);
        PrintWriter pw = new PrintWriter(fw);

        for (String id : idsWithinSet) {
            pw.println(id);
        }

        pw.close();

    }

    private static void printResults() {

        // Total counts
        System.out.println("\n\n#-----------------------------------------------------------------------");
        System.out.println("There are " + (totalDemocrats + totalRepublicans)
                + " total Congress Members with valid twitter handles");
        System.out.println(totalDemocrats + " of these are Democrats");
        System.out.println(totalRepublicans + " of these are Republicans");
        System.out.println("#-----------------------------------------------------------------------\n");

        // Exceed counts
        System.out.println("#-----------------------------------------------------------------------");
        System.out.println((numExceedDem + numExceedRep) + " total Congress Members follow greater than "
                + FOLLOWING_LIMIT + " users");
        System.out.println(
                "The number of Democrats with following greater than " + FOLLOWING_LIMIT + " is : " + numExceedDem);
        System.out.println("The number of Republicans with following greater than " + FOLLOWING_LIMIT + " is : "
                + numExceedRep);
        System.out.println("#-----------------------------------------------------------------------\n");

        // Within counts
        System.out.println("#-----------------------------------------------------------------------");
        System.out.println(
                (numWithinDem + numWithinRep) + " total Congress Members follow at most " + FOLLOWING_LIMIT + " users");
        System.out.println(
                "The number of Democrats with following at most " + FOLLOWING_LIMIT + " is : " + numWithinDem);
        System.out.println("The number of Republicans with following at most " + FOLLOWING_LIMIT + " is : "
                + numWithinRep);
        System.out.println("#-----------------------------------------------------------------------\n");

        // IDs information
        System.out.println(
                "#-------------------------------------------------------------------------------------------------------------------------------------");
        System.out.println("There are " + idsWithinSet.size()
                + " distinct IDs of users followed by at least one Congress Member with total following less than or equal to "
                + FOLLOWING_LIMIT);
        System.out.println("There are " + idsExceedSet.size()
                + " distinct IDs of users followed by at least one Congress Member with total following greater than "
                + FOLLOWING_LIMIT);

        Set<String> allUserIds = new HashSet<>(idsWithinSet);
        allUserIds.addAll(idsExceedSet);

        System.out.println("There are " + (allUserIds.size())
                + " distinct IDs of users followed by at least one Congress Member (All IDs in V1 - Potentially)\n");

        // File write information
        System.out.println("Added " + idsWithinSet.size() + " distinct IDs to " + V1);

        System.out.println(
                "#-------------------------------------------------------------------------------------------------------------------------------------\n");



    }

}
