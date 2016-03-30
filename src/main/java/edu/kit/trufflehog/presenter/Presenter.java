package edu.kit.trufflehog.presenter;

import edu.kit.trufflehog.command.usercommand.*;
import edu.kit.trufflehog.interaction.FilterInteraction;
import edu.kit.trufflehog.interaction.GraphInteraction;
import edu.kit.trufflehog.interaction.ToolbarViewInteraction;
import edu.kit.trufflehog.model.FileSystem;
import edu.kit.trufflehog.model.configdata.ConfigData;
import edu.kit.trufflehog.model.filter.MacroFilter;
import edu.kit.trufflehog.model.network.INetwork;
import edu.kit.trufflehog.model.network.LiveNetwork;
import edu.kit.trufflehog.model.network.graph.IConnection;
import edu.kit.trufflehog.model.network.graph.INode;
import edu.kit.trufflehog.model.network.graph.LiveUpdater;
import edu.kit.trufflehog.model.network.recording.INetworkDevice;
import edu.kit.trufflehog.model.network.recording.INetworkReadingPortSwitch;
import edu.kit.trufflehog.model.network.recording.INetworkViewPortSwitch;
import edu.kit.trufflehog.model.network.recording.INetworkWritingPortSwitch;
import edu.kit.trufflehog.model.network.recording.NetworkDevice;
import edu.kit.trufflehog.model.network.recording.NetworkReadingPortSwitch;
import edu.kit.trufflehog.model.network.recording.NetworkViewPortSwitch;
import edu.kit.trufflehog.model.network.recording.NetworkWritingPortSwitch;
import edu.kit.trufflehog.service.NodeStatisticsUpdater;
import edu.kit.trufflehog.service.executor.CommandExecutor;
import edu.kit.trufflehog.service.packetdataprocessor.profinetdataprocessor.TruffleReceiver;
import edu.kit.trufflehog.service.packetdataprocessor.profinetdataprocessor.UnixSocketReceiver;
import edu.kit.trufflehog.view.*;
import edu.kit.trufflehog.view.jung.visualization.FXVisualizationViewer;
import edu.kit.trufflehog.view.jung.visualization.SceneGestures;
import edu.kit.trufflehog.viewmodel.FilterViewModel;
import edu.kit.trufflehog.viewmodel.GeneralStatisticsViewModel;
import edu.kit.trufflehog.viewmodel.StatisticsViewModel;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.ObservableUpdatableGraph;
import edu.uci.ics.jung.graph.util.Graphs;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.DecimalFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *     The Presenter builds TruffleHog. It instantiates all necessary instances of classes, registers these instances
 *     with each other, distributes resources (parameters) to them etc. In other words it is the glue code of
 *     TruffleHog. There should always only be one instance of a Presenter around.
 * </p>
 */
public class Presenter {
    private static final Logger logger = LogManager.getLogger();

    private final ConfigData configData;
    private final FileSystem fileSystem;
    private final ScheduledExecutorService executorService;
    private final Stage primaryStage;
    private TruffleReceiver truffleReceiver;
    private INetworkViewPortSwitch viewPortSwitch;
    private INetworkDevice networkDevice;
    private INetwork liveNetwork;
    private INetworkWritingPortSwitch writingPortSwitch;
    private final MacroFilter macroFilter = new MacroFilter();

    private FilterViewModel filterViewModel;

    private final CommandExecutor commandExecutor = new CommandExecutor();

    /**
     * <p>
     *     Creates a new instance of a Presenter.
     * </p>
     */
    public Presenter(Stage primaryStage) {
        this.primaryStage = primaryStage;

        if (this.primaryStage == null) {
            throw new NullPointerException("primary stage should not be null");
        }

        this.fileSystem = new FileSystem();
        this.executorService = LoggedScheduledExecutor.getInstance();

        ConfigData configDataTemp;
        try {
            configDataTemp = new ConfigData(fileSystem);
        } catch (NullPointerException e) {
            configDataTemp = null;
            logger.error("Unable to set config data model", e);
        }
        configData = configDataTemp;

       // primaryStage.setOnCloseRequest(event -> finish());


    }


