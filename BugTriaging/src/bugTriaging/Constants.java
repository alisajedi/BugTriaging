package bugTriaging;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Date;
//import java.util.HashSet;
import java.util.List;
import java.util.Map;
//import java.util.Random;


import com.google.common.collect.ImmutableMap;
//import com.google.gwt.thirdparty.guava.common.collect.ImmutableMap;

public class Constants { 
	//GH - MongoDB:
    public static final String DATASET_DIRECTORY_GH_JSON_SEPARATE = "C:\\2-Study\\BugTriaging\\Data Sets\\1- GH\\GH-GHTorrent-Mongodb\\1-Separate json files";
    public static final String DATASET_EXTERNAL_DIRECTORY_GH_JSON_SEPARATE = "D:\\BugTriaging\\Data Sets\\1- GH\\GH-GHTorrent-Mongodb\\1-Separate json files";
    
    public static final String DATASET_DIRECTORY_GH_JSON_MERGED = "C:\\2-Study\\BugTriaging\\Data Sets\\1- GH\\GH-GHTorrent-Mongodb\\2-Cleaned, merged json files";
    public static final String DATASET_EXTERNAL_DIRECTORY_GH_JSON_MERGED = "D:\\BugTriaging\\Data Sets\\1- GH\\GH-GHTorrent-Mongodb\\2-Cleaned, merged json files";
    
    //public static final String DATASET_DIRECTORY_GH_TSV = "C:\\2-Study\\BugTriaging\\Data Sets\\1- GH\\GH-GHTorrent-Mongodb\\3-TSV";
    public static final String DATASET_DIRECTORY_GH_MongoDB_TSV = "C:\\2-Study\\BugTriaging\\Data Sets\\1- GH\\GH-GHTorrent-Mongodb\\3-TSV";
//    public static final String DATASET_EXTERNAL_DIRECTORY_GH_TSV = "D:\\BugTriaging\\Data Sets\\1- GH\\GH-GHTorrent-Mongodb\\3-TSV";
    public static final String DATASET_EXTERNAL_DIRECTORY_GH_MongoDB_TSV = "D:\\BugTriaging\\Data Sets\\1- GH\\GH-GHTorrent-Mongodb\\3-TSV";
    
    //GH - MySQL:
//    public static final String DATASET_DIRECTORY_GH_SQL = "C:\\2-Study\\BugTriaging\\Data Sets\\1- GH\\GH-MySQL-2014-08-06";
    public static final String DATASET_DIRECTORY_GH_MySQL = "C:\\2-Study\\BugTriaging\\Data Sets\\1- GH\\GH-MySQL-2014-08-06";
    //  public static final String DATASET_DIRECTORY_GH_SQL_CONVERTED_TO_TSV = "C:\\2-Study\\BugTriaging\\Data Sets\\1- GH\\GH-MySQL-2014-08-06\\TSV";
    public static final String DATASET_DIRECTORY_GH_MySQL_TSV = "C:\\2-Study\\BugTriaging\\Data Sets\\1- GH\\GH-MySQL-2014-08-06\\TSV";
    
    //GH - mixed (MySQL and MongoDB):
    public static final String DATASET_DIRECTORY_GH_Mixed = "C:\\2-Study\\BugTriaging\\Data Sets\\1- GH";
    //SO:
    public static final String DATASET_DIRECTORY_SO_WITHOUT_EMAIL_HASH = "C:\\2-Study\\BugTriaging\\Data Sets\\2- SO\\SO-20140502-WithoutEmailHash";
    public static final String DATASET_DIRECTORY_SO_WITH_EMAIL_HASH = "C:\\2-Study\\BugTriaging\\Data Sets\\2- SO\\SO-20130909-WithEmailHash";
    public static final String DATASET_DIRECTORY_SO_Mixed = "C:\\2-Study\\BugTriaging\\Data Sets\\2- SO\\SO-20140502-Merged";
    
