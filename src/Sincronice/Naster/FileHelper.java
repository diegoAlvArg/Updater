package Sincronice.Naster;

import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;
import com.github.sardine.impl.SardineException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for synchronizing files/directories.
 *
 */
public class FileHelper {

    private static final long DEFAULT_COPY_BUFFER_SIZE = 16 * 1024 * 1024; // 16 MB
    private static Sardine sardineCon;
    private static final String urlNAS = "https://nas-ter.unizar.es/alumnos/";
    private static final String folderName = "probando";

    /**
     *
     * @param user
     * @param pass
     * @param pathLocal
     * @return 
     * -1 - NasTer posiblemente caido 
     * -2 - Credenciales erroneas 
     * -3 - Fallo al crear la estructura de sincronizacion
     * -4 - Fallo no identificado en conexion
     *
     */
    public static int synchronize(String user, String pass, String pathLocal) {

//        long time_start, time_end;
//        time_start = System.currentTimeMillis();
        int respuesta = 0;
        try {
            sardineCon = SardineFactory.begin(user, pass);
            URI url = URI.create(urlNAS + "/" + user + "/" + folderName);
            // Para comprobar que la carpeta que se utiliza para sincronizar existe
            //  de esta forma no molestaremos con lo que tenga el usuario
            if (!sardineCon.exists((url.toString()))) {
                sardineCon.createDirectory(url.toString());
                if (!sardineCon.exists((url.toString()))) {
                    respuesta = -3;
                }
            }

            if (respuesta == 0) {
                synchronize(new File(pathLocal), url.toString(), true);
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
     * @param source
     * @param destination
     * @param smart
     * @param chunkSize
     * @throws IOException
     */
    private static void synchronize(File source, String destination, boolean isroot) throws Exception {
        List<DavResource> listFilesRemote = null;// = sardineCon.list(destination);

        if (source.exists()) {
            // Comprobamos que el file existe en local
            if (sardineCon.exists(destination)) {
                // Comrpobamos que el file existe en 
                listFilesRemote = sardineCon.list(destination);
                if (source.getName().equals(listFilesRemote.get(0).getName()) || isroot) {
                    // Mal comprobado, pero comprobamos que sean el mismo tipo

                    // Comprobamos que ambos son directorios
                    if (source.isDirectory() && listFilesRemote.get(0).isDirectory()) {
                        // Ambos son directorios, crearemos una Lista conjunta
                        //  de contenido y relanzaremos el metodo
                        Set<String> allfiles = new HashSet<String>(Arrays.asList(source.list()));
                        for (int i = 1; i < listFilesRemote.size(); i++) {
                            allfiles.add(listFilesRemote.get(i).getName());
                        }
                        File auxfile;
                        String auxName;
                        for (String nameResource : allfiles) {
                            auxfile = new File(source, nameResource);
                            auxName = destination + "//" + nameResource.replaceAll(" ", "%20");
                            synchronize(auxfile, auxName, false);
                        }

                    } else {
                        // Ambos son No son directorios, comprobaremos cual fue
                        //  el ultimo modificado y actuaremos en consecuencia
                        long sourceMod = source.lastModified();
                        long remoteMod = listFilesRemote.get(0).getModified().getTime();

                        if (sourceMod > remoteMod) {
//                            System.out.println("Subiendo");
                            uploadFile(source, destination);

                        } else if (source.length() != listFilesRemote.get(0).getContentLength()) {
//                            System.out.println("Bajando");
                            downloadFile(source, destination);
                        }
//                        else {
//                            System.out.println("Son iguales");
//                        }
                    }

                } else {
                    // Los files no son del mismo tipo (remoto y local) 
                    //  borraremos remote y actuamos segun local
                    sardineCon.delete(destination);
                    actionToRemote(source, destination);
                }
            } else {
                // El file no existe en remote, se actua segun local
                actionToRemote(source, destination);
            }
        } else {
            listFilesRemote = sardineCon.list(destination);
            if (listFilesRemote.get(0).isDirectory()) {
                // Sino existe en local el file, y en remoto es un directorio
                //  lo creamos en local
                source.mkdir();
                Set<String> allfiles = new HashSet<String>();
                for (int i = 1; i < listFilesRemote.size(); i++) {
                    allfiles.add(listFilesRemote.get(i).getName());
                }
                File auxfile;
                String auxName;
                for (String nameResource : allfiles) {
                    auxfile = new File(source, nameResource);
                    auxName = destination + "//" + nameResource.replaceAll(" ", "%20");
                    synchronize(auxfile, auxName, false);
                }
            } else {
                // En caso de que el file no exista en local, y en remoto NO es un
                //  directorio, lo descargaremos del remoto
                downloadFile(source, destination);
            }
        }

    }

    /**
     *
     * @param sourceTarget
     * @param destinationTarget
     */
    private static void actionToRemote(File sourceTarget, String destinationTarget) throws Exception {
        if (sourceTarget.isDirectory()) {

            sardineCon.createDirectory(destinationTarget);
            Set<String> allfiles = new HashSet<String>(Arrays.asList(sourceTarget.list()));
            File auxfile;
            String auxName;
            for (String nameResource : allfiles) {
                auxfile = new File(sourceTarget, nameResource);
                auxName = destinationTarget + "//" + nameResource.replaceAll(" ", "%20");
                synchronize(auxfile, auxName, false);
            }
        } else {
            uploadFile(sourceTarget, destinationTarget);
        }
    }
    private static void uploadFile(File sourceTarget, String destinationTarget) {
        try (InputStream fis = new FileInputStream(sourceTarget)) {
            sardineCon.put(destinationTarget.replaceAll(" ", "%20"), fis);
        } catch (IOException ex) {
//            Logger.getLogger(FileHelper.class.getName()).log(Level.SEVERE, null, ex);
//            ex.printStackTrace();
        }
    }

    private static void downloadFile(File sourceTarget, String destinationTarget) {
        try (InputStream is = sardineCon.get(destinationTarget);
                OutputStream outStream = new FileOutputStream(sourceTarget);) {
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
            Logger.getLogger(FileHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
