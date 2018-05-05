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
 *@deprecated 
 * @author Usuario
 */
public class syncronice extends Service<Void> {

    private String user;
    private String pass1;
    private String pass2;
    private String pathDowload;

    public void setUser(String user) {
        this.user = user;
    }

    public void setPass1(String pass1) {
        this.pass1 = pass1;
    }

    public void setPass2(String pass2) {
        this.pass2 = pass2;
    }

    public void setPathDowload(String pathDowload) {
        this.pathDowload = pathDowload;
    }

    public void setRb(ResourceBundle rb) {
        this.rb = rb;
    }

    public void setIu(InterfaceController iu) {
        this.iu = iu;
    }
    private ResourceBundle rb;
    private InterfaceController iu;

    public void launchTread() {
        final String _user = user;
        final String _pass1 = pass1;
        final String _pass2 = pass2;
        final String _path = pathDowload;
        final ResourceBundle _rb = rb;
        final InterfaceController _iu = iu;
        new Thread(() -> {
            try {
                main.performUpdate(_user, _pass1, "(2016-2017)", _path, _iu);
////                main.performUpdateSingle(0, _user, _pass1, "(2016-2017)", _path);
            } catch (IOException ex) {
                Logger.getLogger(syncronice.class.getName()).log(Level.SEVERE, null, ex);
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

    @Override
    protected Task<Void> createTask() {
        final String _user = user;
        final String _pass1 = pass1;
        final String _pass2 = pass2;
        final String _path = pathDowload;
        final ResourceBundle _rb = rb;
        final InterfaceController _iu = iu;
        new Thread(() -> {
            try {
                main.performUpdateSingle(0, _user, _pass1, "(2016-2017)", _path, _iu);
            } catch (IOException ex) {
                Logger.getLogger(syncronice.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                _iu.syncroEnd();
            }
        }).start();
        return null;
//        return new Task<Void>() {
//
//            @Override
//            protected Void call() {
//
////                System.err.println("eyyyyyyyyyyyyyyyyyy");
//                Platform.runLater(new Runnable() {
//                    @Override
//                    public void run() {
//                        try {
//                            
////                            0 - DCU - DM
////                            1- Sist.ayuda toma decisiones
////                            2- Seg.Informatica
////                            3- Sist.y tegnologias web 
////                            4- Ing.Sw 
////                            5- Sist.legados
////                            6- Diseño y administracion de redes 
////                            7- Comercio Electornico 
////                            main.performUpdate(_user, _pass1, "(2016-2017)", _path);
////                            main.performUpdateSingle(0, _user, _pass1, "(2016-2017)", _path);
////                            main.performUpdateSingle(1, _user, _pass1, "(2016-2017)", _path);
////                            main.performUpdateSingle(2, _user, _pass1, "(2016-2017)", _path);
////                            main.performUpdateSingle(3, _user, _pass1, "(2016-2017)", _path);
////                            main.performUpdateSingle(4, _user, _pass1, "(2016-2017)", _path);
////                            main.performUpdateSingle(5, _user, _pass1, "(2016-2017)", _path);
////                            main.performUpdateSingle(6, _user, _pass1, "(2016-2017)", _path);
////                            main.performUpdateSingle(7, _user, _pass1, "(2016-2017)", _path);
//                        } catch (Exception ex) {
//                            ex.printStackTrace();
//                        }finally{
//                            _iu.syncroEnd();
//                        }
//                    }
//                });
//                return null;
//            }
//        };
    }

    public String currentYear() {
        return "(2016-2017)";
    }
}
