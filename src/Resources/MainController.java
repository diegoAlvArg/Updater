/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Resources;

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
import tools.ResourceLeng;

/**
 * FXML Controller class
 *
 * @author Usuario
 */
public class MainController implements Initializable {

    @FXML
    private Label lblText;

    @FXML
    private Button BHello;

    @FXML
    private ComboBox CLanguagues;

    private String TxtButton;
    private String TxtLog;
    private int count;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
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
        
        HelloWorld.changeTitle(rb.getString(ResourceLeng.APP_TITLE));
    }

}
