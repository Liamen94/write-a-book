package com.liamen.texteditor.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.liamen.texteditor.model.FileTreeItem;
import javafx.scene.web.HTMLEditor;
import javafx.scene.control.TreeItem;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class FileService {
    private Map<String, HTMLEditor> editorsMap = new HashMap<>();

    public void saveProject(File selectedFile, FileTreeItem rootItem) throws IOException {
        try (FileWriter writer = new FileWriter(selectedFile)) {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode rootNode = mapper.createObjectNode();
            ObjectNode rootContent = rootNode.putObject(rootItem.getValue());
            rootContent.put("isDirectory", rootItem.isDirectory());
            saveTree(rootContent, rootItem);
            mapper.writerWithDefaultPrettyPrinter().writeValue(writer, rootNode);
        }
    }

    public FileTreeItem loadProject(File selectedFile) throws IOException {
        try (FileReader reader = new FileReader(selectedFile)) {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode rootNode = (ObjectNode) mapper.readTree(reader);
            String rootName = rootNode.fieldNames().next();
            ObjectNode rootContent = (ObjectNode) rootNode.get(rootName);
            boolean isDirectory = rootContent.get("isDirectory").asBoolean();
            FileTreeItem rootItem = new FileTreeItem(rootName, isDirectory);
            loadTree(rootContent, rootItem);
            return rootItem;
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

    public HTMLEditor getEditor(String fullPath) {
        return editorsMap.get(fullPath);
    }

    public void addEditor(String fullPath, HTMLEditor editor) {
        editorsMap.put(fullPath, editor);
    }

    public void removeEditor(String fullPath) {
        editorsMap.remove(fullPath);
    }

    public void clearEditors() {
        editorsMap.clear();
    }

    public String getFilePathByEditor(HTMLEditor editor) {
        for (Map.Entry<String, HTMLEditor> entry : editorsMap.entrySet()) {
            if (entry.getValue().equals(editor)) {
                return entry.getKey();
            }
        }
        return null;
    }
}