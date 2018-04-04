package com.g2soft.g2portal;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;

import com.g2soft.g2portal.model.Apps;
import com.g2soft.g2portal.ui.MainMenu;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class Main extends Application {
	
    private Pane splashLayout;
    private ProgressBar loadProgress;
    private Label progressText;
    private ObservableList<Apps> apps;
    private static final int SPLASH_WIDTH = 676;
    private static final int SPLASH_HEIGHT = 227;
    final Stage initStage = new Stage();
	
	public static final LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);
	
	public static void main(String[] args) {
		File fileConfigLogger = new File("C:/G2 Soft/logs/log4j2.xml");
		if (fileConfigLogger.exists())
			loggerContext.setConfigLocation(fileConfigLogger.toURI());
		launch(args);
	}

	@Override
	public void init() throws Exception {
		ImageView splash = new ImageView(new Image(
                getClass().getResource("/images/painel_g2.jpg").toExternalForm()
        ));
        loadProgress = new ProgressBar();
        loadProgress.setPrefWidth(SPLASH_WIDTH - 20);
        progressText = new Label("Carregando . . .");
        splashLayout = new VBox();
        splashLayout.getChildren().addAll(splash, loadProgress, progressText);
        progressText.setAlignment(Pos.CENTER);
        splashLayout.setStyle(
                "-fx-padding: 5; " +
                "-fx-background-color: white; " +
                "-fx-border-width:5; " +
                "-fx-border-color: " +
                    "linear-gradient(" +
                        "to bottom, " +
                        "chocolate, " +
                        "derive(chocolate, 50%)" +
                    ");"
        );
        splashLayout.setEffect(new DropShadow());
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		final Task<ObservableList<Apps>> friendTask = new Task<ObservableList<Apps>>() {

			@Override
            protected ObservableList<Apps> call() throws InterruptedException {
            	apps = FXCollections.<Apps>observableArrayList();
            	
            	updateMessage("Carregando . . .");
                Thread.sleep(400);
                updateMessage("Carregado");

                return apps;
            }
        };

        /*showSplash(
                initStage,
                friendTask,
                () -> showMainStage(friendTask.valueProperty())
        );*/
        showSplash(
                initStage,
                friendTask,
                new InitCompletionHandler() {
					
					@Override
					public void complete() {
						showMainStage(friendTask.valueProperty());
					}
				});
        
        new Thread(friendTask).start();
	}
	
	private void showMainStage(
            ReadOnlyObjectProperty<ObservableList<Apps>> appsObservableList
    ) {
		List<Apps> apps = new ArrayList<>();
		for (int i = 0; i < appsObservableList.get().size(); i++) {
			apps.add(appsObservableList.get().get(i));
		}
		
		MainMenu mainMenu = new MainMenu();
		mainMenu.drawMainMenu();
    }
	
	private <T> void showSplash(
            final Stage initStage,
            Task<?> task,
            final InitCompletionHandler initCompletionHandler
    ) {
        progressText.textProperty().bind(task.messageProperty());
        loadProgress.progressProperty().bind(task.progressProperty());
        task.stateProperty().addListener((ChangeListener<? super State>) new ChangeListener<State>() {

			@Override
			public void changed(ObservableValue<? extends State> observable, State oldValue, State newValue) {
				// TODO Auto-generated method stub
				if (newValue == Worker.State.SUCCEEDED) {
	                loadProgress.progressProperty().unbind();
	                loadProgress.setProgress(1);
	                initStage.toFront();
	                final FadeTransition fadeSplash = new FadeTransition(Duration.seconds(1.2), splashLayout);
	                fadeSplash.setFromValue(1.0);
	                fadeSplash.setToValue(0.0);
	                fadeSplash.setOnFinished(new EventHandler<ActionEvent>() {
						
						@Override
						public void handle(ActionEvent event) {
							fadeSplash.stop();
							initStage.hide();
						}
					});
	                fadeSplash.play();                
	                initCompletionHandler.complete();
	            } 
			}
		});

        Scene splashScene = new Scene(splashLayout, Color.WHITE);
        final Rectangle2D bounds = Screen.getPrimary().getBounds();
        initStage.setScene(splashScene);
        initStage.setX(bounds.getMinX() + bounds.getWidth() / 2 - SPLASH_WIDTH / 2);
        initStage.setY(bounds.getMinY() + bounds.getHeight() / 2 - SPLASH_HEIGHT / 2);
        initStage.initStyle(StageStyle.TRANSPARENT);
        initStage.setAlwaysOnTop(true);
        initStage.show();
    }

    public interface InitCompletionHandler {
        void complete();
    }
	
}