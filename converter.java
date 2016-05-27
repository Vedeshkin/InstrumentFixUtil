import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

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

        }
    }
    private String parse(String line){


        return  null;
    }
}
