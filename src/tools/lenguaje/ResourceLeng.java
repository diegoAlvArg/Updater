package tools.lenguaje;

//#4 Java
import java.util.HashMap;
import java.util.Map;

/**
 * @version 1.0
 * @author Diego Alvarez
 */
public class ResourceLeng extends BasicLeng {

    //**********EXTRAS
    /**
     * Title on title bar
     */
    public static final String APP_TITLE = "app_title";
    public static final String ASK_UPDATE = "question_update";
    public static final String TXT_YES = "txt_yes";
    public static final String TXT_NO = "txt_no";
    
    
    //**********TAB OptionInit
    public static final String TAB_INIT = "tab_init";
    
    
    //**********TAB OpcionTareas****************************************************************
    public static final String TAB_DELIVERY = "tab_dely";
    
    public static final String C1_TEXT = "c1_text";
    public static final String C2_TEXT = "c2_text";
    public static final String C3_TEXT = "c3_text";
    public static final String C4_TEXT = "c4_text";
    public static final String C5_TEXT = "c5_text";
    public static final String STATE_0_TEXT = "state_0_text";
    public static final String STATE_0_TOOL = "state_0_tool";
    public static final String STATE_1_TEXT = "state_1_text";
    public static final String STATE_1_TOOL = "state_1_tool";
    public static final String STATE_2_TEXT = "state_2_text";
    public static final String STATE_2_TOOL = "state_2_tool";
    public static final String STATE_3_TEXT = "state_3_text";
    public static final String STATE_3_TOOL = "state_3_tool";
    public static final String STATE_4_TEXT = "state_4_text";
    public static final String STATE_4_TOOL = "state_4_tool";
    public static final String STATE_5_TEXT = "state_5_text";
    public static final String STATE_5_TOOL = "state_5_tool";
    public static final String STATE_6_TEXT = "state_6_text";
    public static final String STATE_6_TOOL = "state_6_tool";
    public static final String STATE_7_TEXT = "state_7_text";
    public static final String STATE_7_TOOL = "state_7_tool";
    public static final String STATE_8_TEXT = "state_8_text";
    public static final String STATE_8_TOOL = "state_8_tool";
    public static final String STATE_9_TEXT = "state_9_text";
    public static final String STATE_9_TOOL = "state_9_tool";
    
    public static final String DELIVER_ADD = "deliver_add";
    public static final String DELIVER_DELETE = "deliver_delete";
    public static final String DELIVER_UPLOAD = "deliver_upload";
    public static final String DELIVER_NO_ACTUAL_TITLE = "deliver_no_actual_title";
    public static final String DELIVER_NO_ACTUAL_TEXT = "deliver_no_actual_text";
    public static final String DELIVER_ERROR_TITLE = "deliver_error_title";
    public static final String DELIVER_ERROR_TEXT = "deliver_error_text";
    public static final String FORMAT_TIME = "format_time";
    public static final String FORMAT_TIME_NO_DAYS = "format_time_no_days";
    public static final String INFO_FILE = "info_file";
    public static final String INFO_NOTE = "info_note";
    public static final String INFO_FEED = "info_feed";
    public static final String FEEDBACK_FILE = "feedback_file";
    
    public static final String BUTTON_05_TEXT = "button_05_text";
    public static final String BUTTON_05_TOOL = "button_05_tool";
    public static final String BUTTON_1267_TEXT = "button_1267_text";
    public static final String BUTTON_1267_TOOL = "button_1267_tool";
    public static final String BUTTON_38_TEXT = "button_38_text";
    public static final String BUTTON_38_TOOL = "button_38_tool";
    public static final String BUTTON_4_TEXT = "button_4_text";
    public static final String BUTTON_4_TOOL = "button_4_tool";
    public static final String BUTTON_9_TEXT = "button_9_text";
    public static final String BUTTON_9_TOOL = "button_9_tool";
    
    public static final String STYLE_NORMAL = "style_normal";
    public static final String STYLE_ERROR = "style_error";
    
    public static final String FILECHOOSER_PERMISIONS_TITLE = "filechooser_permisions_title";
    public static final String FILECHOOSER_PERMISIONS_TEXT = "filechooser_permisions_text";
    
    public static final String BUTTON_24_TEXT = "button_24_text";
    public static final String BUTTON_24_TOOL = "button_24_tool";
    public static final String BUTTON_35_TEXT = "button_35_text";
    public static final String BUTTON_35_TOOL = "button_35_tool";
    
