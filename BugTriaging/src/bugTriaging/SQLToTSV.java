package bugTriaging;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;

public class SQLToTSV {

	//This method extracts the records in .SQL file of commits (the MySQL data set):
	public static void readCommitsAndWriteNeededFieldsToTSV(String inputPath, String outputPath, String inputFileName, String outputFileName1, String outputFileName2){
		try{
			FileReader fr = new FileReader(inputPath + "\\" + inputFileName);
			BufferedReader br = new BufferedReader(fr); 
			FileWriter fw1 = new FileWriter(outputPath + "\\" + outputFileName1), fw2 = new FileWriter(outputPath + "\\" + outputFileName2);

			System.out.println("    Reading file \"" + inputPath + "\\" + inputFileName + "\" --------> Started ...");
			System.out.println("    Building file \"" + outputPath + "\\" + outputFileName1 + "\" --------> Started ...");
			System.out.println("    Building file \"" + outputPath + "\\" + outputFileName2 + "\" --------> Started ...");
			String s;
			String[] records, fields; 
			int i;
			long j=1, p=0;
					
			fw1.append("committer_id\tproject_id\tcreated_at\n");
			fw2.append("committer_id\tproject_id\n");
			while((s = br.readLine()) != null) {
				if (s.startsWith("INSERT")){
					records = s.split("\\),\\(");
					records[0] = records[0].replaceAll(".*\\(", "");
					records[records.length-1] = records[records.length-1].replaceAll("\\);", "");
					for (i=0; i<records.length; i++){
						if (j % 1000000 == 0)
							System.out.println("        " + j / 1000000 + ",000,000");
						j++;
						fields = records[i].split(",");
						if (fields.length == 7){
							fw1.append(fields[3] + "\t" + fields[4] + "\t" + fields[5].replaceAll("\\'", "") + "\n");
							fw2.append(fields[3] + "\t" + fields[4] + "\n");
						}//if (fields....
						else{
							p++;
							System.out.println("        Damaged record: " + records[i]);
						}
					}//for (i....
				}
			}//while((s = ....
			
			System.out.println("    Number of damaged records (with less than 7 fields):" + p);
			fw1.flush();
			fw1.close();
			fw2.flush();
			fw2.close();
			br.close();
			fr.close();

			System.out.println("    Reading file \"" + inputPath + "\\" + inputFileName + "\" --------> Finished.");
			System.out.println("    Building file \"" + outputPath + "\\" + outputFileName1 + " --------> Finished ...");
			System.out.println("    Building file \"" + outputPath + "\\" + outputFileName2 + " --------> Finished ...");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}//public void findValidSOTags(....
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		readCommitsAndWriteNeededFieldsToTSV(Constants.DATASET_DIRECTORY_GH_MySQL , Constants.DATASET_DIRECTORY_GH_MySQL_TSV, "commits.sql", "commits.tsv", "commits (only important fields).tsv");
	}

}
