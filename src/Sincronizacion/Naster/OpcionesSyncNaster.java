package Sincronizacion.Naster;

//#3 Third party
import com.github.sardine.DavResource;
import com.github.sardine.impl.SardineException;
import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;
//#4 Java
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Set;

/**
 * @author Diego
 *
 * @version 1.0 Clase que sincroniza el contenido de un path local con el
 * repositorio NAS-TER para el anio actual, manteniendo la ultima version de los
 * ficheros
 *
 *
 */
public class OpcionesSyncNaster {

    //Constantes config para conexion
    private static final String CARPETASINC = "probando";
    private static final long TAMANIO_BUFFER_COPIA = 16 * 1024 * 1024; // 16 MB
    private static final String URLNAS = "https://nas-ter.unizar.es/alumnos/";

    private static Sardine sardineCon;

    /**
     *
     * @param usuario identificador del usuario con el que logeamos en NAS-TER
     * @param contrasenia passwd del usuario con el que logeamos en NAS-TER
     * @param pathLocal path del local sobre el que realizaremos la
     * sincronizacion
     * @param anio anio sobre el que se realiza la sincronizacion
     *
     * @return -1 - NasTer posiblemente caido -2 - Credenciales erroneas -3 -
     * Fallo al crear la estructura de sincronizacion -4 - Fallo no identificado
     * en conexion
     *
     */
    public static int sincronizar(String usuario, String contrasenia, String pathLocal, String anio) {

//        long time_start, time_end;
//        time_start = System.currentTimeMillis();
        int respuesta = 0;
        try {
            sardineCon = SardineFactory.begin(usuario, contrasenia);
            URI url = URI.create(URLNAS + "/" + usuario + "/" + CARPETASINC);
            // Para comprobar que la carpeta que se utiliza para sincronizar existe
            //  de esta forma no molestaremos con lo que tenga el usuario
            if (!sardineCon.exists((url.toString()))) {
                sardineCon.createDirectory(url.toString());
                if (!sardineCon.exists((url.toString()))) {
                    respuesta = -3;
                }
            }

            if (respuesta == 0) {
                File source = new File(pathLocal);
                Set<String> allfiles = new HashSet<String>();
                for (File auxLocal : source.listFiles()) {
                    if (auxLocal.getName().contains(anio)) {
                        allfiles.add(auxLocal.getName());
                    }
                }
                for (DavResource auxRemote : sardineCon.list(url.toString())) {
                    if (auxRemote.getName().contains(anio)) {
                        allfiles.add(auxRemote.getName());
                    }
                }
                File auxfile;
                String auxName;
                for (String nameResource : allfiles) {
                    auxfile = new File(source, nameResource);
                    auxName = url.toString() + "//" + nameResource.replaceAll(" ", "%20");
                    sincronizar(auxfile, auxName, false);
                }
//                sincronizar(new File(pathLocal), url.toString(), true);
                sardineCon.shutdown();
            }
        } catch (SardineException e) {
            // Sardine lanza un error, este puede ser
            if (e.getStatusCode() == 401) {
                // Credenciales erroneas
                respuesta = -2;
            } else {
                // Para identificar otras posibilidades
                respuesta = -4;

            }
            e.printStackTrace();
        } catch (IOException e) {
            // Salta el timeOut, parece que no se extablece la conexion
            respuesta = -1;
        } finally {
//            time_end = System.currentTimeMillis();
//            System.out.println("\n\n--the task has taken " + (time_end - time_start) + " milliseconds");
            return respuesta;
        }
    }

