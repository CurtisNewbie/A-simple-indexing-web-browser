package com.curtisnewbie.controller;

import com.curtisnewbie.config.*;
import com.curtisnewbie.view.*;
import com.curtisnewbie.webBrowserModel.WebDoc;
import com.curtisnewbie.webBrowserModel.WebIndexForBody;
import com.curtisnewbie.webBrowserModel.WebIndexForHead;

import com.curtisnewbie.webBrowserModel.Query;
import com.curtisnewbie.webBrowserModel.QueryBuilder;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.event.*;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

import org.w3c.dom.Document;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * Controller in MVC, this controller controls how the view and the model
 * interact with eachother.
 * 
 * @author Yongjie Zhuang
 */
public class BrowserController {

    private final String DEF_URL = "https://www.google.com";

    private BrowserView view;
    private String default_url;
    private WebIndexForHead headIndex;
    private WebIndexForBody bodyIndex;

    /** Map used to keep track of WebDoc that has never been visited */
    private Map<String, WebDoc> allWebDocs;

    /** transformer for transform Document to HTML text */
    private Transformer transformer;

    public BrowserController(BrowserView view) {
        // initialise components
        this.view = view;
        this.headIndex = new WebIndexForHead();
        this.bodyIndex = new WebIndexForBody();
        this.allWebDocs = new HashMap<>();
        this.default_url = DEF_URL;

        // register EventHandlers
        this.view.addMenuEventHandlers(createMenuEventHandlers());
        this.view.addUrlLoadingEventHandler(createUrlLoadingEventHandler());
        this.view.addNewTabHandler(createNewTabEventHandler());
        this.view.addBackTrackBtnHandler(createBackwordBtnHandler());
        this.view.addForwardBtnHandler(createForwardBtnHandler());
        this.view.addInfixQueryHandler(createInfixQueryHandler());
        this.view.addPrefixQueryHandler(createPrefixQueryHandler());
    }

    public BrowserController(BrowserView view, Map<String, String> configMap) {
        this(view);
        if (configMap != null) {
            default_url = configMap.get(Config.CONFIG_URL);
        }
    }

    /**
     * Create a list of EventHandlers for Menu
     * 
     * @return List of {@code EventHandler} for Menu
     */
    private ArrayList<EventHandler<ActionEvent>> createMenuEventHandlers() {
        ArrayList<EventHandler<ActionEvent>> handlers = new ArrayList<>();
        handlers.add(e -> {
            // handler for toDisplayPane menuItem
            this.view.switchView(view.getDisplayPane());
        });
        handlers.add(e -> {
            // handler for toQueryPane menuItem
            this.view.switchView(view.getQueryPane());
        });
        return handlers;
    }

