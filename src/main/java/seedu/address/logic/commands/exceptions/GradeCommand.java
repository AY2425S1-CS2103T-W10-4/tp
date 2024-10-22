package seedu.address.logic.commands.exceptions;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_GRADE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MODULE;

import java.util.List;
import java.util.Optional;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.CommandResult;
import seedu.address.model.Model;
import seedu.address.model.person.Module;
import seedu.address.model.person.Person;

/**
 * Grades a student in the address book.
 */
public class GradeCommand extends Command {
    public static final String COMMAND_WORD = "grade";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Grades a student identified by the index number "
            + "used in the displayed person list with a numerical score.\n"
            + "Parameters: INDEX (must be a positive integer) "
            + PREFIX_MODULE + "MODULE "
            + PREFIX_GRADE + "GRADE\n"
            + "Example: " + COMMAND_WORD + " 1 "
            + PREFIX_MODULE + "MA2103 "
            + PREFIX_GRADE + "80";
    public static final String MESSAGE_GRADE_SUCCESS = "Graded Module: %1$s for %2$s";
    public static final String MESSAGE_INVALID_GRADE = "The grade provided is invalid.";
    public static final String MESSAGE_INVALID_MODULE = "The module specified is not found.";
    public static final String MESSAGE_INVALID_PERSON = "The person index provided is invalid.";

    private final Index targetIndex;
    private final String module;
    private final int grade;

    /**
     * Creates a GradeCommand to grade the specified {@code Person}
     */
    public GradeCommand(Index targetIndex, String module, int grade) {
        this.targetIndex = targetIndex;
        this.module = module;
        this.grade = grade;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> lastShownList = model.getFilteredPersonList();

        if (targetIndex.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Person personToGrade = lastShownList.get(targetIndex.getZeroBased());
        Optional<Module> moduleToGrade = personToGrade.getModules().stream()
                .filter(m -> m.module.equals(this.module))
                .findFirst();

        if (moduleToGrade.isEmpty()) {
            throw new CommandException(MESSAGE_INVALID_MODULE);
        }
        Module module = moduleToGrade.get();
        module.assignGrade(grade);
        return new CommandResult(String.format(MESSAGE_GRADE_SUCCESS, module, personToGrade.getName()));
    }
}
