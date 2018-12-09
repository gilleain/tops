package tops.dw.editor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * a class which represents a list of File objects
 * 
 * @author David Westhead
 * @version 1.00 6 May 1997
 */
public class FileList {

    private List<File> files;

    private int current;

    public FileList() {
        this.files = new ArrayList<>();
        this.current = -1;
    }

    public FileList(String[] files) {
        this();
        this.setFiles(files);
    }

    public FileList(String dir, String[] files) {
        this();
        this.setFiles(dir, files);
    }

    public synchronized void setFiles(String[] files) {

        if (files == null)
            return;

        this.files = new Vector<>();
        if (files.length > 0)
            this.current = 0;
        else
            this.current = -1;

        int i;
        for (i = 0; i < files.length; i++) {
            this.files.add(new File(files[i]));
        }

    }

    public synchronized void setFiles(String dir, String[] files) {

        if ((files == null) || (dir == null))
            return;

        String fsep = System.getProperty("file.separator");
        if (!dir.endsWith(fsep))
            dir = dir + fsep;

        this.files = new Vector<>();
        if (files.length > 0)
            this.current = 0;
        else
            this.current = -1;

        int i;
        for (i = 0; i < files.length; i++) {
            this.files.add(new File(dir + files[i]));
        }

    }

    public synchronized void setCurrent(int i) {
        if ((i >= 0) && (i < this.size()))
            this.current = i;
    }

    public synchronized int getCurrent() {
        return this.current;
    }

    public synchronized File getCurrentFile() {
        if (this.current >= 0)
            return this.files.get(this.current);
        else
            return null;
    }

    public synchronized File getNextFile() {
        int curr = this.current;
        curr++;
        this.setCurrent(curr);

        return this.getCurrentFile();

    }

    public synchronized File getPreviousFile() {

        int curr = this.current;
        curr--;
        this.setCurrent(curr);

        return this.getCurrentFile();

    }

    public synchronized int size() {
        return this.files.size();
    }

    public synchronized void append(File f) {
        if (f == null)
            return;
        this.files.add(f);
    }

    public synchronized void add(File f) {
        if (f == null)
            return;
        if (this.current == (this.files.size() - 1)) {
            this.files.add(f);
            this.current++;
        } else {
            int index = this.current + 1;
            this.files.add(index, f);
            this.current++;
        }
    }

}
