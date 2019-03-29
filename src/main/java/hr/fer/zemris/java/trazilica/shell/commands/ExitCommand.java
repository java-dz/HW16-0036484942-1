package hr.fer.zemris.java.trazilica.shell.commands;

import java.util.ArrayList;
import java.util.List;

import hr.fer.zemris.java.trazilica.shell.Environment;
import hr.fer.zemris.java.trazilica.shell.components.CommandStatus;

/**
 * A command that is used for terminating the Shell.
 *
 * @author Mario Bobic
 */
public class ExitCommand extends AbstractCommand {

    /**
     * Constructs a new command object of type {@code ExitCommand}.
     */
    public ExitCommand() {
        super("EXIT", createCommandDescription());
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
        desc.add("Exits the MyShell program.");
        return desc;
    }

    @Override
    public CommandStatus execute(Environment env, String s) {
        return CommandStatus.TERMINATE;
    }

}