    /**
     * Create EventHandler for loading url in textfield. When no tab exists,
     * entering url in textfield will result in creating a new tab to load such url.
     * 
     * @return {@code EventHandler} for loading url in textfield
     */
    private EventHandler<ActionEvent> createUrlLoadingEventHandler() {
        return e -> {
            // update view
            var displayPane = this.view.getDisplayPane();
            String url = displayPane.getUrlInputBox().getUrlTextField().getText();

            if (url != null && !url.isEmpty()) {

                // always create a new tab for local file
                if (url.toLowerCase().startsWith("file:")) {
                    try {
                        String content = WebDoc.readLocalFile(url);
                        Tab createdTab = displayPane.loadIntoNewTab(content);
                        // register ChangeListener with this cratedTab
                        regStateChangeHandler(createdTab);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                } else {
                    // for online webpage
                    Tab currTab = this.view.getDisplayPane().getCurrentTab();
                    if (currTab == null) {
                        // if there is no tab being selected, add new tab
                        Tab createdTab = displayPane.addTab(completeURL(url));
                        // register ChangeListener with this cratedTab
                        regStateChangeHandler(createdTab);
                    } else {
                        // update current tab to this url
                        WebView currWebView = (WebView) currTab.getContent();
                        currWebView.getEngine().load(completeURL(url));
                    }
                }
            }
        };

    }

    /**
     * <p>
     * Create EventHandler to handle event for creating new {@code Tab}. The new
     * {@code Tab} created is registered with a {@code
     * ChangeListener<State>}, through which it detects the change of the state of
     * the {@code WebEngine} in the {@code Tab}.
     * </p>
     * <p>
     * When the {@code WebEngine} successfully load the webpage (of the URL entered
     * in textfield or hyperlink clicked inside the {@code WebView}), it records the
     * URL in the {@code HistoryPanel}. Note that history is only updated, when the
     * webpage is successfully loaded.
     * </p>
     * 
     * @return {@code EventHandler} to handle event for creating new {@code Tab}
     */
    private EventHandler<ActionEvent> createNewTabEventHandler() {
        return e -> {
            Tab createdTab = this.view.getDisplayPane().addTab(default_url);
            // register ChangeListener with this cratedTab
            regStateChangeHandler(createdTab);
        };
    }

    /**
     * <p>
     * Register a {@code ChangeListener<State>} with a {@code Tab createdTab} so
     * that whenever the {@code WebView} in this tab successfully loads a webpage,
     * it updates the historyPanel.
     * </P>
     * <p>
     * The createdTab must already has a WebView insider, else it can throw
     * exceptions.
     * </p>
     * 
     * @param createdTab Tab with WebView in it as its content.
     */
    private void regStateChangeHandler(Tab createdTab) {
        WebEngine engine = ((WebView) createdTab.getContent()).getEngine();
        engine.getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {
            @Override
            public void changed(ObservableValue<? extends State> observable, State oldValue, State newValue) {
                // if loading url is successful
                if (newValue == State.SUCCEEDED) {
                    String url = engine.getLocation();

                    // if this is a local file, the .getLocation() method cannot return the actual
                    // location of this file.
                    var textField = view.getDisplayPane().getUrlInputBox().getUrlTextField();
                    if (url == null || url.isEmpty())
                        url = textField.getText();

                    // save unique url in history
                    if (url != null && !allWebDocs.containsKey(url)) {
                        // update browsing history
                        updateHistoryPanel(url);

                        // transform document to a string
                        String content = transformDocumentToString(engine.getDocument());

                        // update web index
                        WebDoc doc = updateWebIndices(url, content);

                        // update unique history
                        if (doc != null) {
                            allWebDocs.put(url, doc);
                        }
                    }

                    // update textField to tell the user the location of current webpage (just like
                    // what normal browser does)
                    if (url != null && !textField.getText().equalsIgnoreCase(url)) {
                        textField.setText(url);
                    }
                }
            }
        });
    }

    /**
     * Transform Document object to a string.
     * 
     * @param document org.w3c.dom.Document representing the loaded webpage
     * @return content of this Document object as a string. It can be {@code NULL}
     *         if the transformation is failed.
     */
    private String transformDocumentToString(Document document) {
        if (transformer == null)
            try {
                transformer = TransformerFactory.newInstance().newTransformer();
            } catch (TransformerConfigurationException e) {
                e.printStackTrace();
            } catch (TransformerFactoryConfigurationError e) {
                e.printStackTrace();
            }

        if (transformer != null) {
            StringWriter writer = new StringWriter();
            try {
                transformer.transform(new DOMSource(document), new StreamResult(writer));
            } catch (TransformerException e) {
                e.printStackTrace();
            }
            return writer.toString();
        } else {
            return null;
        }
    }

    /**
     * Create {@code EventHandler} for Button to go back in history in current
     * selected {@code Tab} or {@code WebView}
     * 
     * @return {@code EventHandler} for Button to go back in history in current
     *         selected {@code Tab} or {@code WebView}
     * 
     * @see UrlInputBox
     * @see BrowserView
     */
    private EventHandler<ActionEvent> createBackwordBtnHandler() {
        return e -> {
            var currTab = this.view.getDisplayPane().getCurrentTab();
            if (currTab != null) {
                var webHistory = ((WebView) currTab.getContent()).getEngine().getHistory();
                int index = webHistory.getCurrentIndex();
                if (index > 0 && index < webHistory.getEntries().size())
                    webHistory.go(-1);
            }
        };
    }

