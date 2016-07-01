package hr.fer.zemris.java.trazilica.shell.commands;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import hr.fer.zemris.java.trazilica.shell.Environment;
import hr.fer.zemris.java.trazilica.shell.components.CommandStatus;
import hr.fer.zemris.java.trazilica.shell.components.QueryResult;

import static hr.fer.zemris.java.trazilica.shell.components.VectorUtilities.*;

/**
 * A command that is responsible for executing the search. A query is expected
 * in form of words, which are searched for in the dictionary and retained as
 * query words.
 * <p>
 * If there are no query words retained at all, an error message is written and
 * the search is not done. Otherwise, the search is {@link #executeQuery
 * executed}, results are fetched and {@link #processQueryResults processed} and
 * are {@link #printQueryResults written} to the environment output stream.
 *
 * @author Mario Bobic
 */
public class QueryCommand extends AbstractCommand {

	/** Defines the proper syntax for using this command */
	private static final String SYNTAX = "query <word1> (optional: <word2>...<wordN>)";

	/** Lowest limit until similarity is considered 0. */
	private static final double SIMILARITY_LIMIT = 5E-4;

	/** Maximum number of query results. */
	private static final int MAX_QUERY_RESULTS = 10;
	
	/**
	 * Constructs a new command object of type {@code QueryCommand}.
	 */
	public QueryCommand() {
		super("QUERY", createCommandDescription());
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
		desc.add("Executes the search.");
		desc.add("This command requires at least one argument in order to execute the search.");
		return desc;
	}

	@Override
	public CommandStatus execute(Environment env, String s) {
		if (s == null) {
			printSyntaxError(env, SYNTAX);
			return CommandStatus.CONTINUE;
		}
		
		List<String> words = env.getDataLoader().getVocabularyWords(s);
		if (words.isEmpty()) {
			writeln(env, "Query words not found in vocabulary (maybe it contains only stopwords).");
			return CommandStatus.CONTINUE;
		}
		
		writeln(env, "Query is: " + words);
		
		List<QueryResult> queryResults = executeQuery(env, words); // Contains at least one result
		processQueryResults(env, queryResults);
		printQueryResults(env, queryResults);
		
		return CommandStatus.CONTINUE;
	}

	/**
	 * Executes the query of the specified <tt>words</tt> by calculating the
	 * <tt>TF-IDF</tt> vector of the words and doing vector operations with
	 * vectors of file-loaded words.
	 * <p>
	 * The results are generated and returned, containing query search
	 * <tt>similarity</tt> and file <tt>path</tt>.
	 * 
	 * @param env an environment
	 * @param words query words
	 * @return results of the executed query
	 */
	private static List<QueryResult> executeQuery(Environment env, List<String> words) {
		List<QueryResult> queryResults = new ArrayList<>();
		
		List<Double> userVector = env.getDataLoader().generateTfIdfVector(words);
		Map<Integer, List<Double>> fileVectors = env.getDataLoader().getFileVectors();

		Map<Integer, Path> files = env.getDataLoader().getFiles();
		
		double userVectorNorm = norm(userVector);
		fileVectors.forEach((fileKey, fileVector) -> {
			double fileVectorNorm = norm(fileVector);
			double scalarProduct = scalarProduct(userVector, fileVector);
			
			double similarity = scalarProduct / (userVectorNorm * fileVectorNorm);
			
			if (similarity >= SIMILARITY_LIMIT) {
				queryResults.add(new QueryResult(similarity, files.get(fileKey)));
			}
		});
		
		return queryResults;
	}
	
	/**
	 * Processes the specified <tt>queryResults</tt> by sorting them by their
	 * natural order specified by the {@linkplain QueryResult#compareTo}
	 * method and shortening the results list if it contains more than
	 * {@linkplain #MAX_QUERY_RESULTS} results.
	 * <p>
	 * Finally, this method sets the current result list to the environment
	 * <tt>env</tt>.
	 * 
	 * @param env an environment
	 * @param queryResults query results to be processed
	 */
	private static void processQueryResults(Environment env, List<QueryResult> queryResults) {
		queryResults.sort(null);
		
		if (queryResults.size() > MAX_QUERY_RESULTS) {
			queryResults.subList(MAX_QUERY_RESULTS, queryResults.size()).clear();
		}
		
		env.setResults(queryResults);
	}
	
	/**
	 * Prints out the specified <tt>queryResults</tt> onto the environment
	 * <tt>env</tt> with special formatting.
	 * 
	 * @param env an environment
	 * @param queryResults query results to be printed
	 */
	private static void printQueryResults(Environment env, List<QueryResult> queryResults) {
		writeln(env, "Top results are:");
		int i = 0;
		for (QueryResult result : queryResults) {
			writeln(env, String.format("[%d] (%.4f) %s", i++, result.similarity, result.filePath));
		}
	}

}
