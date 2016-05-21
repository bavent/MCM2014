import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class DataExtractor
{

    public Graph extractGraph(String filename)
        throws FileNotFoundException
    {
        Scanner in = new Scanner(new FileReader(filename));

        HashMap<String, Team> nameToTeam = new HashMap<String, Team>();
        TreeMap<String, Integer> nameToID = new TreeMap<String, Integer>();
        HashSet<GameData> games = new HashSet<GameData>();
        HashSet<String> teamNames = new HashSet<String>();

        // Input all of the game data
        while (in.hasNextLine())
        {
            String line = in.nextLine();
            // Ignore header lines
            if (line.contains("Date"))
                continue;

            GameData data = new GameData(line);
            // If the data was missing fields or bad
            if (!data.valid)
                continue;

            teamNames.add(data.winner);
            teamNames.add(data.loser);

            games.add(data);

        }

        // Only create Team entries for D1 teams
//        if (tokens.length != 10)
//        {
//            valid = false;
//            return;
//        }
        int id = 0;
        for (String name : teamNames)
        {
            int count = 0;
            for (GameData d : games)
            {
                if (d.loser.equals(name) || d.winner.equals(name))
                    count++;
            }

            if (count > 4)
            {
                Team t = new Team(name);
                nameToTeam.put(name, t);
                nameToID.put(name, id++);
            }
        }

        // Only add games between two D1 teams
        for (GameData d : games)
        {

            if (nameToTeam.containsKey(d.winner)
                && nameToTeam.containsKey(d.loser))
            {
                nameToTeam.get(d.winner).addGameData(d);
                nameToTeam.get(d.loser).addGameData(d);
            }
        }

        in.close();
        return new Graph(nameToTeam, nameToID);
    }


    public NickGraph extractNickGraph(String filename)
        throws FileNotFoundException
    {
        File file = new File(filename);
        FileReader f = new FileReader(file);
        Scanner in = new Scanner(f);

        HashMap<String, Team> nameToTeam = new HashMap<String, Team>();
        TreeMap<String, Integer> nameToID = new TreeMap<String, Integer>();
        HashSet<GameData> games = new HashSet<GameData>();
        HashSet<String> teamNames = new HashSet<String>();

        // Input all of the game data
        while (in.hasNextLine())
        {
            String line = in.nextLine();
            // Ignore header lines
            if (line.contains("Date"))
                continue;

            GameData data = new GameData(line);
            // If the data was missing fields or bad
            if (!data.valid)
                continue;

            teamNames.add(data.winner);
            teamNames.add(data.loser);

            games.add(data);

        }

        // Only create Team entries for D1 teams
//            if (tokens.length != 10)
//            {
//                valid = false;
//                return;
//            }
        int id = 0;
        for (String name : teamNames)
        {
            int count = 0;
            for (GameData d : games)
            {
                if (d.loser.equals(name) || d.winner.equals(name))
                    count++;
            }

            if (count > 4)
            {
                Team t = new Team(name);
                nameToTeam.put(name, t);
                nameToID.put(name, id++);
            }
        }

        // Only add games between two D1 teams
        for (GameData d : games)
        {

            if (nameToTeam.containsKey(d.winner)
                && nameToTeam.containsKey(d.loser))
            {
                nameToTeam.get(d.winner).addGameData(d);
                nameToTeam.get(d.loser).addGameData(d);
            }
        }

        in.close();
        return new NickGraph(nameToTeam, nameToID);
    }


    public static void printGraphXML(Graph graph)
    {

        Map<String, Team> teams = graph.nameToTeam;
        Map<String, Integer> set = graph.nameToID;

        // Header information
        System.out
            .println("<gexf xmlns=\"http://www.gexf.net/1.2draft\" version=\"1.2\">");
        System.out
            .println("<graph mode=\"static\" defaultedgetype=\"directed\">");

        // Print the nodes
        System.out.println("<nodes>");
        for (String team : set.keySet())
        {
            int idx = set.get(team);
            System.out.printf("<node id=\"%d\" label=\"%s\" />\n", idx, team);
        }
        System.out.println("</nodes>");

        int idx = 0;
        // Print the edges
        System.out.println("<edges>");
        for (String teamname : teams.keySet())
        {
            Team t = teams.get(teamname);
            int src = set.get(teamname);

            for (GameData d : t.won)
            {
                int target = set.get(d.loser);
                System.out.printf(
                    "<edge id=\"%d\" source=\"%d\" target=\"%d\" />\n",
                    idx++,
                    src,
                    target);
            }

        }
        System.out.println("</edges>");

        System.out.println("</graph>");
        System.out.println("</gexf>");

    }

}
