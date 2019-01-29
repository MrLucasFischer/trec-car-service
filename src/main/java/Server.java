import edu.unh.cs.treccar_v2.Data;
import edu.unh.cs.treccar_v2.read_data.DeserializeData;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;

import static spark.Spark.*;

public class Server {

    private static IndexSearcher searcher;

    public static void main(String[] args) {

        System.setProperty("file.encoding", "UTF-8");

        if (args.length != 2) {
            usage();
        }

        port(5901);
        setUpIndex(args[1]);
        setUpEndPoints(args[2]);
    }

    /**
     * Error message if not command line parameters not passed
     */
    private static void usage() {
        System.out.println("Command line parameters: paragraphsCBOR indexPath");
        System.exit(-1);
    }

    /**
     * Sets up the endpoints for the server
     */
    private static void setUpEndPoints(String cborFilePath) {

        get("/query", "application/json", (req, res) -> {

            JSONObject json = new JSONObject(req.body());

            if ("bm25".equals(json.getString("algo"))) {
                float k1 = json.getFloat("k1");
                float b = json.getFloat("b");
                searcher.setSimilarity(new BM25Similarity(0.5f, 0.45f));
            } else {
                searcher.setSimilarity(new LMDirichletSimilarity(json.getFloat("mu")));
            }

            getTopPassage(cborFilePath);


            return "algo: " + json.getString("algo") + "\nk1: " + json.getFloat("k1") + "\nb: " + json.getFloat("b") + "\nquery: " + json.getString("query") + "\nsearcher: " + searcher.toString();
        });
    }

    private static String getTopPassage(String cborFilePath) {
        final QueryBuilder queryBuilder = new QueryBuilder(new EnglishAnalyzer());
        try {
            final FileInputStream fileInputStream3 = new FileInputStream(new File(cborFilePath));
            //TODO START FROM HERE
            for (Data.Page page : DeserializeData.iterableAnnotations(fileInputStream3)) {

                for (List<Data.Section> sectionPath : page.flatSectionPaths()) {
                    final String queryId = Data.sectionPathId(page.getPageId(), sectionPath); //Get QueryID

//                    sectionPath.forEach(section -> System.out.println(section.getHeading()));

                    String queryStr = buildSectionQueryStr(page, sectionPath);  //Get queryString to search
//                    System.out.println(queryBuilder.toQuery(queryStr));
                    TopDocs tops = searcher.search(queryBuilder.toQuery(queryStr), 100);//Get 100 docs for the provided query

                    ScoreDoc[] scoreDoc = tops.scoreDocs;
                    HashSet<Object> seen = new HashSet<>(100);

                    for (int i = 0; i < scoreDoc.length; i++) {

                        ScoreDoc score = scoreDoc[i];
                        final Document doc = searcher.doc(score.doc); // to access stored content
                        final String paragraphid = doc.getField("paragraphid").stringValue();
                        final float searchScore = score.score;
                        final int searchRank = i + 1;

                        if (!seen.contains(paragraphid)) {
                            System.out.println(queryId + " Q0 " + paragraphid + " " + searchRank + " " + searchScore + " Lucene-BM25");
                            seen.add(paragraphid);
                        }
                    }

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
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
