import java.util.HashMap;
import java.util.Map;

public class FilingTag {

	private Map<String, String[]> SupportedTags = new HashMap<String, String[]>();

	public FilingTag() {

		// format is shortname as key, then long description as value0 and xbrl tag as
		// value1
		SupportedTags.put("eps", new String[] { "earnings per share", "us-gaap:EarningsPerShareBasic" });
		SupportedTags.put("epsd", new String[] { "diluted earnings per share", "us-gaap:EarningsPerShareDiluted" });
		SupportedTags.put("income", new String[] { "income", "us-gaap:NetIncomeLoss" });

	}

	public String getXbrlTag(String shortTag) {
		// this suits as a container to allow short abbreviations to be translated into
		// the longer XBRL tags

		if (SupportedTags.containsKey(shortTag)) {
			return SupportedTags.get(shortTag)[1];
		} else {
			return null;
		}
	}

	// Returns an array of string descriptions that represent the supported SEC tags
	public String[] getArrayOfSupportedTagDescriptions() {

		return (String[]) SupportedTags.values().toArray()[0];
	}

	// Returns an array of the string tags that represent the supported SEC tags
	public String[] getArrayOfSupportedTags() {
		return SupportedTags.keySet().toArray(new String[SupportedTags.size()]);
	}

	// Checks to see if the requested string tag is supported by the FilingProcessor
	public boolean checkTagSupported(String tg) {
		return SupportedTags.containsKey(tg);
	}

	// Get the full description from the tag name (string)
	public String getFullTagDescription(String tg) {
		return SupportedTags.get(tg)[0];
	}

	public String getFormattedStringOfSupportedTags() {

		String formatted = "";

		for (String key : SupportedTags.keySet()) {
			formatted = formatted + System.lineSeparator() + "[" + key + "] " + SupportedTags.get(key)[0];
		}

		return formatted;
	}

}
