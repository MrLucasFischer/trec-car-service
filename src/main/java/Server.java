import spark.QueryParamsMap;
import utils.FreemarkerEngine;

import java.util.ArrayList;

import static spark.Spark.*;

public class Server {

    private static IndexController indexController = new IndexController();

    public static void main(String[] args) {

        System.setProperty("file.encoding", "UTF-8");

        if (args.length != 1) {
            usage();
        }

        port(5901);
        staticFiles.externalLocation("src/resources");

        // Configure freemarker engine
        FreemarkerEngine engine = new FreemarkerEngine("src/resources/");

        setUpIndex(args[0]);
        setUpEndPoints(engine);
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
    private static void setUpEndPoints(FreemarkerEngine engine) {


        get("/", (req, res) -> engine.render(null, "index.ftl"));

        get("/search", (req, res) -> {
            QueryParamsMap queryMap = req.queryMap();

            if ("bm25".equals(queryMap.get("algo").value())) {
                float k1 = queryMap.get("k1").floatValue();
                float b = queryMap.get("b").floatValue();
                indexController.setSimilarity(k1, b);
            } else {
                indexController.setSimilarity(queryMap.get("mu").floatValue());
            }

            ArrayList<String> passages = indexController.getPassages(queryMap.get("q").value(), 1);

            return passages.size() != 0 ? passages.get(0) : "I'm sorry, I don't have an answer for that";
        });
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
