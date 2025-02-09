package com.liamen.texteditor.controller;

import com.liamen.texteditor.model.FileTreeItem;
import com.liamen.texteditor.view.StyledTreeCell;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

public class EditorController {
    @FXML
    private BorderPane mainPane;

    @FXML
    private TextArea textArea;
    private Node infoPane;

    @FXML
    private TreeView<String> projectTreeView;

    @FXML
    public void initialize() {

        textArea = new TextArea();

        try {
            infoPane = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/infoPane.fxml")));
        } catch (IOException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
            System.out.println("Error loading infoPane.fxml");
        }

        //load stylesheet
        mainPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm());
        // Initialize root
        FileTreeItem rootItem = new FileTreeItem("Project", true);
        rootItem.setExpanded(true);

        // Add some basic child
        FileTreeItem item1 = new FileTreeItem("File1.txt", false);
        FileTreeItem item2 = new FileTreeItem("File2.txt", false);
        FileTreeItem dir1 = new FileTreeItem("Directory1", true);

        rootItem.getChildren().addAll(item1, item2, dir1);

        // Set the root on the view
        projectTreeView.setRoot(rootItem);
        projectTreeView.setShowRoot(true);

        projectTreeView.setCellFactory(treeView -> {
            TreeCell<String> cell = new StyledTreeCell();
            ContextMenu contextMenu = new ContextMenu();
            Menu newMenu = new Menu("New");
            MenuItem newFileItem = new MenuItem("New File");
            newFileItem.setOnAction(event-> createNewFile());
            MenuItem newDirItem = new MenuItem("New Directory");
            newDirItem.setOnAction(event-> createNewDirectory());
            newMenu.getItems().addAll(newFileItem, newDirItem);
            contextMenu.getItems().add(newMenu);

            cell.setOnMouseClicked(event ->{
                if(event.getButton() == MouseButton.SECONDARY ){
                    cell.setContextMenu(contextMenu);
                }
            });
            return cell;
        });

        // Add listener to the tree view in order to disable directory text area
        projectTreeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                FileTreeItem selectedItem = (FileTreeItem) newValue;
                if (selectedItem.isDirectory()) {
                    mainPane.setCenter(infoPane);
                } else {
                    mainPane.setCenter(textArea);
                }
            }
        });
    }

    // Methods to add child in the tree view
    private void createNewItem(boolean isDirectory) {
        String itemType = isDirectory ? "Directory" : "File";
        TextInputDialog dialog = new TextInputDialog(isDirectory ? "NewDirectory" : "NewFile.txt");
        dialog.setTitle("Create New " + itemType);
        dialog.setHeaderText("Enter the name of the new " + itemType.toLowerCase() + ":");
        dialog.setContentText(itemType + " name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(itemName -> {
            FileTreeItem selectedItem = (FileTreeItem) projectTreeView.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                TreeItem<String> parent = selectedItem.isDirectory() ? selectedItem : selectedItem.getParent();
                if (parent != null) {
                    boolean exists = parent.getChildren().stream()
                            .filter(child -> child instanceof FileTreeItem)
                            .map(child -> (FileTreeItem) child)
                            .anyMatch(child -> child.getValue().equals(itemName) && child.isDirectory() == isDirectory);

                    if (exists) {
                        Alert alert = new Alert(Alert.AlertType.ERROR, "An item with the same name already exists in this level.");
                        alert.show();
                    } else {
                        FileTreeItem newItem = new FileTreeItem(itemName, isDirectory);
                        parent.getChildren().add(newItem);
                        parent.setExpanded(true);
                    }
                }
            }
        });
    }

    @FXML
    private void createNewFile() {
        createNewItem(false);
    }

    @FXML
    private void createNewDirectory() {
        createNewItem(true);
    }


}
