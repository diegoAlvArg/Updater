package Sincronice.moodle.tree;

//import Util.Logger.MyLogging;
import application.InterfaceController;
import Sincronice.Moodle.init.Opciones;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.apache.http.HttpEntity;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import Sincronice.Moodle.tools.MyLogging;

/**
 * @author Diego Alvarez
 * @version 1.3
 * 
 * Clase que forma una estructura arborea representativa del contenido Moodle y
 *  descarga o crea el contenido en replica en el Sistema Local
 */
public class Node {

    private String url;
    private String nombre;
    private TypeNode tipo;
    private Map<String, String> cookies;

    //*******SET PARA EVITAR DUPLICADOS
    private Set<Node> secciones;
    private Set<Node> archivos;

    //********MARKs ON SCRAPPING
    final String SALTAR_NAVEGACION = "Saltar Navegación";
    //*******Seed para identificar identados
    final String SEMILLA = "<div class=\"mod-indent mod-indent-%d\"></div>";

    final String COOKIE_SESION = "MoodleSession";
    final String DOMINIO_MOODLE = ".moodle2.unizar.es";
    final String EXISTO = "Suscesfull update";

    //****** SPLIT
    final String SECCIONES_DIVISION = "<li id=\"section-[0-9]+";
    final String SECCIONES_CABECERA = "<li id=\"section-";
    final String SECCIONES_AREA_TITULO_SENIAL = "<div class=\"no-overflow\">";
    final String SECCIONES_AREA_TITULO_ZONA = "<div class=\"no-overflow\"><div class=\"no-overflow\">";
    final String SECCIONES_AREA_ETIQUETA = "<li class=\"";
    final String SECCIONES_AREA_ETIQUETA_FIN = "\" id=";
    final String SECCIONES_AREA_SENIAL = "<div class=\"";
    final String SECCIONES_AREA_SENIAL_FIN = "\"></div>";
    final String SECCIONES_SPAN_DIVISION = "<span ";

    //****** JSOUP
    final String JSOUP_LI_ETIQUETA = "li";
    final String JSOUP_A_ETIQUETA = "a";
    final String JSOUP_SPAN_ETIQUETA = "span";
    final String JSOUP_LI_ETIQUETA_CLASS = "<li class=";
    final String JSOUP_CARPETA_BUSCA_LINKS = "span[id=\"maincontent\"]+h2+div>div>ul>li>ul>li>span>a";
    /**
     * \s A whitespace character, short for [ \t\n\x0b\r\f]
     */
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

    /**
     * Constructor
     *
     * @param url URL la cual el Node representa
     * @param nombre nombre del Node
     * @param tipo tipo de Node
     * @param cookies Map de cookies que el Node utilizara para conectar a _url
     */
    public Node(String url, String nombre, TypeNode tipo, Map<String, String> cookies) {
        this.url = url;

        String auxCadena = nombre;
        auxCadena = auxCadena.replaceAll("\\\\", "_");
        auxCadena = auxCadena.replaceAll("/", "-");
        auxCadena = auxCadena.replaceAll(":", ",");
        auxCadena = auxCadena.replaceAll("¿", "");
        auxCadena = auxCadena.replaceAll("\\?", "");
        auxCadena = auxCadena.replaceAll("\\*", "");
        auxCadena = auxCadena.replaceAll("\"", "'");
        auxCadena = auxCadena.replaceAll("<", "'");
        auxCadena = auxCadena.replaceAll(">", "'");
        auxCadena = auxCadena.replaceAll("\\|", "'");
        while (auxCadena.endsWith(".")) {
            auxCadena = auxCadena.substring(0, auxCadena.length() - 1);
        }
        this.nombre = auxCadena;
        this.tipo = tipo;
        this.cookies = cookies;
    }

    /**
     *
     * @return
     */
    public String getUrl() {
        return url;
    }

    /**
     *
     * @return
     */
    public String getName() {
        return nombre;
    }

    /**
     *
     * @return
     */
    public TypeNode getType() {
        return tipo;
    }

