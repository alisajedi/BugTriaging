package Data;

import bugTriaging.Constants;

public class Scores {
//	0-public double randomScore;
//	1-public double weightedRandomScore;
//	2-public double zeroRScore;
//	
//	3-public double myMetric1; //weightedRandom mixed with recency
//	4-public double myMetric2; //weightedRandom mixed with recency and z-score
	
	public double differentScores[];
	public Scores(){
		differentScores = new double[Constants.TOTAL_NUMBER_OF_METRICS];
		for (int i=0; i<Constants.TOTAL_NUMBER_OF_METRICS; i++)
			differentScores[i] = 0;
	}
}
