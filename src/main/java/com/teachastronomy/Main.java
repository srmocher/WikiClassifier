package com.teachastronomy;

import com.teachastronomy.classifiers.ClassificationResult;
import com.teachastronomy.classifiers.NBMultinomialTextClassifier;
import com.teachastronomy.classifiers.NaiveBayesClassifier;
import com.teachastronomy.classifiers.TrainingDataHelper;
import com.teachastronomy.wikipedia.ParsingHelper;

import com.teachastronomy.wikipedia.WikiArticle;

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



