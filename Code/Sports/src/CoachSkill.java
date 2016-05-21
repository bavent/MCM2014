import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Scanner;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.stat.regression.SimpleRegression;

public class CoachSkill {
	
	static final double alpha = 0.01;	// mean confidence interval
	static final double tAlpha = 0.01;	// mean confidence interval
	
	static final String sportAbrv = "CFB";
	//static final String sportAbrv = "CBBM";
	//static final String sportAbrv = "Bitches";
	
	static final int lowestYear = 2002;
	static final int highestYear = 2013;
	
	static final String filePathTemplate = "../../Data/" + sportAbrv + "/Team Rank/DATA_"; 
	
	
	public static void main(String args[]) throws FileNotFoundException, UnsupportedEncodingException {
		PrintWriter writer;
		
		// Aggregate all coach stats from file
		HashMap<Integer, Integer> teamsThisYear = new HashMap<Integer, Integer>();
		HashMap<String, Team> teams = new HashMap<String, Team>();
		HashMap<String, ArrayList<CoachSeason>> coachStats = generateCoachStats(filePathTemplate, teamsThisYear, teams);		
		
		
		// Add normalized rank into the coaches stats for every coach for every year
		for (ArrayList<CoachSeason> list : coachStats.values()) {
			for (CoachSeason cs : list) {
				int year = cs.year;
				int rankThatYear = cs.rank;
				int numTeamsThatYear = teamsThisYear.get(year);
				
				double normedRank = (numTeamsThatYear - rankThatYear) / (double)(numTeamsThatYear - 1); 
				cs.normedRank = normedRank;
				
				Team currentTeam = teams.get(cs.teamName);
				for (CoachRankYear cry : currentTeam.coachRankYears) {
					if (cry.year == year) {
						cry.normedRank = normedRank;
						break;
					}
				}
			}
		}
		
		
		// Find single average ranking variance for all coaches
		HashMap<String, Double> coachSampleMeans = new HashMap<String, Double>();
		double var = getAvgVariance(coachStats, coachSampleMeans);
		
		
		// Map coaches to their confidence interval rankings
		HashMap<String, MeanInterval> coachIntervals = new HashMap<String, MeanInterval>();
		for (String coach : coachStats.keySet()) {
			double sampleMean = coachSampleMeans.get(coach);
			NormalDistribution d = new NormalDistribution(sampleMean, Math.sqrt(var / coachStats.get(coach).size()));
			double lower = d.inverseCumulativeProbability(alpha);
			double upper = d.inverseCumulativeProbability(1 - alpha);
			coachIntervals.put(coach, new MeanInterval(lower, upper));
		}
		
		ArrayList<CoachInterval> allIntervals = new ArrayList<CoachInterval>();
		for (String coach : coachStats.keySet()) {
			MeanInterval interval = coachIntervals.get(coach);
			CoachInterval ci = new CoachInterval(coach, interval);
			
			allIntervals.add(ci);
		}
		Collections.sort(allIntervals);
    	
		
		// Write absolute ranking results to file
		writer = new PrintWriter("AbsoluteRankings_" + sportAbrv + ".txt", "UTF-8");
		for(CoachInterval ci : allIntervals) {
			writer.println((ci.coach + ": (" + ci.interval.lower + ", " + ci.interval.upper + ") -- " + coachStats.get(ci.coach).size()));
			//System.out.println(ci.coach + ": (" + ci.interval.lower + ", " + ci.interval.upper + ") -- " + coachStats.get(ci.coach).size());
		}
		writer.close();
		

		///////////////////////////////////////////////////////////////////////////////////////
		
		
		HashMap<String, ArrayList<RankPair>> allCoachRankPairs = new HashMap<String, ArrayList<RankPair>>(); 
		
		boolean writeRdR = false; 		
		if (writeRdR)	writer = new PrintWriter("r_dr.csv", "UTF-8");
		
		ArrayList<RankPair> allRankPairs = new ArrayList<RankPair>();
		for (String team : teams.keySet()) {
			ArrayList<CoachRankYear> coachRankYears = teams.get(team).coachRankYears;
			Collections.sort(coachRankYears);
			
			for (int i = 1; i < coachRankYears.size(); i++) {
				CoachRankYear currentCry = coachRankYears.get(i);
				CoachRankYear previousCry = coachRankYears.get(i-1);
				
				int currentYear = currentCry.year;
				int previousYear = previousCry.year;
				if (currentYear != previousYear + 1)	continue;
				
				double currentRank = currentCry.normedRank;
				double previousRank = previousCry.normedRank;

				double deltaRank = currentRank - previousRank;
				
				RankPair rp = new RankPair(previousRank, deltaRank);
				if (writeRdR)	writer.print(rp.r + ", " + rp.dr + "\n");
				allRankPairs.add(rp);
				
				RankPair rpPoint = new RankPair(previousRank, deltaRank);
				ArrayList<RankPair> rankPairs = allCoachRankPairs.get(currentCry.coach);
				if (rankPairs == null) {
					rankPairs = new ArrayList<RankPair>();
					allCoachRankPairs.put(currentCry.coach, rankPairs);
				}
				rankPairs.add(rpPoint);
			}
		}
		if (writeRdR)	writer.close();
		
		double[][] rankPairs = new double[allRankPairs.size()][2];
		for (int i = 0; i < allRankPairs.size(); i++) {
			RankPair rp = allRankPairs.get(i);
			rankPairs[i][0] = rp.r;
			rankPairs[i][1] = rp.dr;
		}
		SimpleRegression fit = new SimpleRegression();
		fit.addData(rankPairs);
		
		double f = fit.getSlope();
		
		HashMap<String, ArrayList<Double>> allCoachCVals = new HashMap<String, ArrayList<Double>>();
		
		for (String coachName : allCoachRankPairs.keySet()) {
			ArrayList<RankPair> coachRankPairs = allCoachRankPairs.get(coachName);
			if (coachName.equals("Red Cagle [235]")) {
				System.out.println();
			}
			ArrayList<Double> cVals = new ArrayList<Double>();
			for (RankPair rp : coachRankPairs) {
				double cVal = rp.dr - f*rp.r;
				cVals.add(cVal);
			}
			allCoachCVals.put(coachName, cVals);
		}
		
		// Find single average ranking variance for all coaches
		HashMap<String, Double> coachTimeSampleMeans = new HashMap<String, Double>();
		double timeVar = getTimeAvgVariance(allCoachCVals, coachTimeSampleMeans);
		
		// Map coaches to their confidence interval rankings
		HashMap<String, MeanInterval> timeCoachIntervals = new HashMap<String, MeanInterval>();
		for (String coach : allCoachCVals.keySet()) {
			double sampleMean = coachTimeSampleMeans.get(coach);
			NormalDistribution d = new NormalDistribution(sampleMean, Math.sqrt(timeVar / allCoachCVals.get(coach).size()));
			double lower = d.inverseCumulativeProbability(tAlpha);
			double upper = d.inverseCumulativeProbability(1 - tAlpha);
			timeCoachIntervals.put(coach, new MeanInterval(lower, upper));
		}
		
		ArrayList<CoachInterval> timeAllIntervals = new ArrayList<CoachInterval>();
		for (String coach : coachTimeSampleMeans.keySet()) {
			MeanInterval interval = timeCoachIntervals.get(coach);
			CoachInterval ci = new CoachInterval(coach, interval);
			timeAllIntervals.add(ci);
		}
		Collections.sort(timeAllIntervals);
    	
		// Write time ranking results to file
		writer = new PrintWriter("TimeRankings_" + sportAbrv + ".txt", "UTF-8");
		for(CoachInterval ci : timeAllIntervals) {
			writer.println(ci.coach + ": (" + ci.interval.lower + ", " + ci.interval.upper + ") -- " + coachStats.get(ci.coach).size());
			//System.out.println(ci.coach + ": (" + ci.interval.lower + ", " + ci.interval.upper + ") -- " + coachStats.get(ci.coach).size());
		}
		writer.close();
		

		
		ArrayList<CoachInterval> finalRank = new ArrayList<CoachInterval>();

		
		for (int i = 0; i < allIntervals.size(); i++) {

			String coach = allIntervals.get(i).coach;
			
			int j;
			for (j = 0; j < timeAllIntervals.size(); j++)
			{
				if (timeAllIntervals.get(j).coach.equals(coach))
					break;
			}
			
			int rank = (allIntervals.size() - i) + (timeAllIntervals.size() - j);
			
			CoachInterval ci = new CoachInterval(coach, new MeanInterval(rank, 0));
			
			finalRank.add(ci);
		}
		
		Collections.sort(finalRank);
		//Collections.reverse(finalRank);
		
		writer = new PrintWriter("FinalRankings_" + sportAbrv + ".txt", "UTF-8");
		for (CoachInterval ci : finalRank) {
			//System.out.println(ci.coach + ", " + ci.interval.lower);
			writer.println(ci.coach + ", " + ci.interval.lower);
		}
		writer.close();
		
		
	}
	
	
	// Time series method for variance
	public static double getTimeAvgVariance(
			HashMap<String, ArrayList<Double>> allCoachCVals,
			HashMap<String, Double> coachTimeSampleMeans) {
		
		double var;
		
		HashMap<String, Double> coachTimeSampleVariance = new HashMap<String, Double>();
		for (String coachName : allCoachCVals.keySet()) {
			ArrayList<Double> list = allCoachCVals.get(coachName);
			
			// find mean of this list
			double mean = 0;
			for (Double cVal : list) {
				mean += cVal;
			}
			mean /= list.size();
			coachTimeSampleMeans.put(coachName, mean);
			
			// find sample variance of this list
			double sampleVar = 0;
			for (Double cVal : list) {
				sampleVar += ((mean - cVal) * (mean - cVal));
			}
			if (list.size() != 1) {
				sampleVar /= (list.size() - 1);
			}
			else {
				sampleVar = 0;
			}
			
			// store sample var
			coachTimeSampleVariance.put(coachName, sampleVar);
			
		}
		
		// pooled variance algorithm
		double numerator = 0;
		double denominator = -allCoachCVals.keySet().size();
		for (String coach : allCoachCVals.keySet()) {
			int n = allCoachCVals.get(coach).size();
			double sampleVar = coachTimeSampleVariance.get(coach);
			
			numerator += ((n - 1) * sampleVar);
			denominator += n;
		}
		var = numerator / denominator;
		
		return var;
	}
	