    /**
     * create {@code EventHandler} for Button to go forward in history in current
     * selected {@code Tab} or {@code WebView}
     * 
     * @return {@code EventHandler} for Button to go forward in history in current
     *         selected {@code Tab} or {@code WebView}
     * @see UrlInputBox
     * @see BrowserView
     */
    private EventHandler<ActionEvent> createForwardBtnHandler() {
        return e -> {
            var currTab = this.view.getDisplayPane().getCurrentTab();
            if (currTab != null) {
                var webHistory = ((WebView) currTab.getContent()).getEngine().getHistory();
                int index = webHistory.getCurrentIndex();
                if (index >= 0 && index < webHistory.getEntries().size() - 1)
                    webHistory.go(1);
            }
        };
    }

    /**
     * Create {@code EventHandler} to process the infix query and update the result
     * panel to display the results
     * 
     * @return {@code EventHandler} to process the infix query and update the result
     *         panel to display the results
     * 
     * @see BrowserView
     * @see QueryControlPanel
     */
    private EventHandler<ActionEvent> createInfixQueryHandler() {
        return e -> {
            var textField = this.view.getQueryPane().getQueryControlPanel().getInfixTf();
            String infixQuery = textField.getText();
            if (infixQuery != null && !infixQuery.isEmpty()) {
                if (infixQuery.trim().equalsIgnoreCase("/all")) {
                    // special command for showing all WebDoc
                    updateQueryResultPanel(allWebDocs.values(), null);
                } else {
                    // parse query
                    Query parsedQuery = QueryBuilder.parseInfixForm(infixQuery);
                    Set<WebDoc> headResultSet = parsedQuery.matches(headIndex);
                    Set<WebDoc> bodyResultSet = parsedQuery.matches(bodyIndex);
                    // update view
                    updateQueryResultPanel(headResultSet, bodyResultSet);
                }
            }
            textField.clear();
        };
    }

    /**
     * Create {@code EventHandler} to process the prefix query and update the result
     * panel to display the results
     * 
     * @return {@code EventHandler} to process the prefix query and update the
     *         result panel to display the results
     * 
     * @see BrowserView
     * @see QueryControlPanel
     */
    private EventHandler<ActionEvent> createPrefixQueryHandler() {
        return e -> {
            var textField = this.view.getQueryPane().getQueryControlPanel().getPrefixTf();
            String prefixQuery = textField.getText();
            if (prefixQuery != null && !prefixQuery.isEmpty()) {

                if (prefixQuery.trim().equalsIgnoreCase("/all")) {
                    // special command for showing all WebDoc
                    updateQueryResultPanel(allWebDocs.values(), null);
                } else {
                    // parse query
                    Query parsedQuery = QueryBuilder.parse(prefixQuery);
                    Set<WebDoc> headResultSet = parsedQuery.matches(headIndex);
                    Set<WebDoc> bodyResultSet = parsedQuery.matches(bodyIndex);
                    // update view
                    updateQueryResultPanel(headResultSet, bodyResultSet);
                }
            }
            textField.clear();
        };
    }

