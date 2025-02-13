package com.liamen.texteditor.controller;

import com.liamen.texteditor.model.FileTreeItem;
import com.liamen.texteditor.service.*;
import com.liamen.texteditor.view.StyledTreeCell;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.HTMLEditor;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class EditorController {
    @FXML
    private BorderPane mainPane;

    @FXML
    private TabPane mainTabPane;

    @FXML
    private Node infoPane;
    private Node rootPane;

    @FXML
    private TreeView<String> projectTreeView;
    
    @FXML
    private MenuItem newFileMenu;
    @FXML
    private MenuItem newDirMenu;
    @FXML
    private MenuItem saveMenu;

    private final FileService fileService = new FileService();
    private final TreeViewService treeViewService = new TreeViewService();
    private final TabService tabService = new TabService();

    private final Image directoryIconEmpty;
    private final Image directoryIconFull;
    private final Image fileIcon;
    private final Image rootIcon;

    public EditorController() {
        directoryIconEmpty = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/icons/empty_dir.png")));
        directoryIconFull = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/icons/dir.png")));
        fileIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/icons/file.png")));
        rootIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/icons/root.png")));
    }

    @FXML
    public void initialize() {

        try {
            infoPane = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/infoPane.fxml")));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error loading infoPane.fxml");
        }

        try {
            rootPane = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/rootPane.fxml")));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error loading rootPane.fxml");
        }

        disableMenus();

        projectTreeView.setCellFactory(_ -> {
            TreeCell<String> cell = new StyledTreeCell(directoryIconEmpty, directoryIconFull, fileIcon, rootIcon);
            ContextMenu contextMenu = new ContextMenu();
            Menu newMenu = new Menu("New");
            MenuItem newFileItem = new MenuItem("New File");
            newFileItem.setOnAction(_ -> {
                createNewFile();
            });
            MenuItem newDirItem = new MenuItem("New Directory");
            newDirItem.setOnAction(_ -> {
                createNewDirectory();
            });
            MenuItem renameItem = new MenuItem("Rename");
            renameItem.setOnAction(_ -> {
                FileTreeItem selectedItem = (FileTreeItem) cell.getTreeItem();
                renameItem(selectedItem);
            });
            newMenu.getItems().addAll(newFileItem, newDirItem);
            contextMenu.getItems().addAll(newMenu, renameItem);

            cell.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.SECONDARY) {
                    FileTreeItem selectedItem = (FileTreeItem) cell.getTreeItem();
                    newFileItem.setDisable(projectTreeView.getRoot() == null);
                    newDirItem.setDisable(projectTreeView.getRoot() == null);
                    renameItem.setDisable(selectedItem == null);
                    cell.setContextMenu(contextMenu);
                }
            });
            return cell;
        });


        projectTreeView.getSelectionModel().selectedItemProperty().addListener((_, _, newValue) -> {
            if (newValue != null) {
                FileTreeItem selectedItem = (FileTreeItem) newValue;
                if (selectedItem.isDirectory()) {
                    mainPane.setCenter(selectedItem.isRoot() ? rootPane : infoPane);
                } else {
                    mainPane.setCenter(mainTabPane);
                    showFileContent(selectedItem);
                }
            }
        });
    }

    private void showFileContent(FileTreeItem fileItem) {
        String fullPath = fileItem.getFullPath();
        HTMLEditor editor;
        Tab tab;

        if (fileService.getEditor(fullPath) != null) {
            editor = fileService.getEditor(fullPath);
            tab = tabService.findTabByEditor(mainTabPane, editor);
            if (tab == null) {
                tab = new Tab(fileItem.getValue(), editor);
                tab.setOnClosed(_ -> fileService.removeEditor(fullPath));
                mainTabPane.getTabs().add(tab);
            }
        } else {
            editor = new HTMLEditor();
            fileService.addEditor(fullPath, editor);
            tab = new Tab(fileItem.getValue(), editor);
            tab.setOnClosed(_ -> fileService.removeEditor(fullPath));
            mainTabPane.getTabs().add(tab);
        }

        mainTabPane.getSelectionModel().select(tab);
    }

    @FXML
    private void createNewFile() {
        FileTreeItem selectedItem = (FileTreeItem) projectTreeView.getSelectionModel().getSelectedItem();
        boolean isDirectory = selectedItem.isDirectory();
        if(isDirectory){
            treeViewService.createNewItem(selectedItem, false);
        }
        else {
            treeViewService.createNewItem(selectedItem.getParent(), false);
        }
    }

    @FXML
    private void createNewDirectory() {
        FileTreeItem selectedItem = (FileTreeItem) projectTreeView.getSelectionModel().getSelectedItem();
        boolean isDirectory = selectedItem.isDirectory();
        if(isDirectory){
            treeViewService.createNewItem(selectedItem, true);
        }
        else {
            treeViewService.createNewItem(selectedItem.getParent(), true);
        }
    }

    private void renameItem(FileTreeItem item) {
        String itemName = item.getValue();
        treeViewService.renameItem(item);
        String itemNewName = item.getValue();
        // Editors map update
        String oldFullPath = item.getFullPath().replace(itemNewName, itemName);
        HTMLEditor editor = fileService.getEditor(oldFullPath);
        fileService.removeEditor(oldFullPath);
        fileService.addEditor(item.getFullPath(), editor);
        // Tab update
        Tab tab = tabService.findTabByEditor(mainTabPane, editor);
        if (tab != null) {
                tab.setText(itemNewName);
            }
        }
                
            


    @FXML
    private void newProject() {
        FileTreeItem root = new FileTreeItem("New Project", true);
        root.setValue(treeViewService.chooseName(root, false, true));
        root.setExpanded(true);
        root.setRoot(true);
        enableMenus();
        projectTreeView.setRoot(root);
        mainTabPane.getTabs().clear();
        fileService.clearEditors();
    }

    @FXML
    private void openProject() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Project File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Write A Book Project Files (.wabop)", "*.wabop"));
        File selectedFile = fileChooser.showOpenDialog(mainPane.getScene().getWindow());

        if (selectedFile != null) {
            try {
                FileTreeItem rootItem = fileService.loadProject(selectedFile);
                rootItem.setRoot(true);
                projectTreeView.setRoot(rootItem);
                mainTabPane.getTabs().clear();
                fileService.clearEditors();
                enableMenus();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void saveProject() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Project File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Write A Book Project Files (.wabop)", "*.wabop"));
        File selectedFile = fileChooser.showSaveDialog(mainPane.getScene().getWindow());

        if (selectedFile != null) {
            try {
                FileTreeItem rootItem = (FileTreeItem) projectTreeView.getRoot();
                fileService.saveProject(selectedFile, rootItem);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void disableMenus(){
        newFileMenu.setDisable(true);
        newDirMenu.setDisable(true);
        saveMenu.setDisable(true);
    }

    private void enableMenus(){
        newFileMenu.setDisable(false);
        newDirMenu.setDisable(false);
        saveMenu.setDisable(false);
    }
}
