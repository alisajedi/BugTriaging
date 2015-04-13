package bugTriaging;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;

public class XMLParser {

	public static String readStringAfterIndicatorUntilDoubleQoutation(String inputString, String indicator){
		int i, j;
		i = inputString.indexOf(indicator);
		j = inputString.indexOf("\"", i + indicator.length()); 
		String result;
		if (i > -1 && j > -1)
			result = inputString.substring(i + indicator.length(),  j);
		else
			result = "";

		//Checking the consistency (there may be more than one instances of the field. Another one in comments of a user, for example.
		int k = inputString.split(indicator, -1).length-1;
		if (k > 1)
			System.out.println("        WARNING: " + k + " instances of " + "\"" + indicator + "\" found in \"" + inputString + "\"");
		return result;
	}//public static void readStringAfterIndicatorUntilDoubleQoutation(....
//--------------------------------------------------------------------------------------------------------------------------------------------
	public static String readFieldInXMLRow(String xmlRow, String field){//This method just is for maximizing readability; 
		//instead of calling readStringAfterIndicatorUntilDoubleQoutation(s, " Id=\"")        we will call    readFieldInXMLRow(s, "Id")  
		String s = readStringAfterIndicatorUntilDoubleQoutation(xmlRow, " " + field + "=\"");
		if (s.equals(""))
			s = " ";
		return s;
	}//readFieldInXMLRow(....
//--------------------------------------------------------------------------------------------------------------------------------------------
	public static String convertTwoDelimitersBeforeAndAfterToOneDelimiterBetween(String value, 
			String delimiter1, String delimiter2, String delimiter1Regex, String delimiter2Regex, 
			String newDelimiter){
		if (value.startsWith(delimiter1))
			value = value.replaceFirst(delimiter1Regex, "");
		if (value.endsWith(delimiter2))
			value = value.substring(0, value.length()-delimiter2.length());
		String[] fields = value.split(delimiter2Regex+delimiter1Regex);
		String s = "";
		for (int i=0; i<fields.length-1; i++)
			s = s + fields[i] + Constants.SEPARATOR_FOR_ARRAY_ITEMS;
		s = s + fields[fields.length-1];
		return s;
	}//convertTwoDelimitersBeforeAndAfterToOneDelimiterBetween(....
//--------------------------------------------------------------------------------------------------------------------------------------------
	public static void findAllSOTagsAndTheriValidCharacters(String inputPath, String outputPath, String inputFileName, String outputFileName1_AllTags, String outputFileName2_AllValidCharacters){
		try{
			FileReader fr = new FileReader(inputPath + "\\" + inputFileName);
			BufferedReader br = new BufferedReader(fr); 
			FileWriter writer1_AllTags = new FileWriter(outputPath + "\\" + outputFileName1_AllTags);
			FileWriter writer2_AllValidCharacters = new FileWriter(inputPath + "\\" + outputFileName2_AllValidCharacters);

			System.out.println("    Reading file \"" + inputPath + "\\" + inputFileName + "\" --------> Started ...");
			System.out.println("    Building file \"" + outputPath + "\\" + outputFileName1_AllTags + " --------> Started ...");
			System.out.println("    Building file \"" + outputPath + "\\" + outputFileName2_AllValidCharacters + " --------> Started ...");
			String s, aTag, validCharacters = "", aValidCharacterAsRegEx;
			
			while((s = br.readLine()) != null) {
				aTag = readStringAfterIndicatorUntilDoubleQoutation(s, "TagName=\"");
				if (aTag != ""){
					writer1_AllTags.append(aTag + "\n");
					if (aTag.matches(".*[^0-9a-zA-Z].*")){
						aTag = aTag.replaceAll("[0-9a-zA-Z]", "");
						for (int i=0; i < aTag.length(); i++){
							aValidCharacterAsRegEx = String.valueOf(aTag.charAt(i));
							if (aValidCharacterAsRegEx.matches("\\p{Punct}"))
								aValidCharacterAsRegEx = "\\" + aValidCharacterAsRegEx;
							if (!(validCharacters.matches(".*" + aValidCharacterAsRegEx + ".*")))
								validCharacters = validCharacters + aTag.charAt(i);
						} //for (int i....
					}//if (aTag....
				}//if (aTag ....
			}//while((s = ....

			writer2_AllValidCharacters.append("a-zA-Z0-9" + validCharacters);
			writer1_AllTags.flush();
			writer1_AllTags.close();
			writer2_AllValidCharacters.flush();
			writer2_AllValidCharacters.close();
			br.close();
			fr.close();

			System.out.println("    Reading file \"" + inputPath + "\\" + inputFileName + "\" --------> Finished.");
			System.out.println("    File \"" + outputPath + "\\" + outputFileName1_AllTags + " --------> Finished ...");
			System.out.println("    File \"" + outputPath + "\\" + outputFileName2_AllValidCharacters + " --------> Finished ...");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}//public static void findValidSOTags(....
//--------------------------------------------------------------------------------------------------------------------------------------------
	public static void joinTwoSODataSetsAndFilterEverythingOnUsersWithEMailHash(String oldDataSetInputPath, String oldDataSetInputXML,
			String newDataSetInputPath, String newDataSetInputXML,
			//Output parameters:
			String oldDataSetOutputPath, String oldDataSetOutputTSV, 
			String mergedDataSetOutputPath, String mergedDataSetOutputTSV){
		try{
			System.out.println("    1- Reading Old Users (with emailHash) --------> Started ...");
			BufferedReader br1 = new BufferedReader(new FileReader(oldDataSetInputPath + "\\" + oldDataSetInputXML)); 
			FileWriter writer1 = new FileWriter(oldDataSetOutputPath + "\\" + oldDataSetOutputTSV);
			String s, anId, aCreationDate, aDisplayName, anEmailHash;
			HashMap<String, String[]> oldUsers = new HashMap<String, String[]>();
			int i = 0, oldUsersRead;
			System.out.println("        2- Building old users' TSV file --------> Started ...");
			writer1.append("id\temailHash\tCreationDate\tDisplayName\n");
			while((s = br1.readLine()) != null) {
				if (s.startsWith("  <row ")){
					anId = readStringAfterIndicatorUntilDoubleQoutation(s, " Id=\"");
					anEmailHash = readStringAfterIndicatorUntilDoubleQoutation(s,  " EmailHash=\"");
					aCreationDate = readStringAfterIndicatorUntilDoubleQoutation(s, " CreationDate=\"");
					aDisplayName = readStringAfterIndicatorUntilDoubleQoutation(s,  " DisplayName=\"");
					oldUsers.put(anId, (new String[] { anId, anEmailHash, aCreationDate, aDisplayName }));
					writer1.append(anId + "\t" + anEmailHash + "\t" + aCreationDate + "\t" + aDisplayName + "\n");
					i++;
					if (i % 100000 == 0)
						System.out.println("            " + Constants.integerFormatter.format(i));
				}//if (s.st...
			}//while((s = ....
			oldUsersRead = i;
			System.out.println("        2- Building old users' TSV file --------> Finished.");
			System.out.println("    1- Reading Old Users (with emailHash) --------> Finished.");
			System.out.println();
			writer1.flush();
			writer1.close();
			br1.close();
			
			System.out.println("    3- Reading New Users (without emailHash) --------> Started ...");
			BufferedReader br2 = new BufferedReader(new FileReader(newDataSetInputPath + "\\" + newDataSetInputXML)); 
			System.out.println("        4- Building merged TSV file (EMailHash from oldUsers plus other info from newUsers) --------> Started ...");
			FileWriter writer2 = new FileWriter(mergedDataSetOutputPath + "\\" + mergedDataSetOutputTSV);
			int newUsersRead = 0, 
					newUsersMatchingOldUsers_IdButNoEmailHashForOldUser = 0, 
					newUsersCompletelyMatchingOldUsers = 0, 
					newUsersWhoseOnlyIdMatchOldUsers = 0, 
					newUsersWithNewId = 0,
					newUsersWhoseIdMatchesOldUsersAndHaveEmailHash = 0;
			writer2.append("SOId\temailHash\tCreationDate\tDisplayName\n");
			while((s = br2.readLine()) != null) {
				if (s.startsWith("  <row ")){
					anId = readStringAfterIndicatorUntilDoubleQoutation(s, " Id=\"");
					aCreationDate = readStringAfterIndicatorUntilDoubleQoutation(s, " CreationDate=\"");
					aDisplayName = readStringAfterIndicatorUntilDoubleQoutation(s,  " DisplayName=\"");
					String[] anOldUser_sFields = new String[4];
					anOldUser_sFields = oldUsers.get(anId);
					if (anOldUser_sFields== null){
						newUsersWithNewId++;
						//System.out.println("            Case 1: Old: " + anOldUser_sFields + "        New: " + aCreationDate + "    " + aDisplayName);
					}
					else{
						if (anOldUser_sFields[1].equals(""))
							newUsersMatchingOldUsers_IdButNoEmailHashForOldUser++;
						else{//anOldUser_sFields[1] is the emailHash:
							writer2.append(anId + "\t" + anOldUser_sFields[1] + "\t" + aCreationDate + "\t" + aDisplayName + "\n");
							newUsersWhoseIdMatchesOldUsersAndHaveEmailHash++;
							if (aCreationDate.equals(anOldUser_sFields[2]) && aDisplayName.equals(anOldUser_sFields[3]))
								newUsersCompletelyMatchingOldUsers++;
							else
								newUsersWhoseOnlyIdMatchOldUsers++;
						}//else.
					}
					newUsersRead++;
					//if (newUsersRead ==100) ////////////////////////////////////////////////////////////////////////////////////////////
					//	break; ////////////////////////////////////////////////////////////////////////////////////////////
					if (newUsersRead % 100000 == 0)
						System.out.println("            " + Constants.integerFormatter.format(newUsersRead));
				}//if (s.st...
			}//while((s = ....
			System.out.println("        4- Building merged TSV file (EMailHash from oldUsers plus other info from newUsers) --------> Finished.");
			System.out.println("    3- Reading New Users (without emailHash) --------> Finished.");
			System.out.println();
			System.out.println("    " + oldUsersRead + ": Total number of old users read.");
			System.out.println("    " + newUsersRead + ": Total number of new users read.");
			System.out.println();
			System.out.println("    " + newUsersWhoseIdMatchesOldUsersAndHaveEmailHash + ": Number of new users matching id of old users.");
			System.out.println("        " + newUsersCompletelyMatchingOldUsers + ": Number of new users whose id and all other fields match old users (perfect).");
			System.out.println("        " + newUsersWhoseOnlyIdMatchOldUsers + ": Number of new users whose Id matches old users but some fields don't match (Still correct).");
			System.out.println();
			System.out.println("    " + newUsersMatchingOldUsers_IdButNoEmailHashForOldUser + ": Number of new users matching id of old users but no emailHash for old user (useless due to no emailHash).");
			System.out.println("    " + newUsersWithNewId + ": Number of new users not in the old users  (useless due to no previous info, so no emailHash).");
			writer2.flush();
			writer2.close();
			br2.close();
			System.out.println("Finished.");
	}
		catch (Exception e) {
			e.printStackTrace();
		}
	}//public static void joinTwoSODataSetsAndFilterEverythingOnUsersWithEMailHash(....
//--------------------------------------------------------------------------------------------------------------------------------------------
	public static void xmlToTSV(String inputXMLPath, String inputXMLFileName,
			String tsvHeaderFieldsSeparatedByDollar, 
			String delimiter1AsValueSeparator, String delimiter2AsValueSeparator,String delimiter1AsValueSeparatorRegex, String delimiter2AsValueSeparatorRegex,  
			String fieldsThatNeedCleanUpSeparatedByDollar, 
			String outputTSVPath, String outputTSVFileName){
		try{ //This method reads an XML file of Stack Overflow data set (like Posts or Votes), and converts it (the given fields) into TSV format. The delimiters are only used for array items (e.g., Tags). 
			System.out.println("    1- Reading XML --------> Started ...");
			System.out.println("        2- Writing TSV --------> Started ...");
			BufferedReader br1 = new BufferedReader(new FileReader(inputXMLPath + "\\" + inputXMLFileName)); 
			FileWriter writer1 = new FileWriter(outputTSVPath + "\\" + outputTSVFileName);
			String s, field, fieldValue;
			int i = 0, j;
			String[] fields = tsvHeaderFieldsSeparatedByDollar.split("\\$");
			String[] fieldsThatNeedCleanup = fieldsThatNeedCleanUpSeparatedByDollar.split("\\$");
			//Writing the header:
			for (j=0; j<fields.length-1; j++)
				if (fields[j].startsWith("[]"))
					writer1.append(fields[j].replaceAll("\\[\\]", "") + "\t");
				else
					writer1.append(fields[j] + "\t");
			if (fields[j].startsWith("[]"))
				writer1.append(fields[j].replaceAll("\\[\\]", "") + "\n");
			else
				writer1.append(fields[j] + "\n");

			//XML contains two additional lines; Just skip them;
			br1.readLine();
			br1.readLine();

			while((s = br1.readLine()) != null) {
				if (s.startsWith("  <row ")){
					for (j=0; j<fields.length; j++){
						field = fields[j];
						if (field.startsWith("[]")){
							field = field.replaceAll("\\[\\]", "");
							fieldValue = readFieldInXMLRow(s, field);
							fieldValue = convertTwoDelimitersBeforeAndAfterToOneDelimiterBetween(fieldValue, 
									delimiter1AsValueSeparator, delimiter2AsValueSeparator, delimiter1AsValueSeparatorRegex, delimiter2AsValueSeparatorRegex, 
									Constants.SEPARATOR_FOR_ARRAY_ITEMS);
							fieldValue = "[" + fieldValue + "]";
						}//if (fiel....
						else{
							fieldValue = readFieldInXMLRow(s, field);
							if (fieldsThatNeedCleanup.length > 0)
								for (int k=0; k<fieldsThatNeedCleanup.length; k++)
									if (field.equals(fieldsThatNeedCleanup[k]))
										fieldValue = MyUtils.applyRegexOnString(Constants.allValidCharactersInSOQUESTION_AND_ANSWER_ForRegEx, fieldValue);
						}
						if (j!=fields.length-1)
							writer1.append(fieldValue + "\t");
						else
							writer1.append(fieldValue + "\n");
					}//for (j=0;....
					i++;
					//				if (i>25) 
					//					break;
					if (i % 500000 == 0)
						System.out.println("            " + Constants.integerFormatter.format(i));
				}//if (s.st...
			}//while((s = ....
			//		oldUsersRead = i;
			System.out.println("        2- Writing TSV --------> Finished.");
			System.out.println("    1- Reading XML --------> Finished.");
			System.out.println();
			writer1.flush();
			writer1.close();
			br1.close();
		}
		catch (Exception e){
			e.printStackTrace();
			}
	}//xmlToTSV().
//--------------------------------------------------------------------------------------------------------------------------------------------
//--------------------------------------------------------------------------------------------------------------------------------------------
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//findAllSOTagsAndTheriValidCharacters(Constants.DATASET_DIRECTORY_SO_WITHOUT_EMAIL_HASH, Constants.DATASET_DIRECTORY_SO_WITHOUT_EMAIL_HASH, "Tags.xml", "allTags.txt", "allValidCharactersInSOTags.txt");
		//Successfully done:
		//joinTwoSODataSetsAndFilterEverythingOnUsersWithEMailHash(Constants.DATASET_DIRECTORY_SO_WITH_EMAIL_HASH, Constants.DATASET_DIRECTORY_SO_WITHOUT_EMAIL_HASH, Constants.DATASET_DIRECTORY_SO_Mixed);
		
		//Run successfully:
//		joinTwoSODataSetsAndFilterEverythingOnUsersWithEMailHash(Constants.DATASET_DIRECTORY_SO_WITH_EMAIL_HASH, "Users.xml", 
//				Constants.DATASET_DIRECTORY_SO_WITHOUT_EMAIL_HASH, "Users.xml", 
//				Constants.DATASET_DIRECTORY_SO_WITH_EMAIL_HASH, "Users.tsv", 
//				Constants.DATASET_DIRECTORY_SO_Mixed, "SOUsers-Mixed.tsv"); 

		//Convert XML to TSV:
		//Run successfully:
		xmlToTSV(Constants.DATASET_DIRECTORY_SO_WITHOUT_EMAIL_HASH, "Posts.xml", 
		"Id$PostTypeId$OwnerUserId$ParentId$Score$[]Tags$CreationDate$AnswerCount$Title", 
		"&lt;", "&gt;", "\\&lt\\;", "\\&gt\\;", 
		"Title", 
		Constants.DATASET_DIRECTORY_SO_WITHOUT_EMAIL_HASH, "Posts.tsv");

		//Run successfully:
//		xmlToTSV(Constants.DATASET_DIRECTORY_SO_WITHOUT_EMAIL_HASH, "Tags.xml", 
//		"Id$TagName$Count", 
//		"", "", "", "", 
//		Constants.DATASET_DIRECTORY_SO_WITHOUT_EMAIL_HASH, "Tags.tsv");

		//Run successfully:
//		xmlToTSV(Constants.DATASET_DIRECTORY_SO_WITHOUT_EMAIL_HASH, "Users.xml", 
//		"Id$Reputation$CreationDate$DisplayName", 
//		"", "", "", "", 
//		Constants.DATASET_DIRECTORY_SO_WITHOUT_EMAIL_HASH, "Users.tsv");

		//Run successfully:
//		xmlToTSV(Constants.DATASET_DIRECTORY_SO_WITHOUT_EMAIL_HASH, "Votes.xml", 
//		"Id$PostId$VoteTypeId$CreationDate$UserId", 
//		"", "", "", "", 
//		Constants.DATASET_DIRECTORY_SO_WITHOUT_EMAIL_HASH, "Votes.tsv");
		
	}

}
