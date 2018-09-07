package aplicacion.controlador;


//#1 Static import
//import aplicacion.controlador.MainController;
import java.util.Locale;
import tools.lenguaje.ResourceLeng;
//#4 Java
import java.util.Map;
import java.util.ResourceBundle;
//#5 JavaFx
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.Border;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;


/** 117
 * Control de la tabla Help, encontraremos creditos, ayuda y para actualizar
 * 
 * @author Diego Alvarez
 */
public class TabHelpController {

    private MainController main;

    @FXML
    private Button bActualizar;
    @FXML
    private Label lVersionActual;
    @FXML
    private Hyperlink hAbrirAyuda;
    private String urlWiki;
    @FXML
    private TextFlow tCreditos;
    @FXML
    private Hyperlink hAyudaNaster;
    private String urlNas;

    
    //---------------------------------------------------FXML---------------------------------------------------
    @FXML
    private void abrirAyudaWiki() {
        main.getHostService().showDocument(this.urlWiki);
    }

    @FXML
    private void abrirAyudaNas() {
        main.getHostService().showDocument(this.urlNas);
    }

    /**
     *
     * Metodo que genera el evento de actualizar la App
     */
    @FXML
    private void actualizarVersion() {
        bActualizar.setDisable(true);
        
        main.cambiarDisponibilidadOpcionSysTray(ResourceLeng.SYS_TRAY_UPDATE, false);
        main.actualizarVersion(true);
    }
  
    
    //---------------------------------------------------EVENTO------------------------------------------------- 
    /**
     *
     * @param text
     */
    private void contactarSoporte(Hyperlink text) {
        String idioma = main.getResource().getLocale().getLanguage().toUpperCase();
        main.getHostService().showDocument("mailto:" + text.getText() + "?Subject=[" + idioma + "] ");
        text.setVisited(false);
    }
    
    protected void actualizarVesionFin() {
        bActualizar.setDisable(false);
    }
    
    
    //---------------------------------------------------UTILS-------------------------------------------------- 
    protected void setLanguague(ResourceBundle rb) {
        this.bActualizar.setText(rb.getString(ResourceLeng.BUTTON_UPDATE));
        double version = main.getVersion();
        this.lVersionActual.setText(String.format(Locale.UK, rb.getString(ResourceLeng.LABEL_CURRENT_VERSION_INFO), version));
        this.urlWiki = rb.getString(ResourceLeng.WIKI_URL);
        this.hAbrirAyuda.setText(rb.getString(ResourceLeng.WIKI_TEXT));
        this.urlNas = rb.getString(ResourceLeng.NAS_URL);
        this.hAyudaNaster.setText(rb.getString(ResourceLeng.NAS_TEXT));
        tCreditos.getChildren().clear();
        tCreditos.getChildren().add(new Text(rb.getString(ResourceLeng.CREDITS_TEXT)));
        int codeSymbol = 8729;
        char symbol = (char) Character.toLowerCase(codeSymbol); //el punto
        Hyperlink link;
        for (Map.Entry<String, String[]> e : ResourceLeng.CREDITS.entrySet()) {
            tCreditos.getChildren().add(new Text("\n\t" + symbol + e.getKey()));
            for (int i = 0; i < e.getValue().length; i++) {
                tCreditos.getChildren().add(new Text("\n\t\t"));
                link = new Hyperlink(e.getValue()[i]);
                link.setOnMouseClicked(actions -> {
                    contactarSoporte((Hyperlink) actions.getSource());
                });
                link.setBorder(Border.EMPTY);
                tCreditos.getChildren().add(link);
            }
        }
    }
    
    protected void abrirAyuda(){
        this.abrirAyudaWiki();
    }
    
    //---------------------------------------------------INIT---------------------------------------------------
    public void init(MainController mainController) {
        main = mainController;
    }
}
