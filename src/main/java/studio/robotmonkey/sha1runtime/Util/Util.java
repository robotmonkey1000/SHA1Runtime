package studio.robotmonkey.sha1runtime.Util;

import studio.robotmonkey.sha1runtime.SHA1Runtime;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class Util {
    public static File GetOrCreateConfig() {
        File hashFile = new File("config/ResourcePackHash.txt");
        if(!hashFile.exists()) {
            SHA1Runtime.LOGGER.warn("Missing Hash File! Generating Now...");
            try {
                boolean created = hashFile.createNewFile();
                if(created) {
                    FileWriter writer = new FileWriter(hashFile);
                    writer.write("");
                    writer.close();
                    SHA1Runtime.LOGGER.info("Config file generated. Update with your new hash when needed.");
                } else {
                    SHA1Runtime.LOGGER.error("Could not create config file!");
                }

            } catch(IOException ioException)
            {
                SHA1Runtime.LOGGER.error(ioException.toString());
            }
        }
        return hashFile;
    }

    public static boolean IsOverrideSet()
    {
        File urlFile = new File("config/packurl.override");
        return urlFile.exists();
    }

    public static File GetOrCreateUrlOverride() {
        File urlFile = new File("config/packurl.override");
        if(!urlFile.exists()) {
            SHA1Runtime.LOGGER.warn("Resource pack URL override file not found, generating now.");
            try {
                boolean created = urlFile.createNewFile();
                if(created) {
                    FileWriter writer = new FileWriter(urlFile);
                    writer.write("");
                    writer.close();
                    SHA1Runtime.LOGGER.info("Config file generated. Update with your new url manually when needed or use the /setpackurl command.");
                } else {
                    SHA1Runtime.LOGGER.error("Could not create URL config file!");
                }

            } catch(IOException ioException)
            {
                SHA1Runtime.LOGGER.error(ioException.toString());
            }
        }
        return urlFile;
    }

    public static String GetURLFromConfig() {
        File urlFile = Util.GetOrCreateUrlOverride();
        try {
            Scanner fileReader = new Scanner(urlFile);
            if (fileReader.hasNextLine()) {
                String urlInFile = fileReader.nextLine();
                return urlInFile;
            } else {
                SHA1Runtime.LOGGER.warn("No URL in file: Please open config folder and add your url or run /setpackurl.");
            }
            fileReader.close();
        } catch (FileNotFoundException e) {
            SHA1Runtime.LOGGER.error("Missing Url File! Generating Now...");
            try {
                urlFile.createNewFile();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }

        return "";
    }

}