	// Returns the pooled variance; also populates the coachSampleMeans Map
	public static double getAvgVariance(HashMap<String, ArrayList<CoachSeason>> coachStats, HashMap<String, Double> coachSampleMeans) {
		double var;
		
		HashMap<String, Double> coachSampleVariance = new HashMap<String, Double>();
		for (ArrayList<CoachSeason> list : coachStats.values()) {
			
			// find mean of this list
			double mean = 0;
			for (CoachSeason cs : list) {
				mean += cs.normedRank;
			}
			mean /= list.size();
			coachSampleMeans.put(list.get(0).coachName, mean);
			
			// find sample variance of this list
			double sampleVar = 0;
			for (CoachSeason cs : list) {
				sampleVar += ((mean - cs.normedRank) * (mean - cs.normedRank));
			}
			if (list.size() != 1) {
				sampleVar /= (list.size() - 1);
			}
			else {
				sampleVar = 0;
			}
			
			// store sample var
			coachSampleVariance.put(list.get(0).coachName, sampleVar);
			
		}
		
		double numerator = 0;
		double denominator = -coachStats.keySet().size();
		for (String coach : coachStats.keySet()) {
			int n = coachStats.get(coach).size();
			double sampleVar = coachSampleVariance.get(coach);
			
			numerator += ((n - 1) * sampleVar);
			denominator += n;
		}
		var = numerator / denominator;
		
		return var;
	}
	
