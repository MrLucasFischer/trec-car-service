import org.apache.lucene.analysis.CharArraySet;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;

public class IndexController {

    private IndexSearcher searcher;

    /**
     * Set up IndexSearcher with the given index specified by "indexPath"
     *
     * @param indexPath - Path to Index to be searched
     */
    public void setUpIndexSearcher(String indexPath) {
        try {
            this.searcher = setupIndexSearcher(indexPath, "paragraph.lucene"); //Create IndexSearcher;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets the similarity to be used when searching the Index
     *
     * @param simParams - Variable number of parameters, if there's 2 parameters
     *                  it's assumed to be k1 and b for BM25, if it's only one parameter
     *                  it's assumed to be mu for LM Dirichlet
     */
    public void setSimilarity(Float... simParams) {
        if (simParams.length == 2) {
            this.searcher.setSimilarity(new BM25Similarity(simParams[0], simParams[1]));
        } else {
            this.searcher.setSimilarity(new LMDirichletSimilarity(simParams[0]));
        }
    }

    /**
     * Querys the Index with the given user query and retrieves only the top passage found for that query
     *
     * @param userQuery        - User query to query the index
     * @param numberOfPassages - Specifies the number of passages to find
     * @return A List with, at most, the specified number of passages found for the user query
     */
    public ArrayList<String> getPassages(String userQuery, int numberOfPassages) {
        try {

            List<String> lines = Files.readAllLines(Paths.get("../resources/english-stoplist.txt"), StandardCharsets.UTF_8);
            HashSet<String> stopwords = new HashSet<>(lines);
            CharArraySet stopWordsSet = new CharArraySet(stopwords, true);

            QueryBuilder queryBuilder = new QueryBuilder(new EnglishAnalyzer(stopWordsSet));

            TopDocs tops = searcher.search(queryBuilder.toQuery(userQuery), numberOfPassages);
            ScoreDoc[] scoreDoc = tops.scoreDocs;

            HashMap<String, String> passages = new HashMap<>();

            for (ScoreDoc score : scoreDoc) {

                final Document doc = searcher.doc(score.doc); // to access stored content
                final String paragraphid = doc.getField("paragraphid").stringValue();
                final String passage = doc.getField("text").stringValue();

                if (!passages.containsKey(paragraphid)) {
                    passages.put(paragraphid, passage);
                }
            }

            return new ArrayList<>(passages.values());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Opens the directory that contains the index and passes it to IndexSearcher
     *
     * @param indexPath - Path to the Index directory
     * @param typeIndex - The type of the index, in this example its fixed to "paragraphs.lucene"
     * @return An IndexSearcher Instance
     * @throws IOException in case the the path to index is incorrect
     */
    @NotNull
    private IndexSearcher setupIndexSearcher(String indexPath, String typeIndex) throws IOException {
        Path path = FileSystems.getDefault().getPath(indexPath, typeIndex);
        Directory indexDir = FSDirectory.open(path);
        IndexReader reader = DirectoryReader.open(indexDir);
        return new IndexSearcher(reader);
    }
}
