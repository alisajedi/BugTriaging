package bugTriaging;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import bugTriaging.Constants.ConditionType;

public class MyUtils {
	public static String applyRegexOnString(String regex, String value){
		Pattern pattern = Pattern.compile("[^"+regex+"]+");
		Matcher matcher = pattern.matcher(value);
		if (matcher.find())
			value = value.replaceAll("[^"+regex+"]+", " ");
		if (value.equals(""))
			value = " ";
		return value;
	}
	//------------------------------------------------------------------------------------------------------------------------------------------------
	public static boolean runLogicalComparison(ConditionType conditionType, String value1A, String value1B, String value2A, String value2B){
		boolean result = false;
		if (conditionType == ConditionType.NO_CONDITION)
			result = true;
		else
			if (value1A.equals(value1B)){
				if (conditionType == ConditionType.OR)
					result = true;
				else //conditionType == ConditionType.AND
					if (value2A.equals(value2B))
						result = true;
			}//if (fiel....
			else
				if (conditionType == ConditionType.OR)
					if (value2A.equals(value2B))
						result = true;
		return result;
	}//runLogicalComparison().
	//------------------------------------------------------------------------------------------------------------------------------------------------
	public static boolean compareTwoStringArrays(String[] s1, String[] s2){
		boolean result = true;
		if (s1.length != s2.length)
			result = false;
		else
			for (int i=0; i<s1.length; i++)
				if (!s1[i].equals(s2[i])){
					result = false;
					break;
				}
		return result;
	}//compareTwoStringArrays().
	//------------------------------------------------------------------------------------------------------------------------------------------------
	//No longer need this. We have implemented the sort by integer in the TSVManipulations:
//	public static TreeMap<String, ArrayList<String[]>> getTreeMap_SortedBasedOnInteger_Descending(TreeMap<String, ArrayList<String[]>> inputTreeMap){
//		TreeMap<String, ArrayList<String[]>> tm = new TreeMap<String, ArrayList<String[]>>(new Comparator<String>(){
//			public int compare(String s1, String s2){//We want the descending order of number:
//				if (Integer.parseInt(s1) < Integer.parseInt(s2))
//					return 1;
//				else
//					if (Integer.parseInt(s1) > Integer.parseInt(s2))
//						return -1;
//					else
//						return 0;
//			}
//		});
//		for (String keyField:inputTreeMap.keySet())
//			tm.put(keyField, inputTreeMap.get(keyField));
//		return tm;
//	}//getSortedTreeMap().
	//------------------------------------------------------------------------------------------------------------------------------------------------
	//------------------------------------------------------------------------------------------------------------------------------------------------
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
