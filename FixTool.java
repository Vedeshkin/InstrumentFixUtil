

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * Created by vedeshkin on 20.12.2015.
 */
public class FixTool {
    private static final SimpleDateFormat inputFullDateFormat = new SimpleDateFormat("yyyyMMdd-HH:mm:ss.SSS");
    private static final SimpleDateFormat outputFullDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static boolean parsingMode = false;
    private static String path_to_charts_in_csv ;
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
        config.list(System.out);

        symbolMapping = getRicToDxSymbolMapping(config.getProperty("mappingFile"));
        path_to_charts_in_csv = config.getProperty("inputDir");
        parsingMode = config.getProperty("parsingMode").equals("Day")?false:true;



        List<String> filelist = listFilesForFolder(new File(path_to_charts_in_csv));
        /* TODO: 21.05.2016
        * There is should be another way to get all file to parse!
        * Also we need add to config some  kind of file mask to determind what files in dir
        * should be added to file list.This improvment help us to exclude all redudant files from parsing
        *Also it's good idea to contain in fileList FULL path to files.
        */
        for (String filename: filelist) {
            //String s = getSymbol(path_to_charts_in_csv+"\\"+ filename);

            /* TODO: 21.05.2016
            *Here we need to to some hard-coding job
            * Main idea is to use one thread per file.
            * That impromvent give to us GREAT boots in parsing files,becouse we will be able to parse
            * many file simultaniosly.
            *
             */


            BufferedReader br;
            br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(path_to_charts_in_csv+"\\" + filename))));
            PrintWriter[] writers = new PrintWriter[suffixes.length];
            for (int i = 0; i < writers.length; i++) {
                String fileout = path_to_charts_in_csv +"\\"+ filename.substring(0,filename.length() -4) + "&" + suffixes[i]+"_correct" + (parsingMode ? "_minute" :"_day")+".csv";
                File f = new File(fileout);
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
    private static String getSymbol(String filename)
    {String line = "";
        try {
        line = Files.readAllLines(Paths.get(filename)).get(2);
        line = line.substring(0,line.indexOf(","));
    }catch (IOException fileNotFound)
    {System.out.println(filename + "is not found");}
        return symbolMapping.get(line);
    }
    private static List<String> listFilesForFolder (final File folder) {
        List<String> filenames = new LinkedList<String>();
        for (final File fileEntry : folder.listFiles()) {
            if(fileEntry.getName().contains(".csv")){
                filenames.add(fileEntry.getName());
            }
        }
        return filenames;
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
