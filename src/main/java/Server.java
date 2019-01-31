import org.json.JSONObject;

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

        get("/search", (req, res) -> {

            JSONObject json = new JSONObject(req.queryMap());

            if ("bm25".equals(json.getString("algo"))) {
                float k1 = json.getFloat("k1");
                float b = json.getFloat("b");
                indexController.setSimilarity(k1, b);
            } else {
                indexController.setSimilarity(json.getFloat("mu"));
            }

            ArrayList<String> passages = indexController.getPassages(json.getString("q"), 1);

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
