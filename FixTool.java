ackage FixTool;

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
class FixTool {
    private static final SimpleDateFormat inputFullDateFormat = new SimpleDateFormat("yyyyMMdd-HH:mm:ss.SSS");
    private static final SimpleDateFormat outputFullDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static boolean MINUTES = false;
    private static  String path_to_mapping_file;
    private static String path_to_charts_in_csv ;
    private static String[] suffixes = {"X", "Y", "Z", ""};
    public FixTool() {
    }

    public static void main(String[] args) throws Exception {
        if(args.length < 3) {System.out.println("Illegal usages!Call Vedeshkin for details xD");return;}

        Map symbolMapping = getRicToDxSymbolMapping(args[0]);
        path_to_charts_in_csv = args[1];
        MINUTES = args[3].equals("min")? true : false;
        List<String> filelist = listFilesForFolder(new File(path_to_charts_in_csv));
        for (String filename: filelist) {
            BufferedReader br;
            br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(path_to_charts_in_csv+filename))));
            PrintWriter[] writers = new PrintWriter[suffixes.length];
            for (int i = 0; i < writers.length; i++) {
                File f = new File(path_to_charts_in_csv + filename + "&" + suffixes[i] + "_day.csv");
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
                        writers[i].println(MINUTES ? convertMin(line, symbolMapping, suffixes[i]):convert(line, symbolMapping, suffixes[i]));
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
        String symbol = (String)symbolMapping.get(parts[0])+"mini" + ampSymbolSuffix;
        String date = parts[1];
        String time = "19:00:00.000";
        String open = parts[4];
        String high = parts[5];
        String low = parts[6];
        String close = parts[7];
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

    private static Map<String, String> getRicToDxSymbolMapping(String mappingFile) throws IOException {
        HashMap mapping = new HashMap();
        List mappingStrings = Files.readAllLines(Paths.get(mappingFile), Charset.defaultCharset());

        String[] parts;
        for (Iterator iter = mappingStrings.iterator(); iter.hasNext(); mapping.put(parts[0], parts[1])) {
            String pair = (String) iter.next();
            parts = pair.split(";");
            if (parts.length != 2 || parts[1].isEmpty()) {
                System.out.println("Shit happens");
            }
        }

        return mapping;
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
        String symbol = (String)symbolMapping.get(parts[0])+"mini" + ampSymbolSuffix;
        //#RIC,Date[G],Time[G],GMT Offset,Type,Open,High,Low,Last,Volume,VWAP
        //.STOXX50,20150102,08:00:00.000,+1,Intraday 1Min,3017.3,3020.56,3017.3,3020.14,2791551,
        String date = parts[1];
        String time = parts[2];
        String open = parts[5];
        String high = parts[6];
        String low = parts[7];
        String close = parts[8];
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
