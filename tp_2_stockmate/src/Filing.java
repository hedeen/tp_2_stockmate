import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Filing {
	private String filingType;
	private LocalDate filingDate;
	private String filingPageUrl;
	private String filingXmlUrl;

	public Filing(String type, String date, String link, String xmlLink) {
		this.filingType = type;
		this.filingDate = parseDate(date);
		this.filingPageUrl = link;
		this.filingXmlUrl = xmlLink;
	}
	
//	public Filing(Filing f) {
//		this.filingType = f.getFilingType();
//		this.filingDate = f.getFilingDate();
//		this.filingPageUrl = f.getFilingURL();
//	}
	
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

//	public String getFilingType() {
//		return this.filingType;
//	}
//
//	public LocalDate getFilingDate() {
//		return this.filingDate;
//	}
//
//	public String getFilingURL() {
//		return this.filingPageUrl;
//	}
}
