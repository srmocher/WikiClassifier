package com.teachastronomy.classifiers;

import com.teachastronomy.wikipedia.WikiArticle;
import org.tartarus.snowball.ext.PorterStemmer;
import weka.core.pmml.Array;
import weka.core.pmml.MappingInfo;
import weka.core.tokenizers.WordTokenizer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashMap;

/**
 * Created by root on 11/9/16.
 */
public class NaiveBayesBernoulli {

    private CustomStopWordsHandler handler;
    private double[] priors;
    private ArrayList<String> classes;
    private LinkedHashMap<String,HashSet<String>> wordsPerClass;
    private Hashtable<String,LinkedHashMap<String,Double>> conditionalProbabilities;
    private HashSet<String> vocabulary;
    private LinkedHashMap<WikiArticle,HashSet<String>> docWordsMapping;
    public NaiveBayesBernoulli(ArrayList<String> classes){
        priors = new double[classes.size()];
        wordsPerClass = new LinkedHashMap<>();
        handler = new CustomStopWordsHandler();
        conditionalProbabilities = new Hashtable<>();
        vocabulary = new HashSet<>();
        docWordsMapping = new LinkedHashMap<>();
        this.classes = classes;
        this.docWordsMapping = new LinkedHashMap<>();
        for(String cls:classes) {
            wordsPerClass.put(cls, new HashSet<>());
            conditionalProbabilities.put(cls,new LinkedHashMap<>());
        }
    }

    public void train(ArrayList<WikiArticle> articles){
        for(int i=0;i<priors.length;i++)
            priors[i]=0.0;
        for(WikiArticle article:articles){
            this.tokenize(article);
            int index = this.classes.indexOf(article.getCategory());
            HashSet<String> termsForDoc = extractTerms(article);
            docWordsMapping.put(article,termsForDoc);
            priors[index]++;
        }

        for(String category:classes){
            LinkedHashMap<String,Double> probabilities = conditionalProbabilities.get(category);
            for(String term:vocabulary){
                int N_c = countDocsInClassWithTerm(term,articles,category);
                int N = (int)priors[classes.indexOf(category)];
                double cond_prob = (double)(N_c+1)/(double)(N+2);
                probabilities.put(term,cond_prob);
            }
        }
        double evidence = 0;
        for(int i=0;i<priors.length;i++)
            evidence = evidence + priors[i];
        for(int i=0;i<priors.length;i++)
            priors[i] = priors[i]/evidence;
    }

    public String classify(WikiArticle article){
        double[] posteriors = new double[classes.size()];
        HashSet<String> terms = extractTermsFromVocabulary(article);
        for(int i=0;i<priors.length;i++)
            posteriors[i] = Math.log(priors[i]);
        for(String category:classes){
            LinkedHashMap<String,Double> probs = conditionalProbabilities.get(category);
            int index = classes.indexOf(category);
            for(String vocabularyTerm:vocabulary){
                if(terms.contains(vocabularyTerm)){
                    posteriors[index] = posteriors[index]+Math.log(probs.get(vocabularyTerm));
                }
                else{
                    posteriors[index] = posteriors[index] + Math.log(1-probs.get(vocabularyTerm));
                }
            }
        }

        double max=posteriors[0];
        int maxIndex = 0;
        for(int i=1;i<classes.size();i++)
            if(posteriors[i]>max) {
                max = posteriors[i];
                maxIndex = i;
            }
        String category = classes.get(maxIndex);
        String title = article.getTitle();
        return category;
    }

    private HashSet<String> extractTerms(WikiArticle article){
        HashSet<String> words = new HashSet<>();
        String title = article.getTitle();
        title = title.replaceAll("[^ a-zA-Z]", " ").replaceAll("\\s+", " ");
        this.extractTerms(title.toLowerCase(),words);

        String text = article.getText();
        text = text.replaceAll("[^ a-zA-Z]", " ").replaceAll("\\s+", " ");
        this.extractTerms(text.toLowerCase(),words);
        return  words;
    }

    private HashSet<String> extractTermsFromVocabulary(WikiArticle article){
        HashSet<String> words = new HashSet<>();
        String title = article.getTitle();
        title = title.replaceAll("[^ a-zA-Z]", " ").replaceAll("\\s+", " ");
        this.extractTermsFromVocabulary(title.toLowerCase(),words);

        String text = article.getText();
        text = text.replaceAll("[^ a-zA-Z]", " ").replaceAll("\\s+", " ");
        this.extractTermsFromVocabulary(text.toLowerCase(),words);
        return  words;
    }

    private void extractTermsFromVocabulary(String str,HashSet<String> words){
        WordTokenizer tokenizer = new WordTokenizer();
        tokenizer.tokenize(str);
        while(tokenizer.hasMoreElements()){
            String word = tokenizer.nextElement();
            if(handler.isStopword(word))
                continue;
            word = stem(word);
            if(!words.contains(word))
                words.add(word);

        }
    }

    private void extractTerms(String str,HashSet<String> words){
        WordTokenizer tokenizer = new WordTokenizer();
        tokenizer.tokenize(str);
        while(tokenizer.hasMoreElements()){
            String word = tokenizer.nextElement();
            if(handler.isStopword(word))
                continue;
            word = stem(word);
            if(!words.contains(word) && vocabulary.contains(word))
                words.add(word);

        }
    }
    private int countDocsInClassWithTerm(String term,ArrayList<WikiArticle> articles,String category){
        HashSet<String> words = wordsPerClass.get(category);
        int count = 0;
        for(WikiArticle article:articles){
            HashSet<String> terms = docWordsMapping.get(article);
            if(terms.contains(term) && article.getCategory().equals(category))
                count++;
        }
        return count;
    }

    public double[] getProbabilities(WikiArticle article){
        double[] posteriors = new double[classes.size()];
        HashSet<String> terms = extractTerms(article);
        for(int i=0;i<priors.length;i++)
            posteriors[i] = Math.log(priors[i]);
        for(String category:classes){
            LinkedHashMap<String,Double> probs = conditionalProbabilities.get(category);
            int index = classes.indexOf(category);
            for(String vocabularyTerm:vocabulary){
                if(terms.contains(vocabularyTerm)){
                    posteriors[index] = posteriors[index]+Math.log(probs.get(vocabularyTerm));
                }
                else{
                    posteriors[index] = posteriors[index] + Math.log(1-probs.get(vocabularyTerm));
                }
            }
        }
        return posteriors;
    }


    private void tokenize(WikiArticle article){
        String title = article.getTitle();
        title = title.replaceAll("[^ a-zA-Z]", " ").replaceAll("\\s+", " ");
        this.tokenize(title.toLowerCase(),article.getCategory());

        String text = article.getText();
        text = text.replaceAll("[^ a-zA-Z]", " ").replaceAll("\\s+", " ");
        this.tokenize(text.toLowerCase(),article.getCategory());
    }

    private void tokenize(String str, String category){
        WordTokenizer tokenizer = new WordTokenizer();
        tokenizer.tokenize(str);
        while(tokenizer.hasMoreElements()){
            String word = tokenizer.nextElement();
            if(handler.isStopword(word))
                continue;
            word = stem(word);
            HashSet<String> words = wordsPerClass.get(category);

            if(!word.contains(word) && word.length() > 2)
                words.add(word);
            if(!vocabulary.contains(word) && word.length()>2)
                vocabulary.add(word);
        }
    }

    private String stem(String text){
        PorterStemmer stemmer = new PorterStemmer();
        stemmer.setCurrent(text.toLowerCase());
        stemmer.stem();
        return stemmer.getCurrent();
    }
}
