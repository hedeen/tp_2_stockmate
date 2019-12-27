import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.UncheckedIOException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

public class FilingSummary {

	protected ArrayList<FilingLocator> filings = new ArrayList<FilingLocator>();
	protected FilingContainer fc = new FilingContainer();
	protected FilingTag filingTag = new FilingTag();
	protected String[] tags;
	protected String ticker;

	private String filingPreviewCache = "";

	public static void main(String[] args) {

		FilingSummary fs = new FilingSummary("HOFT", new String[] { "esb", "esd", "ern", "shb", "shd", "pft", "gpf" });

		fs.bufferAllFilings();

		System.out.println(fs.getFilingPreview("\t"));

	}

	public FilingSummary(String ticker, String[] tags) {
		this.tags = tags;
		this.ticker = ticker;

		// populateFilings("10-");
	}

	public HashMap<LocalDate, String> getFilingDates() {

		// ArrayList<LocalDate> filingDates = new ArrayList<LocalDate>();

		HashMap<LocalDate, String> filingDates = new HashMap<LocalDate, String>();

		for (FilingLocator d : this.filings) {
			filingDates.put(d.getFilingDate(), d.getFilingType());
		}

		return filingDates;
	}

	public void bufferAllFilings() {

		// load all the annuals avail
		populateFilings("10-K");
		populateFilingData(999);

		// load all the quarterlies avail
		populateFilings("10-Q");
		populateFilingData(999);
		// load only the 12 most recent quarters (3 years)
		// populateFilingData(12);
	}

	public void setTag(String tag) {
		// convert to string array from a single string
		String[] tmp = new String[1];
		tmp[0] = tag;
		this.tags = tmp;

		// clear the map if we are changing the tags
		this.fc = new FilingContainer();
	}

	public void bufferMostRecentFiling() {
		populateFilings("10-");
		populateFilingData(1); // load only most recent
	}

	public int getFilingCount() {
		return this.filings.size();
	}

	public ArrayList<String[]> getTagArray() {

		ArrayList<String[]> rtn = new ArrayList<String[]>();
		ArrayList<LocalDate> filingDates = this.fc.getFilingDates();

		for (LocalDate fd: filingDates) {
			if (this.fc.hasPeriodData(fd, 3)) {
				for (int i = 0; i < this.tags.length; i++) {
					String tag = this.tags[i];
					String[] row = new String[] { tag, String.valueOf(fd), String.valueOf(3),	this.fc.get(fd, 3, tag) };
					rtn.add(row);
				}
			}
			if (this.fc.hasPeriodData(fd, 12)) {
				for (int i = 0; i < this.tags.length; i++) {
					String tag = this.tags[i];
					String[] row = new String[] { tag, String.valueOf(fd), String.valueOf(12),	this.fc.get(fd, 12, tag) };
					rtn.add(row);
				}
			}
		}
			

		return rtn;
	}

	public String getTagData(LocalDate periodEnd, int periodLength, String tag) {

		if (this.fc.hasData(periodEnd, periodLength, tag)) {
			return this.fc.get(periodEnd, periodLength, tag);
		} else {
			return null;
		}

	}

//	public String[] getMostRecentFilingData(String tag) {
//
//		int yr = this.fc.getMaxYear();
//		String[] filingData = null;
//
//		for (int prd = 4; prd >= 0; prd--) {
//			if (this.fc.hasData(yr, prd, tag)) {
//				filingData = new String[] { String.valueOf(yr), String.valueOf(prd), this.fc.get(yr, prd, tag) };
//				return filingData;
//			}
//		}
//
//		return null;
//
//	}

	// Returns a string representing the filing data preview
	public String getFilingPreview(String delimiter) {
		// console output of the data we have parsed

		String output = null;
		String newline = System.lineSeparator();
		ArrayList<LocalDate> filingDates = this.fc.getFilingDates();

		if (fc.getRows() == 0) {
			return null;
		}

		String delim = delimiter;

		// output = ("Filing Data for " + this.ticker) + newline;
		output = "endDate" + delim + "period"; // + delim + "startDate" + delim + "endDate";
		for (String tag : this.tags) {
			output = output + (delim + tag);
		}
		output = output + newline;

		String buffer = "";
		for (LocalDate fd: filingDates) {
			
			//annual
			if (this.fc.hasPeriodData(fd, 12)) {
				for (int i = 0; i < this.tags.length - 1; i++) {
					String tag = this.tags[i];
					buffer = buffer + this.fc.get(fd, 12, tag) + delim;
				}
				buffer = buffer + this.fc.get(fd, 12, this.tags[this.tags.length - 1]);
				output = output + (fd + delim + 12 + delim + buffer) + newline;
			}
			
			//quarterly
			if (this.fc.hasPeriodData(fd, 3)) {
				for (int i = 0; i < this.tags.length - 1; i++) {
					String tag = this.tags[i];
					buffer = buffer + this.fc.get(fd, 3, tag) + delim;
				}
				buffer = buffer + this.fc.get(fd, 3, this.tags[this.tags.length - 1]);
				output = output + (fd + delim + 3 + delim + buffer) + newline;
			}
			buffer = "";
		}

		// Save the last filing preview so we can access it without needing to requery
		this.filingPreviewCache = output;
		return output;
	}

