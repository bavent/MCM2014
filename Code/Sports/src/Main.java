import java.io.FileNotFoundException;

public class Main {

	public static void main(String[] args) throws FileNotFoundException {

		String filename = "../../Data/CFB/GAMES_2002.csv";

		DataExtractor de = new DataExtractor();
		for (int i = 0; i < 100; i++) {
			NickGraph g = de.extractNickGraph(filename);
			// DataExtractor.printGraphXML(g);
			NickGraph reference = de.extractNickGraph(filename);
			g.reference = reference;

			g.generateOrderings();
		}
	}
}
