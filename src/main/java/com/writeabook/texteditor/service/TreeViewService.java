package com.writeabook.texteditor.service;

import java.util.Optional;

import com.writeabook.texteditor.model.FileTreeItem;
import com.writeabook.texteditor.model.FileItem;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TreeItem;

//class used to control the TreeView, it exposes method to create and rename item
public class TreeViewService {
    
    //takes a treeItem as a parent and instatiate a new item son of it
    //isDirectory parameter decides if the son is a file or a folder
    //it also avoids duplicated names within the same tree height
    public void createNewItem(TreeItem<String> parent, boolean isDirectory) {
        TreeItem<String> newItem;
        if(isDirectory){
            newItem = new FileTreeItem("new", isDirectory);
        }
        else {
            newItem = new FileItem("new");
        }
        String itemName;
        try {
            itemName = chooseName((FileTreeItem) newItem, false, false);
        } catch (Exception e) {
            return;
        }
        boolean exists = parent.getChildren().stream()
                .filter(child -> child instanceof FileTreeItem)
                .map(child -> (FileTreeItem) child)
                .anyMatch(child -> child.getValue().equals(itemName) && child.isDirectory() == isDirectory);

        if (exists) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "An item with the same name already exists in this level.");
            alert.show();
        } else {
            newItem.setValue(itemName);
            parent.getChildren().add(newItem);
            parent.setExpanded(true);
        }
    }

    //allows renaming an item while avoiding to have duplicated names within the same tree height
    public void renameItem(FileTreeItem item) {
        String itemName;
        String itemOldName = item.getValue();
        String itemNewName;
        try {
            itemName = chooseName(item, true, false);
        } catch (Exception e) {
            itemName = itemOldName;
        }
        itemNewName = itemName;
        boolean exists = item.isRoot() ? false : item.getParent().getChildren().stream()
                .filter(child -> child instanceof FileTreeItem)
                .map(child -> (FileTreeItem) child)
                .anyMatch(child -> child.getValue().equals(itemNewName) && child.isDirectory() == item.isDirectory());

        if (exists) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "An item with the same name already exists in this level.");
            alert.show();
        } else {
            item.setValue(itemName);
        }
    }

    //generic function to prompt the user a name for a new project, a new file or a rename
    public String chooseName(FileTreeItem item, boolean isRename, boolean isNewProject){
        String itemType = item.isDirectory() ? "Directory" : "File";
        String itemName = item.getValue();
        TextInputDialog dialog = new TextInputDialog(itemName);
        if(isRename){
            dialog.setTitle("Rename " + itemName);
            dialog.setHeaderText("Enter the new name of " + itemType.toLowerCase() + ":");
            dialog.setContentText(itemType + " name:");
            //used to avoid error if the given name isn't changed
            item.setValue("");
        } else if (isNewProject){
            dialog.setTitle("Create new project ");
            dialog.setHeaderText("Enter the new name of the project:");
            dialog.setContentText("Project name:");
        } else{
            dialog.setTitle("Create New " + itemType);
            dialog.setHeaderText("Enter the name of the new " + itemType.toLowerCase() + ":");
            dialog.setContentText(itemType + " name:");
        }
        Optional<String> result = dialog.showAndWait();
        String newName = result.get();
        return newName.trim().isEmpty() ? itemName : newName;
    }

    public void deleteItem(FileTreeItem item) {
        if(item.isRoot()){
            Alert alert = new Alert(Alert.AlertType.ERROR, "You can't delete the root.");
            alert.show();
            return;
        }
        if(!item.isLeaf()){
            Alert alert = new Alert(Alert.AlertType.ERROR, "You can't delete a non-empty directory.");
            alert.show();
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete " + (item.isDirectory() ? "Directory" : "File"));
        alert.setHeaderText("Are you sure you want to delete this " + (item.isDirectory() ? "directory" : "file") + "?");
        alert.setContentText(item.getValue());

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            TreeItem<String> parent = item.getParent();
            parent.getChildren().remove(item);   
        }
    }

}