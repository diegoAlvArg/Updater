/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package application;

//import Updater.main.UpdateProcess;
//import Updater.main.VersionCheck;
import java.io.IOException;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import tools.ResourceLeng;

/**
 *
 * @author Diego Alvarez
 */
public class HelloWorld extends Application {

    public static Properties internalInformation = new Properties();

    static {
        //Important for Web Browser
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");

        //----------Properties-------------
        internalInformation.put("Version", 05);
        internalInformation.put("ReleasedDate", "29/02/2018");

        System.out.println("Outside of Application Start Method");
    }

    private int count = 0;
    private static ResourceBundle rb;
//    private String Text_button;
//    private String Text_log;
//    private String Text_title;
    private static Stage stage;
    
    @Override
    public void start(Stage primaryStage) throws IOException {
        rb = ResourceBundle.getBundle("Resources.Languages.SystemMessages", Locale.getDefault());
        Parent root = FXMLLoader.load(getClass().getResource("/Resources/Main.fxml"), rb);
        Scene scene = new Scene(root, 400, 400);
//            scene.getStylesheets().add(STYLESHEET_MODENA)
        primaryStage.setTitle(rb.getString(ResourceLeng.APP_TITLE));
        primaryStage.setScene(scene);
        primaryStage.show();
        stage = primaryStage;
    }

    private static void setLanguage(Locale _languague) {
        rb = ResourceBundle.getBundle("Resources.Languages.SystemMessages", _languague);
    }

//    @Deprecated
//    private void changeLanguage(boolean _refresh, Locale _language, Stage _stage, Button _btn) {
//        setLanguage(_language);
//        this.Text_button = rb.getString(ResourceLeng.HELLO_BUTTON);
//        this.Text_log = rb.getString(ResourceLeng.HELLO_LOG);
//        this.Text_title = rb.getString(ResourceLeng.APP_TITLE);
//
//        if (_refresh) {
//            _btn.setText(Text_button + count);
//            _stage.setTitle(Text_title);
//        }
//    }

    public static void changeTitle(String _title){
       stage.setTitle(_title);
    }
        
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {

//        setLanguage(Locale.getDefault());
//        System.out.println(rb.getString(ResourceLeng.APP_INIT));
//        //***************************************
//        int lastVersion = 0;
//        

//        lastVersion = VersionCheck.isLastUpdate();
////        if(lastVersion == 0){
////            System.out.println("No se encontro el fichero de actualizaciones");
////        }else if(lastVersion == (int)internalInformation.get("Version")){
////            System.out.println("Soy la ultima version");
////        }else{
////            System.out.println("Hay una nueva version");
//////            actualizador.
//////            actualizador2.setUpdate(lastVersion);
//////            actualizador2.starProcces(args);
//////            actualizador.
////            String args2[] = new String[lastVersion];
////            
////            actualizador2.starProcces(args2);
////        }
//        //*****************************************
//       Main.l
        launch(args);
//        System.exit(0);
    }
}