	public String getCachedFilingPreview() {
		return this.filingPreviewCache;
	}

	public String getTicker() {
		return this.ticker;
	}

	public boolean checkTagSupported(String tg) {
		return this.filingTag.checkTagSupported(tg);
	}

	public String getFormattedStringOfSupportedTags() {
		return this.filingTag.getFormattedStringOfSupportedTags();
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
		while (content == null) {
			try {
				content = Jsoup.connect(url).execute().body();
			} catch (IOException e1) {
				return null;
			} catch (UncheckedIOException e2) {
				content = null;
			}
		}
		return content;

	}

	public void populateFilings(String filingsFilter) {
		// this strips the filing information from the SEC filing table

		String cik = getCIK(this.ticker.toUpperCase());

		if (cik == null) {
			return;
		}

		if (this.filings.size() > 0) {
			this.filings.clear();
		}

		String companyPageHTML = getHTML(String.format(
				"https://www.sec.gov/cgi-bin/browse-edgar?action=getcompany&CIK=%s&type=%s", cik, filingsFilter));

		// grab table of filings from company page
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
			this.filings.add(new FilingLocator(filingType, filingDate, filingPageUrl, filingXmlUrl));

		}

	}

	protected String getURLofXML(String html) {
		// this returns the URL of the XML filing

		Document doc = null;
		Element table = null;

		try {
			doc = Jsoup.parse(getHTML(html));
			table = doc.select("table[summary=Data Files]").first();
		} catch (NullPointerException e) {
			// return null;
		}

		if (table != null) {

			Elements rows = table.select("tr");
			for (int i = 1; i < rows.size(); i++) {
				Element row = rows.get(i);
				Elements cols = row.select("td");
				if (cols.get(3).text().contains("INS")) {
					return "https://www.sec.gov" + cols.get(2).select("a").first().attr("href");
				} else if (cols.get(3).text().contains("XML")) {
					return "https://www.sec.gov" + cols.get(2).select("a").first().attr("href");
				}

			}
		}
		return null;
	}

	protected LocalDate getContextDate(Element doc, String contextref, String dateType)
			throws NullPointerException, DateTimeParseException {
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

//		if (fiscalPeriod.toUpperCase().startsWith("Q")) {
//			period = Integer.parseInt(fiscalPeriod.substring(1));
		if (fiscalPeriod.toUpperCase().contains("Q")) {
			String periodWithoutSpaces = fiscalPeriod.replace(" ", "");
			period = Integer.parseInt((periodWithoutSpaces.substring(periodWithoutSpaces.lastIndexOf("Q") + 1)));
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
		LocalDate periodEndDate;

		try {
			fiscalYearFocus = Integer.parseInt(doc.select("dei|DocumentFiscalYearFocus").text());
			periodEndDate = LocalDate.parse(doc.select("dei|DocumentPeriodEndDate").text());
		} catch (NumberFormatException | DateTimeParseException e) {
			return -1;
		}

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

		for (FilingLocator d : this.filings) {

			String filingXML = d.getFilingXML();

			if (rptCount >= maxRpts) {
				break;
			} else {
				if (filingXML != null) {
					String html = getHTML(filingXML);
					if (html != null) {

						Document doc = Jsoup.parse(html, "", Parser.xmlParser());

						for (String tag : this.tags) {
							Elements elements = doc.getElementsByTag(this.filingTag.getXbrlTag(tag));
							for (Element e : elements) {
								try {
									String contextref = e.attr("contextref");
									LocalDate startDate = getContextDate(doc, contextref, "startDate");
									LocalDate endDate = getContextDate(doc, contextref, "endDate");
									int months = getMonths(startDate, endDate);
									int periodScope = getPeriodScope(doc, endDate, months);
									int periodYear = getPeriodYear(doc, endDate);
									if (periodScope != -1 && periodYear != -1) {
										this.fc.put(endDate, months, tag, e.text());
										this.fc.put(endDate, months, "endDate", endDate.toString());
										this.fc.put(endDate, months, "startDate", startDate.toString());
										this.fc.put(endDate, months, "filingDate",
												d.getFilingDate().toString());
									}
								} catch (Exception e1) {
									// e1.printStackTrace();
									// ignore record

								}
							}
						}
					}
					rptCount++;
				}

			}

		}

	}

}
