package com.teachastronomy.lucene;

import com.teachastronomy.Constants;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;

import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;

import java.io.*;
import java.nio.file.Paths;

/**
 * Created by sridh on 10/4/2016.
 */
public class LuceneIndexer {

    //static String indexPath = "/tadata/ewap/legacy/WikiParser/Indexes";
  //  public CharArraySet stopWords;
    IndexWriter writer;
    public LuceneIndexer(String indexFolder, String dirName){
       // stopWords = new CharArraySet(20,true);
       // readStopWords();
        try{
            File dir = new File(indexFolder+"/"+dirName);
            if(!dir.exists()){
                dir.mkdir();
            }

            Directory d = FSDirectory.open(Paths.get(Constants.MainIndexLocation+"/"+dirName));
            writer = new IndexWriter(d, new IndexWriterConfig(new StandardAnalyzer()));

        }
        catch (IOException ioe){
            System.err.println(ioe.getMessage());
            ioe.printStackTrace();
        }
    }





    public void saveDocument(Document document){
        try{
                writer.addDocument(document);
             //   writer.commit();
         //   System.out.println("Saved "+documents.size()+" documents to Lucene");
        }
        catch (Exception e){
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public void close(){
        try{
            writer.forceMerge(1);
            if(writer!=null)
                writer.close();
        }
        catch (Exception e){
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }



}
