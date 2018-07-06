package aplicacion.controlador;

import Tools.lenguaje.ResourceLeng;
import java.util.ResourceBundle;

/**
 *
 * @author Diego
 */
public class ConfigControl {

    /**
     * Max Syncronization Before Clean List Update
     */
    protected static final int MAX_SBCLU = 1;
    
    protected static String STATE_0_TEXT;
    protected static String STATE_0_TOOL;
    protected static String STATE_1_TEXT;
    protected static String STATE_1_TOOL;
    protected static String STATE_2_TEXT;
    protected static String STATE_2_TOOL;
    protected static String STATE_3_TEXT;
    protected static String STATE_3_TOOL;
    protected static String STATE_4_TEXT;
    protected static String STATE_4_TOOL;
    protected static String STATE_5_TEXT;
    protected static String STATE_5_TOOL;
    protected static String STATE_6_TEXT;
    protected static String STATE_6_TOOL;
    protected static String STATE_7_TEXT;
    protected static String STATE_7_TOOL;
    protected static String STATE_8_TEXT;
    protected static String STATE_8_TOOL;
    
    protected static String seedTime;
    protected static String seedTimeNoDays;
   
    protected static String TABLE_FILE;
    protected static String TABLE_NOTE;
    protected static String TBUTTON_1_TEXT;
    protected static String TBUTTON_1_TOOL;
    protected static String TBUTTON_24_TEXT;
    protected static String TBUTTON_24_TOOL;
    protected static String TBUTTON_35_TEXT;
    protected static String TBUTTON_35_TOOL;
    
    protected static void setLanguage(ResourceBundle rb){
        STATE_0_TEXT = rb.getString(ResourceLeng.STATE_0_TEXT);
        STATE_0_TOOL = rb.getString(ResourceLeng.STATE_0_TOOL);
        STATE_1_TEXT = rb.getString(ResourceLeng.STATE_1_TEXT);
        STATE_1_TOOL = rb.getString(ResourceLeng.STATE_1_TOOL);
        STATE_2_TEXT = rb.getString(ResourceLeng.STATE_2_TEXT);
        STATE_2_TOOL = rb.getString(ResourceLeng.STATE_2_TOOL);
        STATE_3_TEXT = rb.getString(ResourceLeng.STATE_3_TEXT);
        STATE_3_TOOL = rb.getString(ResourceLeng.STATE_3_TOOL);
        STATE_4_TEXT = rb.getString(ResourceLeng.STATE_4_TEXT);
        STATE_4_TOOL = rb.getString(ResourceLeng.STATE_4_TOOL);
        STATE_4_TEXT = rb.getString(ResourceLeng.STATE_4_TEXT);
        STATE_4_TOOL = rb.getString(ResourceLeng.STATE_4_TOOL);
        STATE_5_TEXT = rb.getString(ResourceLeng.STATE_5_TEXT);
        STATE_5_TOOL = rb.getString(ResourceLeng.STATE_5_TOOL);
        STATE_6_TEXT = rb.getString(ResourceLeng.STATE_6_TEXT);
        STATE_6_TOOL = rb.getString(ResourceLeng.STATE_6_TOOL);
        STATE_7_TEXT = rb.getString(ResourceLeng.STATE_7_TEXT);
        STATE_7_TOOL = rb.getString(ResourceLeng.STATE_7_TOOL);
        STATE_8_TEXT = rb.getString(ResourceLeng.STATE_8_TEXT);
        STATE_8_TOOL = rb.getString(ResourceLeng.STATE_8_TOOL);
        
        
        
        
        
        
        
        seedTime = rb.getString(ResourceLeng.FORMAT_TIME);
        seedTimeNoDays = rb.getString(ResourceLeng.FORMAT_TIME_NO_DAYS);
        TABLE_FILE = rb.getString(ResourceLeng.INFO_FILE);
        TABLE_NOTE = rb.getString(ResourceLeng.INFO_NOTE);
        TBUTTON_1_TEXT = rb.getString(ResourceLeng.BUTTON_1_TEXT);
        TBUTTON_1_TOOL = rb.getString(ResourceLeng.BUTTON_1_TOOL);
        TBUTTON_24_TEXT = rb.getString(ResourceLeng.BUTTON_24_TEXT);
        TBUTTON_24_TOOL = rb.getString(ResourceLeng.BUTTON_24_TOOL);
        TBUTTON_35_TEXT = rb.getString(ResourceLeng.BUTTON_35_TEXT);
        TBUTTON_35_TOOL = rb.getString(ResourceLeng.BUTTON_35_TOOL);
    }
}
