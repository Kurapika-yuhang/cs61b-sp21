package gitlet;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static gitlet.Utils.*;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Repository {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** sha1 id length */
    public static final int ID_LENGTH = 40;

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));

    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

    /** The .gitlet/objects directory. Store Blobs Commits */
    public static final File OBJECTS_DIR = join(GITLET_DIR, "objects");

    /** The .gitlet/objects directory. Store Blobs Commits */
    public static final File COMMITS_DIR = join(GITLET_DIR, "commits");

    /** The .gitlet/refs directory. */
    public static final File REFS_DIR = join(GITLET_DIR, "refs");

    /** The .gitlet/branches directory. */
    public static final File BRANCHES_DIR = join(GITLET_DIR, "branches");

    /** The .gitlet/branches directory. */
    public static final File ACTIVE_BRANCH = join(GITLET_DIR, "ACTIVE_BRANCH.txt");

    /** The .gitlet/staging area file   it's a file, not a directory! */
    public static final File STAGING_AREA = join(GITLET_DIR, "staging area");

    /** The .gitlet/HEAD  a pointer to the head commit*/
    public static final File HEAD = join(GITLET_DIR, "HEAD.txt");

    public static StagingArea stagingArea = STAGING_AREA.exists() ? StagingArea.fromFile() : new StagingArea();

    //public static String headCommitId = getHeadCommitId();

    //public static Commit HEADCommit = getHEADCommit();

    /* TODO: fill in the rest of this class. */

    /**
     * TODO: init method
     * TODO: create a .gitlet directory
     * TODO: if there exits a .gitlet repo, print the error message
     *
     * */

    public static void init() {
        if (GITLET_DIR.exists()) {
            // print error message
            System.out.println("A Gitlet version-control system already exists in the current directory.");
        } else {
            // make all directories
            GITLET_DIR.mkdir();
            OBJECTS_DIR.mkdir();
            REFS_DIR.mkdir();
            BRANCHES_DIR.mkdir();
            COMMITS_DIR.mkdir();

            // make all txt files
            try {
                HEAD.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                ACTIVE_BRANCH.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // make first commit and create initial head pointer
            Commit initialCommit = new Commit();
            // save the initial commit
            initialCommit.save();
            String initialCommitId = initialCommit.getId();

            // update HEAD

            Branch.setupPointer(HEAD, initialCommitId);

            // initial master branch
            Branch master = new Branch(initialCommit);
            master.save();
            master.saveActiveBranch(); // save master to active branch

            // create initial staging area
            createInitialStagingArea();
        }
    }

    /** Add the file to staging area */
    public static void add(String fileName) {
        File file = MyUtils.getFileFromCWD(fileName);
        if (!file.exists()) {
            System.out.println("The filepath given is: " + file.getPath());
            System.out.println("File does not exist.");
            System.exit(0);
        }

        // file exist
        boolean changed = stagingArea.add(file);
        if (changed) {
            stagingArea.save();
        }
    }

    /** Commit the file */
    public static void commit(String message, String secondParent) {
        if (stagingArea.isClean()) {
            MyUtils.exit("No changes added to the commit.");
        }

        Map<String, String> newTrackedMap = stagingArea.commit();
        // save the stagingArea since commit will change it
        stagingArea.save();

        List<String> parents = new ArrayList<>();
        Commit HEADCommit = getHEADCommit();

        parents.add(HEADCommit.getId());

        if (secondParent != null) {
            parents.add(secondParent);
        }

        Commit commit = new Commit(message, parents, newTrackedMap);
        commit.save();

        // set HEAD pointer
        setHEADPointer(commit.getId());

        // update branch  active branch name doesn't change!
        updateActiveBranch(commit);

    }

    /** remove the file from staging area */
    public static void remove(String fileName) {
        File file = MyUtils.getFileFromCWD(fileName);


        boolean changed = stagingArea.remove(file);
        if (changed) {
            stagingArea.save();
        } else {
            MyUtils.exit("No reason to remove the file.");
        }
    }

    /** print out the commit history following the first parent of the current branch*/
    public static void log() {
        Commit HEADCommit = getHEADCommit();
        Commit currCommit = HEADCommit;

        StringBuilder logBuilder = new StringBuilder();

        do {
            logBuilder.append(currCommit.getLog()).append("\n");

            List<String> parents = currCommit.getParents();
            if (parents.size() == 0) {
                break;
            }

            // update currCommit
            String nextCommitId = parents.get(0);
            Commit nextCommit = Commit.fromFile(nextCommitId);
            currCommit = nextCommit;
        } while (true);
        System.out.print(logBuilder);
    }


    /** print out all commits info without order*/
    public static void globalLog() {
        StringBuilder globalLogBuilder = new StringBuilder();

        List<String> allFileNames = Utils.plainFilenamesIn(Repository.COMMITS_DIR);
        for (String commitId : allFileNames) {
            Commit commit = Commit.fromFile(commitId);
            globalLogBuilder.append(commit.getLog()).append("\n");
        }
        System.out.print(globalLogBuilder);
    }

    /** Print all commits that have the exact message. */
    public static void find(String message) {
        StringBuilder commitsIdWithMessage = new StringBuilder();
        List<String> allFileNames = Utils.plainFilenamesIn(Repository.COMMITS_DIR);
        for (String commitId : allFileNames) {
            Commit commit = Commit.fromFile(commitId);
            if (commit.getMessage().equals(message)) {
                commitsIdWithMessage.append(commitId).append("\n");
            }
        }
        if (commitsIdWithMessage.isEmpty()) {
            MyUtils.exit("Found no commit with that message.");
        } else {
            System.out.print(commitsIdWithMessage);
        }
    }

    /** Displays what branches currently exist, and marks the current branch with a *.
     * Also displays what files have been staged for addition or removal.
     * */
    public static void status() {

        StringBuilder statusBuilder = new StringBuilder();

        // append branch status
        statusBuilder.append(getBranchStatus());
        statusBuilder.append("\n");

        // append staged files
        statusBuilder.append(getStagedStatus());
        statusBuilder.append("\n");

        // append removed files
        statusBuilder.append(getRemovedStatus());

        System.out.print(statusBuilder);
    }


    /** checkout file from HEAD commit */
    public static void checkout(String fileName) {
        String filePath = MyUtils.getFileFromCWD(fileName).getPath();
        if (!getHEADCommit().restoreTrackedFile(filePath)) {
            MyUtils.exit("File does not exist in that commit.");
        }
    }

    /** checkout file from given commit */
    public static void checkout(String commitId, String fileName) {
        String fullCommitId = getFullCommitId(commitId);

        String filePath = MyUtils.getFileFromCWD(fileName).getPath();
        if (!Commit.fromFile(fullCommitId).restoreTrackedFile(filePath)) {
            MyUtils.exit("File does not exist in that commit.");
        }
    }


    /**
     * Takes all files in the commit at the head of the given branch, and puts them in the working directory,
     * overwriting the versions of the files that are already there if they exist.
     * Also, at the end of this command, the given branch will now be considered the current branch (HEAD).
     * Any files that are tracked in the current branch but are not present in the checked-out branch are deleted.
     * The staging area is cleared, unless the checked-out branch is the current branch
     * */
    public static void checkoutBranch(String branchName) {

        File branchFile = Utils.join(BRANCHES_DIR, branchName);
        // case 1: branch doesn't exist
        if (!branchFile.exists()) {
            MyUtils.exit("No such branch exists.");
        }

        // case 2: that branch is the current branch
        if (branchName.equals(Branch.getActiveBranchName())) {
            MyUtils.exit("No need to checkout the current branch.");
        }

        // case 3: If a working file is untracked in the current branch and would be overwritten by the checkout
        //Branch currBranch = Branch.fromFile(Branch.getActiveBranchName());
        Branch checkoutBranch = Branch.fromFile(branchName);

        checkUntrackedFile(checkoutBranch.getHEADCommit());

        // case 4: put all given branch file to CWD


        // Any files that are tracked in the current branch but are not
        // present in the checked-out branch are deleted.
        deleteUntrackedFiles(checkoutBranch.getHEADCommit());

        // restore all files in checkout HEADCommit to CWD
        checkoutCommit(checkoutBranch.getHEADCommit());

        // update HEAD
        setHEADPointer(checkoutBranch.getPointerCommitId());

        // change active branch
        checkoutBranch.saveActiveBranch();
    }


    /** create a new Branch */
    public static void branch(String branchName) {
        // check if the branch already exists
        File branchFile = Utils.join(BRANCHES_DIR, branchName);
        if (branchFile.exists()) {
            MyUtils.exit("A branch with that name already exists.");
        }

        Branch branch = new Branch(getHEADCommit(), branchName);
        branch.save();
    }


    /** rmeove the branch */
    public static void removeBranch(String branchName) {
        File branchFile = Utils.join(BRANCHES_DIR, branchName);

        // branch doesn't exist
        if (!branchFile.exists()) {
            MyUtils.exit("A branch with that name does not exist.");
        }

        // cannot remove current branch
        if (branchName.equals(Branch.getActiveBranchName())) {
            MyUtils.exit("Cannot remove the current branch.");
        }

        branchFile.delete();
    }

    /** Reset to commit with the id. */
    public static void reset(String commitId) {
        commitId = getFullCommitId(commitId);
        Commit commit = Commit.fromFile(commitId);

        // check untracked files
        checkUntrackedFile(commit);


        // delete files that are tracked in curr but not tracked in checkout commit
        deleteUntrackedFiles(commit);

        // checkout the commit  restore files
        checkoutCommit(commit);

        // set branch
        setActiveBranch(commit);

        // set HEAD
        setHEADPointer(commitId);
    }


    /** create initial staging area */
    private static void createInitialStagingArea() {
        StagingArea initialStagingArea = new StagingArea();
        initialStagingArea.save();
    }

    /** Get the head commit's id */
    public static String getHeadCommitId() {
        return Utils.readContentsAsString(HEAD);
    }

    /** Get the headCommit */
    public static Commit getHEADCommit() {
        String HEADCommitId = getHeadCommitId();
        return Commit.fromFile(HEADCommitId);
    }

    /** get branch status string */
    private static String getBranchStatus() {
        StringBuilder branchbuilder = new StringBuilder();
        branchbuilder.append("=== Branches ===\n");

        List<String> allBranchNames = Utils.plainFilenamesIn(BRANCHES_DIR);

        assert allBranchNames != null;
        Collections.sort(allBranchNames);


        for (String branchName : allBranchNames) {
            Branch branch = Branch.fromFile(branchName);
            if (branch.getBranchName().equals(Branch.getActiveBranchName())) {
                // check if it is the current active branch
                branchbuilder.append("*").append(branchName).append("\n");
            } else {
                branchbuilder.append(branchName).append("\n");
            }
        }
        return branchbuilder.toString();
    }

    /** get added files status string */
    private static String getStagedStatus() {
        StringBuilder stagedStatusBuilder = new StringBuilder();
        stagedStatusBuilder.append("=== Staged Files ===\n");
        Collection<String> filePathsCollection = stagingArea.getAddMap().keySet();
        appendFileNamesInOrder(stagedStatusBuilder, filePathsCollection);
        return stagedStatusBuilder.toString();
    }

    /** get removed files string */
    private static String getRemovedStatus() {
        StringBuilder removedStatusBuilder = new StringBuilder();
        removedStatusBuilder.append("=== Removed Files ===\n");
        Collection<String> filePathsCollection = stagingArea.getRemoveSet();
        appendFileNamesInOrder(removedStatusBuilder, filePathsCollection);
        return removedStatusBuilder.toString();
    }

    /** Append lines of file name in order from files paths Set to StringBuilder. */
    private static void appendFileNamesInOrder(StringBuilder stringBuilder, Collection<String> filePathsCollection) {
        List<String> filePathsList = new ArrayList<>(filePathsCollection);
        List<String> fileNamesList = new ArrayList<>();

        for (String filePath : filePathsList) {
            String fileName = Paths.get(filePath).getFileName().toString();
            fileNamesList.add(fileName);
        }

        fileNamesList.sort(String::compareTo);

        for (String fileName : fileNamesList) {
            stringBuilder.append(fileName).append("\n");
        }

    }

    /** Given the abbreviated commitId, return the full Id
     * if commit id not found, exit()
     * */
    private static String getFullCommitId(String shortId) {
        String fullId = "";
        boolean isFound = false;

        for (String commitId : plainFilenamesIn(COMMITS_DIR)) {
            if (commitId.contains(shortId)) {
                fullId = commitId;
                isFound = true;
            }
        }

        if (!isFound) {
            MyUtils.exit("No commit with that id exists.");
        }
        return fullId;
    }

    /** get all current working directory files */
    private static List<File> getCWDFiles() {
        List<File> fileList = new ArrayList<>();
        for (String fileName : plainFilenamesIn(CWD)) {
            File file = Utils.join(CWD, fileName);
            fileList.add(file);
        }
        return fileList;
    }


    /** checkout a commit */
    private static void checkoutCommit(Commit checkoutCommit) {
        // clear and save the staging area
        stagingArea.clear();
        stagingArea.save();

        checkoutCommit.restoreAllTrackedFiles();
    }

    /** set HEAD */
    private static void setHEADPointer(String HEADCommitId) {
        Utils.writeContents(HEAD, HEADCommitId);
    }

    /** update active branch */
    private static void updateActiveBranch(Commit commit) {
        // set branch
        Branch activeBranch = new Branch(commit, Branch.getActiveBranchName());
        activeBranch.save();
        activeBranch.saveActiveBranch();
    }

    /** case 3: Check If a working file is untracked in the current commit
     * and would be overwritten by the checkout
     * */
    private static void checkUntrackedFile(Commit commit) {
        for (String fileName : plainFilenamesIn(CWD)) {
            File sourceFile = Utils.join(CWD, fileName);
            String filePath = sourceFile.getPath();
            Blob blob = new Blob(sourceFile);
            String blobId = blob.getId();

            Map<String, String> checkoutTracked = commit.getTracked();
            Map<String, String> currTracked = getHEADCommit().getTracked();

            boolean isInCurr = currTracked.containsKey(filePath);
            boolean isInCheckout = checkoutTracked.containsKey(filePath);

            // 1. cwd version is different from checkout version (same file name)
            if (!isInCurr && isInCheckout && (!blobId.equals(checkoutTracked.get(filePath)))) {
                MyUtils.exit("There is an untracked file in the way; delete it, or add and commit it first.");
            }

            // 2. isIncheckout isIncurrB curr!=cwd checkout!=cwd
            if (isInCheckout && isInCurr) {
                if (!blobId.equals(checkoutTracked.get(filePath)) && !blobId.equals(currTracked.get(filePath))) {
                    MyUtils.exit("There is an untracked file in the way; delete it, or add and commit it first.");
                }
            }
        }
    }

    /** delete files
     * Any files that are tracked in the current branch
     * but are not present in the checked-out commit are deleted.
     * */
    private static void deleteUntrackedFiles(Commit commit) {
        Map<String, String> currTracked = getHEADCommit().getTracked();
        Map<String, String> checkoutTracked = commit.getTracked();

        for (File cwdFile: getCWDFiles()) {
            boolean isInCurrBranch = currTracked.containsKey(cwdFile.getPath());
            boolean isInCheckoutBranch = checkoutTracked.containsKey(cwdFile.getPath());

            if (isInCurrBranch && !isInCheckoutBranch) {
                cwdFile.delete();
            }
        }
    }

    private static void setActiveBranch(Commit commit) {
        String activeBranchName = Branch.getActiveBranchName();
        Branch newBranch = new Branch(commit, activeBranchName);
        newBranch.save();
        newBranch.saveActiveBranch();
    }
}
