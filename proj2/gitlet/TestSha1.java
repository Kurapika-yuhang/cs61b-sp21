package gitlet;

import java.io.File;
import java.io.Serializable;

public class TestSha1 implements Serializable {
    public static void main(String[] args) {
        File CWD = new File(System.getProperty("user.dir"));
        File self = Utils.join(CWD, "gitlet", "TestSha1.java");
        // System.out.println(self.getAbsolutePath());
        //System.out.println(self.isFile());
        byte[] contents = Utils.readContents(self);
        String sha1 = MyUtils.generateId(self);

        Blob blob = new Blob(self);
        Blob blob1 = new Blob(self);
        blob.save();
        blob1.save();   // saveª·÷±Ω”∏≤∏«
    }
}
