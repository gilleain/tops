package tops.web.display.servlet;

import java.io.File;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.FileNotFoundException;

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
        return (String) this.paths.get(className);
    }

    public String[] getNames(String className, String pdbid, String chain)
            throws FileNotFoundException {
        String path = (String) this.paths.get(className);
        File directory = new File(path);
        String[] names = directory.list(new TopsFileFilter(pdbid, chain));
        if (names.length == 0)
            throw new FileNotFoundException("file : " + pdbid + chain
                    + ".tops not found in dir");
        else
            return names;
    }

    public String getNameFromDir(String className, String name)
            throws FileNotFoundException {
        String path = (String) this.paths.get(className);
        File topsfile = new File(path, name);
        return topsfile.getName();
    }

    public InputStream getStreamFromDir(String className, String name)
            throws FileNotFoundException {
        String path = (String) this.paths.get(className);
        File topsfile = new File(path, name);
        return new FileInputStream(topsfile);
    }

    public InputStream getFromZip(String zipfile, String topsfile)
            throws FileNotFoundException {
        ZipFile ztops = this.initZip(zipfile);
        ZipEntry ze;
        InputStream is = null;

        if (ztops == null)
            throw new FileNotFoundException("file : " + this.pathToZip + zipfile
                    + ".zip not found");

        ze = ztops.getEntry(topsfile + ".tops");

        if (ze != null) {
            try {
                is = ztops.getInputStream(ze);
            } catch (IOException ioe) {
                System.out.println(ioe);
            }
        } else {
            throw new FileNotFoundException("file : " + topsfile
                    + ".tops not found in zip");
        }
        return is;
    }

    private ZipFile initZip(String zipf) {
        ZipFile z = null;
        try {
            z = new ZipFile(this.pathToZip + zipf + ".zip");
        } catch (IOException ioe) {
            System.out.println(ioe);
        }
        return z;
    }

    public static void main(String[] args) {
        TopsFileManager tfm = new TopsFileManager();
        tfm.addPathMapping(args[0], args[1]);
        String chain = null;

        if (!args[3].equals("-")) {
            chain = args[3];
        }

//      String domain = null;
//        if (!args[4].equals("-")) {
//            domain = args[4];
//        }
        try {
            String[] names = tfm.getNames(args[0], args[2], chain);
            for (int i = 0; i < names.length; i++) {
                System.out.println("name " + i + " = " + names[i]);
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(tfm
                    .getStreamFromDir(args[0], names[0])));
            String line;
            try {
                while ((line = br.readLine()) != null)
                    System.out.print(line);
            } catch (IOException ioe) {
                System.out.println(ioe);
            }
        } catch (FileNotFoundException fnf) {
            System.out.println(fnf);
        }
    }
}
