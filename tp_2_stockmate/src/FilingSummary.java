import org.jsoup.parser.Parser;
import java.time.format.DateTimeParseException;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.UncheckedIOException;
import java.io.IOException;
import org.jsoup.Jsoup;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.ArrayList;


public class FilingSummary
{
    protected ArrayList<FilingLocator> filings;
    protected FilingContainer fc;
    protected FilingTag filingTag;
    protected String[] tags;
    protected String ticker;
    private String filingPreviewCache;
    
    public static void main(String[] args) {
        FilingSummary fs = new FilingSummary("DIS", new String[] { "esb", "esd", "ern", "shb", "shd", "pft", "gpf" });
        fs.bufferAllFilings();
        System.out.println(fs.getFilingPreview("\t"));
    }
    
    public FilingSummary(String ticker, String[] tags) {
        this.filings = new ArrayList<FilingLocator>();
        this.fc = new FilingContainer();
        this.filingTag = new FilingTag();
        this.filingPreviewCache = "";
        this.tags = tags;
        this.ticker = ticker;
    }
    
    public HashMap<LocalDate, String> getFilingDates() {
        HashMap<LocalDate, String> filingDates = new HashMap<LocalDate, String>();
        for (FilingLocator d : this.filings) {
            filingDates.put(d.getFilingDate(), d.getFilingType());
        }
        return filingDates;
    }
    
    public void bufferAllFilings() {
        this.populateFilings();
        this.populateFilingData(999);
    }
    
    public void setTag(String tag) {
        String[] tmp = { tag };
        this.tags = tmp;
        this.fc = new FilingContainer();
    }
    
    public void bufferMostRecentFiling() {
        this.populateFilings();
        this.populateFilingData(1);
    }
    
    public int getFilingCount() {
        return this.filings.size();
    }
    
    public ArrayList<String[]> getTagArray() {
        ArrayList<String[]> rtn = new ArrayList<String[]>();
        ArrayList<LocalDate> filingDates = (ArrayList<LocalDate>)this.fc.getFilingDates();
        for (LocalDate fd : filingDates) {
            if (this.fc.hasPeriodData(fd, 3)) {
                for (int i = 0; i < this.tags.length; ++i) {
                    String tag = this.tags[i];
                    String[] row = { tag, String.valueOf(fd), String.valueOf(3), this.fc.get(fd, 3, tag) };
                    rtn.add(row);
                }
            }
            if (this.fc.hasPeriodData(fd, 12)) {
                for (int i = 0; i < this.tags.length; ++i) {
                    String tag = this.tags[i];
                    String[] row = { tag, String.valueOf(fd), String.valueOf(12), this.fc.get(fd, 12, tag) };
                    rtn.add(row);
                }
            }
        }
        return rtn;
    }
    
    public String getTagData(LocalDate periodEnd, int periodLength, String tag) {
        if (this.fc.hasData(periodEnd, periodLength, tag)) {
            return this.fc.get(periodEnd, periodLength, tag);
        }
        return null;
    }
    
