package tops.xml;

import java.io.File;
import java.io.FileNotFoundException;

import java.io.IOException;
import java.io.OutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;

import javax.xml.transform.dom.DOMSource;

import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import tops.dw.protein.Protein;

/**
 * XMLEncoders take tops.dw.protein.Protein objects and transform them into XML.
 * For example :
 * 
 * <pre>
 * XMLEncoder enc = new XMLEncoder(System.out);
 * enc.writeProtein(&quot;2bopA.tops&quot;);
 * </pre>
 * 
 * @author GMT
 * @version 1.0
 */

public class XMLEncoder {

    OutputStream out;

    /**
     * Constructor for the XMLEncoder object
     * 
     * @param out
     *            an OutputStream to write the XML to.
     */
    public XMLEncoder(OutputStream out) {
        this.out = out;
    }

    /**
     * Transforms a tops.dw.protein.Protein object into a DOM document.
     * 
     * @param input
     *            a Protein to be transformed
     * @return a DOM XML document
     */
    public Document transformProtein(Protein input) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;

        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException pce) {
            System.err.println(pce);
            return null;
        }

        Document document = builder.newDocument();

        Element protein = document.createElement("Protein");
        protein.setAttribute("ID", "2bop");
        document.appendChild(protein);

        Element chain = document.createElement("Chain");
        chain.setAttribute("ID", "A");
        protein.appendChild(chain);

        Element sheet = document.createElement("Sheet");
        sheet.setAttribute("ID", "1");
        sheet.setAttribute("Type", "Flat");
        chain.appendChild(sheet);

        int strands = 10;
        for (int i = 0; i < strands; i++) {
            Element strand = document.createElement("Strand");
            strand.setAttribute("chainOrder", String.valueOf(i));
            sheet.appendChild(strand);
        }

        return document;
    }

    /**
     * Tries to make a Protein object from the filename, which should be a TOPS
     * file. Writes out an XML document to the encoder's output stream by first
     * transforming it using transformProtein(Protein input).
     * 
     * @param filename
     *            the name of a TOPS file
     * @exception FileNotFoundException
     *                if the file is not found
     * @exception IOException
     *                if there is some other IO problem
     */
    public void writeProtein(String filename) throws FileNotFoundException,
            IOException {
        Protein p = new Protein(new File(filename));
        this.writeProtein(p);
    }

    /**
     * Writes out an XML document to the encoder's output stream by first
     * transforming it using transformProtein.
     * 
     * @param input
     *            Protein object to be transformed
     */
    public void writeProtein(Protein input) {
        Document document = this.transformProtein(input);
        if (document == null) {
            return;
        }

        try {
            TransformerFactory transformers_RobotsInDisguise = TransformerFactory
                    .newInstance();
            Transformer transformer = transformers_RobotsInDisguise
                    .newTransformer();
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(System.out);
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(source, result);
        } catch (TransformerConfigurationException tce) {
            System.out.println(tce);
        } catch (TransformerException te) {
            System.out.println(te);
        }
    }

    /**
     * The main program for the XMLEncoder class
     * 
     * @param args
     *            A filename to covert
     */
    public static void main(String[] args) {
        XMLEncoder ex = new XMLEncoder(System.out);
        try {
            ex.writeProtein(args[0]);
        } catch (FileNotFoundException fnf) {
            System.err.println(fnf);
        } catch (IOException ioe) {
            System.err.println(ioe);
        }

    }

}
