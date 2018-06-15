/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zzParaBorrar;

import zzParaBorrar.MainController;
import application.HelloWorld;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import Tools.lenguaje.ResourceLeng;
import application.HelloWorld;

/**
 * FXML Controller class
 * @deprecated 
 * @author Usuario
 */
public class AskUpdateController implements Initializable {

    @FXML
    private Label TxtAsk;

    @FXML
    private Button Byes;

    @FXML
    private Button BNo;
    
    
    private String default_quest;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle _rb) {
        // TODO

        ResourceBundle rb;
        if (_rb != null) {
            rb = _rb;
        } else {
            rb = HelloWorld.getResource();
        }
        // TODO
        this.Byes.setText(rb.getString(ResourceLeng.TXT_YES));
        this.BNo.setText(rb.getString(ResourceLeng.TXT_NO));
        this.default_quest = rb.getString(ResourceLeng.ASK_UPDATE);

    }

    public void setAsk(String _question) {
        TxtAsk.setText(_question);
    }
    
    public void setVersionsAsk(int _new, int _old){
        TxtAsk.setText(String.format(default_quest, _new, _old));
    }

    public void Yes(ActionEvent event) throws IOException {
        HelloWorld.restartApplication("XR3PlayerUpdater", 0);
    }

    public void No(ActionEvent event) throws IOException {
        try {
            ((Node) event.getSource()).getScene().getWindow().hide();
            Stage primaryStage = new Stage();
            FXMLLoader loader = new FXMLLoader();
            Pane root = loader.load(getClass().getResource("/Resources/fxml/Main.fxml").openStream());
//            Parent root = FXMLLoader.load(getClass().getResource("/Resources/fxml/Main.fxml"), rb);
            MainController control = (MainController)loader.getController();
            control.setStage(primaryStage);
            
            Scene scene = new Scene(root);
            primaryStage.setTitle(HelloWorld.getResource().getString(ResourceLeng.APP_TITLE)
                    + HelloWorld.internalInformation.get("Version"));
            primaryStage.setScene(scene);
            primaryStage.show();
            HelloWorld.changeStage(primaryStage);
        } catch (Exception ex) {
            ex.printStackTrace();
//            Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
