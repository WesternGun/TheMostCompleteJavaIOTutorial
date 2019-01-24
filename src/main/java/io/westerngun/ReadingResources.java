package io.westerngun;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

public class ReadingResources {
    public static void main(String[] args) {
//        try {
//            InputStream in = ReadingResources.class.getResourceAsStream("/new.csv");
//            if (in != null) {
//                System.out.println(in.available());  // available() returns estimated number of characters in the file loaded
//                InputStreamReader reader = new InputStreamReader(in, Charset.forName("UTF-8"));
//                int i;
//                while ((i = reader.read()) != -1) {
//                    char letter = (char)i;
//                    System.out.print(letter);
//                }
//                reader.close();
//                in.close();
//            } else {
//                System.out.println("Not available");
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        // try-with-resources style
        // try (statement to open a stream) {
        //     ...
        // } catch (Exception e) {
        //     ...
        // }
        // catch part is necessary, for IOException.
        // no explicit close is needed, try-with will handle it
        // if we have opened a FileInputStream outside try-with-resources,
        // and try with a inputstream, when the inputstream is closed,
        // the FileInputStream will be closed, too.
        // very convenient.
        try (InputStream in = ReadingResources.class.getResourceAsStream("/new.csv")){
            if (in != null) {
                System.out.println(in.available());  // available() returns estimated number of characters in the file loaded
                InputStreamReader reader = new InputStreamReader(in, Charset.forName("UTF-8"));
                int i;
                while ((i = reader.read()) != -1) {
                    char letter = (char)i;
                    System.out.print(letter);
                }
            } else {
                System.out.println("Not available");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