    /**
     * <p>
     *     Present TruffleHog. Create all necessary objects, register them with each other, bind them, pass them the
     *     resources they need ect.
     * </p>
     */
    public void run() {

        initModel();
        initServices();
        initDatabase();
        initGUI();

        //ObservableLayout<INode, IConnection> layouts = new ObservableLayout<>(new FRLayout<>(sgv.g));
        //layouts.setSize(new Dimension(300,300)); // sets the initial size of the space



       // truffleReceiver.connect();


        primaryStage.setOnCloseRequest(e -> {

            //viewer.setCache(false);

            Platform.exit();

            //final ExecutorService service = Executors.newSingleThreadExecutor();
            // Close all databases and other resources accessing the hard drive that need to be closed.
            configData.close();

            // Disconnect the truffleReceiver
            if (truffleReceiver != null) {
                truffleReceiver.disconnect();
            }

            // Kill all threads and the thread pool with it
            LoggedScheduledExecutor.getInstance().shutdownNow();

            System.gc();
            // Shut down the system
            System.exit(0);
        });

/*        viewBuilder.build(viewPortSwitch, liveNetwork, networkDevice, commandExecutor.asUserCommandListener(),
                new UpdateFilterCommand(liveNetwork.getRWPort(), macroFilter));*/
    }

    private void initModel() {

        initNetwork();

        filterViewModel = new FilterViewModel(configData);
    }

    private void initServices() {

    }

