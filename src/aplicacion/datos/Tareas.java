package aplicacion.datos;

//#4 Java
import java.io.File;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 *
 * @author Diego
 */
public class Tareas implements Serializable {

    private String fuente;                      //---------------------------#C1 = D1' + D2'
    private String estado;                      //---------------------------#C2 && #C5     
    private Calendar tiempoLimite;              //---------------------------#C3
    private String info;                        //---------------------------#C4 = D3' + D4'

    private String curso;                       //----------------------------D1'
    private String nombre;                      //----------------------------D2'
    private String pathFile;                    //----------------------------D3'
    private String nota;                        //----------------------------D4' 
    private String feed;
    private String urlWeb;
    private String languague;
//    private String seedTime;
//    private String seedTimeNoDays;

    public Tareas(String curso, String nombre, String fichero, String tiempo, String languague, String nota, String comentario, String url) throws ParseException {
        this.nombre = nombre.replaceAll("[^-\\s\\d\\w\\.ñÑáéíóúÁÉÍÓÚ]+[\\s]?", "");//Añadir otros acentos?
        this.curso = curso.replaceAll("\\([\\d-]*\\)", "");
        this.fuente = this.nombre + " - " + this.curso;
        this.pathFile = fichero;
        this.nota = nota;
        this.info = "S: " + this.pathFile + "/n" + "N: " + this.nota;
        this.languague = languague;
        this.feed = comentario;
        this.urlWeb = url;
        setearCuentaRegresiva(tiempo, languague);
        
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

    
    private void setearCuentaRegresiva(String tiempo, String language) throws ParseException {
        SimpleDateFormat formatter;
        Calendar date;
        String auxTiempo = tiempo;

        switch (language) {

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
                throw new ParseException("No locale(" + language + ")", 0);
        }
//
        if (date != null) {
            this.tiempoLimite = date;
//            this.tiempo.set(String.valueOf(tiempoLimite.getTime().getTime() - System.currentTimeMillis()));
        }

    }

    //---------------------------GET PARA LAS COLUMNAS----------------------
    public String getEstado() {
//        return estado.get();
        return estado;
    }
    public String getFuente() {
        return fuente;//.get();
    }
    public String getInfo() {
        return info;
    }
    public String getPathFile() {
        return pathFile;
    }
    public String getTiempo() {
        String temp = String.valueOf(tiempoLimite.getTime().getTime() - System.currentTimeMillis());
//        System.err.printl n("\t>>temp " + temp);
        return temp; //tiempo.get();
    }

  
    
    //---------------------------GET PARA INFO-------------------------------
    private Calendar getGoal(){
        return tiempoLimite;
    }
    private String getFeed(){
        return feed;
    }
    private String getLanguague() {
        return languague;
    }
    private String getNota() {
        return nota;
    }
    private String getUrlWeb(){
        return urlWeb;
    }
  
    //---------------------------UTILs------------------------------------------
    @Override
    public String toString() {
        String sep = "**";
        String endSep = ";;";

        return fuente + sep + estado + sep + tiempoLimite.getTime().getTime() + sep + getInfo() + sep + feed + endSep;
    }

    public String printTrack() {
        String sep = "**";
        String endSep = ";;";
        String fileName;
        fileName = (pathFile.contains(File.pathSeparator)) ? pathFile.substring(pathFile.lastIndexOf(File.pathSeparator + 1)) : pathFile;
        
        return fuente + sep + estado + sep + tiempoLimite.getTime().getTime() + sep +
                "S: " + fileName + "/n" + "N: " + this.nota + sep + feed + endSep ;
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
        if(!getGoal().equals(mirror.getGoal())){
            tiempoLimite = mirror.getGoal();
            languague = mirror.getLanguague();
        }
        
        if(!getInfo().equals(mirror.getInfo())){
            if(nota.equals(mirror.getNota())){
                pathFile = mirror.getPathFile();
            }else{
                nota = mirror.getNota();
            }
           
            this.info =  "S: " + this.pathFile + "/n" + "N: " + this.nota;
        }
        if(!getFeed().equals(mirror.getFeed())){
            this.feed = mirror.getFeed();
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
        System.out.println("Feedback " + feed);

        System.out.println("---------------------------------------------------"
                + "\n---------------------------------------------------\n\n");
    }
}
