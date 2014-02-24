package tops.web.display.servlet;

import java.io.File;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.FileNotFoundException;

import java.util.Arrays;
import java.util.HashMap;

import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;

public class TopsFileManager {

    private HashMap<String, String> paths;

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

    public static void main(String[] args) {
        TopsFileManager tfm = new TopsFileManager("./");
        System.out.println(Arrays.toString(args));
        String className = args[0];
        String path = args[1];
        String pdbId = args[2];
        
        tfm.addPathMapping(className, path);
        String chain = null;

        if (!args[3].equals("-")) {
            chain = args[3];
        }

        try {
           
            BufferedReader br = null;
            if (path.endsWith("gz")) {
            	String topsfile = pdbId + chain;
            	System.out.println("getting " + topsfile);
            	br = new BufferedReader(new InputStreamReader(tfm.getStreamFromZip(path, topsfile)));
            } else {
            	 String[] names = tfm.getNames(className, pdbId, chain);
                 for (int i = 0; i < names.length; i++) {
                     System.out.println("name " + i + " = " + names[i]);
                 }
            	br = new BufferedReader(new InputStreamReader(tfm.getStreamFromDir(className, names[0])));
            }
            String line;
            try {
                while ((line = br.readLine()) != null) {
                    System.out.print(line);
                }
                br.close();
            } catch (IOException ioe) {
                System.out.println(ioe);
            }
        } catch (FileNotFoundException fnf) {
            System.out.println(fnf);
        }
    }
}
