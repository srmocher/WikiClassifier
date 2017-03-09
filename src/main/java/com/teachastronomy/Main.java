package com.teachastronomy;

import com.teachastronomy.classifiers.ClassificationResult;
import com.teachastronomy.classifiers.NBMultinomialTextClassifier;
import com.teachastronomy.classifiers.NaiveBayesClassifier;
import com.teachastronomy.classifiers.TrainingDataHelper;
import com.teachastronomy.wikipedia.ParsingHelper;

import com.teachastronomy.wikipedia.WikiArticle;


import org.sweble.wikitext.engine.EngineException;
import org.sweble.wikitext.engine.PageId;
import org.sweble.wikitext.engine.PageTitle;
import org.sweble.wikitext.engine.WtEngineImpl;
import org.sweble.wikitext.engine.config.WikiConfig;
import org.sweble.wikitext.engine.nodes.EngProcessedPage;
import org.sweble.wikitext.engine.utils.DefaultConfigEnWp;
import org.sweble.wikitext.parser.parser.LinkTargetException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;


public class Main {


    public static void main(String[] args) {
      //  TrainingDataHelper.getTrainingVector();
        ParsingHelper.parseDump(new File(Constants.DumpFileLocation));
       // evaluate();
    }

    static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
    public static String convertWikiText(String title, String wikiText, int maxLineLength) throws LinkTargetException,EngineException {
        // Set-up a simple wiki configuration
        WikiConfig config = DefaultConfigEnWp.generate();
        // Instantiate a compiler for wiki pagesi
        WtEngineImpl engine = new WtEngineImpl(config);
        // Retrieve a page
        PageTitle pageTitle = PageTitle.make(config, title);
        PageId pageId = new PageId(pageTitle, -1);
        // Compile the retrieved page
        EngProcessedPage cp = engine.postprocess(pageId, wikiText, null);
        TextConverter p = new TextConverter(config, maxLineLength);
        return (String)p.go(cp.getPage());
    }

    public static void evaluate(){
        try {

            ArrayList<String> classes = new ArrayList<>();
            classes.add("Astronomy");
            classes.add("Non Astronomy");
            NaiveBayesClassifier classifier = new NaiveBayesClassifier(classes);
            classifier.train(TrainingDataHelper.getTrainingData());
            File[] dataset = new File("F:\\Evaluation").listFiles();
            ArrayList<com.teachastronomy.wikipedia.WikiArticle> articles = new ArrayList<>();
            for(File file:dataset){
                String title = file.getName();
                String text = new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())));
                com.teachastronomy.wikipedia.WikiArticle article = new com.teachastronomy.wikipedia.WikiArticle(text,null,title,null);
                articles.add(article);
            }
            for(WikiArticle article:articles){
                String res = classifier.classify(article);
                System.out.println(article.getTitle()+" - "+res);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}



