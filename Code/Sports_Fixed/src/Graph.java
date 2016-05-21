import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Graph
{

    Map<String, Team>    nameToTeam;
    Map<String, Integer> nameToID;
    Graph                reference = null;


    public Graph(Map<String, Team> n, Map<String, Integer> id)
    {
        this.nameToTeam = n;
        this.nameToID = id;
    }


    public int countInversions(ArrayList<Team> ordering)
    {
        int inv = 0;

        HashSet<Team> seen = new HashSet<Team>();

        ArrayList<Team> originalTeams = new ArrayList<Team>();
        for (Team t : ordering)
        {
            originalTeams.add(nameToTeam.get(t.name));
        }

        for (Team t : originalTeams)
        {
            for (GameData d : t.won)
            {
                String loserName = d.loser;
                Team loser = nameToTeam.get(loserName);

                if (seen.contains(loser))
                {
                    inv++;
                }
            }

//            for (GameData d : t.lost)
//            {
//                String winnerName = d.winner;
//                Team winner = nameToTeam.get(winnerName);
//
//                if (!seen.contains(winner))
//                {
//                    inv++;
//                }
//            }

            seen.add(t);
        }

        return inv;

    }


    public void generateOrderings()
    {
        ArrayList<Team> teams = new ArrayList<Team>();

        for (Team t : nameToTeam.values())
        {
            teams.add(t);
        }

        generateOrderings(teams, new ArrayList<Team>());
    }

    int     lastInversion = 0;
    boolean error         = false;


    private String printResult(ArrayList<Team> result)
    {
        StringBuilder res = new StringBuilder();
        int rank = 1;
        for (Team t : result)
        {
            res.append(String.format("%d: %s\n", rank++, t));
        }
        return res.toString();
    }



    /*
     * Runs through all the possible valid orderings (Using our greedy 
     * algorithm).
     * 
     * 1. Find the set of best teams (teams with the fewest incoming edges)
     * 2. Find the set of candidates from within this set:
     *      - Must have the fewest internal losses
     *      - May be more than one
     * 3. For each of these, select it as the best, remove it from the
     *    list, as well as its outgoing edges, and recurse
     */
    public void generateOrderings(ArrayList<Team> teams, ArrayList<Team> result)
    {
        // Print and store the results of this ordering
        if (teams.size() == 0)
        {
            int inversions = reference.countInversions(result);

            System.out.println(inversions);
            return;
        }

        // Collect the teams with fewest losses
        ArrayList<Team> fewestLosses = new ArrayList<Team>();
        int fewestIn = Integer.MAX_VALUE;
        for (Team t : teams)
        {
            if (t.lost.size() < fewestIn)
            {
                fewestLosses.clear();
                fewestLosses.add(t);
                fewestIn = t.lost.size();
            }
            else if (t.lost.size() == fewestIn)
            {
                fewestLosses.add(t);
            }
        }

        // From this set, find the 'best' candidates
        ArrayList<Team> candidates = findBestest(fewestLosses);
        int size = candidates.size();
        while (true)
        {
            candidates = findBestest(candidates);
            if (size == candidates.size())
                break;
            size = candidates.size();

//            System.out.println("Reducing");
        }

        // Try each of these candidates & recurse
        for (Team t : candidates)
        {
            teams.remove(t);
            result.add(t);

            // Remove wins
            HashSet<GameData> wins = t.won;

            for (GameData edge : wins)
            {
                String loser = edge.loser;
                nameToTeam.get(loser).lost.remove(edge);
            }

            // Recurse
            generateOrderings(teams, result);

            // Re-add wins
            for (GameData edge : wins)
            {
                String loser = edge.loser;
                nameToTeam.get(loser).lost.add(edge);
            }

            teams.add(t);
            result.remove(t);
        }

    }


    /*
     * Given a set of teams with equal numbers of losses,
     * returns a list of the teams with 
     * the fewest internal losses. 
     */
    private ArrayList<Team> findBest(ArrayList<Team> teams)
    {

        ArrayList<Team> bestTeams = new ArrayList<Team>();
        int fewestLosses = Integer.MAX_VALUE;

        // For each team
        for (int i = 0; i < teams.size(); i++)
        {
            Team candidate = teams.get(i);
            int mylosses = 0;

            // For each opponent
            for (int j = 0; j < teams.size(); j++)
            {
                if (i == j)
                    continue;

                // How many of our losses are to them?
                for (GameData loss : candidate.lost)
                {
                    if (teams.get(j).won.contains(loss))
                        mylosses++;
                }

            }
            if (mylosses < fewestLosses)
            {
                bestTeams.clear();
                bestTeams.add(candidate);
                fewestLosses = mylosses;
            }
            else if (mylosses == fewestLosses)
            {
                bestTeams.add(candidate);
            }
        }

        int size = bestTeams.size();

        return bestTeams;

    }


    // Now uses most internal wins
    private ArrayList<Team> findBestest(ArrayList<Team> teams)
    {

        ArrayList<Team> bestTeams = new ArrayList<Team>();
        int mostWins = -1;

        // For each team
        for (int i = 0; i < teams.size(); i++)
        {
            Team candidate = teams.get(i);
            int mywins = 0;

            // For each opponent
            for (int j = 0; j < teams.size(); j++)
            {
                if (i == j)
                    continue;

                // How many of our wins are to them
                for (GameData win : candidate.won)
                {
                    if (teams.get(j).lost.contains(win))
                        mywins++;
                }

            }
            if (mywins > mostWins)
            {
                bestTeams.clear();
                bestTeams.add(candidate);
                mostWins = mywins;
            }
            else if (mywins == mostWins)
            {
                bestTeams.add(candidate);
            }
        }

        return bestTeams;
    }

}
