/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wrapper.services;

import application.InterfaceController;
import java.io.IOException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.apache.http.client.ClientProtocolException;
import wrapper.init.main;

/**
 *
 * @author Usuario
 */
public class syncroniceProcess {

    private String user;
    private String pass1;
    private String pass2;
    private String pathDowload;
    private ResourceBundle rb;
    private InterfaceController iu;

    public syncroniceProcess(String user, String pass1, String pass2, String pathDowload, ResourceBundle rb, InterfaceController iu) {
        this.user = user;
        this.pass1 = pass1;
        this.pass2 = pass2;
        this.pathDowload = pathDowload;
        this.rb = rb;
        this.iu = iu;
        launchTread();
    }

    private void launchTread() {
        final String _user = user;
        final String _pass1 = pass1;
        final String _pass2 = pass2;
        final String _path = pathDowload;
        final ResourceBundle _rb = rb;
        final InterfaceController _iu = iu;
        new Thread(() -> {
            try {
                main.performUpdate(_user, _pass1, "(2016-2017)", _path, _iu);
//                main.performUpdateSingle(0, _user, _pass1, "(2016-2017)", _path, _iu);
            } catch (IOException ex) {
                Logger.getLogger(syncroniceProcess.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                Platform.runLater(
                        () -> {
                            System.err.println("acabando");
                            _iu.syncroEnd();
                        }
                );
//                _iu.syncroEnd();
            }
        }).start();
    }

    private String currentYear() {
        return "(2016-2017)";
    }
}
