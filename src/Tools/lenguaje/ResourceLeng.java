/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Tools.lenguaje;

//#4 Java
import java.util.HashMap;
import java.util.Map;

/**
 * @version 1.0
 * @author Diego Alvarez
 */
public class ResourceLeng extends BasicLeng {

    /**
     * Title on title bar
     */
    public final static String APP_TITLE = "app_title";

    /**
     * Text from main button application
     */
    public final static String HELLO_BUTTON = "hello_button";

    /**
     * Text for log
     */
    public final static String HELLO_LOG = "hello_log";

    /**
     * Text
     */
    public final static String HELLO_WORLD = "hello_world";

    public final static String ASK_UPDATE = "question_update";

    public final static String TXT_YES = "txt_yes";

    public final static String TXT_NO = "txt_no";
    //**********TAB OptionInit
    public final static String TAB_INIT = "tab_init";
    //**********TAB OptionConfig
    public final static String TAB_CONFIG = "tab_config";
    public final static String LANGUAGE = "language";
    public final static Map<String, String> LANGUAGES;

    static {
        LANGUAGES = new HashMap<>(); //ISO 639-1
        LANGUAGES.put("es", "lantag_spanish");
        LANGUAGES.put("en", "lantag_english");
    }
    public final static String TIME_HOUR_TEXT = "time_hour_text";
    public final static String TIME_MINUT_TEXT = "time_minut_text";
    public final static String TIME_BUTTON_TEXT = "time_button_text";
    public final static String TIME_LABEL = "time_label";
    public final static String ASK_TITLE_NEW_USER = "ask_title_new_user";
    public final static String ASK_TITLE_EDIT_USER = "ask_title_edit_user";
    public final static String ASK_BUTTON_ACCEPT = "ask_button_accept";
    public final static String ASK_BUTTON_CANCEL = "ask_button_cancel";
    public final static String ASK_LABEL_USER = "ask_label_user";
    public final static String ASK_LABEL_PASS1 = "ask_label_pass1";
    public final static String ASK_LABEL_PASS2 = "ask_label_pass2";
    public final static String ASK_LABEL_PATH = "ask_label_path";
    public final static String ASK_LABEL_USE_NAS = "ask_label_use_nas";
    
    public final static String ASK_TOOLTIP_NASTER = "ask_tooltip_naster";

    public final static String ASK_FIELD_USER = "ask_field_user";
    public final static String ASK_FIELD_PASS = "ask_field_pass";
    public final static String ASK_FIELD_PATH = "ask_field_path";

    public final static String MESSAGE_TITLE = "message_title";
    
    public final static String MESSAGE_TITLE_MOODLE_DOWN = "message_title_moodle_down";
    public final static String MESSAGE_TITLE_NASTER_DOWN = "message_title_naster_down";
    public final static String MESSAGE_TITLE_FIELD_EMPTY = "message_title_field_empty";
    public final static String MESSAGE_TITLE_PATH_REJECT = "message_title_path_reject";
    public final static String MESSAGE_INFO_DOWN_TEXT = "message_info_down_text";
    public final static String MESSAGE_INFO_FIELD_EMPTY = "message_info_field_empty";
    public final static String MESSAGE_INFO_PATH_REJECT = "message_info_path_reject";
    
    public final static String MESSAGE_TITLE_MOODLE_REJECT = "message_title_moodle_reject";
    public final static String MESSAGE_INFO_MOODLE_REJECT = "message_info_moodle_reject";
//    public final static String MESSAGE_INFO_MOODLE_REJECT = "message_info_moodle_reject";
    public final static String MESSAGE_TITLE_NASTER_REJECT = "message_title_naster_reject";
    public final static String MESSAGE_INFO_NASTER_REJECT = "message_info_naster_reject";

    public final static String LABEL_PATH_DOWNLOAD = "label_path_download";
    public final static String LABEL_NEXT_UPDATE = "label_next_update";
    public final static String BUTTON_UPDATE_MOODLE = "button_update_moodle";
    public final static String LABEL_CHECK_DATES = "label_check_dates";
    public final static String MESSAGE_TITLE_DATES_OK = "message_title_dates_ok";
    
    public final static String DAY_TODAY = "day_today";
    public final static String DAY_TOMORROW = "day_tomorrow";
    public final static String NEXT_TIME_SEED = "next_time_seed";
    
    public final static String SYNCRO_NOW = "syncro_now";
    public final static String TOOLTIP_NEWUSER = "tooltip_newuser";
    public final static String TOOLTIP_EDITUSER = "tooltip_edituser";
    public final static String TOOLTIP_SETTIME = "tooltip_settime";
    public final static String NONE = "none";
    public final static String ERROR_DATA_TITLE = "error_data_title";
    public final static String ERROR_DATA_TEXT = "error_data_text";
    //**********TAB OptionAyuda****************************************************************
    public final static String TAB_HELP = "tab_help";
    public final static String BUTTON_UPDATE = "btt_update";
    public final static String LABEL_CURRENT_VERSION_INFO = "lbl_current_version_info";
    public final static String WIKI_URL = "wiki_url";
    public final static String WIKI_TEXT = "wiki_text";
    public final static String NAS_URL = "nas_url";
    public final static String NAS_TEXT = "nas_text";
    public final static String CREDITS_TEXT = "credits_text";
    public static final Map<String, String[]> CREDITS;

