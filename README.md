# BugTriaging
Bug Triaging with Stack Overflow data combined with Github data

How to run the code:
In order to run the program after forking and cloning the project on your local drive, open the project in Eclipse. Then open the "TriagingAlgorithm.java" in "src\bugTriaging" package. This is the class that runs the code, but before running, we need to set the paths. 
If you look at the main() function at the end of this class, it is calling "triage2_basedOnMultipleCriteria()" function, sending some paths to it. This is the place you can set your own path and filenames. Currently, the paths are constants that are defined in "Constants.java" file. However, you need to change them to your desired paths.

For the input files, they are provided in this repository at "Data\Input" folder. They are 7 ".tsv" files. In the "TriagingAlgorithm.java" class, when calling the function "triage2_basedOnMultipleCriteria()", we refer to 5 of them:
        Posts-madeByCommunityMembers-top20Projects.tsv
        Select one of these two: 
              communitiesOf17Projects.tsv     --> used for evaluation 
              communitiesOf3Projects.tsv     --> used for tuning
        Select one of these two: 
              communitiesSummary(17Projects).tsv     --> used for evaluation 
              communitiesSummary(3Projects).tsv     --> used for tuning
        issues2-forTop20Projects.tsv
        projects-top20.tsv
For easier running, we provided the run command for both tuning and evaluation commented by the followings:
        //Main Run (17 projects):
        //Test Run (3 projects):
but commented the first one and uncommented the second. You can uncomment and run either or both.

For further assistance on the input parameters, you can click on one of the parameters of the function, and then press "Ctrl+ Shift+Space". Then Eclipse provides the names of the parameters that are selected supportive enough to help you understand the meaning of them. 

If you have any question, comment or suggestion, you can contact me at:
        http://alisajedi.blogspot.ca/
Good luck!
