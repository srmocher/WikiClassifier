package com.teachastronomy.lucene;

import com.teachastronomy.Constants;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;

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
            StandardAnalyzer analyzer = new StandardAnalyzer();

            writer = new IndexWriter(new SimpleFSDirectory(new File(Constants.MainIndexLocation+"/"+dirName)), new StandardAnalyzer(Version.LUCENE_29), true, IndexWriter.MaxFieldLength.LIMITED);

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
            if(writer!=null)
                writer.close();
        }
        catch (Exception e){
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }



}