    static {
        CREDITS = new HashMap<>();
        CREDITS.put("Diego Alvarez", new String[]{"diegoAlvArg@gmail.com"});
        CREDITS.put("Isabe Alvarez", new String[]{"diegoAlvArg@gmail.com", "ISAlvArg@gmail.com"});
    }
    public final static String UPDATE_TITLE = "update_title";
    public final static String UPDATE_HEADER = "update_header";
    public final static String UPDATE_CONTENT = "update_content";
    public final static String UPDATE_INFO = "update_info";
    public final static String UPDATE_INFO_TEXT = "update_info_text";
    
    //**********TAB OpcionTareas****************************************************************
    public final static String TAB_DELIVERY = "tab_dely";
    public final static String C1_TEXT = "c1_text";
    public final static String C2_TEXT = "c2_text";
    public final static String C3_TEXT = "c3_text";
    public final static String C4_TEXT = "c4_text";
    public final static String C5_TEXT = "c5_text";
    public final static String STATE_1_TEXT = "state_1_text";
    public final static String STATE_1_TOOL = "state_1_tool";
    public final static String STATE_2_TEXT = "state_2_text";
    public final static String STATE_2_TOOL = "state_2_tool";
    public final static String STATE_3_TEXT = "state_3_text";
    public final static String STATE_3_TOOL = "state_3_tool";
    public final static String STATE_4_TEXT = "state_4_text";
    public final static String STATE_4_TOOL = "state_4_tool";
    public final static String STATE_5_TEXT = "state_5_text";
    public final static String STATE_5_TOOL = "state_5_tool";
    public final static String FORMAT_TIME = "format_time";
    public final static String FORMAT_TIME_NO_DAYS = "format_time_no_days";
    public final static String INFO_FILE = "info_file";
    public final static String INFO_NOTE = "info_note";
    public final static String BUTTON_1_TEXT = "button_1_text";
    public final static String BUTTON_1_TOOL = "button_1_tool";
    public final static String BUTTON_24_TEXT = "button_24_text";
    public final static String BUTTON_24_TOOL = "button_24_tool";
    public final static String BUTTON_35_TEXT = "button_35_text";
    public final static String BUTTON_35_TOOL = "button_35_tool";
    
    //**********SystemTray Options**************************************************************
    public final static String SYS_TRAY_TOOLTIP = "sys_tray_tooltip";
    public final static String SYS_TRAY_EXIT = "sys_tray_exit";
    public final static String SYS_TRAY_OPEN = "sys_tray_open";
    public final static String SYS_TRAY_WIKI = "sys_tray_wiki";
    public final static String SYS_TRAY_UPDATE = "sys_tray_update";
    public final static String SYS_TRAY_SYNCRO = "sys_tray_syncro";
    
    //**********Traces Log**********************************************************************
    public final static String TRACE_INIT = "trace_init";
    public final static String TRACE_USER_NO = "trace_user_no";
    public final static String TRACE_USER_OK = "trace_user_ok";
    public final static String TRACE_USER_LOST = "trace_user_lost";
    public final static String TRACE_DATES_ERROR = "trace_dates_error";
    public final static String TRACE_TREE_ERROR = "trace_tree_error";
    public final static String TRACE_LANGUAGE_UNKNOW = "trace_language_unknow";
    public final static String TRACE_LANGUAGE_OK = "trace_language_ok";
    public final static String TRACE_END_APP = "trace_end_app";
    public final static String TRACE_END_SYSTRAY = "trace_end_systray";
    public final static String TRACE_USE_SYSTRAY = "trace_use_systray";
    public final static String TRACE_ERROR_DATES_CORRUPT = "trace_error_dates_corrupt";
    public final static String TRACE_TIMER_END = "trace_timer_end";
    public final static String TRACE_TIMER_LATE = "trace_timer_late";
    public final static String TRACE_LANGUAGUE_FAULT = "trace_languague_fault";
    public final static String TRACE_INIT_LOAD_XML = "trace_init_load_xml";
    public final static String TRACE_INIT_LOAD_CONTROL = "trace_init_load_control";
    public final static String TRACE_EVENT_USER_NEW = "trace_event_user_new";
    public final static String TRACE_EVENT_USER_EDIT = "trace_event_user_edit";
    public final static String TRACE_EVENT_USER_END = "trace_event_user_end";
    
    //**********OTHER*************************************************************************
    public final static String MESSAGE_TITLE_UPDATE_FAIL = "message_title_update_fail";
    public final static String MESSAGE_TEXT_UPDATE_FAIL = "message_text_update_fail";
    
}
