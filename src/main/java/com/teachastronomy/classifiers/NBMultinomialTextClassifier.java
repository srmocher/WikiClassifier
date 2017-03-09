package com.teachastronomy.classifiers;

/**
 * Created by root on 10/19/16.
 */
import com.teachastronomy.*;
import com.teachastronomy.wikipedia.WikiArticle;
import org.tartarus.snowball.ext.PorterStemmer;
import weka.classifiers.bayes.NaiveBayesMultinomialText;
import weka.core.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;


public class NBMultinomialTextClassifier {
    public static HashSet<String> stopWords = new HashSet<String>();
    public static HashSet<String> vocabulary = new HashSet<String>();
    public static List<String> astronomyTokens = new ArrayList<String>();
    public static List<String> otherTokens = new ArrayList<String>();
    public static int astroDocCount = 0;
    public static int otherDocCount = 0;



    NaiveBayesMultinomialText classifier;
    Logger excelLogger;
    BufferedWriter bw,bw1;


    public NBMultinomialTextClassifier() throws IOException{
        excelLogger = new Logger();
       // bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("/home/sridhar/Desktop/NAProbs.txt",true)));
        //bw1 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("/home/sridhar/Desktop/AProbs.txt",true)));
    }



    //New classifier training code
    public void trainUsingWEKA(){
        ArrayList<Attribute> attributes = TrainingDataHelper.getAttributes();
        Instances trainingSet = new Instances("trainingSet",attributes,0);
        trainingSet.setClassIndex(trainingSet.numAttributes()-1);
        classifier = new NaiveBayesMultinomialText();

        TrainingDataHelper.buildAstInstances(trainingSet);
        TrainingDataHelper.buildNonAstInstances(trainingSet);
        try {

     //       classifier.setMinWordFrequency(5);
            classifier.setLowercaseTokens(true);
            classifier.setUseWordFrequencies(true);
          //  classifier.setStopwordsHandler(new CustomStopWordsHandler());
            classifier.buildClassifier(trainingSet);


         //   double[] probs = classifier.getLogNormalizedProbablities();
         //   System.out.println(probs[0]);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public ClassificationResult classify(WikiArticle article){
        ArrayList<Attribute> attributes = TrainingDataHelper.getAttributes();
        Attribute classAttribute = attributes.get(attributes.size()-1);
        Instances testSet = new Instances("testSet",attributes,0);
        testSet.setClassIndex(testSet.numAttributes()-1);
        String text = article.getText();
        String title = article.getTitle();
        Instance testInstance = new DenseInstance(testSet.numAttributes());
        testInstance.setDataset(testSet);
       // text = text.replaceAll("[^ a-zA-Z]", " ").replaceAll("\\s+", " ");
       text = text.replaceAll("\\{", " ").replaceAll("}"," ");
    //    text = TrainingDataHelper.removeStopWords(text);


       // text = stem(text);

       title = title.replaceAll("[^ a-zA-Z]", " ");
      //  title = TrainingDataHelper.removeStopWords(title);


        title = stem(title);

        testInstance.setValue(0,title);
        testInstance.setValue(1,text);

        try {
            String result = classAttribute.value((int) classifier.classifyInstance(testInstance));
            double[] probs = classifier.distributionForInstance(testInstance);

            if(result.equals("Astronomy")) {
           //     excelLogger.writeToExcelSheet(article.getTitle(),probs[0],probs[1],1);
             //   bw1.write(probs[0]+","+probs[1]+"\n");
              //  bw1.flush();
            //    System.out.println(article.getTitle()+ " A "+(probs[0])+","+probs[1]);

                return ClassificationResult.Astronomy;

            }
            else {
            //    System.out.println(article.getTitle()+ " NA "+(1-probs[0]));

             //   bw.write(article.getTitle()+","+probs[0]+","+probs[1]+"\n");
                return ClassificationResult.NonAstronomy;
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private String stem(String text){
        PorterStemmer stemmer = new PorterStemmer();
        stemmer.setCurrent(text.toLowerCase());
        stemmer.stem();
        return stemmer.getCurrent();
    }

    public void closeLogger(){
        excelLogger.saveExcel();
    }

}





