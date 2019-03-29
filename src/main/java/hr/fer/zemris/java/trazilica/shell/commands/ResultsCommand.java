package hr.fer.zemris.java.trazilica.shell.commands;

import java.util.ArrayList;
import java.util.List;

import hr.fer.zemris.java.trazilica.shell.Environment;
import hr.fer.zemris.java.trazilica.shell.components.CommandStatus;
import hr.fer.zemris.java.trazilica.shell.components.QueryResult;

/**
 * Displays the previously executed query search results.
 *
 * @author Mario Bobic
 */
public class ResultsCommand extends AbstractCommand {

    /** Defines the proper syntax for using this command */
    private static final String SYNTAX = "results";

    /**
     * Constructs a new command object of type {@code ResultsCommand}.
     */
    public ResultsCommand() {
        super("RESULTS", createCommandDescription());
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
        desc.add("Displays the previously executed search results.");
        desc.add("This command takes no arguments.");
        desc.add("It is expected that query was executed before this command is.");
        return desc;
    }

    @Override
    public CommandStatus execute(Environment env, String s) {
        if (s != null) {
            printSyntaxError(env, SYNTAX);
            return CommandStatus.CONTINUE;
        }

        List<QueryResult> results = env.getResults();
        if (results == null) {
            writeln(env, "Query search must be executed before using this command!");
            return CommandStatus.CONTINUE;
        }

        int i = 0;
        for (QueryResult result : results) {
            writeln(env, String.format("[%d] (%.4f) %s", i++, result.similarity, result.filePath));
        }

        return CommandStatus.CONTINUE;
    }

}
