import edu.unh.cs.treccar_v2.Data;
import edu.unh.cs.treccar_v2.read_data.DeserializeData;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class TrecCarQueryLuceneIndex {

    private static final String INDEX_PATH = "/scratch/fmartins/paragraphIndex";

    static class MyQueryBuilder {

        private final EnglishAnalyzer analyzer;
        private List<String> tokens;

        public MyQueryBuilder(EnglishAnalyzer standardAnalyzer) {
            analyzer = standardAnalyzer;
            tokens = new ArrayList<>(128);
        }

        public BooleanQuery toQuery(String queryStr) throws IOException {

            TokenStream tokenStream = analyzer.tokenStream("text", new StringReader(queryStr));
            tokenStream.reset();
            tokens.clear();
            while (tokenStream.incrementToken()) {
                final String token = tokenStream.getAttribute(CharTermAttribute.class).toString();
                tokens.add(token);
            }
            tokenStream.end();
            tokenStream.close();
            BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();


            BooleanQuery.Builder booleanQuery1 = new BooleanQuery.Builder();
            for (String token : tokens) {
                booleanQuery1.add(new TermQuery(new Term("text", token)), BooleanClause.Occur.SHOULD);
            }
            BoostQuery unigramQuery = new BoostQuery(booleanQuery1.build(), 0.85f);
            booleanQuery.add(unigramQuery, BooleanClause.Occur.SHOULD);

            // oW1
            BooleanQuery.Builder booleanQuery2 = new BooleanQuery.Builder();
            for (int i = 0; i < tokens.size() - 1; i++) {
                SpanTermQuery t1 = new SpanTermQuery(new Term("text", tokens.get(i)));
                SpanTermQuery t2 = new SpanTermQuery(new Term("text", tokens.get(i + 1)));
                booleanQuery2.add(new SpanNearQuery(new SpanQuery[]{t1, t2}, 1, true), BooleanClause.Occur.SHOULD);
            }
            BoostQuery bigramQuery = new BoostQuery(booleanQuery2.build(), 0.10f);
            booleanQuery.add(bigramQuery, BooleanClause.Occur.SHOULD);

            // uW8
            BooleanQuery.Builder booleanQuery3 = new BooleanQuery.Builder();
            for (int i = 0; i < tokens.size() - 1; i++) {
                SpanTermQuery t1 = new SpanTermQuery(new Term("text", tokens.get(i)));
                SpanTermQuery t2 = new SpanTermQuery(new Term("text", tokens.get(i + 1)));
                booleanQuery3.add(new SpanNearQuery(new SpanQuery[]{t1, t2}, 8, false), BooleanClause.Occur.SHOULD);
            }
            BoostQuery unorderedQuery = new BoostQuery(booleanQuery3.build(), 0.05f);
            booleanQuery.add(unorderedQuery, BooleanClause.Occur.SHOULD);

            return booleanQuery.build();
        }
    }

    public static void main(String[] args) throws IOException{

        IndexSearcher searcher = setupIndexSearcher(INDEX_PATH, "paragraph.lucene"); //Create IndexSearcher


        if ("bm25".equals(args[3])) {

            float k1 = Float.parseFloat(args[4]);
            float b = Float.parseFloat(args[5]);
            searcher.setSimilarity(new BM25Similarity(k1, b));

        } else if ("lmd".equals(args[3])) {

            float mu = Float.parseFloat(args[4]);
            searcher.setSimilarity(new LMDirichletSimilarity(mu));

        }

        final MyQueryBuilder queryBuilder = new MyQueryBuilder(new EnglishAnalyzer());

        final String topicsFile = args[1];    //Get test.benchmarkY1test.cbor.outlines file
        final FileInputStream fileInputStream3 = new FileInputStream(new File(topicsFile));

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
