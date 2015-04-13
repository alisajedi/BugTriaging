package Data;

public class CommunityMember {
	public CommunityMember(CommunityMember otherCommunityMember){
		this.mySQLProjectId = otherCommunityMember.mySQLProjectId;
		this.ownerLoginAndProjectName = otherCommunityMember.ownerLoginAndProjectName;
		this.SOId = otherCommunityMember.SOId;
		this.ghLogin = otherCommunityMember.ghLogin;
		this.ghMySQLId = otherCommunityMember.ghMySQLId;
		this.ghMongoDBId = otherCommunityMember.ghMongoDBId;
	}//end of constructor.
	public CommunityMember(){
		super();
	}
	public String mySQLProjectId;
	public String ownerLoginAndProjectName;
	public String SOId;
	public String ghLogin;
	public String ghMySQLId;
	public String ghMongoDBId;
	
	public int intersection_A;
	public int intersection_AQ;
	public int intersection_A_score;
	public int intersection_AQ_score;
	
	public int intersection_Q;
	public double intersection_Q_score; //Alaki.
	public double intersection_z_score;
	
	public int totalAnswers;
	public int totalQuestions;
	public double traditional_z_score;
	
	//For greedy approaches, based on only previous assignments:
	public int numberOfAssignmentsUpToNow;
	public int randomScore_zeroOrOne; 
	public int weightedRandomScore_count;
	public int zeroRScore_zeroOrOne;
	
	public double combinedScore1;
	public double combinedScore2;
}