    //GH and SO (merged; common users):
    public static final String DATASET_DIRECTORY_GH_AND_SO_Mixed_TOGETHER = "C:\\2-Study\\BugTriaging\\Data Sets\\3- GH and SO (Merged)";
    public static final String DATASET_DIRECTORY_COMMUNITY = "C:\\2-Study\\BugTriaging\\Data Sets\\3- GH and SO (Merged)\\Communities";
    public static final String DATASET_DIRECTORY_TRIAGE_RESULTS = "C:\\2-Study\\BugTriaging\\Data Sets\\3- GH and SO (Merged)\\TriageResults";
    public static final String DATASET_DIRECTORY_DATASETS_FOR_TRIAGING = "C:\\2-Study\\BugTriaging\\Data Sets\\3- GH and SO (Merged)\\DataSetsForTriaging";
    
    //[] means array. {} means an object (In these cases, first, the name of the object comes. Then the fields come delimited by $).
	public static final Map<String, List<String>> USEFUL_FIELDS_IN_JSON_FILES = ImmutableMap.<String, List<String>> builder() 
	        .put("users", Arrays.asList(new String[] { "id", "login", "email" }))
	        .put("users:labels", Arrays.asList(new String[] { "id", "login", "email" }))
	        .put("users:FieldsToRemoveInvalidCharacters", Arrays.asList(new String[] { }))
	        
//	        .put("issues", Arrays.asList(new String[] { "id", "repo", "user{}id$login", "assignee", "title", "body", "[]labels", "comments", "created_at" }))
//	        .put("issues:labels", Arrays.asList(new String[] { "id", "repo", "reporterId", "reporterLogin", "assignee", "title", "body", "labels", "comments", "created_at" }))
	        .put("issues", Arrays.asList(new String[] { "id", "owner", "repo", "user{}id$login", "assignee{}id$login", "created_at", "comments", "[]labels", "title", "body" }))
	        .put("issues:labels", Arrays.asList(new String[] { "id", "owner", "repo", "reporterId", "reporterLogin", "assigneeId", "assigneeLogin", "created_at", "numberOfComments", "labels", "title", "body" }))
	        .put("issues:FieldsToRemoveInvalidCharacters", Arrays.asList(new String[] { "title", "body" }))
	        
	        .put("repos", Arrays.asList(new String[] { "url", "owner{}login", "name", "language", "description", "created_at" }))
	        .put("repos:labels", Arrays.asList(new String[] { "url", "login", "name", "language", "description", "created_at" }))
	        .put("repos:FieldsToRemoveInvalidCharacters", Arrays.asList(new String[] { "description" }))
	        
	        .put("commits", Arrays.asList(new String[] { "committer{}id$login" }))
	        
	        .put("issue_comments", Arrays.asList(new String[] { "issue_id", "repo", "user{}id$login", "body" }))
	        .put("pull_request_comments", Arrays.asList(new String[] { "id", "user{}id$login", "repo", "body", "diff_hunk" }))
	        .put("repo", Arrays.asList(new String[] { "id", "name", "language", "description", "open_issues", "svn_url" }))
	        .put("commit_comments",Arrays.asList(new String[]  { "id", "repo", "user", "body" })).build();
//	public static final String validSOTagCharacters = "#*\\-+"; //It is used for regex. So it means: # * - +
	public static final String allValidCharactersInSOTags = "a-zA-Z0-9.#+-"; //Obtained from file "allValidCharactersInSOTags.txt" after running the XMLParser.java. 
	public static final String allValidCharactersInSOTags_ForRegEx = "a-zA-Z0-9\\.\\#\\+\\-";  //The same as the above line, but altered for regEx usage. 
	public static final String allValidCharactersInSOQUESTION_AND_ANSWER_ForRegEx = "a-zA-Z0-9\\.\\#\\+\\-\\(\\)\\[\\]\\{\\}\\~\\!\\$\\%\\^\\&\\*\\_\\:\\;\\<\\>\\,\\.\\?\\/\\|\\=\\\"\\'\\`\\\\";  //The same as the above line, but for the question / answer text. 
	public static final String allValidCharactersInGH_PROJECT_NAMES_ForRegEx = "a-zA-Z0-9\\.\\#\\+\\-\\_";  //The same as the above line, but altered for regEx usage. 
	public static final String SEPARATOR_FOR_ARRAY_ITEMS = ";;";
	public static final DecimalFormat integerFormatter = new DecimalFormat("###,###");
	public static final DecimalFormat floatFormatter = new DecimalFormat("###,###.#");
	public static final DecimalFormat highPrecisionFloatFormatter = new DecimalFormat("###,###.######");

