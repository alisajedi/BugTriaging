package bugTriaging;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
//--------------------------------------------------------------------------------------------------------------------------------------------
//--------------------------------------------------------------------------------------------------------------------------------------------
class JSONtoTSV {
	public static void mergeMultipleJSONFilesAndDeleteObjectId(String inputPath, String outputPath, String fileName){ //This method deletes all the data like      "_id" : ObjectId( "50abaedaedecb56a070002ad" ),         It performs this deletion on all files starting by filename. Suppose fileName="issues", then it performs the task on issues1.json, issues2.json, issues3.json, ..., all in the same path.
		try{
			File folder = new File(inputPath);
			BufferedReader br;
			FileWriter writer = new FileWriter(outputPath + "\\" + fileName + ".json");
			System.out.println("    Building file \"" + fileName + ".json\" --------> Started ...");
			String s;
			int i = 0;
			for (File fileEntry : folder.listFiles()) {
				if (fileEntry.isFile() && fileEntry.getName().matches(fileName+"\\d+\\.json")){ //read all files starting with the fileName, then something (usually a number, but mandatory) and finally ".json"
					i++;
					System.out.println("        " + i + "- Reading file \"" + fileEntry.getName() + "\", cleaning and writing in file \"" + fileName + ".json");
					br = new BufferedReader(new FileReader(fileEntry));
					while ((s = br.readLine()) != null){ 
						if (s.contains("_id\" : ObjectId"))
							s = s.replaceAll("\\\"_id\\\" : ObjectId\\( \"[0-9a-z]{24}\\\" \\), ", ""); //removing the <"_id" : ObjectId( "...................." ),>
						writer.append(s + "\n");
					} //while ((s =....
					br.close();
				} // if (file....
			}// for (File....
			writer.flush();
			writer.close();
			System.out.println("    File \"" + fileName + ".json\" --------> Finished.");
			System.out.println("    --------------------------------------------------");
			System.out.println("    --------------------------------------------------");
		} // try.
		catch (Exception e) {
			e.printStackTrace();
		}
	}//public deleteObjectId(....
//--------------------------------------------------------------------------------------------------------------------------------------------
	static String getValueFromJSONAndRemoveInvalidCharactersAndPutSeparatorBeforeIt(JSONObject jsO, String fieldName, String fileName, AtomicBoolean itIsTheFirstFieldInTheLine){
		String fieldValue;
		if (jsO.get(fieldName) == null)
			fieldValue = "";
		else
			fieldValue = jsO.get(fieldName).toString();
		//Remove everything that are not in the valid characters:
		if (Constants.USEFUL_FIELDS_IN_JSON_FILES.containsKey(fileName + ":FieldsToRemoveInvalidCharacters"))
			if (Constants.USEFUL_FIELDS_IN_JSON_FILES.get(fileName + ":FieldsToRemoveInvalidCharacters").contains(fieldName))
				fieldValue=fieldValue.replaceAll("[^"+Constants.allValidCharactersInSOTags_ForRegEx+"]+", " ");
		//delimiter between fields:
		if (fieldValue.equals(""))
			fieldValue = " ";
		if (itIsTheFirstFieldInTheLine.get())
			itIsTheFirstFieldInTheLine.set(false);
		else
			fieldValue = "\t" + fieldValue;
		return fieldValue;
	}//String getValueFromJSONAndRemoveInvalidCharactersAndPutSeparatorBeforeIt(....
//--------------------------------------------------------------------------------------------------------------------------------------------
public static void copyNeededFieldsFromJSONIntoNewTSVFile(String inputPath, String outputPath, String fileName){
	try{
		BufferedReader br = new BufferedReader(new FileReader(inputPath + "\\" + fileName + ".json")); 
		JSONParser jsonParser = new JSONParser();
		JSONObject jsonObject;
		System.out.println("    1- Parsing input json file --------> Started ...");
		System.out.println("        2- Building output tsv file --------> Started ...");
		FileWriter writer = new FileWriter(outputPath + "\\" + fileName + ".tsv");
		String title = "";
		for (String field: Constants.USEFUL_FIELDS_IN_JSON_FILES.get(fileName + ":labels")){
			if (!title.equals(""))	
				title = title + "\t";
			title = title + field;					
		}//for (String 
		writer.append(title + "\n");
			
		String s, severalFieldsSeparatedByDollarSign, firstLevelField, secondLevelJSONString, fieldValue;
		String[] fieldsArray;
		int i=0, j;
		AtomicBoolean itIsTheFirstFieldInTheLine = new AtomicBoolean();
		boolean itIsTheFirstMemberInTheJSONArray;
		while((s = br.readLine()) != null) {
			//s=s.replaceAll("\\p{Cntrl}", "");
			jsonObject= (JSONObject) jsonParser.parse(s);
			itIsTheFirstFieldInTheLine.set(true);
			for (String field: Constants.USEFUL_FIELDS_IN_JSON_FILES.get(fileName))
				if (field.matches(".*\\{\\}.*")){
					firstLevelField = field.substring(0, field.indexOf("{}"));
					
					if (jsonObject.get(firstLevelField) == null){
						secondLevelJSONString = "";
						severalFieldsSeparatedByDollarSign = field.substring(field.indexOf("{}") + 2);
						fieldsArray = severalFieldsSeparatedByDollarSign.split("\\$");
						for (j=0; j<fieldsArray.length; j++){
							if (itIsTheFirstFieldInTheLine.get()){
								itIsTheFirstFieldInTheLine.set(false);
								fieldValue = " ";
							}//if.
							else
								fieldValue = "\t "; 
							writer.append(fieldValue);
						}//for
					}//if.
					else{
						secondLevelJSONString = jsonObject.get(firstLevelField).toString();
					
						severalFieldsSeparatedByDollarSign = field.substring(field.indexOf("{}") + 2);
						fieldsArray = severalFieldsSeparatedByDollarSign.split("\\$");
						for (j=0; j<fieldsArray.length; j++){
							JSONParser secondLevelJSONParser = new JSONParser();
							JSONObject secondLevelJSONObject;

							secondLevelJSONObject = (JSONObject) secondLevelJSONParser.parse(secondLevelJSONString);
							fieldValue = getValueFromJSONAndRemoveInvalidCharactersAndPutSeparatorBeforeIt(secondLevelJSONObject, fieldsArray[j], fileName, itIsTheFirstFieldInTheLine);  
							writer.append(fieldValue);
						}//for (j....
					}//else.
				} //if (field....
				else
					if (field.matches(".*\\[\\].*")){
						fieldValue = "[";
						JSONArray jsonArray = (JSONArray) jsonObject.get("labels");
						Iterator<?> iterator = jsonArray.iterator();
						itIsTheFirstMemberInTheJSONArray = true;
						while (iterator.hasNext()) {
							JSONObject labelsJsonObject = (JSONObject) iterator.next();
							if (itIsTheFirstMemberInTheJSONArray){
								itIsTheFirstMemberInTheJSONArray = false;
								fieldValue = fieldValue + labelsJsonObject.get("name"); 								
							}
							else
								fieldValue = fieldValue + Constants.SEPARATOR_FOR_ARRAY_ITEMS + labelsJsonObject.get("name"); 								
							}//while.
						fieldValue = fieldValue + "]";
						
						if (itIsTheFirstFieldInTheLine.get())
							itIsTheFirstFieldInTheLine.set(false);
						else
							fieldValue = "\t" + fieldValue;
						
						writer.append(fieldValue);
					}//if (field....
				else{
					fieldValue = getValueFromJSONAndRemoveInvalidCharactersAndPutSeparatorBeforeIt(jsonObject, field, fileName, itIsTheFirstFieldInTheLine);  	
					writer.append(fieldValue);
				}//else.
			writer.append("\n");
			i++;
			if (i % 100000 == 0)
				System.out.println("            " + Constants.integerFormatter.format(i));
		}
		System.out.println("        2- Building output tsv file --------> Finished ...");
		System.out.println("    1- Parsing input json file --------> Finished ...");
		br.close();
		writer.flush();
		writer.close();
		System.out.println("Finished.");
	} // try.
	catch (Exception e) {
		e.printStackTrace();
	}
}//public static void copyNeededFieldsIntoNewTSVFile(....
//--------------------------------------------------------------------------------------------------------------------------------------------
//--------------------------------------------------------------------------------------------------------------------------------------------
	public static void main(String[] args) throws IOException, ParseException {
		//Merging several JSON files and Cleaning them:
		//mergeMultipleJSONFilesAndDeleteObjectId(Constants.DATASET_EXTERNAL_DIRECTORY_GH_JSON_SEPARATE, Constants.DATASET_EXTERNAL_DIRECTORY_GH_JSON_MERGED, "issues");
		//mergeMultipleJSONFilesAndDeleteObjectId(Constants.DATASET_EXTERNAL_DIRECTORY_GH_JSON_SEPARATE, Constants.DATASET_EXTERNAL_DIRECTORY_GH_JSON_MERGED, "users"); // users
		
		//mergeMultipleJSONFilesAndDeleteObjectId(Constants.DATASET_EXTERNAL_DIRECTORY_GH_JSON_SEPARATE, Constants.DATASET_EXTERNAL_DIRECTORY_GH_JSON_MERGED, "issue_comments"); //Commenters	
		//mergeMultipleJSONFilesAndDeleteObjectId(Constants.DATASET_EXTERNAL_DIRECTORY_GH_JSON_SEPARATE, Constants.DATASET_EXTERNAL_DIRECTORY_GH_JSON_MERGED, "commit_comments"); //Commenters	
		//mergeMultipleJSONFilesAndDeleteObjectId(Constants.DATASET_EXTERNAL_DIRECTORY_GH_JSON_SEPARATE, Constants.DATASET_EXTERNAL_DIRECTORY_GH_JSON_MERGED, "pull_request_comments"); //Commenters	
		//Above lines: successful.
			//project membership, issue reporters, committers --> barrasi shavad

		//Copying the needed fields from JSON to TSV file:
		//copyNeededFieldsFromJSONIntoNewTSVFile(Constants.DATASET_DIRECTORY_GH_JSON_MERGED, Constants.DATASET_DIRECTORY_GH_TSV, "users"); // commenters (issues, commits and pullRequests)?
		
		
		//Was run successfully:
		//mergeMultipleJSONFilesAndDeleteObjectId(Constants.DATASET_EXTERNAL_DIRECTORY_GH_JSON_SEPARATE, Constants.DATASET_EXTERNAL_DIRECTORY_GH_JSON_MERGED, "repos");
		copyNeededFieldsFromJSONIntoNewTSVFile(Constants.DATASET_EXTERNAL_DIRECTORY_GH_JSON_MERGED, Constants.DATASET_DIRECTORY_GH_MongoDB_TSV, "repos"); // commenters (issues, commits and pullRequests)?
	}
//--------------------------------------------------------------------------------------------------------------------------------------------
}