package com.writeabook.texteditor.model;

import javafx.scene.web.HTMLEditor;

public class FileItem extends FileTreeItem {
    private HTMLEditor editor;

    public FileItem(String name){
        super(name, false);
        editor = new HTMLEditor();
    }

    public HTMLEditor getEditor(){
        return editor;
    }

    public void setEditor(HTMLEditor editor){
        this.editor = editor;
    }
}