	//Community of users:
	public static final String COMMUNITY_11_WITH_AT_LEAST_ONE_SO_A_JUST_All_ISSUE_ASSIGNEES = "11"; //"SO activity: at least one answer. Only project members and committers and issue assignees and issue reporters.";
	public static final String COMMUNITY_12_WITH_AT_LEAST_ONE_SO_A_PROJECT_MEMBERS = "12"; //"SO activity: at least one answer. Only project members.";
	public static final String COMMUNITY_13_WITH_AT_LEAST_ONE_SO_A_PROJECT_MEMBERS_AND_COMMITTERS = "13"; //"SO activity: at least one answer. Only project members and committers.";
	public static final String COMMUNITY_14_WITH_AT_LEAST_ONE_SO_A_PROJECT_MEMBERS_AND_COMMITTERS_AND_ISSUE_ASSIGNEES = "14"; //"SO activity: at least one answer. Only project members and committers and issue assignees.";
	public static final String COMMUNITY_15_WITH_AT_LEAST_ONE_SO_A_PROJECT_MEMBERS_AND_COMMITTERS_AND_ISSUE_ASSIGNEES_AND_ISSUE_REPORTERS = "15"; //"SO activity: at least one answer. Only project members and committers and issue assignees and issue reporters.";
	
	public static final String COMMUNITY_21_WITH_AT_LEAST_ONE_SO_Q_OR_A_JUST_All_ISSUE_ASSIGNEES = "21"; //"SO activity: at least one answer. Only project members and committers and issue assignees and issue reporters.";
	public static final String COMMUNITY_22_WITH_AT_LEAST_ONE_SO_Q_OR_A_PROJECT_MEMBERS = "22"; //"SO activity: at least one answer. Only project members.";
	public static final String COMMUNITY_23_WITH_AT_LEAST_ONE_SO_Q_OR_A_PROJECT_MEMBERS_AND_COMMITTERS = "23"; //"SO activity: at least one answer. Only project members and committers.";
	public static final String COMMUNITY_24_WITH_AT_LEAST_ONE_SO_Q_OR_A_PROJECT_MEMBERS_AND_COMMITTERS_AND_ISSUE_ASSIGNEES = "24"; //"SO activity: at least one answer. Only project members and committers and issue assignees.";
	public static final String COMMUNITY_25_WITH_AT_LEAST_ONE_SO_Q_OR_A_PROJECT_MEMBERS_AND_COMMITTERS_AND_ISSUE_ASSIGNEES_AND_ISSUE_REPORTERS = "25"; //"SO activity: at least one answer. Only project members and committers and issue assignees and issue reporters.";

	public static final String COMMUNITY_31_NO_CHECKING_FOR_SO_ACTIVITY_JUST_All_ISSUE_ASSIGNEES = "31"; //"SO activity is not checked. Only project members and committers and issue assignees and issue reporters.";
	public static final String COMMUNITY_32_NO_CHECKING_FOR_SO_ACTIVITY_JUST_PROJECT_MEMBERS = "32"; //"SO activity is not checked. Only project members.";
	public static final String COMMUNITY_33_NO_CHECKING_FOR_SO_ACTIVITY_JUST_PROJECT_MEMBERS_AND_COMMITTERS = "33"; //"SO activity is not checked. Only project members and committers.";
	public static final String COMMUNITY_34_NO_CHECKING_FOR_SO_ACTIVITY_JUST_PROJECT_MEMBERS_AND_COMMITTERS_AND_ISSUE_ASSIGNEES = "34"; //"SO activity is not checked. Only project members and committers and issue assignees.";
	public static final String COMMUNITY_35_NO_CHECKING_FOR_SO_ACTIVITY_JUST_PROJECT_MEMBERS_AND_COMMITTERS_AND_ISSUE_ASSIGNEES_AND_ISSUE_REPORTERS = "35"; //"SO activity is not checked. Only project members and committers and issue assignees and issue reporters.";

