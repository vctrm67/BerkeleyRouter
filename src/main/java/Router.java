import org.eclipse.jetty.util.ArrayUtil;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class provides a shortestPath method for finding routes between two points
 * on the map. Start by using Dijkstra's, and if your code isn't fast enough for your
 * satisfaction (or the autograder), upgrade your implementation by switching it to A*.
 * Your code will probably not be fast enough to pass the autograder unless you use A*.
 * The difference between A* and Dijkstra's is only a couple of lines of code, and boils
 * down to the priority you use to order your vertices.
 */


public class Router {
    /**
     * The creation of a new Node class that is used for the A* search algorithm. All necessary information about
     * a particular node/vertex has been packaged into this Node class, so that the A* search does not have to
     * rely on many different lists and maps to keep track of shortest distances, representations, and children.
     */
    private static class Node implements Comparable<Node>{
        public long iden;
        public Node previous;
        public double distance;
        public double circleDist;

        private Node(long a, Node b, double c, double d) {
            iden = a;
            previous = b;
            distance = c;
            circleDist = d;
        }

        @Override
        //Implementing a comparable method in order for comparison in the Priority Queue, according to the
        //shortest computed distance travelled + circleDistance to the end.
        public int compareTo(Node x) {
            if ((distance + circleDist) - (x.distance + x.circleDist) > 0) {
                return 1;
            } else if ((distance + circleDist) - (x.distance + x.circleDist) < 0) {
                return -1;
            } else
                return 0;
        }
    }

    /**
     * The method that utilizes A* search in order to find the best possible route to a particular target.
     * @param g The graph to use.
     * @param stlon The longitude of the start location.
     * @param stlat The latitude of the start location.
     * @param destlon The longitude of the destination location.
     * @param destlat The latitude of the destination location.
     *
     * @variable fringe: The priority queue that sorts Nodes by minimum distance travelled + circleDistance. All
     * Nodes are added into the fringe from the beginning, and initialized with a traveled distance of infinity.
     * @variable key: A key to map Node identifiers with Nodes.
     *
     * @return A list of node id's in the order visited on the shortest path.
     */
    public static List<Long> shortestPath(GraphDB g, double stlon, double stlat,
                                          double destlon, double destlat) {
        Queue<Node> fringe = new PriorityQueue<>();
        Map<Long, Node> key = new HashMap<>();
        long start = g.closest(stlon, stlat);
        long end = g.closest(destlon, destlat);
        Node place = new Node(start, null, 0, 0);
        fringe.add(place);
        key.put(start, place);

        //Adding all Nodes into the fringe.
        for (long i : g.vertices()) {
            if (i != start) {
                place = new Node(i, null, Double.POSITIVE_INFINITY, g.distance(i, end));
                fringe.add(place);
                key.put(i, place);
            }
        }

        Node currentNode = fringe.poll();
        //Continue while the minimum Node from the PQ is not yet
        while (currentNode.iden != end) {
            //Getting the neighbors of the currentNode
            for (long i : g.adjacent(currentNode.iden)) {
                place = key.get(i);
                //Updating the current distance and previous Node if the new computed distance is
                //less than the current distance.
                if (currentNode.distance + g.distance(currentNode.iden, place.iden) < place.distance) {
                    fringe.remove(place);
                    place.distance = currentNode.distance + g.distance(currentNode.iden, place.iden);
                    place.previous = currentNode;
                    fringe.add(place);
                }
            }

            currentNode = fringe.poll();
        }

        List<Long> solution = new ArrayList<>();
        solution.add(currentNode.iden);
        while (currentNode.previous != null) {
            currentNode = currentNode.previous;
            solution.add(0, currentNode.iden);
        }

        return solution;
    }

