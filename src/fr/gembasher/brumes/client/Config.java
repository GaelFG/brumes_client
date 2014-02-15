/*
 * This class handle configuration file
 */
package fr.gembasher.brumes.client;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public class Config {
    public static String read_property(String key) {
        Properties prop = new Properties();
	InputStream input = null;
 
	try {
		input = new FileInputStream("config.properties");
		prop.load(input);
	} catch (IOException ex) {
		ex.printStackTrace();
	} finally {
		if (input != null) {
			try {
				input.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
        return prop.getProperty(key);
    }
    
    public static void write_property(String key, String value) {
        Properties prop = new Properties();
	OutputStream output = null;
 
	try {
		output = new FileOutputStream("config.properties");
		// set the properties value
		prop.setProperty(key, value);
		//prop.setProperty("dbuser", "mkyong");
		//prop.setProperty("dbpassword", "password");
		prop.store(output, null);
	} catch (IOException io) {
		io.printStackTrace();
	} finally {
		if (output != null) {
			try {
				output.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
    }
}
