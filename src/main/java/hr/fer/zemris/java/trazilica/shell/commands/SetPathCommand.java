package hr.fer.zemris.java.trazilica.shell.commands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import hr.fer.zemris.java.trazilica.shell.Environment;
import hr.fer.zemris.java.trazilica.shell.components.CommandStatus;
import hr.fer.zemris.java.trazilica.shell.components.ShellUtil;

/**
 * Sets the current path from which documents are read to the new provided path
 * and loads all documents recursively. If a syntax error occurs or path fails
 * to be set, an error message is displayed to the user.
 *
 * @author Mario Bobic
 */
public class SetPathCommand extends AbstractCommand {

    /** Defines the proper syntax for using this command */
    private static final String SYNTAX = "setpath <path>";

    /**
     * Constructs a new command object of type {@code SetPathCommand}.
     */
    public SetPathCommand() {
        super("SETPATH", createCommandDescription());
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
        desc.add("Sets the path from which it loads textual files.");
        desc.add("This command takes a single argument - path to directory with textual files.");
        return desc;
    }

    @Override
    public CommandStatus execute(Environment env, String s) {
        if (s == null) {
            printSyntaxError(env, SYNTAX);
            return CommandStatus.CONTINUE;
        }

        try {
            env.setCurrentPath(s);
        } catch (IOException e) {
            writeln(env, e.getMessage());
        }

        writeln(env, "Path set to " + ShellUtil.resolvePath(s));
        writeln(env, "Dictionary size: " + env.getDataLoader().getVocabularySet().size());
        writeln(env, "Number of loaded documents: " + env.getDataLoader().getFiles().keySet().size());

        return CommandStatus.CONTINUE;
    }

}
