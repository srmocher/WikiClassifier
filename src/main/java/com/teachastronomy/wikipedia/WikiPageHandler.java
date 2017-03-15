package com.teachastronomy.wikipedia;


import com.teachastronomy.Constants;
import com.teachastronomy.Logger;
import com.teachastronomy.classifiers.*;
import com.teachastronomy.lucene.LuceneIndexer;

import com.teachastronomy.wikipedia.WikiArticle;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by root on 10/10/16.
 */
public class WikiPageHandler extends DefaultHandler {
    boolean page = false;
    boolean text = false;
    boolean title = false;
    boolean timestamp = false;
    boolean id = false;
    boolean category = false;
    int n_infoboxes=0;
    static String ttl = "", time = "", ID = "", cat = "";
    String articleText = "";
    int id_val=0;
    StringBuilder txt = new StringBuilder();
    static ArrayList<WikiArticle> wikiArticles = new ArrayList<>();

    String previousTitle= "";

    Pattern imagePattern = Pattern.compile(":.*.(png|.jpg|.svg|.gif|.tiff)");

    Pattern pattern = Pattern.compile("\\[\\[File:.*\\]\\]");

    LuceneIndexer indexer,titleIndexer;

    NaiveBayesClassifier classifier; //multinomial model naive bayes


    int n_ast, n_nast;
    BufferedWriter astroWriter, nonAstroWriter;
    ArrayList<String> articles;
   Logger excelLogger;
    int count;
    ArrayList<Double> ast_probs;
    ArrayList<Double> nast_probs;

