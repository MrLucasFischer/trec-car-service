import edu.unh.cs.treccar_v2.Data;
import edu.unh.cs.treccar_v2.read_data.DeserializeData;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Iterator;

public class TrecCarBuildLuceneIndex {

    private static final String LUCENE_INDEX = "/scratch/fmartins/paragraphIndex"; //TODO REMOVE


    public static void main(String[] args) throws IOException{
        final String paragraphsFile = args[1];
        final FileInputStream fileInputStream2 = new FileInputStream(new File(paragraphsFile));

        System.out.println("Creating paragraph index in " + LUCENE_INDEX);
        final IndexWriter indexWriter = setupIndexWriter(LUCENE_INDEX, "paragraph.lucene");
        final Iterator<Data.Paragraph> paragraphIterator = DeserializeData.iterParagraphs(fileInputStream2);

        for (int i = 1; paragraphIterator.hasNext(); i++) {
            final Document doc = paragraphToLuceneDoc(paragraphIterator.next());
            indexWriter.addDocument(doc);
            if (i % 10000 == 0) {
                System.out.print('.');
                indexWriter.commit();
            }
        }

        System.out.println("\n Done indexing.");

        indexWriter.commit();
        indexWriter.close();
    }

    @NotNull
    private static Document paragraphToLuceneDoc(Data.Paragraph p) {
        final Document doc = new Document();
        final String content = p.getTextOnly(); // <-- Todo Adapt this to your needs!
        doc.add(new TextField("text", content, Field.Store.YES));
        doc.add(new StringField("paragraphid", p.getParaId(), Field.Store.YES));  // don't tokenize this!
        return doc;
    }


    public static class PageToLuceneIterator implements Iterator<Document> {
        private static final int DEBUG_EVERY = 1000;
        private int counter = DEBUG_EVERY;
        private final Iterator<Data.Page> pageIterator;

        PageToLuceneIterator(Iterator<Data.Page> pageIterator){
            this.pageIterator = pageIterator;
        }

        @Override
        public boolean hasNext() {
            return this.pageIterator.hasNext();
        }

        @Override
        public Document next() {
            counter --;
            if(counter < 0) {
                System.out.print('.');
                counter = DEBUG_EVERY;
            }

            Data.Page p = this.pageIterator.next();
            return pageToLuceneDoc(p);
        }

        @Override
        public void remove() {
            this.pageIterator.remove();
        }
    }

    @NotNull
    private static Document pageToLuceneDoc(Data.Page p) {
        final Document doc = new Document();
        StringBuilder content = new StringBuilder();
        pageContent(p, content);                    // Todo Adapt this to your needs!

        doc.add(new TextField("text",  content.toString(), Field.Store.NO));  // dont store, just index
        doc.add(new StringField("pageid", p.getPageId(), Field.Store.YES));  // don't tokenize this!
        return doc;
    }


    private static void sectionContent(Data.Section section, StringBuilder content){
        content.append(section.getHeading()+'\n');
        for (Data.PageSkeleton skel: section.getChildren()) {
            if (skel instanceof Data.Section) sectionContent((Data.Section) skel, content);
            else if (skel instanceof Data.Para) paragraphContent((Data.Para) skel, content);
            else {
            }
        }
    }
    private static void paragraphContent(Data.Para paragraph, StringBuilder content){
        content.append(paragraph.getParagraph().getTextOnly()).append('\n');
    }
    private static void pageContent(Data.Page page, StringBuilder content){
        content.append(page.getPageName()).append('\n');

        for(Data.PageSkeleton skel: page.getSkeleton()){
            if(skel instanceof Data.Section) sectionContent((Data.Section) skel, content);
            else if(skel instanceof Data.Para) paragraphContent((Data.Para) skel, content);
            else {}    // ignore other
        }

    }
    @NotNull
    private static IndexWriter setupIndexWriter(String indexPath, String typeIndex) throws IOException {
        Path path = FileSystems.getDefault().getPath(indexPath, typeIndex);
        Directory indexDir = FSDirectory.open(path);
        IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
        return new IndexWriter(indexDir, config);
    }


}
