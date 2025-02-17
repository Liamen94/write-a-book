package com.writeabook.texteditor.service;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.web.HTMLEditor;

public class TabService {

    public Tab findTabByEditor(TabPane tabPane, HTMLEditor editor) {
        for (Tab tab : tabPane.getTabs()) {
            if (tab.getContent() == editor) {
                return tab;
            }
        }
        return null;
    }
}