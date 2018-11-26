package sincronizacion.moodle.estructura;

//#1 Static import
import aplicacion.controlador.MainController;
import sincronizacion.moodle.inicio.OpcionesSyncMoodle;
import tools.logger.LogSincronizacion;
//#3 Third party
import org.apache.http.HttpEntity;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
//#4 Java
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


/**
 * @author Diego Alvarez
 * @version 1.3
 *
 * Clase que forma una estructura arborea representativa del contenido Moodle y
 * descarga o crea el contenido en replica en el Sistema Local
 */
public class Nodo extends MarcasScrapping {
    private String url;
    private String nombre;
    private TipoNodo tipo;
    private Map<String, String> cookies;
    
    //*******SET PARA EVITAR DUPLICADOS
    private Set<Nodo> secciones;
    private Set<Nodo> archivos;

    
    /**
     * Constructor
     *
     * @param url URL la cual el Node representa
     * @param nombre nombre del Node
     * @param tipo tipo de Node
     * @param cookies Map de cookies que el Node utilizara para conectar a _url
     */
    public Nodo(String url, String nombre, TipoNodo tipo, Map<String, String> cookies) {
        this.url = url;

        String auxCadena = Normalizador.normalizarNombre(nombre);
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
        return this.url;
    }

    /**
     *
     * @return
     */
    public String getNombre() {
        return this.nombre;
    }

    /**
     *
     * @return
     */
    public TipoNodo getTipo() {
        return this.tipo;
    }

