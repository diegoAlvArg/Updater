/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zzParaBorrar;

import application.HelloWorld;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import Tools.language.ResourceLeng;
import application.HelloWorld;
import java.io.File;
import javafx.application.HostServices;
//import javafx.application.HostServices;

/**
 * FXML Controller class
 *
 * @author Usuario
 * @deprecated 
 */
public class MainController implements Initializable {

    @FXML
    private Label lblText;

    @FXML
    private Button BHello;

    @FXML
    private ComboBox CLanguagues;

    @FXML
    private AnchorPane AnchorPanel;

    private String TxtButton;
    private String TxtLog;
    private int count;
    private Stage miStado;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle _rb) {
//        System.out.println("aaaaa");
        ResourceBundle rb;
        if (_rb != null) {
            System.out.println("RB no es null");
            rb = _rb;
        } else {
            rb = HelloWorld.getResource();
            System.out.println("RB ES null");
        }
        // TODO
        this.TxtButton = rb.getString(ResourceLeng.HELLO_BUTTON);
        this.BHello.setText(TxtButton + count);
        this.lblText.setText(rb.getString(ResourceLeng.HELLO_WORLD));
        this.TxtLog = rb.getString(ResourceLeng.HELLO_LOG);

        CLanguagues.getItems().addAll(
                "Español",
                "Ingles"
        );
        CLanguagues.setValue("Español");
    }

    public void SayHello(ActionEvent _event) {
        System.out.println(TxtLog);
        count++;
        this.BHello.setText(TxtButton + count);
        File file = new File("C:\\demo\\Tema 0.pdf");
        
        HostServices hostServices = HelloWorld.getHostService();
        hostServices.showDocument(file.getAbsolutePath());
    
    }

    public void changeLanguague(ActionEvent _event) {
        ResourceBundle rb;
        switch ((String) CLanguagues.getValue()) {
            case "Ingles":
                rb = ResourceBundle.getBundle("Resources.Languages.SystemMessages", Locale.ENGLISH);
                break;
            case "Español":
            default:
                rb = ResourceBundle.getBundle("Resources.Languages.SystemMessages", Locale.getDefault());
                break;
        }

        this.TxtButton = rb.getString(ResourceLeng.HELLO_BUTTON);
        this.BHello.setText(TxtButton + count);
        this.lblText.setText(rb.getString(ResourceLeng.HELLO_WORLD));
        this.TxtLog = rb.getString(ResourceLeng.HELLO_LOG);

//        miStado.setTitle("aaaa");
//        miStado.setTitle(rb.getString(ResourceLeng.APP_TITLE) + HelloWorld.internalInformation.get("Version"));
        HelloWorld.changeTitle(rb.getString(ResourceLeng.APP_TITLE));
    }

    public void setStage(Stage _stage) {
        miStado = _stage;
    }
//    public 
}
