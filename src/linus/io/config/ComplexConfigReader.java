package linus.io.config;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

/**
 *
 *This class can read {@link Config}s and interprete them into the right Form.
 *ConfigClasses readed by this class MUST be default constructable because of the use
 *of reflections.
 *
 * @author Linus Dierheimer
 * @version 1.0
 * @since java 1.5
 * @see ComplexConfigReader
 * @see ConfigFile
 *
 */
public class ComplexConfigReader extends ConfigReader{

	private final Scanner reader;
	private Config<?> buffer;

	private InputStream source;

	/**
	 * Creates a new ConfigReader of the abstract given pathName
	 * @param pathName
	 * @throws FileNotFoundException if the File doesn't exists
	 */
	public ComplexConfigReader(String pathName) throws FileNotFoundException {
		this(new File(pathName));
	}

	/**
	 * Creates a new ConfigReader of the given File
	 * @param f
	 * @throws FileNotFoundException if the File doesn't exists
	 */
	public ComplexConfigReader(File f) throws FileNotFoundException{
		this(new FileInputStream(f));
	}

	/**
	 * Creates a new ConfigReader to the given FileDescritor
	 * @param fd
	 */
	public ComplexConfigReader(FileDescriptor fd) {
		this(new FileInputStream(fd));
	}

	/**
	 * Creates a new ConfigReader of the given inputStream
	 * @param in
	 */
	public ComplexConfigReader(InputStream in) {
		super(in);
		reader = new Scanner(in);
		try {
			buffer = nextConfig();
		} catch (ReaderFinishedException e) {
			buffer = null;
		} catch (ReflectiveOperationException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E extends ConfigBase> E next(){
		E next = (E) buffer;
		try {
			buffer = nextConfig();
		} catch (ReaderFinishedException e) {
			buffer = null;
		} catch (ReflectiveOperationException e) {
			e.printStackTrace();
		}
		return next;
	}

	private Config<?> nextConfig() throws ReflectiveOperationException, ReaderFinishedException{
		if(!reader.hasNextLine()) throw new ReaderFinishedException();
		String nextConfig = reader.nextLine();
		while(!nextConfig.startsWith("" +chars.getClassStart())){
			if(nextConfig.startsWith("%")) systemChange(nextConfig.substring(1));
			if(!reader.hasNextLine()) throw new ReaderFinishedException();
			nextConfig = reader.nextLine();
		}

		ArrayList<String> lines = new ArrayList<>();
		String nextConfigElement = reader.nextLine();
		while(!nextConfigElement.startsWith(chars.getClassEnd())){
			if(!nextConfigElement.startsWith(
				"" +chars.getClassStart())||
				nextConfigElement.trim().length() == 0)
					lines.add(nextConfigElement.trim());
			if(!reader.hasNext()) break;
			nextConfigElement = reader.nextLine();
		}

		String classPath = nextConfig.substring(nextConfig.indexOf(chars.getClassStart() + 1));
		String[] vals = lines.toArray(new String[lines.size()]);
		return loadConfig(classPath, vals);
	}

	@Override
	public boolean hasNext(){
		return buffer != null;
	}

	private void systemChange(String str){
		if(str.startsWith("ConfigIOChars :")){
			try {
				String classPath = str.substring(str.indexOf(":")).trim();
				Class<?> clazz = Class.forName(classPath);
				chars = (ConfigIOChars) clazz.newInstance();
			} catch (ReflectiveOperationException e) {
				chars = ConfigIOChars.getDefault();
			}
		}
	}

	public ConfigIOChars getConfigIOChars(){
		return chars;
	}

	@Override
	public void close() throws IOException{
		reader.close();
		if(source != null) source.close();
	}

	@Override
	public InputStream getSource() {
		return source;
	}

	private Config<?> loadConfig(String classPath, String[] readed) throws ReflectiveOperationException{
		Class<?> clazz = Class.forName(classPath);
		Config<?> cfg = (Config<?>) clazz.newInstance();
		return cfg.read(readed);
	}
}
