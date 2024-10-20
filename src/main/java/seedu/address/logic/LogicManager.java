package seedu.address.logic;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Path;
import java.util.logging.Logger;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.logic.commands.ArchiveCommand;
import seedu.address.logic.commands.ArchivePathChangeCommand;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.FilePathChangeCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Person;
import seedu.address.storage.AddressBookStorage;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.Storage;
import seedu.address.storage.StorageManager;
import seedu.address.storage.UserPrefsStorage;

/**
 * The main LogicManager of the app.
 */
public class LogicManager implements Logic {
    public static final String FILE_OPS_ERROR_FORMAT = "Could not save data due to the following error: %s";

    public static final String FILE_OPS_PERMISSION_ERROR_FORMAT =
            "Could not save data to file %s due to insufficient permissions to write to the file or the folder.";

    private final Logger logger = LogsCenter.getLogger(LogicManager.class);

    private final Model model;
    private Storage storage;
    private final AddressBookParser addressBookParser;

    /**
     * Constructs a {@code LogicManager} with the given {@code Model} and {@code Storage}.
     */
    public LogicManager(Model model, Storage storage) {
        this.model = model;
        this.storage = storage;
        addressBookParser = new AddressBookParser();
    }

    @Override
    public CommandResult execute(String commandText) throws CommandException, ParseException {
        logger.info("----------------[USER COMMAND][" + commandText + "]");

        ReadOnlyAddressBook original = model.getAddressBook();
        CommandResult commandResult;
        Command command = addressBookParser.parseCommand(commandText);
        try {
            if (command instanceof ArchiveCommand) {
                storage.saveArchivedAddressBook(original);
            }
        } catch (AccessDeniedException e) {
            throw new CommandException(String.format(FILE_OPS_PERMISSION_ERROR_FORMAT, e.getMessage()), e);
        } catch (IOException ioe) {
            throw new CommandException(String.format(FILE_OPS_ERROR_FORMAT, ioe.getMessage()), ioe);
        }

        commandResult = command.execute(model);

        try {
            storage = updateStorageAndModel(command, storage, model);
            storage.saveAddressBook(model.getAddressBook());
        } catch (AccessDeniedException e) {
            throw new CommandException(String.format(FILE_OPS_PERMISSION_ERROR_FORMAT, e.getMessage()), e);
        } catch (IOException ioe) {
            throw new CommandException(String.format(FILE_OPS_ERROR_FORMAT, ioe.getMessage()), ioe);
        }

        return commandResult;
    }

    @Override
    public ReadOnlyAddressBook getAddressBook() {
        return model.getAddressBook();
    }

    @Override
    public ObservableList<Person> getFilteredPersonList() {
        return model.getFilteredPersonList();
    }

    @Override
    public Path getAddressBookFilePath() {
        return model.getAddressBookFilePath();
    }

    @Override
    public GuiSettings getGuiSettings() {
        return model.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        model.setGuiSettings(guiSettings);
    }

    @Override
    public Storage getStorage() {
        return storage;
    }

    private Storage updateStorageAndModel(Command command, Storage currentStorage, Model model) {
        if (command instanceof FilePathChangeCommand) {
            UserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(currentStorage.getUserPrefsFilePath());
            AddressBookStorage addressBookStorage = new JsonAddressBookStorage(model.getAddressBookFilePath(),
                    model.getArchivedAddressBookFilePath());
            Storage newStorage = new StorageManager(addressBookStorage, userPrefsStorage);
            try {
                model.setAddressBook(newStorage.readAddressBook().orElse(new AddressBook()));
            } catch (DataLoadingException e) {
                throw new RuntimeException(e);
            }
            return newStorage;
        } else if (command instanceof ArchivePathChangeCommand) {
            UserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(currentStorage.getUserPrefsFilePath());
            AddressBookStorage addressBookStorage = new JsonAddressBookStorage(model.getAddressBookFilePath(),
                    model.getArchivedAddressBookFilePath());
            Storage newStorage = new StorageManager(addressBookStorage, userPrefsStorage);
            return newStorage;
        } else {
            return currentStorage;
        }
    }
}
