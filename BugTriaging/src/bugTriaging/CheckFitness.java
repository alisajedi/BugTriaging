package bugTriaging;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import bugTriaging.Constants.ConditionType;
import bugTriaging.Constants.SortOrder;
import Data.CommonUser;
import Data.CommunitiesSummary;
import Data.CommunityMember;
import Data.Issue;
import Data.MySQLProject;
import Data.ProjectAndItsCommitters;
import Data.ProjectAndItsMembers;
import Data.ProvideData;
import Data.SOPost;

public class CheckFitness {
	//--------------------------------------------------------------------------------------------------------------------------------------------
	//--------------------------------------------------------------------------------------------------------------------------------------------
	public static void checkFitnessOfUsersForIssues_1_initial(String mongoDBInputPath, String mongoDBUsersTSV, String mongoDBIssuesTSV, 
			String mySQLInputPath, String mySQLusersTSV){
		try{
			BufferedReader br;
			//Reading MongoDB users and putting them in a HashMap:
			br = new BufferedReader(new FileReader(mongoDBInputPath + "\\" + mongoDBUsersTSV)); 
			System.out.println("    1- Parsing file \"" + mongoDBInputPath + "\\" + mongoDBUsersTSV + "\" and putting items in a HashMap --------> Started ...");
			HashMap<String, String> mongoDBUsers = new HashMap<String, String>();
			String[] fields = {""};
			int i=0;
			String s;
			int error = 0;
			br.readLine(); //header.
			while ((s=br.readLine())!=null){
				if (s.endsWith("\t"))
					s = s + " ";
				fields = s.split("\t");
				if (fields.length<2)
					error++;
				else{
					if (fields[2].equals(" "))
						fields[2] = "";
					mongoDBUsers.put(fields[1], fields[2]);
				}//else.
				//System.out.println(fields[1] + "----->" + usersHM.get(fields[1]));
				i++;
				if (i % 500000 == 0)
					System.out.println("        " +  Constants.integerFormatter.format(i));
			}//while ((s=br....
			System.out.println("    1- Parsing file \"" + mongoDBInputPath + "\\" + mongoDBUsersTSV + "\" and putting items in a HashMap --------> Finished ...");
			br.close();

			//Reading MySQL users and putting them in another hashMap:
			br = new BufferedReader(new FileReader(mySQLInputPath + "\\" + mySQLusersTSV)); 
			System.out.println("    2- Parsing file \"" + mySQLInputPath + "\\" + mySQLusersTSV + " and putting items in a HashMap --------> Started ...");
			HashMap<String, String> mySQLUsers = new HashMap<String, String>();
			br.readLine(); //header.
			i=0;
			while ((s=br.readLine())!=null){
				if (s.endsWith("\t"))
					s = s + " ";
				fields = s.split("\t");
				if (fields[2].equals(" "))
					fields[2] = "";
				mySQLUsers.put(fields[1], fields[2]);
				//System.out.println(fields[1] + "----->" + usersHM.get(fields[1]));
				i++;
				if (i % 500000 == 0)
					System.out.println("        " +  Constants.integerFormatter.format(i));
			}//while ((s=br....
			System.out.println("    2- Parsing file \"" + mySQLInputPath + "\\" + mySQLusersTSV + " and putting items in a HashMap --------> Finished ...");
			br.close();

			br = new BufferedReader(new FileReader(mongoDBInputPath + "\\" + mongoDBIssuesTSV)); 
			System.out.println("    3- Parsing file \"" + mongoDBInputPath + "\\" + mongoDBIssuesTSV + " and checking the existence of their assignee in the MongoDB HashMap of users built in previous step --------> Started ...");
			br.readLine(); //header.
			int haveAssignee=0, doesntHaveAssignee=0, eMailOnlyInMongoDB=0, eMailOnlyInMySQL=0, eMailInBoth=0, eMailInBothTheSame=0, eMailInBothDifferent=0, eMailInNeither=0;
			i=0;
			String mongoDBEmail, mySQLEmail, assigneeLogin;
			while ((s=br.readLine())!=null){
				if (s.endsWith("\t"))
					s = s + " ";
				fields = s.split("\t");
				//System.out.println("reporterLogin: <" + fields[3] + ">");

				assigneeLogin = fields[6];
				if (assigneeLogin.equals(""))
					doesntHaveAssignee++;
				else{
					//System.out.println("assigneeLogin: <" + fields[5] + ">");
					haveAssignee++;
					if (mongoDBUsers.containsKey(assigneeLogin))
						mongoDBEmail = mongoDBUsers.get(assigneeLogin);
					else
						mongoDBEmail = "";
					if (mySQLUsers.containsKey(assigneeLogin))
						mySQLEmail = mySQLUsers.get(assigneeLogin);
					else
						mySQLEmail = "";

					if (mongoDBEmail.equals(""))
						if (mySQLEmail.equals(""))
							eMailInNeither++;
						else{
							eMailOnlyInMySQL++;
							//System.out.println("--eMailOnlyInMySQL-- assigneeLogin: <" + fields[5] + ">" + mongoDBEmail + "    " + mySQLEmail);	
						}
					else
						if (mySQLEmail.equals("")){
							eMailOnlyInMongoDB++;
							//System.out.println("--eMailOnlyInMongoDB-- assigneeLogin: <" + fields[5] + ">" + mongoDBEmail + "    " + mySQLEmail);
						}
						else{
							eMailInBoth++;
							if (mongoDBEmail.equals(mySQLEmail))
								eMailInBothTheSame++;
							else{
								eMailInBothDifferent++;
								System.out.println("--eMailInBothDifferent-- assigneeLogin: <" + fields[6] + ">" + mongoDBEmail + "    " + mySQLEmail);	
							}
						}//else.
				}//else.
				//System.out.println(fields[1] + "----->" + usersHM.get(fields[1]));
				i++;
				if (i % 500000 == 0)
					System.out.println("        " +  Constants.integerFormatter.format(i));
			}//while ((s=br....
			System.out.println("    3- Parsing file \"" + mongoDBInputPath + "\\" + mongoDBIssuesTSV + " and checking the existence of their assignee in the MongoDB HashMap of users built in previous step --------> Finished ...");
			br.close();
			System.out.println("        haveAssignee: " + haveAssignee + "    doesntHaveAssignee: " + doesntHaveAssignee);
			System.out.println("        eMailOnlyInMongoDB: " + eMailOnlyInMongoDB + "    eMailOnlyInMySQL: " + eMailOnlyInMySQL);
			System.out.println("        eMailInBoth: " + eMailInBoth + "    eMailInBothTheSame: " + eMailInBothTheSame);
			System.out.println("        eMailInBothDifferent: " + eMailInBothDifferent + "    eMailInNeither: " + eMailInNeither);
			System.out.println("Error lines:" + error);
		}
		catch (Exception e){
			e.printStackTrace();		
		}
	}//public void checkFitnessOfUsersForIssues(....
	//--------------------------------------------------------------------------------------------------------------------------------------------
	//In this method, we want to check the number of common users as members of each GH project. We'd like to know how many common users are in the first top 100 projects for example. The result for all projects are placed in the output file outputRepoNumberOfUsersTSV.
	public static void checkFitnessOfUsersForProjects_1_initial(String commonUsersInputPath, String commonUsersInputTSV, 
			String projectsInputPath, String projectsInputTSV,
			String projectMembersInputPath, String projectMembersInputTSV, 
			String outputPath, String outputRepoUsersTSV, String outputRepoNumberOfUsersTSV){ 
		try{ 
			BufferedReader br;
			//Reading common users and putting them in a HashMap<mySQLId, eMailHash + \t + mongoDBId>:
			br = new BufferedReader(new FileReader(commonUsersInputPath + "\\" + commonUsersInputTSV)); 
			System.out.println("    1- Parsing file \"" + commonUsersInputPath + "\\" + commonUsersInputTSV + "\" and putting <mySQLId, eMailHash + \t + mongoDBId> items in a HashMap --------> Started ...");
			HashMap<String, String> commonUsers = new HashMap<String, String>();
			int error = 0;
			String[] fields;
			int i=0;
			String s, mySQLUserId, mongoDBUserId, soIdAndLoginAndMongoDBUserId, login, soId;
			br.readLine(); //header.
			while ((s=br.readLine())!=null){
				fields = s.split("\t");
				if (fields.length < 6)
					error++;
				else{
					mySQLUserId = fields[4];

					soId = fields[0];
					login = fields[2];
					mongoDBUserId = fields[5];
					soIdAndLoginAndMongoDBUserId = soId + "\t" + login + "\t" + mongoDBUserId;
					commonUsers.put(mySQLUserId, soIdAndLoginAndMongoDBUserId);
				}//else.
				i++;
				if (i % 500000 == 0)
					System.out.println("        " +  Constants.integerFormatter.format(i));
			}//while ((s=br....
			if (error>0)
				System.out.println("        Number of records in common users with error: " + error);
			System.out.println("    1- Parsing file \"" + commonUsersInputPath + "\\" + commonUsersInputTSV + "\" and putting <mySQLId, eMailHash + \t + mongoDBId> items in a commonUsers HashMap --------> Finished ...");
			br.close();

			//Reading all projects and putting pairs of <projectMySQLId, repoNameAndDescriptionAndLanguage> in "repos" HashMap:
			//This HashMap will be used in the next step in writing the repo's info to the output file.
			HashMap<String, String> repos = new HashMap<String, String>();
			System.out.println("    2- Reading projects' information and making the \"repos\" hashmap --------> Started ...");
			br = new BufferedReader(new FileReader(projectsInputPath + "\\" + projectsInputTSV)); 
			br.readLine(); //Header.
			error = 0; i = 0;
			String repoId, repoName, repoDescription, repoLanguage, repoNameAndDescriptionAndLanguage;
			while ((s=br.readLine())!=null){
				fields = s.split("\t");
				if (fields.length == 4){
					repoId = fields[0];
					repoName = fields[1];
					repoDescription = fields[2];
					repoLanguage = fields[3];
					repoNameAndDescriptionAndLanguage = repoName + "\t" + repoDescription + "\t" + repoLanguage;
					repos.put(repoId, repoNameAndDescriptionAndLanguage);
				}
				else{
					error++;
					System.out.println("Error: <Number of fields=" + fields.length + ">" + s );	
				}//else.
				i++;
				if (i % 500000 == 0)
					System.out.println("        " +  Constants.integerFormatter.format(i));
			}//while ((s=br...
			br.close();
			if (error>0)
				System.out.println("        Number of records in projects file with error: " + error);
			System.out.println("    2- Reading projects' information and making the \"repos\" hashmap --------> Finished.");

			//Reading project members (that are from mySQL data set) and checking the user_id field (that is from MySQL) in the commonUsers hashmap. 
			//If it (a user that is a member in a project) is there, it means it is about a common user; so write these info in a new file: 
			//createdAt(membershipDate)	emailHash	login	mySQLUserId	mongoDBUserId	projectName	mySQLProjectId
			//The first two plus mySQLId are from projectmembersInputTSV file and the three other fields are from commonUsers hashmap (that is filled from commonUsersInputTSV).
			br = new BufferedReader(new FileReader(projectMembersInputPath + "\\" + projectMembersInputTSV)); 
			System.out.println("    3- Parsing MySQL project membership file and checking its items with the commonUsers HashMap --------> Started ...");
			System.out.println("    4- Building a file for the members of projects who are common users --------> Started ...");
			FileWriter writer1 = new FileWriter(outputPath + "\\" + outputRepoUsersTSV);
			writer1.append("soId\tcreatedAt(ghProjectMembershipDate)\tlogin\tmySQLUserId\tmongoDBUserId\tmySQLProjectId\tprojectName\tprojectDescription\tprojectlanguage\n");
			HashMap<String, Integer> reposAndNumberOfTheirMembers = new HashMap<String, Integer>();
			error = 0;
			i=0;
			int projectMembershipsThatAreCommonUser = 0, projectMembershipsThatAreNotCommonUser = 0, error2=0;
			String createdAt;
			br.readLine(); //header.
			while ((s=br.readLine())!=null){
				fields = s.split("\t");
				if (fields.length < 3)
					error++;
				else{
					mySQLUserId = fields[1];
					if (commonUsers.containsKey(mySQLUserId)){//This means that this project membership is about a common user, being a member of GH project.
						repoId = fields[0];
						createdAt = fields[2];
						if (repos.containsKey(repoId))
							repoNameAndDescriptionAndLanguage = repos.get(repoId);
						else{
							repoNameAndDescriptionAndLanguage = " \t \t ";
							System.out.println("        Error MySQL repoId = " + repoId);
							error2++;
						}//else.
						soIdAndLoginAndMongoDBUserId = commonUsers.get(mySQLUserId);
						fields = soIdAndLoginAndMongoDBUserId.split("\t");
						soId = fields[0];
						login = fields[1];
						mongoDBUserId = fields[2];
						projectMembershipsThatAreCommonUser++;
						writer1.append(soId + "\t" + createdAt + "\t" + login + "\t" + mySQLUserId + "\t" + mongoDBUserId + "\t" + repoId + "\t" + repoNameAndDescriptionAndLanguage + "\n");
						if (reposAndNumberOfTheirMembers.containsKey(repoId))
							reposAndNumberOfTheirMembers.put(repoId, reposAndNumberOfTheirMembers.get(repoId)+1);
						else
							reposAndNumberOfTheirMembers.put(repoId, 1);
					}//if (comm....
					else
						projectMembershipsThatAreNotCommonUser++;
				}//else.
				i++;
				if (i % 500000 == 0)
					System.out.println("        " +  Constants.integerFormatter.format(i));
			}//while ((s=br....
			if (error>0)
				System.out.println("        Number of records in common users with error: " + error);
			if (error2>0)
				System.out.println("        Weird: Number of repos that their id does not exist in the list of repos: " + error2);
			System.out.println("    4- Building a file for the members of projects who are common users --------> Finished ...");
			System.out.println("    3- Parsing MySQL project membership file and checking its items with the commonUsers HashMap --------> Finished ...");
			br.close();
			writer1.flush();
			writer1.close();

			//Now, we have the number of members for each repo (in reposAndNumberOfTheirMembers hashMap). 
			//We have to sort them based on number of members and send to output file.
			//So first, we reverse the HashMap from <repo - numberOfTheirMembers> to <numberOfMembers - respectiveRepos(separatedByComma)>
			System.out.println("    5- Building output file for repos, sorted by their numberOfusers --------> Started ...");
			int numberOfMembers;
			HashMap<Integer, String> numberOfMembersAndRespectiveRepos = new HashMap<Integer, String>();
			System.out.println("        Step A) Making the numberOfMembersAndRespectiveRepos hashmap (including values separated by commas):");
			i = 0;
			String repoIdsSeparatedByCollon = "";
			for (Map.Entry<String, Integer> entry: reposAndNumberOfTheirMembers.entrySet()){
				repoId = entry.getKey();
				numberOfMembers = entry.getValue();
				//if (numberOfMembers > 3)
				if (numberOfMembersAndRespectiveRepos.containsKey(numberOfMembers)){
					repoIdsSeparatedByCollon = numberOfMembersAndRespectiveRepos.get(numberOfMembers);
					repoIdsSeparatedByCollon = repoIdsSeparatedByCollon + ";" + repoId;
					numberOfMembersAndRespectiveRepos.put(numberOfMembers, repoIdsSeparatedByCollon);
				}//if (numb....
				else
					numberOfMembersAndRespectiveRepos.put(numberOfMembers, repoId);
				i++;
				if (i % 2000 == 0)
					System.out.println("        " +  Constants.integerFormatter.format(i));
			}//for (Map.....
			//Now, sort numberOfMembersAndRespectiveRepos hashMap based on its keys, in descending order. 
			System.out.println("        Step B) Sorting the keys in numberOfMembersAndRespectiveRepos and splitting the values (that are separated by commas)");
			FileWriter writer2 = new FileWriter(outputPath + "\\" + outputRepoNumberOfUsersTSV);
			writer2.append("numberOfCommonUsers\tmySQLProjectId\tprojectName\tprojectDescription\tprojectlanguage\n");
			List<Integer> sortedKeys=new ArrayList<Integer>(numberOfMembersAndRespectiveRepos.keySet());
			Collections.sort(sortedKeys, Collections.reverseOrder());
			String reposSeparatedByCommas = "";
			i = 0;
			error2 = 0;
			for (Integer numberOfRepoMembers: sortedKeys){
				reposSeparatedByCommas = numberOfMembersAndRespectiveRepos.get(numberOfRepoMembers);
				fields = reposSeparatedByCommas.split(";");
				for (String aRepoId: fields){
					if (repos.containsKey(aRepoId))
						repoNameAndDescriptionAndLanguage = repos.get(aRepoId);
					else{
						repoNameAndDescriptionAndLanguage = " \t \t ";
						System.out.println("        Error: MySQL repoId = " + aRepoId + " not found");
						error2++;
					}//else.
					writer2.append(numberOfRepoMembers.toString() + "\t" + aRepoId + "\t" + repoNameAndDescriptionAndLanguage + "\n");
				}//for
				i++;
				if (i % 2000 == 0)
					System.out.println("        " +  Constants.integerFormatter.format(i));
			}//for (Inte....
			if (error2>0)
				System.out.println("        Number of repos that their id does not exist in the list of repos: " + error2);
			writer2.flush();
			writer2.close();
			System.out.println("    5- Building output file for repos, sorted by their numberOfusers --------> Finished.");
			System.out.println("    projectMembersThatAreCommonUser: " + projectMembershipsThatAreCommonUser); //Printed result: 1,001,639
			System.out.println("    projectMembersThatAreNotCommonUser: " + projectMembershipsThatAreNotCommonUser); //Printed result: 2,697,673
		}
		catch (Exception e){
			e.printStackTrace();		
		}
	}//public void checkFitnessOfUsersForProjects(....
	//--------------------------------------------------------------------------------------------------------------------------------------------
	public static void addTotalNumberOfUsersToNumberOfMembersInEachProject(String projectMembershipsInputPath, String projectMembershipsInputTSV,
			String numberOfMembersInEachProjectInputPath, String numberOfMembersInEachProjectInputTSV, 
			String outputPath, String numberOfMembersInEachProjectOutputTSV){
		try{ 
			BufferedReader br;
			//Reading project memberships and adding <repoId, totalNumberOfMembers> in a hashSet:
			br = new BufferedReader(new FileReader(projectMembershipsInputPath + "\\" + projectMembershipsInputTSV)); 
			System.out.println("    1- Parsing the project memberships --------> Started ...");
			HashMap<String, Integer> reposAndTheirTotalMembers = new HashMap<String, Integer>();
			int error = 0, totalNumberOfRepoMembers;
			String[] fields;
			int i=0;
			String s, repoId;
			br.readLine(); //header.
			while ((s=br.readLine())!=null){
				fields = s.split("\t");
				if (fields.length != 3)
					error++;
				else{
					repoId = fields[0];
					if (reposAndTheirTotalMembers.containsKey(repoId)){
						totalNumberOfRepoMembers = reposAndTheirTotalMembers.get(repoId);
						totalNumberOfRepoMembers++;
						reposAndTheirTotalMembers.put(repoId, totalNumberOfRepoMembers);
					}//if
					else
						reposAndTheirTotalMembers.put(repoId, 1);
				}//else.
				i++;
				if (i % 500000 == 0)
					System.out.println("        " +  Constants.integerFormatter.format(i));
			}//while ((s=br....
			if (error>0)
				System.out.println("        Error) Number of records in \"" +  numberOfMembersInEachProjectInputTSV + "\" with !=3 fields: " + error);
			System.out.println("    1- Parsing the project memberships --------> Finished ...");
			br.close();

			//Now, reading all numberOfMembersInEachProjectInputTSV records, and add total number of members to it:
			br = new BufferedReader(new FileReader(numberOfMembersInEachProjectInputPath + "\\" + numberOfMembersInEachProjectInputTSV)); 
			System.out.println("    2- Reading numberOfMembersInEachProjectInputTSV records --------> Started ...");
			System.out.println("        3- Adding total number of users --------> Started ...");
			FileWriter writer = new FileWriter(outputPath + "\\" + numberOfMembersInEachProjectOutputTSV);
			writer.append("numberOfCommonUsers\tmySQLProjectId\tprojectName\tprojectDescription\tprojectlanguage\ttotalUsers\n");
			error = 0;
			i=0;
			String mySQLProjectId;
			br.readLine(); //header.
			while ((s=br.readLine())!=null){
				fields = s.split("\t");
				if (fields.length != 5)
					error++;
				else{
					mySQLProjectId = fields[1];
					if (reposAndTheirTotalMembers.containsKey(mySQLProjectId)){
						totalNumberOfRepoMembers = reposAndTheirTotalMembers.get(mySQLProjectId);
						writer.append(s + "\t" + Integer.toString(totalNumberOfRepoMembers) + "\n");
					}
					else{
						error++;
						System.out.println(s);	
					}
				}//else.
				i++;
				if (i % 500000 == 0)
					System.out.println("            " +  Constants.integerFormatter.format(i));
			}//while ((s=br....
			if (error>0)
				System.out.println("        Weird errors: " + error);
			System.out.println("        3- Adding total number of users --------> Fnished ...");
			System.out.println("    2- Reading numberOfMembersInEachProjectInputTSV records --------> Finished ...");
			br.close();

			writer.flush();
			writer.close();
			System.out.println("Done.");
		}//try
		catch(Exception e){
			e.printStackTrace();
		}
	}
	//--------------------------------------------------------------------------------------------------------------------------------------------
	public static void buildCommunitiesOfUsers(String commonUsersInputPath, String commonUsersInputTSV, 
			String mySQLProjectsInputPath, String mySQLProjectsInputTSV, 
			String projectMembersInputPath, String projectMembersInputTSV, 
			String committersInputPath, String committersInputTSV,
			String issueAssigneesInputPath, String issueAssigneesInputTSV, 
			String SOPostsInputPath, String SOPostsInputTSV,
			String communityOutputPath, String communityOutputTSVFileName_NoExtension, String communityOutputSummaryTSV,
			int showProgressInterval, long testOrReal){//This method builds the community users based on the first input parameter. There are 8 possible cases for this parameter that are defined in Constants.java.
		try{
			//1- Read Projects and their assigned issues from TSV: Total: 1,393,640
			TreeMap<String, ArrayList<String[]>> projectsOfAssignedIssuesAndTheirIssuesInformation = TSVManipulations.readNonUniqueKeyAndItsValueFromTSV(
					issueAssigneesInputPath, issueAssigneesInputTSV, null, 1, SortOrder.DEFAULT_FOR_STRING , 11, "3$5", ConditionType.NO_CONDITION, 0, "", 0, "", showProgressInterval*5, testOrReal, 1);
			
			//2- Read mySQL projects from TSV (key = mySQLProjectId): Total: 10,358,102
			TreeMap<String, String[]> mySQLProjects1 = TSVManipulations.readUniqueKeyAndItsValueFromTSV(
					mySQLProjectsInputPath, mySQLProjectsInputTSV, null, 0, 9, "2", ConditionType.NO_CONDITION, 0, "", 0, "", showProgressInterval*5, testOrReal, 2);
			
			//3- Removing projects that do not have assignee (from all mysql projects) (meaning that they do not have assigned issue to test):
			System.out.println("3- Removing mySQL projects without assignee ..."); 
			System.out.println("    Started ...");
			int i = 0;
			Iterator<Map.Entry<String, String[]>> mySQLProjectsIterator1 = mySQLProjects1.entrySet().iterator();
			while (mySQLProjectsIterator1.hasNext()){
				Map.Entry<String, String[]> aMySQLProject = mySQLProjectsIterator1.next();
				MySQLProject mySQLP = ProvideData.copyMySQLProjectFromStringArray_OnlyThereAreSomeFields1(aMySQLProject.getValue());
				if (!projectsOfAssignedIssuesAndTheirIssuesInformation.containsKey(mySQLP.ownerLoginAndProjectName))
					mySQLProjectsIterator1.remove();
				i++;
				if (testOrReal > Constants.THIS_IS_REAL)
					if (i >= testOrReal)
						break;
				if (i % (showProgressInterval*5) == 0)
					System.out.println("    " +  Constants.integerFormatter.format(i));
			}//while.
			System.out.println("    Number of remained mySQL projects: " + mySQLProjects1.size());
			System.out.println("    Finished.");//# of Remained projects:144,958.
			System.out.println();
			
			//4- Read Project committers from TSV: Total: 138,835,595 (only 20,477,480 will be read)
			TreeMap<String, ArrayList<String[]>> projectsAndTheirCommitters = TSVManipulations.readNonUniqueKeyAndItsValueFromTSV(
					committersInputPath, committersInputTSV, mySQLProjects1.keySet(), 1, SortOrder.DEFAULT_FOR_STRING , 2, "0", ConditionType.NO_CONDITION, 0, "", 0, "", showProgressInterval*5, testOrReal, 4);
			
			//5- Read common users from TSV (key = mySQLUserId): Total: 358,472
			TreeMap<String, String[]> commonUsers1ByMySQLId = TSVManipulations.readUniqueKeyAndItsValueFromTSV(
					commonUsersInputPath, commonUsersInputTSV, null, 4, 6, "0$2$4$5", ConditionType.NO_CONDITION, 0, "", 0, "", showProgressInterval, testOrReal, 5);
			
			//6- Read common users from TSV (key = login): Total: 358,472
			TreeMap<String, String[]> commonUsers2ByLogin = TSVManipulations.readUniqueKeyAndItsValueFromTSV(
					commonUsersInputPath, commonUsersInputTSV, null, 2, 6, "0$2$4$5", ConditionType.NO_CONDITION, 0, "", 0, "", showProgressInterval, testOrReal, 6);

			//7- Read common users  from TSV (only SOId): Total: 358,472
			HashSet<String> commonUsers3BySOId = TSVManipulations.readNonUniqueFieldFromTSV_OnlyRepeatEachEntryOnce(
					commonUsersInputPath, commonUsersInputTSV, 0, 6, ConditionType.NO_CONDITION, 0, "", 0, "", showProgressInterval, testOrReal, 7);
			
			//8- Reading SOId of answerers in a HashSet: Total: 19,881,018 (12,609,623 read + 7,271,395 ignored)
			HashSet<String> allWhoAnswered = TSVManipulations.readNonUniqueFieldFromTSV_OnlyRepeatEachEntryOnce(
					SOPostsInputPath, SOPostsInputTSV, 2, 9, ConditionType.AND, 1, "2", 1, "2", showProgressInterval*5, testOrReal, 8);

			//9- Removing answerers who are not common user:
			System.out.println("9- Removing answerers who are not common user ..."); 
			System.out.println("    Started ...");
			i = 0;
			for (Iterator<String> it = allWhoAnswered.iterator(); it.hasNext();){
				String SOId = it.next();
				if (!commonUsers3BySOId.contains(SOId))
					it.remove();
				i++;
				if (testOrReal > Constants.THIS_IS_REAL)
					if (i >= testOrReal)
						break;
				if (i % showProgressInterval == 0)
					System.out.println("    " +  Constants.integerFormatter.format(i));
			}//for.
			System.out.println("    Number of remained answerers: " + allWhoAnswered.size());
			System.out.println("    Finished.");
			System.out.println(); //remained answerers: 179,437
			
			//10- Reading SOId of question Askers / answerers in a HashSet:  Total: 19,881,018(19,824,320 read + 56,698 ignored)
			HashSet<String> allWhoAskedOrAnswered = TSVManipulations.readNonUniqueFieldFromTSV_OnlyRepeatEachEntryOnce(
					SOPostsInputPath, SOPostsInputTSV, 2, 9, ConditionType.OR, 1, "1", 1, "2", showProgressInterval*5, testOrReal, 10);
			
			//11- Removing askerOrAnswerers who are not common user:
			System.out.println("11- Removing askerOrAnswerers who are not common user ..."); 
			System.out.println("    Started ...");
			i = 0;
			for (Iterator<String> it = allWhoAskedOrAnswered.iterator(); it.hasNext();){
				String SOId = it.next();
				if (!commonUsers3BySOId.contains(SOId))
					it.remove();
				i++;
				if (testOrReal > Constants.THIS_IS_REAL)
					if (i >= testOrReal)
						break;
				if (i % showProgressInterval == 0)
					System.out.println("    " +  Constants.integerFormatter.format(i));
			}//for.
			commonUsers3BySOId.clear();
			System.out.println("    Number of remained askerOrAnswerers: " + allWhoAskedOrAnswered.size());
			System.out.println("    Finished.");
			System.out.println(); //Remained askerOrAnswerers: 238728

			//12- Read Project memberships from TSV: Total: 204,332 (that is filtered to mySQLProjects1 that have assignee. All records read: 3,699,312)
			TreeMap<String, ArrayList<String[]>> projectsAndTheirMembers = TSVManipulations.readNonUniqueKeyAndItsValueFromTSV(
					projectMembersInputPath, projectMembersInputTSV, mySQLProjects1.keySet(), 0, SortOrder.DEFAULT_FOR_STRING , 3, "1", ConditionType.NO_CONDITION, 0, "", 0, "", showProgressInterval*5, testOrReal, 12);
			
			System.out.println("13- Producing outputs (building 15 communities plus a summary of them) ..."); 
			System.out.println("    Started ...");
			//Now, writing the <project, member> in output files:
			FileWriter writer11 = new FileWriter(communityOutputPath + "\\" + communityOutputTSVFileName_NoExtension + Constants.COMMUNITY_11_WITH_AT_LEAST_ONE_SO_A_JUST_All_ISSUE_ASSIGNEES + ".tsv");
			writer11.append("mySQLProjectId\townerLogin/projName\tSOId\tghLogin\tghMySQLId\tghMongoDBId\n");
			FileWriter writer12 = new FileWriter(communityOutputPath + "\\" + communityOutputTSVFileName_NoExtension + Constants.COMMUNITY_12_WITH_AT_LEAST_ONE_SO_A_PROJECT_MEMBERS + ".tsv");
			writer12.append("mySQLProjectId\townerLogin/projName\tSOId\tghLogin\tghMySQLId\tghMongoDBId\n");
			FileWriter writer13 = new FileWriter(communityOutputPath + "\\" + communityOutputTSVFileName_NoExtension + Constants.COMMUNITY_13_WITH_AT_LEAST_ONE_SO_A_PROJECT_MEMBERS_AND_COMMITTERS + ".tsv");
			writer13.append("mySQLProjectId\townerLogin/projName\tSOId\tghLogin\tghMySQLId\tghMongoDBId\n");
			FileWriter writer14 = new FileWriter(communityOutputPath + "\\" + communityOutputTSVFileName_NoExtension + Constants.COMMUNITY_14_WITH_AT_LEAST_ONE_SO_A_PROJECT_MEMBERS_AND_COMMITTERS_AND_ISSUE_ASSIGNEES + ".tsv");
			writer14.append("mySQLProjectId\townerLogin/projName\tSOId\tghLogin\tghMySQLId\tghMongoDBId\n");
			FileWriter writer15 = new FileWriter(communityOutputPath + "\\" + communityOutputTSVFileName_NoExtension + Constants.COMMUNITY_15_WITH_AT_LEAST_ONE_SO_A_PROJECT_MEMBERS_AND_COMMITTERS_AND_ISSUE_ASSIGNEES_AND_ISSUE_REPORTERS + ".tsv");
			writer15.append("mySQLProjectId\townerLogin/projName\tSOId\tghLogin\tghMySQLId\tghMongoDBId\n");
			FileWriter writer21 = new FileWriter(communityOutputPath + "\\" + communityOutputTSVFileName_NoExtension + Constants.COMMUNITY_21_WITH_AT_LEAST_ONE_SO_Q_OR_A_JUST_All_ISSUE_ASSIGNEES + ".tsv");
			writer21.append("mySQLProjectId\townerLogin/projName\tSOId\tghLogin\tghMySQLId\tghMongoDBId\n");
			FileWriter writer22 = new FileWriter(communityOutputPath + "\\" + communityOutputTSVFileName_NoExtension + Constants.COMMUNITY_22_WITH_AT_LEAST_ONE_SO_Q_OR_A_PROJECT_MEMBERS + ".tsv");
			writer22.append("mySQLProjectId\townerLogin/projName\tSOId\tghLogin\tghMySQLId\tghMongoDBId\n");
			FileWriter writer23 = new FileWriter(communityOutputPath + "\\" + communityOutputTSVFileName_NoExtension + Constants.COMMUNITY_23_WITH_AT_LEAST_ONE_SO_Q_OR_A_PROJECT_MEMBERS_AND_COMMITTERS + ".tsv");
			writer23.append("mySQLProjectId\townerLogin/projName\tSOId\tghLogin\tghMySQLId\tghMongoDBId\n");
			FileWriter writer24 = new FileWriter(communityOutputPath + "\\" + communityOutputTSVFileName_NoExtension + Constants.COMMUNITY_24_WITH_AT_LEAST_ONE_SO_Q_OR_A_PROJECT_MEMBERS_AND_COMMITTERS_AND_ISSUE_ASSIGNEES + ".tsv");
			writer24.append("mySQLProjectId\townerLogin/projName\tSOId\tghLogin\tghMySQLId\tghMongoDBId\n");
			FileWriter writer25 = new FileWriter(communityOutputPath + "\\" + communityOutputTSVFileName_NoExtension + Constants.COMMUNITY_25_WITH_AT_LEAST_ONE_SO_Q_OR_A_PROJECT_MEMBERS_AND_COMMITTERS_AND_ISSUE_ASSIGNEES_AND_ISSUE_REPORTERS + ".tsv");
			writer25.append("mySQLProjectId\townerLogin/projName\tSOId\tghLogin\tghMySQLId\tghMongoDBId\n");
			FileWriter writer31 = new FileWriter(communityOutputPath + "\\" + communityOutputTSVFileName_NoExtension + Constants.COMMUNITY_31_NO_CHECKING_FOR_SO_ACTIVITY_JUST_All_ISSUE_ASSIGNEES + ".tsv");
			writer31.append("mySQLProjectId\townerLogin/projName\tSOId\tghLogin\tghMySQLId\tghMongoDBId\n");
			FileWriter writer32 = new FileWriter(communityOutputPath + "\\" + communityOutputTSVFileName_NoExtension + Constants.COMMUNITY_32_NO_CHECKING_FOR_SO_ACTIVITY_JUST_PROJECT_MEMBERS + ".tsv");
			writer32.append("mySQLProjectId\townerLogin/projName\tSOId\tghLogin\tghMySQLId\tghMongoDBId\n");
			FileWriter writer33 = new FileWriter(communityOutputPath + "\\" + communityOutputTSVFileName_NoExtension + Constants.COMMUNITY_33_NO_CHECKING_FOR_SO_ACTIVITY_JUST_PROJECT_MEMBERS_AND_COMMITTERS + ".tsv");
			writer33.append("mySQLProjectId\townerLogin/projName\tSOId\tghLogin\tghMySQLId\tghMongoDBId\n");
			FileWriter writer34 = new FileWriter(communityOutputPath + "\\" + communityOutputTSVFileName_NoExtension + Constants.COMMUNITY_34_NO_CHECKING_FOR_SO_ACTIVITY_JUST_PROJECT_MEMBERS_AND_COMMITTERS_AND_ISSUE_ASSIGNEES + ".tsv");
			writer34.append("mySQLProjectId\townerLogin/projName\tSOId\tghLogin\tghMySQLId\tghMongoDBId\n");
			FileWriter writer35 = new FileWriter(communityOutputPath + "\\" + communityOutputTSVFileName_NoExtension + Constants.COMMUNITY_35_NO_CHECKING_FOR_SO_ACTIVITY_JUST_PROJECT_MEMBERS_AND_COMMITTERS_AND_ISSUE_ASSIGNEES_AND_ISSUE_REPORTERS + ".tsv");
			writer35.append("mySQLProjectId\townerLogin/projName\tSOId\tghLogin\tghMySQLId\tghMongoDBId\n");
			FileWriter writer_Summary = new FileWriter(communityOutputPath + "\\" + communityOutputSummaryTSV);
			writer_Summary.append("mySQLProjectId\townerLogin/projName\t#projMembers\t" +
					"C11_WITH_AT_LEAST_ONE_SO_A_JUST_All_ISSUE_ASSIGNEES\t" + 
					"C12_WITH_AT_LEAST_ONE_SO_A_PROJECT_MEMBERS\tC12_assigned\t" + 
					"C13_WITH_AT_LEAST_ONE_SO_A_PROJECT_MEMBERS_AND_COMMITTERS\tC13_assigned\t" + 
					"C14_WITH_AT_LEAST_ONE_SO_A_PROJECT_MEMBERS_AND_COMMITTERS_AND_ISSUE_ASSIGNEES\tC14_assigned\t" + 
					"C15_WITH_AT_LEAST_ONE_SO_A_PROJECT_MEMBERS_AND_COMMITTERS_AND_ISSUE_ASSIGNEES_AND_ISSUE_REPORTERS\tC15_assigned\t" +
					"C21_WITH_AT_LEAST_ONE_SO_Q_OR_A_JUST_All_ISSUE_ASSIGNEES\t" + 
					"C22_WITH_AT_LEAST_ONE_SO_Q_OR_A_PROJECT_MEMBERS\tC22_assigned\t" + 
					"C23_WITH_AT_LEAST_ONE_SO_Q_OR_A_PROJECT_MEMBERS_AND_COMMITTERS\tC23_assigned\t" + 
					"C24_WITH_AT_LEAST_ONE_SO_Q_OR_A_PROJECT_MEMBERS_AND_COMMITTERS_AND_ISSUE_ASSIGNEES\tC24_assigned\t" + 
					"C25_WITH_AT_LEAST_ONE_SO_Q_OR_A_PROJECT_MEMBERS_AND_COMMITTERS_AND_ISSUE_ASSIGNEES_AND_ISSUE_REPORTERS\tC25_assigned\t" +
					"C31_NO_CHECKING_FOR_SO_ACTIVITY_JUST_All_ISSUE_ASSIGNEES\t" + 
					"C32_NO_CHECKING_FOR_SO_ACTIVITY_JUST_PROJECT_MEMBERS\tC32_assigned\t" + 
					"C33_NO_CHECKING_FOR_SO_ACTIVITY_JUST_PROJECT_MEMBERS_AND_COMMITTERS\tC33_assigned\t" + 
					"C34_NO_CHECKING_FOR_SO_ACTIVITY_JUST_PROJECT_MEMBERS_AND_COMMITTERS_AND_ISSUE_ASSIGNEES\tC34_assigned\t" + 
					"C35_NO_CHECKING_FOR_SO_ACTIVITY_JUST_PROJECT_MEMBERS_AND_COMMITTERS_AND_ISSUE_ASSIGNEES_AND_ISSUE_REPORTERS\tC35_assigned\n");
			i=0;
			Iterator<Map.Entry<String, String[]>> mySQLProjectsIterator3 = mySQLProjects1.entrySet().iterator();
			while (mySQLProjectsIterator3.hasNext()){
				Map.Entry<String, String[]> aMySQLProject = mySQLProjectsIterator3.next();
				String mySQLProjectId = aMySQLProject.getKey();
				MySQLProject mySQLP = ProvideData.copyMySQLProjectFromStringArray_OnlyThereAreSomeFields1(aMySQLProject.getValue());
				int numberOfMembersOfTheProject = 0,
					numberOfCommunity11Members = 0, numberOfCommunity12Members = 0, numberOfCommunity13Members = 0, numberOfCommunity14Members = 0, numberOfCommunity15Members = 0,  
					numberOfCommunity21Members = 0, numberOfCommunity22Members = 0, numberOfCommunity23Members = 0, numberOfCommunity24Members = 0, numberOfCommunity25Members = 0, 
					numberOfCommunity31Members = 0, numberOfCommunity32Members = 0, numberOfCommunity33Members = 0, numberOfCommunity34Members = 0, numberOfCommunity35Members = 0, 
					numberOfCommunity12Members_assigned = 0, numberOfCommunity13Members_assigned = 0, numberOfCommunity14Members_assigned = 0, numberOfCommunity15Members_assigned = 0, 
					numberOfCommunity22Members_assigned = 0, numberOfCommunity23Members_assigned = 0, numberOfCommunity24Members_assigned = 0, numberOfCommunity25Members_assigned = 0, 
					numberOfCommunity32Members_assigned = 0, numberOfCommunity33Members_assigned = 0, numberOfCommunity34Members_assigned = 0, numberOfCommunity35Members_assigned = 0; 
				//See which users are to be considered in this project:	
				HashSet <String> neededMySQLUsedIdsForThisProject = new HashSet<String>();
				//Adding mySQLId of assignees and reporters to neededMySQLUsedIdsForThisProject:
				ArrayList<Issue> issAL1 = ProvideData.copyIssuesFromArrayListOfStringArray_OnlyThereAreSomeFields1(
						projectsOfAssignedIssuesAndTheirIssuesInformation.get(mySQLP.ownerLoginAndProjectName));
				//Adding mySQLId of assignees to neededMySQLUsedIdsForThisProject:
				for (int j=0; j< issAL1.size(); j++){//for all issues in this project
					Issue iss = issAL1.get(j);
					if (commonUsers2ByLogin.containsKey(iss.assigneeLogin)){
						CommonUser cu = ProvideData.copyCommonUserFromStringArray_OnlyThereAreSomeFields1(commonUsers2ByLogin.get(iss.assigneeLogin));
						neededMySQLUsedIdsForThisProject.add(cu.mySQLId);
					}
				}//for.
				//Adding mySQLId of reporters to neededMySQLUsedIdsForThisProject:
				for (int j=0; j< issAL1.size(); j++){//for all issues in this project
					Issue iss = issAL1.get(j);
					if (commonUsers2ByLogin.containsKey(iss.reporterLogin)){
						CommonUser cu = ProvideData.copyCommonUserFromStringArray_OnlyThereAreSomeFields1(commonUsers2ByLogin.get(iss.reporterLogin));
						neededMySQLUsedIdsForThisProject.add(cu.mySQLId);
					}
				}//for.

				//Adding mySQLId of project members to neededMySQLUsedIdsForThisProject:
				if (projectsAndTheirMembers.containsKey(mySQLProjectId)){
					ArrayList<ProjectAndItsMembers> pmAL1 = ProvideData.copyProjectsAndItsMembersFromArrayListOfStringArray_OnlyThereAreSomeFields1(
							projectsAndTheirMembers.get(mySQLProjectId));
					for (int j=0; j< pmAL1.size(); j++){//for all members in this project
						ProjectAndItsMembers pm = pmAL1.get(j);
						if (commonUsers1ByMySQLId.containsKey(pm.mySQLUserId))
							neededMySQLUsedIdsForThisProject.add(pm.mySQLUserId);
					}//for (j....
					numberOfMembersOfTheProject = pmAL1.size();
				}
				
				//Adding mySQLId of committers to neededMySQLUsedIdsForThisProject:
				if (projectsAndTheirCommitters.containsKey(mySQLProjectId)){
					ArrayList<ProjectAndItsCommitters> pcAL1 = ProvideData.copyProjectAndItsCommittersFromArrayListOfStringArray_OnlyThereAreSomeFields1(
							projectsAndTheirCommitters.get(mySQLProjectId));
					for (int j=0; j< pcAL1.size(); j++){//for all members in this project
						ProjectAndItsCommitters pc = pcAL1.get(j);
						if (commonUsers1ByMySQLId.containsKey(pc.committerId))
							neededMySQLUsedIdsForThisProject.add(pc.committerId);
					}//for (j....
				}
				//Now, considering all the users in neededMySQLUsedIdsForThisProject hashSet (that are project member/committer/assignee/reporter), 
					//see if they have each role (project member/committer/assignee/reporter) in this project, add them in the related community: 
				for (String mySQLUserId: neededMySQLUsedIdsForThisProject){
					CommonUser cu = ProvideData.copyCommonUserFromStringArray_OnlyThereAreSomeFields1(commonUsers1ByMySQLId.get(mySQLUserId));
					boolean thisUserIsIssueAssignee = false, thisUserIsProjectMember = false, thisUserIsCommitter = false, thisUserisIssueReporter = false; 
					//Issue assignees:
					if (projectsOfAssignedIssuesAndTheirIssuesInformation.containsKey(mySQLP.ownerLoginAndProjectName)){//means if this project has at least one assignee:
						for (int j=0; j< issAL1.size(); j++){//for all issues in this project
							Issue iss = issAL1.get(j);
							if (cu.login.equals(iss.assigneeLogin)){//if this cu is assigned
								thisUserIsIssueAssignee = true;
								break;
							}//if (cu.login....
						}//for (j....
					}//if (proj....
					//Issue reporters: 
					if (projectsOfAssignedIssuesAndTheirIssuesInformation.containsKey(mySQLP.ownerLoginAndProjectName)){//means if this project has at least one reporter (whose reporter issue is actually assigned to someone):
							for (int j=0; j< issAL1.size(); j++){//for all issues in this project
								Issue iss = issAL1.get(j);
								if (cu.login.equals(iss.reporterLogin)){//if this cu is issue reporter
									thisUserisIssueReporter = true;
									break;
								}//if (cu.login....
							}//for (j....
					}//if (proj....
					//Project members:
					if (projectsAndTheirMembers.containsKey(mySQLProjectId)){ //Means that there is at least one member in this project:
						ArrayList<ProjectAndItsMembers> pmAL2 = ProvideData.copyProjectsAndItsMembersFromArrayListOfStringArray_OnlyThereAreSomeFields1(
								projectsAndTheirMembers.get(mySQLProjectId));
						for (int j=0; j< pmAL2.size(); j++){//for all members in this project
							ProjectAndItsMembers pm = pmAL2.get(j);
							if (cu.mySQLId.equals(pm.mySQLUserId)){//if this cu is a member
								thisUserIsProjectMember = true;
								break;
							}//if (cu.mySQLId....
						}//for (j....
					}//if (proj....
					//Committers:
					if (projectsAndTheirCommitters.containsKey(mySQLProjectId)){ //Means that there is at least one committer in this project:
						ArrayList<ProjectAndItsCommitters> pcAL2 = ProvideData.copyProjectAndItsCommittersFromArrayListOfStringArray_OnlyThereAreSomeFields1(
								projectsAndTheirCommitters.get(mySQLProjectId));
						for (int j=0; j< pcAL2.size(); j++){//for all members in this project
							ProjectAndItsCommitters pc = pcAL2.get(j);
							if (cu.mySQLId.equals(pc.committerId)){//if this cu is a member
								thisUserIsCommitter = true;
								break;
							}//if (cu.mySQLId....
						}//for (j....
					}//if (proj....

					boolean thisUserAskedOrAnswered = false, thisUserAnswered = false;
					//Checking if the user has activity on SO, and setting some variables for further use (in order to avoid multiple calling of ".contains()"):
					if (allWhoAskedOrAnswered.contains(cu.SOId)){
						thisUserAskedOrAnswered = true;
						if (allWhoAnswered.contains(cu.SOId))
							thisUserAnswered = true;
					}//if (allW....
					
					//Now, writing the community members in the 15 separate files:
					//Communities 31, 21 and 11 (Those who are issue assignee):
					if (thisUserIsIssueAssignee){//means that this user (cu) is is an assignee in this project.
						numberOfCommunity31Members++;
						writer31.append(mySQLProjectId + "\t" + mySQLP.ownerLoginAndProjectName + "\t" + cu.SOId + "\t" + cu.login + "\t" + cu.mySQLId + "\t" + cu.mongoDBId + "\n");
						if (thisUserAskedOrAnswered){//means that this proj member that is a common user also answerer.
							numberOfCommunity21Members++;
							writer21.append(mySQLProjectId + "\t" + mySQLP.ownerLoginAndProjectName + "\t" + cu.SOId + "\t" + cu.login + "\t" + cu.mySQLId + "\t" + cu.mongoDBId + "\n");
						}//if (allW....
						if (thisUserAnswered){//means that this proj member that is a common user also asker/replier.
							numberOfCommunity11Members++;
							writer11.append(mySQLProjectId + "\t" + mySQLP.ownerLoginAndProjectName + "\t" + cu.SOId + "\t" + cu.login + "\t" + cu.mySQLId + "\t" + cu.mongoDBId + "\n");
						}//if (allA....
					}//if (condition1).
					//Communities 32, 22 and 12 (Those who are Project member):
					if (thisUserIsProjectMember){//means that this user (cu) is is a member in this project.
						numberOfCommunity32Members++;
						writer32.append(mySQLProjectId + "\t" + mySQLP.ownerLoginAndProjectName + "\t" + cu.SOId + "\t" + cu.login + "\t" + cu.mySQLId + "\t" + cu.mongoDBId + "\n");
						if (thisUserIsIssueAssignee)
							numberOfCommunity32Members_assigned++;
						if (thisUserAskedOrAnswered){//means that this proj member that is a common user also answerer.
							numberOfCommunity22Members++;
							writer22.append(mySQLProjectId + "\t" + mySQLP.ownerLoginAndProjectName + "\t" + cu.SOId + "\t" + cu.login + "\t" + cu.mySQLId + "\t" + cu.mongoDBId + "\n");
							if (thisUserIsIssueAssignee)
								numberOfCommunity22Members_assigned++;
						}//if (allW....
						if (thisUserAnswered){//means that this proj member that is a common user also asker/replier.
							numberOfCommunity12Members++;
							writer12.append(mySQLProjectId + "\t" + mySQLP.ownerLoginAndProjectName + "\t" + cu.SOId + "\t" + cu.login + "\t" + cu.mySQLId + "\t" + cu.mongoDBId + "\n");
							if (thisUserIsIssueAssignee)
								numberOfCommunity12Members_assigned++;
						}//if (allW....
					}//if (thisUserIsProjectMember).
					//Communities 33, 23 and 13 (Those who are Project member or committer):
					if (thisUserIsProjectMember || thisUserIsCommitter){//means that this user (cu) is is a member in this project.
						numberOfCommunity33Members++;
						writer33.append(mySQLProjectId + "\t" + mySQLP.ownerLoginAndProjectName + "\t" + cu.SOId + "\t" + cu.login + "\t" + cu.mySQLId + "\t" + cu.mongoDBId + "\n");
						if (thisUserIsIssueAssignee)
							numberOfCommunity33Members_assigned++;
						if (thisUserAskedOrAnswered){
							numberOfCommunity23Members++;
							writer23.append(mySQLProjectId + "\t" + mySQLP.ownerLoginAndProjectName + "\t" + cu.SOId + "\t" + cu.login + "\t" + cu.mySQLId + "\t" + cu.mongoDBId + "\n");
							if (thisUserIsIssueAssignee)
								numberOfCommunity23Members_assigned++;
						}//if (allW....
						if (thisUserAnswered){
							numberOfCommunity13Members++;
							writer13.append(mySQLProjectId + "\t" + mySQLP.ownerLoginAndProjectName + "\t" + cu.SOId + "\t" + cu.login + "\t" + cu.mySQLId + "\t" + cu.mongoDBId + "\n");
							if (thisUserIsIssueAssignee)
								numberOfCommunity13Members_assigned++;
						}//if (allW....
					}//if (thisUserIsProjectMember || ...).
					//Communities 34, 24 and 14 (Those who are Project member or committer or assignee):
					if (thisUserIsProjectMember || thisUserIsCommitter || thisUserIsIssueAssignee){//means that this user (cu) is is a member/assignee/committer in this project.
						numberOfCommunity34Members++;
						writer34.append(mySQLProjectId + "\t" + mySQLP.ownerLoginAndProjectName + "\t" + cu.SOId + "\t" + cu.login + "\t" + cu.mySQLId + "\t" + cu.mongoDBId + "\n");
						if (thisUserIsIssueAssignee)
							numberOfCommunity34Members_assigned++;
						if (thisUserAskedOrAnswered){
							numberOfCommunity24Members++;
							writer24.append(mySQLProjectId + "\t" + mySQLP.ownerLoginAndProjectName + "\t" + cu.SOId + "\t" + cu.login + "\t" + cu.mySQLId + "\t" + cu.mongoDBId + "\n");
							if (thisUserIsIssueAssignee)
								numberOfCommunity24Members_assigned++;
						}//if (allW....
						if (thisUserAnswered){
							numberOfCommunity14Members++;
							writer14.append(mySQLProjectId + "\t" + mySQLP.ownerLoginAndProjectName + "\t" + cu.SOId + "\t" + cu.login + "\t" + cu.mySQLId + "\t" + cu.mongoDBId + "\n");
							if (thisUserIsIssueAssignee)
								numberOfCommunity14Members_assigned++;
						}//if (allW....
					}//if (thisUserIsProjectMember || ...).
					//Communities 35, 25 and 15 (Those who are Project member or committer or assignee or reporter):
					if (thisUserIsProjectMember || thisUserIsCommitter || thisUserIsIssueAssignee || thisUserisIssueReporter){//means that this user (cu) is is a member/assignee/committer/reporter in this project.
						numberOfCommunity35Members++;
						writer35.append(mySQLProjectId + "\t" + mySQLP.ownerLoginAndProjectName + "\t" + cu.SOId + "\t" + cu.login + "\t" + cu.mySQLId + "\t" + cu.mongoDBId + "\n");
						if (thisUserIsIssueAssignee)
							numberOfCommunity35Members_assigned++;
						if (thisUserAskedOrAnswered){
							numberOfCommunity25Members++;
							writer25.append(mySQLProjectId + "\t" + mySQLP.ownerLoginAndProjectName + "\t" + cu.SOId + "\t" + cu.login + "\t" + cu.mySQLId + "\t" + cu.mongoDBId + "\n");
							if (thisUserIsIssueAssignee)
								numberOfCommunity25Members_assigned++;
						}//if (allW....
						if (thisUserAnswered){
							numberOfCommunity15Members++;
							writer15.append(mySQLProjectId + "\t" + mySQLP.ownerLoginAndProjectName + "\t" + cu.SOId + "\t" + cu.login + "\t" + cu.mySQLId + "\t" + cu.mongoDBId + "\n");
							if (thisUserIsIssueAssignee)
								numberOfCommunity15Members_assigned++;
						}//if (allW....
					}//if (thisUserIsProjectMember || ...).
				}//for (String mySQLUserId: neededMySQLUsedIdsForThisProject).
				//Now, writing the summary of community members in the summary files:
				if (numberOfCommunity11Members>0 || numberOfCommunity12Members>0 || numberOfCommunity13Members>0 || numberOfCommunity14Members>0 || numberOfCommunity15Members>0 || 
						numberOfCommunity21Members>0 || numberOfCommunity22Members>0 || numberOfCommunity23Members>0 || numberOfCommunity24Members>0 || numberOfCommunity25Members>0 || 
						numberOfCommunity31Members>0 || numberOfCommunity32Members>0 || numberOfCommunity33Members>0 || numberOfCommunity34Members>0 || numberOfCommunity35Members>0)
					writer_Summary.append(mySQLProjectId + "\t" + mySQLP.ownerLoginAndProjectName + "\t" + numberOfMembersOfTheProject + "\t" + 
							numberOfCommunity11Members + "\t" +  
							numberOfCommunity12Members + "\t" + numberOfCommunity12Members_assigned + "\t" + 
							numberOfCommunity13Members + "\t" + numberOfCommunity13Members_assigned + "\t" + 
							numberOfCommunity14Members + "\t" + numberOfCommunity14Members_assigned + "\t" + 
							numberOfCommunity15Members + "\t" + numberOfCommunity15Members_assigned + "\t" + 
							numberOfCommunity21Members + "\t" +  
							numberOfCommunity22Members + "\t" + numberOfCommunity22Members_assigned + "\t" + 
							numberOfCommunity23Members + "\t" + numberOfCommunity23Members_assigned + "\t" + 
							numberOfCommunity24Members + "\t" + numberOfCommunity24Members_assigned + "\t" + 
							numberOfCommunity25Members + "\t" + numberOfCommunity25Members_assigned + "\t" + 
							numberOfCommunity31Members + "\t" +  
							numberOfCommunity32Members + "\t" + numberOfCommunity32Members_assigned + "\t" + 
							numberOfCommunity33Members + "\t" + numberOfCommunity33Members_assigned + "\t" + 
							numberOfCommunity34Members + "\t" + numberOfCommunity34Members_assigned + "\t" + 
							numberOfCommunity35Members + "\t" + numberOfCommunity35Members_assigned + "\n");						
				mySQLProjectsIterator3.remove();
				//if (projectsAndTheirMembers.containsKey(mySQLProjectId))
				projectsAndTheirMembers.remove(mySQLProjectId);
				//if (projectsOfAssignedIssuesAndTheirIssuesInformation.containsKey(mySQLP.ownerLoginAndProjectName))
				projectsOfAssignedIssuesAndTheirIssuesInformation.remove(mySQLP.ownerLoginAndProjectName);
				i++;
				if (testOrReal > Constants.THIS_IS_REAL)
					if (i >= testOrReal)
						break;
				if (i % (showProgressInterval/100) == 0)
					System.out.println("    Projects examined: " +  Constants.integerFormatter.format(i));
			}//for (String mySQLProjectId: mySQLProjects_byId.keySet()).

			writer11.flush(); 	writer11.close();	writer12.flush(); 	writer12.close();	writer13.flush(); 	writer13.close();	writer14.flush(); 	writer14.close();	writer15.flush(); 	writer15.close();
			writer21.flush(); 	writer21.close();	writer22.flush(); 	writer22.close();	writer23.flush(); 	writer23.close();	writer24.flush(); 	writer24.close();	writer25.flush(); 	writer25.close();
			writer31.flush(); 	writer31.close();	writer32.flush(); 	writer32.close();	writer33.flush(); 	writer33.close();	writer34.flush(); 	writer34.close();	writer35.flush(); 	writer35.close();
			writer_Summary.flush(); 	writer_Summary.close();
			System.out.println("    15 files for communities plus a summary file were written successfully.");
			System.out.println("    Finished.");
			System.out.println();
			System.out.println("End of building communities.");
		}catch(Exception e){
			e.printStackTrace();
		}
	}//buildCommunityOfUsers().
	//--------------------------------------------------------------------------------------------------------------------------------------------
	public static void sortAllCommunitiesBasedOnNumberOfMembers(String communityInputPath, String communityInpputTSVFileName_NoExtension, String communitiesSummaryInputTSV,
			String communityOututPath, String communityOutputTSVFileName_NoExtension, 
			int showProgressInterval, long testOrReal){//This method sorts all the communities based on their number of members (descendng). No change in summary (just reads it for the number of members).
		try{
			String[] communityNumbers = {Constants.COMMUNITY_11_WITH_AT_LEAST_ONE_SO_A_JUST_All_ISSUE_ASSIGNEES,
					Constants.COMMUNITY_12_WITH_AT_LEAST_ONE_SO_A_PROJECT_MEMBERS, 
					Constants.COMMUNITY_13_WITH_AT_LEAST_ONE_SO_A_PROJECT_MEMBERS_AND_COMMITTERS, 
					Constants.COMMUNITY_14_WITH_AT_LEAST_ONE_SO_A_PROJECT_MEMBERS_AND_COMMITTERS_AND_ISSUE_ASSIGNEES, 
					Constants.COMMUNITY_15_WITH_AT_LEAST_ONE_SO_A_PROJECT_MEMBERS_AND_COMMITTERS_AND_ISSUE_ASSIGNEES_AND_ISSUE_REPORTERS, 

					Constants.COMMUNITY_21_WITH_AT_LEAST_ONE_SO_Q_OR_A_JUST_All_ISSUE_ASSIGNEES, 
					Constants.COMMUNITY_22_WITH_AT_LEAST_ONE_SO_Q_OR_A_PROJECT_MEMBERS, 
					Constants.COMMUNITY_23_WITH_AT_LEAST_ONE_SO_Q_OR_A_PROJECT_MEMBERS_AND_COMMITTERS, 
					Constants.COMMUNITY_24_WITH_AT_LEAST_ONE_SO_Q_OR_A_PROJECT_MEMBERS_AND_COMMITTERS_AND_ISSUE_ASSIGNEES, 
					Constants.COMMUNITY_25_WITH_AT_LEAST_ONE_SO_Q_OR_A_PROJECT_MEMBERS_AND_COMMITTERS_AND_ISSUE_ASSIGNEES_AND_ISSUE_REPORTERS, 

					Constants.COMMUNITY_31_NO_CHECKING_FOR_SO_ACTIVITY_JUST_All_ISSUE_ASSIGNEES, 
					Constants.COMMUNITY_32_NO_CHECKING_FOR_SO_ACTIVITY_JUST_PROJECT_MEMBERS, 
					Constants.COMMUNITY_33_NO_CHECKING_FOR_SO_ACTIVITY_JUST_PROJECT_MEMBERS_AND_COMMITTERS, 
					Constants.COMMUNITY_34_NO_CHECKING_FOR_SO_ACTIVITY_JUST_PROJECT_MEMBERS_AND_COMMITTERS_AND_ISSUE_ASSIGNEES, 
					Constants.COMMUNITY_35_NO_CHECKING_FOR_SO_ACTIVITY_JUST_PROJECT_MEMBERS_AND_COMMITTERS_AND_ISSUE_ASSIGNEES_AND_ISSUE_REPORTERS};
			int columnCounter = 0;
			int i;
			int[] keyFieldNumberInSummaryFileToRead = {3, 4, 6, 8, 10,      12, 13, 15, 17, 19,      21, 22, 24, 26, 28};//Since we have another column added in the communitiesSummary for the number of assigned users in that community (except for first community in each group above), we want to jump from those columns.

			for (columnCounter=0; columnCounter<15; columnCounter++){
				String communityInputFileName = communityInpputTSVFileName_NoExtension + communityNumbers[columnCounter] + ".tsv";
				String communityOutputFileName = communityOutputTSVFileName_NoExtension + communityNumbers[columnCounter] + ".tsv";
				//Reading the summary file for community 11, 12, 13, ... or 35:
				System.out.println(3*columnCounter+1 + "- Reading summary for column #: " + columnCounter + " (" + communityInputFileName + "):");
				TreeMap<String, ArrayList<String[]>> numberAndTheCommunitiesWithThisNumberOfMembers = TSVManipulations.readNonUniqueKeyAndItsValueFromTSV(
						communityInputPath, communitiesSummaryInputTSV, null, keyFieldNumberInSummaryFileToRead[columnCounter], SortOrder.DESCENDING_INTEGER , 30, "0$1", ConditionType.NO_CONDITION, 0, "", 0, "", showProgressInterval, testOrReal, 3*columnCounter+1);
				
				//Reading the community file 11, 12, ... or 35:
				System.out.println(3*columnCounter+2 + "- Reading and sorting file: " + communityInputFileName + " based on number of members:");
				TreeMap<String, ArrayList<String[]>> mySQLProjectIdAndItsMembers = TSVManipulations.readNonUniqueKeyAndItsValueFromTSV(
						communityInputPath, communityInputFileName, null, 0, SortOrder.ASCENDING_INTEGER , 6, "ALL", ConditionType.NO_CONDITION, 0, "", 0, "", showProgressInterval, testOrReal, 3*columnCounter+2);
				
				//Starting from the top numberOfProjectmembers, write the members in output files:
				System.out.println(3*columnCounter+3 + "- Writing in sorted community file (" + communityOutputFileName + "):");
				FileWriter writer = new FileWriter(communityOututPath + "\\" + communityOutputFileName);
				writer.append("mySQLProjectId\townerLogin/projName\tSOId\tghLogin\tghMySQLId\tghMongoDBId\n");
				i=0;
				for (String aNumberOfProjectMembers:numberAndTheCommunitiesWithThisNumberOfMembers.keySet()){//For all numberOfMembers in decreasing order:
					ArrayList<CommunitiesSummary> csAL1 = ProvideData.copyCommunitiesSummaryFromArrayListOfStringArray_OnlyThereAreSomeFields1(
							numberAndTheCommunitiesWithThisNumberOfMembers.get(aNumberOfProjectMembers));
					if (!aNumberOfProjectMembers.equals("0")){
						System.out.println("    Number of Members: " + aNumberOfProjectMembers);
						for (int j=0; j<csAL1.size(); j++){//All these projects are with the same number of members:
							String mySQLProjectId = csAL1.get(j).mySQLProjectId;
							if (mySQLProjectIdAndItsMembers.containsKey(mySQLProjectId)){
								ArrayList<CommunityMember> communityMembers = ProvideData.copyCommunityFromArrayListOfStringArray(mySQLProjectIdAndItsMembers.get(mySQLProjectId));
								for (int k=0; k<communityMembers.size(); k++){//For all the members of this project:
									CommunityMember aCommunityMember = communityMembers.get(k);
									writer.append(aCommunityMember.mySQLProjectId + "\t" + 
											aCommunityMember.ownerLoginAndProjectName + "\t" + 
											aCommunityMember.SOId + "\t" + 
											aCommunityMember.ghLogin + "\t" + 
											aCommunityMember.ghMySQLId + "\t" + 
											aCommunityMember.ghMongoDBId + "\n");
								}//for (j.
							}
							else{
								System.out.println("Weird error: The communitesSummary file contains a project, but the community file does not! (maybe due to \"testOrReal\"=THIS_IS_A_TEST)");
								System.out.println(mySQLProjectId);
								break;
							}//else.
						}//for (i.
					}//if (!aNumberOfProjectMembers.equals("0")).
					i++;
					if (testOrReal > Constants.THIS_IS_REAL)
						if (i >= 1) //instead of i >= testOrReal (because we want to write just the top project's members for testing)
							break;
					if (i % showProgressInterval == 0)
						System.out.println("    number of numberOfMembers (including all projects in that range) examined: " +  Constants.integerFormatter.format(i));
				}//for.
				writer.flush(); 	writer.close();
				System.out.println("===========================================");
				System.out.println("===========================================");
			}//for.
		}catch (Exception e){
			e.printStackTrace();
		}
	}//sortAllCommunitiesBasedOnNumberOfMembers
	//--------------------------------------------------------------------------------------------------------------------------------------------
	public static void copySpecificCommunitiesThatAreInCommunitiesSummary(String communityInputPath, String communityInpputTSVFileName_NoExtension, String communitiesSummaryInputTSV,
			String communityOututPath, String communityOutputTSVFileName_NoExtension, 
			int showProgressInterval, long testOrReal){//This method sorts all the communities based on their number of members (descendng). No change in summary (just reads it for the number of members).
		try{
			String[] communityNumbers = {Constants.COMMUNITY_11_WITH_AT_LEAST_ONE_SO_A_JUST_All_ISSUE_ASSIGNEES,
					Constants.COMMUNITY_12_WITH_AT_LEAST_ONE_SO_A_PROJECT_MEMBERS, 
					Constants.COMMUNITY_13_WITH_AT_LEAST_ONE_SO_A_PROJECT_MEMBERS_AND_COMMITTERS, 
					Constants.COMMUNITY_14_WITH_AT_LEAST_ONE_SO_A_PROJECT_MEMBERS_AND_COMMITTERS_AND_ISSUE_ASSIGNEES, 
					Constants.COMMUNITY_15_WITH_AT_LEAST_ONE_SO_A_PROJECT_MEMBERS_AND_COMMITTERS_AND_ISSUE_ASSIGNEES_AND_ISSUE_REPORTERS, 

					Constants.COMMUNITY_21_WITH_AT_LEAST_ONE_SO_Q_OR_A_JUST_All_ISSUE_ASSIGNEES, 
					Constants.COMMUNITY_22_WITH_AT_LEAST_ONE_SO_Q_OR_A_PROJECT_MEMBERS, 
					Constants.COMMUNITY_23_WITH_AT_LEAST_ONE_SO_Q_OR_A_PROJECT_MEMBERS_AND_COMMITTERS, 
					Constants.COMMUNITY_24_WITH_AT_LEAST_ONE_SO_Q_OR_A_PROJECT_MEMBERS_AND_COMMITTERS_AND_ISSUE_ASSIGNEES, 
					Constants.COMMUNITY_25_WITH_AT_LEAST_ONE_SO_Q_OR_A_PROJECT_MEMBERS_AND_COMMITTERS_AND_ISSUE_ASSIGNEES_AND_ISSUE_REPORTERS, 

					Constants.COMMUNITY_31_NO_CHECKING_FOR_SO_ACTIVITY_JUST_All_ISSUE_ASSIGNEES, 
					Constants.COMMUNITY_32_NO_CHECKING_FOR_SO_ACTIVITY_JUST_PROJECT_MEMBERS, 
					Constants.COMMUNITY_33_NO_CHECKING_FOR_SO_ACTIVITY_JUST_PROJECT_MEMBERS_AND_COMMITTERS, 
					Constants.COMMUNITY_34_NO_CHECKING_FOR_SO_ACTIVITY_JUST_PROJECT_MEMBERS_AND_COMMITTERS_AND_ISSUE_ASSIGNEES, 
					Constants.COMMUNITY_35_NO_CHECKING_FOR_SO_ACTIVITY_JUST_PROJECT_MEMBERS_AND_COMMITTERS_AND_ISSUE_ASSIGNEES_AND_ISSUE_REPORTERS};
			
			int columnCounter = 0;
			int i;
			//int[] keyFieldNumberInSummaryFileToRead = {3, 4, 6, 8, 10,      12, 13, 15, 17, 19,      21, 22, 24, 26, 28};//Since we have another column added in the communitiesSummary for the number of assigned users in that community (except for first community in each group above), we want to jump from those columns.
			//Keeping Arie's projects in a hashSet:
			HashSet<String> ariesProjects = TSVManipulations.readUniqueFieldFromTSV(communityInputPath, communitiesSummaryInputTSV, 1, 30, ConditionType.NO_CONDITION, 0, "", 0, "", showProgressInterval, testOrReal, 1);

			//Reading and filtering 15 communities based on projects in communitiesSummary:
			System.out.println("Reading and filtering community files based on projects in communitiesSummary and writing in file \"" + communityOutputTSVFileName_NoExtension + "#.tsv\":");
			for (columnCounter=0; columnCounter<15; columnCounter++){
				String communityInputFileName = communityInpputTSVFileName_NoExtension + communityNumbers[columnCounter] + ".tsv";
				String communityOutputFileName = communityOutputTSVFileName_NoExtension + communityNumbers[columnCounter] + ".tsv";
				//Reading the community file 11, 12, ... or 35:
				TreeMap<String, ArrayList<String[]>> project_AndItsMembers = TSVManipulations.readNonUniqueKeyAndItsValueFromTSV( //mySQLProjectIdAndItsMembers
						communityInputPath, communityInputFileName, ariesProjects, 1, SortOrder.DEFAULT_FOR_STRING , 6, "ALL", ConditionType.NO_CONDITION, 0, "", 0, "", showProgressInterval, testOrReal, columnCounter+1);
				
				FileWriter writer = new FileWriter(communityOututPath + "\\" + communityOutputFileName);
				writer.append("mySQLProjectId\townerLogin/projName\tSOId\tghLogin\tghMySQLId\tghMongoDBId\n");
				i=0;
				
				for (String aLoginAndProjectName: project_AndItsMembers.keySet()){
					ArrayList<CommunityMember> communityMembers = ProvideData.copyCommunityFromArrayListOfStringArray(project_AndItsMembers.get(aLoginAndProjectName));
					for (int k=0; k<communityMembers.size(); k++){//For all the members of this project:
						CommunityMember aCommunityMember = communityMembers.get(k);
						writer.append(aCommunityMember.mySQLProjectId + "\t" + 
								aCommunityMember.ownerLoginAndProjectName + "\t" + 
								aCommunityMember.SOId + "\t" + 
								aCommunityMember.ghLogin + "\t" + 
								aCommunityMember.ghMySQLId + "\t" + 
								aCommunityMember.ghMongoDBId + "\n");
					}//for (j.
					i++;
					if (testOrReal > Constants.THIS_IS_REAL)
						if (i >= 1) //instead of i >= testOrReal (because we want to write just the top project's members for testing)
							break;
					if (i % showProgressInterval == 0)
						System.out.println("    number of numberOfMembers (including all projects in that range) examined: " +  Constants.integerFormatter.format(i));
					System.out.println("Project " + i + " was filtered.");
				}//for.
				writer.flush(); 	writer.close();
				System.out.println("===========================================");
			}//for.
			
			//Sorting the projects based on number of members:
			sortAllCommunitiesBasedOnNumberOfMembers(communityOututPath, communityOutputTSVFileName_NoExtension, communitiesSummaryInputTSV,
			communityOututPath, communityOutputTSVFileName_NoExtension,
			showProgressInterval, testOrReal);
			
			System.out.println("End of copySpecificCommunitiesThatAreInCommunitiesSummary().");
		}catch (Exception e){
			e.printStackTrace();
		}
	}//sortAllCommunitiesBasedOnNumberOfMembers
	//--------------------------------------------------------------------------------------------------------------------------------------------
	public static void checkTags(){
		
	}//checkTags().
	//--------------------------------------------------------------------------------------------------------------------------------------------
	public static void checkAnswerToSelf(String inputPath, String communitiesSummaryInputTSV, 
			String communityInputTSV, String postsInputTSV,
			String answerToSelfSummaryOututPath, String answerToSelfSummaryOutputFileName, 
			int showProgressInterval, long testOrReal){

	try{
		HashSet<String> ownerLoginAndProjectNames1 = TSVManipulations.readUniqueFieldFromTSV(
				inputPath, communitiesSummaryInputTSV, 1, 3, ConditionType.NO_CONDITION, 0, "", 0, "", showProgressInterval, Constants.THIS_IS_REAL, 1);

		//Read all communities' projects from summary file (maybe just to double check with the community file):
		HashSet<String> ownerLoginAndProjectNames = TSVManipulations.readUniqueFieldFromTSV(
				inputPath, communitiesSummaryInputTSV, 1, 3, ConditionType.NO_CONDITION, 0, "", 0, "", showProgressInterval, Constants.THIS_IS_REAL, 1);
		//Reading the community file:
		TreeMap<String, ArrayList<String[]>> ownerLoginAndProjectName_and_ItsCommunityMembers = TSVManipulations.readNonUniqueKeyAndItsValueFromTSV(
				inputPath, communityInputTSV, null, 1, SortOrder.DEFAULT_FOR_STRING, 6, Constants.ALL, ConditionType.NO_CONDITION, 0, "", 0, "", showProgressInterval, testOrReal, 6);

		//Read info of all posts made by all community members by id: (total: ?)
		TreeMap<String, String[]> posts1ById = TSVManipulations.readUniqueKeyAndItsValueFromTSV(
				inputPath, postsInputTSV, null, 0, 9, "0$1$2$3$4$5", ConditionType.NO_CONDITION, 0, "", 0, "", showProgressInterval*10, testOrReal, 3);

		FileWriter writer = new FileWriter(answerToSelfSummaryOututPath + "\\" + answerToSelfSummaryOutputFileName);
		writer.append("project\tqId\taId\tqVote\taVote\n");
		
		for (String anOwnerLoginAndProjectName: ownerLoginAndProjectNames)
			if (ownerLoginAndProjectName_and_ItsCommunityMembers.containsKey(anOwnerLoginAndProjectName)){//This condition is just for double checking the existence of the records of this project (that has beed read from communitiesSummary) with the info in the community file, and then triaging them:
				int numberOfAnswersToSelf = 0, numberOfQuestionsByCommunityMembers = 0, numberOfAnswersByCommunityMembers = 0;
				//Getting community members:
				ArrayList<CommunityMember> cmAL = ProvideData.copyCommunityFromArrayListOfStringArray(ownerLoginAndProjectName_and_ItsCommunityMembers.get(anOwnerLoginAndProjectName));
				System.out.println("    Checking project \"" + anOwnerLoginAndProjectName + "\"");
				System.out.println("        Total # of community members: " + cmAL.size());

				for(Map.Entry<String, String[]> entry: posts1ById.entrySet()) {
					SOPost post = ProvideData.copySOPostFromStringArray_OnlyThereAreSomeFields1(entry.getValue());
					String ownerUserId = post.ownerUserId;
					for (int k=0; k<cmAL.size(); k++){
						CommunityMember cm = cmAL.get(k);
						if (cm.SOId.equals(ownerUserId) && post.postTypeId.equals("1"))//question
							numberOfQuestionsByCommunityMembers++;
						if (cm.SOId.equals(ownerUserId) && post.postTypeId.equals("2")){ //answer
							numberOfAnswersByCommunityMembers++;
							String questionId = post.parentId;
							SOPost questionPost = ProvideData.copySOPostFromStringArray_OnlyThereAreSomeFields1(posts1ById.get(questionId));
							if (ownerUserId.equals(questionPost.ownerUserId)){
								numberOfAnswersToSelf++;
								writer.append(anOwnerLoginAndProjectName + "\t" + questionId + "\t" + post.id + "\t" + questionPost.Score + "\t" + post.Score + "\n");
//								System.out.println("-----question's vote: " + questionPost.Score);
								//System.out.println("-----answer's vote: " + post.Score);
								break;
							}//if (owner....
						}//if (cm.SOI....
					}// for (k.
				} //for (Map.E....

				System.out.println("            Total questions: " + numberOfQuestionsByCommunityMembers);
				System.out.println("            Total answers: " + numberOfAnswersByCommunityMembers);
				System.out.println("            Total answers to self: " + numberOfAnswersToSelf);
			}//if (owner....
			else{
				System.out.println("            Weird error: The communitesSummary file contains project \"" + anOwnerLoginAndProjectName + "\", but the community file does not! (maybe due to \"testOrReal\"=THIS_IS_A_TEST):");
				break;
			}//else.
		writer.flush(); 	writer.close();
		System.out.println("    Finished.");
	}catch (Exception e){
		e.printStackTrace();
	}
	}//checkAnswerToSelf().
	//--------------------------------------------------------------------------------------------------------------------------------------------
	public static void checkTotalNumberOfIssuesInEachSelectedProject(String ioPath, 
			String selectedProjectsInputTSV, String allProjectsInputTSV,
			String outputTSV, 
			int showProgressInterval, long testOrReal){
		//Selected projects:
		HashSet<String> selectedProjectNames = TSVManipulations.readUniqueFieldFromTSV(
				ioPath, selectedProjectsInputTSV, 1, 3, ConditionType.NO_CONDITION, 0, "", 0, "", showProgressInterval, Constants.THIS_IS_REAL, 1);

		//Read all issues, filtering on the selected projects:
		TreeMap<String, ArrayList<String[]>> projectsAndTheirIssues = TSVManipulations.readNonUniqueKeyAndItsValueFromTSV(
				ioPath, allProjectsInputTSV, selectedProjectNames, 1, SortOrder.DEFAULT_FOR_STRING, 11, Constants.ALL, ConditionType.NO_CONDITION, 0, "", 0, "", showProgressInterval, testOrReal, 2);

		try{
			FileWriter writer = new FileWriter(ioPath + "\\" + outputTSV);
			int i = 1;
			for(Map.Entry<String, ArrayList<String[]>> entry: projectsAndTheirIssues.entrySet()){
				//			ArrayList<String[]> issAL = entry.getValue();			
				System.out.println(i + ") " + entry.getKey() + ": \t\t" + entry.getValue().size());
				writer.append(entry.getKey() + "\t" + entry.getValue().size() + "\n");
				i++;
				//			ArrayList<String[]> issAL = ProvideData.copyIssuesFromArrayListOfStringArray_OnlyThereAreSomeFields3(entry.getValue());
			}//for.
			writer.flush(); writer.close();
		}catch (Exception e){
			e.printStackTrace();
		}


	}//checkTotalNumberOfIssuesInEachSelectedProject().
	//--------------------------------------------------------------------------------------------------------------------------------------------
	//--------------------------------------------------------------------------------------------------------------------------------------------
	public static void main(String[] args) {
		//checkFitnessOfUsersForIssues_1_initial(Constants.DATASET_DIRECTORY_GH_MongoDB_TSV, "users.tsv", "issues.tsv", Constants.DATASET_DIRECTORY_GH_MySQL_TSV, "users3 (only important fields)-fixed.tsv");

		//Run successfully:
		//		checkFitnessOfUsersForProjects_1_initial(Constants.DATASET_DIRECTORY_GH_AND_SO_Mixed_TOGETHER, "commonUsers.tsv", 
		//				Constants.DATASET_DIRECTORY_GH_MySQL_TSV, "projects3 (only important fields) - Cleaned.tsv", 
		//				Constants.DATASET_DIRECTORY_GH_MySQL_TSV, "project_members5 (only important fields - more).tsv", 
		//				Constants.DATASET_DIRECTORY_GH_AND_SO_Mixed_TOGETHER, "projectMembershipsForCommonUsers.tsv", "numberOfMembersInEachProjectForCommonUsers.tsv");

		//Run successfully:
		//		addTotalNumberOfUsersToNumberOfMembersInEachProject(Constants.DATASET_DIRECTORY_GH_MySQL_TSV, "project_members5 (only important fields - more).tsv",
		//				Constants.DATASET_DIRECTORY_GH_AND_SO_Mixed_TOGETHER, "numberOfMembersInEachProjectForCommonUsers.tsv", 
		//				Constants.DATASET_DIRECTORY_GH_AND_SO_Mixed_TOGETHER, "numberOfMembersInEachProjectForCommonUsers2-withTotalMembers.tsv");	

//Run seccessfully:
//		buildCommunitiesOfUsers(Constants.DATASET_DIRECTORY_GH_AND_SO_Mixed_TOGETHER, "commonUsers.tsv",
//				Constants.DATASET_DIRECTORY_GH_MySQL_TSV, "projects2-Cleaned-ownerIdReplacedWithLogin-mergedTwoColumns.tsv",
//				Constants.DATASET_DIRECTORY_GH_MySQL_TSV, "project_members5 (only important fields - more).tsv",
//				Constants.DATASET_DIRECTORY_GH_MySQL_TSV, "commits (only important fields).tsv",
//				Constants.DATASET_DIRECTORY_GH_MongoDB_TSV, "issues-Assigned-mergedTwoColumns.tsv", 
//				Constants.DATASET_DIRECTORY_SO_WITHOUT_EMAIL_HASH, "Posts.tsv",
//				Constants.DATASET_DIRECTORY_COMMUNITY, "community", "communitiesSummary.tsv",
//				100000, Constants.THIS_IS_REAL);

		//Run seccessfully: Perfect!
//		sortAllCommunitiesBasedOnNumberOfMembers(Constants.DATASET_DIRECTORY_COMMUNITY, "community", "communitiesSummary.tsv",
//		Constants.DATASET_DIRECTORY_COMMUNITY, "sortedCommunity",
//		100000, Constants.THIS_IS_REAL);
		
		//Run seccessfully: Perfect!
		//Copy top 20 projects into 15 new files: 
//		copySpecificCommunitiesThatAreInCommunitiesSummary(Constants.DATASET_DIRECTORY_COMMUNITY, "community", "communitiesSummary4-top 500 projects of community25 - Top20ProjectsWithMostNumberOfAssignees.tsv",
//		Constants.DATASET_DIRECTORY_COMMUNITY, "communitiesOf20TopProjects", //for "Arie Van deursen"
//		100000, Constants.THIS_IS_REAL);
		
//		checkAnswerToSelf(Constants.DATASET_DIRECTORY_DATASETS_FOR_TRIAGING, "communitiesSummary6-3TestProjects.tsv", "communitiesOf3TestProjects.tsv", 
//				"Posts-madeByCommunityMembers-top20Projects.tsv", 
//				Constants.DATASET_DIRECTORY_TRIAGE_RESULTS, "answerToSelf.tsv", 
//				100000, Constants.THIS_IS_REAL);
				
				
		checkTotalNumberOfIssuesInEachSelectedProject(Constants.DATASET_DIRECTORY_DATASETS_FOR_TRIAGING, 
				"20Projects.tsv", "allIssues.tsv", 
				"selectedProjectsAndTotalIssues.tsv", 
				200000, Constants.THIS_IS_REAL);
				
				
	}
	//--------------------------------------------------------------------------------------------------------------------------------------------
}
