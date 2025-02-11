package com.liamen.texteditor.controller;

import com.liamen.texteditor.model.FileTreeItem;
import com.liamen.texteditor.view.StyledTreeCell;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.HTMLEditor;
import javafx.stage.FileChooser;

import java.io.*;
import java.util.*;

public class EditorController {
    @FXML
    private BorderPane mainPane;

    @FXML
    private HTMLEditor htmlEditor;

    @FXML
    private TabPane mainTabPane;

    @FXML
    private VBox textEditorTab;

    @FXML
    private Node infoPane;
    private Node rootPane;

    @FXML
    private TreeView<String> projectTreeView;

    private Map<String, HTMLEditor> editorsMap = new HashMap<>();

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



    @SuppressWarnings("unused")
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

        //load stylesheet
        mainPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm());

        mainPane.setCenter(null);


        projectTreeView.setCellFactory(_treeView -> {
            TreeCell<String> cell = new StyledTreeCell(directoryIconEmpty, directoryIconFull, fileIcon, rootIcon);
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

        // Add listener to the tree view in order to disable item text area
        projectTreeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                FileTreeItem selectedItem = (FileTreeItem) newValue;
                if (selectedItem.isDirectory()) {
                    if(selectedItem.isRoot()){
                        mainPane.setCenter(rootPane);
                    } else {
                        mainPane.setCenter(infoPane);
                    }
                } else {
                    mainPane.setCenter(mainTabPane);
                    showFileContent(selectedItem);
                }
            }
        });

        mainTabPane.getTabs().addListener((ListChangeListener<Tab>) c -> {
        while (c.next()) {
            if (c.wasRemoved()) {
                for (Tab tab : c.getRemoved()) {
                    HTMLEditor editor = (HTMLEditor) tab.getContent();
                    String filePath = getFilePathByEditor(editor);
                    if (filePath != null) {
                        editorsMap.put(filePath, editor);
                    }
                }
            }
        }
    });
}


    

    private void showFileContent(FileTreeItem fileItem) {
        String filePath = fileItem.getFullPath();
        HTMLEditor editor;
        Tab tab;

        if (editorsMap.containsKey(filePath)) {
            editor = editorsMap.get(filePath);
            tab = findTabByEditor(editor);
        } else {
            editor = new HTMLEditor();
            editorsMap.put(filePath, editor);
            tab = new Tab(fileItem.getValue(), editor);
            tab.setOnClosed(_ -> editorsMap.remove(filePath)); // Rimuove l'editor dalla mappa quando la scheda viene chiusa
            mainTabPane.getTabs().add(tab);
        }

        mainTabPane.getSelectionModel().select(tab);
    }

    private Tab findTabByEditor(HTMLEditor editor) {
        for (Tab tab : mainTabPane.getTabs()) {
            if (tab.getContent() == editor) {
                return tab;
            }
        }
        return null;
    }

    private void setIcon(FileTreeItem item) {
        if(item.isDirectory()){
            if (item.getChildren().isEmpty()) {
                item.setGraphic(new ImageView(directoryIconEmpty));
            } else {
                item.setGraphic(new ImageView(directoryIconFull));
            }
        } else {
            item.setGraphic(new ImageView((fileIcon)));
        }
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
                        if (isDirectory) {
                            setIcon(newItem);
                        }
                        parent.getChildren().add(newItem);
                        parent.setExpanded(true);
                        if (parent instanceof FileTreeItem){
                            setIcon((FileTreeItem) parent);
                        }
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

    @FXML
    private void newProject() {
        FileTreeItem root = new FileTreeItem("New Project",true);
        root.setRoot(true);
        projectTreeView.setRoot(root);
        mainPane.setCenter(rootPane);
        mainTabPane.getTabs().clear();
        editorsMap.clear();
    }

    @FXML
    private void openProject() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Project File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Write A Book Project Files (.wabop)", "*.wabop"));
        File selectedFile = fileChooser.showOpenDialog(mainPane.getScene().getWindow());

        if (selectedFile != null) {
            try (FileReader reader = new FileReader(selectedFile)) {
                ObjectMapper mapper = new ObjectMapper();
                ObjectNode rootNode = (ObjectNode) mapper.readTree(reader);

                FileTreeItem rootItem = new FileTreeItem(rootNode.fieldNames().next(), true);
                rootItem.setRoot(true);
                loadTree((ObjectNode) rootNode.get(rootItem.getValue()), rootItem);
                projectTreeView.setRoot(rootItem);
                editorsMap.clear();
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
            try (FileWriter writer = new FileWriter(selectedFile)) {
                ObjectMapper mapper = new ObjectMapper();
                ObjectNode rootNode = mapper.createObjectNode();
                FileTreeItem rootItem = (FileTreeItem) projectTreeView.getRoot();
                ObjectNode rootContent = rootNode.putObject(rootItem.getValue());
                rootContent.put("isDirectory", rootItem.isDirectory());
                saveTree(rootContent, rootItem);
                mapper.writerWithDefaultPrettyPrinter().writeValue(writer, rootNode);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadTree(ObjectNode jsonNode, FileTreeItem parentItem) {
        Iterator<String> fieldNames = jsonNode.fieldNames();
        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();
            JsonNode childNode = jsonNode.get(fieldName);
            
            if (childNode.isObject()) {
                ObjectNode childObjectNode = (ObjectNode) childNode;
                boolean isDirectory = childObjectNode.get("isDirectory").asBoolean();
                FileTreeItem newItem = new FileTreeItem(fieldName, isDirectory);
                parentItem.getChildren().add(newItem);
            
                 if (isDirectory) {
                    loadTree(childObjectNode, newItem);
                } else {
                    HTMLEditor editor = new HTMLEditor();
                    editor.setHtmlText(childObjectNode.get("content").asText());
                    editorsMap.put(newItem.getFullPath(), editor);
                }
        }
    }
}

    private void saveTree(ObjectNode jsonNode, FileTreeItem parentItem) {
        for (TreeItem<String> child : parentItem.getChildren()) {
            FileTreeItem fileTreeItem = (FileTreeItem) child;
            ObjectNode childNode = jsonNode.putObject(fileTreeItem.getValue());
            childNode.put("isDirectory", fileTreeItem.isDirectory());
            if (fileTreeItem.isDirectory()) {
                saveTree(childNode, fileTreeItem);
            } else {
                HTMLEditor editor = editorsMap.get(fileTreeItem.getFullPath());
                childNode.put("content", editor != null ? editor.getHtmlText() : "");
            }
        }
    }

    private String getFilePathByEditor(HTMLEditor editor) {
        for (Map.Entry<String, HTMLEditor> entry : editorsMap.entrySet()) {
            if (entry.getValue().equals(editor)) {
                return entry.getKey();
            }
        }
        return null;
    }
}


