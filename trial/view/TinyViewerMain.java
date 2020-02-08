package view;

import tops.view.app.TinyViewer;

public class TinyViewerMain {
    
    public static void main(String[] args) {
        String name = "blah";
        String vertices = "NEeEeEeC";
        String edges = "1:2A2:3A3:4A";
        int width = 500;
        int height = 300;
        new TinyViewer(name, vertices, edges, width, height);
    }

}
