import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

public class NickGraph {

	Map<String, Team> nameToTeam;
	Map<String, Integer> nameToID;
	NickGraph reference = null;

	public NickGraph(Map<String, Team> n, Map<String, Integer> id) {
		this.nameToTeam = n;
		this.nameToID = id;
	}

	public int countInversions(ArrayList<Team> ordering) {
		int inv = 0;

		HashSet<Team> seen = new HashSet<Team>();

		ArrayList<Team> originalTeams = new ArrayList<Team>();
		for (Team t : ordering) {
			originalTeams.add(nameToTeam.get(t.name));
		}

		for (Team t : originalTeams) {

			for (GameData d : t.won) {
				String loserName = d.loser;
				Team loser = nameToTeam.get(loserName);

				if (seen.contains(loser)) {
					inv++;
				}
			}

			// for (GameData d : t.lost) {
			// String winnerName = d.winner;
			// Team winner = nameToTeam.get(winnerName);
			//
			// if (!seen.contains(winner)) {
			// inv++;
			// }
			// }

			seen.add(t);
		}

		return inv;

	}

	public void generateOrderings() {
		ArrayList<Team> teams = new ArrayList<Team>();
		for (Team t : nameToTeam.values()) {
			teams.add(t);
		}

		ArrayList<Team> ranking = rank(teams);

		int i = 1;
		for (Team t : ranking) {
			// System.out.println("Rank " + (i++) + " is " + t.toString() +
			// "  ............ record " + t.truewin + " - " + t.trueloss);
		}

		System.out.println("Number of inversions is "
				+ reference.countInversions(ranking));
	}

	public ArrayList<Team> rank(ArrayList<Team> unrankedTeams) {

		// Assumes unrankedTeams only contains game data for other teams in the
		// set

		ArrayList<Team> ranking = new ArrayList<Team>();
		System.out.println(" ===== Calling rank for unranked list of size "
				+ unrankedTeams.size());

		Team bestTeam = rankOne(unrankedTeams);
		removeFromGraph(bestTeam);
		ranking.add(bestTeam);
		unrankedTeams.remove(bestTeam);

		// Recurse to rank the next team
		if (unrankedTeams.size() > 0) {
			ranking.addAll(rank(unrankedTeams));
		}

		return ranking;
	}

	public void removeFromGraph(Team t) {

		// System.out.println("Removing team " + t);

		// Remove all the games for which t was the loser
		for (Team oTeam : nameToTeam.values()) {
			Iterator<GameData> it = oTeam.won.iterator();
			while (it.hasNext()) {
				GameData g = it.next();
				if (nameToTeam.get(g.loser).equals(t)) {
					// System.out.println("Removing loss game " + g.toString());
					it.remove();
				}
			}
		}

		// Remove all the games for which t was the winner
		for (Team oTeam : nameToTeam.values()) {
			Iterator<GameData> it = oTeam.lost.iterator();
			while (it.hasNext()) {
				GameData g = it.next();
				if (nameToTeam.get(g.winner).equals(t)) {
					// System.out.println("Removing win game " + g.toString());
					it.remove();
				}
			}
		}

		// Remove t
		nameToTeam.remove(t.name);

	}

	public Team rankOne(ArrayList<Team> unrankedTeams) {

		// DOES NOT have the precondition that only games involving
		// unrankedTeams are listed

		System.out.println("---> Calling rankOne for unranked list of size "
				+ unrankedTeams.size());

		// Build the set of teams with the fewestIns (S_l)
		ArrayList<Team> fewestIns = new ArrayList<Team>();
		int fewestIn = Integer.MAX_VALUE;
		for (Team unrankedTeam : unrankedTeams) {
			// Count just the losses against other unrankedTeams so that this is
			// branch-recursion safe.
			int nInteralLosses = 0;
			for (GameData loss : unrankedTeam.lost) {
				if (unrankedTeams.contains(nameToTeam.get(loss.winner))) {
					nInteralLosses++;
				}
			}

			if (nInteralLosses < fewestIn) {
				fewestIn = nInteralLosses;
				fewestIns.clear();
				fewestIns.add(unrankedTeam);
			} else if (nInteralLosses == fewestIn) {
				fewestIns.add(unrankedTeam);
			}
		}

		// Check whether all of these have the same number of internal incoming
		// edges
		boolean sameIn = true;
		int inNum = -1;
		for (Team fewestTeam : fewestIns) {
			int thisInNum = 0;
			for (GameData g : fewestTeam.lost) {
				if (fewestIns.contains(nameToTeam.get(g.winner))) {
					thisInNum++;
				}
			}
			if (inNum == -1) {
				inNum = thisInNum;
			} else if (thisInNum != inNum) {
				sameIn = false;
			}
		}

		System.out.println("---> Fewest list contains " + fewestIns.size()
				+ " teams and sameIn = " + sameIn + "  with inNum = " + inNum);

		// Id they are all the same here we sort by outgoing edges
		// otherwise we solve that subproblem recursively
		if (sameIn) {

			ArrayList<Team> mostOuts = new ArrayList<Team>();
			int mostOut = -1;
			for (Team fewestInTeam : fewestIns) {
				// Count just the losses against other unrankedTeams so that
				// this is branch-recursion safe.
				int nInteralWins = 0;
				for (GameData win : fewestInTeam.won) {
					if (fewestIns.contains(nameToTeam.get(win.loser))) {
						nInteralWins++;
					}
				}

				if (nInteralWins > mostOut) {
					mostOut = nInteralWins;
					mostOuts.clear();
					mostOuts.add(fewestInTeam);
				} else if (nInteralWins == mostOut) {
					mostOuts.add(fewestInTeam);
				}
			}

			// Check whether all of these have the same number of internal
			// outgoing edges
			boolean sameOut = true;
			int outNum = -1;
			for (Team mostTeam : mostOuts) {
				int thisOutNum = 0;
				for (GameData g : mostTeam.won) {
					if (mostOuts.contains(nameToTeam.get(g.loser))) {
						thisOutNum++;
					}
				}
				if (outNum == -1) {
					outNum = thisOutNum;
				} else if (thisOutNum != outNum) {
					sameOut = false;
				}
			}

			if (sameOut) {

				System.out.println("--- Making random choice:");
				System.out.println("---    fewestIn.size() = "
						+ fewestIns.size());
				System.out.println("---    mostOuts.size() = "
						+ mostOuts.size());

				Random r = new Random();
				return mostOuts.get(r.nextInt(mostOuts.size())); // Chose
			} else {
				return rankOne(mostOuts);
			}

		} else {
			return rankOne(fewestIns);
		}

	}

}
