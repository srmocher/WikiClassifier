package com.teachastronomy.transformers;

import java.util.Hashtable;
import java.util.List;

/**
 * Created by sridh on 12/30/2016.
 */
public class TfIdfTransformer {

    public double getTf(Hashtable<String,Integer> document, String term){
        double result = 0;
        Integer count = document.get(term);
        if(count!=null)
            result = count;
        else
            result=0;
        int sz=0;
        for(int cnt:document.values())
            sz = sz + cnt;
        double tf = (double)result / (double)sz;
        if(Double.isNaN(tf))
            System.out.println(tf);
        return Math.log(1+tf);
    }

    public double getIdf(List<Hashtable<String,Integer>> docs, String term) {
        double n = 0;
        for(Hashtable<String,Integer> doc:docs){
            if(doc.containsKey(term))
                n++;
        }
        double idf =  Math.log(docs.size() / n);

        return idf;
    }

    public double tfIdf(Hashtable<String,Integer> doc, List<Hashtable<String,Integer>> docs, String term) {
        double tf = getTf(doc, term);
        if(tf==0)
            return 0;
        double idf =  getIdf(docs, term);
        double tfidf = tf*idf;
        if(Double.isNaN(tfidf))
            System.out.println(term);
        return tfidf;
        //return 1;
    }
}
