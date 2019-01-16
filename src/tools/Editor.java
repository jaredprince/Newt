package tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class Editor extends Application {

	final FileChooser fileChooser = new FileChooser();
	ToolBar toolBar = new ToolBar();

	@Override
	public void start(Stage primaryStage) throws Exception {

		VBox vBox = new VBox();
		ObservableList<Node> list = vBox.getChildren();

		MenuBar menuBar = new MenuBar();
		TabPane tabPane = new TabPane();

		tabPane.prefWidthProperty().bind(vBox.widthProperty());
		tabPane.prefHeightProperty().bind(vBox.heightProperty());

		tabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
			@Override
			public void changed(ObservableValue<? extends Tab> ov, Tab t, Tab t1) {
				rebindToolbar(t1);

				//disable save options if there is no selection
				if(t1 == null) {
					menuBar.getMenus().get(0).getItems().get(2).setDisable(true);
					menuBar.getMenus().get(0).getItems().get(3).setDisable(true);
				} else {
					//save is enabled if the selection is a previously saved file
					if(tabPane.getSelectionModel().getSelectedItem().getTooltip() != null) {
						menuBar.getMenus().get(0).getItems().get(2).setDisable(false);
					} else {
						menuBar.getMenus().get(0).getItems().get(2).setDisable(true);
					}
					
					//save as is enabled if there is a selection
					menuBar.getMenus().get(0).getItems().get(3).setDisable(false);
				}
			}
		});

		Menu Menu = new Menu("Menu");

		menuBar.getMenus().addAll(fileMenu(primaryStage, tabPane), Menu);

		toolBar.setPrefHeight(75);

		Text chars = new Text(0, 0, "");
		chars.setFont(new Font(16));

		Text lines = new Text(0, 0, "");
		lines.setFont(new Font(16));

		toolBar.getItems().add(chars);
		toolBar.getItems().add(lines);

		// Adding all the nodes to the observable list
		list.addAll(menuBar, tabPane, toolBar);

		// Creating a Scene by passing the group object, height and width
		Scene scene = new Scene(vBox, 1600, 900);

		// setting color to the scene
		scene.setFill(Color.FLORALWHITE);

		// Setting the title to Stage.
		primaryStage.setTitle("Salamander");

		// Adding the scene to Stage
		primaryStage.setScene(scene);

		// Displaying the contents of the stage
		primaryStage.show();
	}

	public static void main(String args[]) {
		launch(args);
	}

	public Menu fileMenu(Stage stage, TabPane pane) {
		Menu fileMenu = new Menu("File");

		MenuItem open = new MenuItem("Open");
		MenuItem newFile = new MenuItem("New");
		MenuItem save = new MenuItem("Save");
		MenuItem saveAs = new MenuItem("Save As");
		save.setDisable(true);
		saveAs.setDisable(true);

		open.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				File file = fileChooser.showOpenDialog(stage);
				if (file != null) {
					openFile(file, pane);
				}
			}
		});
		
		newFile.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				newTab(pane);
			}
		});
		
		save.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
                try {
                    FileWriter fw = new FileWriter(new File(pane.getSelectionModel().getSelectedItem().getTooltip().getText()));
                    //TODO: finish
                    
                    fw.write(pane.getSelectionModel().getSelectedItem().getText());
                    fw.close();
                } catch (IOException ex) {
                    System.out.println(ex.getMessage());
                }
			}
		});
		
		saveAs.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				fileChooser.setTitle("Save File");

	            File file = fileChooser.showSaveDialog(stage);
	            if (file != null) {
	                try {
	                    FileWriter fw = new FileWriter(file);
	                    //TODO: finish
	                    
	                    fw.close();
	                } catch (IOException ex) {
	                    System.out.println(ex.getMessage());
	                }
	            }
			}
		});
		
		
		fileMenu.getItems().addAll(open, newFile, save, saveAs);
		return fileMenu;
	}

	public void openFile(File file, TabPane pane) {
		// create Tab
		Tab tab = new Tab(file.getName());

		TextArea text = new TextArea();
		text.setWrapText(false);
//        text.setStyle("-fx-text-fill: red");

		text.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(final ObservableValue<? extends String> observable, final String oldValue,
					final String newValue) {
			}
		});

		Scanner fileIn;
		try {
			fileIn = new Scanner(file);

			String fileText = "";
			while (fileIn.hasNextLine()) {
				fileText += "\n" + fileIn.nextLine();
			}

			text.setText(fileText);
			tab.setContent(text);
			tab.setTooltip(new Tooltip(file.toString()));

			pane.getTabs().add(tab);
			pane.getSelectionModel().select(tab);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void newTab(TabPane pane) {
		Tab tab = new Tab("Untitled");
		
		TextArea text = new TextArea();
		text.setWrapText(false);
		
		tab.setContent(text);
		
		pane.getTabs().add(tab);
		pane.getSelectionModel().select(tab);
	}

	public void rebindToolbar(Tab tab) {
		if(tab == null) {
			((Text) toolBar.getItems().get(0)).textProperty().unbind();
			((Text) toolBar.getItems().get(0)).setText("");
			return;
		}
		
		((Text) toolBar.getItems().get(0)).textProperty()
				.bind(Bindings.length(((TextArea) tab.getContent()).textProperty()).asString("Characters: %d"));
	}
}