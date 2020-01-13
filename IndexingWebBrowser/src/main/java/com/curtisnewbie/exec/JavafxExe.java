package com.curtisnewbie.exec;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.geometry.*;
import javafx.stage.Screen;
import javafx.scene.Scene;

import com.curtisnewbie.view.*;
import com.curtisnewbie.controller.*;

/**
 * This where the javafx implementation of this program is started.
 * 
 * @author Yongjie Zhuang
 */
public class JavafxExe extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        BrowserView view = new BrowserView();

        // controller that controls the view
        BrowserController controller = new BrowserController(view);

        // get screen size
        Rectangle2D screen = Screen.getPrimary().getBounds();
        double width = screen.getWidth();
        double height = screen.getHeight();

        Scene scene = new Scene(view, width * 0.7, height * 0.7);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

}