    public WikiPageHandler() {
        try {
            indexer = new LuceneIndexer(Constants.MainIndexLocation, "astronomyIndex");
            titleIndexer = new LuceneIndexer(Constants.MainIndexLocation, "titleIndex");

            articles = new ArrayList<>();
            count=0;
            //classifier = new NBClassifier();
           //classifier.trainUsingWEKA();
            ArrayList<String> classes = new ArrayList<>();
            classes.add("Astronomy");
            classes.add("Non Astronomy");
            ast_probs = new ArrayList<>();
            nast_probs = new ArrayList<>();
//          //  classifier = new NaiveBayesBernoulli(classes);
//
          //  excelLogger = new Logger();
            classifier = new NaiveBayesClassifier(classes);
            classifier.train(TrainingDataHelper.getTrainingData());
            ArrayList<WikiArticle> articles = TrainingDataHelper.getTrainingData();
            for(WikiArticle article:articles)
            {
                System.out.println(article.getTitle()+"-"+classifier.classify(article));
            }

            n_ast = 0;
            n_nast = 0;
            count=0;

          //  reader = new LuceneReader(Constants.MainIndexLocation + "/astronomyIndex2");
//            astroWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Constants.astroLogFile)));
   //         nonAstroWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Constants.nonAstroLogFile)));

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }


    @Override
    public void startElement(String s, String s1, String qName, Attributes attributes) throws SAXException {
        if (qName.equals("page")) {
            //  System.out.println("Page");
            page = true;
        } else if (qName.equals("text")) {
            text = true;
            txt.setLength(0);
        } else if (qName.equals("title")) {
            title = true;
        } else if (qName.equals("timestamp")) {
            timestamp = true;
        } else if (qName.equals("id")) {
            if(ID==null||ID.equals(""))
                id = true;
        }


    }

    @Override
    public void characters(char[] chars, int start, int end) throws SAXException {

        if (title) {
            ttl = new String(chars, start, end);
            title = false;
        }
        if (id) {
            ID = new String(chars, start, end);
            ;
         //   System.out.println(ttl+"-"+ID);
            //  System.out.println("ID "+ID);
            id = false;
        }
        if (text) {
            txt.append(chars, start, end);
            //  System.out.println(ttl + " "+txt.length());


        }
        if (timestamp) {
            time = new String(chars, start, end);
            ;
            timestamp = false;
        }

    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equals("text")) {
            articleText = txt.toString();
            //   System.out.println(articleText);
            text = false;

//            Matcher matcher = pattern.matcher(articleText);
//            //  boolean match = matcher.find();
//            while (matcher.find()) {
//                String matchText = articleText.substring(matcher.start(), matcher.end());
//
//                Matcher imageMatches = imagePattern.matcher(matchText);
//                if (imageMatches.find()) {
//                    String imageName = matchText.substring(imageMatches.start() + 1, imageMatches.end());
//
//                }
//
//            }
        }
        if (qName.equals("page")) {

            count++;

            WikiArticle article = new WikiArticle(articleText, ID, ttl, time);



                //System.exit(0);

            //  Validate if page is article, and if article check for infoboxes
                int res = validateTitleAndText(ttl, articleText);
                try {
                    //classify page
                    String result = classifier.classify(article);
                    //get result probabilities
                    double[] probs = classifier.getProbabilities(article);

                    int decision=1;
                    if(result.equals("Astronomy"))
                        decision=1;
                    else decision=0;
                    //store result in excel

                    if (res == 0) {


                        if (result.equals("Astronomy")) {



                           System.out.println(ttl+" - "+probs[0]+","+probs[1]);
                            n_ast++;//Increment astronomy count
                               //excelLogger.writeToExcelSheet(ttl,probs[0],probs[1],decision);
                            Document d = new Document();
                            Field lFId = new Field("id", ID, Field.Store.YES, Field.Index.NOT_ANALYZED);
                            Field lFtitle = new Field("title", ttl, Field.Store.YES, Field.Index.ANALYZED);
                            Field lFtext = new Field("text", articleText, Field.Store.YES, Field.Index.NO);
                            Field cleanFtext = new Field("cleantext", article.getCleanText(), Field.Store.YES, Field.Index.ANALYZED);
                            Field lFtimestamp = new Field("timestamp", time, Field.Store.YES, Field.Index.NO);
                            d.add(lFId);
                            d.add(lFtitle);
                            d.add(lFtext);
                            d.add(cleanFtext);
                            d.add(lFtimestamp);
                            indexer.saveDocument(d);
                            Document titleDoc = new Document();
                            Field titlelFId = new Field("id", ID, Field.Store.YES, Field.Index.NOT_ANALYZED);
                            Field titlelFtitle = new Field("title", ttl, Field.Store.YES, Field.Index.ANALYZED);
                            titleDoc.add(titlelFId);
                            titleDoc.add(titlelFtitle);
                            titleIndexer.saveDocument(titleDoc);
                            if(ttl.equals("Astronomy"))
                            {
                                indexer.close();
                                titleIndexer.close();
                                System.exit(0);
                            }
                            ID=null;
                             //  astroWriter.write(ttl + ","+probs[0]+","+probs[1]+"\n");
                        } else {
                             //    nonAstroWriter.write(ttl +","+probs[0]+","+probs[1]+"\n");
                          //  excelLogger.writeToExcelSheet(ttl,probs[0],probs[1],0);
                            n_nast++;//Increment non astronomy count
                            ID=null;
                        }


                    } else if (res == 1) { // Infobox article
                       // ClassificationResult result = classifier.classify(article);
                        id_val++;
                    Document d = new Document();
                        Field lFId = new Field("id", ID, Field.Store.YES, Field.Index.NOT_ANALYZED);
                        Field lFtitle = new Field("title", ttl, Field.Store.YES, Field.Index.NO);
                        Field lFtext = new Field("text", articleText, Field.Store.YES, Field.Index.NO);
                        Field cleanFtext = new Field("cleantext", article.getCleanText(), Field.Store.YES, Field.Index.NO);
                        Field lFtimestamp = new Field("timestamp", time, Field.Store.YES, Field.Index.NO);
                        d.add(lFId);
                        d.add(lFtitle);
                        d.add(cleanFtext);
                        d.add(lFtext);
                        d.add(lFtimestamp);
                    indexer.saveDocument(d);
                        Document titleDoc = new Document();
                        Field titlelFId = new Field("id", ID, Field.Store.YES, Field.Index.NOT_ANALYZED);
                        Field titlelFtitle = new Field("title", ttl, Field.Store.YES, Field.Index.ANALYZED);
                        titleDoc.add(titlelFId);
                        titleDoc.add(titlelFtitle);
                        titleIndexer.saveDocument(titleDoc);
                        ID=null;
//                    astroWriter.write(ttl + "\n");
                        System.out.println(ttl+" - "+probs[0]+","+probs[1]+" Infobox");
                        //excelLogger.writeToExcelSheet(ttl,probs[0],probs[1],1);//write probabilities
                      //  n_ast++;//increment count
                    } else {
                //   nonAstroWriter.write(ttl+"\n");
                      // n_nast++;
                        ID=null;
                    }
                } catch (Exception e) {
                   System.out.println(e.getMessage());
                }

        }


    }