	public static final String ALL = "ALL";
	
	public static final long THIS_IS_A_TEST = 10000;
	public static final long THIS_IS_REAL = -1;//unlimited!
	public static final double UNIMPORTANCE_OF_RECENT_ASSIGNMENTS = 0.0; //The less amounts, the more important; if it equals 2, the previous assignments has 1/(1+2) score, the one before that has 1/(1+3), and so on.
//	public static final double[] Z_SCORE_COEFFICIENTS = {0, 0, 0, 0, 0.003};
	public static final double[] Z_SCORE_COEFFICIENTS = {0, 0, 0, 0, 0.0001, 0.0001, 0.001, 0.002, 0.003, 0.005, 0.007, 0.01, 0.05, 0.1, 0.5, 1, 5, 10};
//	public static final double[] Z_SCORE_COEFFICIENTS = {0, 0, 0, 0, 0.00001, 0.00001, 0.0001, 0.001, 0.01, 0.3, 0.6, 1, 2, 5, 10, 100, 1000, 10000};
//	public static final double[] Z_SCORE_COEFFICIENTS = {0, 0, 0, 0, 0.0005, 0.0006, 0.0008, 0.001, 0.0012, 0.0014, 0.0016, 0.0018, 0.002, 0.0022, 0.0024, 0.0025, 0.0027, 0.003};
//	public static final double[] Z_SCORE_COEFFICIENTS = {0, 0, 0, 0, 0.001, 0.002, 0.005, 0.01, 0.02, 0.05, 0.1, 0.2, 0.5, 1, 2, 5, 10, 100};
//	public static final double[] Z_SCORE_COEFFICIENTS = {0, 0, 0, 0, 0.001, 0.01, 0.3, 0.6, 1, 2, 5, 10};
//	public static final int TOTAL_NUMBER_OF_METRICS = 5;
	public static final int TOTAL_NUMBER_OF_METRICS = 18;
	public static final double A_FAR_LARGESCORE = 1000000.0;
	public static final double INVALID_AVERAGE_UPVOTE = -999999.0;
	//Sort order:
	public enum SortOrder{
		ASCENDING_INTEGER, DESCENDING_INTEGER, DEFAULT_FOR_STRING
	}
	
	public enum ConditionType {
		NO_CONDITION, AND, OR
	}
	
	public static void changeStr(String s){
		s = s + "sss";
	}
	public static void main(String[] args) {
		System.out.println("---------------- Constants.java: ----------------");
		String s1 = "2013-05-25T21:50:02Z";
		String s2 = "2011-07-25T21:50:02Z";
		if (s1.compareTo(s2) > 0) //if s1 > s2 ==> returns positive value.
			System.out.println("1111");
		else
			System.out.println("2222");
		
		Date d1 = new Date();
		String s= "";
		for (int i=0; i<7000; i++)
			s = s + "a";
		Date d2 = new Date();
		System.out.println((d2.getTime()-d1.getTime())/1000);

		double a = 0.0000004;
		System.out.println(highPrecisionFloatFormatter.format(0.0000004));
		System.out.println(Constants.highPrecisionFloatFormatter.format(a));
	}

}
//Iterating:
//	for (Map.Entry<String, List<String>> entry : Constants.USEFUL_FIELDS_IN_JSON_FILES.entrySet())
//		System.out.println(entry.getKey() + "    ------>    " + entry.getValue());

