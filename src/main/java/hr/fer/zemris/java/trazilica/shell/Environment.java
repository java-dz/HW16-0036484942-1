package hr.fer.zemris.java.trazilica.shell;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import hr.fer.zemris.java.trazilica.shell.commands.QueryCommand;
import hr.fer.zemris.java.trazilica.shell.commands.ShellCommand;
import hr.fer.zemris.java.trazilica.shell.components.DataLoader;
import hr.fer.zemris.java.trazilica.shell.components.QueryResult;

/**
 * This interface represents an environment where the whole program works. It is
 * used for claiming the current user's path, working with commands, writing
 * out informational messages to the user and handling query results.
 *
 * @author Mario Bobic
 */
public interface Environment {

	/**
	 * Reads the user's input and returns it as a string.
	 * 
	 * @return the user's input
	 * @throws IOException if an I/O exception occurs
	 */
	public String readLine() throws IOException;

	/**
	 * Writes the given string using the writer.
	 * 
	 * @param s string to be written
	 * @throws IOException if an I/O exception occurs
	 */
	public void write(String s) throws IOException;
	
	/**
	 * Writes the given array of characters using the writer.
	 * 
	 * @param cbuf array of characters to be written
	 * @param off offset
	 * @param len length to be written
	 */
	public void write(char cbuf[], int off, int len);

	/**
	 * Writes the given string using the writer, inputting a new line at the
	 * end.
	 * 
	 * @param s string to be written
	 * @throws IOException if an I/O exception occurs
	 */
	public void writeln(String s) throws IOException;

	/**
	 * Returns an iterable object containing this Shell's commands.
	 * 
	 * @return an iterable object containing this Shell's commands
	 */
	public Iterable<ShellCommand> commands();
	
	/**
	 * Returns the current path from which documents are read.
	 * 
	 * @return the current path from which documents are read
	 */
	public Path getCurrentPath();

	/**
	 * Sets the current path from which documents are read and loads
	 * all documents from the specified <tt>path</tt> recursively.
	 * 
	 * @param path the new path from which documents are read
	 * @throws IllegalArgumentException if the specified path is not a directory
	 * @throws IOException if an error occurs while loading documents
	 */
	public void setCurrentPath(Path path) throws IOException;

	/**
	 * Sets the current path from which documents are read and loads
	 * all documents from the specified <tt>path</tt> recursively.
	 * <p>
	 * Converts the specified string <tt>path</tt> to a {@linkplain Path}
	 * object. Throws {@linkplain IllegalArgumentException} if path is invalid
	 * or is not a directory.
	 * 
	 * @param path the new path from which documents are read
	 * @throws IllegalArgumentException if the specified path is invalid
	 * @throws IOException if an error occurs while loading documents
	 */
	public void setCurrentPath(String path) throws IOException;
	
	/**
	 * Returns the data loader that loaded all documents and created a
	 * vocabulary.
	 * 
	 * @return the data loader that loaded all documents
	 */
	public DataLoader getDataLoader();
	
	/**
	 * Returns a list of last generated results from the query search or
	 * <tt>null</tt> if {@linkplain QueryCommand} was never ran.
	 * 
	 * @return a list of last generated results or <tt>null</tt>
	 */
	public List<QueryResult> getResults();
	
	/**
	 * Sets the list of results to the specified list.
	 * 
	 * @param results list of query results
	 * @throws NullPointerException if <tt>results</tt> is <tt>null</tt>
	 */
	public void setResults(List<QueryResult> results);
}
