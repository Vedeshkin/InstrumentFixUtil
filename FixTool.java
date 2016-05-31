

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * Created by vedeshkin on 20.12.2015.
 */
public class FixTool {
    public static final SimpleDateFormat inputFullDateFormat = new SimpleDateFormat("yyyyMMdd-HH:mm:ss.SSS");
    public static final SimpleDateFormat outputFullDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static Map<String, String> symbolMapping;
    public FixTool() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1)
        {
            System.out.println("Config file is not specified!Unable to start utility");
            System.out.println("Usage:FixTool <path_to_config>");
            return;
        }
        Properties config = new Properties();
        try {
            config.load(Files.newBufferedReader(Paths.get(args[0])));
        }catch (IOException ex) {
            System.out.println("Cound't open config file:" + args[0]);
            return;
        }

        symbolMapping = getRicToDxSymbolMapping(config.getProperty("mappingFile"));
        List<Path> filelist = fileList.getFiles(config.getProperty("inputDir"),config.getProperty("pattern"));

        for (Path file: filelist) {
            Path p = Paths.get(config.getProperty("outputDir")).resolve(file.getFileName().toString() + "converted.csv");
            new converter(file, p);
        }

    }

    private static Map<String, String> getRicToDxSymbolMapping(String mappingFile)   {
        HashMap mapping = new HashMap();
        List mappingStrings = null;
        try {
             mappingStrings = Files.readAllLines(Paths.get(mappingFile), Charset.defaultCharset());
        }catch (IOException ex)
        {
            System.out.println("Unable to open mappingfile:"+mappingFile);
            System.out.println("Please check configuration file,make sure that <mappingFile> property contain correct path!");
        }
        String[] parts;
        for (Iterator iter = mappingStrings.iterator(); iter.hasNext(); mapping.put(parts[0], parts[1])) {
            String pair = (String) iter.next();
            parts = pair.split(";");
            if (parts.length != 2 || parts[1].isEmpty()) {
                System.out.println("Unable to create mapping!");
            }
        }

        return mapping;
    }
}