    /**
     * Create the list of directions corresponding to a route on the graph.
     * @param g The graph to use.
     * @param route The route to translate into directions. Each element
     *              corresponds to a node from the graph in the route.
     *
     * @variable gps: The list of NavigationDirections to be shown.
     * @variable current_bearing: The bearing of the current node.
     * @variable previous_bearing: The bearing of the previous node.
     * @variable direction: The direction corresponding to which way to turn.
     * @variable change: A boolean corresponding to if there was a change in street between two vertices.
     *
     * @return A list of NavigationDirection objects corresponding to the input
     * route.
     */
    public static List<NavigationDirection> routeDirections(GraphDB g, List<Long> route) {
        List<NavigationDirection> gps = new ArrayList<>();
        double current_bearing;
        double previous_bearing = g.bearing(route.get(0), route.get(1));
        int direction = 0;
        boolean change = true;

        String currentWay = getWay(g, route.get(0), route.get(1));

        for (int i = 1; i < route.size(); i++) {
            long current = route.get(i);
            long previous = route.get(i - 1);

            String currentStreet = getWay(g, previous, current);
            boolean sameStreet = currentWay.equals(currentStreet);
            current_bearing = g.bearing(previous, current);
            double bearing_diff = current_bearing - previous_bearing;

            if (gps.isEmpty()) {
                direction = 0;
            } else if (sameStreet) {
                    gps.get(gps.size() - 1).distance += g.distance(current, previous);
            } else {
                direction = getDirections(bearing_diff);
                change = true;
            }

            if (change) {
                currentWay = getWay(g, previous, current);

                NavigationDirection newDirection = new NavigationDirection();
                newDirection.direction = direction;
                newDirection.distance = g.distance(current, previous);
                newDirection.way = currentWay;
                gps.add(newDirection);

                change = false;
            }

            previous_bearing = current_bearing;
        }

        return gps; // FIXME
    }

    private static String getWay(GraphDB g, long node1, long node2) {
        Map<Long, String> streets1 = g.getStreets(node1);
        List<Long> neighbors1 = new ArrayList<>(g.getNeighbors(node1));
        List<Long> neighbors2 = new ArrayList<>(g.getNeighbors(node2));

        for (int i = 0; i < neighbors1.size(); i++) {
            if (node2 == neighbors1.get(i)) {
                return streets1.get(neighbors1.get(i));
            }
        }
        return null;
    }

    //Getting the directions corresponding to the relative bearing of two sequential vertices.
    private static int getDirections(double bearing) {
        double absBearing = Math.abs(bearing);

        if (absBearing > 180) {
            absBearing = 360 - absBearing;
            bearing *= -1;
        }

        if (absBearing <= 15) {
            return NavigationDirection.STRAIGHT;
        }
        if (absBearing <= 30) {
            return bearing < 0 ? NavigationDirection.SLIGHT_LEFT : NavigationDirection.SLIGHT_RIGHT;
        }
        if (absBearing <= 100) {
            return bearing < 0 ? NavigationDirection.LEFT : NavigationDirection.RIGHT;
        }
        else {
            return bearing < 0 ? NavigationDirection.SHARP_LEFT : NavigationDirection.SHARP_RIGHT;
        }
    }


    /**
     * Class to represent a navigation direction, which consists of 3 attributes:
     * a direction to go, a way, and the distance to travel for.
     */
    public static class NavigationDirection {

        /** Integer constants representing directions. */
        public static final int START = 0;
        public static final int STRAIGHT = 1;
        public static final int SLIGHT_LEFT = 2;
        public static final int SLIGHT_RIGHT = 3;
        public static final int RIGHT = 4;
        public static final int LEFT = 5;
        public static final int SHARP_LEFT = 6;
        public static final int SHARP_RIGHT = 7;

        /** Number of directions supported. */
        public static final int NUM_DIRECTIONS = 8;

        /** A mapping of integer values to directions.*/
        public static final String[] DIRECTIONS = new String[NUM_DIRECTIONS];

        /** Default name for an unknown way. */
        public static final String UNKNOWN_ROAD = "unknown road";
        
        /** Static initializer. */
        static {
            DIRECTIONS[START] = "Start";
            DIRECTIONS[STRAIGHT] = "Go straight";
            DIRECTIONS[SLIGHT_LEFT] = "Slight left";
            DIRECTIONS[SLIGHT_RIGHT] = "Slight right";
            DIRECTIONS[LEFT] = "Turn left";
            DIRECTIONS[RIGHT] = "Turn right";
            DIRECTIONS[SHARP_LEFT] = "Sharp left";
            DIRECTIONS[SHARP_RIGHT] = "Sharp right";
        }

