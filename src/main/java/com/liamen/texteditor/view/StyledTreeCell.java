package com.liamen.texteditor.view;

import com.liamen.texteditor.model.FileTreeItem;
import javafx.scene.control.TreeCell;

public class StyledTreeCell extends TreeCell<String> {
    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText(null);
            setGraphic(null);
            getStyleClass().remove("directory-cell");
        } else {
            setText(item);
            if (getTreeItem() != null && !getTreeItem().getChildren().isEmpty()) {
                getStyleClass().add("directory-cell");
                getStyleClass().remove("empty-directory-cell");
            } else if(getTreeItem() instanceof FileTreeItem && ((FileTreeItem) getTreeItem()).isDirectory()) {
                getStyleClass().remove("directory-cell");
                getStyleClass().add("empty-directory-cell");
            }
        }
    }
}