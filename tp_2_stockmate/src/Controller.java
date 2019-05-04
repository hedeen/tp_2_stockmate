
public class Controller {

	public static void main(String[] args) {

		//load and print incomes from latest filing
		FilingProcessor fp1 = new FilingProcessor("GOOG", new String[] { "income" });
		System.out.println("GOOG has " + fp1.getFilingCount() + " filings");
		fp1.bufferMostRecentFiling();
		String[] mrfd = fp1.getMostRecentFilingData("income");
		int mostRecentYear = Integer.parseInt(mrfd[0]);
		int mostRecentPeriod =Integer.parseInt(mrfd[1]);

		System.out.println("Most recent filing: " + mostRecentYear + "-" + mostRecentPeriod);
		System.out.println("\t has data of : " + fp1.getTagData(mostRecentYear, mostRecentPeriod, "income"));
		System.out.println("\t also attained with: " + mrfd[2]);
		System.out.println("We can preview all data with this:\n" + fp1.getFilingPreview());

		//load and print all data from all filings
		FilingProcessor fp2 = new FilingProcessor("AAPL", new String[] { "eps", "epsd" });
		fp2.bufferAllFilings();
		System.out.println(fp2.getFilingPreview());

		//load data from latest filing and then return data for specific tag
		FilingProcessor fp3 = new FilingProcessor("MSFT", new String[] { "income" });
		fp3.bufferMostRecentFiling();
		System.out.println("The most recent income for microsoft is " + fp3.getMostRecentFilingData("income")[2]);		
	
	}

}
