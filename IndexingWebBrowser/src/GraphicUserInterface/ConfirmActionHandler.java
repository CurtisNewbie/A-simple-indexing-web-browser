package GraphicUserInterface;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

// The listener for confirm button in the webBrowserCard
public class ConfirmActionHandler implements ActionListener {

	JTabbedPane tabbedPane;
	JTextField urlInput;
	JEditorPane htmlContent;
	WebBrowserGUI webBrowserObj;

	public ConfirmActionHandler(JTabbedPane tabbedPane, JTextField urlInput, WebBrowserGUI webBrowserObj) {
		this.tabbedPane = tabbedPane;
		this.urlInput = urlInput;
		this.webBrowserObj = webBrowserObj;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		this.htmlContent = webBrowserObj.addTabToHtmlBrowserCard();
		String url = urlInput.getText();
		try {
			htmlContent.setPage(new URL(url));
		} catch (MalformedURLException e1) {
			JOptionPane.showMessageDialog(null, "Incorrect form of URL", "Error", JOptionPane.WARNING_MESSAGE);
		} catch (IOException e1) {
			JOptionPane.showMessageDialog(null, "URL not accessible, please check internet connect", "Error",
					JOptionPane.WARNING_MESSAGE);
		}
	}

}