    /**
     * @param ficheroRecurso fichero representativo en local del recurso
     * @param pathRemoto path del fichero en remoto
     * @param esRaiz para identificar la primera raiz de la sincronizacion
     *
     * @throws @throws Exception normalmente IOException - I/O error or HTTP
     * response validation failure
     * @see
     * http://www.atetric.com/atetric/javadoc/com.github.lookfirst/sardine/5.4/com/github/sardine/Sardine.html
     */
    private static void sincronizar(File ficheroRecurso, String pathRemoto, boolean esRaiz) throws Exception {
        List<DavResource> listFilesRemote = null;// = sardineCon.list(destination);

        if (ficheroRecurso.exists()) {
            // Comprobamos que el file existe en local
            if (sardineCon.exists(pathRemoto)) {
                // Comrpobamos que el file existe en 
                listFilesRemote = sardineCon.list(pathRemoto);
                if (ficheroRecurso.getName().equals(listFilesRemote.get(0).getName()) || esRaiz) {
                    // Mal comprobado, pero comprobamos que sean el mismo tipo

                    // Comprobamos que ambos son directorios
                    if (ficheroRecurso.isDirectory() && listFilesRemote.get(0).isDirectory()) {
                        // Ambos son directorios, crearemos una Lista conjunta
                        //  de contenido y relanzaremos el metodo
                        Set<String> allfiles = new HashSet<String>(Arrays.asList(ficheroRecurso.list()));
                        for (int i = 1; i < listFilesRemote.size(); i++) {
                            allfiles.add(listFilesRemote.get(i).getName());
                        }
                        File auxfile;
                        String auxName;
                        for (String nameResource : allfiles) {
                            auxfile = new File(ficheroRecurso, nameResource);
                            auxName = pathRemoto + "//" + nameResource.replaceAll(" ", "%20");
                            sincronizar(auxfile, auxName, false);
                        }

                    } else {
                        // Ambos son No son directorios, comprobaremos cual fue
                        //  el ultimo modificado y actuaremos en consecuencia
                        long sourceMod = ficheroRecurso.lastModified();
                        long remoteMod = listFilesRemote.get(0).getModified().getTime();

                        if (sourceMod > remoteMod) {
//                            System.out.println("Subiendo");
                            subirArchivo(ficheroRecurso, pathRemoto);

                        } else if (ficheroRecurso.length() != listFilesRemote.get(0).getContentLength()) {
//                            System.out.println("Bajando");
                            descargarArchivo(ficheroRecurso, pathRemoto);
                        }
//                        else {
//                            System.out.println("Son iguales");
//                        }
                    }

                } else {
                    // Los files no son del mismo tipo (remoto y local) 
                    //  borraremos remote y actuamos segun local
                    sardineCon.delete(pathRemoto);
                    actuarDeLocalaRemoto(ficheroRecurso, pathRemoto);
                }
            } else {
                // El file no existe en remote, se actua segun local
                actuarDeLocalaRemoto(ficheroRecurso, pathRemoto);
            }
        } else {
            listFilesRemote = sardineCon.list(pathRemoto);
            if (listFilesRemote.get(0).isDirectory()) {
                // Sino existe en local el file, y en remoto es un directorio
                //  lo creamos en local
                ficheroRecurso.mkdir();
                Set<String> allfiles = new HashSet<String>();
                for (int i = 1; i < listFilesRemote.size(); i++) {
                    allfiles.add(listFilesRemote.get(i).getName());
                }
                File auxfile;
                String auxName;
                for (String nameResource : allfiles) {
                    auxfile = new File(ficheroRecurso, nameResource);
                    auxName = pathRemoto + "//" + nameResource.replaceAll(" ", "%20");
                    sincronizar(auxfile, auxName, false);
                }
            } else {
                // En caso de que el file no exista en local, y en remoto NO es un
                //  directorio, lo descargaremos del remoto
                descargarArchivo(ficheroRecurso, pathRemoto);
            }
        }

    }

    /**
     *
     * @param ficheroRecurso fichero representativo en local del recurso
     * @param pathRemoto path del fichero en remoto
     *
     * @throws Exception normalmente IOException - I/O error or HTTP response
     * validation failure
     * @see
     * http://www.atetric.com/atetric/javadoc/com.github.lookfirst/sardine/5.4/com/github/sardine/Sardine.html
     */
    private static void actuarDeLocalaRemoto(File ficheroRecurso, String pathRemoto) throws Exception {
        if (ficheroRecurso.isDirectory()) {

            sardineCon.createDirectory(pathRemoto);
            Set<String> allfiles = new HashSet<String>(Arrays.asList(ficheroRecurso.list()));
            File auxfile;
            String auxName;
            for (String nameResource : allfiles) {
                auxfile = new File(ficheroRecurso, nameResource);
                auxName = pathRemoto + "//" + nameResource.replaceAll(" ", "%20");
                sincronizar(auxfile, auxName, false);
            }
        } else {
            subirArchivo(ficheroRecurso, pathRemoto);
        }
    }

    /**
     *
     * @param ficheroRecurso fichero representativo en local del recurso
     * @param pathRemoto path del fichero en remoto
     */
    private static void subirArchivo(File ficheroRecurso, String pathRemoto) {
        try (InputStream fis = new FileInputStream(ficheroRecurso)) {
            sardineCon.put(pathRemoto.replaceAll(" ", "%20"), fis);
        } catch (IOException ex) {
//            Logger.getLogger(OpcionesSyncNaster.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        }
    }

    /**
     *
     * @param ficheroRecurso fichero representativo en local del recurso
     * @param pathRemoto path del fichero en remoto
     */
    private static void descargarArchivo(File ficheroRecurso, String pathRemoto) {
        try (InputStream is = sardineCon.get(pathRemoto);
                OutputStream outStream = new FileOutputStream(ficheroRecurso);) {
            byte[] buffer = new byte[8 * 1024];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                outStream.write(buffer, 0, bytesRead);
            }
            outStream.flush();
        } catch (SardineException e) {
            // fail(e.getMessage());
            e.printStackTrace();
        } catch (IOException ex) {
            Logger.getLogger(OpcionesSyncNaster.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        }
    }
}
