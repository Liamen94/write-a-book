package com.liamen.texteditor.controller;

import com.liamen.texteditor.model.FileTreeItem;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import java.util.Optional;

public class EditorController {
    @FXML
    private TextArea textArea;

    @FXML
    private TreeView<String> projectTreeView;

    @FXML
    public void initialize() {
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

        // Add listener to the tree view in order to disable root text area
        projectTreeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                textArea.setDisable(((FileTreeItem) newValue).isDirectory());
            }
        });
    }

    // Simple method to add a new child, both file or directory
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
                TreeItem<String> parent = selectedItem.getParent();
                FileTreeItem newItem = new FileTreeItem(itemName, isDirectory);
                if (selectedItem.isDirectory()) {
                    selectedItem.getChildren().add(newItem);
                    selectedItem.setExpanded(true);
                } else if (parent != null) {
                    parent.getChildren().add(newItem);
                }
            }
            else{
                FileTreeItem newItem = new FileTreeItem(itemName, isDirectory);
                projectTreeView.getRoot().getChildren().add(newItem);
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
