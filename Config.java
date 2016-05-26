import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Created by Valentin on 21.05.2016.
 * Testing Properties Class
 * Just for internal purpose!
 */
public class Config {
    private Properties config;
    private String configFile;

    public Config(String configFile){

        this.configFile = configFile;


    }
    public boolean loadConfig()
    {
        config = new Properties();
        if(configFile.isEmpty())
        {
            System.out.println("Config file is not specified!Unable to start utility");
            System.out.println("Usage:FixTool <path_to_config>");
            return false;
        }
        try {
            config.load(Files.newBufferedReader(Paths.get(configFile)));
        } catch (IOException ex) {
            System.out.println("Cound't open config file:" + configFile);
            return false;
        }
        return  true;
    }


    public String getProperty(String key) {
       return config.getProperty(key);
    }
}
