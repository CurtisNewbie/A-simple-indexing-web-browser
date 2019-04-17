package webBrowserGUI;

import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import webBrowserModel.*;
import webBrowserModel.WebIndex.TypeOfWords;

public class WebBrowserController {

	private WebBrowserView view;
	private WebIndex webIndex_Keyword;
	private WebIndex webIndex_ContentWord;
	private ArrayList<WebDoc> webDocCollection;

	public WebBrowserController(WebBrowserView view) {
		this.view = view;
		webIndex_Keyword = new WebIndex(TypeOfWords.KEYWORD);
		webIndex_ContentWord = new WebIndex(TypeOfWords.CONTENT_WORD);
		webDocCollection = new ArrayList<>();

		view.addConfirmActionListener(new ConfirmActionHandler());
		view.addSearchActionListener(new SearchActionHandler());
		view.addMenuItemActionListener(new MenuItemActionHandler());
	}

	private class ConfirmActionHandler implements ActionListener {
		JTextField urlInput;
		JEditorPane htmlContent;
		JTextArea historyTextArea;

		public ConfirmActionHandler() {
			this.urlInput = view.getUrlTextField();
			this.historyTextArea = view.getHistoryTextArea();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			this.htmlContent = view.addTabToHtmlBrowserCard();
			String url = urlInput.getText();
			try {
				htmlContent.setPage(new URL(url));
				WebDoc wd = new WebDoc(url);
				historyTextArea.append(wd.getEntry());
				webDocCollection.add(wd);
				webIndex_Keyword.add(wd);
				webIndex_ContentWord.add(wd);
			} catch (MalformedURLException e1) {
				JOptionPane.showMessageDialog(null, "Incorrect form of URL", "Error", JOptionPane.WARNING_MESSAGE);
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(null, "URL not accessible, please check internet connect", "Error",
						JOptionPane.WARNING_MESSAGE);
			}
		}
	}

	private class SearchActionHandler implements ActionListener {
		JTextArea keywordResultTextArea;
		JTextArea contentWordResultTextArea;
		JTextField infixQuery;
		JTextField prefixQuery;

		public SearchActionHandler() {
			keywordResultTextArea = view.getKeywordQueryResultTextArea();
			contentWordResultTextArea = view.getContentWordQueryResultTextArea();
			infixQuery = view.getInfixQuery();
			prefixQuery = view.getPrefixQuery();
		}

		@Override
		public void actionPerformed(ActionEvent e) {

			if(e.getActionCommand().equals("infix")) {
				keywordResultTextArea.setText(infixQuery.getText());
				contentWordResultTextArea.setText(infixQuery.getText());
			} else {
				keywordResultTextArea.setText(prefixQuery.getText());
				contentWordResultTextArea.setText(prefixQuery.getText());
			}
		}
	}

	private class MenuItemActionHandler implements ActionListener {

		public MenuItemActionHandler() {
			// TODO Auto-generated constructor stub
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			CardLayout layoutControl = view.getCardLayoutControl();
			JPanel cards = view.getCards();
			if (command.equals(view.WEB_BROWSER_TAG)) {
				layoutControl.show(cards, view.WEB_BROWSER_TAG);
			} else if (command.equals(view.QUERY_BROWSER_TAG))
				layoutControl.show(cards, view.QUERY_BROWSER_TAG);
		}
	}
}