    /**
     * Descenso del Nodo, el Nodo conectara a su URL asociada y procesara las
     *  sections 'li' conforme a su TYPE
     * 
     * @see <a href="https://jsoup.org/apidocs/org/jsoup/Connection.html#get--">Link 01</a>
     * @see <a href="https://jsoup.org/cookbook/extracting-data/selector-syntax">Link 02</a>
     */
    public void descender() {
        LogRecord logRegistro = null;
        String[] sectionsAray;
        
        try {
            // Doblamos el timeout porque parece tenet problemas de tiempo de conexion 
            //  al haber variso Nodes usando la Connect
            Document doc = Jsoup.connect(this.url).timeout(180 * 1000)
                    .cookies(this.cookies).get();

            sectionsAray = doc.toString().split(SECCIONES_DIVISION);
            
            // 0 - Cabeceras antes de la primera section
            // 1 - Section general
            // N - Section N
            if (this.tipo == TipoNodo.CURSO) {
                procesarSeccion(Jsoup.parse(SECCIONES_CABECERA + "1" +
                        sectionsAray[1]), this, true);
            }
            
            // Resto de sections
            for (int index = 2; index < sectionsAray.length; index++) {
                // Ultimo elemento, para no procesar resto de html que no interesa.
                if (index == sectionsAray.length - 1) {
                    Document auxDoc = Jsoup.parse(SECCIONES_CABECERA + (index - 1) + sectionsAray[index]);
                    procesarSeccion(Jsoup.parse(auxDoc.selectFirst(JSOUP_LI_ETIQUETA).toString()), this, false);
                } else {
                    procesarSeccion(Jsoup.parse(SECCIONES_CABECERA + (index - 1) + sectionsAray[index]), this, false);
                }
            }
            
            // Descenderemos en las SECTIONCOLAP (sections que tiene un link de
            //  conexion
            if (this.secciones != null) {
                for (Nodo item : this.secciones) {
                    if (item.tipo.equals(TipoNodo.SECTIONCOLAP)) {
                        item.descender();
                    }
                }
            }
            
            //*******EL CURSO YA SE ANALIZO Y PROCEDE A DESCARGARSWE
            if (this.tipo.equals(TipoNodo.CURSO)) {
                // Reconstruccion de la cookie utilizable para HttpClients           
                CookieStore cookieAlmacen = new BasicCookieStore();
                BasicClientCookie cookie = new BasicClientCookie(COOKIE_SESION, (String) this.cookies.get(COOKIE_SESION));
                cookie.setDomain(DOMINIO_MOODLE);
                cookie.setPath("/");
                cookieAlmacen.addCookie(cookie);

                SocketConfig socketConfig = SocketConfig.custom().setSoTimeout(240 * 1000).build();
                try (CloseableHttpClient httpclient = HttpClients.custom()
                        .setDefaultCookieStore(cookieAlmacen)
                        .setDefaultSocketConfig(socketConfig)
                        .build();){
                    descargarEnLocal(OpcionesSyncMoodle.getPathDescarga(), httpclient, OpcionesSyncMoodle.getIU());
                    
//                    httpclient.close();
                    logRegistro = new LogRecord(Level.INFO, EXISTO);
                    logRegistro.setSourceClassName(this.nombre);  
                } catch (Exception e) {
                    //Catch para el try, no me interesa saber porque no se abrio 
                    // el canal
//                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            logRegistro = new LogRecord(Level.WARNING, this.tipo + ":" + this.nombre + "\n" + errors.toString());
            logRegistro.setSourceMethodName("descender");
            logRegistro.setSourceClassName(getClass().getName());
        } finally {
            if (logRegistro != null) {
                LogSincronizacion.log(logRegistro);
            }
        }
    }

    /**
     * Descenso de un Nodo TipoNodo.FOLDER, se trata de forma diferente que en
     *  el metodo descender
     *
     * @see https://jsoup.org/apidocs/org/jsoup/Connection.html#get--
     * @see https://jsoup.org/cookbook/extracting-data/selector-syntax
     */
    private void descenderCarpeta() {
        LogRecord logRegistro = null;
        try {
            Document doc2 = Jsoup.connect(this.url)
                    .timeout(80000)
                    .cookies(this.cookies)
                    .get();
            String auxNombre;
            String auxUrl;
            TipoNodo auxTipo;
            int index = 0;
            Nodo auxHijo;
            Element seccion = doc2.selectFirst("div[role=main]");
            Elements enlaces = Jsoup.parse(seccion.toString()).select("a:eq(0)");
            
            
            for (Element resource : enlaces) {
                if (resource.toString().contains(".pdf")) {
                    auxTipo = TipoNodo.ARCHIVO;
                } else {
                    auxTipo = TipoNodo.OTHER;
                }
                auxUrl = resource.attr("href");
                auxNombre = resource.text();
                if ((auxNombre.isEmpty()) || (auxUrl.equals(auxNombre))) {
                    auxNombre = "Enlace_" + index;
                    index++;
                }
                auxHijo = new Nodo(auxUrl, auxNombre, auxTipo, this.cookies);
                archivosAniadir(auxHijo);
            }
        } catch (Exception e) {
            // Se podria mejorar mandandolo hacia arriba para saber el curso en el que esta el folder.
            // Exception (MalformedURLException | HttpStatusException | UnsupportedMimeTypeException | SocketTimeoutException | IOException)
            // Consultar Connection Javadoc: https://jsoup.org/apidocs/org/jsoup/Connection.html#get--
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            logRegistro = new LogRecord(Level.WARNING, this.tipo + ":" + this.nombre + "\n" + errors.toString());
            logRegistro.setSourceMethodName("descenderCarpeta");
            logRegistro.setSourceClassName(getClass().getName());
        } finally {
            if (logRegistro != null) {
                LogSincronizacion.log(logRegistro);
            }
        }
    }

    /**
     * Identifica la Section: TipoNodo.SECTIONEXPAND creara un Nodo 
     *  representativo. TipoNodo.SECTIONCOLAP procesara el contenido, y en caso
     *  de que la section este expandia en el Nodo General, creara un subNode
     *  aniadiendo el contenido a este, a su vez dicho Nodo al Nodo General.
     *
     *
     * @param seccion Texto identificativo de la Section
     * @param padre Nodo raiz de la estructura
     * @param seccionGeneral Indica que la section es la section general.
     *  Utilizado para crear un Sub Nodo representativo de TipoNodo.SECTIONEXPAND
     *
     * @throws IOException Posible error con la I/O de datos.
     */
    public void procesarSeccion(Document seccion, Nodo padre, boolean seccionGeneral) throws IOException {
        Element elementoColapsado = seccion.selectFirst(JSOUP_SECCIONES_COLLAPSADAS);
        Nodo auxHijo = padre;
        
        if ((elementoColapsado != null) && (elementoColapsado.hasText())) {
            padre.seccionAniadir(new Nodo(elementoColapsado.attr("href"), elementoColapsado
                    .text(), TipoNodo.SECTIONCOLAP, this.cookies));
        } else if (seccion.selectFirst("a") != null) {
            // Sections expandidas
            if ((padre.tipo.equals(TipoNodo.CURSO)) && (!seccionGeneral)) {
                auxHijo = new Nodo("cccc", seccion.selectFirst(JSOUP_SECCIONES_EXPANDIDAS_NOMBRE).text(), TipoNodo.SECTIONEXPAND, this.cookies);
                padre.seccionAniadir(auxHijo);
            }
            procesarSeccion(seccion.selectFirst(JSOUP_SECCIONES_AREA_DATOS).toString(), auxHijo);
        }
    }
    /**
     * Procesa una section convirtiendola en una estructura arborea de Nodo's.
     *
     *
     * @param seccion Texto identificativo de la Section
     * @param padre Nodo raiz de la estructura
     *
     * @throws IOException Posible error con la I/O de datos.
     */
    public void procesarSeccion(String seccion, Nodo padre) throws IOException {
        String auxCadena;
        Document doc;
        Element auxElement;
        Nodo auxHijo;
        List<String> listaRecursos = reconstruirSeccion(seccion);
        
        if (listaRecursos.size() == 1) {
            // No tiene SUBSection, podemos procesarlo directamente
            encontrarRecursos(Jsoup.parse((String) listaRecursos.get(0)), padre, 0);
        } else if (listaRecursos.size() > 1) {
            // Tiene al menos 1 lvl de SUBSection
            //********* PROCESADO DE LAS SUBSECTIONS RECONSTRUIDAS
            for (String line : listaRecursos) {
                auxHijo = padre;
                auxCadena = "";
                //********* OBTENCION DEL NOMBRE DE LA SECTION
                doc = Jsoup.parse(line);
                auxElement = doc.selectFirst(JSOUP_SECCIONES_AREA_TITULO);
                if ((auxElement != null) && (!auxElement.hasText())) {
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
                if ((auxElement != null) && (auxElement.hasText())) {
                    //Solo si tiene algun link la procesamo, sino es una section vacia 
                    // y no hace falta
                    if (!auxCadena.isEmpty()) {
                        //********* ANIADIMOS UN NODO REPRESENTATIVO DE LA SECTION
                        auxHijo = new Nodo("cccc", auxCadena, TipoNodo.SECTIONEXPAND, this.cookies);
                        padre.seccionAniadir(auxHijo);
                    }
                    procesarSubSeccion(line, 1, auxHijo);
                    if ((!padre.equals(auxHijo)) && (auxHijo.archivosVacios()) && (auxHijo.seccionesVacias())) {
                        //Se puede haber colado a de elementos reservados
                        padre.secciones.remove(auxHijo);
                    }
                }
            }
        }
    }
    /**
     * Procesa una SUBsection de forma recursiva tratando los niveles (_lvl) de
     * identado convirtiendo la SUBsection en una estructura arborea de Nodo's
     *
     * @param seccion Texto identificativo de la SUBSection
     * @param nivel Nivel de la SUBSection que se quiere procesar
     * @param padre Nodo raiz de la estructura
     *
     * @throws IOException Posible error con la I/O de datos.
     */
    public void procesarSubSeccion(String seccion, int nivel, Nodo padre) throws IOException {
        String auxSemilla = String.format(SEMILLA, nivel);
        String[] subSections = seccion.split(auxSemilla);
        Document doc;
        String auxCadena;
        Element auxElement ;
        Nodo nodeSon;
        int indexURLNoName = 0;
        
        // Dividimos en SUBSection del nivel esperado nivel
        for (int index = 1; index < subSections.length; index++) {
            auxElement = null;
            nodeSon = padre;
            if (subSections[index].contains(String.format(SEMILLA, nivel + 1))) {
                // Si la division realizada contiene una subsection del nivel 
                //  inferior nivel + 1; recurrimos
                procesarSubSeccion(subSections[index], nivel + 1, nodeSon);
            } else {
                // En caso de que la SUBSection no contenga una subsection 
                //  inferior,la procesamos.
                
                //********* OBTENCION DEL NOMBRE DE LA SUBSECTION
                doc = Jsoup.parse(subSections[index]);
                auxElement = doc.selectFirst(JSOUP_SECCIONES_AREA_TITULO);
                if ((auxElement == null) && (subSections[index].contains(SECCIONES_AREA_TITULO_ZONA))) {
                    auxCadena = subSections[index].substring(subSections[index].indexOf(SECCIONES_AREA_TITULO_ZONA));
                    auxCadena = auxCadena.substring(auxCadena.indexOf(P_ETIQUETA_FIN) + P_ETIQUETA_FIN.length());
                    auxCadena = auxCadena.substring(0, auxCadena.indexOf(ETIQUETA_FIN));
                    auxCadena = Jsoup.parse(auxCadena).text();
                    
                    //********* ANIADIMOS UN NODO REPRESENTATIVO DE LA SECTION
                    if (auxCadena.length() > 1) {
                        // Para eviar SubSections vacias, utilizadas para aniadir
                        //  un hueco visualmente.
                        nodeSon = new Nodo("cccc", auxCadena, TipoNodo.SECTIONEXPAND, null);
                        padre.seccionAniadir(nodeSon);
                    }
                } else if (auxElement != null) {
                    auxCadena = auxElement.text();
                    //********* ANIADIMOS UN NODO REPRESENTATIVO DE LA SECTION
                    if (auxCadena.length() > 1) {
                        // Para eviar SubSections vacias, utilizadas para aniadir
                        //  un hueco visualmente. AQUI NO DEBERIA HACER FALTA.
                        nodeSon = new Nodo("cccc", auxCadena, TipoNodo.SECTIONEXPAND, null);
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
    private List<String> reconstruirSeccion(String seccion) {
        String[] lista = null;
        List<String> listaRespuesta = new ArrayList();
        Document doc;
        Document auxDoc;
        String lastEdent = "";
        String aux = "";
        String auxSangrado;
        String auxTipo;
        String auxEtiqueta;
        String miEtiqueta;
        String remplazo;
        int indexA;
        int indexB;
        int index2;
        
        //Dividimos seccion por el campo LI en el cual va un recurso,
        //  y reconstruimos la lista
        lista = seccion.split(JSOUP_SECCIONES);
        
        if (lista.length > 1) {
            // 0 es cabecera    

            aux = lista[0].substring(lista[0].indexOf(JSOUP_LI_ETIQUETA_CLASS));
            for (int ind = 1; ind < lista.length - 1; ind++) {
                lista[ind] = (aux + lista[ind]);
                aux = lista[ind].substring(lista[ind].lastIndexOf(JSOUP_LI_ETIQUETA_CLASS));
                lista[ind] = lista[ind].substring(0, lista[ind].lastIndexOf(JSOUP_LI_ETIQUETA_CLASS));
            }
            lista[(lista.length - 1)] = (aux + lista[(lista.length - 1)]);
            
            // Creamos una Lista de "nodos" LI, en la cual agrupamos el identado al 
            //  anterior
            //  A   B       C       D       E
            //  F-G-H       I       
            for (int index1 = 1; index1 < lista.length - 1; index1++) {
                doc = Jsoup.parse(lista[index1]);
                auxTipo = doc.selectFirst(JSOUP_LI_ETIQUETA).attr("class");
                
                if (auxTipo.equals(LI_ETIQEUTA)) {
                    //tragar todos los hijos
                    lastEdent = doc.selectFirst(JSOUP_SECCIONES_AREA_TIPO).attr("class");
                    remplazo = lista[index1];
                    for (index2 = index1 + 1; index2 < lista.length; index2++) {
                        auxDoc = Jsoup.parse(lista[index2]);
                        auxSangrado = auxDoc.selectFirst(JSOUP_SECCIONES_AREA_TIPO).attr("class");
                        if (lastEdent.compareTo(auxSangrado) < 0) {
                            remplazo += lista[index2];
                        }else{
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
            if ((listaRespuesta.size() >= 1) && (!((String) listaRespuesta.get(listaRespuesta.size() - 1)).contains(lista[(lista.length - 1)]))) {
                listaRespuesta.add(lista[(lista.length - 1)]);
            } else if (listaRespuesta.size() == 0) {
                listaRespuesta.add(lista[(lista.length - 1)]);
            }
            
            // Agrupamos los "nodos" de forma que las Label esten con una label "padre"
            //  Y los no label se agrupen en el "nodo" con un identado inferior
            // A-B-F-G-H-I      C-D-E
            indexA = listaRespuesta.size() - 1;
            while (indexA > 0) {
                indexB = indexA - 1;
                miEtiqueta = ((String) listaRespuesta.get(indexA)).substring(SECCIONES_AREA_ETIQUETA.length(), ((String) listaRespuesta.get(indexA)).indexOf(SECCIONES_AREA_ETIQUETA_FIN));
                
                if (miEtiqueta.equals(LI_ETIQEUTA)) {
                    auxSangrado = ((String) listaRespuesta.get(indexA)).substring(((String) listaRespuesta.get(indexA)).indexOf(SECCIONES_AREA_SENIAL) + SECCIONES_AREA_SENIAL.length(), ((String) listaRespuesta.get(indexA)).indexOf(SECCIONES_AREA_SENIAL_FIN));
                    // add al primer padre label
                    while (indexB >= 0) {
                        auxEtiqueta = ((String) listaRespuesta.get(indexB)).substring(SECCIONES_AREA_ETIQUETA.length(), ((String) listaRespuesta.get(indexB)).indexOf(SECCIONES_AREA_ETIQUETA_FIN));
                        lastEdent = ((String) listaRespuesta.get(indexB)).substring(((String) listaRespuesta.get(indexB)).indexOf(SECCIONES_AREA_SENIAL) + SECCIONES_AREA_SENIAL.length(), ((String) listaRespuesta.get(indexB)).indexOf(SECCIONES_AREA_SENIAL_FIN));
                        
                        if ((auxEtiqueta.equals(LI_ETIQEUTA)) && (auxSangrado.compareTo(lastEdent) > 0)) {
                            listaRespuesta.set(indexB, (String) listaRespuesta.get(indexB) + (String) listaRespuesta.get(indexA));
                            listaRespuesta.remove(indexA);
                            break;
                        }
                        indexB--;
                    }
                }else{
                    // add al primero no label
                    while (indexB >= 0) {
                        auxEtiqueta = ((String) listaRespuesta.get(indexB)).substring(SECCIONES_AREA_ETIQUETA.length(), ((String) listaRespuesta.get(indexB)).indexOf(SECCIONES_AREA_ETIQUETA_FIN));
                        if (!auxEtiqueta.equals(LI_ETIQEUTA)) {
                            listaRespuesta.set(indexB, (String) listaRespuesta.get(indexB) + (String) listaRespuesta.get(indexA));
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
     * Encuentra todos los enlaces utiles y los aniade al Nodo "padre" al que
     * pertenencen.
     *
     * @param seccion section sobre la que se desea encontrar todos los enlaces
     * @param padre Nodo "padre" al cual aniadiremos los enlaces encontrados
     * @param indiceUrlSinNombre indexado numeracion para enlaces encontrados
     * que no tengan nombre propio.
     *
     * @throws IOException Posible error con la I/O de datos.
     * @return Devuelve el indexado numerico
     */
    public int encontrarRecursos(Document seccion, Nodo padre, int indiceUrlSinNombre) throws IOException {
        String[] aux = null;
        String name = "";
        String typeAux = "";
        String url = "";
        TipoNodo type = null;
        Elements links = seccion.select("a");
        Element auxElement;     
        Nodo auxSon;
        // Para cuando ponen un link como Texto, en vez de aniadirlo como una 
        //  URL; darle una numeracion y nombre pordefecto.
        int indexURLNoName = indiceUrlSinNombre;
        
        for (int i = 0; i < links.size(); i++) {
            aux = null;
            //********* CONSTRUCION DE UN NODE REPRESENTATIVO
            auxElement = (Element) links.get(i);
            url = auxElement.select(JSOUP_A_ETIQUETA).attr("href");
            //********* OBTENCION DE NAME & TYPE ASOCIADOS
            aux = auxElement.select(JSOUP_SPAN_ETIQUETA).toString().split(SECCIONES_SPAN_DIVISION);
            if (aux.length > 1) {
                name = aux[1].replaceAll(LIMPIEZA_NOMBRE, "");

                // El profesor se ha podido olvidar de ponerle tipo
                typeAux = "Other";
                if (aux.length == 4) {
                    typeAux = aux[3].replaceAll(LIMPIEZA_NOMBRE, "");
                    typeAux = typeAux.replaceAll(LIMPIEZA_TIPO_FIN, "");
                }
                type = TipoNodo.getEnum(typeAux);
            } else {
                // Se trata de una URL puesta como texto plano
                type = TipoNodo.URL;
                name = ENLACE_NOMBRE_DEFECTO + indexURLNoName;
                indexURLNoName++;
            }
            
            if ((type != TipoNodo.FORO) && (!url.contains(A_RESERVADOR_01))) {
                auxSon = new Nodo(url, Jsoup.parse(name).text(), type, this.cookies);
                padre.archivosAniadir(auxSon);
                if ((type.equals(TipoNodo.FOLDER)) || (type.equals(TipoNodo.PAGE))) {
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
        Nodo other = (Nodo) obj;
        if ((!Objects.equals(this.nombre, other.nombre)) && (!this.tipo.equals(other.tipo))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return this.tipo.toString() + "{" + "url=" + this.url + ", Name=" + this.nombre + "}";
    }

    /**
     * Aniade un Nodo(hijo) representativo de un archivo al actual (padre)
     *
     * @param nodoHijo
     */
    public void archivosAniadir(Nodo nodoHijo) {
        if (this.archivos == null) {
            this.archivos = new HashSet();
        }
        this.archivos.add(nodoHijo);
    }
    /**
     * @return True set<> archivos == null | archivos.size() == 0. False en caso
     * contrario
     */
    private boolean archivosVacios() {
        if (this.archivos != null) {
            return this.archivos.isEmpty();
        }
        return true;
    }

    /**
     * Aniade un Nodo(hijo) representativo de una seccion al actual (padre)
     *
     * @param nodoHijo
     */
    public void seccionAniadir(Nodo nodoHijo) {
        if (this.secciones == null) {
            this.secciones = new HashSet();
        }
        this.secciones.add(nodoHijo);
    }
    /**
     * @return True set<> secciones == null | secciones.size() == 0. False en
     * caso contrario
     */
    private boolean seccionesVacias() {
        if (this.secciones != null) {
            return this.secciones.isEmpty();
        }
        return true;
    }

    
    /**
     * Creacion del contenido representativo del Nodo en el Sistema de Ficheros
     * (Local)
     *
     * @param pathDescarga path del SF donde descargar
     * @param httpclient cliente Http utilizado para descargar los archivos
     * @param iu controllador de la interfaz
     */
    private void descargarEnLocal(String pathDescarga, CloseableHttpClient httpclient, MainController iu) {
        LogRecord logRegistro = null;
        
        try {
            //**************SI PUEDE SER UN DIRECTORIO Y NO EXISTE LO CREAS**********
            if ((this.tipo == TipoNodo.CURSO) || (this.tipo == TipoNodo.FOLDER) || (this.tipo == TipoNodo.SECTIONCOLAP) || (this.tipo == TipoNodo.SECTIONEXPAND)) {
                File carpeta = new File(pathDescarga + File.separator + this.nombre);
                if (!carpeta.exists()) {
                    carpeta.mkdir();
                }
            }
            //**************************DESCARGAR LOS ARCHIVOS**********************
            if (this.archivos != null) {
                for (Nodo item : this.archivos) {
                    item.descargarEnLocal(pathDescarga + File.separator + this.nombre, httpclient, iu);
                }
            }
            //**************************DESCARGAR LAS SECTIONS**********************
            if (this.secciones != null) {
                for (Nodo item : this.secciones) {
                    item.descargarEnLocal(pathDescarga + File.separator + this.nombre, httpclient, iu);
                }
            }
            //********COMPROVAMOS QUE TIPO ES Y QUE NO ESTA DESCARGADO**********
            if ((this.tipo.equals(TipoNodo.ARCHIVO)) && (!archivoExiste(pathDescarga, this.nombre))) {
                descargarArchivo(pathDescarga, httpclient, iu);
            } else if ((this.tipo.equals(TipoNodo.URL)) && (!archivoExiste(pathDescarga, this.nombre + ".htm"))) {
                descargarEnlaceWeb(pathDescarga, httpclient, iu);
            } else if ((this.tipo.equals(TipoNodo.OTHER)) && (!archivoExiste(pathDescarga, this.nombre))) {
                descargarArchivo(pathDescarga, httpclient, iu);
            } else if (this.tipo.equals(TipoNodo.TAREA)) {
                descargarTarea(iu);
            }
        } catch (Exception e) {
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            logRegistro = new LogRecord(Level.WARNING, "Descargando: " + this.nombre + " on " + pathDescarga + "\n" + errors.toString());
            logRegistro.setSourceMethodName("descargarEnLocal");
            logRegistro.setSourceClassName(getClass().getName());
        } finally {
            if (logRegistro != null) {
                LogSincronizacion.log(logRegistro);
            }
        }
    }
    /**
     * Conecta con la URL de un Nodo archivo, obtencion de la extension esperada
     * y creacion en el SF de un archivo con dicha extension. NOTA: los archivos
     * binarios no tiene extension y se descargaran igual.
     *
     *
     * @param pathDescarga path del local donde descargar
     * @param httpclient cliente Http utilizado para descargar los archivos
     * @param iu controllador de la interfaz
     * 
     * @see
     * https://codereview.stackexchange.com/questions/116596/downloading-big-pdf-files
     * @see
     * http://hc.apache.org/httpcomponents-core-ga/httpcore/apidocs/org/apache/http/HttpEntity.html#isChunked()
     *
     */
    private void descargarArchivo(String pathDescarga, CloseableHttpClient httpclient, MainController iu) {
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
        byte[] buffer = new byte[1024];
        HttpRequestBase request = new HttpPost(this.url);
        
        try {
            //***************CONECTAMOS A LA URL************************************
            // Create a custom response handler
            response = httpclient.execute(request, context);
            entity = response.getEntity();
            List<URI> redirectURIs = context.getRedirectLocations();
            
            if (this.url.contains("forcedownload")) {
                 //La propia URL nos abre un canal directo al archivo
                formato = this.url;
                formato = formato.substring(0, formato.indexOf("forcedownload"));
                formato = formato.substring(formato.lastIndexOf("."), formato.indexOf("?"));
            } else if ((redirectURIs != null) && (!redirectURIs.isEmpty())) {
                //Hay Redireccion
                formato = ((URI) redirectURIs.get(0)).toString();
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
                if ((formato != null) && (this.url.compareTo(formato) != 0)) {
                    this.url = formato;
                    response.close();
                    request = new HttpPost(this.url);
                    response = httpclient.execute(request);
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
                    if ((formato == "") || (this.nombre.contains(formato))) {
                        nombreConFormato = this.nombre;
                    } else {
                        nombreConFormato = this.nombre + formato;
                    }
                    //Comprobacion de existencia
                    if(!archivoExiste(pathDescarga, nombreConFormato)){
                        fos = new FileOutputStream(new File(pathDescarga + File.separator + nombreConFormato));
                        while ((len = is.read(buffer)) != -1) {
                            fos.write(buffer, 0, len);
                            bytesBuffered += len;
                            if (bytesBuffered > 1048576) {
                                fos.flush();
                            }
                        }
                    iu.aniadirRecurso(pathDescarga + File.separator + nombreConFormato, this.nombre);
                    }
                }
            } else {
                // Se trata de un Nodo TipoNodo.OTHER, el cual no refleja una URL
                // que podamos tratar de descargar, entonces la tratara como un 
                // Link URL
                response.close();
                response = null;
                //Comprobacion de existencia
                if(!archivoExiste(pathDescarga, nombre+".htm")){
                    descargarEnlaceWeb(pathDescarga, httpclient, iu);
                }
            }
        } catch (Exception e) {
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            logRegistro = new LogRecord(Level.WARNING, this.nombre + "(" + this.url + ")\n" + errors.toString());
            logRegistro.setSourceMethodName("descargarArchivo");
            logRegistro.setSourceClassName(getClass().getName());
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ex) {
                    StringWriter errors = new StringWriter();
                    ex.printStackTrace(new PrintWriter(errors));
                    Logger.getLogger(Nodo.class
                            .getName()).log(Level.SEVERE, null, errors.toString());
                }
            }
            
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException ex) {
                    StringWriter errors = new StringWriter();
                    ex.printStackTrace(new PrintWriter(errors));
                    Logger.getLogger(Nodo.class
                            .getName()).log(Level.SEVERE, null, errors.toString());
                }
            }
            
            if (response != null) {
                try {
                    response.close();
                } catch (IOException ex) {
                    StringWriter errors = new StringWriter();
                    ex.printStackTrace(new PrintWriter(errors));
                    Logger.getLogger(Nodo.class
                            .getName()).log(Level.SEVERE, null, errors.toString());
                }
            }
            if (logRegistro != null) {
                LogSincronizacion.log(logRegistro);
            }
        }
    }
    /**
     * Dada un Nodo TipoNodo.URL obtendra el link final al que direcciona, y
     *  creara un archivo URL en el Sistema de Ficheros que enlazara con dicho 
     *  Link.
     *
     * @param pathDescarga path del local donde descargar
     * @param httpclient cliente Http utilizado para conectar a la Url
     * @param iu controllador de la interfaz
     * 
     * @throws Exception posibles excepciones debido a Jsoup
     * @see https://jsoup.org/apidocs/org/jsoup/Connection.html#get--
     */
    private void descargarEnlaceWeb(String pathDescarga, CloseableHttpClient httpclient, MainController iu) throws Exception {
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
        HttpRequestBase request = new HttpPost(this.url);
        try {
            //Miramos si la url redirige a moodle, porque la verdadera url estara
            //  en span9, podriamos parsear con Jsoup pero la linea que buscamos
            //  esta hacia la mitad y quiza cueste menos que parsear
            if (this.url.contains("moodle2.unizar.es/add/mod/url")) {
                response = httpclient.execute(request, context);
                entity = response.getEntity();
                List<URI> redirectURIs = context.getRedirectLocations();
                
                if (redirectURIs == null) {
                    is = entity.getContent();
                    bufferedReader = new BufferedReader(new InputStreamReader(is));
                    
                    while ((line = bufferedReader.readLine()) != null) {
                        if (line.contains(NO_REDIRECCION_AREA)) {
                            urlAux = line.substring(0, line.indexOf(SALTAR_NAVEGACION));
                            urlAux = urlAux.substring(urlAux.indexOf("<a href=\"") + "<a href=\"".length());
                            urlAux = urlAux.substring(0, urlAux.indexOf("\" >"));
                            break;
                        }
                    }
                    //Sacado del compilador, guardar por si acaso
//                    do {
//                        if ((line = bufferedReader.readLine()) == null) {
//                            break;
//                        }
//                    } while (!line.contains(NO_REDIRECCION_AREA));
//                    urlAux = line.substring(0, line.indexOf(SALTAR_NAVEGACION));
//                    urlAux = urlAux.substring(urlAux.indexOf("<a href=\"") + "<a href=\"".length());
//                    urlAux = urlAux.substring(0, urlAux.indexOf("\" >"));
                } else {
                    urlAux = redirectURIs.get(0).toString();
                }
            }
            fw = new FileWriter(pathDescarga + File.separator + this.nombre + ".htm");
            String seed = "<script type=\"text/javascript\" src=\"/712BCC42-17A0-D44D-A1E8-AA762A409D44/main.js\" charset=\"UTF-8\"></script><script>window.googleJavaScriptRedirect=1</script><META name=\"referrer\" content=\"origin\"><script>var n={navigateTo:function(b,a,d){if(b!=a&&b.google){if(b.google.r){b.google.r=0;b.location.href=d;a.location.replace(\"about:blank\");}}else{a.location.replace(d);}}};n.navigateTo(window.parent,window,\"%s\");\n"
                    + "</script><noscript><META http-equiv=\"refresh\" content=\"0;URL='%s'\"></noscript>";
            seed = String.format(seed, urlAux, urlAux);
            fw.write(seed);
            iu.aniadirRecurso(pathDescarga + File.separator + this.nombre + ".htm", this.nombre);
        } catch (Exception e) {
            //El enlace esta caido, podemos capturar al error o crear en enlace
            // con la Url caid y que los alumnos al ver en enlace que no va 
            // comuniquen al profesor.
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            logRegistro = new LogRecord(Level.WARNING, this.nombre + "(" + this.url + ")\n" + errors.toString());
            logRegistro.setSourceMethodName("descargarEnlaceWeb");
            logRegistro.setSourceClassName(getClass().getName());
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
     * Dado un Nodo TipoNodo.Tarea recogera la informacion distintiva y util
     *  y se la comunicara a la iu (MainController). Siendo estas:
     *  Nombre curso
     *  Nombre entrega
     *  Fichero asociado (si hubiera un fichero entregado)
     *  Fecha maxima de entrega
     *  Lenguaje (para la fecha)
     *  Nota
     *  Feedback (comentarios del profesor)
     *  Url de la tarea
     * 
     * 
     * @param iu controllador de la interfaz
     * @throws Exception 
     */
    private void descargarTarea(MainController iu) throws Exception {
        String ficheroEntrega = "";
        double noteAux = -1.0;
        String feedBack = "";
        String fecha = "";
        Document doc = Jsoup.connect(this.url).timeout(180000).cookies(this.cookies).get();
        String languague = doc.selectFirst("*[lang]").attr("lang");
        String curso = doc.selectFirst("div>h1").text();
        String nombre = doc.selectFirst("div>h2").text();
        String note;
        Element basic = doc.selectFirst("div[role=main]>div[class=submissionstatustable]");
        Elements interes = basic.select("td[class=cell c1 lastcol]");
        
        for (Element resource : interes) {
            if (resource.text().matches(".*2[0-9]{3}.*")) {
                fecha = resource.text();
                break;
            }
        }
        Element aux = basic.selectFirst("li[yuiConfig]");
        if (aux != null) {
            String auxString = aux.text();
            ficheroEntrega = auxString.equals("-") ? "" : auxString;
        } else {
            ficheroEntrega = "";
        }
        basic = doc.selectFirst("div[class=feedback]");
        if (basic != null) {
            interes = basic.select("td[class=cell c1 lastcol]");
            String auxString = ((Element) interes.get(0)).text().replaceAll(" ", "");
            auxString = auxString.replaceAll(",", ".");
            noteAux = Double.valueOf(auxString.split("/")[0]).doubleValue();
            noteAux = noteAux >= 10.0 ? noteAux / 10.0 : noteAux;

            interes = basic.select("tr[class=lastrow]>td>div");
            if (interes != null) {
                if ((interes.size() < 2) || (((Element) interes.get(0)).toString().contains("_assignfeedback_file"))) {
                    feedBack = "feedback_file";
                } else if (((Element) interes.get(0)).toString().contains("_assignfeedback_comments_")) {
                    feedBack = ((Element) interes.get(1)).text();
                }
            }
        }
        note = noteAux >= 0.0 ? noteAux + "" : "";
        iu.aniadirTarea(curso, nombre, ficheroEntrega, fecha, languague, note, feedBack, this.url);
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
     * Metodo para comprobar si existe un archivo (file) en un directorio,
     * independiente de su extension.
     *
     * @param path del directorio sobre el que se hace la comprobacion
     * @param archivo nombre del archivo que se quiere comprobar
     * @return True, si el archivo existe. False en caso contrario. //@throws
     * Exception posible error debido a caracteres reservados en SF
     *
     * @see <a href="https://stackoverflow.com/questions/17697646/how-to-detect-if-a-filewith-any-extension-exist-in-java">Link 01</a>
     */
    public boolean archivoExiste(String path, String archivo) {
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();
        if (listOfFiles != null) {
            for (File file : listOfFiles) {
                if ((file.isFile())
                        && (file.getName().contains(archivo))) {
                    return true;
                }
            }
        }
        return false;
    }
}
