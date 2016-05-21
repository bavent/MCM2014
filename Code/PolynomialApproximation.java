import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

public class PolynomialApproximation
{

    Graph g;


//    public ArrayList<Team> polynomialApproxRank(Graph g)
    public HashSet<GameData> polynomialApproxRank(Graph g)
    {
        this.g = g;

        HashSet<Team> V = g.getVertexSet();
        HashSet<GameData> E = g.getEdgeSet();

        for (GameData d : E)
            d.weight = 1;
        HashSet<GameData> F = new HashSet<GameData>();

        ArrayList<GameData> cycle;

        while ((cycle = findSimpleCycle(V, complement(E, F))) != null)
        {
            GameData smallestEdge = getSmallestEdge(cycle);
            double eps = smallestEdge.weight;

            for (GameData e : cycle)
            {
                e.weight = e.weight - eps;
                if (e.weight == 0)
                {
                    F.add(e);
                }
            }

        }

        System.out.println(containsCycle(V, complement(E, F)));

        int idx = F.size();

        Iterator<GameData> iter = F.iterator();
        while (iter.hasNext())
        {
            idx--;

            System.out.println(idx);
            GameData e = iter.next();
            HashSet<GameData> union = complement(E, F);
            union.add(e);

            if (!containsCycle(V, union))
            {
                iter.remove();
            }
        }

        for (GameData e : E)
        {
            if (F.contains(e))
                g.deleteEdge(e);
        }



        DataExtractor.printGraphXML(g);

        return F;
    }


    public boolean containsCycle(HashSet<Team> V, HashSet<GameData> E)
    {
        return findSimpleCycle(V, E) != null;
    }


    public GameData getSmallestEdge(ArrayList<GameData> E)
    {
        double smallest = Integer.MAX_VALUE;
        GameData res = null;
        for (GameData d : E)
        {
            if (d.weight < smallest)
            {
                smallest = d.weight;
                res = d;
            }
        }
        return res;
    }

    int blah = 0;


    public ArrayList<GameData> findSimpleCycle(
        HashSet<Team> V,
        HashSet<GameData> E)
    {
        blah = 0;

        HashSet<Team> marked = new HashSet<Team>();
        for (Team t : V)
        {
            if (marked.contains(t))
                continue;

//            System.out.println("               " + blah++);

            KEEPADDING = true;
            STOPHERE = null;
            ArrayList<GameData> cycle =
                findSimpleCycle(t, V, E, new HashSet<Team>(), marked);

            if (cycle != null)
                return cycle;
        }

        return null;
    }

    Team    STOPHERE   = null;
    boolean KEEPADDING = true;


    public ArrayList<GameData> findSimpleCycle(
        Team t,
        HashSet<Team> V,
        HashSet<GameData> E,
        HashSet<Team> onStack,
        HashSet<Team> marked)
    {
//        if (marked.contains(t))
//            return null;
        marked.add(t);

        if (onStack.contains(t))
        {
            STOPHERE = t;
            return new ArrayList<GameData>();
        }
        onStack.add(t);

        for (GameData d : t.won)
        {
            if (!E.contains(d))
                continue;

            Team other = g.nameToTeam.get(d.loser);

            ArrayList<GameData> cycle =
                findSimpleCycle(other, V, E, onStack, marked);

            if (cycle != null)
            {

                if (KEEPADDING)
                    cycle.add(d);
                if (STOPHERE.equals(t))
                    KEEPADDING = false;
                return cycle;
            }

        }

        onStack.remove(t);

        return null;
    }


    public HashSet<GameData> complement(HashSet<GameData> E, HashSet<GameData> F)
    {
        HashSet<GameData> res = new HashSet<GameData>();
        for (GameData d : E)
        {
            if (!F.contains(d))
            {
                res.add(d);
            }
        }
        return res;
    }

}
