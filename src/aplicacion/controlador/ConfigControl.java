package aplicacion.controlador;

//#1 Static import
import tools.lenguaje.ResourceLeng;
//#4 Java
import java.util.ResourceBundle;

/**
 * 114
 * @author Diego Alvarez
 */
public class ConfigControl {

    /**
     * Max Syncronization Before Clean List Update
     */
    protected static final int MAX_SBCLU = 1;
    
    // C2 el estado de la entrega
    protected static String state0Text;
    protected static String state0Tool;
    protected static String state1Text;
    protected static String state1Tool;
    protected static String state2Text;
    protected static String state2Tool;
    protected static String state3Text;
    protected static String state3Tool;
    protected static String state4Text;
    protected static String state4Tool;
    protected static String state5Text;
    protected static String state5Tool;
    protected static String state6Text;
    protected static String state6Tool;
    protected static String state7Text;
    protected static String state7Tool;
    protected static String state8Text;
    protected static String state8Tool;
    protected static String state9Text;
    protected static String state9Tool;
    
    // C3 tiempo restante
    protected static String seedTime;
    protected static String seedTimeNoDays;
    
    // C4 fichero & nota
    protected static String tableFile;
    protected static String tableNote;
    protected static String tableFeed;
    protected static String feedFile;
    
    // C5 Accion dependiente del estado
    protected static String tButton05Text;
    protected static String tButton05Tool;
    protected static String tButton1267Text;
    protected static String tButton1267Tool;
    protected static String tButton38Text;
    protected static String tButton38Tool;
    protected static String tButton4Text;
    protected static String tButton4Tool;
    protected static String tButton9Text;
    protected static String tButton9Tool;
    
    // El style
    protected static String styleNormal;
    protected static String styleError;
    
    protected static void setLanguage(ResourceBundle rb){
        state0Text = rb.getString(ResourceLeng.STATE_0_TEXT);
        state0Tool = rb.getString(ResourceLeng.STATE_0_TOOL);
        state1Text = rb.getString(ResourceLeng.STATE_1_TEXT);
        state1Tool = rb.getString(ResourceLeng.STATE_1_TOOL);
        state2Text = rb.getString(ResourceLeng.STATE_2_TEXT);
        state2Tool = rb.getString(ResourceLeng.STATE_2_TOOL);
        state3Text = rb.getString(ResourceLeng.STATE_3_TEXT);
        state3Tool = rb.getString(ResourceLeng.STATE_3_TOOL);
        state4Text = rb.getString(ResourceLeng.STATE_4_TEXT);
        state4Tool = rb.getString(ResourceLeng.STATE_4_TOOL);
        state4Text = rb.getString(ResourceLeng.STATE_4_TEXT);
        state4Tool = rb.getString(ResourceLeng.STATE_4_TOOL);
        state5Text = rb.getString(ResourceLeng.STATE_5_TEXT);
        state5Tool = rb.getString(ResourceLeng.STATE_5_TOOL);
        state6Text = rb.getString(ResourceLeng.STATE_6_TEXT);
        state6Tool = rb.getString(ResourceLeng.STATE_6_TOOL);
        state7Text = rb.getString(ResourceLeng.STATE_7_TEXT);
        state7Tool = rb.getString(ResourceLeng.STATE_7_TOOL);
        state8Text = rb.getString(ResourceLeng.STATE_8_TEXT);
        state8Tool = rb.getString(ResourceLeng.STATE_8_TOOL);
        state9Text = rb.getString(ResourceLeng.STATE_9_TEXT);
        state9Tool = rb.getString(ResourceLeng.STATE_9_TOOL);
        
        seedTime = rb.getString(ResourceLeng.FORMAT_TIME);
        seedTimeNoDays = rb.getString(ResourceLeng.FORMAT_TIME_NO_DAYS);
        
        tableFile = rb.getString(ResourceLeng.INFO_FILE);
        tableNote = rb.getString(ResourceLeng.INFO_NOTE);
        tableFeed = rb.getString(ResourceLeng.INFO_FEED);
        feedFile = rb.getString(ResourceLeng.FEEDBACK_FILE);
        
        tButton05Text = rb.getString(ResourceLeng.BUTTON_05_TEXT);
        tButton05Tool = rb.getString(ResourceLeng.BUTTON_05_TOOL);
        tButton1267Text = rb.getString(ResourceLeng.BUTTON_1267_TEXT);
        tButton1267Tool = rb.getString(ResourceLeng.BUTTON_1267_TOOL);
        tButton38Text = rb.getString(ResourceLeng.BUTTON_38_TEXT);
        tButton38Tool = rb.getString(ResourceLeng.BUTTON_38_TOOL);
        tButton4Text = rb.getString(ResourceLeng.BUTTON_4_TEXT);
        tButton4Tool = rb.getString(ResourceLeng.BUTTON_4_TOOL);
        tButton9Text = rb.getString(ResourceLeng.BUTTON_9_TEXT);
        tButton9Tool = rb.getString(ResourceLeng.BUTTON_9_TOOL);
        
        
        styleNormal = rb.getString(ResourceLeng.STYLE_NORMAL);
        styleError = rb.getString(ResourceLeng.STYLE_ERROR);
    }
}