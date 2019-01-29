import edu.unh.cs.treccar_v2.Data;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.List;

import static spark.Spark.*;

public class Server {

    private static IndexSearcher searcher;

    public static void main(String[] args) throws IOException {

        System.setProperty("file.encoding", "UTF-8");

        if (args.length != 1)
            usage();

        port(8081);
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

        get("/query", "application/json", (req, res) -> {
//            Example req.body format
//            {
//                "algo" : "bm25",
//                "k1": 0.5,
//                "b": 0.45,
//                "query": "insert your query here"
//
//            }

//            {
//                "algo" : "lmd",
//                "mu": 100,
//                "query": "insert your query here"
//
//            }

            JSONObject json = new JSONObject(req.body());

            if ("bm25".equals(json.get("algo"))) {
                float k1 = json.getFloat("k1");
                float b = json.getFloat("b");
                searcher.setSimilarity(new BM25Similarity(k1, b));
            } else {
                searcher.setSimilarity(new LMDirichletSimilarity(json.getFloat("mu")));
            }

            return "algo: " + json.get("algorithm") + "\nk1: " + json.get("k1") + "\nb: " + json.get("b") + "\nquery: " + json.get("query");
        });
    }


    /**
     * Creates and sets up the index
     *
     * @throws IOException
     */
    private static void setUpIndex(String indexPath) throws IOException {
        searcher = setupIndexSearcher(indexPath, "paragraph.lucene"); //Create IndexSearcher;
    }

    @NotNull
    private static IndexSearcher setupIndexSearcher(String indexPath, String typeIndex) throws IOException {
        Path path = FileSystems.getDefault().getPath(indexPath, typeIndex);
        Directory indexDir = FSDirectory.open(path);
        IndexReader reader = DirectoryReader.open(indexDir);
        return new IndexSearcher(reader);
    }

    @NotNull
    private static String buildSectionQueryStr(Data.Page page, List<Data.Section> sectionPath) {
        StringBuilder queryStr = new StringBuilder();
        queryStr.append(page.getPageName());
        for (Data.Section section : sectionPath) {
            queryStr.append(" ").append(section.getHeading());
        }
//        System.out.println("queryStr = " + queryStr);
        return queryStr.toString();
    }
}