    /**
     * Update the QueryResultPanel by clearing the {@code ObservableList<Node>} in
     * the two VBoxs, and refill them with the Buttons that contain the results of
     * the query (the URL String). Each button is registered with an EventHandler
     * that when it is clicked, the panel in the middle of the QueryPane will show
     * the summary (words in head and body) of this url.
     * 
     * @param headResSet Collection of WebDoc for the head section
     * @param bodyResSet Collection of WebDoc for the body section
     * @see QueryResultPanel
     */
    private void updateQueryResultPanel(Collection<WebDoc> headResSet, Collection<WebDoc> bodyResSet) {
        List<String> headList = new ArrayList<>();
        if (headResSet != null)
            for (WebDoc doc : headResSet) {
                headList.add(doc.getUrlString());
            }
        List<String> bodyList = new ArrayList<>();
        if (bodyResSet != null)
            for (WebDoc doc : bodyResSet) {
                bodyList.add(doc.getUrlString());
            }

        // update view
        var queryResult = this.view.getQueryPane().getQueryResultPanel();
        var headVBox = queryResult.getHdResVBox();
        headVBox.getChildren().clear();
        for (String url : headList) {
            headVBox.getChildren().add(createQueryResultBtn(url));
        }
        var bodyVBox = queryResult.getBdResVBox();
        bodyVBox.getChildren().clear();
        for (String url : bodyList) {
            bodyVBox.getChildren().add(createQueryResultBtn(url));
        }
    }

    /**
     * Return a new Button of the given url, which is registered with an
     * EventHandler that when it is clicked, the panel in the middle of the
     * QueryPane will show the summary (words in head and body) of this url.
     * 
     * @param url URL string
     * @return a new Button of the url that is registered with an EventHandler that
     *         shows the summary (words in head and body) of this url in the middle
     *         of the QueryPane.
     * @see QueryPane
     */
    private Button createQueryResultBtn(String url) {
        var btn = new Button(url);
        btn.setOnAction(e -> {
            var textArea = view.getQueryPane().getUrlSummaryPanel().getTextArea();
            WebDoc webDoc = allWebDocs.get(url);
            StringBuilder headWords = new StringBuilder();
            for (String wd : webDoc.getHeadWords()) {
                headWords.append(wd + " ");
            }

            StringBuilder bodyWords = new StringBuilder();
            for (String wd : webDoc.getBodyWords()) {
                bodyWords.append(wd + " ");
            }
            textArea.setText("[Words In Head:]\n" + headWords + "\n\n[Words In Body:]\n" + bodyWords);
        });
        return btn;
    }

    /**
     * Add "http://" protocol to the given url string if this url is not starting
     * with "http://" or "https://".
     * 
     * @param oriUrl
     * @return url string
     */
    private String completeURL(String oriUrl) {
        return oriUrl.startsWith("http://") || oriUrl.startsWith("https://") ? oriUrl : "http://" + oriUrl;
    }

    /**
     * <p>
     * Update historyPanel. This involves creating a new {@code Button} (containing
     * this url string) in the historyPanel, and setup a
     * {@code EventHandler<ActionEvent>} for this button to navigate back to the
     * DisplayPane to display the webpage of this url.
     * </p>
     * <p>
     * This method does not know whether this url has been visited before, so this
     * method can generate duplicates buttons in the historyPanel.
     * </p>
     * 
     * 
     * @param url URL string
     */
    private void updateHistoryPanel(String url) {
        // update history view
        var btn = new Button(url);
        view.getQueryPane().getHistoryPanel().add(btn);

        // assign eventhandler for this btn
        btn.setOnAction(e1 -> {
            view.getDisplayPane().addTab(url);
            view.switchView(view.getDisplayPane());
        });
    }

    /**
     * <p>
     * Update the WebIndex for body and head sections of the webpage, only when this
     * url has neven been accessed to. This method should only be called when the
     * url is correct and has been successfully accessed.
     * </p>
     * <p>
     * This method does not know whether this url has been visited before, so other
     * means should be used to ensure this url is unique and has neven been passed
     * to this method before.
     * </p>
     * 
     * @param url     URL String
     * @param content Actual content (html/js code) of this webpage. if it's
     *                {@code Null}, a {@code new WebDoc} will be constructed by
     *                making connection to the URL, else it will be constructed by
     *                parsing the given content string.
     * @return the created WebDoc
     */
    private WebDoc updateWebIndices(String url, String content) {
        try {
            WebDoc doc;
            if (content == null) {
                doc = new WebDoc(url);
            } else {
                doc = new WebDoc(url, content);
            }
            headIndex.add(doc);
            bodyIndex.add(doc);
            return doc;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
