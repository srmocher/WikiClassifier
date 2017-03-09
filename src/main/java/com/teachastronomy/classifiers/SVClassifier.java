package com.teachastronomy.classifiers;

import com.teachastronomy.transformers.TfIdfTransformer;
import com.teachastronomy.wikipedia.WikiArticle;
import org.tartarus.snowball.ext.PorterStemmer;
import weka.core.tokenizers.WordTokenizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;

/**
 * Created by sridh on 12/30/2016.
 */


public class SVClassifier {

    Hashtable<String,HashMap<String,Double>> wordVecs;
    HashSet<String> vocabulary;
    Hashtable<String,ArrayList<WikiDocument>> documentsCounts;

    ArrayList<Hashtable<String,Integer>> documents;

    public void trainUsingSMO(ArrayList<WikiArticle> articles)
    {
        int[] y = new int[articles.size()];
        int i=0;
        for(WikiArticle article:articles)
        {
            if(article.getCategory().equals("Astronomy"))
                y[i]=1;
            else
                y[i]=-1;
        }
    }

    private void computeTfIdf(ArrayList<WikiArticle> articles) {
        wordVecs = new Hashtable<>();
        vocabulary = new HashSet<>();
        documentsCounts = new Hashtable<>();
        documentsCounts.put("Astronomy",new ArrayList<>());
        documentsCounts.put("Non Astronomy",new ArrayList<>());
        wordVecs.put("Astronomy",new HashMap<>());
        wordVecs.put("Non Astronomy",new HashMap<>());
        documents = new ArrayList<>();
        for(WikiArticle article:articles){
            String text = article.getText();
            //
            //  text = convertWikiText(article.getTitle(),text,45);
            text = text.replaceAll("<ref.*</ref>"," ");
            text = text.replaceAll("https?:\\/\\/(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~#?&//=]*)"," ");

            text = text.replaceAll("[^ a-zA-Z]", " ").replaceAll("\\s+", " ");
            Hashtable<String,Integer> words = getWords(text);
            for(String word:words.keySet()){
                if(!vocabulary.contains(word))
                    vocabulary.add(word);
            }
            ArrayList<WikiDocument> docCounts= documentsCounts.get(article.getCategory());
            WikiDocument wd = new WikiDocument(words,article.getCategory());
            docCounts.add(wd);
            documents.add(words);
        }
        TfIdfTransformer transformer = new TfIdfTransformer();

        int i=0;
        for(WikiArticle article:articles){
            String text = article.getText();

            text = article.getText();
            text = text.replaceAll("<ref.*</ref>"," ");
            text = text.replaceAll("https?:\\/\\/(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~#?&//=]*)"," ");

            text = text.replaceAll("[^ a-zA-Z]", " ").replaceAll("\\s+", " ");
            Hashtable<String,Integer> words = getWords(text);

            //  HashSet<TermVector> termVecs = new HashSet<>();
            HashMap<String,Double> vecs = wordVecs.get(article.getCategory());

            for(String word:vocabulary){
                double tfidf = transformer.tfIdf(words,documents,word);

                if(vecs.containsKey(word))
                {
                    double score = vecs.get(word);
                    score+=tfidf;
                    vecs.remove(word);
                    vecs.put(word,score);
                }
                else
                    vecs.put(word,tfidf);
            }

        }


    }

    private Hashtable<String,Integer> getWords(String text) {
        text = text.toLowerCase();
        WordTokenizer tokenizer = new WordTokenizer();
        CustomStopWordsHandler handler = new CustomStopWordsHandler();
        tokenizer.tokenize(text);
        Hashtable<String,Integer> words = new Hashtable<>();
        while(tokenizer.hasMoreElements()){
            String word = tokenizer.nextElement();
            if(handler.isStopword(word)||word.length()<2)
                continue;
            word = stem(word);
            if(word.length()<2)
                continue;
            if(!words.containsKey(word)){
                words.put(word,1);
            }
            else{
                int count = words.get(word);
                count++;
                words.remove(word);
                words.put(word,count);
            }
        }
        return words;
    }

    private static String stem(String text){ //use Porter stemmer algorithm
        PorterStemmer stemmer = new PorterStemmer();
        stemmer.setCurrent(text.toLowerCase());
        stemmer.stem();
        return stemmer.getCurrent();
    }
}