    public static final String ERROR_RECOVER_TITLE = "error_recover_title";
    public static final String ERROR_RECOVER_TEXT = "error_recover_text";
    public static final String INFO_LEGACY_FILE_TITLE = "info_legacy_file_title";
    public static final String INFO_LEGACY_FILE_TEXT = "info_legacy_file_text";
    
    
    //**********TAB OptionConfig
    public static final String TAB_CONFIG = "tab_config";
    public static final String LANGUAGE = "language";
    public static final Map<String, String> LANGUAGES;
    static {
        LANGUAGES = new HashMap(); //ISO 639-1
        LANGUAGES.put("es", "lantag_spanish");
        LANGUAGES.put("en", "lantag_english");
    }
   
    public static final String TIME_HOUR_TEXT = "time_hour_text";
    public static final String TIME_MINUT_TEXT = "time_minut_text";
    public static final String TIME_BUTTON_TEXT = "time_button_text";
    public static final String TIME_LABEL = "time_label";
    
    public static final String ASK_TITLE_NEW_USER = "ask_title_new_user";
    public static final String ASK_TITLE_EDIT_USER = "ask_title_edit_user";
    public static final String ASK_BUTTON_ACCEPT = "ask_button_accept";
    public static final String ASK_BUTTON_CANCEL = "ask_button_cancel";
    public static final String ASK_LABEL_USER = "ask_label_user";
    public static final String ASK_LABEL_PASS1 = "ask_label_pass1";
    public static final String ASK_LABEL_PASS2 = "ask_label_pass2";
    public static final String ASK_LABEL_PATH = "ask_label_path";
    public static final String ASK_LABEL_USE_NAS = "ask_label_use_nas";
    
    public static final String ASK_TOOLTIP_USER = "ask_tooltip_user";
    public static final String ASK_TOOLTIP_PASS_MOODLE = "ask_tooltip_pass_moodle";
    public static final String ASK_TOOLTIP_PASS_NASTER = "ask_tooltip_pass_naster";
    public static final String ASK_TOOLTIP_NASTER = "ask_tooltip_naster";
    public static final String ASK_TOOLTIP_PATH = "ask_tooltip_path";
    public static final String ASK_FIELD_USER = "ask_field_user";
    public static final String ASK_FIELD_PASS = "ask_field_pass";
    public static final String ASK_FIELD_PATH = "ask_field_path";
    
    public static final String MESSAGE_TITLE = "message_title";
    public static final String MESSAGE_TITLE_MOODLE_DOWN = "message_title_moodle_down";
    public static final String MESSAGE_TITLE_NASTER_DOWN = "message_title_naster_down";
    public static final String MESSAGE_INFO_DOWN_TEXT = "message_info_down_text";
    public static final String MESSAGE_TITLE_FIELD_EMPTY = "message_title_field_empty";
    public static final String MESSAGE_INFO_FIELD_EMPTY = "message_info_field_empty";
    public static final String MESSAGE_TITLE_PATH_REJECT = "message_title_path_reject";
    public static final String MESSAGE_INFO_PATH_REJECT = "message_info_path_reject";
    public static final String MESSAGE_INFO_PATH_NO_SPACE = "message_info_path_no_space";
    public static final String MESSAGE_TITLE_MOODLE_REJECT = "message_title_moodle_reject";
    public static final String MESSAGE_INFO_MOODLE_REJECT = "message_info_moodle_reject";
    public static final String MESSAGE_TITLE_NASTER_REJECT = "message_title_naster_reject";
    public static final String MESSAGE_INFO_NASTER_REJECT = "message_info_naster_reject";
    
    public static final String LABEL_PATH_DOWNLOAD = "label_path_download";
    public static final String LABEL_NEXT_UPDATE = "label_next_update";
    public static final String BUTTON_UPDATE_MOODLE = "button_update_moodle";
    public static final String LABEL_CHECK_DATES = "label_check_dates";
    public static final String MESSAGE_TITLE_DATES_OK = "message_title_dates_ok";
    
    public static final String DAY_TODAY = "day_today";
    public static final String DAY_TOMORROW = "day_tomorrow";
    public static final String NEXT_TIME_SEED = "next_time_seed";
    
    public static final String SYNCRO_NOW = "syncro_now";
    public static final String TOOLTIP_NEWUSER = "tooltip_newuser";
    public static final String TOOLTIP_EDITUSER = "tooltip_edituser";
    public static final String TOOLTIP_SETTIME = "tooltip_settime";
    public static final String NONE = "none";
    
    public static final String ERROR_DATA_TITLE = "error_data_title";
    public static final String ERROR_DATA_TEXT = "error_data_text";
    public static final String SYNCRO_END_TITLE = "syncro_end_title";
    public static final String SYNCRO_END_NO_NEWS = "syncro_end_no_news";
    public static final String SYNCRO_END_RESOURCES = "syncro_end_resources";
    public static final String SYNCRO_END_DELIVERY = "syncro_end_delivery";
    public static final String VALIDATING_DATA = "validating_data";
    
    
    //**********TAB OptionAyuda****************************************************************
    public static final String TAB_HELP = "tab_help";
    
