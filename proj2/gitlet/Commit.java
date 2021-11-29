package gitlet;

// TODO: any imports you need here

import javax.swing.*;
import java.io.File;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private final String message;

    /** The date for this commit */
    private final Date date;

    /** The parent commit */
    private final List<String> parents;

    /** The Commit's sha-1 id.  Cannot be get from file! Since you need the id before you store it!*/
    private final String id;

    /** something with the files I track */
    // TODO: Not decided yet

    /** The files that I tracked  key: filepath val: sha-1 id */
    private final Map<String, String> tracked;

    /** The commit object file of this instance with the path generated from sha-1 id */
    private final File file;

    /* TODO: fill in the rest of this class. */

    /** constructor */
    public Commit(String message, List<String> parents, Map<String, String> tracked) {
        this.message = message;
        this.parents = parents;
        this.tracked = tracked;

        this.date = new Date();
        this.id = generateId();
        this.file = Utils.join(Repository.COMMITS_DIR, this.id);
    }

    /** Initial Commit */
    public Commit() {
        this.message = "initial commit";
        this.date = new Date(0); // The timestamp for this initial commit will be 00:00:00 UTC, Thursday, 1 January 1970
        this.parents = new ArrayList<>();
        this.tracked = new HashMap<>();

        this.id = generateId();
        this.file = Utils.join(Repository.COMMITS_DIR, this.id);
    }


    /** Save the commit object to /.gitlet/objects/xx/xxxxx */
    public void save() {
        Utils.writeObject(file, this);
    }

    /** get the commit object from sha1 id */
    public static Commit fromFile(String id) {
        File commitFile = Utils.join(Repository.COMMITS_DIR, id);
        return Utils.readObject(commitFile, Commit.class);
    }

    /** Generate a sha-1 id for the Commit object */
    private String generateId() {
        // sha1 : parameters must be String or byte[]
        return Utils.sha1(this.message, getTimestamp(), this.parents.toString(), this.tracked.toString());
    }


    /**
     * Get the timestamp.
     *
     * @return Date and time
     */
    public String getTimestamp() {
        // Thu Jan 1 00:00:00 1970 +0000
        DateFormat dateFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z", Locale.ENGLISH);
        return dateFormat.format(date);
    }

    /** get message */
    public String getMessage() {
        return this.message;
    }

    /** get sha1 id */
    public String getId() {
        return this.id;
    }

    /** get tracked files */
    public Map<String, String> getTracked() {
        return this.tracked;
    }

    /** get list of parents sha1 id */
    public List<String> getParents() {
        return this.parents;
    }

    /** get the log message of the commit */
    public String getLog() {
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("===").append("\n");
        logBuilder.append("commit").append(" ").append(id).append("\n");
        if (parents.size() > 1) {
            logBuilder.append("Merge:");
            for (String parent : parents) {
                logBuilder.append(" ").append(parent, 0, 7);
            }
            logBuilder.append("\n");
        }
        logBuilder.append("Date:").append(" ").append(getTimestamp()).append("\n");
        logBuilder.append(message).append("\n");
        return logBuilder.toString();
    }


    /** restore tracked file return true if the file exists in this commit */
    public boolean restoreTrackedFile(String filePath) {
        String blobId = tracked.get(filePath);

        if (blobId == null) {
            return false;
        }

        Blob.fromFile(blobId).writeContentToSource();
        return true;
    }

    /** restore all tracked files  overwrite the existing ones */
    public void restoreAllTrackedFiles() {
        for (String blobId : tracked.values()) {
            Blob.fromFile(blobId).writeContentToSource();
        }
    }

}
