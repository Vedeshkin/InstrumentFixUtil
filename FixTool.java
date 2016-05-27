

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
    private static boolean parsingMode = false;
    private static String[] suffixes = {"X", "Y", "Z", ""};
    private static Map<String,String> symbolMapping;
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
        }catch (IOException ex)
        {System.out.println("Cound't open config file:"+args[0]);}

        symbolMapping = getRicToDxSymbolMapping(config.getProperty("mappingFile"));
        parsingMode = config.getProperty("parsingMode").equals("Day")?false:true;
        List<Path> filelist = fileList.getFiles(config.getProperty("inputDir"),config.getProperty("pattern"));

        for (Path file: filelist) {

            /* TODO: 21.05.2016
            *Here we need to to some hard-coding job
            * Main idea is to use one thread per file.
            * That impromvent give to us GREAT boots in parsing files,becouse we will be able to parse
            * many file simultaniosly.
            *
             */
            BufferedReader br = Files.newBufferedReader(file);
            PrintWriter[] writers = new PrintWriter[suffixes.length];
            for (int i = 0; i < writers.length; i++) {
                //There should be another way of file creation...
                //Also i'm not sure if constuction below will work...But who care?
                //At least we have usable file-getting user-frienly methods.
                Path p = Paths.get(config.getProperty("outputDir")).resolve(file.getFileName().toString()+"converted.csv");
                File f = p.toFile();
                if (f.createNewFile()) {
                    writers[i] = new PrintWriter(f);
                } else {
                    System.out.println("Unable to create file " + f.getName());
                }
            }
            String line;
            int start = 0;

            while ((line = br.readLine()) != null) {
                if (start != 0) {
                    for (int i = 0; i < suffixes.length; i++) {
                        writers[i].println(parsingMode ? convertMin(line, symbolMapping, suffixes[i]):convert(line, symbolMapping, suffixes[i]));
                    }

                }

                start++;
            }
            for (int i = 0; i < suffixes.length; i++) {
                if (writers[i] != null) {
                    writers[i].close();
                }
            }
            br.close();
        }

    }


    private static String convert(String trthDayString, Map<String, String> symbolMapping, String symbolSuffix) {
        String ampSymbolSuffix = symbolSuffix.isEmpty() ? "" : "&" + symbolSuffix;
        String[] parts = trthDayString.split(",", -1);
        String symbol = (String)symbolMapping.get(parts[0]) + ampSymbolSuffix;
        String date = parts[1];
        String time = "19:00:00.000";
        String open = parts[4];
        String high = parts[5];
        String low = parts[6];
        String close = parts[7].isEmpty()?parts[4]:parts[7];
        String volume = parts[8];
        String vwap;
        if (parts.length < 11) {
            vwap = "0";
        } else {
            vwap = parts[10];
        }

        Date datetime = null;
        try {
            datetime = inputFullDateFormat.parse(date + "-" + time);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        String outDate = outputFullDateFormat.format(datetime);
        return symbol + "{price=bid}," + outDate + "," + open + "," + high + "," + low + "," + close + "," + volume + "," + vwap;
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

    private static String convertMin(String trthMinuteString, Map<String, String> symbolMapping, String symbolSuffix) {
        String ampSymbolSuffix = symbolSuffix.isEmpty()?"":"&" + symbolSuffix;
        String[] parts = trthMinuteString.split(",", -1);
        String symbol = (String)symbolMapping.get(parts[0])+ ampSymbolSuffix;
        //#RIC,Date[G],Time[G],GMT Offset,Type,Open,High,Low,Last,Volume,VWAP
        //.STOXX50,20150102,08:00:00.000,+1,Intraday 1Min,3017.3,3020.56,3017.3,3020.14,2791551,
        String date = parts[1];
        String time = parts[2];
        String open = parts[5];
        String high = parts[6];
        String close = parts[8].isEmpty() ? open : parts[8];
        String low = parts[7].isEmpty() ? String.valueOf(Math.min(Double.parseDouble(open),Double.parseDouble(close))): parts[7];
        String volume = parts[9];
        String vwap = parts[10];
        if(vwap.isEmpty()) {
            vwap = "0";
        }

        Date datetime = null;
        try {
            datetime = inputFullDateFormat.parse(date + "-" + time);
        } catch (ParseException ex) {
           System.out.println(ex.getMessage());
        }

        String outDate = outputFullDateFormat.format(datetime);
        return symbol + "{price=bid}," + outDate + "," + open + "," + high + "," + low + "," + close + "," + volume + "," + vwap;
    }

    }
