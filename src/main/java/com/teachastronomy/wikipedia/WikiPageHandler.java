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
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
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

    NaiveBayesClassifier []classifier; //multinomial model naive bayes


    int n_ast, n_nast;
    BufferedWriter astroWriter[][][], nonAstroWriter[][][];
    HashSet<String> articles[][];
   Logger excelLogger;
    int count;
    ArrayList<Double> ast_probs;
    ArrayList<Double> nast_probs;

    //creating a temporary collection for storing articles generated by SVM so as to index them
    HashSet<String> astroArticles;
    String[] categories;

    public WikiPageHandler() {
        try {
           // indexer = new LuceneIndexer(Constants.MainIndexLocation, "astronomyIndex");
          //  titleIndexer = new LuceneIndexer(Constants.MainIndexLocation, "titleIndex");
            categories = new String[]{"Astronomy","Computer Science","Dance","Psychology"};
            count=0;

            ArrayList<String> classes = new ArrayList<>();
            classes.add("R");
            classes.add("NR");
            ast_probs = new ArrayList<>();
            nast_probs = new ArrayList<>();

            classifier = new NaiveBayesClassifier[categories.length];
            for(int i=0;i<categories.length;i++) {
                classifier[i] = new NaiveBayesClassifier(classes);
                classifier[i].train(TrainingDataHelper.getTrainingData(categories[i]));
            }
          //  ArrayList<WikiArticle> articles = TrainingDataHelper.getTrainingData();

            selectedArticles();
            n_ast = 0;
            n_nast = 0;
            count=0;
            astroWriter = new BufferedWriter[categories.length][27][10];
            nonAstroWriter = new BufferedWriter[categories.length][27][10];
            for(int j=0;j<categories.length;j++) {
                for(int k=0;k<27;k++) {
                    for (int i = 0; i < 10; i++) {
                        astroWriter[j][k][i] = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("/home/sridhar/results/Dump-Results/" + categories[j] + "/"+(k+1)+"/" + (i + 1) + "Y.txt")));
                        nonAstroWriter[j][k][i] = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("/home/sridhar/results/Dump-Results/" + categories[j] + "/"+(k+1)+"/" + (i + 1) + "N.txt")));
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void selectedArticles()
    {
        articles = new HashSet[27][10];
        for(int j=0;j<27;j++)
        for(int i=0;i<10;i++)
            articles[j][i] = new HashSet<>();

        try
        {
            for(int j=0;j<27;j++) {
                for (int i = 0; i < 10; i++) {
                    BufferedReader br = new BufferedReader(new FileReader("/home/sridhar/results/Dump-Subsets/"+(j+1)+"/" + (i + 1) + ".txt"));
                    String s;
                    while ((s = br.readLine()) != null) {
                        articles[j][i].add(s);
                    }
                    br.close();
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
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


            for(int j=0;j<categories.length;j++) {
                for(int k=0;k<27;k++)
                for (int i = 0; i < 10; i++) {
                    if (articles[k][i].contains(ttl)) {
                        //System.exit(0);

                        //  Validate if page is article, and if article check for infoboxes
                        int res = validateTitleAndText(ttl, articleText);
                        try {
                            //classify page
                            String result = classifier[j].classify(article);
                            //get result probabilities
                            double[] probs = classifier[j].getProbabilities(article);

                            //store result in excel

                            if (res == 0) {


                                if (result.equals("R")) {


                                    System.out.println(ttl + "," + probs[0] + "," + probs[1]);
                                    //   System.out.println(ttl+" - "+probs[0]+","+probs[1]);
                                    n_ast++;//Increment astronomy count

                                    ID = null;
                                    astroWriter[j][k][i].write(ttl + "\n");
                                } else {

                                    n_nast++;//Increment non astronomy count
                                    ID = null;
                                    nonAstroWriter[j][k][i].write(ttl + "\n");
                                }


                            } else if (res == 1) { // Infobox article
                                // ClassificationResult result = classifier.classify(article);

                                ID = null;

                            } else {
                                nonAstroWriter[j][k][i].write(ttl + "\n");
                                // n_nast++;
                                ID = null;
                            }
                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                        }
                    }

                }
            }
        }


    }



    @Override
    public void endDocument() throws SAXException {
       // indexer.close();
      //  titleIndexer.close();
        System.out.println("Total astronomy - " + n_ast);
        System.out.println("Total non astronomy - " + n_nast);
        System.out.println("Total infoboxes "+n_infoboxes);
        try {
            for(int j=0;j<categories.length;j++){
            for(int k=0;k<27;k++) {
                for (int i = 0; i < 10; i++) {
                    astroWriter[j][k][i].flush();
                    astroWriter[j][k][i].close();
                    nonAstroWriter[j][k][i].flush();
                    nonAstroWriter[j][k][i].close();
                }
            }
            }
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

