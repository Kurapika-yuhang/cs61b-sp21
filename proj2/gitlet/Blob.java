package gitlet;


import java.io.File;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;

/**
 *
 * represent the file object
 *
 */

public class Blob implements Serializable {
    // Blob's Sha-1 ID and contents will never change

    /** The source file of the Blob, given by the constructor */
    private final File source;

    /** content of this blob */
    private final byte[] content;

    /** sha-1 ID it's only related to the src file and the contents, doesn't relate to where it's stored! */
    private final String id;

    /** The blob file stored in .gitlet   这个file应该放在哪里*/
    private final File file;

    /** Constructor of Blob */
    public Blob(File source) {
        this.source = source;
        this.content = Utils.readContents(source);
        String filePath = source.getPath();
        this.id = Utils.sha1(filePath, this.content);
        this.file = MyUtils.getObjectFile(this.id);
    }


    /** save the blob */
    public void save() {
        MyUtils.saveObjectFile(file, this);
    }


    /** Get the blob object from sha-1 id*/
    public static Blob fromFile(String id) {
        return Utils.readObject(MyUtils.getObjectFile(id), Blob.class);
    }

    /** given file, generate sha-1 */
    // use MyUtils.generateId

    /** Get sourceFile */
    public File getSourceFile() {
        return source;
    }

    /** Get Blob File in /.gitlet/objects */
    public File getFile() {
        return file;
    }

    /** Get sha-1 id of this blob */
    public String getId() {
        return id;
    }

    /** Get String content of this blob */
    public String getContentAsString() {
        return new String(content, StandardCharsets.UTF_8);
    }

    /** Write contents to the source file */
    public void writeContentToSource() {
        Utils.writeContents(source, content);
    }
}
