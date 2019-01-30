import org.apache.lucene.analysis.en.EnglishAnalyzer;
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

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
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
     * @param userQuery - User query to query the index
     * @return
     */
    public String getTopPassage(String userQuery) {
        QueryBuilder queryBuilder = new QueryBuilder(new EnglishAnalyzer());
        try {
            TopDocs tops = searcher.search(queryBuilder.toQuery(userQuery), 100);
            ScoreDoc[] scoreDoc = tops.scoreDocs;
            HashSet<Object> seen = new HashSet<>(100);
            //TODO START FROM HERE

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "TBD";
    }


    @NotNull
    private IndexSearcher setupIndexSearcher(String indexPath, String typeIndex) throws IOException {
        Path path = FileSystems.getDefault().getPath(indexPath, typeIndex);
        Directory indexDir = FSDirectory.open(path);
        IndexReader reader = DirectoryReader.open(indexDir);
        return new IndexSearcher(reader);
    }

//    @NotNull
//    private String buildSectionQueryStr(Data.Page page, List<Data.Section> sectionPath) {
//        StringBuilder queryStr = new StringBuilder();
//        queryStr.append(page.getPageName());
//        for (Data.Section section : sectionPath) {
//            queryStr.append(" ").append(section.getHeading());
//        }
////        System.out.println("queryStr = " + queryStr);
//        return queryStr.toString();
//    }
}
