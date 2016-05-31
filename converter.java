import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by vedeshkin on 27.05.2016.
 */
public class converter implements Runnable {
    private Path file;
    private Thread thread;
    private List<String> buffer;
    private PrintWriter pw;

    public converter(Path fileIn, Path fileOut) {
        this.file = fileIn;
        buffer = new LinkedList<String>();
        try {
            pw = new PrintWriter(fileOut.toFile());
        } catch (FileNotFoundException ex) {
            System.out.println("FixTool is unable to create file!");
        }
        Thread thread = new Thread(this);
        thread.start();
    }



    private  void readFile(Path filename){
        try{
        buffer = Files.readAllLines(filename);}
        catch (IOException ex)
        { System.out.println(ex.getMessage());}
    }
    private void convert()
    {

        //skip first line in file;
        for(int i =1;i<buffer.size();i++)
        {
            String s = buffer.get(i);
            try {
                s = parse(s);
                pw.println(s);
            } catch (ParseException ex) {
                System.out.println("Error occured while parsing line:" + i);
                System.out.println(buffer.get(i));
                System.out.println(ex.getMessage());
            }
        }
        pw.close();
    }

    private String parse(String line) throws ParseException {

        String[] values = line.split(",");//Should be added to config?
        if (values.length < 10) throw new ParseException("Argument lenght is too small!", 0);
        if (values[fields.SYMBOL.getField()].isEmpty())
            throw new ParseException("Symbol fields is empty", fields.SYMBOL.getField());

        if (values[fields.OPENPRICE.getField()].isEmpty()
                || values[fields.HIGHPRICE.getField()].isEmpty()
                || values[fields.LOWPRICE.getField()].isEmpty()
                || values[fields.CLOSEPRICE.getField()].isEmpty()) {
            throw new ParseException("Price is missing!!!", 0);
        }
        String date = null;
        try {
            date = FixTool.outputFullDateFormat.format
                    (FixTool.inputFullDateFormat.parse(values[fields.DATE.getField()] + "-" + values[fields.TIME.getField()]));
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        StringBuffer result = new StringBuffer();
        result.append(FixTool.symbolMapping.get(values[fields.SYMBOL.getField()]));
        result.append("{price=bid},");
        result.append(date);
        result.append(",");
        result.append(values[fields.OPENPRICE.getField()]);
        result.append(",");
        result.append(fields.HIGHPRICE.getField());
        result.append(",");
        result.append(fields.LOWPRICE.getField());
        result.append(",");
        result.append(fields.CLOSEPRICE.getField());
        result.append(",");
        result.append(fields.VOLUME.getField());
        result.append(",");
        result.append(0);//VWAP?

        return result.toString();
    }

    @Override
    public void run() {
        // System.out.println("Thread :"+thread.getName());
        readFile(file);
        //  System.out.println("Thread :"+thread.getName()+"Is done reading file!");
        convert();
        // System.out.println("Thread :"+thread.getName()+"Is done converting!");

    }
}