    public String getFilingPreview(String delimiter) {
        String output = null;
        String newline = System.lineSeparator();
        ArrayList<LocalDate> filingDates = (ArrayList<LocalDate>)this.fc.getFilingDates();
        if (this.fc.getRows() == 0) {
            return null;
        }
        String delim = delimiter;
        output = "endDate" + delim + "period";
        String[] tags;
        for (int length = (tags = this.tags).length, j = 0; j < length; ++j) {
            String tag = tags[j];
            output = String.valueOf(output) + delim + tag;
        }
        output = String.valueOf(output) + newline;
        String buffer = "";
        for (LocalDate fd : filingDates) {
            if (this.fc.hasPeriodData(fd, 12)) {
                for (int i = 0; i < this.tags.length - 1; ++i) {
                    String tag2 = this.tags[i];
                    buffer = String.valueOf(buffer) + this.fc.get(fd, 12, tag2) + delim;
                }
                buffer = String.valueOf(buffer) + this.fc.get(fd, 12, this.tags[this.tags.length - 1]);
                output = String.valueOf(output) + fd + delim + 12 + delim + buffer + newline;
            }
            if (this.fc.hasPeriodData(fd, 3)) {
                for (int i = 0; i < this.tags.length - 1; ++i) {
                    String tag2 = this.tags[i];
                    buffer = String.valueOf(buffer) + this.fc.get(fd, 3, tag2) + delim;
                }
                buffer = String.valueOf(buffer) + this.fc.get(fd, 3, this.tags[this.tags.length - 1]);
                output = String.valueOf(output) + fd + delim + 3 + delim + buffer + newline;
            }
            buffer = "";
        }
        return this.filingPreviewCache = output;
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
        Pattern pattern = Pattern.compile(".*CIK=(\\d{10}).*");
        try {
        	Matcher matcher = pattern.matcher(this.getHTML("https://www.sec.gov/cgi-bin/browse-edgar?CIK=" + ticker + "&Find=Search&owner=exclude&action=getcompany"));
            if (matcher.find()) {
                MatchResult result = matcher.toMatchResult();
                return result.group(1);
            }
		} catch (NullPointerException ex) {
			System.out.println("null CIK from sec.gov, aborting this attempt!");
		}
        
        return null;
    }
    
    protected String getHTML(String url) {
        String content = null;
        while (content == null) {
            try {
                content = Jsoup.connect(url).execute().body();
            }
            catch (IOException e1) {
                return null;
            }
            catch (UncheckedIOException e2) {
                content = null;
            }
        }
        return content;
    }
    
    public void populateFilings() {
        String cik = this.getCIK(this.ticker.toUpperCase());
        if (cik == null) {
            return;
        }
        if (this.filings.size() > 0) {
            this.filings.clear();
        }
        String companyPageHTML = this.getHTML(String.format("https://www.sec.gov/cgi-bin/browse-edgar?action=getcompany&CIK=%s&type=10-&dateb=&owner=exclude&count=100", cik));
        Document doc = Jsoup.parse(companyPageHTML);
        Element table = doc.select("table[class=tableFile2]").first();
        Elements rows = table.select("tr");
        for (int i = 1; i < rows.size(); ++i) {
            Element row = (Element)rows.get(i);
            Elements cols = row.select("td");
            String filingType = ((Element)cols.get(0)).text();
            String filingDate = ((Element)cols.get(3)).text();
            String filingPageUrl = "https://www.sec.gov" + ((Element)cols.get(1)).select("a").first().attr("href");
            String filingXmlUrl = this.getURLofXML(filingPageUrl);
            this.filings.add(new FilingLocator(filingType, filingDate, filingPageUrl, filingXmlUrl));
        }
    }
    
    protected String getURLofXML(String html) {
        Document doc = null;
        Element table = null;
        try {
            doc = Jsoup.parse(this.getHTML(html));
            table = doc.select("table[summary=Data Files]").first();
        }catch (NullPointerException ex) {}
        if (table != null) {
            Elements rows = table.select("tr");
            for (int i = 1; i < rows.size(); ++i) {
                Element row = (Element)rows.get(i);
                Elements cols = row.select("td");
                if (((Element)cols.get(3)).text().contains("INS")) {
                    return "https://www.sec.gov" + ((Element)cols.get(2)).select("a").first().attr("href");
                }
                if (((Element)cols.get(3)).text().contains("XML")) {
                    return "https://www.sec.gov" + ((Element)cols.get(2)).select("a").first().attr("href");
                }
            }
        }
        return null;
    }
    
    protected LocalDate getContextDate(Element doc, String contextref, String dateType) throws NullPointerException, DateTimeParseException {
        String dateString = doc.getElementById(contextref).select(dateType).text();
        if (dateString.contentEquals("")) {
            dateString = doc.getElementById(contextref).select("xbrli|" + dateType).text();
        }
        return LocalDate.parse(dateString);
    }
    
