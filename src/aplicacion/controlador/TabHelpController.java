package aplicacion.controlador;

import Tools.lenguaje.ResourceLeng;
//import aplicacion.HelloWorld;
//import aplicacion.controlador.InterfaceController;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.Border;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import aplicacion.controlador.MainController;

/**
 *
 * @author Usuario
 */
public class TabHelpController {// implements Initializable{

    private MainController main;

    @FXML
    private Button BActualizar;
    @FXML
    private Label LCurrentVersion;
    @FXML
    private Hyperlink HopenHelp;
    private String urlWiki;
    @FXML
    private TextFlow TCredits;
    @FXML
    private Hyperlink HnasterHelp;
    private String urlNas;

    
    //---------------------------------------------------FXML---------------------------------------------------
    @FXML
    private void openHelpWiki() {
        main.getHostService().showDocument(this.urlWiki);
    }

    @FXML
    private void openHelpNas() {
        main.getHostService().showDocument(this.urlNas);
    }

    /**
     *
     * Metodo que genera el evento de actualizar la App
     */
    @FXML
    private void actualizarVersion() {
        BActualizar.setDisable(true);
        
        main.cambiarDisponibilidadOpcionSysTray(ResourceLeng.SYS_TRAY_UPDATE, false);
        main.actualizarVersion(true);
    }
  
    
    //---------------------------------------------------EVENTO------------------------------------------------- 
    /**
     *
     * @param text
     */
    private void credictContact(Hyperlink text) {
        String idioma = main.getResource().getLocale().getLanguage().toUpperCase();
        main.getHostService().showDocument("mailto:" + text.getText() + "?Subject=[" + idioma + "] ");
        text.setVisited(false);
    }

    protected void actualizarVesionEnd() {
//        System.err.println("Reactivando....");
        BActualizar.setDisable(false);
    }
    
    
    //---------------------------------------------------UTILS-------------------------------------------------- 
    protected void setLanguague(ResourceBundle rb) {
        System.out.println("tab4 is changing language");
//        this.OptionAyuda.setText(rb.getString(ResourceLeng.TAB_HELP));
        this.BActualizar.setText(rb.getString(ResourceLeng.BUTTON_UPDATE));
        int version = main.getVersion();
        this.LCurrentVersion.setText(String.format(rb.getString(ResourceLeng.LABEL_CURRENT_VERSION_INFO), version));
        this.urlWiki = rb.getString(ResourceLeng.WIKI_URL);
        this.HopenHelp.setText(rb.getString(ResourceLeng.WIKI_TEXT));
        this.urlNas = rb.getString(ResourceLeng.NAS_URL);
        this.HnasterHelp.setText(rb.getString(ResourceLeng.NAS_TEXT));
        TCredits.getChildren().clear();
        TCredits.getChildren().add(new Text(rb.getString(ResourceLeng.CREDITS_TEXT)));
        int codeSymbol = 8729;
        char symbol = (char) Character.toLowerCase(codeSymbol);
        Hyperlink link;
        for (Map.Entry<String, String[]> e : ResourceLeng.CREDITS.entrySet()) {
            TCredits.getChildren().add(new Text("\n\t" + symbol + e.getKey()));
            for (int i = 0; i < e.getValue().length; i++) {
                TCredits.getChildren().add(new Text("\n\t\t"));
                link = new Hyperlink(e.getValue()[i]);
                link.setOnMouseClicked(actions -> {
                    credictContact((Hyperlink) actions.getSource());
                });
                link.setBorder(Border.EMPTY);
                TCredits.getChildren().add(link);
            }
        }
    }
    
    
    //---------------------------------------------------INIT---------------------------------------------------
    public void init(MainController mainController) {
        main = mainController;
    }
    

}
