import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

public class FilingProcessor {

	protected ArrayList<Filing> filings = new ArrayList<Filing>();
	protected FilingData filingData = new FilingData();
	protected String[] tags;
	protected String ticker;
	protected FilingTag[] SupportedTags = { new FilingTag("eps", "earnings per share"),
			new FilingTag("epsd", "earnings per share"), new FilingTag("income", "income") };
	private String filingPreviewCache = "";

	public FilingProcessor(String ticker, String[] tags) {
		this.tags = tags;
		this.ticker = ticker;

		populateFilings("10-");
	}

	public FilingProcessor(String ticker) {
		this.ticker = ticker;

		populateFilings("10-");
	}

	public void bufferAllFilings() {

		// load all the annuals avail
		populateFilings("10-K");
		populateFilingData(999);

		// load only the 3 most recent quarters
		populateFilings("10-Q");
		populateFilingData(3);
	}

//	public void setTags(String[] tags) {
//		this.tags = tags;
//	}

	public void setTag(String tag) {
		// convert to string array from a single string
		String[] tmp = new String[1];
		tmp[0] = tag;
		this.tags = tmp;
	}

	public void bufferMostRecentFiling() {
		populateFilings("10-");
		populateFilingData(1); // load only most recent
	}

	public int getFilingCount() {
		return this.filings.size();
	}

	protected String getCIK(String ticker) {
		// this translates the CIK from a given ticker

		Pattern pattern = Pattern.compile(".*CIK=(\\d{10}).*");
		Matcher matcher = pattern.matcher(getHTML("https://www.sec.gov/cgi-bin/browse-edgar?CIK=" + ticker
				+ "&Find=Search&owner=exclude&action=getcompany"));

		if (matcher.find()) {
			MatchResult result = matcher.toMatchResult();
			return result.group(1);
		} else {
			return null;
		}

	}

	protected String getHTML(String url) {
		// turns a url into a string of raw html
		String content = null;

		try {
			URLConnection connection = new URL(url).openConnection();
			Scanner scanner = new Scanner(connection.getInputStream());
			scanner.useDelimiter("\\Z");
			content = scanner.next();
			scanner.close();
		} catch (IOException e1) {
			// e.printStackTrace();
			return null;
		}
		return content;

	}

	protected void populateFilings(String filingsFilter) {
		// this strips the filing information from the SEC filing table

		String cik = getCIK(this.ticker.toUpperCase());

		if (this.filings.size() > 0) {
			this.filings.clear();
		}

		String companyPageHTML = getHTML(String.format(
				"https://www.sec.gov/cgi-bin/browse-edgar?action=getcompany&CIK=%s&type=%s", cik, filingsFilter));

		Document doc = Jsoup.parse(companyPageHTML);
		Element table = doc.select("table[class=tableFile2]").first();
		Elements rows = table.select("tr");

		// loop through each row, and break the columns into an element
		for (int i = 1; i < rows.size(); i++) {
			Element row = rows.get(i);
			Elements cols = row.select("td");
			String filingType = cols.get(0).text();
			String filingDate = cols.get(3).text();
			String filingPageUrl = "https://www.sec.gov" + cols.get(1).select("a").first().attr("href");
			String filingXmlUrl = getURLofXML(filingPageUrl);
			this.filings.add(new Filing(filingType, filingDate, filingPageUrl, filingXmlUrl));

		}

	}

	protected String getURLofXML(String html) {
		// this returns the URL of the XML filing

		Document doc = Jsoup.parse(getHTML(html));
		Element table = doc.select("table[summary=Data Files]").first();
		if (table == null) {
			return null;
		}
		Elements rows = table.select("tr");
		for (int i = 1; i < rows.size(); i++) {
			Element row = rows.get(i);
			Elements cols = row.select("td");
			if (cols.get(3).text().contains("INS")) {
				return "https://www.sec.gov" + cols.get(2).select("a").first().attr("href");
			}else if(cols.get(3).text().contains("XML")){
				return "https://www.sec.gov" + cols.get(2).select("a").first().attr("href");
			}

		}
		return null;
	}

