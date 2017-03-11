package com.teachastronomy.classifiers;

import com.teachastronomy.Constants;
import com.teachastronomy.TextConverter;
import com.teachastronomy.transformers.TfIdfTransformer;
import com.teachastronomy.wikipedia.WikiArticle;
import org.apache.lucene.index.Term;
import org.sweble.wikitext.engine.EngineException;
import org.sweble.wikitext.engine.PageId;
import org.sweble.wikitext.engine.PageTitle;
import org.sweble.wikitext.engine.WtEngineImpl;
import org.sweble.wikitext.engine.config.WikiConfig;
import org.sweble.wikitext.engine.nodes.EngProcessedPage;
import org.sweble.wikitext.engine.utils.DefaultConfigEnWp;
import org.sweble.wikitext.parser.parser.LinkTargetException;
import org.tartarus.snowball.ext.PorterStemmer;
import sun.awt.image.ImageWatched;
import weka.core.*;
import weka.core.tokenizers.WordTokenizer;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by root on 10/27/16.
 */
public class TrainingDataHelper {

    public static ArrayList<String> stopWords = new ArrayList<>();

    public static void buildAstInstances(Instances trainingSet){
        try{
            File[] files = new File("/home/sridhar/TrainingData/CS").listFiles();
            for(File f:files) {
                String s = f.getName();
                //  s=s.replace(" ","_");
                String text = new String(Files.readAllBytes(Paths.get(f.getAbsolutePath())));
                Instance inst = new DenseInstance(3);
                inst.setDataset(trainingSet);
              //  text = text.replaceAll("[^ a-zA-Z]", " ").replaceAll("\\s+", " ");
                 text = text.replaceAll("\\}","").replaceAll("\\{","").replaceAll("\\[","");
                   text = text.replaceAll("\\{", " ").replaceAll("\\}"," ");
//                text = TrainingDataHelper.removeStopWords(text);

                text = removeStopWords(text);
                text = stem(text);

                s = s.replaceAll("[^ a-zA-Z]", " ").replaceAll("\\s+", " ");
                ;
                //   s= TrainingDataHelper.removeStopWords(s);


                s = stem(s);
                inst.setValue(0, s);
                inst.setValue(1, text);
                inst.setClassValue("CS");
                trainingSet.add(inst);
            }

        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void buildNonAstInstances(Instances trainingSet){
        try{
            File[] files = new File("/home/sridhar/TrainingData/NCS").listFiles();
            for(File f:files) {
                    String s = f.getName();
                    //  s=s.replace(" ","_");
                    String text = new String(Files.readAllBytes(Paths.get(f.getAbsolutePath())));
                    Instance inst = new DenseInstance(3);
                    inst.setDataset(trainingSet);
                   // text = text.replaceAll("[^ a-zA-Z]", " ").replaceAll("\\s+", " ");
                      text = text.replaceAll("\\}","").replaceAll("\\{","").replaceAll("\\[","");
                       text = text.replaceAll("\\{", " ").replaceAll("\\}"," ");
//                text = TrainingDataHelper.removeStopWords(text);

                    text = removeStopWords(text);
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
        classes.add("Astronomy");
        classes.add("Non Astronomy");
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

            File[] files = new File("F:\\TD\\A").listFiles();

            for(File f:files){
                String text = new String(Files.readAllBytes(Paths.get(f.getAbsolutePath())));
                WikiArticle article = new WikiArticle(text,null,f.getName(),null);
                article.setCategory("Astronomy");
                articles.add(article);
            }

             files = new File("F:\\TD\\NA").listFiles();
            int j=0;
            for(File f:files){
                String text = new String(Files.readAllBytes(Paths.get(f.getAbsolutePath())));
                WikiArticle article = new WikiArticle(text,null,f.getName(),null);
                article.setCategory("Non Astronomy");
                articles.add(article);
                j++;

            }

        }
        catch (Exception e){
            e.printStackTrace();
        }
        return articles;
    }



    private static String convertWikiText(String title, String wikiText, int maxLineLength) {
        try {
            if(title.equals("")|| wikiText.equals("")){
                System.out.println(title+" "+wikiText);
            }
            // Set-up a simple wiki configuration
            WikiConfig config = DefaultConfigEnWp.generate();
            // Instantiate a compiler for wiki pages
            WtEngineImpl engine = new WtEngineImpl(config);
            // Retrieve a page
            PageTitle pageTitle = PageTitle.make(config, title);
            PageId pageId = new PageId(pageTitle, -1);
            // Compile the retrieved page
            EngProcessedPage cp = engine.postprocess(pageId, wikiText, null);
            TextConverter p = new TextConverter(config, maxLineLength);
            return (String) p.go(cp.getPage());
        }
        catch (EngineException ee){
            ee.printStackTrace();;
        }
        catch (LinkTargetException lte){
            lte.printStackTrace();
        }
        return null;
    }

}



class WikiDocument
{
    public Hashtable<String,Integer> wordCounts;
    public String category;

    public WikiDocument(Hashtable<String,Integer> ht,String cat)
    {
        this.wordCounts = ht;
        this.category=cat;
    }
}

