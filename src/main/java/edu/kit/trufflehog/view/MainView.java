package edu.kit.trufflehog.view;

import edu.kit.trufflehog.command.usercommand.IUserCommand;
import edu.kit.trufflehog.interaction.MainInteraction;
import edu.kit.trufflehog.view.controllers.BorderPaneController;

import java.util.EnumMap;
import java.util.Map;

/**
 * <p>
 *     The MainView incorporates all GUI elements that belong to the primary scope of the application.
 *     This for example includes the top Menu Bar and all the settings menus as well as the statistic windows.
 * </p>
 */
public class MainView extends BorderPaneController {
    /**
     * <p>
     *     The commands that are mapped to their interactions.
     * </p>
     */
    private final Map<MainInteraction, IUserCommand> interactionMap = new EnumMap<>(MainInteraction.class);

    /**
     * <p>
     *     Creates a new MainView with the given fxmlFileName. The fxml file has to be in the same namespace
     *     as the MainView.
     * </p>
     *
     * @param fxmlFileName the name of the fxml file to be loaded.
     */
    public MainView(final String fxmlFileName) {
        super(fxmlFileName);
    }

    /**
     * <p>
     *     Execute the routine for quitting the application.
     * </p>
     */
    public void onExit() {
        throw new UnsupportedOperationException("Not implemented yet!");
    }
}
