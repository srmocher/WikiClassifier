package com.teachastronomy.wikipedia;


import com.teachastronomy.Constants;
import com.teachastronomy.Logger;
import com.teachastronomy.classifiers.*;
import com.teachastronomy.lucene.LuceneIndexer;
import com.teachastronomy.lucene.LuceneReader;
import com.teachastronomy.wikipedia.WikiArticle;
import javafx.scene.chart.ScatterChart;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.Styler;
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
    StringBuilder txt = new StringBuilder();
    static ArrayList<WikiArticle> wikiArticles = new ArrayList<>();



    Pattern imagePattern = Pattern.compile(":.*.(png|.jpg|.svg|.gif|.tiff)");

    Pattern pattern = Pattern.compile("\\[\\[File:.*\\]\\]");

    LuceneIndexer indexer;
   // NBMultinomialTextClassifier classifier;
    //LogisticRegressionClassifier classifier;
    LuceneReader reader;
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
            classifier = new NaiveBayesClassifier(classes);
            classifier.train(TrainingDataHelper.getTrainingData());
            String filename = "Project Mercury";
            String text = new String(Files.readAllBytes(Paths.get("/home/sridhar/Desktop/TestData/"+filename)));
            WikiArticle art = new WikiArticle(text,null,filename,null);
            classifier.classify(art);
          //  classifier.trainUsingWEKA();
            indexer = new LuceneIndexer(Constants.MainIndexLocation, "astronomyIndex2");
            n_ast = 0;
            n_nast = 0;
            count=0;
            excelLogger = new Logger();
            reader = new LuceneReader(Constants.MainIndexLocation + "/astronomyIndex2");
            astroWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Constants.astroLogFile)));
            nonAstroWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Constants.nonAstroLogFile)));

        } catch (Exception e) {
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
//
//            if(astroDocTitles.contains(ttl)){
//                return;
//            }
//            else if(nonAstroDocTitles.contains(ttl)){
//                return;
//            }
//            HashMap<String, ArrayList<Double>> condProb = new HashMap<String, ArrayList<Double>>();
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
                            excelLogger.writeToExcelSheet(ttl,probs[0],probs[1],decision);
//                        Document d = new Document();
//                        d.add(new StringField("id", ID, Field.Store.YES));
//                        d.add(new StringField("title", ttl, Field.Store.YES));
//                        d.add(new TextField("text", articleText, Field.Store.YES));
//                        d.add(new StringField("timestamp", time, Field.Store.YES));

                            //     indexer.saveDocument(d);
                            //     astroWriter.write(ttl + "\n");
                        } else {
                                 nonAstroWriter.write(ttl +","+probs[0]+","+probs[1]+"\n");
                            n_nast++;//Increment non astronomy count
                        }


                    } else if (res == 1) { // Infobox article
                       // ClassificationResult result = classifier.classify(article);
//                    Document d = new Document();
//                    d.add(new StringField("id", ID, Field.Store.YES));
//                    d.add(new StringField("title", ttl, Field.Store.YES));
//                    d.add(new TextField("text", articleText, Field.Store.YES));
//                    d.add(new StringField("timestamp", time, Field.Store.YES));
//                    indexer.saveDocument(d);
//                    astroWriter.write(ttl + "\n");
                        System.out.println(ttl+" - "+probs[0]+","+probs[1]);
                        excelLogger.writeToExcelSheet(ttl,probs[0],probs[1],1);//write probabilities
                        n_ast++;//increment count
                    } else {
//                    nonAstroWriter.write(ttl+"\n");
                        n_nast++;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

        }


    }

    private void createScatterPlot() {
        // Create Chart
        XYChart chart = new XYChartBuilder().width(800).height(600).build();

        // Customize Chart
        chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Scatter);
        chart.getStyler().setChartTitleVisible(false);
        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideSW);
        chart.getStyler().setMarkerSize(16);

        // Series
        List<Double> xData1 = new LinkedList<Double>();
        List<Double> yData1 = new LinkedList<Double>();

        List<Double> xData2 = new LinkedList<Double>();
        List<Double> yData2 = new LinkedList<Double>();

        for (int i = 0; i < ast_probs.size(); i++) {
            if(ast_probs.get(i)>nast_probs.get(i)) {
                xData1.add(ast_probs.get(i));
                yData1.add(nast_probs.get(i));
            }
            else{
                xData2.add(ast_probs.get(i));
                yData2.add(nast_probs.get(i));
            }
        }

        chart.addSeries("Astronomy",xData1,yData1);
        chart.addSeries("Non Astronomy",xData2,yData2);

       // ScatterChart exampleChart = new ScatterChart();

        new SwingWrapper<XYChart>(chart).displayChart();
    }


    @Override
    public void endDocument() throws SAXException {
        //indexer.close();
        System.out.println("Total astronomy - " + n_ast);
        System.out.println("Total non astronomy - " + n_nast);
        System.out.println("Total infoboxes "+n_infoboxes);
        try {
            astroWriter.close();
            nonAstroWriter.close();
            excelLogger.saveExcel();
         //   classifier.closeLogger();;
        } catch (Exception e) {
            e.printStackTrace();
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
        if(title.startsWith("File:")||title.startsWith("Wikipedia:")||title.startsWith("Template:")||title.startsWith("Category:")||title.startsWith("Portal:")||title.startsWith("MediaWiki:")||title.startsWith("Book:")||title.startsWith("Draft:")||title.startsWith("Help:"))
            return -1;

        if (text.toLowerCase().startsWith("#redirect")||text.toLowerCase().contains("#REDIRECT[[") || text.contains("{{disambiguation")
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