	public String translateTag(String shortTag) {
		// this suits as a container to allow short abbreviations to be translated into
		// the longer XBRL tags
		switch (shortTag.toLowerCase()) {
		case "epsd":
			return "us-gaap:EarningsPerShareDiluted";
		case "eps":
			return "us-gaap:EarningsPerShareBasic";
		case "income":
			return "us-gaap:NetIncomeLoss";
		default:
			return null;
		}
	}

	protected LocalDate getContextDate(Element doc, String contextref, String dateType) {
		// this returns a date for a given element list with a given context for a
		// specific tag
		String dateString;
		dateString = doc.getElementById(contextref).select(dateType).text();
		if (dateString.contentEquals("")) {
			dateString = doc.getElementById(contextref).select("xbrli|" + dateType).text();
		}
		return LocalDate.parse(dateString);
	}

	protected int getPeriodScope(Element doc, LocalDate endDate, int periodLength) {
		// this converts the passed endDate and period to standard quarterly or annual
		// format
		int period;
		String fiscalPeriod = doc.select("dei|DocumentFiscalPeriodFocus").text();
		LocalDate periodEndDate;
		try {
			periodEndDate = LocalDate.parse(doc.select("dei|DocumentPeriodEndDate").text());
		} catch (DateTimeParseException e) {
			return -1;
		}

		if (fiscalPeriod.toUpperCase().startsWith("Q")) {
			period = Integer.parseInt(fiscalPeriod.substring(1));
		} else {

			if (periodEndDate.getMonthValue() == endDate.getMonthValue() && periodLength == 3) {
				// period = "Q4";
				period = 4;
			} else if (periodEndDate.minusMonths(3).getMonthValue() == endDate.getMonthValue() && periodLength == 3) {
				// period = "Q3";
				period = 3;
			} else if (periodEndDate.minusMonths(6).getMonthValue() == endDate.getMonthValue() && periodLength == 3) {
				// period = "Q2";
				period = 2;
			} else if (periodEndDate.minusMonths(9).getMonthValue() == endDate.getMonthValue() && periodLength == 3) {
				// period = "Q1";
				period = 1;
			} else if (periodEndDate.getMonthValue() == endDate.getMonthValue() && periodLength == 12) {
				// period = "FY";
				period = 0;
			} else {
				// period = "error";
				period = -1;
			}
		}

		return period;
	}

	protected int getPeriodYear(Element doc, LocalDate endDate) {
		// this attempts to appropriate the proper reporting year since sometimes
		// companies will have an end date that falls outside the calendar year but is
		// reported for a different fiscal year

		int periodYear = -1;
		int fiscalYearFocus;

		try {
			fiscalYearFocus = Integer.parseInt(doc.select("dei|DocumentFiscalYearFocus").text());
		} catch (NumberFormatException e) {
			return -1;
		}

		LocalDate periodEndDate = LocalDate.parse(doc.select("dei|DocumentPeriodEndDate").text());

		int monthsDif = getMonths(endDate, periodEndDate);
		if (monthsDif < 12) {
			periodYear = (fiscalYearFocus - 0);
		} else if (monthsDif < 24) {
			periodYear = (fiscalYearFocus - 1);
		} else if (monthsDif < 36) {
			periodYear = (fiscalYearFocus - 2);
		}

		return periodYear;
	}

	protected int getMonths(LocalDate startDate, LocalDate endDate) {
		// this returns the length in months between two dates
		double days = (endDate.getDayOfMonth() - startDate.getDayOfMonth())
				+ 31 * (endDate.getMonthValue() - startDate.getMonthValue())
				+ 365 * (endDate.getYear() - startDate.getYear());
		int len = (int) Math.round(days / 30.0);
		return len;
	}

	protected void populateFilingData(int maxRpts) {
		// this stores the tags from the list of filings up to the requested number of
		// reports

		int rptCount = 0;

		for (Filing d : this.filings) {

			String filingXML = d.getFilingXML();

			if (rptCount >= maxRpts) {
				break;
			} else {
				if (filingXML != null) {
					String html = getHTML(filingXML);
					if (html != null) {

						Document doc = Jsoup.parse(html, "", Parser.xmlParser());

						for (String tag : this.tags) {
							Elements elements = doc.getElementsByTag(translateTag(tag));
							for (Element e : elements) {
								String contextref = e.attr("contextref");
								LocalDate startDate = getContextDate(doc, contextref, "startDate");
								LocalDate endDate = getContextDate(doc, contextref, "endDate");
								int months = getMonths(startDate, endDate);
								int periodScope = getPeriodScope(doc, endDate, months);
								int periodYear = getPeriodYear(doc, endDate);
								if (periodScope != -1 && periodYear != -1) {
									this.filingData.put(periodYear, periodScope, tag, e.text());
								}

							}
						}
					}
					rptCount++;
				}

			}

		}

	}

