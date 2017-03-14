package com.teachastronomy.wikipedia;

import com.teachastronomy.Constants;
import com.teachastronomy.classifiers.CustomStopWordsHandler;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import weka.core.tokenizers.WordTokenizer;


import java.io.*;
import java.util.ArrayList;

/**
 * Created by sridh on 10/4/2016.
 */
public class WikiArticle {
    public ArrayList<WikiImage> images;
    String text;
    String id;
    String title;
    String timestamp;
    String category;

    public WikiArticle(String txt,String id,String ttl,String timestamp){
        this.text= txt;
        this.id = id;
        this.title = ttl;
        this.timestamp = timestamp;
        images = new ArrayList<>();
    }

    public String getText(){
        return this.text;
    }

    public  String getID(){
        return this.id;
    }

    public String getTitle(){
        return this.title;
    }

    public String getCategory(){
        return this.category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTimestamp(){
        return  this.timestamp;
    }

    public String getCleanText(){
        ArrayList<String> stopWords = new ArrayList<>();
        try {
            FileInputStream fis = new FileInputStream(new File(Constants.stopWordsFilePath));
            InputStreamReader is = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(is);

            String line = br.readLine();

            while(line != null) {
                stopWords.add(line);
                line = br.readLine();
            }
            br.close();
            String temp =this.text;

            temp = temp.toLowerCase();
            temp = temp.replaceAll("[^ a-zA-Z]", " ").replaceAll("\\s+", " ");
            WordTokenizer tokenizer = new WordTokenizer();
            tokenizer.tokenize(temp);
            CustomStopWordsHandler handler = new CustomStopWordsHandler();
            String cleantext="";
            while(tokenizer.hasMoreElements())
            {
                String token = tokenizer.nextElement();
                if(handler.isStopword(token))
                    continue;
                cleantext = cleantext + token+" ";
            }
            return cleantext;
        } catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public void addImage(WikiImage image){
        images.add(image);
    }
}
