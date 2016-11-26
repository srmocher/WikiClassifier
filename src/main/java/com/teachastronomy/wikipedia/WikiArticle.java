package com.teachastronomy.wikipedia;

import com.teachastronomy.Constants;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;

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
            temp = temp.replaceAll("(<ref>).*?(<\\/ref>)", " ")
                    .replaceAll("[^a-z]", " ");

            for(String word:stopWords){
                temp =temp.replace(word,"");
            }
            return temp;
        } catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    public Document getFullDocument(){
        Document d = new Document();
        StringField id = new StringField("id",this.id, Field.Store.YES);
        StringField title = new StringField("title",this.title, Field.Store.YES);
        TextField text = new TextField("text",this.text, Field.Store.YES);
        StringField timestamp = new StringField("timestamp",this.timestamp, Field.Store.YES);
        d.add(id);
        d.add(title);
        d.add(text);
        d.add(timestamp);
        return  d;
    }

    public Document getTitleDocument(){
        Document d = new Document();
        StringField id = new StringField("id",this.id, Field.Store.YES);
        StringField title = new StringField("title",this.title, Field.Store.YES);
        d.add(id);
        d.add(title);
        return  d;
    }

    public void addImage(WikiImage image){
        images.add(image);
    }
}
