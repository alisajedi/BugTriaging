package bugTriaging;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Data.CommunityMember;
import Data.Issue;
import Data.ProvideData;
import bugTriaging.Constants.ConditionType;
import bugTriaging.Constants.SortOrder;

public class TSVManipulations {
	//--------------------------------------------------------------------------------------------------------------------------------------------
	//--------------------------------------------------------------------------------------------------------------------------------------------
	//In this method, we want to join two tsv files to make a new one including these fields: <numberOfCommonUsers, projectMySQLId, projectName, projectDescription, projectLanguage>
	//We do not need this method, because I added the join process in CheckFitness.java-->checkFitnessOfUsersForProjects().
	public static void joinProjects_With_NumberOfMembersInEachProject(String projectsInputPath, String projectsInputTSV,
			String numberOfMembersInEachProjectInputPath, String numberOfMembersInEachProjectInputTSV, 
			String outputPath, String numberOfMembersInEachProjectIdAndNameTSV){
		try{ 
			BufferedReader br;
			//Reading needed projectId's and keeping them in a hashSet:
			//This is because all the projects are too much to keep in memory (for next step).
			br = new BufferedReader(new FileReader(numberOfMembersInEachProjectInputPath + "\\" + numberOfMembersInEachProjectInputTSV)); 
			System.out.println("    1- Parsing the needed projects and putting <mySQLProjectId> items in a HashSet --------> Started ...");
			HashSet<String> neededProjectIds = new HashSet<String>();
			int error = 0;
			String[] fields;
			int i=0;
			String s, projectId;
			br.readLine(); //header.
			while ((s=br.readLine())!=null){
				fields = s.split("\t");
				if (fields.length != 3)
					error++;
				else{
					projectId = fields[1];
					neededProjectIds.add(projectId);
				}//else.
				i++;
				if (i % 500000 == 0)
					System.out.println("        " +  Constants.integerFormatter.format(i));
			}//while ((s=br....
			if (error>0)
				System.out.println("        Error) Number of records in \"" +  numberOfMembersInEachProjectInputTSV + "\" with !=3 fields: " + error);
			System.out.println("    1- Parsing the needed projects and putting <mySQLProjectId> items in a HashSet --------> Finished ...");
			br.close();

			//Now, reading all projects' info, Watching if the project is in the above hashSet, then it is a project with more than one common user as member; 
			//So put the name, description and language of this project to a hashMap<projectMySQLId, projectNameAndDescriptionAndLanguage>:
			br = new BufferedReader(new FileReader(projectsInputPath + "\\" + projectsInputTSV)); 
			System.out.println("    2- Extracting useful projects' other info and putting them in a HashMap --------> Started ...");
			HashMap<String, String> projects = new HashMap<String, String>();
			error = 0;
			i=0;
			String projectName, projectDescription, projectLanguage, projectNameAndDescriptionAndLanguage;
			br.readLine(); //header.
			while ((s=br.readLine())!=null){
				fields = s.split("\t");
				if (fields.length != 4)
					error++;
				else{
					projectId = fields[0];
					if (neededProjectIds.contains(projectId)){
						projectName = fields[1];
						projectDescription = fields[2];
						projectLanguage = fields[3];
						projectNameAndDescriptionAndLanguage = projectName + "\t" + projectDescription + "\t" + projectLanguage;
						projects.put(projectId, projectNameAndDescriptionAndLanguage);
					}
				}//else.
				i++;
				if (i % 500000 == 0)
					System.out.println("        " +  Constants.integerFormatter.format(i));
			}//while ((s=br....
			if (error>0)
				System.out.println("        Number of records in \"" +  projectsInputTSV + "\" with error: " + error);
			System.out.println("    2- Extracting useful projects' other info and putting them in a HashMap --------> Finished ...");
			br.close();

			//Now, reading the numberOfMembersInEachProjectInputTSV file again, and mixing the data with the relative row in the above hashMap to add other info of the projects:
			br = new BufferedReader(new FileReader(numberOfMembersInEachProjectInputPath + "\\" + numberOfMembersInEachProjectInputTSV)); 
			System.out.println("    3- Parsing useful projects (sorted by number of common users), and add the needed info from the above hashMap --------> Started ...");
			System.out.println("    4- Building output file --------> Started ...");
			FileWriter writer = new FileWriter(outputPath + "\\" + numberOfMembersInEachProjectIdAndNameTSV);
			writer.append("numberOfCommonUsers\tmySQLProjectId\tprojectName\tprojectDescription\tprojectLanguage\n");
			error = 0;
			i=0;
			String numberOfCommonUsers, numberOfCommonUsersAndMySQLProjectIdAndNameAndDescriptionAndLanguage;
			br.readLine(); //header.
			while ((s=br.readLine())!=null){
				fields = s.split("\t");
				if (fields.length != 3)
					error++;
				else{
					numberOfCommonUsers  = fields[0];
					projectId = fields[1];
					projectNameAndDescriptionAndLanguage = projects.get(projectId);
					numberOfCommonUsersAndMySQLProjectIdAndNameAndDescriptionAndLanguage = numberOfCommonUsers + "\t" + projectId + "\t" + projectNameAndDescriptionAndLanguage;
					writer.append(numberOfCommonUsersAndMySQLProjectIdAndNameAndDescriptionAndLanguage + "\n");
				}//else.
				i++;
				if (i % 500000 == 0)
					System.out.println("        " +  Constants.integerFormatter.format(i));
			}//while ((s=br....
			if (error>0)
				System.out.println("        Number of records in \"" +  numberOfMembersInEachProjectInputTSV + "\" with error: " + error);
			System.out.println("    4- Building output file --------> Finished ...");
			System.out.println("    3- Parsing useful projects (sorted by number of common users), and add the needed info from the above hashMap --------> Finished ...");
			br.close();
			writer.flush();
			writer.close();
			System.out.println("Done.");
		}//try
		catch(Exception e){
			e.printStackTrace();
		}
	}//joinProjects_With_NumberOfMembersInEachProject().
	//--------------------------------------------------------------------------------------------------------------------------------------------
	public static void saveOnlyTheAssignedIssues(String issuesInputPath, String issuesInputTSV,
			String issuesOutputPath, String issuesOutputTSV){
		try{ 
			BufferedReader br;
			//Reading issues, writing those lines with assignee into the output file.
			br = new BufferedReader(new FileReader(issuesInputPath + "\\" + issuesInputTSV)); 
			System.out.println("    1- Parsing file \"" + issuesInputPath + "\\" + issuesInputTSV + "\" and writing the lines with assigneeId in file \"" + issuesOutputPath + "\\" + issuesOutputTSV + "\" --------> Started ...");
			FileWriter writer = new FileWriter(issuesOutputPath + "\\" + issuesOutputTSV);
			writer.append("id\towner\trepo\treporterId\treporterLogin\tassigneeId\tassigneeLogin\tcreated_at\tnumberOfComments\tlabels\ttitle\tbody\n");
			int error = 0;
			String[] fields;
			int i=0;
			String s, assigneeId;
			br.readLine(); //header.
			while ((s=br.readLine())!=null){
				fields = s.split("\t");
				if (fields.length != 12){
					error++;
					System.out.println("----: " + s);	
				}
				else{
					assigneeId = fields[5];
					if (!assigneeId.equals(" ")){
						writer.append(s + "\n");
						//System.out.println("++++: <" + assigneeId + ">   " + s);
					}
				}//else.
				i++;
				if (i % 500000 == 0)
					System.out.println("        " +  Constants.integerFormatter.format(i));
			}//while ((s=br....
			if (error > 0)
				System.out.println("        Number of issues with invalid number of fields: " + error);
			System.out.println("    1- Parsing file \"" + issuesInputPath + "\\" + issuesInputTSV + "\" and writing the lines with assigneeId in file \"" + issuesOutputPath + "\\" + issuesOutputTSV + "\" --------> Finished ...");
			br.close();
			writer.flush();
			writer.close();
			System.out.println("Finished.");
		}
		catch(Exception e){
			e.printStackTrace();
		}//catch.
	}//saveOnlyTheAssignedIssues().
	//--------------------------------------------------------------------------------------------------------------------------------------------
	//This method wants to clean the extra invisible or other invalid characters based on the regex Constants.allValidCharactersInSOTags_ForRegEx
	//There are some other valid characters like ()/\{}
	public static void cleanProjectsTSVFile(String projectsInputPath, String projectsInputTSV, 
			String projectsOutputPath, String projectsOutputCompleteTSV, String projectsOutputShortTSV){
		try{ 
			BufferedReader br;
			//Reading projects, cleaning them, and writing in output file:
			br = new BufferedReader(new FileReader(projectsInputPath + "\\" + projectsInputTSV)); 
			System.out.println("    1- Parsing projects file and cleaning it --------> Started ...");
			FileWriter writer1 = new FileWriter(projectsOutputPath + "\\" + projectsOutputCompleteTSV);
			FileWriter writer2 = new FileWriter(projectsOutputPath + "\\" + projectsOutputShortTSV);
			writer1.append("projectMySQLId\turl\towner_id\tname\tdescription\tlanguage\tcreated_at\text_ref_id\tforked_from\tdeleted\n");
			writer2.append("projectMySQLId\tname\tdescription\tlanguage\n");
			int error = 0, error2=0;
			String[] fields;
			int i=0, j;
			String s, s2;
			Pattern pattern;
			Matcher matcher;
			br.readLine(); //header.
			while ((s=br.readLine())!=null){
				fields = s.split("\t\t");
				if (fields.length < 10){ //This line is not complete (finished with '\n'. 
					error++;
					while (fields.length < 10){
						s2=br.readLine();
						s = s + s2;
						fields = s.split("\t\t");
						error2++;
					}
				}
				//Removing invalid characters from "description":
				//Description:
				pattern = Pattern.compile("[^"+Constants.allValidCharactersInSOTags_ForRegEx+"]+");
				matcher = pattern.matcher(fields[4]);
				if (matcher.find())
					fields[4] = fields[4].replaceAll("[^"+Constants.allValidCharactersInSOTags_ForRegEx+"]+", " ");
				if (fields[4].equals(""))
					fields[4] = " ";

				//Converting '\N' to ' ':
				//language:
				pattern = Pattern.compile("\\\\N");
				matcher = pattern.matcher(fields[5]);
				if (matcher.find())
					fields[5] = fields[5].replaceAll("\\\\N", " ");
				//forked_from:
				matcher = pattern.matcher(fields[8]);
				if (matcher.find())
					fields[8] = fields[8].replaceAll("\\\\N", " ");

				//changing empty or null fields to ' ':
				//ext_ref_id:
				if (fields[7].equals(""))
					fields[7] = " ";
				//We no longer need these lines (there are no null values. It was checked right now):
				//	        	for (j=0; j<fields.length; j++)
				//		        	if (fields[j].equals(null))
				//		        		System.out.println(j);

				//Now, writing the cleaned fields[] to projectsOutputTSV (output project file, that is cleaned):
				for (j=0; j<fields.length-1; j++)
					writer1.append(fields[j] + "\t");
				writer1.append(fields[fields.length - 1] + "\n");

				writer2.append(fields[0] + "\t" + fields[3] + "\t" + fields[4] + "\t" + fields[5] + "\t" + "\n");
				//counter to show:
				i++;
				if (i % 500000 == 0)
					System.out.println("        " +  Constants.integerFormatter.format(i));
			}//while ((s=br....
			if (error > 0)
				System.out.println("        Number of projects (with invalid number of fields) - fixed: " + error);
			if (error2 > 0)
				System.out.println("            Number of fixed lines in these projects: " + error2);
			System.out.println("    1- Parsing projects file and cleaning it --------> Finished.");
			br.close();
			writer1.flush();
			writer1.close();
			writer2.flush();
			writer2.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}//catch.
	}//cleanProjectsFile(...
	//--------------------------------------------------------------------------------------------------------------------------------------------
	public static TreeMap<String, String[]> readUniqueKeyAndItsValueFromTSV(String inputTSVPath, String inputTSVFileName, Set<String> keySetToCheckExistenceOfKeyField, 
			int keyFieldNumber, int totalFieldsCount, String fieldNumbersToBeRead_separatedByDollar, 
			ConditionType conditionType, int field1Number, String field1Value, int field2Number, String field2Value, 
			int showProgressInterval,
			long testOrReal, int writeMessageStep){//This method reads TSV lines into HashMap. The key is a unique field and value is a String[] containing all the values of that row. 
		TreeMap<String, String[]> tsvRecordsHashMap = new TreeMap<String, String[]>();
		try{ 
			BufferedReader br;
			//Reading posts and adding <repoId, totalNumberOfMembers> in a hashMap:
			br = new BufferedReader(new FileReader(inputTSVPath + "\\" + inputTSVFileName)); 
			System.out.println(writeMessageStep + "- Parsing " + inputTSVFileName  + ":");
			System.out.println("    Started ...");
			int error1 = 0, error2 = 0, unmatchedRecords = 0;;
			int i=0, matchedRec = 0;
			String s, keyField;
			boolean recordShouldBeRead;
			br.readLine(); //header.
			while ((s=br.readLine())!=null){
				String[] fields = s.split("\t");
				if (fields.length == totalFieldsCount){
					recordShouldBeRead = MyUtils.runLogicalComparison(conditionType, fields[field1Number], field1Value, fields[field2Number], field2Value);
					if (recordShouldBeRead){
						keyField = fields[keyFieldNumber];
						if (keySetToCheckExistenceOfKeyField == null || keySetToCheckExistenceOfKeyField.contains(keyField)){
							matchedRec++;
							if (!tsvRecordsHashMap.containsKey(keyField)){
								if (fieldNumbersToBeRead_separatedByDollar.equals("ALL"))//means that all the fields are needed.
									tsvRecordsHashMap.put(keyField, fields);
								else{//means that only some of the fields are needed.
									String[] neededFields = fieldNumbersToBeRead_separatedByDollar.split("\\$");
									for (int k=0; k<neededFields.length; k++)
										neededFields[k] = fields[Integer.parseInt(neededFields[k])];
									tsvRecordsHashMap.put(keyField, neededFields);
								}//else.
							}//if.
							else
								error2++;	//System.out.println("----" + keyField + "----" + fields[0] + "\t" + fields[1] + "\t" + fields[2]); //It should have been unique though!
						}//if (KeyS....					
					}
					else
						unmatchedRecords++;
				}//if.
				else
					error1++;
				i++;
				if (testOrReal > Constants.THIS_IS_REAL)
					if (i >= testOrReal)
						break;
				if (i % showProgressInterval == 0)
					System.out.println("        " +  Constants.integerFormatter.format(i));
			}//while ((s=br....
			if (error1>0)
				System.out.println("        Error) Number of records with !=" + totalFieldsCount + " fields: " + error1);
			if (error2>0)
				System.out.println("        Error) Number of records with repeated keyField: " + error2);

			if (conditionType == ConditionType.NO_CONDITION)
				System.out.println("        Number of records read: " + Constants.integerFormatter.format(matchedRec));
			else{
				System.out.println("        Number of records read (matched with the provided conditions): " + Constants.integerFormatter.format(matchedRec));
				if (unmatchedRecords == 0)
					System.out.println("        :-) No unmatched records with the conditions provided.");
				else
					System.out.println("        Number of ignored records (unmatched with the provided conditions): " + Constants.integerFormatter.format(unmatchedRecords));
			}//if (cond....
			System.out.println("    Finished.");
			System.out.println();
			br.close();
		}catch (Exception e){
			e.printStackTrace();
		}
		return tsvRecordsHashMap;
	}
	//--------------------------------------------------------------------------------------------------------------------------------------------
	public static HashSet<String> readNonUniqueFieldFromTSV_OnlyRepeatEachEntryOnce(String inputTSVPath, String inputTSVFileName,
			int keyFieldNumber, int totalFieldsCount,
			ConditionType conditionType, int field1Number, String field1Value, int field2Number, String field2Value, 
			int showProgressInterval,
			long testOrReal, int writeMessageStep){//This method reads only one field in TSV lines into HashSet. The key is a non-unique field. If it sees repeats of that field, just ignores it. 
		HashSet<String> tsvfieldHashSet = new HashSet<String>();
		try{ 
			BufferedReader br;
			//Reading posts and adding <repoId, totalNumberOfMembers> in a hashMap:
			br = new BufferedReader(new FileReader(inputTSVPath + "\\" + inputTSVFileName)); 
			System.out.println(writeMessageStep + "- Parsing " + inputTSVFileName  + ":");
			System.out.println("    Started ...");
			int error1 = 0, unmatchedRecords = 0;
			String[] fields;
			int i=0; 
			int matchedRec = 0;
			String s, keyField;
			boolean recordShouldBeRead;
			br.readLine(); //header.
			while ((s=br.readLine())!=null){
				fields = s.split("\t");
				if (fields.length != totalFieldsCount)
					error1++;
				else{
					recordShouldBeRead = MyUtils.runLogicalComparison(conditionType, fields[field1Number], field1Value, fields[field2Number], field2Value);
					if (recordShouldBeRead){
						keyField = fields[keyFieldNumber];
						if (!tsvfieldHashSet.contains(keyField))
							tsvfieldHashSet.add(keyField);
						matchedRec++;
					}//if (reco....
					else
						unmatchedRecords++;
				}//else.
				i++;
				if (testOrReal > Constants.THIS_IS_REAL)
					if (i >= testOrReal)
						break;
				if (i % showProgressInterval == 0)
					System.out.println("        " +  Constants.integerFormatter.format(i));
			}//while ((s=br....
			if (error1>0)
				System.out.println("        Error) Number of records with !=" + totalFieldsCount + " fields: " + error1);
			if (conditionType == ConditionType.NO_CONDITION)
				System.out.println("        Number of records read: " + Constants.integerFormatter.format(matchedRec));
			else{
				System.out.println("        Number of records read (matched with the provided conditions): " + Constants.integerFormatter.format(matchedRec));
				if (unmatchedRecords == 0)
					System.out.println("        :-) No unmatched records with the conditions provided.");
				else{
					System.out.println("        Number of ignored records (unmatched with the provided conditions): " + Constants.integerFormatter.format(unmatchedRecords));
				}//else.
			}//else.
			System.out.println("    Finished.");
			System.out.println();
			br.close();
		}catch (Exception e){
			e.printStackTrace();
		}
		return tsvfieldHashSet;
	}
	//--------------------------------------------------------------------------------------------------------------------------------------------	
	public static HashSet<String> readUniqueFieldFromTSV(String inputTSVPath, String inputTSVFileName,
			int keyFieldNumber, int totalFieldsCount,
			ConditionType conditionType, int field1Number, String field1Value, int field2Number, String field2Value, 
			int showProgressInterval,
			long testOrReal, int writeMessageStep){//This method reads only one field in TSV lines into HashSet. The key is a unique field. If it sees repeats of that field, just ignores it but increments the number of errors and finally reports it. 
		HashSet<String> tsvfieldHashSet = new HashSet<String>();
		try{ 
			BufferedReader br;
			//Reading posts and adding <repoId, totalNumberOfMembers> in a hashMap:
			br = new BufferedReader(new FileReader(inputTSVPath + "\\" + inputTSVFileName)); 
			System.out.println(writeMessageStep + "- Parsing " + inputTSVFileName  + ":");
			System.out.println("    Started ...");
			int error1 = 0, error2 = 0, unmatchedRecords = 0;
			String[] fields;
			int i=0; 
			int matchedRec = 0;
			String s, keyField;
			boolean recordShouldBeRead;
			br.readLine(); //header.
			while ((s=br.readLine())!=null){
				fields = s.split("\t");
				if (fields.length != totalFieldsCount)
					error1++;
				else{
					recordShouldBeRead = MyUtils.runLogicalComparison(conditionType, fields[field1Number], field1Value, fields[field2Number], field2Value);
					if (recordShouldBeRead){
						keyField = fields[keyFieldNumber];
						if (!tsvfieldHashSet.contains(keyField))
							tsvfieldHashSet.add(keyField);
						else
							error2++;
						matchedRec++;
					}//if (reco....
					else
						unmatchedRecords++;
				}//else.
				i++;
				if (testOrReal > Constants.THIS_IS_REAL)
					if (i >= testOrReal)
						break;
				if (i % showProgressInterval == 0)
					System.out.println("        " +  Constants.integerFormatter.format(i));
			}//while ((s=br....
			if (error1>0)
				System.out.println("        Error) Number of records with != " + totalFieldsCount + " fields: " + error1);
			if (error2>0)
				System.out.println("        Error) Number of records with duplicate keyfield: " + error2);
			if (conditionType == ConditionType.NO_CONDITION)
				System.out.println("        Number of records read: " + Constants.integerFormatter.format(matchedRec));
			else{
				System.out.println("        Number of records read (matched with the provided conditions): " + Constants.integerFormatter.format(matchedRec));
				if (unmatchedRecords == 0)
					System.out.println("        :-) No unmatched records with the conditions provided.");
				else{
					System.out.println("        Number of ignored records (unmatched with the provided conditions): " + Constants.integerFormatter.format(unmatchedRecords));
				}//else.
			}//else.
			System.out.println("    Finished.");
			System.out.println();
			br.close();
		}catch (Exception e){
			e.printStackTrace();
		}
		return tsvfieldHashSet;
	}
	//--------------------------------------------------------------------------------------------------------------------------------------------	
	//This method 
	public static void mergeTwoTSVFieldsTogether(String inputTSVPath, String inputTSVFileName, 
			String outputTSVPath, String outputTSVFileName, 
			int field1Number, int field2Number, String delimiter, int totalFieldsNumber,
			int showProgressInterval,
			long testOrReal, int writeMessageStep){
		try{ 
			BufferedReader br;
			//Reading posts and adding <repoId, totalNumberOfMembers> in a hashMap:
			br = new BufferedReader(new FileReader(inputTSVPath + "\\" + inputTSVFileName)); 
			System.out.println(writeMessageStep + "- Parsing " + inputTSVFileName  + ", merging two columns and writing in " + outputTSVFileName + ":");
			System.out.println("    Started ...");
			FileWriter writer = new FileWriter(outputTSVPath + "\\" + outputTSVFileName);
			int error = 0;
			String[] fields;
			int i=0;
			String s, output;
			while ((s=br.readLine())!=null){
				fields = s.split("\t");
				if (fields.length == totalFieldsNumber){
					fields[field1Number] = fields[field1Number] + "/" + fields[field2Number];
					output = fields[0];
					for (int j=1; j<totalFieldsNumber; j++)
						if (j != field2Number)
							output = output + "\t" + fields[j];
					output = output + "\n";
					writer.append(output);
				}
				else
					error++;
				i++;
				if (testOrReal > Constants.THIS_IS_REAL)
					if (i >= testOrReal)
						break;
				if (i % showProgressInterval == 0)
					System.out.println("        " +  Constants.integerFormatter.format(i));
			}
			writer.flush();writer.close();
			br.close();
			System.out.println("        " + Constants.integerFormatter.format(i) + " records have been read.");
			if (error>0)
				System.out.println("        Error) Number of records with !=" + totalFieldsNumber + " fields: " + error);
			System.out.println("    Finished.");
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}//mergeTwoTSVFieldsTogether().
	//--------------------------------------------------------------------------------------------------------------------------------------------
	//--------------------------------------------------------------------------------------------------------------------------------------------
	public static TreeMap<String, ArrayList<String[]>> readNonUniqueKeyAndItsValueFromTSV(String inputTSVPath, String inputTSVFileName, Set<String> keySetToCheckExistenceOfKeyField, 
			int keyFieldNumber, SortOrder sortOrder, int totalFieldsCount, String fieldNumbersToBeRead_separatedByDollar, 
			ConditionType conditionType, int field1Number, String field1Value, int field2Number, String field2Value, 
			int showProgressInterval, 
			long testOrReal, int writeMessageStep){//This method reads TSV lines into HashMap. 
		//The key is a non-unique field (#keyfieldNumber) and value is an ArrayList<String[]> containing all the values of all the rows that have the same keyFieldNumber. Values of each row is stored in a String[].
		//		TreeMap<String, ArrayList<String[]>> tsvRecordsHashMap = new TreeMap<String, ArrayList<String[]>>();
		TreeMap<String, ArrayList<String[]>> tsvRecordsHashMap;
		if (sortOrder == SortOrder.DEFAULT_FOR_STRING)//means that keyfield is not integer.
			tsvRecordsHashMap = new TreeMap<String, ArrayList<String[]>>();
		else
			if (sortOrder == SortOrder.ASCENDING_INTEGER){//means that keyfield is an integer.
				tsvRecordsHashMap = new TreeMap<String, ArrayList<String[]>>(new Comparator<String>(){
					public int compare(String s1, String s2){//We want the ascending order of number:
						if (Integer.parseInt(s1) > Integer.parseInt(s2))
							return 1;
						else
							if (Integer.parseInt(s1) < Integer.parseInt(s2))
								return -1;
							else
								return 0;
					}
				});
			}//if.
			else{
				tsvRecordsHashMap = new TreeMap<String, ArrayList<String[]>>(new Comparator<String>(){
					public int compare(String s1, String s2){//We want the descending order of number:
						if (Integer.parseInt(s1) < Integer.parseInt(s2))
							return 1;
						else
							if (Integer.parseInt(s1) > Integer.parseInt(s2))
								return -1;
							else
								return 0;
					}
				});
			}//else.
		try{ 
			BufferedReader br;
			//Reading posts and adding <repoId, totalNumberOfMembers> in a hashMap:
			br = new BufferedReader(new FileReader(inputTSVPath + "\\" + inputTSVFileName)); 
			System.out.println(writeMessageStep + "- Parsing " + inputTSVFileName  + ":");
			System.out.println("    Started ...");
			int error = 0, unmatchedRecords = 0;
			String[] fields;
			boolean isNew, recordShouldBeRead;
			ArrayList<String[]> aTSVItemRelatedToTheRepeatedKey; 
			int i=0, equalObjectsFound = 0, matchedRec = 0;
			String s, keyField;
			br.readLine(); //header.
			while ((s=br.readLine())!=null){
				fields = s.split("\t");
				if (fields.length != totalFieldsCount)
					error++;
				else{
					recordShouldBeRead = MyUtils.runLogicalComparison(conditionType, fields[field1Number], field1Value, fields[field2Number], field2Value);
					if (recordShouldBeRead){
						keyField = fields[keyFieldNumber];
						if (keySetToCheckExistenceOfKeyField == null || keySetToCheckExistenceOfKeyField.contains(keyField)){
							if (tsvRecordsHashMap.containsKey(keyField)){
								aTSVItemRelatedToTheRepeatedKey = tsvRecordsHashMap.get(keyField);
								isNew = true;
								for (String[] stringArray: aTSVItemRelatedToTheRepeatedKey)
									if (MyUtils.compareTwoStringArrays(fields, stringArray)){ //Note:   if (fields.equals(stringArray))   does not work! Just the used notation is correct.
										equalObjectsFound++;
										isNew = false;
										break;
									}//if.					
								if (isNew){
									if (fieldNumbersToBeRead_separatedByDollar.equals("ALL"))//means that all the fields are needed.
										aTSVItemRelatedToTheRepeatedKey.add(fields);
									else{//means that only some of the fields are needed.
										String[] neededFields = fieldNumbersToBeRead_separatedByDollar.split("\\$");
										for (int k=0; k<neededFields.length; k++)
											neededFields[k] = fields[Integer.parseInt(neededFields[k])];
										aTSVItemRelatedToTheRepeatedKey.add(neededFields);
									}//else.
								}//if
							}//if.
							else{
								ArrayList<String[]> aTSVItemRelatedToANewKey = new ArrayList<String[]>();
								if (fieldNumbersToBeRead_separatedByDollar.equals("ALL"))//means that all the fields are needed.
									aTSVItemRelatedToANewKey.add(fields);
								else{//means that only some of the fields are needed.
									String[] neededFields = fieldNumbersToBeRead_separatedByDollar.split("\\$");
									for (int k=0; k<neededFields.length; k++)
										neededFields[k] = fields[Integer.parseInt(neededFields[k])];
									aTSVItemRelatedToANewKey.add(neededFields);
								}//else.
								tsvRecordsHashMap.put(keyField, aTSVItemRelatedToANewKey);
							}//else.
							matchedRec++;
						}//if (tsvRecordsHashMap.containsKey(keyField)).
					}//if (reco....
					else
						unmatchedRecords++;
				}//else.
				i++;
				if (testOrReal > Constants.THIS_IS_REAL)
					if (i >= testOrReal)
						break;
				if (i % showProgressInterval == 0)
					System.out.println("        " +  Constants.integerFormatter.format(i));
			}//while ((s=br....

			if (error>0)
				System.out.println("        Error) Number of records with !=" + totalFieldsCount + " fields: " + error);

			if (equalObjectsFound>0)
				System.out.println("        Hint) Number of repeated TSV records (ignored): " + equalObjectsFound);

			if (conditionType == ConditionType.NO_CONDITION)
				System.out.println("        Number of records read: " + Constants.integerFormatter.format(matchedRec));
			else
			{
				System.out.println("        Number of records read (matched with the provided conditions): " + Constants.integerFormatter.format(matchedRec));
				if (unmatchedRecords == 0)
					System.out.println("        :-) No unmatched records with the conditions provided.");
				else
					System.out.println("        Number of ignored records (unmatched with the provided conditions): " + Constants.integerFormatter.format(unmatchedRecords));
			}//if (cond....
			System.out.println("    Finished.");
			System.out.println();
			br.close();
		}catch (Exception e){
			e.printStackTrace();
		}
		return tsvRecordsHashMap;
	}
	//--------------------------------------------------------------------------------------------------------------------------------------------
	//This file replaces a foreign key (e.g., userId) with its value from another TSV (provided that the relation is 1:1 or 1:n)
	public static void replaceForeignKeyInTSVWithValueFromAnotherTSV(String foreignKeyInputTSVPath, String foreignKeyInputTSVFile, 
			String primaryKeyInputTSVPath, String primaryKeyInputTSVFile, 
			String outputTSVPath, String outputTSVFile, 
			int foreignKeyFieldNumber, int foreignKeyTotalFieldsNumber,
			int PrimaryKeyFieldNumber, int primaryKeyTotalFieldsNumber, 
			int primaryKeySubstituteFieldNumber, //this is the number of the field that is written instead of foreign key.
			String substituteTitle,
			int showProgressInterval,
			long testOrReal, int writeMessageStep
			){
		try{ 
			int error = 0;
			String[] fields;
			int i=0;
			String s, header, outputLine;

			TreeMap<String, String[]> primaryKeyRecords = TSVManipulations.readUniqueKeyAndItsValueFromTSV(
					primaryKeyInputTSVPath, primaryKeyInputTSVFile, null, PrimaryKeyFieldNumber, primaryKeyTotalFieldsNumber, "", ConditionType.NO_CONDITION, 0, "", 0, "", 100000, testOrReal, 1);
			//TreeMap<String, String[]> foreignKeyRecords = TSVManipulations.readUniqueKeyAndItsValueFromTSV(foreignKeyInputTSVPath, foreignKeyInputTSVFile, ForeignKeyFieldNumber, foreignKeyTotalFieldsNumber, ConditionType.NO_CONDITION, 0, "", 0, "", 100000, testOrReal, 2);
			System.out.println("2- Producing output (foreign key replaced by value) ...");
			System.out.println("    Started ...");
			String[] theFKRecord, aPKRecord;

			BufferedReader br;
			//Reading the header and substituting the foreign key field title:
			br = new BufferedReader(new FileReader(foreignKeyInputTSVPath + "\\" + foreignKeyInputTSVFile)); 
			FileWriter writer = new FileWriter(outputTSVPath + "\\" + outputTSVFile);
			s = br.readLine();
			fields = s.split("\t");
			if (foreignKeyFieldNumber == 0)
				header = substituteTitle;
			else
				header = fields[0];
			for (int j=1; j<foreignKeyTotalFieldsNumber; j++)
				if (j == foreignKeyFieldNumber)
					header = header + "\t" + substituteTitle;
				else
					header = header + "\t" + fields[j];
			header = header + "\n";
			writer.append(header);
			//Now, replacing the FK with values from PK file:
			while ((s=br.readLine())!=null){
				theFKRecord = s.split("\t");
				if (theFKRecord.length == foreignKeyTotalFieldsNumber){
					aPKRecord = primaryKeyRecords.get(theFKRecord[foreignKeyFieldNumber]);
					theFKRecord[foreignKeyFieldNumber] = aPKRecord[primaryKeySubstituteFieldNumber];
					outputLine = theFKRecord[0];
					for (int j=1; j<theFKRecord.length; j++)
						outputLine = outputLine + "\t" + theFKRecord[j];
					outputLine = outputLine + "\n";
					writer.append(outputLine);
				}
				else
					error++;
				i++;
				if (testOrReal > Constants.THIS_IS_REAL)
					if (i >= testOrReal)
						break;
				if (i % showProgressInterval == 0)
					System.out.println("        " +  Constants.integerFormatter.format(i));

			}//for (Stri....
			writer.flush();writer.close();
			br.close();
			System.out.println("        " + Constants.integerFormatter.format(i) + " records have been read.");
			if (error>0)
				System.out.println("        Error) Number of FK records with !=" + foreignKeyTotalFieldsNumber + " fields: " + error);
			System.out.println("    Finished.");
		}catch(Exception e){
			e.printStackTrace();
		}
	}//replaceForeignKeyInTSVWithValueFromAnotherTSV().
	//--------------------------------------------------------------------------------------------------------------------------------------------
	public static void filterPostsToThosePerformedByCommunityMembers(String postsInputPath, String postsInputTSVFileName,
			String communityInputPath, String communityInpputTSVFileName_NoExtension,
			String postsOutputPath, String postsOutputTSVFileName,
			String communityToFilter,
			int showProgressInterval, long testOrReal, int writeMessageStep){
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
			//Iterating over <all 15 communities / only one community>, reading the SOId's, and adding them to the hashSet "allSOIdsOfCommunityMembers":
			HashSet<String> allSOIdsOfCommunityMembers;
			System.out.println(writeMessageStep + "- Start filtering posts:");
			if (communityToFilter.equals(Constants.ALL)){
				allSOIdsOfCommunityMembers = new HashSet<String>();
				for (int communityCounter=0; communityCounter<15; communityCounter++){
					String communityInputFileName = communityInpputTSVFileName_NoExtension + communityNumbers[communityCounter] + ".tsv";
					HashSet<String> soIDsInThisCommunity = TSVManipulations.readNonUniqueFieldFromTSV_OnlyRepeatEachEntryOnce(
							communityInputPath, communityInputFileName, 2, 6, ConditionType.NO_CONDITION, 0, "", 0, "", showProgressInterval*4, testOrReal, 1000+communityCounter+1);
					for (String soId: soIDsInThisCommunity)
						allSOIdsOfCommunityMembers.add(soId);
				}//for.
			}//if (commu....
			else{
				String communityInputFileName = communityInpputTSVFileName_NoExtension + communityToFilter + ".tsv";
				allSOIdsOfCommunityMembers = TSVManipulations.readNonUniqueFieldFromTSV_OnlyRepeatEachEntryOnce(
						communityInputPath, communityInputFileName, 2, 6, ConditionType.NO_CONDITION, 0, "", 0, "", showProgressInterval*4, testOrReal, 1001);
			}//else.
			
			//Now, filter the posts (read all posts, and write those that are performed by a member of "allSOIdsOfCommunityMembers" to the output posts file):
			HashSet<String> neededPostIds = new HashSet<String>();
			int i = 0, error = 0, error2 = 0;
			String[] fields;
			String s, ownerId, id;
			System.out.println(writeMessageStep*1000+2 + "- Parse \"" + postsInputTSVFileName + "\" and see which questions and answers are needed (performed by the above SOIds [of all members]):");
			System.out.println("    Started ...");
			BufferedReader br;
			br = new BufferedReader(new FileReader(postsInputPath + "\\" + postsInputTSVFileName)); 
			s=br.readLine();	//read the header.
			while ((s=br.readLine())!=null){
				fields = s.split("\t");
				if (fields.length == 9){
					ownerId = fields[2];
					if (allSOIdsOfCommunityMembers.contains(ownerId))
						if (fields[1].equals("1"))//i.e., this is a question, so we need it:
							neededPostIds.add(fields[0]);//id
						else 
							if (fields[1].equals("2")){//i.e., this is an answer. So we need both the answer and the related question:
								neededPostIds.add(fields[0]);//parentId (id of related question)
								neededPostIds.add(fields[3]);//parentId (id of related question)
							}
				}
				else
					error++;
				i++;
				if (testOrReal > Constants.THIS_IS_REAL)
					if (i >= testOrReal)
						break;
				if (i % (showProgressInterval*4) == 0)
					System.out.println("        " +  Constants.integerFormatter.format(i));
			}
			br.close();
			System.out.println("    " + Constants.integerFormatter.format(i) + " records have been read.");
			if (error>0)
				System.out.println("        Error) Number of records with != 9 fields: " + error);
			System.out.println("    Finished.");
			System.out.println();

			System.out.println(writeMessageStep*1000+3 + "- Filtering posts to the identified ones (performed by community members):");
			System.out.println("    Started ...");
			br = new BufferedReader(new FileReader(postsInputPath + "\\" + postsInputTSVFileName)); 
			FileWriter writer = new FileWriter(postsOutputPath + "\\" + postsOutputTSVFileName);
			i = 0;
			s=br.readLine();	//read the header.
			writer.append(s + "\n");	//write the header.
			while ((s=br.readLine())!=null){
				fields = s.split("\t");
				if (fields.length == 9){
					id = fields[0];
					if (neededPostIds.contains(id))
						writer.append(s + "\n");
				}
				else
					error2++;
				i++;
				if (testOrReal > Constants.THIS_IS_REAL)
					if (i >= testOrReal)
						break;
				if (i % (showProgressInterval*4) == 0)
					System.out.println("        " +  Constants.integerFormatter.format(i));
			}
			writer.flush();writer.close();
			br.close();
			System.out.println("    " + Constants.integerFormatter.format(i) + " records have been read.");
			if (error2>0)
				System.out.println("        Error) Number of records with != 9 fields: " + error2);
			System.out.println("    Finished.");
			System.out.println("End of filtering posts.");
			System.out.println();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}//filterPostsToThodePerformedByCommunityMembers().
	//--------------------------------------------------------------------------------------------------------------------------------------------
	public static void filterProjectsToTheProjectsInTheCommunitiesSummary(String projectsInputPath, String projectsInputTSVFileName,
			String communitiesSummaryInputPath, String communitiesSummaryInpputTSVFileName,
			String projectsOutputPath, String projectsOutputTSVFileName,
			int showProgressInterval, long testOrReal, int writeMessageStep){
		try{ 
			//Reading all needed projects from summary of communities:
			System.out.println(writeMessageStep + "- Start Filtering projects:");
			HashSet<String> neededOwnerLoginAndProjectNames = TSVManipulations.readUniqueFieldFromTSV(
					communitiesSummaryInputPath, communitiesSummaryInpputTSVFileName, 1, 30, ConditionType.NO_CONDITION, 0, "", 0, "", showProgressInterval, testOrReal, writeMessageStep*1000+1);
			
			//Now, read all projects and save those that are in the above hashSet in a new file:
			System.out.println(writeMessageStep*1000+2 + "- Filtering projects to the identified ones (in the summary of communities file):");
			System.out.println("    Started ...");
			BufferedReader br = new BufferedReader(new FileReader(projectsInputPath + "\\" + projectsInputTSVFileName)); 
			FileWriter writer = new FileWriter(projectsOutputPath + "\\" + projectsOutputTSVFileName);
			int i = 0, error = 0;
			String s = br.readLine();	//read the header.
			writer.append(s + "\n");	//write the header.
			while ((s=br.readLine())!=null){
				String[] fields = s.split("\t");
				if (fields.length == 9){
					String ownerLoginAndProjectName = fields[2];
					if (neededOwnerLoginAndProjectNames.contains(ownerLoginAndProjectName))
						writer.append(s + "\n");
				}
				else
					error++;
				i++;
				if (testOrReal > Constants.THIS_IS_REAL)
					if (i >= testOrReal)
						break;
				if (i % (showProgressInterval*2) == 0)
					System.out.println("        " +  Constants.integerFormatter.format(i));
			}
			writer.flush();writer.close();
			br.close();
			System.out.println("    " + Constants.integerFormatter.format(i) + " records have been read.");
			if (error>0)
				System.out.println("    Error) Number of records with != 9 fields: " + error);
			System.out.println("    Finished.");
			System.out.println("End of filtering projects.");
			System.out.println();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}//filterProjectsToTheProjectsInTheCommunitiesSummary().	
	//--------------------------------------------------------------------------------------------------------------------------------------------
	public static void filterIssuesToTheIssuesThat_AreInTheProjectsOfCommunitiesSummaryAndTheAssigneeIsACommunityMember(
			String issuesInputPath, String issuesInputTSVFileName,
			String communitiesInputPath, String communitiesSummaryInpputTSVFileName, String communityInputTSVFileName,
			String issuesOutputPath, String issuesOutputTSVFileName,
			int showProgressInterval, long testOrReal, int writeMessageStep){
		try{ 
			System.out.println(writeMessageStep + "-Start Filtering issues:");
			//Reading all needed projects from summary of communities:
			HashSet<String> neededOwnerLoginAndProjectNames = TSVManipulations.readUniqueFieldFromTSV(
					communitiesInputPath, communitiesSummaryInpputTSVFileName, 1, 30, ConditionType.NO_CONDITION, 0, "", 0, "", showProgressInterval, testOrReal, writeMessageStep*1000+1);
			//Read all <project --> communityMember[]>:
			TreeMap<String, ArrayList<String[]>> ownerLoginAndProjectName_and_itsCommunityMembers = TSVManipulations.readNonUniqueKeyAndItsValueFromTSV(
					communitiesInputPath, communityInputTSVFileName, null, 1, SortOrder.DEFAULT_FOR_STRING, 6, Constants.ALL, ConditionType.NO_CONDITION, 0, "", 0, "", showProgressInterval, testOrReal, writeMessageStep*1000+2);
			
			//Now, read all issues, and filter based on: 1- project (should be in above community projects) and 2- assigned to one of the above communityMembers: 
			System.out.println(writeMessageStep*1000+3 + "- Filtering issues to the identified ones (in the selected projects, and assigned to their community members):");
			System.out.println("    Started ...");
			BufferedReader br = new BufferedReader(new FileReader(issuesInputPath + "\\" + issuesInputTSVFileName)); 
			FileWriter writer = new FileWriter(issuesOutputPath + "\\" + issuesOutputTSVFileName);
			int i = 0, error = 0;
			String s = br.readLine();	//read the header.
			writer.append(s + "\n");	//write the header.
			while ((s=br.readLine())!=null){
				String[] fields = s.split("\t");
				if (fields.length == 11){
					String anOwnerLoginAndProjectName = fields[1];
					String assigneeLogin = fields[5];
					if (neededOwnerLoginAndProjectNames.contains(anOwnerLoginAndProjectName)){ //Means that this project is one of communities we are to evaluate.
						if (ownerLoginAndProjectName_and_itsCommunityMembers.containsKey(anOwnerLoginAndProjectName)){ //Means that there are issues in the community we are evaluating.
							ArrayList<CommunityMember> cmAL = ProvideData.copyCommunityFromArrayListOfStringArray(ownerLoginAndProjectName_and_itsCommunityMembers.get(anOwnerLoginAndProjectName));
							boolean thisProjectIsNeeded = false;
							for (int j=0; j<cmAL.size(); j++){
								CommunityMember cm = cmAL.get(j);
								if (assigneeLogin.equals(cm.ghLogin)){
									thisProjectIsNeeded = true;
									break;
								}//if (assig....
							}//for (j.
							if (thisProjectIsNeeded)
								writer.append(s + "\n");
						}//if (owner....
					}//if (neede....
				}
				else
					error++;
				i++;
				if (testOrReal > Constants.THIS_IS_REAL)
					if (i >= testOrReal)
						break;
				if (i % showProgressInterval == 0)
					System.out.println("        " +  Constants.integerFormatter.format(i));
			}
			writer.flush();writer.close();
			br.close();
			System.out.println("    " + Constants.integerFormatter.format(i) + " records have been read.");
			if (error>0)
				System.out.println("    Error) Number of records with != 11 fields: " + error);
			System.out.println("    Finished.");
			System.out.println("End of filtering issues.");
			System.out.println();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}//filterProjectsToTheProjectsInTheCommunitiesSummary().	
	//--------------------------------------------------------------------------------------------------------------------------------------------
	public static void removeDuplicateIssues_RemoveBothIssuesIfAssigneesDiffer(String issuesInputTSVPath, String issuesInputTSVFileName, 
			String issuesOutputTSVPath, String issuesOutputTSVFileName, 
			int showProgressInterval,
			long testOrReal, int writeMessageStep){
		try{
			TreeMap<String, ArrayList<String[]>> allIssues = TSVManipulations.readNonUniqueKeyAndItsValueFromTSV(
					issuesInputTSVPath, issuesInputTSVFileName, null, 0, SortOrder.ASCENDING_INTEGER, 11, "ALL", ConditionType.NO_CONDITION, 0, "", 0, "", showProgressInterval, testOrReal, 1);

			System.out.println("2- Writing the non-duplicate issues in file \"" + issuesOutputTSVFileName + "\":");
			System.out.println("    Started ....");
			//Reading the header from input file and writing in output file:
			BufferedReader br;
			br = new BufferedReader(new FileReader(issuesInputTSVPath + "\\" + issuesInputTSVFileName)); 
			String s = br.readLine(); //header.
			br.close();
			FileWriter writer = new FileWriter(issuesOutputTSVPath + "\\" + issuesOutputTSVFileName);
			writer.append(s + "\n");
			
			int i = 0, numberOfSetsOfIgnoredIssues = 0, numberOfIgnoredIssues = 0;
			for (String issueId:allIssues.keySet()){
				ArrayList<Issue> issAL = ProvideData.copyIssuesFromArrayListOfStringArray(allIssues.get(issueId));
				if (issAL.size()>1){
					boolean thisIssueShouldBeConsidered = true;
					Issue issue = issAL.get(0);
					for (int j=1; j<issAL.size(); j++){
						Issue issue2 = issAL.get(j);
						if (!issue.assigneeLogin.equals(issue2.assigneeLogin)){
							thisIssueShouldBeConsidered = false;
							numberOfSetsOfIgnoredIssues++;
							numberOfIgnoredIssues = numberOfIgnoredIssues + issAL.size();
							System.out.println("Number of issues that are ignored: " + issAL.size());
							break;
						}
					}//for (j.
					if (thisIssueShouldBeConsidered)
						writer.append(issue.id + "\t" + issue.ownerLoginAndProjectName + "\t" + issue.reporterId + "\t" + issue.reporterLogin + "\t" + issue.assigneeId + "\t" + issue.assigneeLogin + "\t" + issue.createdAt + "\t" + issue.numberOfComments + "\t" + issue.labels + "\t" + issue.title + "\t" + issue.body + "\n");
				}//if (issAL....
				else{
					Issue issue = issAL.get(0);
					writer.append(issue.id + "\t" + issue.ownerLoginAndProjectName + "\t" + issue.reporterId + "\t" + issue.reporterLogin + "\t" + issue.assigneeId + "\t" + issue.assigneeLogin + "\t" + issue.createdAt + "\t" + issue.numberOfComments + "\t" + issue.labels + "\t" + issue.title + "\t" + issue.body + "\n");
				}//else.
				i++;
				if (testOrReal > Constants.THIS_IS_REAL)
					if (i >= testOrReal)
						break;
				if (i % showProgressInterval == 0)
					System.out.println("        " +  Constants.integerFormatter.format(i));
			}//for (String ....
			if (numberOfSetsOfIgnoredIssues > 0){
				System.out.println("Number of sets of deleted issues (each one contains at least two issues): " + numberOfSetsOfIgnoredIssues);
				System.out.println("Total number of deleted issues: " + numberOfIgnoredIssues);
			}
			writer.flush();		writer.close();
			System.out.println("    Finished.");
			System.out.println("Finished removing duplicate issues.");
		}catch (Exception e){
			e.printStackTrace();	
		}		
	}//removeDuplicateIssues_RemoveBothIssuesIfAssigneesDiffer().
	//--------------------------------------------------------------------------------------------------------------------------------------------
	public static void filterFilesToThoseOfCommunityMembersAndSaveThemInSingleOutputFolder(String postsInputPath, String postsInputTSVFileName,
			String communitiesInputPath, String communitiesSummaryInputTSVFileName, String communityInpputTSVFileName_NoExtension, 
			String projectsInputPath, String projectsInputTSVFileName,
			String issuesInputPath, String issuesInputTSVFileName,  
			String outputPath, 
			String postsOutputTSVFileName, String projectsOutputTSVFileName, String issuesOutputTSVFileName, 
			String communityNumberToFilter,
			int showProgressInterval, long testOrReal){
		//Was run successfully: 
		filterPostsToThosePerformedByCommunityMembers(postsInputPath, postsInputTSVFileName,
			communitiesInputPath, communityInpputTSVFileName_NoExtension,
			outputPath, postsOutputTSVFileName,
			communityNumberToFilter, 
			showProgressInterval, testOrReal, 1);

		//Was run successfully: 
		filterProjectsToTheProjectsInTheCommunitiesSummary(projectsInputPath, projectsInputTSVFileName,
		communitiesInputPath, communitiesSummaryInputTSVFileName,
		outputPath, projectsOutputTSVFileName,
		showProgressInterval, testOrReal, 2);

		filterIssuesToTheIssuesThat_AreInTheProjectsOfCommunitiesSummaryAndTheAssigneeIsACommunityMember(
			issuesInputPath, issuesInputTSVFileName,
			communitiesInputPath, communitiesSummaryInputTSVFileName, communityInpputTSVFileName_NoExtension+communityNumberToFilter+".tsv", 
			outputPath, issuesOutputTSVFileName,
			showProgressInterval, testOrReal, 3);
	}//filterFilesToThoseOfCommunityMembers().
	//--------------------------------------------------------------------------------------------------------------------------------------------
	//--------------------------------------------------------------------------------------------------------------------------------------------
	//--------------------------------------------------------------------------------------------------------------------------------------------
	//--------------------------------------------------------------------------------------------------------------------------------------------
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		//Cleaned successfully:
		//		cleanProjectsTSVFile(Constants.DATASET_DIRECTORY_GH_MySQL_TSV, "projects2 (two tabs).tsv", 
		//				Constants.DATASET_DIRECTORY_GH_MySQL_TSV, "projects2 - Cleaned.tsv", "projects3 (only important fields) - Cleaned.tsv");


		//saveOnlyTheAssignedIssues(Constants.DATASET_DIRECTORY_GH_MongoDB_TSV, "issues.tsv", Constants.DATASET_DIRECTORY_GH_MongoDB_TSV, "issues-Assigned.tsv"); 

		//The "numberOfMembersInEachProject-ForCommonUsers.tsv" file is obtained from "CheckFitness.java":	
		//But we do not need this method anymore. The CheckFitness.java is updated.
		//		joinProjects_With_NumberOfMembersInEachProject(Constants.DATASET_DIRECTORY_GH_MySQL_TSV, "projects3 (only important fields) - Cleaned.tsv", 
		//				Constants.DATASET_DIRECTORY_GH_AND_SO_Mixed_TOGETHER, "numberOfMembersInEachProjectForCommonUsers.tsv",
		//				Constants.DATASET_DIRECTORY_GH_AND_SO_Mixed_TOGETHER, "numberOfMembersInEachProjectForCommonUsers-WithDescriptionAndLanguage.tsv");

		//Was run successfully: 
		//		mergeTwoTSVFieldsTogether(Constants.DATASET_DIRECTORY_GH_MongoDB_TSV, "repos.tsv",
		//				Constants.DATASET_DIRECTORY_GH_MongoDB_TSV, "repos-mergedTwoColumns.tsv",
		//				1, 2, "/", 6, 
		//				500000,
		//				Constants.THIS_IS_REAL, 1);

		//Was run successfully: 
		//		mergeTwoTSVFieldsTogether(Constants.DATASET_DIRECTORY_GH_MongoDB_TSV, "issues-Assigned.tsv",
		//				Constants.DATASET_DIRECTORY_GH_MongoDB_TSV, "issues-Assigned-mergedTwoColumns.tsv",
		//				1, 2, "/", 12, 
		//				500000,
		//				Constants.THIS_IS_REAL, 1);

		//Was run successfully: 
		//		replaceForeignKeyInTSVWithValueFromAnotherTSV(Constants.DATASET_DIRECTORY_GH_MySQL_TSV, "projects2 - Cleaned.tsv",
		//				Constants.DATASET_DIRECTORY_GH_MySQL_TSV, "users3 (only important fields)-fixed.tsv", 
		//				Constants.DATASET_DIRECTORY_GH_MySQL_TSV, "projects2-Cleaned-ownerIdReplacedWithLogin.tsv",
		//				2, 10,
		//				0, 3, 
		//				1,
		//				"ownerLogin",
		//				500000,
		//				Constants.THIS_IS_REAL, 1);

		//Was run successfully: 
//		mergeTwoTSVFieldsTogether(Constants.DATASET_DIRECTORY_GH_MySQL_TSV, "projects2-Cleaned-ownerIdReplacedWithLogin.tsv",
//				Constants.DATASET_DIRECTORY_GH_MySQL_TSV, "projects2-Cleaned-ownerIdReplacedWithLogin-mergedTwoColumns.tsv",
//				2, 3, "/", 10, 
//				500000,
//				Constants.THIS_IS_REAL, 1);

		//Testing readUniqueKeyAndItsValueFromTSV():
		//		TreeMap<String, String[]> projectsAndMemberships = readUniqueKeyAndItsValueFromTSV(Constants.DATASET_DIRECTORY_GH_AND_SO_Mixed_TOGETHER, "numberOfMembersInEachProjectForCommonUsers2-withTotalMembers.tsv", 1, 6, ConditionType.NO_CONDITION, 0, "", 0, "", 100000, Constants.THIS_IS_A_TEST, 1);
		//		ProjectAndItsNumberOfMemberships pm;
		//		System.out.println("key -->\tnumberOfCommonUsers\tmySQLProjectId\tprojectName\tprojectDescription\tprojectlanguage\ttotalUsers");
		//		for (String s:projectsAndMemberships.keySet()){
		//			pm = ProvideData.copyProjectAndItsNumberOfMembershipsFromStringArray(projectsAndMemberships.get(s));
		//			System.out.println(s + ")  " + 
		//					pm.numberOfCommonUsers + "  " + 
		//					pm.mySQLProjectId + "  " + 
		//					pm.projectName + "  " +
		//					pm.projectDescription + "  " + 
		//					pm.projectLanguage + "  " + 
		//					pm.totalUsers);
		//		}//for.
		//
		//		//Testing readNonUniqueKeyAndItsValueFromTSV():
		//		TreeMap<String, ArrayList<String[]>> projectsAndTheirCommiters = readNonUniqueKeyAndItsValueFromTSV(Constants.DATASET_DIRECTORY_GH_MySQL_TSV, "commits (only important fields).tsv", 1, 2, ConditionType.NO_CONDITION, 0, "", 0, "", 100000, Constants.THIS_IS_A_TEST, 2);
		//		ArrayList<ProjectAndItsCommitters> pcA;
		//		ProjectAndItsCommitters pc;
		//		System.out.println("key -->\tprojectId\tcommitterId");
		//		for (String s: projectsAndTheirCommiters.keySet()){
		//			pcA = ProvideData.copyProjectAndItsCommittersFromArrayListOfStringArray(projectsAndTheirCommiters.get(s));
		//			for (int j=0; j<pcA.size(); j++){
		//				pc = pcA.get(j);
		//				System.out.println(s + ")  " + pc.projectId + "  " + pc.committerId);
		//			}//for (int j ....
		//		}//for (Stri....
		
		//Was run successfully: 
		//Fixing the issues tsv file (some of the issues are repeated. So we want to delete the duplicate issues):
//		removeDuplicateIssues_RemoveBothIssuesIfAssigneesDiffer(Constants.DATASET_DIRECTORY_GH_MongoDB_TSV, "issues-Assigned-mergedTwoColumns.tsv",
//				Constants.DATASET_DIRECTORY_GH_MongoDB_TSV, "issues2.tsv",
//				100000,
//				Constants.THIS_IS_REAL, 1);
				
				
		//Was run successfully: 
//		filterPostsToThodePerformedByCommunityMembers(Constants.DATASET_DIRECTORY_SO_WITHOUT_EMAIL_HASH, "Posts.tsv",
//		Constants.DATASET_DIRECTORY_COMMUNITY, "community",
//		Constants.DATASET_DIRECTORY_GH_AND_SO_Mixed_TOGETHER, "Posts-madeByCommunityMembers.tsv",
//		Constants.ALL, 
//		500000, Constants.THIS_IS_REAL);

		//Was run successfully: 
//		filterPostsToThosePerformedByCommunityMembers(Constants.DATASET_DIRECTORY_SO_WITHOUT_EMAIL_HASH, "Posts.tsv",
//		Constants.DATASET_DIRECTORY_COMMUNITY, "community",
//		Constants.DATASET_DIRECTORY_DATASETS_FOR_TRIAGING, "Posts-madeByCommunityMembers-19Projects.tsv",
//		"25", //replace it by <Constants.ALL,> if you want to consider union of all communities. 
//		500000, Constants.THIS_IS_REAL, 1);

		//Was run successfully: 
		filterFilesToThoseOfCommunityMembersAndSaveThemInSingleOutputFolder(Constants.DATASET_DIRECTORY_SO_WITHOUT_EMAIL_HASH, "Posts.tsv",
				Constants.DATASET_DIRECTORY_COMMUNITY, "communitiesSummary4-top 500 projects of community25 - Top20ProjectsWithMostNumberOfAssignees.tsv", "communitiesOf20TopProjects",
				Constants.DATASET_DIRECTORY_GH_MySQL_TSV, "projects2-Cleaned-ownerIdReplacedWithLogin-mergedTwoColumns.tsv",
				Constants.DATASET_DIRECTORY_GH_MongoDB_TSV, "issues2.tsv", 
				Constants.DATASET_DIRECTORY_DATASETS_FOR_TRIAGING, 
				"Posts-madeByCommunityMembers-top20Projects.tsv", "projects-top20.tsv", "issues2-forTop20Projects.tsv", 
				Constants.COMMUNITY_25_WITH_AT_LEAST_ONE_SO_Q_OR_A_PROJECT_MEMBERS_AND_COMMITTERS_AND_ISSUE_ASSIGNEES_AND_ISSUE_REPORTERS, //replace it by <Constants.ALL,> if you want to consider union of all communities. 
				500000, Constants.THIS_IS_REAL);

		
		
	}//main().
}//Class.
