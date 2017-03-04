package tops.web.display.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class TopsFileManager {

    private Map<String, String> paths;

    private String pathToZip;

//    private String suffix = ".tops";

    public TopsFileManager() {
        this.pathToZip = ".";
        this.paths = new HashMap<String, String>();
    }

    public TopsFileManager(String path) {
        this();
        this.pathToZip = path;
    }

    public void addPathMapping(String className, String path) {
        this.paths.put(className, path);
    }

    public String getPathMapping(String className) {
        return this.paths.get(className);
    }

    public String[] getNames(String className, String pdbid, String chain) throws FileNotFoundException {
        String path = this.paths.get(className);
        System.out.println("path = " + path);
        File directory = new File(path);
        String[] names = directory.list(new TopsFileFilter(pdbid, chain));
        System.out.println("names = " + Arrays.toString(names));
        if (names.length == 0) {
            throw new FileNotFoundException("file : " + pdbid + chain + ".tops not found in dir");
        } else {
            return names;
        }
    }

    public String getNameFromDir(String className, String name) throws FileNotFoundException {
        String path = this.paths.get(className);
        File topsfile = new File(path, name);
        return topsfile.getName();
    }

    public InputStream getStreamFromDir(String className, String name) throws FileNotFoundException {
        String path = this.paths.get(className);
        File topsfile = new File(path, name);
        return new FileInputStream(topsfile);
    }

    public InputStream getStreamFromZip(String zipfile, String topsfile) throws FileNotFoundException {
        ZipFile ztops = this.initZip(zipfile);
        ZipEntry ze;
        InputStream is = null;

        if (ztops == null) {
            throw new FileNotFoundException("file : " + this.pathToZip + zipfile + ".zip not found");
        }

        ze = ztops.getEntry(topsfile + ".tops");

        if (ze != null) {
            try {
                is = ztops.getInputStream(ze);
            } catch (IOException ioe) {
                System.out.println(ioe);
            }
        } else {
            throw new FileNotFoundException("file : " + topsfile + ".tops not found in zip");
        }
        return is;
    }

    private ZipFile initZip(String zipf) {
        ZipFile z = null;
        try {
//            z = new ZipFile(this.pathToZip + zipf + ".zip");
        	z = new ZipFile(this.pathToZip + zipf);
        } catch (IOException ioe) {
            System.out.println(ioe);
        }
        return z;
    }
}
