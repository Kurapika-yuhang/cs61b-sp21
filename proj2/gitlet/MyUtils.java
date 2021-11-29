package gitlet;


import java.io.File;
import java.io.Serializable;
import java.nio.file.Paths;


/**
 *
 * My own util functions
 *
 */
public class MyUtils {

    /**
     * Get a File instance with the path generated from SHA1 id in the objects folder.
     *
     * @param id SHA1 id
     * @return File instance
     */
    public static File getObjectFile(String id) {
        String dirName = id.substring(0, 2);    // id first 2 digits as  dir name
        String fileName = id.substring(2);
        return Utils.join(Repository.OBJECTS_DIR, dirName, fileName);
    }

    /** Given sha-1 id, return the staging area file */
    public static File getStaingFile(String id) {
        String dirName = id.substring(0, 2);    // id first 2 digits as  dir name
        String fileName = id.substring(2);
        return Utils.join(Repository.STAGING_AREA, dirName, fileName);
    }


    /** if file's parent dirs don't exit, create parent dirs. Then writeObject(file, object) */
    public static void saveObjectFile(File file, Serializable object) {
        File parent = file.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }
        Utils.writeObject(file, object);
    }

    /** Generate sha-1 ID for a single file */
    public static String generateId(File file) {
        String filePath = file.getPath();
        byte[] contents = Utils.readContents(file);
        return Utils.sha1(filePath, contents);
    }

    /** Given file name, return the full File Object */
    public static File getFileFromCWD(String name) {
        if (Paths.get(name).isAbsolute()) {
            return new File(name);
        } else {
            return Utils.join(Repository.CWD, name);
        }
    }


    /**
     * Print a message and exit with status code 0.
     *
     * @param message String to print
     * @param //args    Arguments referenced by the format specifiers in the format string
     */
    public static void exit(String message) {
        System.out.println(message);
        System.exit(0);
    }
}
