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

        get("/query", "application/json", (req, res) -> {
            JSONObject json = new JSONObject(req.body());
            if ("bm25".equals(json.getString("algo"))) {
                float k1 = json.getFloat("k1");
                float b = json.getFloat("b");
                searcher.setSimilarity(new BM25Similarity(0.5f, 0.45f));
            } else {
                searcher.setSimilarity(new LMDirichletSimilarity(json.getFloat("mu")));
            }
            return "Still no error";
//
//            return "algo: " + json.get("algorithm") + "\nk1: " + json.get("k1") + "\nb: " + json.get("b") + "\nquery: " + json.get("query")+ "\nsearcher: ";
        });
    }


    /**
     * Creates and sets up the index
     */
    private static void setUpIndex(String indexPath) {
        try {
            searcher = setupIndexSearcher(indexPath, "paragraph.lucene"); //Create IndexSearcher;
        } catch (Exception e) {
            e.printStackTrace();
        }
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
