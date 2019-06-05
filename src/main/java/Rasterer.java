import java.util.HashMap;
import java.util.Map;

/**
 * This class provides all code necessary to take a query box and produce
 * a query result. The getMapRaster method must return a Map containing all
 * seven of the required fields, otherwise the front end code will probably
 * not draw the output correctly.
 */
public class Rasterer {

    private double ul_lon, ul_lat, lr_lon, lr_lat, qb_height, qb_width, tMap_LDPP, total_Lon, total_Lat;
    private boolean query_successful;

    public Rasterer() {
        tMap_LDPP = Math.abs(MapServer.ROOT_LRLON - MapServer.ROOT_ULLON) / MapServer.TILE_SIZE;
        total_Lon = Math.abs(MapServer.ROOT_LRLON - MapServer.ROOT_ULLON);
        total_Lat = Math.abs(MapServer.ROOT_LRLAT - MapServer.ROOT_ULLAT);
        query_successful = true;
    }

    /**
     * Takes a user query and finds the grid of images that best matches the query. These
     * images will be combined into one big image (rastered) by the front end. <br>
     *
     *     The grid of images must obey the following properties, where image in the
     *     grid is referred to as a "tile".
     *     <ul>
     *         <li>The tiles collected must cover the most longitudinal distance per pixel
     *         (LonDPP) possible, while still covering less than or equal to the amount of
     *         longitudinal distance per pixel in the query box for the user viewport size. </li>
     *         <li>Contains all tiles that intersect the query bounding box that fulfill the
     *         above condition.</li>
     *         <li>The tiles must be arranged in-order to reconstruct the full image.</li>
     *     </ul>
     *
     * @param params Map of the HTTP GET request's query parameters - the query box and
     *               the user viewport width and height.
     *
     * @return A map of results for the front end as specified: <br>
     * "render_grid"   : String[][], the files to display. <br>
     * "raster_ul_lon" : Number, the bounding upper left longitude of the rastered image. <br>
     * "raster_ul_lat" : Number, the bounding upper left latitude of the rastered image. <br>
     * "raster_lr_lon" : Number, the bounding lower right longitude of the rastered image. <br>
     * "raster_lr_lat" : Number, the bounding lower right latitude of the rastered image. <br>
     * "depth"         : Number, the depth of the nodes of the rastered image <br>
     * "query_success" : Boolean, whether the query was able to successfully complete; don't
     *                    forget to set this to true on success! <br>
     */
    public Map<String, Object> getMapRaster(Map<String, Double> params) {

        double user_LDPP = Math.abs(params.get("ullon") - params.get("lrlon")) / (params.get("w"));
        int depth = (int) Math.ceil(Math.log(tMap_LDPP / user_LDPP) / Math.log(2.0));
        if (depth > 7) {
            depth = 7;
        }

        if (params.get("ullon") < MapServer.ROOT_ULLON) {
            ul_lon = MapServer.ROOT_ULLON;
        } else {
            ul_lon = params.get("ullon");
        }
        if (params.get("ullat") > MapServer.ROOT_ULLAT) {
            ul_lat = MapServer.ROOT_ULLAT;
        } else {
            ul_lat = params.get("ullat");
        }
        if (params.get("lrlat") < MapServer.ROOT_LRLAT) {
            lr_lat = MapServer.ROOT_LRLAT;
        } else {
            lr_lat = params.get("lrlat");
        }
        if (params.get("lrlon") > MapServer.ROOT_LRLON) {
            lr_lon = MapServer.ROOT_LRLON;
        } else {
            lr_lon = params.get("lrlon");
        }
        qb_height = params.get("h");
        qb_width = params.get("w");

        double lonTile_Dist = total_Lon / Math.pow(2, depth);
        double latTile_Dist = total_Lat / Math.pow(2, depth);

        int y_lower = (int) Math.floor(Math.abs((MapServer.ROOT_ULLAT - ul_lat)) / latTile_Dist);
        int x_lower = (int) Math.floor(Math.abs((MapServer.ROOT_ULLON - ul_lon)) / lonTile_Dist);
        int y_upper, x_upper;
        if ((Math.abs((MapServer.ROOT_ULLAT - lr_lat)) % latTile_Dist) == 0) {
            y_upper = (int) Math.floor(Math.abs((MapServer.ROOT_ULLAT - lr_lat)) / latTile_Dist) - 1;
        } else {
            y_upper = (int) Math.floor(Math.abs((MapServer.ROOT_ULLAT - lr_lat)) / latTile_Dist);
        }
        if ((Math.abs((MapServer.ROOT_ULLON - lr_lon)) % lonTile_Dist) == 0) {
            x_upper = (int) Math.floor(Math.abs((MapServer.ROOT_ULLON - lr_lon)) / lonTile_Dist) - 1;
        } else {
            x_upper = (int) Math.floor(Math.abs((MapServer.ROOT_ULLON - lr_lon)) / lonTile_Dist);
        }

        String[][] tiles = new String[(y_upper - y_lower) + 1][(x_upper - x_lower) + 1];
        int x_index = x_lower;
        int y_index = y_lower;

        for (int i = 0; i <= (y_upper - y_lower); i += 1) {
            for (int j = 0; j <= (x_upper - x_lower); j += 1) {
                tiles[i][j] = "d" + depth + "_x" + x_index + "_y" + y_index + ".png";
                x_index += 1;
            }
            x_index = x_lower;
            y_index += 1;
        }

        double raster_ul_lon = MapServer.ROOT_ULLON + x_lower * lonTile_Dist;
        double raster_lr_lon = MapServer.ROOT_ULLON + (x_upper + 1) * lonTile_Dist;
        double raster_ul_lat = MapServer.ROOT_ULLAT - y_lower * latTile_Dist;
        double raster_lr_lat = MapServer.ROOT_ULLAT - (y_upper + 1) * latTile_Dist;

        query_successful = testValid();
        Map<String, Object> results = new HashMap<>();
        results.put("render_grid", tiles);
        results.put("raster_ul_lon", raster_ul_lon);
        results.put("raster_ul_lat", raster_ul_lat);
        results.put("raster_lr_lon", raster_lr_lon);
        results.put("raster_lr_lat", raster_lr_lat);
        results.put("depth", depth);
        results.put("query_success", query_successful);

        return results;
    }

    private boolean testValid() {
        if (ul_lon > lr_lon || ul_lat < lr_lat) {
            return false;
        } else if (lr_lat > MapServer.ROOT_ULLAT || ul_lat < MapServer.ROOT_LRLAT) {
            return false;
        } else if (ul_lon > MapServer.ROOT_LRLON || lr_lon < MapServer.ROOT_ULLON) {
            return false;
        }
        return true;
    }



}
