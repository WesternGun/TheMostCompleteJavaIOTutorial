package io.westerngun;

import java.io.File;

/**
 * Read file with a relative path without leading "/".
 * This will not work when it is a JAR and the folder is under some folder in the jar,
 * like "foo.jar:files/new.csv".
 * If the file is externalized(same level as the JAR), it works. So the relative path
 * here cannot be relied on.
 */
public class ReadingFiles {
    public static void main(String[] args) {
        File file = new File("files/new.csv");
        System.out.println(file.exists());
    }
}
