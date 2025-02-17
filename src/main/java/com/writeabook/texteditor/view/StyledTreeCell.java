package com.writeabook.texteditor.view;

import com.writeabook.texteditor.model.FileTreeItem;
import javafx.scene.control.TreeCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class StyledTreeCell extends TreeCell<String> {
    private final Image directoryIconEmpty;
    private final Image directoryIconFull;
    private final Image fileIcon;
    private final Image rootIcon;

    public StyledTreeCell(Image directoryIconEmpty, Image directoryIconFull, Image fileIcon, Image rootIcon) {
        this.directoryIconEmpty = directoryIconEmpty;
        this.directoryIconFull = directoryIconFull;
        this.fileIcon = fileIcon;
        this.rootIcon = rootIcon;
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
                    if(fileTreeItem.isRoot()){
                        getStyleClass().add("root-cell");
                        imageView = new ImageView(rootIcon);
                    } else if(fileTreeItem.getChildren().isEmpty()){
                        getStyleClass().remove("directory-cell");
                        imageView =new ImageView(directoryIconEmpty);
                        getStyleClass().add("empty-directory-cell");
                    } else {
                        getStyleClass().add("directory-cell");
                        imageView = new ImageView(directoryIconFull);
                        getStyleClass().remove("empty-directory-cell");
                    }
                } else {
                    imageView = new ImageView(fileIcon);
                }
                imageView.setFitWidth(16);
                imageView.setFitHeight(16);
                setGraphic(imageView);
            }
        }
    }
}