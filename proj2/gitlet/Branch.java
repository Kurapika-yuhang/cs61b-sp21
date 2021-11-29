package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

public class Branch implements Serializable {

    private String branchName;

    private String pointerCommitId;

    private Commit HEADCommit;


    // initial Branch constructor
    public Branch(Commit initialCommit) {
        this.branchName = "master";
        this.HEADCommit = initialCommit;
        this.pointerCommitId = HEADCommit.getId();
    }

    public Branch(Commit HEADCommit, String newBranchName) {
        this.branchName = newBranchName;
        this.pointerCommitId = HEADCommit.getId();
        this.HEADCommit = HEADCommit;
    }

    /** read Branch object from file */
    public static Branch fromFile(String branchName) {
        File file = Utils.join(Repository.BRANCHES_DIR, branchName);
        if (!file.exists()) {
            return null;
        }
        return Utils.readObject(file, Branch.class);
    }

//    public static Branch getActiveBranch() {
//        String activeBranchName = Utils.readContentsAsString(Repository.ACTIVE_BRANCH);
//        return fromFile(activeBranchName);
//    }

    public static String getActiveBranchName() {
        return Utils.readContentsAsString(Repository.ACTIVE_BRANCH);
    }

    /** save Branch object to file */
    public void save() {
        File file = Utils.join(Repository.BRANCHES_DIR, this.branchName);
        Utils.writeObject(file, this);
    }

    /** save this branch name! to ACTIVE_BRANCH */
    public void saveActiveBranch() {
        Utils.writeContents(Repository.ACTIVE_BRANCH, this.branchName);
    }


    /** GET HEAD pointer */
    public static String getHEAD() {
        return Utils.readContentsAsString(Repository.HEAD);
    }

    /** set up HEAD pointer */
    public static void setupPointer(File file, String commitId) {
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Utils.writeContents(file, commitId);
    }

    public Commit getHEADCommit() {
        return HEADCommit;
    }


    public String getBranchName() {
        return branchName;
    }

    public String getPointerCommitId() {
        return pointerCommitId;
    }


}
