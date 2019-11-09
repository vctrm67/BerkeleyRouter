import edu.princeton.cs.algs4.Edge;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.*;

/**
 *  Parses OSM XML files using an XML SAX parser. Used to construct the graph of roads for
 *  pathfinding, under some constraints.
 */
public class GraphBuildingHandler extends DefaultHandler {
    /**
     * Only allow for non-service roads; this prevents going on pedestrian streets as much as
     * possible. Note that in Berkeley, many of the campus roads are tagged as motor vehicle
     * roads, but in practice we walk all over them with such impunity that we forget cars can
     * actually drive on them.
     */
    private static final Set<String> ALLOWED_HIGHWAY_TYPES = new HashSet<>(Arrays.asList
            ("motorway", "trunk", "primary", "secondary", "tertiary", "unclassified",
                    "residential", "living_street", "motorway_link", "trunk_link", "primary_link",
                    "secondary_link", "tertiary_link"));
    private String activeState = "";
    private final GraphDB g;
    private Map<String, String> node;
    private ArrayList<String> edgeList = new ArrayList<>();
    private boolean end = false;
    private boolean name = false;

    /**
     * Create a new GraphBuildingHandler.
     * @param g The graph to populate with the XML data.
     */
    public GraphBuildingHandler(GraphDB g) {
        this.g = g;
    }

    /**
     * Called at the beginning of an element. Typically, you will want to handle each element in
     * here, and you may want to track the parent element.
     * @param uri The Namespace URI, or the empty string if the element has no Namespace URI or
     *            if Namespace processing is not being performed.
     * @param localName The local name (without prefix), or the empty string if Namespace
     *                  processing is not being performed.
     * @param qName The qualified name (with prefix), or the empty string if qualified names are
     *              not available. This tells us which element we're looking at.
     * @param attributes The attributes attached to the element. If there are no attributes, it
     *                   shall be an empty Attributes object.
     * @throws SAXException Any SAX exception, possibly wrapping another exception.
     * @see Attributes
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException {
        if (qName.equals("node")) {
            activeState = "node";

            //The creation of a new node as the parser finds a new node.
            node = new HashMap<>();
            node.put("id", attributes.getValue("id"));
            node.put("lon", attributes.getValue("lon"));
            node.put("lat", attributes.getValue("lat"));
            g.addNode(node);


        } else if (qName.equals("way")) {
            //The creation of a new way, or street.
            activeState = "way";

        } else if (activeState.equals("way") && qName.equals("nd")) {
            //Creating a list of nodes that correspond to the same street, to be added later if
            //the street is one of the listed available streets for traversing.
            edgeList.add(attributes.getValue("ref"));

        } else if (activeState.equals("way") && qName.equals("tag")) {
            //Looking at the characteristics of a particular way.
            String k = attributes.getValue("k");
            String v = attributes.getValue("v");
            if (k.equals("highway")) {
                //Making sure the way, or highway, is on the list of allowed types.
                if (ALLOWED_HIGHWAY_TYPES.contains(v)) { end = true; }
            } else if (k.equals("name")) {
                //If there is a name for the way, add it as well.
                name = true;
                edgeList.add(v);
            }

        } else if (activeState.equals("node") && qName.equals("tag") && attributes.getValue("k")
                .equals("name")) {
            //If there is a name for the particular node, add it as well.
            g.removeNode(node.get("id"));
            node.put("name", attributes.getValue("v"));
            g.addNode(node);
        }

    }

    /**
     * Receive notification of the end of an element. You may want to take specific terminating
     * actions here, like finalizing vertices or edges found.
     * @param uri The Namespace URI, or the empty string if the element has no Namespace URI or
     *            if Namespace processing is not being performed.
     * @param localName The local name (without prefix), or the empty string if Namespace
     *                  processing is not being performed.
     * @param qName The qualified name (with prefix), or the empty string if qualified names are
     *              not available.
     * @throws SAXException  Any SAX exception, possibly wrapping another exception.
     */
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equals("way")) {
            //Only continue if the way is on the list of allowed ways.
            if (end) {
                //Depending on if there is a name for the way, add the way between sequential nodes according to its
                //name. If there is no name, label it as "unknown road".
                if (!name) {
                    for (int i = 0; i < (edgeList.size() - 1); i += 1) {
                        g.addEdge(edgeList.get(i), edgeList.get(i + 1), "unknown road");
                    }
                } else {
                    for (int i = 0; i < (edgeList.size() - 2); i += 1) {
                        g.addEdge(edgeList.get(i), edgeList.get(i + 1), edgeList.get(edgeList.size() - 1));
                    }
                }
            }
            edgeList = new ArrayList<>();
            end = false;
            name = false;
        }
    }

}
