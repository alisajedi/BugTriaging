package Data;

import java.util.ArrayList;
import java.util.Collections;

public class Issue implements Comparable<Issue>{
	public String id;
	public String ownerLoginAndProjectName;
	public String reporterId;
	public String reporterLogin;
	public String assigneeId;
	public String assigneeLogin;
	public String createdAt;
	public String numberOfComments;
	public String labels;
	public String title;
	public String body;
	
	@Override
	public int compareTo(Issue iss) {
		if (this.createdAt != null && iss.createdAt != null)		
			return this.createdAt.compareTo(iss.createdAt);
		else
			if (this.createdAt == null && iss.createdAt == null)
				return 0;
			else
				if (this.createdAt != null)
					return 1;
				else 
					return -1;
	}
	
	public static void main(String args[]){
		ArrayList<Issue> issAL = new ArrayList<Issue>();

		Issue i1 = new Issue();
		i1.createdAt = "b";
		issAL.add(i1);

		Issue i2 = new Issue();
		i2.createdAt = "a";
		issAL.add(i2);
		
		Issue i3 = new Issue();
		i3.createdAt = "0";
		issAL.add(i3);

		Collections.sort(issAL);
		for (Issue i:issAL)
			System.out.println(i.createdAt);
		i1.createdAt="a";
		
	}
}
