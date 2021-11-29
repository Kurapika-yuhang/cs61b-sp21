package gitlet;

import java.io.File;

public class TestCWD {
    public static void main(String[] args) {
        File CWD = new File(System.getProperty("user.dir"));
        System.out.println(CWD.getPath());
    }
}