    /**
     * Descenso del Node, el Node conectara a su URL asociada y procesara las
     * sections 'li' conforme a su TYPE
     *
     * @see https://jsoup.org/apidocs/org/jsoup/Connection.html#get--
     * @see https://jsoup.org/cookbook/extracting-data/selector-syntax
     */
    public void descender() {
        LogRecord logRegistro = null;
        String[] sectionsAray;
        Document doc;
        Document auxDoc;
        try {
            // Doblamos el timeout porque parece tenet problemas de tiempo de conexion 
            //  al haber variso Nodes usando la Connect
            doc = Jsoup.connect(url)
                    .timeout(180 * 1000)
                    .cookies(cookies)
                    .get();

            sectionsAray = doc.toString().split(SECCIONES_DIVISION);

            // 0 - Cabeceras antes de la primera section
            // 1 - Section general
            // N - Section N
            if (tipo == TypeNode.CURSO) {
                procesarSeccion(Jsoup.parse(SECCIONES_CABECERA + "1" + sectionsAray[1]), this, true);
            }

            // Resto de sections
            for (int index = 2; index < sectionsAray.length; index++) {
                if (index == sectionsAray.length - 1) {
                    // Ultimo elemento, para no procesar resto de html que no interesa.
                    auxDoc = Jsoup.parse(SECCIONES_CABECERA + (index - 1) + sectionsAray[index]);
                    procesarSeccion(Jsoup.parse(auxDoc.selectFirst(JSOUP_LI_ETIQUETA).toString()), this, false);
                } else {
                    procesarSeccion(Jsoup.parse(SECCIONES_CABECERA + (index - 1) + sectionsAray[index]), this, false);
                }
            }

            // Descenderemos en las SECTIONCOLAP (sections que tiene un link de
            //  conexion
            if (secciones != null) {
                for (Node item : secciones) {
                    if (item.tipo.equals(TypeNode.SECTIONCOLAP)) {//                   
                        item.descender();
//                    // Podriamos eliminarlo si esta vacio
//                    if (item.isempty()) {
//                        sections.remove(item);
//                    }
                    }

                }
            }
            //*******EL CURSO YA SE ANALIZO Y PROCEDE A DESCARGARSWE
            if (tipo.equals(TypeNode.CURSO)) {
                // Reconstruccion de la cookie utilizable para HttpClients           
                CookieStore cookieAlmacen = new BasicCookieStore();
                BasicClientCookie cookie = new BasicClientCookie(COOKIE_SESION, cookies.get(COOKIE_SESION));
                cookie.setDomain(DOMINIO_MOODLE);
                cookie.setPath("/");
                cookieAlmacen.addCookie(cookie);
                SocketConfig socketConfig = SocketConfig.custom()
                .setSoTimeout(240 * 1000)
                .build();
                try (CloseableHttpClient httpclient = HttpClients.custom()
                        .setDefaultCookieStore(cookieAlmacen)
                        .setDefaultSocketConfig(socketConfig)
                        .build()) {
                    descargarEnLocal(Opciones.getDownloadPath(), httpclient, Opciones.getIU());

                    httpclient.close();
                    logRegistro = new LogRecord(Level.INFO, EXISTO);
                    logRegistro.setSourceClassName(nombre);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        } catch (Exception e) {
            // Exception (MalformedURLException | HttpStatusException | UnsupportedMimeTypeException | SocketTimeoutException | IOException)
            // Consultar Connection Javadoc: https://jsoup.org/apidocs/org/jsoup/Connection.html#get--
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            logRegistro = new LogRecord(Level.WARNING, tipo + ":" + nombre + "\n" + errors.toString());
            logRegistro.setSourceMethodName("descender");
            logRegistro.setSourceClassName(this.getClass().getName());
        } finally {
            if (logRegistro != null) {
                MyLogging.log(logRegistro);
            }
        }
    }

    /**
     * Descenso de un Node TypeNode.FOLDER, se trata de forma diferente que en
     * el metodo descender
     *
     * @see https://jsoup.org/apidocs/org/jsoup/Connection.html#get--
     * @see https://jsoup.org/cookbook/extracting-data/selector-syntax
     */
    private void descenderCarpeta() {
        LogRecord logRegistro = null;
        Elements archivos;
        try {
            Document doc2 = Jsoup.connect(url)
                    .timeout(80 * 1000)
                    .cookies(cookies)
                    .get();
            String auxNombre;
            String auxUrl;
            Node auxHijo;
            archivos = doc2.select(JSOUP_CARPETA_BUSCA_LINKS);

            for (Element resource : archivos) {
                auxNombre = resource.text();
                auxUrl = resource.attr("href");
                auxHijo = new Node(auxUrl, auxNombre, TypeNode.ARCHIVO, cookies);
                archivosAniadir(auxHijo);
            }
        } catch (Exception e) {
            // Se podria mejorar mandandolo hacia arriba para saber el curso en el que esta el folder.
            // Exception (MalformedURLException | HttpStatusException | UnsupportedMimeTypeException | SocketTimeoutException | IOException)
            // Consultar Connection Javadoc: https://jsoup.org/apidocs/org/jsoup/Connection.html#get--
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            logRegistro = new LogRecord(Level.WARNING, tipo + ":" + nombre + "\n" + errors.toString());
            logRegistro.setSourceMethodName("descenderCarpeta");
            logRegistro.setSourceClassName(this.getClass().getName());
        } finally {
            if (logRegistro != null) {
                MyLogging.log(logRegistro);
            }
        }
    }

    /**
     * Identifica la Section: TypeNode.SECTIONEXPAND creara un Node
     * representativo. TypeNode.SECTIONCOLAP procesara el contenido, y en caso
     * de que la section este expandia en el Node General, creara un subNode
     * aniadiendo el contenido a este, a su vez dicho Node al Node General.
     *
     *
     * @param seccion Texto identificativo de la Section
     * @param padre Node raiz de la estructura
     * @param seccionGeneral Indica que la section es la section general.
     * Utilizado para crear un Sub Node representativo de TypeNode.SECTIONEXPAND
     *
     * @throws IOException Posible error con la I/O de datos.
     */
    public void procesarSeccion(Document seccion, Node padre, boolean seccionGeneral) throws IOException {
        Element elementoColapsado = seccion.selectFirst(JSOUP_SECCIONES_COLLAPSADAS);
        Node auxHijo = padre;

        if (elementoColapsado != null && elementoColapsado.hasText()) {
            padre.seccionAniadir(new Node(elementoColapsado.attr("href"),
                    elementoColapsado.text(), TypeNode.SECTIONCOLAP, cookies));
        } else if (seccion.selectFirst("a") != null) {
            // Sections expandidas
            if (padre.tipo.equals(TypeNode.CURSO) && !seccionGeneral) {
                auxHijo = new Node("cccc", seccion.selectFirst(JSOUP_SECCIONES_EXPANDIDAS_NOMBRE).text(), TypeNode.SECTIONEXPAND, cookies);
                padre.seccionAniadir(auxHijo);
            }
            procesarSeccion(seccion.selectFirst(JSOUP_SECCIONES_AREA_DATOS).toString(), auxHijo);
        }
    }

    /**
     * Procesa una section convirtiendola en una estructura arborea de Node's.
     *
     *
     * @param seccion Texto identificativo de la Section
     * @param padre Node raiz de la estructura
     *
     * @throws IOException Posible error con la I/O de datos.
     */
    public void procesarSeccion(String seccion, Node padre) throws IOException {
        String auxCadena;
        Document doc;
        Element auxElement;
        Node auxHijo;
        List<String> listaRecursos = reconstruccionSeccion(seccion);

        if (listaRecursos.size() == 1) {
            // No tiene SUBSection, podemos procesarlo directamente
            encontrarRecursos(Jsoup.parse(listaRecursos.get(0)), padre, 0);
        } else if (listaRecursos.size() > 1) {
            // Tiene al menos 1 lvl de SUBSection
            //********* PROCESADO DE LAS SUBSECTIONS RECONSTRUIDAS
            for (String line : listaRecursos) {
                auxHijo = padre;
                auxCadena = "";
                //********* OBTENCION DEL NOMBRE DE LA SECTION
                doc = Jsoup.parse(line);
                auxElement = doc.selectFirst(JSOUP_SECCIONES_AREA_TITULO);
                if (auxElement != null && !auxElement.hasText()) {
                    // Es posible que el nombre no este en la etiqueta  <p>, sino
                    //  en la siguiente etiqueta ul/br; Cosas de Moodle
                    // Al tratarlo como String no podemos utilizar el indexOf junto porque ...
                    auxCadena = line.substring(line.indexOf(SECCIONES_AREA_TITULO_SENIAL) + SECCIONES_AREA_TITULO_SENIAL.length());
                    auxCadena = auxCadena.substring(auxCadena.indexOf(SECCIONES_AREA_TITULO_SENIAL) + SECCIONES_AREA_TITULO_SENIAL.length());
                    // ...entre ambos hay un numero no determiando de \n\t
                    auxCadena = auxCadena.substring(auxCadena.indexOf(P_ETIQUETA_FIN) + P_ETIQUETA_FIN.length());
                    auxCadena = auxCadena.substring(0, auxCadena.indexOf(ETIQUETA_FIN));
                    auxCadena = Jsoup.parse(auxCadena).text();
                } else if (auxElement != null) {
                    auxCadena = auxElement.text();
                }
                //********* PROCESAMOS LA SECTION
                auxElement = doc.selectFirst("a");
                if (auxElement != null && auxElement.hasText()) {
                    //Solo si tiene algun link la procesamo, sino es una section vacia 
                    // y no hace falta
                    if (!auxCadena.isEmpty()) {
                        //********* ANIADIMOS UN NODO REPRESENTATIVO DE LA SECTION
                        auxHijo = new Node("cccc", auxCadena, TypeNode.SECTIONEXPAND, cookies);
                        padre.seccionAniadir(auxHijo);
                    }
                    procesarSubSeccion(line, 1, auxHijo);
                    if (!padre.equals(auxHijo) && auxHijo.archivosVacios() && auxHijo.seccionesVacios()) {
                        //Se puede haber colado a de elementos reservados
                        padre.secciones.remove(auxHijo);
                    }
                }
            }
        }
    }

    /**
     * Devuelve una lista de cada recurso aniadido a su previa section, siendo
     * un recurso (X,Y). Donde X es que el recurso es Label (Yes/NO). Donde Y es
     * el identado (-/1/2/n) Como ejemplo la sig lista. A(n,
     * 0)//B(n,0)//C(y,0)//D(n,1)//E(n,1)//F(n,0)//G(n,1)//H(n,1)//I(n,0)
     *
     * El resultado sera: A-B-F-G-H-I C-D-E
     *
     * @param seccion Section en formato texto que vamos a reconstuiri
     * @return List<String> donde cada String es un conjunto de Li acoplados
     * segun Label & identado
     *
     */
    private List<String> reconstruccionSeccion(String seccion) {
        String[] lista = null;
        List<String> listaRespuesta = new ArrayList<>();
        Document doc;
        Document auxDoc;
        String lastEdent = "";
        String aux = "";
        String auxSangrado;
        String tipo;
        String auxEtiqueta;
        String miEtiqueta;
        String remplazo;
        int indexA;
        int indexB;
        int index2;

        //Dividimos _section por el campo LI en el cual va un recurso,
        //  y reconstruimos la lista 
        lista = seccion.split(JSOUP_SECCIONES);

        if (lista.length > 1) {
            // 0 es cabecera    
            aux = lista[0].substring(lista[0].indexOf(JSOUP_LI_ETIQUETA_CLASS));
            for (int ind = 1; ind < lista.length - 1; ind++) {
                lista[ind] = aux + lista[ind];
                aux = lista[ind].substring(lista[ind].lastIndexOf(JSOUP_LI_ETIQUETA_CLASS));
                lista[ind] = lista[ind].substring(0, lista[ind].lastIndexOf(JSOUP_LI_ETIQUETA_CLASS));
            }
            lista[lista.length - 1] = aux + lista[lista.length - 1];

            // Creamos una Lista de "nodos" LI, en la cual agrupamos el identado al 
            //  anterior
            //  A   B       C       D       E
            //  F-G-H       I       
            for (int index1 = 1; index1 < lista.length - 1; index1++) {
                doc = Jsoup.parse(lista[index1]);
                tipo = doc.selectFirst(JSOUP_LI_ETIQUETA).attr("class");

                if (tipo.equals(LI_ETIQEUTA)) {
                    //tragar todos los hijos
                    lastEdent = doc.selectFirst(JSOUP_SECCIONES_AREA_TIPO).attr("class");
                    remplazo = lista[index1];
                    for (index2 = index1 + 1; index2 < lista.length; index2++) {
                        auxDoc = Jsoup.parse(lista[index2]);
                        auxSangrado = auxDoc.selectFirst(JSOUP_SECCIONES_AREA_TIPO).attr("class");
                        if (lastEdent.compareTo(auxSangrado) < 0) {
                            remplazo += lista[index2];
                        } else {
                            break;
                        }
                    }
                    index1 = index2 - 1;
                    listaRespuesta.add(remplazo);
                } else {
                    listaRespuesta.add(lista[index1]);
                }
            }
            // El ultimo elemento de lista podria no haber sido aniadido debido 
            //  a la comprobacion de que Label se "coma" todos los hijos
            if (listaRespuesta.size() >= 1 && !listaRespuesta.get(listaRespuesta.size() - 1).contains(lista[lista.length - 1])) {
                listaRespuesta.add(lista[lista.length - 1]);
            } else if (listaRespuesta.size() == 0) {
                listaRespuesta.add(lista[lista.length - 1]);
            }

            // Agrupamos los "nodos" de forma que las Label esten con una label "padre"
            //  Y los no label se agrupen en el "nodo" con un identado inferior
            // A-B-F-G-H-I      C-D-E
            indexA = listaRespuesta.size() - 1;
            while (indexA > 0) {
                indexB = indexA - 1;
                miEtiqueta = listaRespuesta.get(indexA).substring(SECCIONES_AREA_ETIQUETA.length(), listaRespuesta.get(indexA).indexOf(SECCIONES_AREA_ETIQUETA_FIN));

                if (miEtiqueta.equals(LI_ETIQEUTA)) {
                    auxSangrado = listaRespuesta.get(indexA).substring(listaRespuesta.get(indexA).indexOf(SECCIONES_AREA_SENIAL) + SECCIONES_AREA_SENIAL.length(), listaRespuesta.get(indexA).indexOf(SECCIONES_AREA_SENIAL_FIN));
                    // add al primer padre label
                    while (indexB >= 0) {
                        auxEtiqueta = listaRespuesta.get(indexB).substring(SECCIONES_AREA_ETIQUETA.length(), listaRespuesta.get(indexB).indexOf(SECCIONES_AREA_ETIQUETA_FIN));
                        lastEdent = listaRespuesta.get(indexB).substring(listaRespuesta.get(indexB).indexOf(SECCIONES_AREA_SENIAL) + SECCIONES_AREA_SENIAL.length(), listaRespuesta.get(indexB).indexOf(SECCIONES_AREA_SENIAL_FIN));

                        if (auxEtiqueta.equals(LI_ETIQEUTA) && auxSangrado.compareTo(lastEdent) > 0) {
                            listaRespuesta.set(indexB, listaRespuesta.get(indexB) + listaRespuesta.get(indexA));
                            listaRespuesta.remove(indexA);
                            break;
                        }
                        indexB--;
                    }
                } else {
                    // add al primero no label
                    while (indexB >= 0) {
                        auxEtiqueta = listaRespuesta.get(indexB).substring(SECCIONES_AREA_ETIQUETA.length(), listaRespuesta.get(indexB).indexOf(SECCIONES_AREA_ETIQUETA_FIN));
                        if (!auxEtiqueta.equals(LI_ETIQEUTA)) {
                            listaRespuesta.set(indexB, listaRespuesta.get(indexB) + listaRespuesta.get(indexA));
                            listaRespuesta.remove(indexA);
                            break;
                        }
                        indexB--;
                    }
                }
                indexA--;
            }
        }

        return listaRespuesta;
    }

    /**
     * Procesa una SUBsection de forma recursiva tratando los niveles (_lvl) de
     * identado convirtiendo la SUBsection en una estructura arborea de Node's
     *
     * @param _seccion Texto identificativo de la SUBSection
     * @param nivel Nivel de la SUBSection que se quiere procesar
     * @param padre Node raiz de la estructura
     *
     * @throws IOException Posible error con la I/O de datos.
     */
    public void procesarSubSeccion(String _seccion, int nivel, Node padre) throws IOException {
        String auxSemilla = String.format(SEMILLA, nivel);
        String[] subSections = _seccion.split(auxSemilla);
        Document doc;
        String auxCadena;
        Element auxElement;
        Node nodeSon;
        int indexURLNoName = 0;

        // Dividimos en SUBSection del nivel esperado _lvl
        for (int index = 1; index < subSections.length; index++) {
            auxElement = null;
            nodeSon = padre;
            if (subSections[index].contains(String.format(SEMILLA, nivel + 1))) {
                // Si la division realizada contiene una subsection del nivel 
                //  inferior _lvl + 1; recurrimos
                procesarSubSeccion(subSections[index], nivel + 1, nodeSon);
            } else {
                // En caso de que la SUBSection no contenga una subsection 
                //  inferior,la procesamos.

                //********* OBTENCION DEL NOMBRE DE LA SUBSECTION
                doc = Jsoup.parse(subSections[index]);
                auxElement = doc.selectFirst(JSOUP_SECCIONES_AREA_TITULO);
                if (auxElement == null && subSections[index].contains(SECCIONES_AREA_TITULO_ZONA)) {
                    auxCadena = subSections[index].substring(subSections[index].indexOf(SECCIONES_AREA_TITULO_ZONA));
                    auxCadena = auxCadena.substring(auxCadena.indexOf(P_ETIQUETA_FIN) + P_ETIQUETA_FIN.length());
                    auxCadena = auxCadena.substring(0, auxCadena.indexOf(ETIQUETA_FIN));
                    auxCadena = Jsoup.parse(auxCadena).text();

                    //********* ANIADIMOS UN NODO REPRESENTATIVO DE LA SECTION
                    if (auxCadena.length() > 1) {
                        // Para eviar SubSections vacias, utilizadas para aniadir
                        //  un hueco visualmente.
                        nodeSon = new Node("cccc", auxCadena, TypeNode.SECTIONEXPAND, null);
                        padre.seccionAniadir(nodeSon);
                    }
                } else if (auxElement != null) {
                    auxCadena = auxElement.text();
                    //********* ANIADIMOS UN NODO REPRESENTATIVO DE LA SECTION
                    if (auxCadena.length() > 1) {
                        // Para eviar SubSections vacias, utilizadas para aniadir
                        //  un hueco visualmente. AQUI NO DEBERIA HACER FALTA.
                        nodeSon = new Node("cccc", auxCadena, TypeNode.SECTIONEXPAND, null);
                        padre.seccionAniadir(nodeSon);
                    }
                }
                //********* PROCESAMOS LA SUBSECTION
                indexURLNoName = encontrarRecursos(Jsoup.parse(auxSemilla + subSections[index]), nodeSon, indexURLNoName);
            }
        }

        //********* PROCESAMIENTO DE LO PREVIO A LA PRIMERA SUBSECTION
        encontrarRecursos(Jsoup.parse(auxSemilla + subSections[0]), padre, indexURLNoName);
    }

    /**
     * Encuentra todos los enlaces utiles y los aniade al Node "padre" al que
     * pertenencen.
     *
     * @param seccion section sobre la que se desea encontrar todos los enlaces
     * @param padre Node "padre" al cual aniadiremos los enlaces encontrados
     * @param indiceUrlSinNombre indexado numeracion para enlaces encontrados
     * que no tengan nombre propio.
     *
     * @throws IOException Posible error con la I/O de datos.
     * @return Devuelve el indexado numerico
     */
    public int encontrarRecursos(Document seccion, Node padre, int indiceUrlSinNombre) throws IOException {
        String[] aux = null;
        String name = "";
        String typeAux = "";
        String url = "";
        TypeNode type = null;
        Elements links = seccion.select("a");
        Element auxElement;
        Node auxSon;
        // Para cuando ponen un link como Texto, en vez de aniadirlo como una 
        //  URL; darle una numeracion y nombre pordefecto.
        int indexURLNoName = indiceUrlSinNombre;

        for (int i = 0; i < links.size(); i++) {
            aux = null;
            //********* CONSTRUCION DE UN NODE REPRESENTATIVO
            auxElement = links.get(i);
            url = auxElement.select(JSOUP_A_ETIQUETA).attr("href");
            //********* OBTENCION DE NAME & TYPE ASOCIADOS
            aux = auxElement.select(JSOUP_SPAN_ETIQUETA).toString().split(SECCIONES_SPAN_DIVISION);
            if (aux.length > 1) {
                name = aux[1].replaceAll(LIMPIEZA_NOMBRE, "");

                // El profesor se ha podido olvidar de ponerle tipo
                typeAux = "Other";
                if (aux.length == 4) {
                    typeAux = aux[3].replaceAll(LIMPIEZA_TIPO, "");
                    typeAux = typeAux.replaceAll(LIMPIEZA_TIPO_FIN, "");
                }
                type = TypeNode.getEnum(typeAux);
            } else {
                // Se trata de una URL puesta como texto plano
                type = TypeNode.URL;
                name = ENLACE_NOMBRE_DEFECTO + indexURLNoName;
                indexURLNoName++;
            }

            if (type != TypeNode.FORO && !url.contains(A_RESERVADOR_01)) {
                auxSon = new Node(url, Jsoup.parse(name).text(), type, cookies);
                padre.archivosAniadir(auxSon);
                if (type.equals(TypeNode.FOLDER)) {
                    auxSon.descenderCarpeta();
                }
            }
        }
        return indexURLNoName;
    }

    //******************* UTILES *********************************
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + Objects.hashCode(this.nombre) + Objects.hashCode(this.tipo);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Node other = (Node) obj;
        if (!Objects.equals(this.nombre, other.nombre) && !this.tipo.equals(other.tipo)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return tipo.toString() + "{" + "url=" + url + ", Name=" + nombre + "}";// + sections.size();
    }

    /**
     * Imprime por la Salida estandar (out), una representacion del Node actual
     * imprimiendo las sections & files correspondientes y atendiendo su
     * indetizacion respecto a su Node "padre"
     *
     * @param _lvl identizacion respecto al Node "padre"
     */
    public void listar(int _lvl) {
//        try {
        String lvlAux = new String(new char[_lvl]).replace("\0", "\t");
        String size = "";

        if (tipo == TypeNode.SECTIONCOLAP && archivos != null) {
            size = "Size: " + archivos.size() + " -- ";
        } else {
            size = "Size: 0 -- ";
        }
        System.out.println(lvlAux + size + this.toString());

        if (archivos != null) {
            for (Node fil : archivos) {
                fil.listar(_lvl + 1);
            }
        }

        if (secciones != null) {
            for (Node sect : secciones) {
                sect.listar(_lvl + 1);
            }
        }
//        } catch (Exception e) {
//            System.err.println("Error on " + NAME + "--" + URL);
//            e.printStackTrace();
//
//        }
    }

    /**
     * Aniade un Node(hijo) representativo de una seccion al actual (padre)
     *
     * @param _node
     */
    public void seccionAniadir(Node _node) {
        if (secciones == null) {
            secciones = new HashSet<Node>();
        }
        secciones.add(_node);
    }

    /**
     * Aniade un Node(hijo) representativo de un archivo al actual (padre)
     *
     * @param _node
     */
    public void archivosAniadir(Node _node) {
        if (archivos == null) {
            archivos = new HashSet<Node>();
        }
        archivos.add(_node);
    }

    /**
     * @return True set<> archivos == null | archivos.size() == 0. False en caso
     * contrario
     */
    private boolean archivosVacios() {
        if (archivos != null) {
            return archivos.isEmpty();
        } else {
            return true;
        }
    }

    /**
     * @return True set<> secciones == null | secciones.size() == 0. False en
     * caso contrario
     */
    private boolean seccionesVacios() {
        if (secciones != null) {
            return secciones.isEmpty();
        } else {
            return true;
        }
    }

    /**
     * Creacion del contenido representativo del Node en el Sistema de Ficheros
     * (Local)
     *
     * @param pathDescarga path del SF donde descargar
     * @param httpclient cliente Http utilizado para descargar los archivos
     */
    private void descargarEnLocal(String pathDescarga, CloseableHttpClient httpclient,InterfaceController iu) { //, UnsupportedOperationException {
        LogRecord logRegistro = null;
        try {
            //**************SI PUEDE SER UN DIRECTORIO Y NO EXISTE LO CREAS**********
            if (tipo == TypeNode.CURSO || tipo == TypeNode.FOLDER || tipo == TypeNode.SECTIONCOLAP || tipo == TypeNode.SECTIONEXPAND) {
                File carpeta = new File(pathDescarga + File.separator + nombre);
                if (!carpeta.exists()) {
                    carpeta.mkdir();
                }
            }
            //**************************DESCARGAR LOS ARCHIVOS**********************
            if (archivos != null) {
                for (Node item : archivos) {
                    item.descargarEnLocal(pathDescarga + File.separator + nombre, httpclient, iu);
                }
            }
            //**************************DESCARGAR LAS SECTIONS**********************
            if (secciones != null) {
                for (Node item : secciones) {
                    item.descargarEnLocal(pathDescarga + File.separator + nombre, httpclient, iu);
                }
            }
            //********COMPROVAMOS QUE TIPO ES Y QUE NO ESTA DESCARGADO**********
            if (tipo.equals(TypeNode.ARCHIVO) && !archivoExiste(pathDescarga, nombre)) {
                descargarArchivo(pathDescarga, httpclient, iu);
//                iu.addTreeItem(pathDescarga, this.nombre, this.tipo);
            } else if (tipo.equals(TypeNode.URL) && !archivoExiste(pathDescarga, nombre+".htm")) {
                descargarEnlaceWeb(pathDescarga, httpclient, iu);
//                iu.addTreeItem(pathDescarga, this.nombre, this.tipo);
            } else if (tipo.equals(TypeNode.OTHER) && !archivoExiste(pathDescarga, nombre)) {
                //Se puede dar el caso de que no reconocio el tipo o el profesor se 
                //  olvido, lo lanzamos como file y en caso de no ser file se tratara 
                //  en el propio metodo
                descargarArchivo(pathDescarga, httpclient, iu);
//                iu.addTreeItem(pathDescarga, this.nombre, this.tipo);
            }
//        } catch (HttpHostConnectException e0) {
////            StringWriter errors = new StringWriter();
////            e.printStackTrace(new PrintWriter(errors));
//            logRegistro = new LogRecord(Level.WARNING, "Recurso caido: " + nombre + " on " + pathDescarga + "\n");
//            logRegistro.setSourceMethodName("descargarEnLocal");
//            logRegistro.setSourceClassName(this.getClass().getName());
        } catch (Exception e) {
            // Exception (MalformedURLException | HttpStatusException | UnsupportedMimeTypeException | SocketTimeoutException | IOException)
            // Consultar Connection Javadoc: https://jsoup.org/apidocs/org/jsoup/Connection.html#get--
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            logRegistro = new LogRecord(Level.WARNING, "Descargando: " + nombre + " on " + pathDescarga + "\n" + errors.toString());
            logRegistro.setSourceMethodName("descargarEnLocal");
            logRegistro.setSourceClassName(this.getClass().getName());
        } finally {
            if (logRegistro != null) {
                MyLogging.log(logRegistro);
            }
        }
    }

    /**
     * Conecta con la URL de un Node archivo, obtencion de la extension esperada
     * y creacion en el SF de un archivo con dicha extension. NOTA: los archivos
     * binarios no tiene extension y se descargaran igual.
     *
     *
     * @param pathDescarga path del local donde descargar
     * @param httpclient cliente Http utilizado para descargar los archivos
     *
     * @see
     * https://codereview.stackexchange.com/questions/116596/downloading-big-pdf-files
     * @see
     * http://hc.apache.org/httpcomponents-core-ga/httpcore/apidocs/org/apache/http/HttpEntity.html#isChunked()
     *
     */
    private void descargarArchivo(String pathDescarga, CloseableHttpClient httpclient, InterfaceController iu) {
        HttpClientContext context = HttpClientContext.create();
        CloseableHttpResponse response = null;
        HttpEntity entity;
        InputStream is = null;
        FileOutputStream fos = null;
        String nombreConFormato = "";
        String formato = "";
        LogRecord logRegistro = null;
        int bytesBuffered = 0;
        int len = 0;
        byte[] buffer = new byte[10240];
        HttpRequestBase request = new HttpPost(url); 
//        RequestConfig.Builder requestConfig = RequestConfig.custom();
//        requestConfig.setConnectTimeout(500 * 1000);
//        requestConfig.setConnectionRequestTimeout(500 * 1000);
//        requestConfig.setSocketTimeout(500 * 1000);
//        request.setConfig(requestConfig.build());
        long time_start, time_end;
        time_start = System.currentTimeMillis();
        try {
            //***************CONECTAMOS A LA URL************************************
            // Create a custom response handler
            response = httpclient.execute(request, context);
            entity = response.getEntity();
            List<URI> redirectURIs = context.getRedirectLocations();

            if (url.contains("forcedownload")) {
                //La propia URL nos abre un canal directo al archivo
                formato = url;
                formato = formato.substring(0, formato.indexOf("forcedownload"));
                formato = formato.substring(formato.lastIndexOf("."), formato.indexOf("?"));
            } else if (redirectURIs != null && !redirectURIs.isEmpty()) {
                //Hay Redireccion
                formato = redirectURIs.get(0).toString();
                if (formato.contains("forcedownload")) {
                    formato = formato.substring(0, formato.indexOf("forcedownload"));
                    formato = formato.substring(formato.lastIndexOf("."), formato.indexOf("?"));
                } else {
                    formato = formato.substring(formato.lastIndexOf("."));
                    if (formato.contains("mod_resource")) {
                        //Es aglo similar a el forcedownload, pero redireciona como a 
                        // un repositorio el cual te autodescarga el archivo
                        // Utilizado para archivos Binarios
                        formato = "";
                    }
                }
            } else {
                // No hay redireccion (el archivo hay que encontrarlo en la URL actual)
                //Leer el contenido/ encontar url/ cerar response, conectar response a la URL
                formato = encontrarUrlTrasNoRedirigir(entity);
                if (formato != null && url.compareTo(formato) != 0) {
                    this.url = formato;
                    response.close();
                    request = new HttpPost(url); 
//                    request.setConfig(requestConfig.build());
                    response = httpclient.execute(request);
//                    response = httpclient.execute(new HttpPost(this.url));
                    entity = response.getEntity();
                    formato = formato.substring(formato.lastIndexOf("."));
                }

            }
            if (entity != null) {
                if (!entity.isChunked()) {
                    //Lo que hay en la URL en la que nos encontramos es archivo
                    // con codificacion fragmentada, lo que quiere decir es que
                    // nos lo podemos descargar
                    is = entity.getContent();
//                    File downloadedFile = File.createTempFile(_pathdownload + File.separator + NAME.replace("/", "-"), formato);
                    if (formato == "" || nombre.contains(formato)) {
                        nombreConFormato = nombre;
                    } else {
                        nombreConFormato = nombre + formato;
                    }
                    //Comprobacion de existencia
//                    if(!archivoExiste(pathDescarga, nombreConFormato)){
                        fos = new FileOutputStream(new File(pathDescarga + File.separator + nombreConFormato));
//                      System.out.println("Downloadinwg " + _pathdownload + File.separator + nameFormat);

                        while ((len = is.read(buffer)) != -1) {
                            fos.write(buffer, 0, len);
                            bytesBuffered += len;
                            if (bytesBuffered > 1024 * 1024) {
                                fos.flush();
                            }
                        }
                        iu.addTreeItem(pathDescarga + File.separator + nombreConFormato, this.nombre, this.tipo);
//                    }
                }
            } else {
                // Se trata de un Node TypeNode.OTHER, el cual no refleja una URL
                // que podamos tratar de descargar, entonces la tratara como un 
                // Link URL
                response.close();
                response = null;
                //Comprobacion de existencia
//                if(!archivoExiste(pathDescarga, nombre+".htm")){
                    descargarEnlaceWeb(pathDescarga, httpclient, iu);
//                }
            }
        } catch (Exception e) {
            time_end = System.currentTimeMillis();
            System.err.println("\n\n--the request has taken " + (time_end - time_start) + " milliseconds");
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            logRegistro = new LogRecord(Level.WARNING, nombre + "(" + url + ")\n" + errors.toString());
            logRegistro.setSourceMethodName("descargarArchivo");
            logRegistro.setSourceClassName(this.getClass().getName());
        } finally {
            if (is != null) {
                try {
                    is.close();

                } catch (IOException ex) {
                    Logger.getLogger(Node.class
                            .getName()).log(Level.SEVERE, null, ex);
                }
            }
            if (fos != null) {
                try {
                    fos.close();

                } catch (IOException ex) {
                    Logger.getLogger(Node.class
                            .getName()).log(Level.SEVERE, null, ex);
                }
            }
            if (response != null) {
                try {
                    response.close();

                } catch (IOException ex) {
                    Logger.getLogger(Node.class
                            .getName()).log(Level.SEVERE, null, ex);
                }
            }
            if (logRegistro != null) {
                MyLogging.log(logRegistro);
            }
        }
    }

    /**
     * Busca la URL de un posible recurso, hubicado en una direccion moodle; al
     * cual se esperaba una redireccion fuera de Moodle.
     *
     *
     * @param entity respuesta tras conectar a una Url
     * @return String de la URL relacionada con recurso. Null en caso de que no
     * haya ningun recurso asignado
     */
    private String encontrarUrlTrasNoRedirigir(HttpEntity entity) throws IOException {
        String respuesta = null;
        String linea = null;
        InputStream is = null;
        BufferedReader bufferedReader = null;
        try {
            is = entity.getContent();
            bufferedReader = new BufferedReader(new InputStreamReader(is));

            while ((linea = bufferedReader.readLine()) != null) {
                if (linea.contains(NO_REDIRECCION_AREA)) {
                    respuesta = linea.substring(0, linea.indexOf(SALTAR_NAVEGACION));
                    respuesta = respuesta.substring(respuesta.indexOf(NO_RIDIRECCION_INDICE));
                    respuesta = respuesta.substring(0, respuesta.indexOf(NO_RIDIRECCION_INDICE_FIN));
                    break;
                }
            }
        } finally {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (is != null) {
                is.close();
            }
            return respuesta;
        }
    }

    /**
     * Dada un Node TypeNode.URL obtendra el link final al que direcciona, y
     * creara un archivo URL en el Sistema de Ficheros que enlazara con dicho
     * Link.
     *
     * @param pathDescarga path del local donde descargar
     * @param httpclient cliente Http utilizado para conectar a la Url
     * @throws Exception posibles excepciones debido a Jsoup
     * @see https://jsoup.org/apidocs/org/jsoup/Connection.html#get--
     */
    private void descargarEnlaceWeb(String pathDescarga, CloseableHttpClient httpclient, InterfaceController iu) throws Exception {
        //Comprobar si la URL esta escondida en una section de moodle
        HttpClientContext context = HttpClientContext.create();
        CloseableHttpResponse response = null;
        HttpEntity entity;
        InputStream is = null;
        BufferedReader bufferedReader = null;
        FileWriter fw = null;
        String urlAux = this.url;
        String line = null;
        LogRecord logRegistro = null;
        HttpRequestBase request = new HttpPost(url); 
//        RequestConfig.Builder requestConfig = RequestConfig.custom();
//        requestConfig.setConnectTimeout(180 * 1000);
//        requestConfig.setConnectionRequestTimeout(180 * 1000);
//        requestConfig.setSocketTimeout(180 * 1000);
//        request.setConfig(requestConfig.build());
        try {
            //Miramos si la url redirige a moodle, porque la verdadera url estara
            //  en span9, podriamos parsear con Jsoup pero la linea que buscamos
            //  esta hacia la mitad y quiza cueste menos que parsear
            if (url.contains("moodle2.unizar.es/add/mod/url")) {
                response = httpclient.execute(request, context);
                entity = response.getEntity();
                List<URI> redirectURIs = context.getRedirectLocations();

                if (redirectURIs == null) {
                    is = entity.getContent();
                    bufferedReader = new BufferedReader(new InputStreamReader(is));

                    while ((line = bufferedReader.readLine()) != null) {
                        if (line.contains("<div id=\"content\" class=\"span9")) {
                            urlAux = line.substring(0, line.indexOf(SALTAR_NAVEGACION));
                            urlAux = urlAux.substring(urlAux.indexOf("<a href=\"") + "<a href=\"".length());
                            urlAux = urlAux.substring(0, urlAux.indexOf("\" >"));
                            break;
                        }
                    }
                }else{
                    urlAux = redirectURIs.get(0).toString();
                }
                    
            }
            fw = new FileWriter(pathDescarga + File.separator + nombre + ".htm");
            String seed = "<script type=\"text/javascript\" src=\"/712BCC42-17A0-D44D-A1E8-AA762A409D44/main.js\" charset=\"UTF-8\"></script><script>window.googleJavaScriptRedirect=1</script><META name=\"referrer\" content=\"origin\"><script>var n={navigateTo:function(b,a,d){if(b!=a&&b.google){if(b.google.r){b.google.r=0;b.location.href=d;a.location.replace(\"about:blank\");}}else{a.location.replace(d);}}};n.navigateTo(window.parent,window,\"%s\");\n"
                    + "</script><noscript><META http-equiv=\"refresh\" content=\"0;URL='%s'\"></noscript>";
            seed = String.format(seed, urlAux, urlAux);
            fw.write(seed);
            iu.addTreeItem(pathDescarga + File.separator + nombre + ".htm", this.nombre, this.tipo);
        } catch (Exception e) {
            //El enlace esta caido, podemos capturar al error o crear en enlace
            // con la Url caid y que los alumnos al ver en enlace que no va 
            // comuniquen al profesor.
            e.printStackTrace();
//            urlAux = this.url;
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            logRegistro = new LogRecord(Level.WARNING, nombre + "(" + url + ")\n" + errors.toString());
            logRegistro.setSourceMethodName("descargarArchivo");
            logRegistro.setSourceClassName(this.getClass().getName());
        } finally {
            if (fw != null) {
                fw.flush();
                fw.close();
            }
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (is != null) {
                is.close();
            }
            if (response != null) {
                response.close();
            }
        }
    }

    /**
     * Metodo para comprobar si existe un archivo (file) en un directorio,
     * independiente de su extension.
     *
     * @param path del directorio sobre el que se hace la comprobacion
     * @param archivo nombre del archivo que se quiere comprobar
     * @return True, si el archivo existe. False en caso contrario. //@throws
     * Exception posible error debido a caracteres reservados en SF
     *
     * @see
     * https://stackoverflow.com/questions/17697646/how-to-detect-if-a-filewith-any-extension-exist-in-java
     */
    public boolean archivoExiste(String path, String archivo) {// throws Exception {
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();

        if (listOfFiles != null) {
            for (File file : listOfFiles) {
                if (file.isFile()) {
//                    String[] filename = file.getName().split(EXTENSION_ARCHIVO); //split filename from it's extension
//                    if (filename[0].equalsIgnoreCase(archivo)) { //matching defined filename         
//                        return true;
//                    }
                    if (file.getName().contains(archivo)) {
//                    if (file.getName().equals(archivo)) { //matching defined filename         
                        return true;
                    }
                }
            }
        }
        return false;// respuesta;
    }

}