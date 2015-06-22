package bi.meteorite.plugin;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Hello world!
 *
 */
public class App 
{
    private static final Log LOG = LogFactory.getLog(App.class);

    String CEOSGILIBS = "0.1";
    String EEOSGILIBS = "0.1";
    String COREOSGI = "0.1";
    URL LOCATION = App.class.getProtectionDomain().getCodeSource().getLocation();
    String backup = LOCATION.getPath().substring(0, LOCATION.getPath().lastIndexOf("/"))+"/../backup.zip";
    String installed = LOCATION.getPath().substring(0, LOCATION.getPath().lastIndexOf("/"))+"/../.installedversion";

    public void zipIt(String zipFile, String SOURCE_FOLDER)
    {
        ArrayList<String> fileList = generateFileList(new File(SOURCE_FOLDER), SOURCE_FOLDER);

        byte[] buffer = new byte[1024];
        String source = "";
        FileOutputStream fos = null;
        ZipOutputStream zos = null;
        try
        {
            try
            {
                source = SOURCE_FOLDER.substring(SOURCE_FOLDER.lastIndexOf("\\") + 1, SOURCE_FOLDER.length());
            }
            catch (Exception e)
            {
                source = SOURCE_FOLDER;
            }
            fos = new FileOutputStream(zipFile);
            zos = new ZipOutputStream(fos);

            System.out.println("Output to Zip : " + zipFile);
            FileInputStream in = null;

            for (String file : fileList)
            {
                System.out.println("File Added : " + file);
                ZipEntry ze = new ZipEntry(source + File.separator + file);
                zos.putNextEntry(ze);
                try
                {
                    in = new FileInputStream(SOURCE_FOLDER + File.separator + file);
                    int len;
                    while ((len = in.read(buffer)) > 0)
                    {
                        zos.write(buffer, 0, len);
                    }
                }
                finally
                {
                    in.close();
                }
            }

            zos.closeEntry();
            System.out.println("Folder successfully compressed");

        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
        finally
        {
            try
            {
                zos.close();
            }
            catch (IOException e)
            {
                LOG.error("Couldn't zip libs, IO Exception", e);
            }
        }
    }

    public ArrayList<String> generateFileList(File node, String SOURCE_FOLDER)
    {

        // add file only
        ArrayList<String> fileList = new ArrayList<String>();
        if (node.isFile())
        {
            fileList.add(generateZipEntry(node.toString(), SOURCE_FOLDER));

        }

        if (node.isDirectory())
        {
            String[] subNote = node.list();
            for (String filename : subNote)
            {
                generateFileList(new File(node, filename), SOURCE_FOLDER);
            }
        }

        return fileList;
    }

    private String generateZipEntry(String file, String SOURCE_FOLDER)
    {
        return file.substring(SOURCE_FOLDER.length() + 1, file.length());
    }

    public void loadFiles(String version){


        System.out.println(LOCATION.getPath().substring(0, LOCATION.getPath().lastIndexOf("/")));


        try {
            zipIt(backup,
                LOCATION.getPath().substring(0, LOCATION.getPath().lastIndexOf("/")) + "/../../osgi/bundles/");
        }
        catch (Exception e){
            LOG.error("Couldn't backup OSGI files", e);
        }

        try {
            if (version.equals("CE")) {
                LOG.info("Extracting OSGI Libs");
                String zippath =
                    LOCATION.getPath().substring(0, LOCATION.getPath().lastIndexOf("/")) + "/."
                    + "./CEOSGILIBS-"+CEOSGILIBS+".zip";

                unzipLibs(zippath,
                    LOCATION.getPath().substring(0, LOCATION.getPath().lastIndexOf("/")) + "/../../osgi/bundles/");

                zippath =
                    LOCATION.getPath().substring(0, LOCATION.getPath().lastIndexOf("/")) + "/."
                    + "./COREOSGILIBS-"+COREOSGI+".zip";

                FileUtils.cleanDirectory(new File(LOCATION.getPath().substring(0, LOCATION.getPath().lastIndexOf("/")
                ) + "/../../osgi/core_bundles/"));

                unzipLibs(zippath,
                    LOCATION.getPath().substring(0, LOCATION.getPath().lastIndexOf("/")) + "/../../osgi/core_bundles/");

                /*FileUtils.cleanDirectory(new File(LOCATION.getPath().substring(0, LOCATION.getPath().lastIndexOf("/")
                ) + "/../../osgi/cache/"));*/

                File f = new File(LOCATION.getPath().substring(0, LOCATION.getPath().lastIndexOf("/")
                ) + "/../../saiku/lib/olap4j-1.2.0.jar");
                if(f.exists()) {
                    FileUtils.forceDelete(f);
                }
            } else {
                LOG.info("Extracting EE OSGI Libs");
                String zippath = LOCATION.getPath().substring(0, LOCATION.getPath().lastIndexOf("/")) + "/."
                                 + "./EEOSGILIBS-"+EEOSGILIBS+".zip";

                unzipLibs(zippath,
                    LOCATION.getPath().substring(0, LOCATION.getPath().lastIndexOf("/")) + "/../../osgi/bundles/");

                /*FileUtils.cleanDirectory(new File(LOCATION.getPath().substring(0, LOCATION.getPath().lastIndexOf("/")
                ) + "/../../osgi/cache/"));*/
            }
        }
        catch (Exception e){
            LOG.error("Could not extract Mondrian to OSGI bundle directory", e);
        }

        try {
            PrintWriter out = new PrintWriter(installed);
            if(version.equals("CE")){
                out.println(CEOSGILIBS);
            }
            else{
                out.println(EEOSGILIBS);
            }

            out.close();

        } catch (FileNotFoundException e) {
            LOG.error("Could not write to installer log", e);
        }


    }

    private void unzipLibs(String zipFile, String outputFolder) {
        byte[] buffer = new byte[1024];

        try {
            File folder = new File(outputFolder);
            if (!folder.exists()) {
                throw new Exception("Unknown Path");
            }

            //get the zip file content
            ZipInputStream zis =
                new ZipInputStream(new FileInputStream(zipFile));
            //get the zipped file list entry
            ZipEntry ze = zis.getNextEntry();

            while (ze != null) {

                String fileName = ze.getName();
                File newFile = new File(outputFolder + File.separator + fileName);

                System.out.println("file unzip : " + newFile.getAbsoluteFile());

                //create all non exists folders
                //else you will hit FileNotFoundException for compressed folder
                new File(newFile.getParent()).mkdirs();

                FileOutputStream fos = new FileOutputStream(newFile);

                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }

                fos.close();
                ze = zis.getNextEntry();
            }

            zis.closeEntry();
            zis.close();

            System.out.println("Done");

        } catch (IOException ex) {
            LOG.error("Couldn't unzip libs, IO Exception",ex);
        } catch (Exception e) {
            LOG.error("Couldn't unzip libs",e);
        }
    }

    public BIVersion getVersion() {
        Class c = PentahoSessionHolder.getSession().getClass();
        ClassLoader c2l = c.getClassLoader();

        URL[] urls = ((URLClassLoader)c2l).getURLs();

        BIVersion v = new BIVersion("CE");
        for(URL url: urls){
            if(url.getFile().contains("pentaho-bi-platform-ee")){
                v.setVersion("EE");
                break;
            }
        }
        return v;
    }

    public String getInstalledVersion() {

        File f = new File(installed);
        if(f.exists() && !f.isDirectory()) {


            FileInputStream inputStream = null;
            try {
                inputStream = new FileInputStream(installed);
            } catch (FileNotFoundException e) {
                LOG.error("Could not open FileInputStream", e);
            }
            String everything = null;
            try {
                if (inputStream != null) {
                    everything = IOUtils.toString(inputStream);
                }
            } catch (IOException e) {
                LOG.error("IO Exception reading file", e);
            } finally {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (IOException e) {
                    LOG.error("IO Exception closing file", e);
                }
            }
            return everything;
        }
        return null;
    }
}
