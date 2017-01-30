package gui.jolanda;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Main {

    public static void main(String[] args) {

        EventQueue.invokeLater(() -> {
            Properties prop = new Properties();
            String propFileName = "jolanda.properties";
            InputStream inputStream = Window.class.getClassLoader().getResourceAsStream(propFileName);

            if (inputStream != null) {
                try {
                    prop.load(inputStream);
                } catch (IOException ex){
                    throw new RuntimeException("Cannot load property from input stream.");
                }
            } else {
                throw new RuntimeException("property file '" + propFileName + "' not found in the classpath");
            }
            Window frame = new Window(prop);
            frame.setVisible(true);
        });
    }
}
