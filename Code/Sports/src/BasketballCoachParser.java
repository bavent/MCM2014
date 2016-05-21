import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.PrintWriter;

public class BasketballCoachParser {	

    static int coachId = 1;
	
	/*
     * Gets basketball data across specified years
     */
	public static void main(String[] args) throws IOException {
    	StringBuilder coachesData = new StringBuilder();
		
		String coachesUrlTemplate = "http://www.sports-reference.com/cbb/coaches/";
    	
    	for (char initLetter = 'a'; initLetter <= 'z'; initLetter++) {
    		if (initLetter == 'x') continue;
    		
    		System.out.print("Getting data for last names '" + Character.toUpperCase(initLetter) + "'...");
    		
    		String coachesUrl = coachesUrlTemplate + initLetter + "-index.html";
    		
    		coachesData.append(getCoachesData(coachesUrl));
    		
    		System.out.println("done.");
    	}
    		
    	PrintWriter writer = new PrintWriter("COACHES.txt", "UTF-8");
		writer.print(coachesData.toString());
		writer.close();
    }
	
	public static String getCoachesData(String url) throws IOException {
		StringBuilder coachesString = new StringBuilder();
		Document doc = Jsoup.connect(url).get();
		
		Element table = doc.select("table[id=coaches]").first().select("tbody").first();
		
        Elements rows = table.select("tr");
        
        for (Element row : rows) {
        	Element link = row.select("a[href]").first();
            if (link == null) continue;
        	
        	String coachName = link.text();
        	
        	String coachUrl = link.attr("abs:href");
        	//coachUrl = coachUrl.trim().substring(0, coachUrl.length()-5) + "-schedule.html";
            
        	coachesString.append(getCoachData(coachUrl, coachName + " [" + coachId + "]"));
        	coachId++;
        }
        
        return coachesString.toString();
	}
	
	public static String getCoachData(String url, String name) throws IOException {
		StringBuilder coachString = new StringBuilder();
		Document doc = Jsoup.connect(url).get();
        
		Element table = null;
		
		try {
			table = doc.select("table[id=stats]").first().select("tbody").first();
		}
		catch (Exception e) {
			System.out.print("FAILED!\n...");
			return "";
		}
		
        Elements rows = table.select("tr");
        
        for (Element row : rows) {
        	Elements cells = row.select("td");
            
        	coachString.append(name + "^");
            for (Element cell : cells) {
            	Element link = cell.select("a[href]").first();
                if (link == null) {
                	coachString.append(cell.text() + "^");
                }
                else {
                	coachString.append(link.text() + "^");
                }
            }
            coachString.append("\n");        	
        }
        
        return csvFormat(coachString.toString());
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