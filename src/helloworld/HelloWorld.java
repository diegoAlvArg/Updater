/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package helloworld;

import java.util.Locale;
import java.util.ResourceBundle;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import tools.ResourceLeng;

/**
 *
 * @author Usuario
 */
public class HelloWorld extends Application {

    private int count = 0;
    private static ResourceBundle rb;
    private String Text_button;
    private String Text_log;
    private String Text_title;

    @Override
    public void start(Stage primaryStage) {
        changeLanguage(false, Locale.getDefault(), primaryStage, null);

        //***************************BUTTON*************************************
        Button btn = new Button();
        btn.setText(Text_button + count);
        btn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                System.out.println(Text_log);
                count++;
                if (count >= 10) {
                    count = 0;
                }
                btn.setText(Text_button + count);

            }
        });
        //**********************************************************************
        ComboBox languagueSelector = new ComboBox();
        languagueSelector.getItems().addAll(
                "Español",
                "Ingles"
        );
        languagueSelector.setValue("Español");
        languagueSelector.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue _ov, String _t, String _t1) {
                if(_t1.contains("Ingles")){
                    System.out.println("A ingles");
                    changeLanguage(true, Locale.ENGLISH, primaryStage, btn);
                }else if(_t1.contains("Español")){
                    System.out.println("A español");
                    changeLanguage(true, Locale.getDefault(), primaryStage, btn);
                }
            }
        });

        GridPane grid = new GridPane();
        grid.setVgap(3);
        grid.setHgap(3);
        //grid.setPadding(new Insets(3, 3, 3, 3));
        grid.add(btn, 0, 1);
        //grid.add
        grid.add(languagueSelector, 2, 3);

        StackPane root = new StackPane();
        root.getChildren().add(grid);

        Scene scene = new Scene(root, 300, 100);

        primaryStage.setTitle(Text_title);
        primaryStage.setScene(scene);
        primaryStage.show();                            
    }

    private static void setLanguage(Locale _languague) {
        rb = ResourceBundle.getBundle("Resources.Languages.SystemMessages", _languague);
    }

    private void changeLanguage(boolean _refresh, Locale _language, Stage _stage, Button _btn) {
        setLanguage(_language);
        this.Text_button = rb.getString(ResourceLeng.HELLO_BUTTON);
        this.Text_log = rb.getString(ResourceLeng.HELLO_LOG);
        this.Text_title = rb.getString(ResourceLeng.APP_TITLE);

        if (_refresh) {
            _btn.setText(Text_button + count);
            _stage.setTitle(Text_title);
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        setLanguage(Locale.getDefault());
        System.out.println(rb.getString(ResourceLeng.APP_INIT));
        launch(args);
    }

}
