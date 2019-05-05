import edu.princeton.cs.introcs.StdIn;
import edu.princeton.cs.introcs.StdOut;

public class UI {

	public enum InterfaceOption {
		Console, GUI
	}

	private InterfaceOption Interface;

	public UI(InterfaceOption opt) throws Exception {
		// Initialize the constructor based on the requested type
		this.Interface = opt;

		switch (this.Interface) {
		case Console:
			break;
		case GUI:
			throw new UnsupportedOperationException("GUI interface not developed");
		}
	}

	public void displayMessageToUser(String message) {

		switch (this.Interface) {
		case Console:
			displayMessageToConsole(message);
			break;
		case GUI:
			throw new UnsupportedOperationException("GUI interface not developed");
		}
	}

	public String displayMessageAndGetResponse(String message, String defaultResponse) {

		String res = "";

		switch (this.Interface) {
		case Console:
			res = getUserStringFromConsole(message,defaultResponse);
			break;
		case GUI:
			throw new UnsupportedOperationException("GUI interface not developed");
		}
		return res;
	}

	private void displayMessageToConsole(String message) {
		System.out.println(message);
	}

	private static int getUserIntFromConsole(String prompt, int minVal, int maxVal) {

		int userInt = minVal - 1;

		do {
			try {
				StdOut.println(prompt);
				userInt = Integer.parseInt(StdIn.readLine());
			} catch (NumberFormatException e) {
				StdOut.println("That is not an integer, try again.");
			}
			if (userInt < minVal) {
				StdOut.println("Min allowed value is " + minVal);
			}

			if (userInt > maxVal) {
				StdOut.println("Max allowed value is " + maxVal);
			}
		} while (userInt < minVal || userInt > maxVal);

		return userInt;

	}

	private static String getUserStringFromConsole(String prompt, String defaultResponse) {

		String userString = "";
		do {
			
			// Modify the prompt (add on default response in brackets) if supplied
			if (defaultResponse.length()>0) {
				StdOut.println(prompt + " [" + defaultResponse + "]" );
			} else {
				StdOut.println(prompt);
			}			
			
			userString = StdIn.readLine();

			// Allow loop to kick out if defaultResponse is supplied and user has not entered anything
		} while ((userString.length() == 0) && !(defaultResponse.length()>0));
		
		if (userString.length() == 0) {
			return defaultResponse;
		} else {
			return userString;
		}
	}	
}