    protected int getPeriodScope(Element doc, LocalDate endDate, int periodLength) {
        String fiscalPeriod = doc.select("dei|DocumentFiscalPeriodFocus").text();
        LocalDate periodEndDate;
        try {
            periodEndDate = LocalDate.parse(doc.select("dei|DocumentPeriodEndDate").text());
        }
        catch (DateTimeParseException e) {
            return -1;
        }
        int period;
        if (fiscalPeriod.toUpperCase().contains("Q")) {
            String periodWithoutSpaces = fiscalPeriod.replace(" ", "");
            period = Integer.parseInt(periodWithoutSpaces.substring(periodWithoutSpaces.lastIndexOf("Q") + 1));
        }
        else if (periodEndDate.getMonthValue() == endDate.getMonthValue() && periodLength == 3) {
            period = 4;
        }
        else if (periodEndDate.minusMonths(3L).getMonthValue() == endDate.getMonthValue() && periodLength == 3) {
            period = 3;
        }
        else if (periodEndDate.minusMonths(6L).getMonthValue() == endDate.getMonthValue() && periodLength == 3) {
            period = 2;
        }
        else if (periodEndDate.minusMonths(9L).getMonthValue() == endDate.getMonthValue() && periodLength == 3) {
            period = 1;
        }
        else if (periodEndDate.getMonthValue() == endDate.getMonthValue() && periodLength == 12) {
            period = 0;
        }
        else {
            period = -1;
        }
        return period;
    }
    
    protected int getPeriodYear(Element doc, LocalDate endDate) {
        int periodYear = -1;
        int fiscalYearFocus;
        LocalDate periodEndDate;
        try {
            fiscalYearFocus = Integer.parseInt(doc.select("dei|DocumentFiscalYearFocus").text());
            periodEndDate = LocalDate.parse(doc.select("dei|DocumentPeriodEndDate").text());
        }
        catch (NumberFormatException | DateTimeParseException ex2) {
            return -1;
        }
        int monthsDif = this.getMonths(endDate, periodEndDate);
        if (monthsDif < 12) {
            periodYear = fiscalYearFocus - 0;
        }
        else if (monthsDif < 24) {
            periodYear = fiscalYearFocus - 1;
        }
        else if (monthsDif < 36) {
            periodYear = fiscalYearFocus - 2;
        }
        return periodYear;
    }
    
    protected int getMonths(LocalDate startDate, LocalDate endDate) {
        double days = endDate.getDayOfMonth() - startDate.getDayOfMonth() + 31 * (endDate.getMonthValue() - startDate.getMonthValue()) + 365 * (endDate.getYear() - startDate.getYear());
        int len = (int)Math.round(days / 30.0);
        return len;
    }
    
    protected void populateFilingData(int maxRpts) {
        int rptCount = 0;
        for (FilingLocator d : this.filings) {
            String filingXML = d.getFilingXML();
            if (rptCount >= maxRpts) {
                break;
            }
            if (filingXML == null) {
                continue;
            }
            String html = this.getHTML(filingXML);
            if (html != null) {
                Document doc = Jsoup.parse(html, "", Parser.xmlParser());
                String[] tags;
                for (int length = (tags = this.tags).length, i = 0; i < length; ++i) {
                    String tag = tags[i];
                    Elements elements = doc.getElementsByTag(this.filingTag.getXbrlTag(tag));
                    for (Element e : elements) {
                        try {
                            String contextref = e.attr("contextref");
                            LocalDate startDate = this.getContextDate((Element)doc, contextref, "startDate");
                            LocalDate endDate = this.getContextDate((Element)doc, contextref, "endDate");
                            int months = this.getMonths(startDate, endDate);
                            String val = e.text();
                            this.fc.put(endDate, months, tag, val);
                            this.fc.put(endDate, months, "endDate", endDate.toString());
                            this.fc.put(endDate, months, "startDate", startDate.toString());
                            this.fc.put(endDate, months, "filingDate", d.getFilingDate().toString());
                        }
                        catch (Exception ex) {}
                    }
                }
            }
            ++rptCount;
        }
    }
}