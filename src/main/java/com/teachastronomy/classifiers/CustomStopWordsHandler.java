package com.teachastronomy.classifiers;

import com.teachastronomy.Constants;
import weka.core.stopwords.StopwordsHandler;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;

/**
 * Created by root on 11/1/16.
 */
public class CustomStopWordsHandler implements StopwordsHandler {
    HashSet<String> stopWords;

    public CustomStopWordsHandler(){
        stopWords = new HashSet<>();
        try{
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
    }

    @Override
    public boolean isStopword(String s) {
        if(stopWords.contains(s))
            return true;
        return false;
    }
}
