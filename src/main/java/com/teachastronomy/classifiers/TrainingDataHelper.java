package com.teachastronomy.classifiers;

import com.teachastronomy.Constants;
import com.teachastronomy.wikipedia.WikiArticle;
import org.tartarus.snowball.ext.PorterStemmer;
import weka.core.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by root on 10/27/16.
 */
public class TrainingDataHelper {

    public static ArrayList<String> stopWords = new ArrayList<>();

    public static void buildAstInstances(Instances trainingSet){
        try{
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(Constants.astroTitlesFilePath)));
            String s;
            while((s=br.readLine())!=null){
                String text = new String(Files.readAllBytes(Paths.get(Constants.astroTrainingDataPath+s+".txt")));
                Instance inst = new DenseInstance(3);
                inst.setDataset(trainingSet);

              //  String text = new String(Files.readAllBytes(Paths.get("/home/sridhar/Desktop/TestData/"+title)));
                text = text.replaceAll("[^ a-zA-Z]", " ").replaceAll("\\s+", " ");
          //      text = text.replaceAll("\\{", " ").replaceAll("}"," ");
              //  text = TrainingDataHelper.removeStopWords(text);


                text = stem(text);

                s= s.replaceAll("[^ a-zA-Z]", " ");
           //    s= TrainingDataHelper.removeStopWords(s);


                s = stem(s);
                inst.setValue(0,s);

                //     text = text.toLowerCase();

                   // text = text.replaceAll("(<ref>).*?(<\\/ref>)", " ");
                            // .replaceAll("[^a-z]", " ");

                inst.setValue(1,text);
                inst.setClassValue("Astronomy");
                trainingSet.add(inst);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void buildNonAstInstances(Instances trainingSet){
        try{
            File[] files = new File("/home/sridhar/Desktop/TrainingData/New").listFiles();
            for(File f:files) {
                    String s = f.getName();
                    //  s=s.replace(" ","_");
                    String text = new String(Files.readAllBytes(Paths.get(f.getAbsolutePath())));
                    Instance inst = new DenseInstance(3);
                    inst.setDataset(trainingSet);
                    text = text.replaceAll("[^ a-zA-Z]", " ").replaceAll("\\s+", " ");
                    //  text = text.replaceAll("\\}","").replaceAll("\\{","").replaceAll("\\[","");
                    //   text = text.replaceAll("\\{", " ").replaceAll("\\}"," ");
//                text = TrainingDataHelper.removeStopWords(text);


                    text = stem(text);

                    s = s.replaceAll("[^ a-zA-Z]", " ").replaceAll("\\s+", " ");
                    ;
                    //   s= TrainingDataHelper.removeStopWords(s);


                    s = stem(s);
                    inst.setValue(0, s);
                    inst.setValue(1, text);
                    inst.setClassValue("Non Astronomy");
                    trainingSet.add(inst);
                }

        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public static ArrayList<Attribute> getAttributes(){
        Attribute titleAttribute = new Attribute("title",(FastVector)null);
      //  titleAttribute.setWeight(2);
        Attribute textAttribute = new Attribute("text",(FastVector)null);
        ArrayList<String> classes = new ArrayList<>();
        classes.add(Constants.astronomy);
        classes.add(Constants.nonAstronomy);
        Attribute classAttribute = new Attribute("@@class@@",classes);
        ArrayList<Attribute> attributes = new ArrayList<>();
        attributes.add(titleAttribute);
        attributes.add(textAttribute);
        attributes.add(classAttribute);
        return attributes;
    }

    public static String removeStopWords(String text){
        try {
            stopWords.clear();
            FileInputStream fis = new FileInputStream(new File(Constants.stopWordsFilePath));
            InputStreamReader is = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(is);

            String line = br.readLine();

            while(line != null) {
                stopWords.add(line);
                line = br.readLine();
            }
            br.close();
            List<String> wikiStopwords = Files.readAllLines(Paths.get(Constants.wikipediaStopWordsFile));
            for(String word:wikiStopwords){
                stopWords.add(word);
            }
        } catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        StringBuffer result = new StringBuffer();
        String[] words = text.split(" ");
        for(String word:words){
            if(!stopWords.contains(word.toLowerCase()))
                result.append(word+" ");
        }
        return result.toString();
    }

    private static String stem(String text){
        PorterStemmer stemmer = new PorterStemmer();
        stemmer.setCurrent(text.toLowerCase());
        stemmer.stem();
        return stemmer.getCurrent();
    }

    public static ArrayList<WikiArticle> getTrainingData(){
        ArrayList<WikiArticle> articles = new ArrayList<>();
        try {

            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(Constants.astroTitlesFilePath)));
            String s;
            while ((s = br.readLine()) != null) {
                String text = new String(Files.readAllBytes(Paths.get(Constants.astroTrainingDataPath + s + ".txt")));
                WikiArticle article = new WikiArticle(text,null,s,null);
                article.setCategory("Astronomy");
                articles.add(article);
            }
            br.close();

            File[] files = new File("/home/sridhar/Desktop/TrainingData/NA").listFiles();

            for(File f:files){
                String text = new String(Files.readAllBytes(Paths.get(f.getAbsolutePath())));
                WikiArticle article = new WikiArticle(text,null,f.getName(),null);
                article.setCategory("Non Astronomy");
                articles.add(article);
            }

        }
        catch (Exception e){
            e.printStackTrace();
        }
        return articles;
    }

}
