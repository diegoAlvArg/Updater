package sincronizacion.moodle.estructura;

/**
 *
 * @author Diego Alvarez
 */
public class MarcasScrapping {

    final String SALTAR_NAVEGACION = "Saltar Navegacion";
    final String SEMILLA = "<div class=\"mod-indent mod-indent-%d\"></div>";
    final String COOKIE_SESION = "MoodleSession";
    final String DOMINIO_MOODLE = ".moodle2.unizar.es";
    final String EXISTO = "Suscesfull update";
    final String SECCIONES_DIVISION = "<li id=\"section-[0-9]+";
    final String SECCIONES_CABECERA = "<li id=\"section-";
    final String SECCIONES_AREA_TITULO_SENIAL = "<div class=\"no-overflow\">";
    final String SECCIONES_AREA_TITULO_ZONA = "<div class=\"no-overflow\"><div class=\"no-overflow\">";
    final String SECCIONES_AREA_ETIQUETA = "<li class=\"";
    final String SECCIONES_AREA_ETIQUETA_FIN = "\" id=";
    final String SECCIONES_AREA_SENIAL = "<div class=\"";
    final String SECCIONES_AREA_SENIAL_FIN = "\"></div>";
    final String SECCIONES_SPAN_DIVISION = "<span ";
    final String JSOUP_LI_ETIQUETA = "li";
    final String JSOUP_A_ETIQUETA = "a";
    final String JSOUP_SPAN_ETIQUETA = "span";
    final String JSOUP_LI_ETIQUETA_CLASS = "<li class=";
    final String JSOUP_CARPETA_BUSCA_LINKS = "span[id=\"maincontent\"]+h2+div>div>ul>li>ul>li>span>a";
    final String JSOUP_SECCIONES = "<div>[\\s]+<div class=\"mod-indent-outer\">";
    final String JSOUP_SECCIONES_COLLAPSADAS = "h3>a";
    final String JSOUP_SECCIONES_EXPANDIDAS_NOMBRE = "li>span";
    final String JSOUP_SECCIONES_AREA_DATOS = "li[role]";
    final String JSOUP_SECCIONES_AREA_TITULO = "div[class=\"no-overflow\"]>div[class=\"no-overflow\"]>p";
    final String JSOUP_SECCIONES_AREA_TIPO = "li>div[class]";
    final String P_ETIQUETA_FIN = "</p>";
    final String ETIQUETA_FIN = "</";
    final String LI_ETIQEUTA = "activity label modtype_label ";
    final String LIMPIEZA_NOMBRE = "class=\"[A-Za-z\\s]*\">";
    final String LIMPIEZA_TIPO = "class=\"[A-Za-z\\s]*\"> ";
    final String LIMPIEZA_TIPO_FIN = "<\\/span>";
    final String ENLACE_NOMBRE_DEFECTO = "Enlace ";
    final String EXTENSION_ARCHIVO = "\\.(?=[^\\.]+$)";
    final String NO_REDIRECCION_AREA = "<div id=\"content\" class=\"span9";
    final String NO_RIDIRECCION_INDICE = "https://moodle2.unizar.es/add/pluginfile.php/";
    final String NO_RIDIRECCION_INDICE_FIN = "\" alt=\"\" />";
    final String A_RESERVADOR_01 = "mailto";
}
