import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.PrintWriter;

public class BasketballGamesParser {	
    
	static final int latestYear = 2013;
    static final int earliestYear = 2013;
	
	/*
     * Gets basketball data across specified years
     */
	public static void main(String[] args) throws IOException {
    	String conferencesUrlTemplate = "http://www.sports-reference.com/cbb/seasons/";
    	
    	for (int i = latestYear; i >= earliestYear; i--) {
    		System.out.print("Getting data from " + i + "...");
    		
    		String conferencesUrl = conferencesUrlTemplate + i + ".html";
	    	
    		String seasonData = getConferencesData(conferencesUrl);
	    	
    		PrintWriter writer = new PrintWriter("GAMES_" + i + ".txt", "UTF-8");
    		writer.print(seasonData);
    		writer.close();
    		
    		System.out.println("done.");
    	}
    }
    
	/*
	 * For a given year, iterate over all conferences and aggregate their games
	 */
	public static String getConferencesData(String url) throws IOException {
		StringBuilder seasonString = new StringBuilder();
		Document doc = Jsoup.connect(url).get();
		
		Element table = doc.select("table[id=conferences]").first().select("tbody").first();
		
        Elements rows = table.select("tr");

        for (Element row : rows) {
        	Element link = row.select("a[href]").first();
            
        	String conferenceName = link.text(); 
        	String scheduleURL = link.attr("abs:href");
        	scheduleURL = scheduleURL.trim().substring(0, scheduleURL.length()-5) + "-schedule.html";
            
        	seasonString.append(getScheduleData(scheduleURL, conferenceName));
        }
        
        return seasonString.toString();
	}
    
	/*
	 * For a given conference, get all games played
	 */
	public static String getScheduleData(String url, String conferenceName) throws IOException {
        System.out.print("from " + conferenceName + "...");
		StringBuilder scheduleString = new StringBuilder();
		Document doc = Jsoup.connect(url).get();
        
		Element table = null;
		
		try {
			table = doc.select("table[id=schedule]").first().select("tbody").first();
		}
		catch (Exception e) {
			System.out.print("FAILED!\n...");
			return "";
		}
		
        Elements rows = table.select("tr");
        
        for (Element row : rows) {
        	Elements cells = row.select("td");
            
        	scheduleString.append(conferenceName + "^");
            for (Element cell : cells) {
            	scheduleString.append(cell.text() + "^");
            }
            scheduleString.append("\n");        	
        }
        
        return csvFormat(scheduleString.toString());
	}
    
	/*
	 * Convert my shitty CSV work-around into CSV
	 */
	public static String csvFormat(String s) {
		s = s.replace(',',' ');
		s = s.replace('^',',');
		
		return s;
	}
}