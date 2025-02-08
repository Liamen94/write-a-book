package com.liamen.texteditor.model;

import javafx.scene.control.TreeItem;

public class FileTreeItem extends TreeItem<String> {
    private boolean isDirectory;

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
}