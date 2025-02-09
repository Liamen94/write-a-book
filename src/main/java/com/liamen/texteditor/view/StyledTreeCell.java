package com.liamen.texteditor.view;

import com.liamen.texteditor.model.FileTreeItem;
import javafx.scene.control.TreeCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class StyledTreeCell extends TreeCell<String> {
    private final Image directoryIconEmpty;
    private final Image directoryIconFull;
    private final Image file;

    public StyledTreeCell(Image directoryIconEmpty, Image directoryIconFull, Image file) {
        this.directoryIconEmpty = directoryIconEmpty;
        this.directoryIconFull = directoryIconFull;
        this.file = file;
    }

    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText(null);
            setGraphic(null);
            getStyleClass().remove("directory-cell");
        } else {
            setText(item);
            if (getTreeItem() instanceof FileTreeItem) {
                FileTreeItem fileTreeItem = (FileTreeItem) getTreeItem();
                ImageView imageView;
                if(fileTreeItem.isDirectory()){
                    if(fileTreeItem.getChildren().isEmpty()){
                        getStyleClass().remove("directory-cell");
                        imageView =new ImageView(directoryIconEmpty);
                        getStyleClass().add("empty-directory-cell");
                    } else {
                        getStyleClass().add("directory-cell");
                        imageView = new ImageView(directoryIconFull);
                        getStyleClass().remove("empty-directory-cell");
                    }

                } else {
                    imageView = new ImageView(file);
                }
                imageView.setFitWidth(16);
                imageView.setFitHeight(16);
                setGraphic(imageView);
            }
        }
    }
}