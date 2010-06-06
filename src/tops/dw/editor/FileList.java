package tops.dw.editor;

import java.io.*;
import java.util.*;

/**
 * a class which represents a list of File objects
 * 
 * @author David Westhead
 * @version 1.00 6 May 1997
 */
public class FileList {

    /* START instance variables */

    private Vector Files;

    private int Current;

    /* END instance variables */

    /* START constructors */

    public FileList() {
        this.Files = new Vector();
        this.Current = -1;
    }

    public FileList(String[] files) {
        this();
        this.setFiles(files);
    }

    public FileList(String dir, String[] files) {
        this();
        this.setFiles(dir, files);
    }

    /* END constructors */

    /* START get/set methods */

    public synchronized void setFiles(String[] files) {

        if (files == null)
            return;

        this.Files = new Vector();
        if (files.length > 0)
            this.Current = 0;
        else
            this.Current = -1;

        int i;
        for (i = 0; i < files.length; i++) {
            this.Files.addElement(new File(files[i]));
        }

    }

    public synchronized void setFiles(String dir, String[] files) {

        if ((files == null) || (dir == null))
            return;

        String fsep = System.getProperty("file.separator");
        if (!dir.endsWith(fsep))
            dir = dir + fsep;

        this.Files = new Vector();
        if (files.length > 0)
            this.Current = 0;
        else
            this.Current = -1;

        int i;
        for (i = 0; i < files.length; i++) {
            this.Files.addElement(new File(dir + files[i]));
        }

    }

    public synchronized void setCurrent(int i) {
        if ((i >= 0) && (i < this.size()))
            this.Current = i;
    }

    public synchronized int getCurrent() {
        return this.Current;
    }

    public synchronized File getCurrentFile() {
        if (this.Current >= 0)
            return (File) this.Files.elementAt(this.Current);
        else
            return null;
    }

    public synchronized File getNextFile() {
        int curr = this.Current;
        curr++;
        this.setCurrent(curr);

        return this.getCurrentFile();

    }

    public synchronized File getPreviousFile() {

        int curr = this.Current;
        curr--;
        this.setCurrent(curr);

        return this.getCurrentFile();

    }

    /* END get/set methods */

    /* START utils */

    public synchronized int size() {
        return this.Files.size();
    }

    public synchronized void append(File f) {
        if (f == null)
            return;
        this.Files.addElement(f);
    }

    public synchronized void add(File f) {
        if (f == null)
            return;
        if (this.Current == (this.Files.size() - 1)) {
            this.Files.addElement(f);
            this.Current++;
        } else {
            int index = this.Current + 1;
            this.Files.insertElementAt(f, index);
            this.Current++;
        }
    }

    /* END utils */

}
