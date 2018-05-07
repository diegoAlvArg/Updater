/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Updater.tools;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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

    public final static String ASK_FIELD_USER = "ask_field_user";
    public final static String ASK_FIELD_PASS = "ask_field_pass";
    public final static String ASK_FIELD_PATH = "ask_field_path";

    public final static String MESSAGE_TITLE = "message_title";
    public final static String MESSAGE_INFO_MOODLE = "message_info_moodle";
    public final static String MESSAGE_TITLE_MOODLE_DOWN = "message_title_moodle_down";
    public final static String MESSAGE_TITLE_NASTER_DOWN = "message_title_naster_down";
    public final static String MESSAGE_TITLE_FIELD_EMPTY = "message_title_field_empty";
    public final static String MESSAGE_TITLE_PATH_REJECT = "message_title_path_reject";
    public final static String MESSAGE_INFO_DOWN_TEXT = "message_info_down_text";
    public final static String MESSAGE_INFO_FIELD_EMPTY = "message_info_field_empty";
    public final static String MESSAGE_INFO_PATH_REJECT = "message_info_path_reject";
    
    public final static String MESSAGE_TITLE_MOODLE_REJECT = "message_title_moodle_reject";
    public final static String MESSAGE_TITLE_NASTER_REJECT = "message_title_naster_reject";
    public final static String MESSAGE_INFO_REJECT = "message_info_reject";

    public final static String LABEL_PATH_DOWNLOAD = "label_path_download";
    public final static String LABEL_NEXT_UPDATE = "label_next_update";
    public final static String BUTTON_UPDATE_MOODLE = "button_update_moodle";
    public final static String LABEL_CHECK_DATES = "label_check_dates";
    public final static String MESSAGE_TITLE_DATES_OK = "message_title_dates_ok";
    
    public final static String DAY_TODAY = "day_today";
    public final static String DAY_TOMORROW = "day_tomorrow";
    public final static String NEXT_TIME_SEED = "next_time_seed";
    
    public final static String SYNCRO_NOW = "syncro_now";
   
    
    
    //**********TAB OptionAyuda****************************************************************
    public final static String TAB_HELP = "tab_help";
    public final static String BUTTON_UPDATE = "btt_update";
    public final static String LABEL_CURRENT_VERSION_INFO = "lbl_current_version_info";
    public final static String WIKI_URL = "wiki_url";
    public final static String WIKI_TEXT = "wiki_text";
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
}
