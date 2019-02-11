import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;


public class IO {
	// I used recursive DFS treenode traversal function to find and return all the
	// JSONObjects that matches

	public static void main(String[] args) throws Exception {

		String url = "https://raw.githubusercontent.com/jdolan/quetoo/master/src/cgame/default/ui/settings/SystemViewController.json";

		Scanner in = new Scanner(System.in);

		JSONObject originalJson = new JSONObject(readJsonFromURL(url));// load the JSONObject from URL

		System.out.println(originalJson.toString());

		List<JSONObject> result = new ArrayList<>(); // storing the final result;

		String inputLine;

		do {
			System.out.println(
					"Enter a selector below. For Compound Selector, please seperate each selector by comma. For Chain Selector, use dot."
							+ "\n");

			inputLine = in.nextLine();
			String input = inputLine.replaceAll("\\s+", "");// get rid of Space;

			int type = checkSelectorType(input);

			// To support Compound Selector and Chain Selector "checkSelectorType" returns 0
			// for simple, 1 for Compound, 2 for Chain

			if (type == 0) {
				result.clear(); // clear before use.

				recur(originalJson, input, result);

				for (JSONObject a : result) {

					System.out.println(a.toString());
				}
			}

			if (type == 1) {

				result.clear();

				List<String> selectorList = Arrays.asList(input.split(","));// split each selector

				for (String a : selectorList) {

					recur(originalJson, a, result); // for compound , just do the recursion for each selector

				}

				for (JSONObject a : result) {

					System.out.println(a.toString());

				}

			}

			if (type == 2) {

				result.clear();

				List<String> chainList = Arrays.asList(input.split("\\."));// split each selector
				List<JSONObject> resultOfEachLayer = new ArrayList<>();
				resultOfEachLayer.add(originalJson);

				chainSelector(0, chainList, resultOfEachLayer, result);
				// for chain, a new recursion has to be added , because for the chain selector
				// list I added an other DFS.

				for (JSONObject a : result) {

					System.out.println(a.toString());

				}

			}

		} while (!inputLine.equals("Quit"));
		in.close();

	}

	private static JSONObject readJsonFromURL(String url) {

		JSONObject myresponse = new JSONObject();

		try {
			// read the json file

			URL obj = new URL(url);

			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));

			String inputLine;

			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {

				response.append(inputLine);

			}
			in.close();

			JSONParser parser = new JSONParser();
			myresponse = (JSONObject) parser.parse(response.toString());

		} catch (Exception e) {
			e.printStackTrace();
		}

		return myresponse;

	}

	private static int checkSelectorType(String input) {
		for (int i = 0; i < input.length(); i++) {
			if (input.charAt(i) == ',') {
				return 1;
			}
			if (input.charAt(i) == '.') {
				return 2;
			}

		}
		return 0;
	}

	private static void recur(JSONObject json, String target, List<JSONObject> result) {

		// This whole function is just a glorified DFS traversal of a K-nary tree.

		try {

			for (Object key : json.keySet()) { // Json is key value pairs; key is attributes , value is the selector

				String Jkey = (String) key;

				Object val = json.get(Jkey);

				if (Jkey.equals("classNames")) {// classNames is tricky,

					JSONArray arr = (JSONArray) val;

					for (Object e : arr) {

						if (e.equals(target)) {

							result.add(json);

							return;
							// if the program runs to here , it means one matching selector is found. For
							// this case, a matching className selector is found.
						}
					}

				}

				// Now, program does not know what kind of object it is facing so has to treat
				// them differently.

				if (val instanceof String) { // if it is String, check if match

					if (val.equals(target)) {

						result.add(json);// found one.

						return;

					}

				}
				if (val instanceof JSONObject) { // if it is JSONObject, just call the function.

					recur((JSONObject) val, target, result);

				}
				if (val instanceof JSONArray) {
					// if it is JSONArray, still need to call the function.
					// But in this function's input is JSONObject, so I wrote another one which
					// supports JSONArray.

					recurArray((JSONArray) val, target, result);

				}

			}
		} catch (Exception e) {

			e.printStackTrace();
		}
	}

	private static void recurArray(JSONArray input, String target, List<JSONObject> result) {

		try {

			JSONArray array = input;

			for (Object a : array) {

				if (a instanceof JSONObject) {

					recur((JSONObject) a, target, result);

				}
				if (a instanceof JSONArray) {

					recurArray((JSONArray) a, target, result);

				}

			}

		} catch (Exception e) {

			e.printStackTrace();

		}

	}

	private static void chainSelector(int index, List<String> list, List<JSONObject> resultOfEachLayer,
			List<JSONObject> result) {
		
		// what this function do is basically recursively look for each of the selector in Json.

		try {

			if (index == list.size()) {// base case , if found all selectors in the list, add to result;
				for (JSONObject j : resultOfEachLayer) {
					result.add(j);
					
				}

				return;
			}
			for (JSONObject a : resultOfEachLayer) {
				// find target selector in given result list
				// Box.VideoModeSelect for example, 0th layer is the whole json , target is Box. results of Box 
				// are in currResult, call chainSelector to get in 1st layer , do the same thing for VideoModeSelect in Box's result.
				List<JSONObject> currResult = new ArrayList<>();
				recur(a, list.get(index), currResult);
				chainSelector(index + 1, list, currResult, result);
			}
			return;

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
