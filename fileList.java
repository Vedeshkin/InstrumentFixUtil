import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;
import java.util.LinkedList;


/**
 * Created by Valentin on 24.05.2016.
 */
public  class fileList {
    private fileList(){}

    public static LinkedList  getFiles(String path,String pattern) throws FileNotFoundException{


        if(Files.notExists(Paths.get(path))){
            System.out.printf("Path %s is no exist!",path);
            throw new FileNotFoundException("File not fount");
        }

        LinkedList<Path> fileList = new LinkedList<>();
        try(DirectoryStream<Path> pathStream = Files.newDirectoryStream(Paths.get(path),pattern)){
            for (Path file: pathStream) fileList.add(file);
        }catch (IOException | DirectoryIteratorException ex){
            System.out.println(ex.getMessage());
        }
        return fileList;
        }

    }







