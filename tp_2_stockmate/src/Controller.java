import java.text.SimpleDateFormat;
import java.util.Date;
import edu.princeton.cs.introcs.StdOut;

public class Controller {

	// Setup variables and objects
	static FilingProcessor fp;
	static String response = "";
	static DataStore ds;
	static UI ui;
	static DataStore.WriteOption dataOption = DataStore.WriteOption.CSV; // Default to .csv files (will ask user if they
																			// want to change)

	// program constants
	static final String fileDateFormat = "yyyy.MM.dd.HH.mm.ss";

	public static void main(String[] args) {

		try {

			setupStockMate();

			if (ds.checkFileIOValid()) {

				// Sit in loop forever. If user wants to end they can close the console
				while (true) {
					try {
						// File IO setup has been confirmed
						// Need to ask the user what ticker to begin working off of
						initiateTickerProcessing();

						if (fp.getFilingCount() > 0) {

							// filings exist for ticker (valid)
							reportInitialTickerInfo();

							// Proceed to ask what filing data (ticker + tag) they are interested in
							String tagResponse = ui.displayMessageAndGetStringResponse(loopQuestion(), "");

							// Sit in this loop and allow them to run various queries on this stock ticker
							while (!tagResponse.toLowerCase().equals("exit")) {

								// Make sure its a valid tag before continuing
								if (fp.checkTagSupported(tagResponse)) {
									fp.setTag(tagResponse);

									handleTagResponseAndReport();

									// setup the filename (e.g. GOOG - eps (2019.03.....)"
									String timeStamp = new SimpleDateFormat(fileDateFormat).format(new Date());
									String filename = fp.getTicker() + " - " + tagResponse + " (" + timeStamp + ")";

									ds.writeData(fp.getCachedFilingPreview(), filename);
									ui.displayMessageToUser(ds.getLastWriteInfo());

								} else {
									// error - requested filing tag
									ui.displayMessageToUser(
											"Requested SEC filing tag (" + tagResponse + ") not supported");
								}

								// Same prompt as above (while loop)
								tagResponse = ui.displayMessageAndGetStringResponse(loopQuestion(), "");
							}

						} else {
							// error - requested ticker
							ui.displayMessageToUser("0 SEC filings detected. Enter a new ticker and try again");
						}
					}

					catch (Exception e) {
						// Error handling for while loop
						StdOut.println("Program error " + e.getMessage());
					}
				}

			} else {
				// error
				ui.displayMessageToUser("Requested folder not valid. Program will end");
			}

		} catch (Exception e) {
			// Error handling in case issue occur before we get to while loop
			StdOut.println("Program error on initialization " + e.getMessage());
		}
	}

	// Constant defined loop question that is used multiple times (defined here as
	// field for better readability)
	static String loopQuestion() {

		String q = "What data would you like to retrieve for " + fp.getTicker() + "?" + System.lineSeparator()
				+ "Select a tag (in brackets) from the list of supported tags " + fp.getFormattedStringOfSupportedTags()
				+ System.lineSeparator() + "Enter 'exit' to select a new ticker";

		return q;
	}

	static void setupStockMate() throws Exception {

		ui = new UI(UI.InterfaceOption.Console); // initialize the UI (this will be for console interaction)
		ui.displayMessageToUser(
				"Welcome to stock mate. This tool will help you gather SEC filing data for your favorite stocks. Before we begin, let's get some setup out of the way");

		int writeOpt = ui.displayMessageAndGetIntResponse(
				"Please select how you would like your data stored [1] CSV files, [2] TXT files", 1, 2, 2); // text
																											// files
																											// default

		switch (writeOpt) {

		case 1:
			ds = new DataStore(DataStore.WriteOption.CSV); // initialize the data store
			String tmp1 = ui.displayMessageAndGetStringResponse(
					"You have selected to create .csv files. Please enter a local folder to save stock files: ",
					"C:\\Stock Mate");
			ds.setupFileIO(tmp1);
			break;
		case 2:
			ds = new DataStore(DataStore.WriteOption.TXT); // initialize the data store
			String tmp2 = ui.displayMessageAndGetStringResponse(
					"You have selected to create .txt files. Please enter a local folder to save stock files: ",
					"C:\\Stock Mate");
			ds.setupFileIO(tmp2);
			break;
		case 3:
			ds.setupSQL(""); // not currently supported
			break;
		}

	}

	static void initiateTickerProcessing() {
		response = ui.displayMessageAndGetStringResponse("Enter a ticker id", "");
		ui.displayMessageToUser("Retrieving SEC filings for " + response + "...");
		fp = new FilingProcessor(response);
	}

	static void reportInitialTickerInfo() {
		// Tell user how many filings exist
		ui.displayMessageToUser(fp.getFilingCount() + " SEC filings detected");

	}

	static void handleTagResponseAndReport() {
		int filingResponse = ui.displayMessageAndGetIntResponse(
				"Would you like to return all filings [1] for " + fp.getTicker() + " or just the most recent [2]?", 1,
				2, 2);

		switch (filingResponse) {

		case 1:
			ui.displayMessageToUser("Retrieving all filings for " + fp.getTicker() + "...");
			fp.bufferAllFilings();
			break;
		case 2:
			ui.displayMessageToUser("Retrieving the most recent filing for " + fp.getTicker() + "...");
			fp.bufferMostRecentFiling();
			break;
		}

		// text files are the default, tab separated values
		String delimiter = "\t";

		if (ds.getWriteOptionInt() == DataStore.WriteOption.CSV.ordinal()) {
			delimiter = ",";
		}

		// Show preview to user before writing to datastore
		ui.displayMessageToUser(fp.getFilingPreview(delimiter));
	}
}
