public class GameData
{

    String  date;
    String  winner;
    String  loser;
    int     wpoints;
    int     lpoints;
    boolean valid;


    public GameData(String line)
    {
        valid = true;
        String[] tokens = line.split(",");
        /*
         * tokens[i]:
         *  0 - Rk
         *  1 - Wk
         *  2 - Date
         *  3 - Day
         *  4 - Winner/Tie
         *  5 - Pts
         *  6 - Nothing?
         *  7 - Lose/Tie
         *  8 - Points
         *  9 - Notes
         */

        date = tokens[2];

        winner = cleanString(tokens[4]);
        if (tokens[5].isEmpty())
        {
            valid = false;
            return;
        }
        wpoints = Integer.parseInt(tokens[5]);

        loser = cleanString(tokens[7]);
        if (tokens[8].isEmpty())
        {
            valid = false;
            return;
        }

        lpoints = Integer.parseInt(tokens[8]);

    }


    public String cleanString(String in)
    {
        if (in.charAt(0) != '(')
            return in;

        int closing = in.indexOf(')');

        return in.substring(closing + 2);
    }


    @Override
    public int hashCode()
    {
        return winner.hashCode() ^ loser.hashCode();
    }


    public boolean equals(Object o)
    {
        GameData other = (GameData)o;
        return other.date.equals(date) && other.winner.equals(winner)
            && other.loser.equals(loser) && other.wpoints == wpoints
            && other.lpoints == lpoints;
    }


    public String toString()
    {
        return String.format(
            "%s %d : %s %d on %s\n",
            winner,
            wpoints,
            loser,
            lpoints,
            date);
    }

}
