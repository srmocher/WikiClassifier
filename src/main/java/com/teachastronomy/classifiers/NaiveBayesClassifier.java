package com.teachastronomy.classifiers;

import com.teachastronomy.TextConverter;
import com.teachastronomy.transformers.TfIdfTransformer;
import com.teachastronomy.wikipedia.WikiArticle;
import org.apache.commons.collections4.IterableGet;


import org.tartarus.snowball.ext.PorterStemmer;
import weka.core.tokenizers.WordTokenizer;

import javax.naming.LinkException;
import java.util.*;

/**
 * Created by Sridhar on 11/6/16.
 */

//Implements multinomial model for Naive Bayes
public class NaiveBayesClassifier {

    //handler for stop words
    CustomStopWordsHandler stopWordsHandler = new CustomStopWordsHandler();

    //store unique words for each class label
    Hashtable<String,HashSet<String>> classDictionary;

    //store word counts for set of words for each class label
    Hashtable<String,LinkedHashMap<String,Double>> wordCounts;

    //store word probabilties for each word in the vocabulary for each class label
    Hashtable<String,LinkedHashMap<String,Double>> wordProbabilites;

    Hashtable<String,LinkedHashMap<String,Double>> compWordProbabilites;

    //class labels
    ArrayList<String> classes;

    //prior probabilties
    double[] priors;

    //store all words including repetitions across all documents in the training set
    ArrayList<String> allWords;

    Hashtable<String,HashMap<String,Double>> wordVecs;
    HashSet<String> vocabulary;
    Hashtable<String,ArrayList<WikiDocument>> documentsCounts;

    ArrayList<Hashtable<String,Integer>> documents;

  //  double[] probWordGivenClass;
    Hashtable<String,ArrayList<String>> wordsForClass; //vocabular for each class
    public NaiveBayesClassifier(ArrayList<String> classes){
        this.classDictionary = new Hashtable<>();
        this.wordCounts = new Hashtable<>();
        this.wordProbabilites = new Hashtable<>();
        this.compWordProbabilites = new Hashtable<>();
        this.priors = new double[classes.size()];
        this.wordsForClass = new Hashtable<>();
        this.allWords = new ArrayList<>();
        this.classes = classes;
        for(String cls:classes){
            this.classDictionary.put(cls,new HashSet<>(100));
            this.wordCounts.put(cls,new LinkedHashMap<>(100));
            this.wordProbabilites.put(cls,new LinkedHashMap<>(100));
            this.wordsForClass.put(cls,new ArrayList<>());
            this.compWordProbabilites.put(cls,new LinkedHashMap<>(100));
        }
    }


    public void trainUsingTfIdf(ArrayList<WikiArticle> articles){
        for(int i=0;i<this.classes.size();i++)
            priors[i]=0.0; //initialize priors

        for(WikiArticle article:articles){

            int index = classes.indexOf(article.getCategory()); //get index of class label
            priors[index]++; //increment doc count for class label
        }
        computeTfIdf(articles);

        for(WikiArticle article:articles)
        {
            LinkedHashMap<String,Double> probs = this.wordProbabilites.get(article.getCategory());
            HashMap<String,Double> vec = wordVecs.get(article.getCategory());
            int uniqueWordsInClass = 0;
            int totalWordsInClass = 0;
            ArrayList<WikiDocument> wds = documentsCounts.get(article.getCategory());
            for(WikiDocument wd:wds)
            {
                for(int cnt:wd.wordCounts.values())
                    totalWordsInClass+=cnt;
                uniqueWordsInClass+=wd.wordCounts.size();
            }

            for(String word:vocabulary)
            {
                double scr;
                Double score = vec.get(word);
                if(score==null)
                    scr=0.0;
                else scr = score;
                double prob = (double)(scr+1)/(double)(totalWordsInClass+uniqueWordsInClass);
                probs.put(word,prob);
            }

        }

        double evidence = 0.0;
        for(int i=0;i<priors.length;i++)
            evidence = evidence + priors[i];
        for(int i=0;i<priors.length;i++)
            priors[i]=priors[i]/evidence;
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
        Hashtable<WikiArticle,Hashtable<String,Integer>> articleCounts = new Hashtable<>();
        for(WikiArticle article:articles){
            String text = article.getText();
            //
            //  text = convertWikiText(article.getTitle(),text,45);
            text = text.replaceAll("<ref.*</ref>"," ");
            text = text.replaceAll("https?:\\/\\/(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~#?&//=]*)"," ");

            text = text.replaceAll("[^ a-zA-Z]", " ").replaceAll("\\s+", " ");
            Hashtable<String,Integer> words = getWords(text);
            articleCounts.put(article,words);
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

            Hashtable<String,Integer> words = articleCounts.get(article);

            //  HashSet<TermVector> termVecs = new HashSet<>();
            HashMap<String,Double> vecs = wordVecs.get(article.getCategory());

            for(String word:vocabulary){
                double tfidf = transformer.tfIdf(words,documents,word);

                if(vecs.containsKey(word))
                {
                    double score = vecs.get(word);

                    score=score+words.size()*tfidf;
                    if(Double.isNaN(score))
                    {
                        System.out.println(tfidf);
                    }
                    vecs.remove(word);
                    vecs.put(word,score);
                }
                else
                    vecs.put(word,tfidf);
            }



        }
        double denom=0.0;
        for(int j=0;i<classes.size();i++)
        {
            HashMap<String,Double> tfIdfVals = this.wordVecs.get(classes.get(j));
            for(Double d:tfIdfVals.values())
                denom = denom + d*d;
        }
        denom = Math.sqrt(denom);
        for(int j=0;i<classes.size();i++)
        {
            HashMap<String,Double> tfIdfVals = this.wordVecs.get(classes.get(j));
            HashMap<String,Double> newVals= new HashMap<>();
            Iterator<Map.Entry<String,Double>> it = tfIdfVals.entrySet().iterator();
            while (it.hasNext()){
                Map.Entry<String,Double> ele = it.next();
                double newtfIdf = ele.getValue()/denom;
                newVals.put(ele.getKey(),newtfIdf);
            }
            this.wordVecs.remove(classes.get(j));
            this.wordVecs.put(classes.get(i),newVals);
        }
    }



