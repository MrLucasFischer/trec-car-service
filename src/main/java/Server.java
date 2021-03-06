import com.google.gson.Gson;
import spark.QueryParamsMap;

import java.util.ArrayList;

import static spark.Spark.*;

public class Server {

    private static IndexController indexController = new IndexController();
    private static Gson gson = new Gson();

    public static void main(String[] args) {

        System.setProperty("file.encoding", "UTF-8");

        if (args.length != 1) {
            usage();
        }

        port(5901);

        CorsFilter.apply();
        setUpIndex(args[0]);
        setUpEndPoints();
    }

    /**
     * Error message if not command line parameters not passed
     */
    private static void usage() {
        System.out.println("Command line parameters: indexPath");
        System.exit(-1);
    }

    /**
     * Sets up the endpoints for the server
     */
    private static void setUpEndPoints() {

        get("/", (req, res) -> "hello");

        get("/search", (req, res) -> {
            res.type("application/json");
            QueryParamsMap queryMap = req.queryMap();

            if ("bm25".equals(queryMap.get("algo").value())) {
                float k1 = queryMap.get("k1").floatValue();
                float b = queryMap.get("b").floatValue();
                indexController.setSimilarity(k1, b);
            } else {
                indexController.setSimilarity(queryMap.get("mu").floatValue());
            }

            ArrayList<String> passages = indexController.getPassages(queryMap.get("q").value(), 100);

            return passages.size() != 0 ? passages : "I'm sorry, I don't have an answer for that";
        }, gson::toJson);
    }

    /**
     * Calls IndexController to set up the IndexSearch for this session searching on the
     * index specified by "indexPath"
     *
     * @param indexPath - Path to Index to be searched
     */
    private static void setUpIndex(String indexPath) {
        indexController.setUpIndexSearcher(indexPath);
    }
}
