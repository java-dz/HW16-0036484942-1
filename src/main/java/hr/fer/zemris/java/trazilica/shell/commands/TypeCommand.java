package hr.fer.zemris.java.trazilica.shell.commands;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import hr.fer.zemris.java.trazilica.shell.Environment;
import hr.fer.zemris.java.trazilica.shell.components.CommandStatus;
import hr.fer.zemris.java.trazilica.shell.components.QueryResult;

/**
 * A command that is used for writing out the contents of a file. The file is
 * read with <tt>UTF-8</tt> charset. The command requires an argument that is an
 * index of the query result from the query result list. If the specified index
 * does not exist, an error message is written and the command returns.
 *
 * @author Mario Bobic
 */
public class TypeCommand extends AbstractCommand {

	/** Defines the proper syntax for using this command */
	private static final String SYNTAX = "type <result_index>";
	
	/**
	 * Constructs a new command object of type {@code TypeCommand}.
	 */
	public TypeCommand() {
		super("TYPE", createCommandDescription());
	}

	/**
	 * Creates a list of strings where each string represents a new line of this
	 * command's description. This method is generates description exclusively
	 * for the command that this class represents.
	 * 
	 * @return a list of strings that represents description
	 */
	private static List<String> createCommandDescription() {
		List<String> desc = new ArrayList<>();
		desc.add("Displays the contents of a file.");
		desc.add("The file is specified by the result index.");
		desc.add("This command is expected to be executed after the query command has generated results.");
		return desc;
	}

	@Override
	public CommandStatus execute(Environment env, String s) {
		if (s == null) {
			printSyntaxError(env, SYNTAX);
			return CommandStatus.CONTINUE;
		}
		
		int index;
		try {
			index = Integer.parseInt(s);
		} catch (NumberFormatException e) {
			printSyntaxError(env, SYNTAX);
			return CommandStatus.CONTINUE;
		}
		
		List<QueryResult> results = env.getResults();
		if (results == null) {
			writeln(env, "Query search must be executed before using this command!");
			return CommandStatus.CONTINUE;
		}
		
		if (index < 0 || index >= results.size()) {
			writeln(env, "Index is out of bounds. Valid indexes are in range [0," + (results.size()-1) + "]");
			return CommandStatus.CONTINUE;
		}
		
		QueryResult result = results.get(index);
		printDocument(env, result.filePath);
		
		return CommandStatus.CONTINUE;
	}
	
	/**
	 * Prints out contents of file with the specified <tt>path</tt> onto the
	 * environment <tt>env</tt> with special formatting.
	 * 
	 * @param env an environment
	 * @param path path to file to be printed
	 */
	private static void printDocument(Environment env, Path path) {
		writeln(env, "-------------------------------------------------");
		writeln(env, "Document: " + path);
		writeln(env, "-------------------------------------------------");
		
		try {
			Files.lines(path, StandardCharsets.UTF_8).forEach((line) -> {
				writeln(env, line);
			});
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		writeln(env, "-------------------------------------------------");
	}

}
