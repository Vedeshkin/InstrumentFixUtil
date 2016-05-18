import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * Created by vedeshkin on 20.12.2015.
 */
public class Test {
    private static final SimpleDateFormat inputFullDateFormat = new SimpleDateFormat("yyyyMMdd-HH:mm:ss.SSS");
    private static final SimpleDateFormat outputFullDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public Test() {
    }

    public static void main(String[] args) throws Exception {
        Map symbolMapping = getRicToDxSymbolMapping("C:\\temp\\charts\\Intsrument\\quota_limit_symbols.conf");
        String filepath = "C:\\temp\\charts\\Intsrument\\";

        String[] suffixes = {"X", "Y", "Z", ""};
        List<String> filelist = listFilesForFolder(new File(filepath));
        for (String filename: filelist) {
            BufferedReader br;
            br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filepath+filename))));
            PrintWriter[] writers = new PrintWriter[suffixes.length];
            for (int i = 0; i < writers.length; i++) {
                File f = new File(filepath + filename + "&" + suffixes[i] + "_day.csv");
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
                        writers[i].println(convert(line, symbolMapping, suffixes[i]));
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

    }