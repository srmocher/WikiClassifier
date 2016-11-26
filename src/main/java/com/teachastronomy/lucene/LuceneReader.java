package com.teachastronomy.lucene;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.SimpleFSDirectory;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * Created by root on 10/13/16.
 */
public class LuceneReader {

    IndexReader reader;
    public LuceneReader(String dir){
        try{
            reader = DirectoryReader.open(new SimpleFSDirectory(Paths.get(dir)));
        }
        catch (IOException ioe){
            ioe.printStackTrace();
            System.out.println(ioe.getMessage());
        }

    }

    public int getCount(){
        return reader.maxDoc();
    }

    public Document getDocument(){
        Document d=null;
        try{
            d = reader.document(0);
        }
        catch (Exception e){

        }
        return d;
    }

    public boolean checkIfArticleExists(String articleId){
        if(reader!=null) {
            IndexSearcher searcher = new IndexSearcher(reader);
            try {
                TopDocs docs = searcher.search(new TermQuery(new Term("id", articleId)), 1);
                if (docs.totalHits == 0)
                    return false;
                else
                    return true;
            } catch (IOException ioe) {
                ioe.printStackTrace();
                System.out.println(ioe.getMessage());
            }
            return false;
        }
        return false;
    }
}
