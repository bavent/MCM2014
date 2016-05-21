import java.util.HashSet;

// Equivalent to a Vertex
public class Team
    implements Comparable<Team>
{

    String            name;
    HashSet<GameData> won;
    HashSet<GameData> lost;
    int               truewin;
    int               trueloss;


    public Team(String name)
    {
        this.name = name;
        won = new HashSet<GameData>();
        lost = new HashSet<GameData>();
    }


    public void addGameData(GameData d)
    {
        //TODO Ties don't matter. Should we change this?
        if (d.lpoints == d.wpoints)
        {
            return;
        }

        if (d.winner.equals(name))
        {
            won.add(d);
            truewin = won.size();
        }
        else
        {
            lost.add(d);
            trueloss = lost.size();
        }
    }


    @Override
    public int hashCode()
    {
        return name.hashCode();
    }


    @Override
    public boolean equals(Object other)
    {
        Team o = (Team)other;
        return o.name.equals(this.name);
    }


    @Override
    public int compareTo(Team other)
    {
        // Fewer losses is better
        int comp = Integer.compare(this.lost.size(), other.lost.size());

        // If equal, more wins is better
        if (comp == 0)
        {
            comp = -1 * Integer.compare(other.won.size(), this.won.size());
        }

        return comp;
    }


    public String toString()
    {
        return name;
    }

}