        /** The direction a given NavigationDirection represents.*/
        int direction;
        /** The name of the way I represent. */
        String way;
        /** The distance along this way I represent. */
        double distance;

        /**
         * Create a default, anonymous NavigationDirection.
         */
        public NavigationDirection() {
            this.direction = STRAIGHT;
            this.way = UNKNOWN_ROAD;
            this.distance = 0.0;
        }

        public String toString() {
            return String.format("%s on %s and continue for %.3f miles.",
                    DIRECTIONS[direction], way, distance);
        }

        /**
         * Takes the string representation of a navigation direction and converts it into
         * a Navigation Direction object.
         * @param dirAsString The string representation of the NavigationDirection.
         * @return A NavigationDirection object representing the input string.
         */
        public static NavigationDirection fromString(String dirAsString) {
            String regex = "([a-zA-Z\\s]+) on ([\\w\\s]*) and continue for ([0-9\\.]+) miles\\.";
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(dirAsString);
            NavigationDirection nd = new NavigationDirection();
            if (m.matches()) {
                String direction = m.group(1);
                if (direction.equals("Start")) {
                    nd.direction = NavigationDirection.START;
                } else if (direction.equals("Go straight")) {
                    nd.direction = NavigationDirection.STRAIGHT;
                } else if (direction.equals("Slight left")) {
                    nd.direction = NavigationDirection.SLIGHT_LEFT;
                } else if (direction.equals("Slight right")) {
                    nd.direction = NavigationDirection.SLIGHT_RIGHT;
                } else if (direction.equals("Turn right")) {
                    nd.direction = NavigationDirection.RIGHT;
                } else if (direction.equals("Turn left")) {
                    nd.direction = NavigationDirection.LEFT;
                } else if (direction.equals("Sharp left")) {
                    nd.direction = NavigationDirection.SHARP_LEFT;
                } else if (direction.equals("Sharp right")) {
                    nd.direction = NavigationDirection.SHARP_RIGHT;
                } else {
                    return null;
                }

                nd.way = m.group(2);
                try {
                    nd.distance = Double.parseDouble(m.group(3));
                } catch (NumberFormatException e) {
                    return null;
                }
                return nd;
            } else {
                // not a valid nd
                return null;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof NavigationDirection) {
                return direction == ((NavigationDirection) o).direction
                    && way.equals(((NavigationDirection) o).way)
                    && distance == ((NavigationDirection) o).distance;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(direction, way, distance);
        }
    }

    /**
     * The creation of a searchNode class in order to implement AutoComplete features. The searchNode is used
     * in the creation of a trie data structure for search.
     *
     * @variable value: The letter of the searchNode.
     * @variable children: The child searchNodes, contained in a mapping of their values to their searchNodes.
     */
    public static class searchNode {
        public char value;
        public Map<Character, searchNode> children;

        public searchNode(char a) {
            value = a;
            children = new TreeMap<>();
        }
    }

    public static searchNode createSearchTree(List<String> names) {
        searchNode master = new searchNode('a');

        for (String i : names) {
            addNode(i, master);
        }

        return master;
    }

    public static void addNode(String query, searchNode newNode) {
        char first = query.charAt(0);

        if (!newNode.children.containsKey(first)) {
            newNode.children.put(first, new searchNode(first));
            if (query.length() > 1) {
                addNode(query.substring(1), newNode.children.get(first));
            }
        } else {
            if (query.length() > 1) {
                addNode(query.substring(1), newNode.children.get(first));
            } else {
                newNode.children.put(first, new searchNode(first));
            }
        }
    }

    //Traversing all searchNodes in a trie, given a particular starting searchNode, and returning all
    //searchNodes below it in alphabetical order.
    public static String[] traverseNodes(searchNode newNode) {
        if (newNode.children.isEmpty()) {
            String[] value = {Character.toString(newNode.value)};
            return value;
        } else {
            String[] modified = {};
            for (char i : newNode.children.keySet()) {
                String[] kids = traverseNodes(newNode.children.get(i));
                for (String j : kids) {
                    String[] newWord = {Character.toString(newNode.value) + j};
                    modified = ArrayUtil.add(modified, newWord);
                }
            }

            return modified;
        }
    }

}