    @Override
    public void endDocument() throws SAXException {
        indexer.close();
        titleIndexer.close();
        System.out.println("Total astronomy - " + n_ast);
        System.out.println("Total non astronomy - " + n_nast);
        System.out.println("Total infoboxes "+n_infoboxes);
        try {
//    astroWriter.close();
         //  nonAstroWriter.close();
          // excelLogger.saveExcel();
         //   classifier.closeLogger();;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private String getImageUrl(String imageName) {
        try {
            org.jsoup.nodes.Document doc = Jsoup.connect("https://en.wikipedia.org/wiki/File:" + imageName).get();
            Element ele = doc.getElementById("file");
            Element aTag = ele.child(0);
            return aTag.attr("href");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private int  validateTitleAndText(String title,String text) {
        if(title.startsWith("File:")||title.startsWith("Wikipedia:")||title.startsWith("Template:")||title.startsWith("Category:")||title.startsWith("Portal:")||title.startsWith("MediaWiki:")||title.startsWith("Book:")||title.startsWith("Draft:")||title.startsWith("Help:")||title.startsWith("Module:"))
            return -1;

        if (text.toLowerCase().startsWith("#redirect")|| text.contains("{{disambiguation")
                || text.contains("{{hndis")) {
            return -1;
        } else if (text.contains("{{Infobox")) {
            if (text.contains("(\\[\\[Category:).*?(astronomers\\]\\])") ||
                    text.contains("(\\[\\[Category:).*?(Astronomers\\]\\])")||
                    text.contains("(\\[\\[Category:).*?(NASA\\]\\])")||
                    text.contains("(\\[\\[Category:).*?(Space program\\]\\])")) {
                return 1;
            } else if (text.indexOf("Infobox planet") > 0
                    || text.indexOf("Infobox astronomical survey") > 0
                    || text.indexOf("Infobox comet") > 0
                    || text.indexOf("Infobox constellation") > 0
                    || text.indexOf("Infobox nebula") > 0
                    || text.indexOf("Infobox galaxy") > 0
                    || text.indexOf("Infobox star") > 0
                    || text.indexOf("Infobox launch pad") > 0
                    || text.indexOf("Infobox rocket") > 0
                    || text.indexOf("Infobox rocket engine") > 0
                    || text.indexOf("Infobox rocket stage") > 0
                    || text.indexOf("Infobox space program") > 0
                    || text.indexOf("Infobox space shuttle") > 0
                    || text.indexOf("Infobox space station") > 0
                    || text.indexOf("Infobox space station module") > 0
                    || text.indexOf("Infobox spacecraft") > 0
                    || text.indexOf("Infobox spacecraft class") > 0
                    || text.indexOf("Infobox spacecraft instrument") > 0
                    || text.indexOf("Infobox spaceflight") > 0
                    || text.indexOf("Infobox year in spaceflight") > 0
                    || text.indexOf("Infobox cluster") > 0
                    || text.indexOf("Infobox crater data") > 0
                    || text.indexOf("Infobox feature on celestial object") > 0
                    || text.indexOf("Infobox galaxy cluster") > 0
                    || text.indexOf("Infobox GRB") > 0
                    || text.indexOf("Infobox globular cluster") > 0
                    || text.indexOf("Infobox lunar crater or mare") > 0
                    || text.indexOf("Infobox magnetosphere") > 0
                    || text.indexOf("Infobox meteor shower") > 0
                    || text.indexOf("Infobox meteorite") > 0
                    || text.indexOf("Infobox meteorite subdivision") > 0
                    || text.indexOf("Infobox navigation satellite system") > 0
                    || text.indexOf("Infobox quasar") > 0
                    || text.indexOf("Infobox solar cycle") > 0
                    || text.indexOf("Infobox solar eclipse") > 0
                    || text.indexOf("Infobox supercluster") > 0
                    || text.indexOf("Infobox supernova") > 0) {
                n_infoboxes++;

                return 1;
            }

        }
        return 0;
    }

}

