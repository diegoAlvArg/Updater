/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zzParaBorrar;

import aplicacion.datos.Tareas;
import javafx.scene.control.Button;

/**
 * @deprecated 
 * @author Usuario
 */
public class actionTable extends Button{
     public actionTable(String text, Tareas focus) {
            super("Edit");
            setOnAction((event) -> {
                
//                Alert alert = new Alert(AlertType.INFORMATION);
//                alert.setTitle("Hey!");
//                alert.setHeaderText(null);
//                alert.setContentText("You're editing \"" + fileName + "\"");
//                alert.showAndWait();
//                this.setText("Editato");
            });
        }
}
