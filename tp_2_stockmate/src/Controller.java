import edu.princeton.cs.introcs.StdOut;

public class Controller {

	// Setup variables and objects
	static FilingProcessor fp;
	static String response = "";
	static String folder = "";
	static DataStore ds;

	public static void main(String[] args) {

		try {
			// Setup variables and objects

			UI ui = new UI(UI.InterfaceOption.Console); // initialize the UI (this will be for console interaction)
			ds = new DataStore(); // initialize the data store (this will be for CSV/TXT)

			ui.displayMessageToUser(
					"Welcome to stock mate. This tool will help you gather SEC filing data for your favorite stocks.");

			// Supply a default folder if user doesn't specify
			folder = ui.displayMessageAndGetResponse(
					"Before we begin, please enter a local folder to save stock files:", "C:\\Stock Mate");

			ds.setupFileIO(folder);

			if (ds.checkFileIOValid()) {

				// Sit in loop forever. If user wants to end they can close the console
				while (true) {
					try {
						// File IO setup has been confirmed
						// Proceed to ask what filing data (ticker + tag) they are interested in
						response = ui.displayMessageAndGetResponse(
								"Enter a ticker id to begin", "");
						ui.displayMessageToUser("Retrieving SEC filings for " + response + "...");
						fp = new FilingProcessor(response);

						if (fp.getFilingCount() > 0) {
							// Tell user how many filings exist
							ui.displayMessageToUser(fp.getFilingCount() + " SEC filings detected");

							String tagResponse = "";
							String loopQuestion = "What data would you like to retrieve for " + fp.getTicker()
									+ "?" + System.lineSeparator()
									+ "Select a tag (in brackets) from the list of supported tags "
									+ fp.getFormattedStringOfSupportedTags()
									+ System.lineSeparator() + "Enter 'exit' to select a new ticker";

							tagResponse = ui.displayMessageAndGetResponse(loopQuestion, "");

							// Sit in this loop and allow them to run various queries on this stock ticker
							while (!tagResponse.toLowerCase().equals("exit")) {

								// Make sure its a valid tag before continuing
								if (fp.checkTagSupported(tagResponse)) {
									fp.setTag(tagResponse);

									String filingResponse = ui.displayMessageAndGetResponse(
											"Would you like to return all filings [1] for " + fp.getTicker()
													+ " or just the most recent [2]?",
											"1");

									switch (filingResponse) {
									
									case "1":
										ui.displayMessageToUser("Retrieving all filings for " + fp.getTicker() + "...");
										fp.bufferAllFilings();
										break;
									case "2":
										ui.displayMessageToUser(
												"Retrieving the most recent filings for " + fp.getTicker() + "...");
										fp.bufferMostRecentFiling();
										break;
									}

									ui.displayMessageToUser(fp.getFilingPreview());
									//TODO Write to CSV file

								} else {
									// error - requested filing tag
									ui.displayMessageToUser(
											"Requested SEC filing tag (" + tagResponse + ") not supported");
								}

								// Same prompt as above (while loop)
								tagResponse = ui.displayMessageAndGetResponse(loopQuestion, "");
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

//		// load and print incomes from latest filing
//		FilingProcessor fp1 = new FilingProcessor("GOOG", new String[] { "income" });
//		System.out.println("GOOG has " + fp1.getFilingCount() + " filings");
//		fp1.bufferMostRecentFiling();
//		String[] mrfd = fp1.getMostRecentFilingData("income");
//		int mostRecentYear = Integer.parseInt(mrfd[0]);
//		int mostRecentPeriod = Integer.parseInt(mrfd[1]);
//
//		System.out.println("Most recent filing: " + mostRecentYear + "-" + mostRecentPeriod);
//		System.out.println("\t has data of : " + fp1.getTagData(mostRecentYear, mostRecentPeriod, "income"));
//		System.out.println("\t also attained with: " + mrfd[2]);
//		System.out.println("We can preview all data with this:\n" + fp1.getFilingPreview());
//
//		// load and print all data from all filings
//		FilingProcessor fp2 = new FilingProcessor("AAPL", new String[] { "eps", "epsd" });
//		fp2.bufferAllFilings();
//		System.out.println(fp2.getFilingPreview());
//
//		// load data from latest filing and then return data for specific tag
//		FilingProcessor fp3 = new FilingProcessor("MSFT", new String[] { "income" });
//		fp3.bufferMostRecentFiling();
//		System.out.println("The most recent income for microsoft is " + fp3.getMostRecentFilingData("income")[2]);

	}

}
