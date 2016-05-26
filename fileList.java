import java.io.IOException;
import java.nio.file.*;
import java.util.LinkedList;


/**
 * Created by Valentin on 24.05.2016.
 */
public class fileList {
    private String pattern;
    private String path;
    private LinkedList<Path> fileList;
    public fileList(String path,String pattern){
        this.path =path;
        this.pattern = pattern;
        fileList = new LinkedList<Path>();
    }

    public LinkedList  getFiles(){

        if(Files.notExists(Paths.get(path))){
            System.out.printf("Path %s is no exist!",path);
            return null;}

        try(DirectoryStream<Path> pathStream = Files.newDirectoryStream(Paths.get(path),pattern)){
            for (Path file: pathStream) fileList.add(file);
        }catch (IOException | DirectoryIteratorException ex){
            System.out.println(ex.getMessage());
        }
        return fileList;
        }

    }







