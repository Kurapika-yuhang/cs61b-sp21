package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;

public class StagingArea implements Serializable {
    /** staging area: only one instance? */


    /** added files, key: file path  value: sha-1 id */
    private HashMap<String, String> addMap;

    /** removed files file path  */
    private HashSet<String> removeSet;

    /** already tracked files key: file path  value: sha-1 id*/
    private HashMap<String, String> trackedMap;


    // private File file;  no need, since only one staging area.  File: Repository.STAGING_AREA

    /** Constructor of StagingArea */
    public StagingArea() {
        addMap = new HashMap<>();
        removeSet = new HashSet<>();
        trackedMap = new HashMap<>();
    }

    /** set tracked files */
    public void setTrackedMap(HashMap<String, String> fileMaps) {
        trackedMap = fileMaps;
    }

    /** Tells whether the staging area is clean, means no files are added or modified or removed */
    public boolean isClean() {
        return addMap.isEmpty() && removeSet.isEmpty();
    }

    /** clear the staging area */
    public void clear() {
        addMap.clear();
        removeSet.clear();
    }


    /** Add the File file to staging area, return true if the staging area changed*/
    public boolean add(File file) {
        // create the blob
        Blob blob = new Blob(file);
        String blobId = blob.getId();

        String filePath = file.getPath();

        // check if it's already been tracked
        String trackedId = trackedMap.get(filePath);

        if (trackedId != null) {
            // this file has been tracked now
            if (trackedId.equals(blobId)) {
                // the file is the same as the tracked one now
                // the file could be in addMap or removeSet, only one is possible
                if (addMap.remove(filePath) != null) {
                    // the file was in addMap, now it's removed
                    return true;
                } else {
                    return removeSet.remove(filePath);
                }
            }
        }

        // the file is not tracked now
        String prevId = addMap.put(filePath, blobId);
        if (prevId != null && prevId.equals(blobId)) {
            // added the same blob before
            return false;
        }


        if (!blob.getFile().exists()) {
            // redundant ?
            blob.save();
        }

        removeSet.remove(filePath); // need or not?
        return true;
    }

    /** remove the file, return true if staging area is changed */
    public boolean remove(File file) {
        /**
         * What case return false?
         * 1. not tracked, not added
         * 2. tracked,  removed before
         *
         * TO DO: remove.add()  add.remove
         * */
        String filePath = file.getPath();

        String trackedId = trackedMap.get(filePath);

        String addedId = addMap.remove(filePath);

        if (trackedId != null) {
            // tracked
            return removeSet.add(filePath);
        } else {
            // not tracked
            if (addedId == null) {
                // not added
                // case 1, not need to change removeSet
                return false;
            } else {
                removeSet.add(filePath);
            }
        }
        return true;
    }

    /** commit the staging area. Return the trackedMap */
    public HashMap<String, String> commit() {
        trackedMap.putAll(addMap);
        for (String filePath : removeSet) {
            trackedMap.remove(filePath);
        }
        clear();
        return trackedMap;  // Why return the trackedMap?  Because when we commit, we need a new trackedMap
    }


    /** Get the StagingArea object from .gitlet/ */
    public static StagingArea fromFile() {
        return Utils.readObject(Repository.STAGING_AREA, StagingArea.class);
    }

    /** save the staging area file */
    public void save() {
        Utils.writeObject(Repository.STAGING_AREA, this);
    }

    /** Get the added files map */
    public HashMap<String, String> getAddMap() {
        return addMap;
    }

    /** Get the added files map */
    public HashSet<String> getRemoveSet() {
        return removeSet;
    }

    /** Get the added files map */
    public HashMap<String, String> getTrackedMap() {
        return trackedMap;
    }
}