    private static Hashtable<String,Integer> getWords(String text) {
        text = text.toLowerCase();
        WordTokenizer tokenizer = new WordTokenizer();
        CustomStopWordsHandler handler = new CustomStopWordsHandler();
        tokenizer.tokenize(text);
        Hashtable<String,Integer> words = new Hashtable<>();
        while(tokenizer.hasMoreElements()){
            String word = tokenizer.nextElement();
            if(handler.isStopword(word))
                continue;
            word = stem(word);

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

    //train using set of articles
    public void train(ArrayList<WikiArticle> articles){
        for(int i=0;i<this.classes.size();i++)
            priors[i]=0.0; //initialize priors
     //   String[] docsText = new String[classes.size()];
        for(WikiArticle article:articles){
            this.tokenize(article); //extract tokens
            int index = classes.indexOf(article.getCategory()); //get index of class label
            priors[index]++; //increment doc count for class label
        }

      //  computeTFTransform();
      //  computeIDFTransform(articles);
     //   normalizeFrequencies();
        int totalVocabularySize=0;

//        for(int i=0;i<classes.size();i++){
//            LinkedHashMap<String,Double> counts = this.wordCounts.get(classes.get(i));
//            Set<String> words=counts.keySet();
//            for(String word:words){
//                if(counts.get(word)<5)
//                    counts.remove(word);
//            }
//        }
//        for(int i=0;i<classes.size();i++)
//        {
//            LinkedHashMap<String,Double> map = this.wordCounts.get(classes.get(i));
//            Iterator<Map.Entry<String,Double>> it = map.entrySet().iterator();
//            HashSet<String> classwords = this.classDictionary.get(classes.get(i));
//            while(it.hasNext()){
//                Map.Entry<String,Double> entry = it.next();
//                if(entry.getValue()<3) {
//                    it.remove();
//                    allWords.remove(entry.getKey());
//                    classwords.remove(entry.getKey());
//
//                }
//            }
//        }
        for(int i=0;i<classes.size();i++)
            totalVocabularySize+=this.classDictionary.get(classes.get(i)).size();
        for(int i=0;i<classes.size();i++){ //for each class label
            HashSet<String> map = this.classDictionary.get(classes.get(i)); // get hashtable to store words for that vocabulary
            LinkedHashMap<String,Double> termCounts = this.wordCounts.get(classes.get(i));//get hashmap to store counts for each word
            LinkedHashMap<String,Double> probabilties = this.wordProbabilites.get(classes.get(i)); //get hashmap to store probabilities for each word
            Collection<Double> counts = termCounts.values(); //iterate over all counts
            int totalCounts = 0;
            for(Double count:counts)
                totalCounts+=count;
            double denom = (double)totalVocabularySize+(double)(totalCounts);
            //totalCounts+=termCounts.size(); //total vocabulary size
            for(String word:allWords){ //for each word in all documents
                Double count = termCounts.get(word); // get count
                if(count==null) //set count to zero if it did not occur
                    count=0.0;

               // String cat = getWordClass(word);
                double prob = (double)(count+1)/(denom); // compute cond prob with laplacian smooting to account for zero counts

                probabilties.put(word,prob);//store probability
            }
        }


        double evidence = 0.0;
        for(int i=0;i<priors.length;i++)
            evidence = evidence + priors[i];
        for(int i=0;i<priors.length;i++)
            priors[i]=priors[i]/evidence; //compute prior probabilities
    }

    private String getWordClass(String w){
        for(int i=0;i<classes.size();i++){
            HashSet<String> words = this.classDictionary.get(classes.get(i));
            if(words.contains(w))
                return classes.get(i);
        }
        return null;
    }



//    private int findWordCount(String source,String category){
//        int count=0;
//        ArrayList<String> words = wordsForClass.get(category);
//        for(String word:words)
//            if(word.equals(source))
//                count++;
//        return count;
//    }
    private void tokenize(WikiArticle article){
        String title = article.getTitle(); //get title
        title = title.replaceAll("[^ a-zA-Z]", " ").replaceAll("\\s+", " "); //remove non letters and additional spaces
        this.tokenize(title.toLowerCase(),article.getCategory()); // get words

        String text = article.getText(); //get text
        text = text.replaceAll("<ref.*</ref>"," ");
        text = text.replaceAll("https?:\\/\\/(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~#?&//=]*)"," ");

        text = text.replaceAll("[^ a-zA-Z]", " ").replaceAll("\\s+", " ");//remove non letters and additional spaces
        this.tokenize(text.toLowerCase(),article.getCategory());//get words and store in vocabular
    }

    public String classify(WikiArticle article){
       double[] posteriors = getProbabilities(article); //get probabilties
      //  normalize(posteriors);
        double max=posteriors[0]; //compute max of class probabilities
        int maxIndex = 0;
        for(int i=0;i<classes.size();i++){
            if(posteriors[i]>max ){
                max=posteriors[i];
                maxIndex=i;
            }
        }

        return classes.get(maxIndex);//return corresponding label
    }


    public double[] getProbabilities(WikiArticle article){
        ArrayList<String> terms = extractTerms(article);
        double[] posteriors = new double[classes.size()];

        for(int i=0;i<classes.size();i++){ //for each class label
            LinkedHashMap<String,Double> probabilities = this.wordProbabilites.get(classes.get(i)); //get probabilities
            posteriors[i] = Math.log(priors[i]); //initialize and store log of prior probability
            int j=0;
            for(String term:terms){ //for each word in the unclassified document
                Double condProb = probabilities.get(term.toLowerCase()); //get probability
                if(condProb==null) {
                  //  System.out.println(term);//ignore the word if it does not exist in vocabulary
                    continue;
                }
                j++;
                posteriors[i]+=Math.log(condProb); //add log of cond prob
            }
         //   System.out.println(j);
        }
        return  posteriors;//return the probs
    }
    private ArrayList<String> extractTerms(WikiArticle article){ //same as tokenize function but stores words in an ArrayList
        ArrayList<String> words = new ArrayList<>();
        WordTokenizer tokenizer = new WordTokenizer();
        String title = article.getTitle();
        //title = title.replaceAll("[^ a-zA-Z]", " ").replaceAll("\\s+", " ");


        this.tokenize(title.toLowerCase(),words);

        String text = article.getText();
        text = text.replaceAll("<ref.*</ref>"," ");
        text = text.replaceAll("https?:\\/\\/(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~#?&//=]*)"," ");
        text = text.replaceAll("[^ a-zA-Z]", " ").replaceAll("\\s+", " ");


        this.tokenize(text.toLowerCase(),words);

        return words;
    }

    private void tokenize(String s,ArrayList<String> words){
        WordTokenizer tokenizer = new WordTokenizer();
        tokenizer.tokenize(s);
        while(tokenizer.hasMoreElements()){
            String word = tokenizer.nextElement();
            if(stopWordsHandler.isStopword(word))
                continue;
            word = stem(word);

            words.add(word);
        }
    }


    private void tokenize(String s,String category){
        WordTokenizer tokenizer = new WordTokenizer(); //use word tokenizer
        tokenizer.tokenize(s);
        int index = classes.indexOf(category);
        while(tokenizer.hasMoreElements()){
            String word = tokenizer.nextElement();
            if(stopWordsHandler.isStopword(word)) //ignore stop words
                continue;
           String stemmedWord = stem(word); //stem the word

            HashSet<String> words = classDictionary.get(category); //get class vocabulary
            LinkedHashMap<String,Double> counts = wordCounts.get(category);//get hashmap to store counts
            if(counts.containsKey(stemmedWord))
            {
                double count = counts.get(stemmedWord);
                count++;
                counts.put(stemmedWord,count);
            }
            else
                counts.put(stemmedWord,1.0);
            if(!words.contains(stemmedWord)) //store only unique words
                words.add(stemmedWord);
            allWords.add(stemmedWord); //store all words including duplications
        }
    }
    private static String stem(String text){ //use Porter stemmer algorithm
        PorterStemmer stemmer = new PorterStemmer();
        stemmer.setCurrent(text.toLowerCase());
        stemmer.stem();
        return stemmer.getCurrent();
    }


}
