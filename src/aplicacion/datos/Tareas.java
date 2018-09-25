package aplicacion.datos;

//#4 Java
import java.io.File;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/** 242
 *
 * @author Diego Alvarez
 */
public class Tareas implements Serializable {

    private String identificador;                      //---------------------------#C1 = D1' + D2'
    private String estado;                      //---------------------------#C2 && #C5     
    private Calendar tiempoLimite;              //---------------------------#C3
    private String feedBack;                        //---------------------------#C4 = D3' + D4'

    private String curso;                       //----------------------------D1'
    private String nombre;                      //----------------------------D2'
    private String pathFile;                    //----------------------------D3'
    private String nota;                        //----------------------------D4' 
    private String comentario;
    private String urlWeb;
    private String languague;

    public Tareas(String curso, String nombre, String fichero, String tiempo, String languague, String nota, String comentario, String url) throws ParseException {
        this.nombre = nombre.replaceAll("[^-\\s\\d\\w\\.ñÑáéíóúÁÉÍÓÚ]+[\\s]?", "");//Añadir otros acentos?
        this.curso = curso.replaceAll("\\([\\d-]*\\)", "");
        this.identificador = this.nombre + " - " + this.curso;
        this.pathFile = fichero;
        this.nota = nota;
        this.comentario = comentario;
        this.feedBack = "S: " + this.pathFile + "/n" + "N: " + this.nota + "/n" + "C: " + this.comentario;
        this.languague = languague;
        
        this.urlWeb = url;
        
        setearCuentaRegresiva(tiempo, languague);
        calcularEstado(fichero, nota, comentario);
         
        if(nombre == "practica 6"){
            estado = "9";
        }
    }

    /**
     * Metodo que dado un 'tiempo' en un 'lenguague', obtendra un calendar 
     *  representativo.
     * 
     * @param tiempo stamp de tiempo
     * @param lenguague lenguague en el que esta el stamp
     * @throws ParseException Error cuando no esta en un idioma esperado o estos
     * no cumplen con el formato esperado
     */
    private void setearCuentaRegresiva(String tiempo, String lenguague) throws ParseException {
        SimpleDateFormat formatter;
        Calendar date;
        String auxTiempo = tiempo;

        switch (lenguague) {

            case "en":
                formatter = new SimpleDateFormat("EEEE, dd MMM yyyy, HH:mm a", Locale.forLanguageTag("en"));//a?
                date = Calendar.getInstance();
                date.setTime(formatter.parse(auxTiempo));
                break;

            case "es":
                formatter = new SimpleDateFormat("EEEE, dd MMMM yyyy, HH:mm", Locale.forLanguageTag("en"));//es
                auxTiempo = auxTiempo.replaceAll(" de", "");
                date = Calendar.getInstance();
                date.setTime(formatter.parse(auxTiempo));
                break;

            default:
                throw new ParseException("No locale(" + lenguague + ")", 0);
        }
//
        if (date != null) {
            this.tiempoLimite = date;
//            this.tiempo.set(String.valueOf(tiempoLimite.getTime().getTime() - System.currentTimeMillis()));
        }

    }
    private void calcularEstado(String fichero, String nota, String comentario){
        if (!nota.equals("")) {
            estado = (comentario == "") ? "1" : "6";
        } else if (!tiempoLimite.after(Calendar.getInstance())) {
            estado = (comentario.equals("")) ? "2" : "7";
        } else if (fichero == "") {
            estado = (comentario.equals("")) ? "0" : "5";
        } else {
            estado = (comentario.equals("")) ? "3" : "8";
        }
    }
    //---------------------------GET PARA LAS COLUMNAS----------------------
    /**
     * Devuelve el estado de la Tarea. Dicho estado es calculado a partir de 
     * pathFile + nota + comentario
     * @return 
     */
    public String getEstado() {
        return estado;
    }
    /**
     * Devuelve el Identificador de la Tarea. Formado por nombre + curso
     * @return 
     */
    public String getIdentificador() {
        return identificador;//.get();
    }
    /**
     * Devuelve el feedback de la Tarea. PathFile + Nota
     * @return 
     */
    public String getFeedBack() {
        return feedBack;
    }
    /**
     * Devuelve el path del fichero asociado a la Tarea
     * @return 
     */
    public String getPathFile() {
        return pathFile;
    }
    /**
     * Devuelve el stamp time asociado a la fecha limite de la Tarea
     * @return 
     */
    public String getTiempo() {
        String temp = String.valueOf(tiempoLimite.getTime().getTime() - System.currentTimeMillis());
        return temp;
    }
    /**
     * Devuelve la URL asociada a la Tarea
     * @return 
     */
    public String getUrlWeb(){
        return urlWeb;
    }
  
    
    //---------------------------GET PARA INFO-------------------------------
    /**
     * Devuelve el Calendar asociado a la fecha limite de la Tarea
     * @return 
     */
    private Calendar getCalendar(){
        return tiempoLimite;
    }
    private String getFeed(){
        return comentario;
    }
    private String getLanguague() {
        return languague;
    }
    private String getNota() {
        return nota;
    }
    
  
    //---------------------------UTILs------------------------------------------
    public void resetearEstado(){
        calcularEstado(pathFile, nota, comentario);
    }
    public void setEstado(String nuevoEstado){
        estado = nuevoEstado;
    }
    public void setPathFile(String nuevoPath){
        pathFile = nuevoPath;
        this.feedBack = "S: " + this.pathFile + "/n" + "N: " + this.nota + "/n" + "C: " + this.comentario;
    }
    
