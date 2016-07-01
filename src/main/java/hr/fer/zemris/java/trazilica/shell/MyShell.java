package hr.fer.zemris.java.trazilica.shell;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import hr.fer.zemris.java.trazilica.shell.commands.*;
import hr.fer.zemris.java.trazilica.shell.components.CommandStatus;
import hr.fer.zemris.java.trazilica.shell.components.DataLoader;
import hr.fer.zemris.java.trazilica.shell.components.QueryResult;

/**
 * MyShell, this is where the magic happens. Scans the user's input and searches
 * for a matching command. Some commands require arguments, so the user must
 * input them as well. If the inputed command is found, the command is executed,
 * otherwise an error message is displayed. The program stops and prints out a
 * goodbye message if the inputed command is {@linkplain ExitCommand}. The
 * program also prompts an input symbol while waiting for a command to be
 * inputed. If a critical error occurs, an error message is printed out onto the
 * <b>standard error</b> with a detail message specifying what went wrong and
 * the program terminates.
 *
 * @author Mario Bobic
 */
public class MyShell {
	
	/** A map of commands. */
	private static Map<String, ShellCommand> commands;
	
	static {
		commands = new HashMap<>();
		ShellCommand[] cc = {
				new QueryCommand(),
				new TypeCommand(),
				new ResultsCommand(),
				new SetPathCommand(),
				new HelpCommand(),
				new ExitCommand()
		};
		for (ShellCommand c : cc) {
			commands.put(c.getCommandName(), c);
		}
	}
	
	/** An environment used by MyShell. */
	private static EnvironmentImpl environment = new EnvironmentImpl();
	
	/**
	 * Program entry point.
	 * 
	 * @param args not used in this program
	 * @throws IOException
	 *             if an IO exception occurs while writing or reading the input.
	 *             This is a critical exception which terminates the program
	 *             violently.
	 */
	public static void main(String[] args) throws IOException {
		if (args.length != 1) {
			environment.write("Enter path to directory: ");
			args = new String[] {environment.readLine()};
		}
		
		try {
			environment.setCurrentPath(args[0]);
		} catch (Exception e) {
			environment.writeln("Error occured while loading from " + args[0] + ": " + e.getMessage());
			return;
		}

		environment.writeln("Dictionary size: " + environment.dataLoader.getVocabularySet().size());
		environment.writeln("Number of loaded documents: " + environment.dataLoader.getFiles().keySet().size());
		environment.writeln("");
		environment.writeln("Welcome to MyShell! You may enter commands.");
		
		while (true) {
			environment.write("Enter command> ");
			
			String line = environment.readLine().trim();
			
			String cmd;
			String arg;
			int splitter = indexOfWhitespace(line);
			if (splitter != -1) {
				cmd = line.substring(0, splitter).toUpperCase();
				arg = line.substring(splitter+1).trim();
			} else {
				cmd = line.toUpperCase();
				arg = null;
			}
			
			ShellCommand command = commands.get(cmd);
			if (command == null) {
				environment.writeln("Unknown command!");
				environment.writeln("");
				continue;
			}
			
			CommandStatus status;
			try {
				status = command.execute(environment, arg);
			} catch (RuntimeException critical) {
				System.err.println("A critical error occured: " + critical.getMessage());
				return;
			}
			
			if (status == CommandStatus.TERMINATE) {
				break;
			} else {
				environment.writeln("");
			}
		}
		
		environment.writeln("Goodbye!");
	}
	
	/**
	 * Returns the index within the specified string <tt>str</tt> of the first
	 * occurrence of a whitespace character determined by the
	 * {@linkplain Character#isWhitespace(char)} method.
	 * 
	 * @param str string whose index of the first whitespace is to be returned
	 * @return the index of the first occurrence of a whitespace character
	 */
	private static int indexOfWhitespace(String str) {
		for (int i = 0, n = str.length(); i < n; i++) {
			if (Character.isWhitespace(str.charAt(i))) {
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * An environment implemented. Both reader and writer are implemented to
	 * work with the standard input and output.
	 *
	 * @author Mario Bobic
	 */
	public static class EnvironmentImpl implements Environment {
		
		/** Current path from which documents are read. */
		private Path currentPath;
		
		/** Data loader that loads all documents and creates a vocabulary. */
		private DataLoader dataLoader;
		
		/** The last generated query search results. */
		private List<QueryResult> queryResults;
		/** A reader that reads from the standard input. */
		private BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		/** A writer that writes on the standard output. */
		private BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out));
		
		@Override
		public String readLine() throws IOException {
			return reader.readLine();
		}
		
		@Override
		public void write(String s) throws IOException {
			writer.write(s);
			writer.flush();
		}
		
		@Override
		public void write(char cbuf[], int off, int len) {
			try {
				writer.write(cbuf, off, len);
				writer.flush();
			} catch (IOException e) {}
		}
		
		@Override
		public void writeln(String s) throws IOException {
			write(s);
			writer.newLine();
			writer.flush();
		}
		
		@Override
		public Iterable<ShellCommand> commands() {
			return commands.values()
				.stream()
				.sorted((cmd1, cmd2) -> cmd1.getCommandName().compareTo(cmd2.getCommandName()))
				.collect(Collectors.toList());
		}
		
		@Override
		public Path getCurrentPath() {
			return currentPath;
		}

		@Override
		public void setCurrentPath(Path path) throws IOException {
			if (!Files.isDirectory(path)) {
				throw new IllegalArgumentException("Specified path must be a directory: " + path);
			}
			
			currentPath = path.toAbsolutePath().normalize();
			dataLoader = new DataLoader(currentPath);
		}

		@Override
		public void setCurrentPath(String path) throws IOException {
			try {
				setCurrentPath(Paths.get(path));
			} catch (InvalidPathException e) {
				throw new IllegalArgumentException("Invalid path", e);
			}
		}

		@Override
		public DataLoader getDataLoader() {
			return dataLoader;
		}

		@Override
		public List<QueryResult> getResults() {
			return queryResults;
		}

		@Override
		public void setResults(List<QueryResult> results) {
			queryResults = Objects.requireNonNull(results);
		}
	}
	
}
