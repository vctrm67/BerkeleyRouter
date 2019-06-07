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
     * Return a List of longs representing the shortest path from the node
     * closest to a start location and the node closest to the destination
     * location.
     * @param g The graph to use.
     * @param stlon The longitude of the start location.
     * @param stlat The latitude of the start location.
     * @param destlon The longitude of the destination location.
     * @param destlat The latitude of the destination location.
     * @return A list of node id's in the order visited on the shortest path.
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
        public int compareTo(Node x) {
            if ((distance + circleDist) - (x.distance + x.circleDist) > 0) {
                return 1;
            } else if ((distance + circleDist) - (x.distance + x.circleDist) < 0) {
                return -1;
            } else
                return 0;
        }
    }

    public static List<Long> shortestPath(GraphDB g, double stlon, double stlat,
                                          double destlon, double destlat) {
        Queue<Node> fringe = new PriorityQueue<>();
        Map<Long, Node> key = new HashMap<>();
        long start = g.closest(stlon, stlat);
        long end = g.closest(destlon, destlat);
        Node place = new Node(start, null, 0, 0);
        fringe.add(place);
        key.put(start, place);

        for (long i : g.vertices()) {
            if (i != start) {
                place = new Node(i, null, Double.POSITIVE_INFINITY, g.distance(i, end));
                fringe.add(place);
                key.put(i, place);
            }
        }

        Node currentNode = fringe.poll();
        while (currentNode.iden != end) {
            for (long i : g.adjacent(currentNode.iden)) {
                place = key.get(i);
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

        return solution; // FIXME
    }

    /**
     * Create the list of directions corresponding to a route on the graph.
     * @param g The graph to use.
     * @param route The route to translate into directions. Each element
     *              corresponds to a node from the graph in the route.
     * @return A list of NavigationDirection objects corresponding to the input
     * route.
     */
    public static List<NavigationDirection> routeDirections(GraphDB g, List<Long> route) {
        List<NavigationDirection> gps = new ArrayList<>();
        double current_bearing;
        double previous_bearing = g.bearing(route.get(0), route.get(1));
        int direction = 0;
        boolean change = true;
        Set<String> currentStreets = new HashSet<>(g.getStreets(route.get(1)));
        currentStreets.retainAll(g.getStreets(route.get(0)));
        String currentWay = currentStreets.toArray(new String[0])[0];

        for (int i = 1; i < route.size(); i++) {
            long current = route.get(i);
            long previous = route.get(i - 1);
/*
            for (String k : g.getStreets(current)) {
                System.out.println(k);
                System.out.println(route.get(i));
            }
            for (String j : currentStreets) {
                System.out.println(j);
                System.out.println(route.get(i - 1));
            }
            System.out.println("");

 */
            boolean sameStreet = g.getStreets(current).contains(currentWay);
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
                currentStreets = new HashSet<>(g.getStreets(route.get(i)));
                currentStreets.retainAll(g.getStreets(route.get(i - 1)));
                currentWay = currentStreets.toArray(new String[0])[0];

                NavigationDirection newDirection = new NavigationDirection();
                newDirection.direction = direction;
                newDirection.distance = g.distance(current, previous);
                newDirection.way = currentWay;
                gps.add(newDirection);

                change = false;
            }

            previous_bearing = current_bearing;
        }

        System.out.println(gps.size());


        return gps; // FIXME
    }

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
}
