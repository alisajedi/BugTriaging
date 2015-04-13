package bugTriaging;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FixMySQLDataSet {
//--------------------------------------------------------------------------------------------------------------------------------------------
	public static void fixUsers(String inputPath, String inputUsersTSV, String outputUsersTSV){
		try{
			BufferedReader br;
			br = new BufferedReader(new FileReader(inputPath + "\\" + inputUsersTSV + ".tsv")); 
			System.out.println("    Parsing file \"" + inputPath + "\\" + inputUsersTSV + "\" --------> Started ...");
			System.out.println("    Building file \"" + inputPath + "\\" + outputUsersTSV + "\" --------> Started ...");
			FileWriter writer = new FileWriter(inputPath + "\\" + outputUsersTSV + ".tsv");
			String s;
			s=br.readLine(); //header.
			int i=0, p1=0, p2=0, p3=0, p4=0;
			writer.append(s + "\n");
			while ((s=br.readLine())!=null){
				if (s.endsWith("\t"))
					s = s + " ";
				String[] fields = s.split("\t");
				if (fields[2].equals("\\N"))
					fields[2] = " ";
				if (fields[2].matches("(?i).*\\s(underscore)\\s.*"))//{
					fields[2] = fields[2].replaceAll("(?i)\\s(underscore)\\s", "_");
					//System.out.println("::::::::::::" + fields[2]);
				//}
				if (fields[2].matches("(?i).*\\s(dash)\\s.*"))//{
					fields[2] = fields[2].replaceAll("(?i)\\s(dash)\\s", "-");
					//System.out.println("::::::::::::" + fields[2]);
				//}
				//System.out.println("-----------");
				if (fields[2].matches("(?i)[a-zA-Z_\\.\\-]+\\s.*(at).*\\s[a-zA-Z_\\.\\-]+\\s.*(dot).*\\s[a-zA-Z_\\.\\-]+")){
					fields[2] = fields[2].replaceAll("(?i)\\s[^a-zA-Z0-9_\\.\\-]*(at)[^a-zA-Z0-9_\\.\\-]*\\s", "@");
					fields[2] = fields[2].replaceAll("(?i)\\s[^a-zA-Z0-9_\\.\\-]*(dot)[^a-zA-Z0-9_\\.\\-]*\\s", ".");
					p1++;
				}
				if (fields[2].matches("(?i)[a-zA-Z_\\.\\-]+\\s.*(at).*\\s.*")){
					fields[2] = fields[2].replaceAll("(?i)\\s[^a-zA-Z0-9_\\.\\-]*(at)[^a-zA-Z0-9_\\.\\-]*\\s", "@");
					p2++;
				}
				if (fields[2].matches("(?i)[a-zA-Z_\\.\\-]+\\s.*(dot).*\\s.*")){
					fields[2] = fields[2].replaceAll("(?i)\\s[^a-zA-Z0-9_\\.\\-]*(dot)[^a-zA-Z0-9_\\.\\-]*\\s", ".");
					p3++;
				}
				if (fields[2].matches("(?i).*\\s[a-zA-Z_\\.\\-]+@[a-zA-Z_\\.\\-]+\\.[a-zA-Z_\\.\\-]+.*") ||
						fields[2].matches("(?i).*[a-zA-Z_\\.\\-]+@[a-zA-Z_\\.\\-]+\\.[a-zA-Z_\\.\\-]+\\s.*")){
					String ss = fields[2];
					Pattern p = Pattern.compile("[a-zA-Z_\\.\\-]+@[a-zA-Z_\\.\\-]+\\.[a-zA-Z_\\.\\-]+");
					Matcher m = p.matcher(fields[2]);
			        if(m.find())
			            fields[2] = m.group();					
					System.out.println(ss + "    ---->    " + fields[2]);
					p4++;
				}
				writer.append(fields[0] + "\t" + fields[1] + "\t" + fields[2] + "\n");
				i++;
			}//while ((s=br....
			System.out.println("Records fixed: " + p1 + "    " + p2 + "    " + p3 + "    " + p4);
			System.out.println("    Building file \"" + inputPath + "\\" + outputUsersTSV + "\" --------> Finished ...");
			System.out.println("    Parsing file \"" + inputPath + "\\" + inputUsersTSV + "\" --------> Finished ...");
			System.out.println(i + " records copied");
			br.close();
			writer.flush();
			writer.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
//--------------------------------------------------------------------------------------------------------------------------------------------
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		fixUsers(Constants.DATASET_DIRECTORY_GH_MySQL_TSV, "users3 (only important fields)", "users3 (only important fields)-fixed");
	}
}
