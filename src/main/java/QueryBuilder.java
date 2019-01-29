import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class QueryBuilder {

    private final EnglishAnalyzer analyzer;
    private List<String> tokens;

    public QueryBuilder(EnglishAnalyzer standardAnalyzer) {
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
