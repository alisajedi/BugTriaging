package bugTriaging;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class MergeUsers {
	public static void MergeMongoDBANdMySQLUsers(String mongoDBInputPath, String mongoDBUsersTSV, 
			String mySQLInputPath, String mySQLusersTSV,
			String outputPath, String outputUsersTSV){
		try{
			BufferedReader br;
			//Reading MongoDB users and putting them in a HashMap:
			br = new BufferedReader(new FileReader(mongoDBInputPath + "\\" + mongoDBUsersTSV)); 
			System.out.println("    1- Parsing file \"" + mongoDBInputPath + "\\" + mongoDBUsersTSV + "\" and putting items in a HashMap --------> Started ...");
			HashMap<String, String> mongoDBUsers = new HashMap<String, String>();
			String s;
			int error = 0;
			String[] fields;
			int i=0;
			String mySQLId, mongoDBId, eMailAndId, login, eMail;
			br.readLine(); //header.
			while ((s=br.readLine())!=null){
				if (s.endsWith("\t"))
					s = s + " ";
				fields = s.split("\t");
				if (fields.length<2)
					error++;
				else{
					mongoDBId = fields[0];
					login = fields[1];
					eMail = fields[2];
					if (eMail.equals(""))
						eMail = " ";
					mongoDBUsers.put(login, eMail + "\t" + mongoDBId);
				}//else.
				i++;
				if (i % 500000 == 0)
					System.out.println("        " +  Constants.integerFormatter.format(i));
			}//while ((s=br....
			System.out.println("        Number of records in MongoDB with error: " + error);
			System.out.println("    1- Parsing file \"" + mongoDBInputPath + "\\" + mongoDBUsersTSV + "\" and putting items in a HashMap --------> Finished ...");
			br.close();
			
			//Reading MySQL users and putting them in another hashMap:
			br = new BufferedReader(new FileReader(mySQLInputPath + "\\" + mySQLusersTSV)); 
			System.out.println("    2- Parsing file \"" + mySQLInputPath + "\\" + mySQLusersTSV + " and putting items in a HashMap --------> Started ...");
			HashMap<String, String> mySQLUsers = new HashMap<String, String>();
			FileWriter writer = new FileWriter(outputPath + "\\" + outputUsersTSV);
			System.out.println("    3- Building file \"" + outputPath + "\\" + outputUsersTSV + " --------> Started ...");
			System.out.println("        Checking mainly from MySQL and partly MoongoDB (for email only in MySQL or email in both):");
			writer.append("login\temail\tmySQLId\tmongoDBId\tgotTheeMailFrom_1_MySQLOr_2_MongoDB\n");
			//HashMap<String, String> mySQLUsers = new HashMap<String, String>();
			i=0;
			String gotTheEMailFrom_1_MySQL_Or_2_MongoDB;
			String[] tempFields;
			br.readLine(); //header.
			while ((s=br.readLine())!=null){
				if (s.endsWith("\t"))
					s = s + " ";
				fields = s.split("\t");
				mySQLId = fields[0];
				login = fields[1];
				eMail = fields[2];
				mySQLUsers.put(login, mySQLId);

				gotTheEMailFrom_1_MySQL_Or_2_MongoDB = "1";
				if (mongoDBUsers.containsKey(login)){
					eMailAndId = mongoDBUsers.get(login);
					tempFields = eMailAndId.split("\t");
					if (eMail.equals(" ")){
						eMail = tempFields[0];
						gotTheEMailFrom_1_MySQL_Or_2_MongoDB = "2";
					}//if(mong....
					mongoDBId = tempFields[1];
				}//if (mong....
				else
					mongoDBId = "-";
				if (!eMail.equals(" ")){
					writer.append(login + "\t" + eMail + "\t" + mySQLId + "\t" + mongoDBId + "\t" + gotTheEMailFrom_1_MySQL_Or_2_MongoDB + "\n");
					if (mongoDBUsers.containsKey(login))
						mongoDBUsers.remove(login);
				}//if (!eMail....
				i++;
				if (i % 500000 == 0)
					System.out.println("        " +  Constants.integerFormatter.format(i));
			}//while ((s=br....
			
			System.out.println("        Check the remaining from MongoDB (for email only in MongoDb):");
			i=0;
			gotTheEMailFrom_1_MySQL_Or_2_MongoDB = "2";
			for (Map.Entry<String, String> entry: mongoDBUsers.entrySet()){
				login = entry.getKey();
				eMailAndId = entry.getValue();
				tempFields = eMailAndId.split("\t");
				eMail = tempFields[0];
				mongoDBId = tempFields[1];
				if (mySQLUsers.containsKey(login)){
					mySQLId = mySQLUsers.get(login);
					if (!eMail.equals("") && !eMail.equals(" "))
						System.out.println("yessssssss: " + mySQLId);
				}
				else
					mySQLId = "-";
				if ((!eMail.equals("")) && (!eMail.equals(" ")))
					writer.append(login + "\t" + eMail + "\t" + mySQLId + "\t" + mongoDBId + "\t" + gotTheEMailFrom_1_MySQL_Or_2_MongoDB + "\n");
				i++;
				if (i % 500000 == 0)
					System.out.println("        " +  Constants.integerFormatter.format(i));
			}//for.
			System.out.println("    3- Building file \"" + outputPath + "\\" + outputUsersTSV + " --------> Finished ...");
			System.out.println("    2- Parsing file \"" + mySQLInputPath + "\\" + mySQLusersTSV + " and putting items in a HashMap --------> Finished ...");
			br.close();
			writer.flush();
			writer.close();
			System.out.println("Good Luck");
		}
		catch (Exception e){
			e.printStackTrace();		
		}
	}//public void checkFitnessOfUsersForIssues(....
//--------------------------------------------------------------------------------------------------------------------------------------------
	public static String getMD5(String sInput) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("MD5");
		md.update(sInput.getBytes());
		byte byteData[] = md.digest();
		//convert the byte to hex format method 1
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < byteData.length; i++) 
			sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
		//convert the byte to hex format method 2
		StringBuffer hexString = new StringBuffer();
		for (int i=0;i<byteData.length;i++) {
			String hex=Integer.toHexString(0xff & byteData[i]);
			if(hex.length()==1) hexString.append('0');
			hexString.append(hex);
		}
		//	System.out.println("Digest(in hex format):: " + hexString.toString());
		return(hexString.toString());
	}
