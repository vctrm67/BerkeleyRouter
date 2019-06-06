import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import static org.junit.Assert.assertEquals;

/**
 * Created by hug, 4/9/2018. Basic tests for A* on the tiny graph.
 * This graph is so small you can draw it out by hand and visually inspect the results!
 */
public class TestRouterTiny {
    private static final String OSM_DB_PATH_TINY = "../library-sp18/data/tiny-clean.osm.xml";
    private static GraphDB graphTiny;
    private static boolean initialized = false;

    @Before
    public void setUp() throws Exception {
        if (initialized) {
            return;
        }
        graphTiny = new GraphDB(OSM_DB_PATH_TINY);
        initialized = true;
    }
/*
    @Test
    public void test22to66() {
        List<Long> actual = Router.shortestPath(graphTiny, 0.2, 38.2, 0.6, 38.6);
        List<Long> expected = new ArrayList<>();
        expected.add(22L);
        expected.add(46L);
        expected.add(66L);
        assertEquals("Best path from 22 to 66 is incorrect.", expected, actual);
    }

    @Test
    public void test22to11() {
        List<Long> actual = Router.shortestPath(graphTiny, 0.2, 38.2, 0.1, 38.1);
        List<Long> expected = new ArrayList<>();
        expected.add(22L);
        expected.add(11L);
        assertEquals(expected, actual);
    }


*/

    private static class test implements Comparable<test>{
        public double num;

        private test(double a) {
            num = a;
        }

        public int compareTo(test b) {
            if (num > b.num) {
                return 1;
            } else if (num < b.num) {
                return -1;
            } else {
                return 0;
            }
        }

    }

    @Test
    public void tester() {

        Queue<test> Test = new PriorityQueue<>();
        test a = new test(-5.0);
        Test.add(a);
        test b = new test(Double.POSITIVE_INFINITY);
        Test.add(b);
        test c = new test(5.0);
        Test.add(c);
        test d = new test(2.0);
        Test.add(d);
        test e = new test(10.0);
        Test.add(e);
        test f = new test(Double.POSITIVE_INFINITY);
        Test.add(f);

        d.num = 9.0;
        Test.add(new test(-10.0));
        System.out.println(Test.poll().num);
        System.out.println(Test.poll().num);
        System.out.println(Test.poll().num);
        System.out.println(Test.poll().num);
        System.out.println(Test.poll().num);

    }

    @Test
    public void test41to46() {
        List<Long> actual = Router.shortestPath(graphTiny, 0.4, 38.1, 0.4, 38.6);
        List<Long> expected = new ArrayList<>();
        expected.add(41L);
        expected.add(63L);
        expected.add(66L);
        expected.add(46L);
        assertEquals(expected, actual);


    }
/*
    @Test
    public void test66to55() {
        List<Long> actual = Router.shortestPath(graphTiny, 0.6, 38.6, 0.5, 38.5);
        List<Long> expected = new ArrayList<>();
        expected.add(66L);
        expected.add(63L);
        expected.add(55L);
        assertEquals(expected, actual);
    }
    */

}