	public String getTagData(int year, int period, String tag) {

		if (this.filingData.hasData(year, period, tag)) {
			return this.filingData.get(year, period, tag);
		} else {
			return null;
		}

	}

	public String[] getMostRecentFilingData(String tag) {

		int yr = this.filingData.getMaxYear();
		String[] filingData = null;

		for (int prd = 4; prd >= 0; prd--) {
			if (this.filingData.hasData(yr, prd, tag)) {
				filingData = new String[] { String.valueOf(yr), String.valueOf(prd),
						this.filingData.get(yr, prd, tag) };
				return filingData;
			}
		}

		return null;

	}

	// Returns a string representing the filing data preview
	public String getFilingPreview(String delimiter) {
		// console output of the data we have parsed

		String output = null;
		String newline = System.lineSeparator();

		if (filingData.getRows() == 0) {
			return null;
		}

		String delim = delimiter;

		//output = ("Filing Data for " + this.ticker) + newline;
		output = "year" + delim + "period";
		for (String tag : this.tags) {
			output = output + (delim + tag);
		}
		output = output + newline;

		String buffer = "";
		for (int yr = this.filingData.getMaxYear(); yr >= this.filingData.getMinYear(); yr--) {
			for (int prd = 4; prd >= 0; prd--) {
				if (this.filingData.hasPeriodData(yr, prd)) {
					for (int i = 0; i < this.tags.length - 1; i++) {
						String tag = this.tags[i];
						buffer = buffer + this.filingData.get(yr, prd, tag) + delim;
					}
					buffer = buffer + this.filingData.get(yr, prd, this.tags[this.tags.length-1]);
					output = output + (yr + delim + prd + delim + buffer) + newline;
				}
				buffer = "";
			}
		}

		// Save the last filing preview so we can access it without needing to requery
		this.filingPreviewCache = output;
		return output;
	}

	// Returns an array of string descriptions that represent the supported SEC tags
	public String[] getArrayOfSupportedTagDescriptions() {
		String[] tmp = new String[SupportedTags.length];

		for (int i = 0; i < SupportedTags.length; i++) {
			tmp[i] = SupportedTags[i].getFullDescription();
		}
		return tmp;
	}

	// Returns an array of the string tags that represent the supported SEC tags
	public String[] getArrayOfSupportedTags() {
		String[] tmp = new String[SupportedTags.length];

		for (int i = 0; i < SupportedTags.length; i++) {
			tmp[i] = SupportedTags[i].getTag();
		}
		return tmp;
	}

	// Checks to see if the requested string tag is supported by the FilingProcessor
	public boolean checkTagSupported(String tg) {
		// Convert String Array to List for easy contains method
		List<String> list = Arrays.asList(this.getArrayOfSupportedTags());

		if (list.contains(tg)) {
			return true;
		} else {
			return false;
		}
	}

	// Get the full description from the tag name (string)
	public String getFullTagDescription(String tg) {
		String tmp = null;

		for (int i = 0; i < SupportedTags.length; i++) {
			if (SupportedTags[i].getTag().equals(tg)) {
				tmp = SupportedTags[i].getFullDescription();
				break; // we found the matching tag so can leave
			}
		}
		return tmp;
	}

	public String getTicker() {
		return this.ticker;
	}

	public String getFormattedStringOfSupportedTags() {

		String formatted = "";

		for (int i = 0; i < SupportedTags.length; i++) {
			String tmp = "[" + SupportedTags[i].getTag() + "] " + SupportedTags[i].getFullDescription();

			// Only want to append on spaces after we have more than 1 supported tag
			// if (i>0) {
			formatted = formatted + System.lineSeparator() + tmp;
			// } else {
			// formatted = tmp;
			// }
		}

		return formatted;
	}

	public String getCachedFilingPreview() {
		return this.filingPreviewCache;
	}
}
