package com.teachastronomy.classifiers;

import com.teachastronomy.Logger;
import com.teachastronomy.wikipedia.WikiArticle;
import org.tartarus.snowball.ext.PorterStemmer;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.*;
import weka.filters.unsupervised.attribute.StringToWordVector;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * Created by root on 10/26/16.
 */
public class NBClassifier implements IClassifier {

   FilteredClassifier classifier;
   StringToWordVector filter;
    Logger excelLogger;
    public NBClassifier(){
        classifier = new FilteredClassifier();
        filter = new StringToWordVector();
        excelLogger = new Logger();
        classifier.setClassifier(new NaiveBayes());
    }



    public void trainUsingWEKA(){
        ArrayList<Attribute> attributes = TrainingDataHelper.getAttributes();
        Instances inputInstances = new Instances("TrainingSet",attributes,0);

        inputInstances.setClassIndex(attributes.size()-1);
        TrainingDataHelper.buildAstInstances(inputInstances);
        TrainingDataHelper.buildNonAstInstances(inputInstances);

        try {


            filter.setInputFormat(inputInstances);
       //    classifier.setFilter(filter);
            classifier.setFilter(filter);
            filter.setStopwordsHandler(new CustomStopWordsHandler());
          //  filter.setIDFTransform(true);
         //   filter.setTFTransform(true);
            classifier.buildClassifier(inputInstances);


            Instance testInstance = new DenseInstance(3);
            Instances testInstances = new Instances("TestSet",attributes,0);
            testInstances.setClassIndex(testInstances.numAttributes()-1);
            String title = "Astronaut";
            String text = new String(Files.readAllBytes(Paths.get("/home/sridhar/Desktop/TestData/"+title)));

            testInstance.setDataset(testInstances);
            text = text.replaceAll("[^ a-zA-Z]", " ").replaceAll("\\s+", " ");
            //  text = TrainingDataHelper.removeStopWords(text);
            text = stem(text);

            title = title.replaceAll("[^ a-zA-Z]", " ");
            //  title = TrainingDataHelper.removeStopWords(title);


            title = stem(title);

            testInstance.setValue(0,title);
            testInstance.setValue(1,text);

            testInstances.add(testInstance);

         //   Instances outputtestInsts = Filter.useFilter(testInstances,filter);
            for(int i=0;i<testInstances.size();i++){
                double res = classifier.classifyInstance(testInstances.get(i));
                double[] probs = classifier.distributionForInstance(testInstances.get(i));
                System.out.println(attributes.get(2).value((int)res) +","+probs[0]+","+probs[1]);


            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public ClassificationResult classify(WikiArticle article) {
        ArrayList<Attribute> attributes = TrainingDataHelper.getAttributes();
        Attribute classAttribute = attributes.get(attributes.size()-1);
        Instances testSet = new Instances("testSet",attributes,0);
        testSet.setClassIndex(testSet.numAttributes()-1);
        String text = article.getText();
        String title = article.getTitle();
        Instance testInstance = new DenseInstance(testSet.numAttributes());
        testInstance.setDataset(testSet);
        text = text.replaceAll("[^ a-zA-Z]", " ").replaceAll("\\s+", " ");
      //  text = TrainingDataHelper.removeStopWords(text);
        text = stem(text);

        title = title.replaceAll("[^ a-zA-Z]", " ");
     //  title = TrainingDataHelper.removeStopWords(title);


        title = stem(title);

        testInstance.setValue(0,title);
        testInstance.setValue(1,text);






            try{
            String result = classAttribute.value((int) classifier.classifyInstance(testInstance));
            double[] probs = classifier.distributionForInstance(testInstance);

            if(result.equals("Astronomy")) {

                excelLogger.writeToExcelSheet(article.getTitle(),probs[0],probs[1],1);
                if(probs[0]<0.9)
                 System.out.println(article.getTitle()+ " A "+probs[0]+","+probs[1]);
                return ClassificationResult.Astronomy;

            }
            else {
                if(probs[1]<0.9)
                    System.out.println(article.getTitle()+" NA "+probs[0]+","+probs[1]);
              //  System.out.println(article.getTitle()+ " NA "+res.toPlainString());
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

    public double[] getProbabilities(WikiArticle article){
        try {
            ArrayList<Attribute> attributes = TrainingDataHelper.getAttributes();
            Attribute classAttribute = attributes.get(attributes.size() - 1);
            Instances testSet = new Instances("testSet", attributes, 0);
            testSet.setClassIndex(testSet.numAttributes() - 1);
            String text = article.getText();
            String title = article.getTitle();
            Instance testInstance = new DenseInstance(testSet.numAttributes());
            testInstance.setDataset(testSet);
            text = text.replaceAll("[^ a-zA-Z]", " ").replaceAll("\\s+", " ");
            //  text = TrainingDataHelper.removeStopWords(text);
            text = stem(text);

            title = title.replaceAll("[^ a-zA-Z]", " ");
            //  title = TrainingDataHelper.removeStopWords(title);


            title = stem(title);

            testInstance.setValue(0, title);
            testInstance.setValue(1, text);

            return classifier.distributionForInstance(testInstance);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

}
