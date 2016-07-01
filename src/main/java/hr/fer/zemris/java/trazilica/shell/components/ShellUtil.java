package hr.fer.zemris.java.trazilica.shell.components;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A utility class. Used for defining and providing utility methods.
 *
 * @author Mario Bobic
 */
public class ShellUtil {
	
	/**
	 * Disables instantiation.
	 */
	private ShellUtil() {
	}
	
	/**
	 * Resolves the given path by checking if it is a valid path. If the
	 * {@code str} path parameter is an absolute path then this method trivially
	 * returns the given path. In the simplest case, the given path does not
	 * have a root component, in which case this method joins the given path
	 * with the root and returns the absolute path. If the given path has
	 * invalid characters {@code null} value is returned.
	 * 
	 * @param str the given path string
	 * @return the absolute path of the given path
	 */
	public static Path resolvePath(String str) {
		str = str.replace("\"", "");
		
		try {
			return Paths.get(str).toAbsolutePath().normalize();
		} catch (InvalidPathException e) {
			return null;
		}
	}
	
	/**
	 * Used for extracting arguments passed to a function. This method
	 * supports an unlimited number of arguments, and can be inputed either
	 * with quotation marks or not. Returns an array of strings containing the
	 * extracted arguments.
	 * 
	 * @param s a string containing arguments
	 * @return an array of strings containing extracted arguments.
	 */
	public static String[] extractArguments(String s) {
		List<String> list = new ArrayList<>();
		
		String regex = "\"([^\"]*)\"|(\\S+)";
		Matcher m = Pattern.compile(regex).matcher(s);
		while (m.find()) {
			if (m.group(1) != null) {
				list.add(m.group(1));
			} else {
				list.add(m.group(2));
			}
		}

		return list.toArray(new String[list.size()]);
	}
	
	/**
	 * Loads a textual resource file with the specified <tt>name</tt> using the
	 * {@linkplain ClassLoader#getResource(String)} method. The <tt>URL</tt>
	 * object is converted to URI which is applicable for the
	 * {@linkplain Paths#get(java.net.URI)} method.
	 * <p>
	 * Returns all lines loaded from the specified resource file using the
	 * {@linkplain Files#readAllLines(Path)} method.
	 * 
	 * @param name name of the resource
	 * @return lines loaded from the specified resource as a <tt>List</tt>
	 * @throws IOException if loading the resource fails
	 */
	public static List<String> loadTextResource(String name) throws IOException {
		URL url = ShellUtil.class.getClassLoader().getResource(name);
		
		Path path;
		try {
			path = Paths.get(url.toURI());
		} catch (URISyntaxException e) {
			throw new IOException("Error loading resource file: " + name, e);
		}
		
		return Files.readAllLines(path);
	}
	
	/**
	 * Converts the number of bytes to a human readable byte count with binary
	 * prefixes.
	 * 
	 * @param bytes number of bytes
	 * @return human readable byte count with binary prefixes
	 */
	public static String humanReadableByteCount(long bytes) {
		/* Use the natural 1024 units and binary prefixes. */
		int unit = 1024;
		if (bytes < unit) return bytes + " B";
		int exp = (int) (Math.log(bytes) / Math.log(unit));
		String pre = "kMGTPE".charAt(exp - 1) + "i";
		return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}
	
}