//--------------------------------------------------------------------------------------------------------------------------------------------
	public static void mergeGHAndSO(String ghInputPath, String ghInputTSV, String soInputPath, String soInputTSV, String outputPath, String outputUsersTSV){
		try{
			BufferedReader br;
			//Reading MongoDB users and putting them in a HashMap:
			br = new BufferedReader(new FileReader(soInputPath + "\\" + soInputTSV)); 
			System.out.println("    1- Parsing SO file --------> Started ...");
			HashMap<String, String> soUsers = new HashMap<String, String>();
			int error = 0;
			String[] fields;
			int i=0;
			String s="";
			br.readLine(); //header.
			while ((s=br.readLine())!=null){
				if (s.endsWith("\t"))
					s = s + " ";
				fields = s.split("\t");
				if (fields.length<4)
					error++;
				else
					soUsers.put(fields[1], fields[0]); //<eMailHash, SOId>
				i++;
				if (i % 500000 == 0)
					System.out.println("            " +  Constants.integerFormatter.format(i));
			}//while ((s=br....
			if (error>0)
				System.out.println("        Number of records in SO with error: " + error);
			System.out.println("    1- Parsing SO file --------> Finished ...");
			br.close();
			
			//Reading MySQL users and putting them in another hashMap:
			br = new BufferedReader(new FileReader(ghInputPath + "\\" + ghInputTSV)); 
			System.out.println("    2- Parsing GH file --------> Started ...");
			FileWriter writer = new FileWriter(outputPath + "\\" + outputUsersTSV);
			System.out.println("        3- Building file \"" + outputPath + "\\" + outputUsersTSV + "\" --------> Started ...");
			writer.append("SOId\teMailHash\tlogin\temail\tmySQLId\tmongoDBId\n");
			//HashMap<String, String> mySQLUsers = new HashMap<String, String>();
			br.readLine(); //header.
			i=0; 
			int numberOfMatchedRecords = 0;
			error = 0;
			String login, eMail, eMailHash, mySQLId, mongoDBId, soId;
			while ((s=br.readLine())!=null){
				if (s.endsWith("\t"))
					s = s + " ";
				fields = s.split("\t");
				if (fields[1].equals(" "))
					fields[1] = "";
				login = fields[0];
				eMail = fields[1];
				mySQLId = fields[2];
				mongoDBId = fields[3];
				if (eMail.equals(""))
					error++;
				else{
					eMail = eMail.toLowerCase();
					eMailHash = getMD5(eMail);
					if (soUsers.containsKey(eMailHash)){
						soId = soUsers.get(eMailHash);
						writer.append(soId + "\t" + eMailHash + "\t" + login + "\t" + eMail + "\t" + mySQLId + "\t" + mongoDBId + "\n");
						numberOfMatchedRecords++;
					}//if (soUs....
				}//else.
					i++;
				if (i % 500000 == 0)
					System.out.println("            " +  Constants.integerFormatter.format(i));
			}//while ((s=br....
			if (error>0)
				System.out.println("        Number of records in GH with error: " + error);
			System.out.println("        Found total of " + numberOfMatchedRecords + " matches.");
			System.out.println("        3- Building file \"" + outputPath + "\\" + outputUsersTSV + "\" --------> Finished ...");
			System.out.println("    2- Parsing GH file --------> Finished ...");
			br.close();
			writer.flush();
			writer.close();
			System.out.println("Finished.");
		}
		catch (Exception e){
			e.printStackTrace();		
		}
	}//mergeGHAndSO(
//--------------------------------------------------------------------------------------------------------------------------------------------
	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		MergeMongoDBANdMySQLUsers(Constants.DATASET_DIRECTORY_GH_MongoDB_TSV, "users.tsv", 
//				Constants.DATASET_DIRECTORY_GH_MySQL_TSV, "users3 (only important fields)-fixed.tsv",
//				Constants.DATASET_DIRECTORY_GH_Mixed, "GHUsers-mixed.tsv");
		
		mergeGHAndSO(Constants.DATASET_DIRECTORY_GH_Mixed, "GHUsers-mixed.tsv", 
				Constants.DATASET_DIRECTORY_SO_Mixed, "SOUsers-Mixed.tsv", 
				Constants.DATASET_DIRECTORY_GH_AND_SO_Mixed_TOGETHER, "commonUsers.tsv");
	}

}
