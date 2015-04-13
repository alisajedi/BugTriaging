package Data;
import bugTriaging.Constants;

public class IssueTextualInformation {
	public String projectLanguage;
	public String projectDescription;
	public String[] issueLabels;
	public String issueTitle;
	public String issueBody;
	public IssueTextualInformation(String projectLanguage, String projectDescription, String issueLabels_separatedByDollar, String issueTitle, String issueBody){
		this.projectLanguage = projectLanguage.toLowerCase();
		this.projectDescription = projectDescription.toLowerCase();
		if (issueLabels_separatedByDollar.equals("[]"))
			this.issueLabels = null;
		else
			this.issueLabels = issueLabels_separatedByDollar.toLowerCase().split(Constants.SEPARATOR_FOR_ARRAY_ITEMS);
		this.issueTitle = issueTitle.toLowerCase();
		this.issueBody = issueBody.toLowerCase();
	}//IssueTextualInformation().
}
