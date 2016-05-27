import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by vedeshkin on 27.05.2016.
 */
public class converter {
    private Path file;
    private Thread thread;
    private List<String> buffer;
    public converter(Path file){
        this.file = file;
        buffer = new LinkedList<String>();
    }

    private String[] splitline(String line)
    {
        return line.split(",");
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
            buffer.set(i,parse(buffer.get(i)));
        }
    }
    private String parse(String line) {

        String[] values = splitline(line);
        if (values[4].isEmpty() || values[5].isEmpty() || values[6].isEmpty() || values[7].isEmpty()){
            System.out.println("Price not found!");
        return null;
    }
        try {
            String date = FixTool.outputFullDateFormat.format(FixTool.inputFullDateFormat.parse(values[1] + "-" + values[2]));
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }


        return  null;
    }
}
