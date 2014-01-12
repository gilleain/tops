package tops.model;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TopsParser {

    private static Pattern edgeP = Pattern.compile("(\\d+):(\\d+)(\\w)"); // the
                                                                            // regex

    public static String getName(String s) {
        int firstSpace = s.indexOf(' ');
        if (firstSpace != -1) {
            return s.substring(0, firstSpace);
        }
        return "";
    }

    public static String getVertexSubstring(String s) {
        int firstSpace = s.indexOf(' ');
        if (firstSpace != -1) {
            int secondSpace = s.indexOf(' ', firstSpace + 1);
            if (secondSpace != -1) {
                return s.substring(firstSpace + 1, secondSpace);
            }
        }
        return "";
    }

    public static String getEdgeSubstring(String s) {
        int firstSpace = s.indexOf(' ');
        if (firstSpace != -1) {
            int secondSpace = s.indexOf(' ', firstSpace + 1);
            if (secondSpace != -1) {
                int thirdSpace = s.indexOf(' ', secondSpace + 1);
                if (thirdSpace != -1) {
                    return s.substring(secondSpace + 1, thirdSpace);
                } else {
                    return s.substring(secondSpace + 1);
                }
            }
        }
        return "";
    }

    public static String getDataSubstring(String s) {
        int firstSpace = s.indexOf(' ');
        if (firstSpace != -1) {
            int secondSpace = s.indexOf(' ', firstSpace + 1);
            if (secondSpace != -1) {
                int thirdSpace = s.indexOf(' ', secondSpace + 1);
                if (thirdSpace != -1) {
                    return s.substring(firstSpace + 1, thirdSpace);
                } else {
                    return s.substring(firstSpace + 1);
                }
            }
        }
        return "";
    }

    public static String getClassification(String s) {
        int firstSpace = s.indexOf(' ');
        if (firstSpace != -1) {
            int secondSpace = s.indexOf(' ', firstSpace + 1);
            if (secondSpace != -1) {
                int thirdSpace = s.indexOf(' ', secondSpace + 1);
                if (thirdSpace != -1) {
                    return s.substring(thirdSpace + 1);
                }
            }
        }
        return "";
    }

    public static TopsGraph parseString(String s) {
        if (s.length() == 0) {
            return new TopsGraph();
        } else {
            int firstSpace = s.indexOf(' ');

            if (firstSpace != -1) {
                String name = s.substring(0, firstSpace);
                int secondSpace = s.indexOf(' ', firstSpace + 1);
                Vertex[] vertices = null;
                if (secondSpace != -1) {
                    vertices = TopsParser.parseVertexString(s.substring(
                            firstSpace + 1, secondSpace));
                    Edge[] edges = TopsParser.parseEdgeString(s.substring(
                            secondSpace, s.length()));
                    return new TopsGraph(name, vertices, edges);
                } else {
                    vertices = TopsParser.parseVertexString(s.substring(
                            firstSpace + 1, s.length()));
                    return new TopsGraph(name, vertices, null);
                }
            } else {
                return new TopsGraph(s, null, null); // just a name
            }
        }
    }

    public static Vertex[] parseVertexString(String vertexString) {
        Vertex[] vertices = new Vertex[vertexString.length()];
        for (int i = 0; i < vertices.length; i++) {
            vertices[i] = new Vertex(vertexString.charAt(i));
        }
        return vertices;
    }

    public static Edge[] parseEdgeString(String edgeString) {
        Matcher m = TopsParser.edgeP.matcher(edgeString);
        ArrayList<Edge> edges = new ArrayList<Edge>();
        while (m.find()) {
            // start i at 1, because group(0) is the whole match!
            edges.add(new Edge(m.group(1), m.group(2), m.group(3)));
        }
        return (Edge[]) edges.toArray(new Edge[0]);
    }

    public static String toString(String name, Vertex[] vertices, Edge[] edges) {
        if (name == null && vertices == null) {
            return "empty pattern";
        }

        StringBuffer stringBuffer = new StringBuffer();

        if (name != null) {
            stringBuffer.append(name).append(' ');
        } else {
            stringBuffer.append("noname ");
        }

        if (vertices != null) {
            for (int i = 0; i < vertices.length; i++) {
                stringBuffer.append(vertices[i].getChar());
            }

            stringBuffer.append(" ");

            if (edges != null) {
                for (int j = 0; j < edges.length; j++) {
                    stringBuffer.append(edges[j].toString());
                }
            }
        }

        return stringBuffer.toString();
    }

    public static void main(String[] args) {
        TopsGraph graph = TopsParser.parseString(args[0]);
        System.out.println(graph);
    }

}
