import org.eclipse.jetty.util.ArrayUtil;

import java.util.*;

public class test {
    public static void main(String[] args) {
        System.out.println();

        String[] a = {"hi"};
        String[] b = {"bye"};

        List<String> list = new ArrayList<>();
        list.add("victor");
        list.add("victoe");
        //list.add("victa");

        Router.searchNode master = Router.createSearchTree(list);

        //list = Router.tester("vic", master);
        //for (String i : list) {
        //    System.out.println(i);
        //}

        System.out.println("hi" + "bye");
    }
}
