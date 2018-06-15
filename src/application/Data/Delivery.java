/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package application.Data;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
//import java.time.Duration;

/**
 *
 * @author Usuario
 */
public class Delivery implements Serializable {

    private String fuente;                      //---------------------------#C1 = D1' + D2'
    private String estado;                      //---------------------------#C2 && #C5     
    private Calendar tiempoLimite;              //---------------------------#C3
    private String info;                        //---------------------------#C4 = D3' + D4'
     
    private String curso;                       //----------------------------D1'
    private String nombre;                      //----------------------------D2'
    private String pathFile;                    //----------------------------D3'
    private String nota;                        //----------------------------D4' 


    private String languague;
//    private String seedTime;
//    private String seedTimeNoDays;

    public Delivery(String curso, String nombre, int estado, String fichero, String tiempo, String languague, String nota) throws ParseException {
        this.nombre = nombre;
        this.curso = curso;        
        this.fuente = nombre + " - " + curso;
        this.estado = "" + estado;
        this.pathFile = fichero;
        this.nota = nota;
        this.info = "S: " + this.pathFile + "/n" + "N: " + this.nota;       
        this.languague = languague;
        setearCuentaRegresiva(tiempo, languague);

    }

//    public Delivery(String curso, String nombre, int estado, String fichero, String tiempo, String languague) throws ParseException {
//        this(curso, nombre, estado, fichero, tiempo, languague, "");
//    }

    private void setearCuentaRegresiva(String tiempo, String language) throws ParseException {
        SimpleDateFormat formatter;
        Calendar date;
        String auxTiempo = tiempo;

        switch (language) {

            case "en":
                formatter = new SimpleDateFormat("EEEE, dd MMM yyyy, HH:mm a", Locale.forLanguageTag("en"));
                date = Calendar.getInstance();
                date.setTime(formatter.parse(auxTiempo));
                break;

            case "es":
                formatter = new SimpleDateFormat("EEEE, dd MMMM yyyy, HH:mm", Locale.forLanguageTag("es"));
                auxTiempo = auxTiempo.replaceAll(" de", "");
                date = Calendar.getInstance();
                date.setTime(formatter.parse(auxTiempo));
                break;

            default:
                throw new ParseException("No locale(" + language + ")", 0);
        }
//
        if (date != null) {
            this.tiempoLimite = date;
//            this.tiempo.set(String.valueOf(tiempoLimite.getTime().getTime() - System.currentTimeMillis()));
        }

    }

    
    //---------------------------GET & SETTER PARA LAS COLUMNAS----------------------
    public String getFuente() {
        return fuente;//.get();
    }
    public void setFuente(String fuente) {
        this.fuente = fuente;//.set(fuente);
    }

    public String getEstado() {
//        return estado.get();
        return estado;
    }
    public void setEstado(String estado) {
//        this.estado.set(estado);
        this.estado = estado;
    }

    public String getTiempo() {
        String temp = String.valueOf(tiempoLimite.getTime().getTime() - System.currentTimeMillis()); 
        System.err.println("\t>>temp " + temp);
        return temp; //tiempo.get();
    }
    public void setTiempo(String tiempo) throws ParseException {
        this.setearCuentaRegresiva(tiempo, languague);
    }
    public Calendar getTiempoLimite() {
        return tiempoLimite;
    }
    public void setTiempoLimite(Calendar tiempoLimite) {
        this.tiempoLimite = tiempoLimite;
    }

    public String getInfo() {
        return info;
    }
    public void setInfo(String info) {
        this.info = info;//.set(info);
    }

    //---------------------------GET & SETTER PARA INFO-------------------------------
    public String getCurso() {
        return curso;
    }
    public void setCurso(String curso) {
        this.curso = curso;
    }
    
    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPathFile() {
        return pathFile;
    }
    public void setPathFile(String pathFile) {
        this.pathFile = pathFile;
    }

    public String getNota() {
        return nota;
    }
    public void setNota(String nota) {
        this.nota = nota;
    }
    
    public String getLanguague() {
        return languague;
    }
    public void setLanguague(String languague) {
        this.languague = languague;
    }
    
    
    @Override
    public String toString() {
        return print();//"Item " + fuente;//.get();
    }

    public String print(){
        String sep = "**";
        String endSep = ";;";
        
        return fuente + sep + estado + sep + tiempoLimite.getTime().getTime() + sep + getInfo() +endSep;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Delivery)) {
            return false;
        }
        Delivery otherDel = (Delivery) obj;

        return print().equals(otherDel.print());
    }
}