    private void initGUI() {

        final FXVisualizationViewer<INode, IConnection> viewer = new FXVisualizationViewer<>(liveNetwork.getViewPort().getDelegate(), liveNetwork.getViewPort());
        SceneGestures sceneGestures = new SceneGestures(viewer.getCanvas());

        Scene scene = new Scene(viewer);

        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.X, KeyCombination.CONTROL_DOWN),
                viewer::refreshLayout);
        scene.addEventFilter( MouseEvent.MOUSE_PRESSED, sceneGestures.getOnMousePressedEventHandler());
        scene.addEventFilter( MouseEvent.MOUSE_DRAGGED, sceneGestures.getOnMouseDraggedEventHandler());
        scene.addEventFilter( ScrollEvent.ANY, sceneGestures.getOnScrollEventHandler());

        final FilterEditingMenuViewController filterEditingMenuViewController = new FilterEditingMenuViewController(configData, filterViewModel);
        filterEditingMenuViewController.setVisible(false);
        viewer.getChildren().add(filterEditingMenuViewController);
        AnchorPane.setRightAnchor(filterEditingMenuViewController, 0d);
        filterEditingMenuViewController.addCommand(FilterInteraction.ADD, new AddFilterCommand(configData, liveNetwork.getRWPort(), macroFilter));
        filterEditingMenuViewController.addListener(commandExecutor.asUserCommandListener());

        final FilterOverlayView filterOverlayView = new FilterOverlayView(configData.getAllLoadedFilters(), filterEditingMenuViewController);
        filterOverlayView.setVisible(false);
        viewer.getChildren().add(filterOverlayView);
        AnchorPane.setLeftAnchor(filterOverlayView, 0d);



        final ToolbarView toolbarView = new ToolbarView(filterOverlayView);
        toolbarView.addCommand(ToolbarViewInteraction.CONNECT, new ConnectToSPPProfinetCommand(truffleReceiver));
        toolbarView.addCommand(ToolbarViewInteraction.DISCONNECT, new DisconnectSPPProfinetCommand(truffleReceiver));
        viewer.getChildren().add(toolbarView);
        AnchorPane.setBottomAnchor(toolbarView, 5d);
        AnchorPane.setLeftAnchor(toolbarView, 5d);

        final StatisticsViewModel statisticsViewModel = new StatisticsViewModel();
        viewer.addCommand(GraphInteraction.SELECTION, new SelectionCommand(statisticsViewModel));
        final StatisticsViewController statisticsViewController = new StatisticsViewController(statisticsViewModel);
        viewer.getChildren().add(statisticsViewController);
        AnchorPane.setTopAnchor(statisticsViewController, 10d);
        AnchorPane.setRightAnchor(statisticsViewController, 10d);

        final GeneralStatisticsViewModel generalStatisticsViewModel = new GeneralStatisticsViewModel();
        final GeneralStatisticsViewController generalStatisticsViewController = new GeneralStatisticsViewController(generalStatisticsViewModel);
        viewer.getChildren().add(generalStatisticsViewController);

        // FIXME This part is a bit verbose!!! _____________ START
        StringProperty timeProperty = new SimpleStringProperty("");
        StringProperty throughputStringProperty = new SimpleStringProperty();
        throughputStringProperty.bindBidirectional(viewPortSwitch.getThroughputProperty(), new DecimalFormat("0.00"));

        generalStatisticsViewModel.getRootItem().getChildren().add(new TreeItem<>(new GeneralStatisticsViewModel.StringEntry<>("Population", viewPortSwitch.getPopulationProperty())));
        generalStatisticsViewModel.getRootItem().getChildren().add(new TreeItem<>(new GeneralStatisticsViewModel.StringEntry<>("Packages per second", throughputStringProperty)));
        generalStatisticsViewModel.getRootItem().getChildren().add(new TreeItem<>(new GeneralStatisticsViewModel.StringEntry<>("Running", timeProperty)));
        //generalStatisticsOverlay.setVisible(true);

        //TODO improve this!
        viewPortSwitch.getViewTimeProperty().addListener((observable, oldValue, newValue) -> {
            StringBuilder sb = new StringBuilder();
            long ms = newValue.longValue();
            long hours = TimeUnit.MILLISECONDS.toHours(ms);
            ms -= TimeUnit.HOURS.toMillis(hours);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(ms);
            ms -= TimeUnit.MINUTES.toMillis(minutes);
            long seconds = TimeUnit.MILLISECONDS.toSeconds(ms);

            sb.append(hours);
            sb.append("h ");
            sb.append(minutes);
            sb.append("m ");
            sb.append(seconds);
            sb.append("s");

            timeProperty.setValue(sb.toString());
        });
        AnchorPane.setBottomAnchor(generalStatisticsViewController, 10d);
        AnchorPane.setRightAnchor(generalStatisticsViewController, 10d);

        // FIXME This part is a bit verbose!!! _____________ END

        viewer.addListener(commandExecutor.asUserCommandListener());


        primaryStage.setScene(scene);
        primaryStage.show();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void initDatabase() {



    }

    private void initNetwork() {

        // initialize the live network that will be writte on by the receiver commands

        // create a Graph


        // TODO check if synchronized is really needed
        final Graph<INode, IConnection> graph = Graphs.synchronizedDirectedGraph(new DirectedSparseGraph<>());

        final ObservableUpdatableGraph<INode, IConnection> og = new ObservableUpdatableGraph<>(graph, new LiveUpdater());

        // TODO Ctor injection with the Ports that are within the networks
        liveNetwork = new LiveNetwork(og);

        /*
        // initialize the replay network that will be written on by a networkTape if the device plays a replay
        final INetwork replayNetwork = new ReplayNetwork(new ConcurrentDirectedSparseGraph<>());
*/
        // initialize the writing port switch that use the writing port of the live network
        // as their initial default writing port
        writingPortSwitch = new NetworkWritingPortSwitch(liveNetwork.getWritingPort());

        // initialize the reading port switch that uses the reading port of the live network
        // as its initial default reading port
        final INetworkReadingPortSwitch readingPortSwitch = new NetworkReadingPortSwitch(liveNetwork.getReadingPort());

        // initialize the view port switch that uses the view port of the live network
        // as its initial default view port
        viewPortSwitch = new NetworkViewPortSwitch(liveNetwork.getViewPort());

        // intialize the network device which is capable of recording and replaying any ongoing network session
        // on serveral screens
        networkDevice = new NetworkDevice();

        // create a new empty tape to record something on
        // Tell the network observation device to start recording the
        // given network with 25fps on the created tape

        final ExecutorService truffleFetchService = Executors.newSingleThreadExecutor();

        // TODO register the truffleReceiver somewhere so we can start or stop it.
       truffleReceiver = new UnixSocketReceiver(liveNetwork.getWritingPort(), macroFilter);
       //truffleReceiver = new TruffleCrook(liveNetwork.getWritingPort(), macroFilter);


        //this.viewBuilder = new ViewBuilder(configData, this.primaryStage, this.viewPortMap, this.truffleReceiver);
        truffleFetchService.execute(truffleReceiver);


        // Initialize the command executor and register it.
        final ExecutorService commandExecutorService = Executors.newSingleThreadExecutor();
        commandExecutorService.execute(commandExecutor);
        truffleReceiver.addListener(commandExecutor.asTruffleCommandListener());

        final ExecutorService nodeStatisticsUpdaterService = Executors.newSingleThreadExecutor();
        final NodeStatisticsUpdater nodeStatisticsUpdater = new NodeStatisticsUpdater(readingPortSwitch, viewPortSwitch);
        nodeStatisticsUpdaterService.execute(nodeStatisticsUpdater);

        // goReplay that ongoing recording on the given viewportswitch
        //networkDevice.goReplay(tape, viewPortSwitch);

        // track the live network on the given viewportswitch
        networkDevice.goLive(liveNetwork, viewPortSwitch);
    }

    /**
     * This method shuts down any services that are still running properly.
     */
    public void finish() {

        // Exit the view
        Platform.exit();

        // Close all databases and other resources accessing the hard drive that need to be closed.
        if (configData != null) {
            configData.close();
        }

        // Disconnect the truffleReceiver
        if (truffleReceiver != null) {
            truffleReceiver.disconnect();
        }

        // Kill all threads and the thread pool with it
        LoggedScheduledExecutor.getInstance().shutdownNow();

        // Shut down the system
       // System.exit(0);
    }

    //TODO remove someday but for now leave as reference
   /* private void initGUI() {

        // setting up main window
        MainViewController mainView = new MainViewController("main_view.fxml");
        Scene mainScene = new Scene(mainView);
        RootWindowController rootWindow = new RootWindowController(primaryStage, mainScene, "icon.png", menuBar);
        //primaryStage.setScene(mainScene);
        //primaryStage.show();
        rootWindow.show();

        final Node node = new NetworkViewScreen(viewPort, 50);

        final AnchorPane pane = new AnchorPane();

        mainView.setCenter(pane);

        final Slider slider = new Slider(0, 100, 0);
        slider.setTooltip(new Tooltip("replay"));
        tape.getCurrentReadingFrameProperty().bindBidirectional(slider.valueProperty());
        tape.getFrameCountProperty().bindBidirectional(slider.maxProperty());

        final ToggleButton liveButton = new ToggleButton("Live");
        liveButton.setDisable(true);
        final ToggleButton playButton = new ToggleButton("Play");
        playButton.setDisable(false);
        final ToggleButton stopButton = new ToggleButton("Stop");
        stopButton.setDisable(false);
        final ToggleButton recButton = new ToggleButton("Rec");
        recButton.setDisable(false);

        liveButton.setOnAction(h -> {
            networkDevice.goLive(liveNetwork, viewPortSwitch);
            liveButton.setDisable(true);
        });

        playButton.setOnAction(handler -> {
            networkDevice.goReplay(tape, viewPortSwitch);
            liveButton.setDisable(false);
        });

        final IUserCommand startRecordCommand = new StartRecordCommand(networkDevice, liveNetwork, tape);
        recButton.setOnAction(h -> startRecordCommand.execute());

        slider.setStyle("-fx-background-color: transparent");

        final ToolBar toolBar = new ToolBar();
        toolBar.getItems().add(stopButton);
        toolBar.getItems().add(playButton);
        toolBar.getItems().add(recButton);
        toolBar.getItems().add(liveButton);
        toolBar.setStyle("-fx-background-color: transparent");
        //  toolBar.getItems().add(slider);

        final FlowPane flowPane = new FlowPane();

        flowPane.getChildren().addAll(toolBar, slider);

        mainView.setBottom(flowPane);

        pane.getChildren().add(node);
        AnchorPane.setBottomAnchor(node, 0d);
        AnchorPane.setTopAnchor(node, 0d);
        AnchorPane.setLeftAnchor(node, 0d);
        AnchorPane.setRightAnchor(node, 0d);


        // setting up general statistics overlay
        OverlayViewController generalStatisticsOverlay = new OverlayViewController("general_statistics_overlay.fxml");
        pane.getChildren().add(generalStatisticsOverlay);
        AnchorPane.setBottomAnchor(generalStatisticsOverlay, 10d);
        AnchorPane.setRightAnchor(generalStatisticsOverlay, 10d);

        // setting up menubar
        ToolBarViewController mainToolBarController = new ToolBarViewController("main_toolbar.fxml");
        pane.getChildren().add(mainToolBarController);

        // setting up node statistics overlay
        OverlayViewController nodeStatisticsOverlay = new OverlayViewController("node_statistics_overlay.fxml");

        nodeStatisticsOverlay.add(new Label("Max Connection Size"), 0, 0);
        nodeStatisticsOverlay.add(new Label("Max Throughput"), 0, 1);

        final PlatformIntegerBinding maxConBinding = new PlatformIntegerBinding(viewPortSwitch.getMaxConnectionSizeProperty());
        final PlatformIntegerBinding maxThroughBinding = new PlatformIntegerBinding(viewPortSwitch.getMaxThroughputProperty());

        final Label connectionSizeLabel = new Label();
        connectionSizeLabel.textProperty().bind(maxConBinding.asString());

        final Label throughputLabel = new Label();
        throughputLabel.textProperty().bind(maxThroughBinding.asString());

        nodeStatisticsOverlay.add(connectionSizeLabel, 1, 0);
        nodeStatisticsOverlay.add(throughputLabel, 1, 1);



        pane.getChildren().add(nodeStatisticsOverlay);
        AnchorPane.setTopAnchor(nodeStatisticsOverlay, 10d);
        AnchorPane.setRightAnchor(nodeStatisticsOverlay, 10d);

    }*/

}