	// Map coaches to all their seasonal data over all years
	public static HashMap<String, ArrayList<CoachSeason>> generateCoachStats(
			String fileTemplate, 
			HashMap<Integer, Integer> teamsThisYear, 
			HashMap<String, Team> teams) throws FileNotFoundException{
		
		HashMap<String, ArrayList<CoachSeason>> coachStats = new HashMap<String, ArrayList<CoachSeason>>(); 
		
		for (int i = lowestYear; i <= highestYear; i++) {
			Scanner in = new Scanner(new File(fileTemplate + i + ".csv"));
	        in.useDelimiter(",");
			in.nextLine(); // skip header
			
			int numTeams = 0;
			while (in.hasNextLine()) {
				numTeams++;
				
				String[] line = in.nextLine().split(",");
				String teamName = line[0];
				String coachName =  line[1];
				int rank = Integer.parseInt(line[7]);
				
				CoachSeason cs = new CoachSeason(coachName, teamName, i, rank);
				Team team = teams.get(teamName);
				if (team == null) {
					team = new Team(teamName);
					teams.put(teamName, team);
				}
				team.coachRankYears.add(new CoachRankYear(coachName, rank, i));
				
				ArrayList<CoachSeason> currentList = coachStats.get(coachName);
				if (currentList == null) {
					currentList = new ArrayList<CoachSeason>();
					coachStats.put(coachName, currentList);
				}
				currentList.add(cs);
				
			}
			
			in.close();
			
			teamsThisYear.put(i, numTeams);
		}
		
		return coachStats;
	}
	
	public static class CoachRankYear implements Comparable<CoachRankYear> {
		String coach;
		int rank;
		int year;
		double normedRank;
		double deltaRank;

		public CoachRankYear(String c, int r, int y) {
			coach = c;
			rank = r;
			year = y;
		}

		@Override
		public int compareTo(CoachRankYear other) {
			return (this.year - other.year);
		}
	}
	
	public static class RankPair {
		double r;
		double dr;
		
		public RankPair(double r, double dr) {
			this.r = r;
			this.dr = dr;
		}
	}
	
	public static class CoachInterval implements Comparable<CoachInterval>{
		String coach;
		MeanInterval interval;
		
		public CoachInterval(String c, MeanInterval i) {
			coach = c;
			interval = i;
		}

		@Override
		public int compareTo(CoachInterval other) {
			return Double.compare(this.interval.lower, other.interval.lower);	// best teams
			//return -Double.compare(this.interval.upper, other.interval.upper);	// worst teams
		}
		
	}
	
	public static class MeanInterval {
		double lower, upper;
		
		public MeanInterval(double l, double u) {
			lower = l;
			upper = u;
		}
	}
	
	public static class Team {
		String teamName;
		ArrayList<CoachRankYear> coachRankYears;
		
		public Team(String n) {
			teamName = n;
			coachRankYears = new ArrayList<CoachRankYear>();
		}
	}
	
	public static class CoachSeason {
		String coachName;
		String teamName;
		int year;
		int rank; // ApproxRank
		int deltaRank;
		double normedRank;
		
		public CoachSeason(String c, String t, int y, int r) {
			coachName = c;
			teamName = t;
			year = y;
			rank = r;			
		}
	}
}
