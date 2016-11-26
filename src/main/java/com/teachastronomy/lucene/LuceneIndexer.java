package com.teachastronomy.lucene;

import com.teachastronomy.Constants;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.SimpleFSDirectory;

import java.io.*;
import java.nio.file.Paths;

/**
 * Created by sridh on 10/4/2016.
 */
public class LuceneIndexer {

    //static String indexPath = "/tadata/ewap/legacy/WikiParser/Indexes";
    public CharArraySet stopWords;
    IndexWriter writer;
    public LuceneIndexer(String indexFolder, String dirName){
        stopWords = new CharArraySet(20,true);
       // readStopWords();
        try{
            File dir = new File(indexFolder+"/"+dirName);
            if(!dir.exists()){
                dir.mkdir();
            }
            StandardAnalyzer analyzer = new StandardAnalyzer();
            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            config.setRAMBufferSizeMB(128);
            writer = new IndexWriter(new SimpleFSDirectory(Paths.get(indexFolder+"/"+dirName)),config);

        }
        catch (IOException ioe){
            System.err.println(ioe.getMessage());
            ioe.printStackTrace();
        }
    }

    public void readStopWords(){
        try{
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(Constants.wikipediaStopWordsFile)));
            String s="";
            while((s=br.readLine())!=null){
                stopWords.add(s);
            }
            br.close();

            BufferedReader sReader = new BufferedReader(new InputStreamReader(new FileInputStream(Constants.stopWordsFilePath)));

            while((s=sReader.readLine())!=null){
                stopWords.add(s);
            }
            br.close();
            sReader.close();
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
