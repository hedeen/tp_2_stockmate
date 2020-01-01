import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class FilingLocator {
	private String filingType;
	private LocalDate filingDate;
	private String filingPageUrl;
	private String filingXmlUrl;

	public FilingLocator(String type, String date, String link, String xmlLink) {
		this.filingType = type;
		this.filingDate = parseDate(date);
		this.filingPageUrl = link;
		this.filingXmlUrl = xmlLink;
	}
	
	public String getFilingXML() {
		return this.filingXmlUrl;
	}

	private LocalDate parseDate(String date) {
		//this function will attempt to parse a string date into a java date
		DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		LocalDate parsedDate;
		parsedDate = LocalDate.parse(date,format);
		return parsedDate;
	}
	
	public LocalDate getFilingDate() {
		return this.filingDate;
	}
	
	public String getFilingType() {
		return this.filingType;
	}

	public String getFilingPageUrl() {
		return filingPageUrl;
	}
}
