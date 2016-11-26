package com.teachastronomy.classifiers;

import com.teachastronomy.wikipedia.WikiArticle;

/**
 * Created by root on 10/27/16.
 */
public interface IClassifier {

    void trainUsingWEKA();
    ClassificationResult classify(WikiArticle article);
}
