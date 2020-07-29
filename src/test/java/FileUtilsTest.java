import gr.dcu.utils.FileUtilities;

import java.io.File;
import java.io.IOException;

public class FileUtilsTest {

    public static void main(String[] args) {

        File dir = new File("/home/vangelis/tests/mint_test");

        if(dir.exists() && dir.isDirectory()) {
            try {
                FileUtilities.listAllFiles(dir);
            } catch(IOException ex) {
                System.err.println(ex.getMessage());
            }
        }
    }
}
