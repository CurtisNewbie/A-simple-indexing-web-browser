package com.curtisnewbie.view;

import javafx.scene.layout.BorderPane;
import javafx.scene.control.Tab;
import javafx.scene.web.WebView;
import javafx.scene.control.TabPane;

/**
 * <p>
 * This program has two views (or cards of CardLayout) in Javafx, one is for
 * viewing the content of the webpages, and another one is for using query to
 * seaarch contents.
 * </p>
 * <p>
 * This class itself is a subclass of BorderPane, and it represents one of the
 * view in this program for displaying content of webpages.
 * </p>
 * 
 * @see BrowserView
 */
public class DisplayPane extends BorderPane {

    /**
     * It's a HBox that contains some of the control components/nodes in this pane
     * 
     * @see UrlInputBox
     */
    private UrlInputBox urlInputbox;

    /**
     * TabPane where each tab shows the content of a web page
     */
    private TabPane tabPane;

    /**
     * Instantiate DisplayPane
     * 
     * @param menuBtn the menu for this view (it can be universal for the whole
     *                program if necessary)
     * @see DisplayPane
     * @see MenuButton
     */
    public DisplayPane() {
        this.urlInputbox = new UrlInputBox();
        this.setTop(urlInputbox);
        this.tabPane = new TabPane();
        this.setCenter(tabPane);

        // for testing
        var v = new WebView();
        v.getEngine().load("https://www.google.com");
        var tb = new Tab();
        tb.setContent(v);
        tabPane.getTabs().add(tb);
    }

    /**
     * Create a new {@code Tab} that loads the webpage of the given url. It doesn
     * not create new tab if the given url is {@code NULL} or of a length of 0.
     * However, if the given url is invalid (i.e., doesn't exist), it will simply
     * create a empty tab with no content in it.
     * 
     * @param url a URL String
     * 
     */
    public void addTab(String url) {
        if (url != null && !url.isEmpty()) {
            Tab tab = new Tab();
            var view = new WebView();
            view.getEngine().load(url);
            tab.setContent(view);
            tabPane.getTabs().add(tab);
        }
    }

    /**
     * Get the UrlInputBox which contains the TextField for entering URL, and the
     * Buttons for going back and forth between the previously viewed web pages.
     * 
     * @return an object of UrlInputBox
     * @see UrlInputBox
     */
    public UrlInputBox getUrlInputBox() {
        return this.urlInputbox;
    }

    public Tab getCurrentTab() {
        return this.tabPane.getSelectionModel().getSelectedItem();
    }

}
