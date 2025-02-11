package com.liamen.texteditor.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

public class FileTreeItem extends TreeItem<String> {
    private boolean isDirectory;
    private boolean isRoot;
    private boolean isFirstTimeChildren= true;

    public FileTreeItem(String value, boolean isDirectory) {
        super(value);
        this.isDirectory = isDirectory;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public void setDirectory(boolean isDirectory) {
        this.isDirectory = isDirectory;
    }

    public boolean isRoot() {
        return isRoot;
    }

    public void setRoot(boolean root) {
        isRoot = root;
    }

    @Override
    public ObservableList<TreeItem<String>> getChildren() {
        if(isFirstTimeChildren){
            isFirstTimeChildren = false;
            super.getChildren().setAll(buildChildren(this));
        }
        return super.getChildren();
    }

        private ObservableList<TreeItem<String>> buildChildren(TreeItem<String> treeItem) {
        List<TreeItem<String>> children = new ArrayList<>();
        if (isDirectory) {
            // Carica i file e le directory figli
            File dir = new File(getValue());
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    FileTreeItem childItem = new FileTreeItem(file.getName(), file.isDirectory());
                    children.add(childItem);
                }
            }
        }
        return FXCollections.observableArrayList(children);
    }

    public String getFullPath() {
        StringBuilder fullPath = new StringBuilder(getValue());
        TreeItem<String> parent = getParent();
        while (parent != null) {
            fullPath.insert(0, parent.getValue() + "/");
            parent = parent.getParent();
        }
        return fullPath.toString();
    }
}