    public static final String BUTTON_UPDATE = "btt_update";
    public static final String LABEL_CURRENT_VERSION_INFO = "lbl_current_version_info";
    
    public static final String WIKI_URL = "wiki_url";
    public static final String WIKI_TEXT = "wiki_text";
    public static final String NAS_URL = "nas_url";
    public static final String NAS_TEXT = "nas_text";
    
    public static final String CREDITS_TEXT = "credits_text";
    public static final Map<String, String[]> CREDITS;
    static {
        CREDITS = new HashMap();
        CREDITS.put("Diego Alvarez", new String[]{"diegoAlvArg@gmail.com"});
        CREDITS.put("Isabe Alvarez", new String[]{"diegoAlvArg@gmail.com", "ISAlvArg@gmail.com"});
    }
    
    public static final String UPDATE_TITLE = "update_title";
    public static final String UPDATE_HEADER = "update_header";
    public static final String UPDATE_CONTENT = "update_content";
    public static final String UPDATE_INFO = "update_info";
    public static final String UPDATE_INFO_TEXT = "update_info_text";
    public static final String UPDATE_NO_ETHERNET = "update_no_ethernet";
    public static final String UPDATE_NO_ETHERNET_TEXT = "update_no_ethernet_text";
    public static final String UPDATE_ERROR_FILE = "update_error_file";
    public static final String UPDATE_ERROR_FILE_TEXT = "update_error_file_text";
    

    //**********SystemTray Options**************************************************************
    public static final String SYS_TRAY_TOOLTIP = "sys_tray_tooltip";
    public static final String SYS_TRAY_EXIT = "sys_tray_exit";
    public static final String SYS_TRAY_OPEN = "sys_tray_open";
    public static final String SYS_TRAY_WIKI = "sys_tray_wiki";
    public static final String SYS_TRAY_UPDATE = "sys_tray_update";
    public static final String SYS_TRAY_SYNCRO = "sys_tray_syncro";

    
    //**********Traces Log**********************************************************************
    public static final String TRACE_INIT = "trace_init";
    public static final String TRACE_USER_NO = "trace_user_no";
    public static final String TRACE_USER_OK = "trace_user_ok";
    public static final String TRACE_USER_LOST = "trace_user_lost";
    public static final String TRACE_DATES_ERROR = "trace_dates_error";
    public static final String TRACE_TREE_ERROR = "trace_tree_error";
    public static final String TRACE_LANGUAGE_UNKNOW = "trace_language_unknow";
    public static final String TRACE_LANGUAGE_OK = "trace_language_ok";
    public static final String TRACE_END_APP = "trace_end_app";
    public static final String TRACE_END_SYSTRAY = "trace_end_systray";
    public static final String TRACE_USE_SYSTRAY = "trace_use_systray";
    public static final String TRACE_ERROR_DATES_CORRUPT = "trace_error_dates_corrupt";
    public static final String TRACE_TIMER_END = "trace_timer_end";
    public static final String TRACE_TIMER_LATE = "trace_timer_late";
    public static final String TRACE_LANGUAGUE_FAULT = "trace_languague_fault";
    public static final String TRACE_INIT_LOAD_XML = "trace_init_load_xml";
    public static final String TRACE_INIT_LOAD_CONTROL = "trace_init_load_control";
    public static final String TRACE_EVENT_CHANGE_PATH = "trace_event_change_path";
    public static final String TRACE_EVENT_USER_NEW = "trace_event_user_new";
    public static final String TRACE_EVENT_USER_EDIT = "trace_event_user_edit";
    public static final String TRACE_EVENT_USER_END = "trace_event_user_end";
    public static final String TRACE_STORE_SAVE = "trace_store_save";
    public static final String TRACE_UPDATE_CONNECT = "trace_update_connect";
    public static final String TRACE_STORE_LOAD_FILE = "trace_store_load_file";
    public static final String TRACE_STORE_LOAD_DATA = "trace_store_load_data";
    

    //**********OTHER*************************************************************************
    public static final String MESSAGE_TITLE_UPDATE_FAIL = "message_title_update_fail";
    public static final String MESSAGE_TEXT_UPDATE_FAIL = "message_text_update_fail";
    public static final String BUSSY_USER_TITLE = "bussy_user_title";
    public static final String BUSSY_USER_TEXT = "bussy_user_text";    
}