    @Override
    public String toString() {
        String sep = "**";
        String endSep = ";;";

        return identificador + sep + estado + sep + tiempoLimite.getTime().getTime() + sep + getFeedBack() + sep + comentario + endSep;
    }

    /**
     * Metodo que genera un String identificativo de la Tarea. Se diferencia de 
     *  toString en que en la parte del fichero asociado solo representa el 
     *  fichero, sin tener en cuenta la ruta.
     * 
     * @return 
     */
    public String printTrack() {
        String sep = "**";
        String endSep = ";;";
        String fileName;
        fileName = (pathFile.contains(File.pathSeparator)) ? pathFile.substring(pathFile.lastIndexOf(File.pathSeparator + 1)) : pathFile;
        
        return identificador + sep + estado + sep + tiempoLimite.getTime().getTime() + sep +
                "S: " + fileName + "/n" + "N: " + this.nota + "/n" + "C: " + this.comentario + sep + comentario + endSep ;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Tareas)) {
            return false;
        }
        Tareas otherDel = (Tareas) obj;

        return printTrack().equals(otherDel.printTrack());
    }
    
    public void updateInfo(Tareas mirror){
        if(!getCalendar().equals(mirror.getCalendar())){
            tiempoLimite = mirror.getCalendar();
            languague = mirror.getLanguague();
        }
        
        if(!getFeedBack().equals(mirror.getFeedBack())){
            if(nota.equals(mirror.getNota())){
                pathFile = mirror.getPathFile();
            }else{
                nota = mirror.getNota();
            }
           
            this.feedBack = "S: " + this.pathFile + "/n" + "N: " + this.nota + "/n" + "C: " + this.comentario;
        }
        if(!getFeed().equals(mirror.getFeed())){
            this.comentario = mirror.getFeed();
        }
        if(!getUrlWeb().equals(mirror.getUrlWeb())){
            this.urlWeb = mirror.getUrlWeb();
        }
        this.estado = mirror.getEstado();
    }
    
    
    /**
     * @deprecated 
     */
    public void printInfo() {
        System.out.println("Curso " + curso);
        System.out.println("Titulo " + nombre);
        System.out.println("Estado " + estado);
        System.out.println("Languague " + languague);
        System.out.println("Fecha entrega: " + tiempoLimite.getTime());
        System.out.println("Entrega " + pathFile);
        System.out.println("Nota " + nota);
        System.out.println("Feedback " + comentario);

        System.out.println("---------------------------------------------------"
                + "\n---------------------------------------------------\n\n");
    }
}