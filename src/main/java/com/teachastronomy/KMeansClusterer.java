package com.teachastronomy;

import com.teachastronomy.classifiers.TrainingDataHelper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.SimpleFSDirectory;
import weka.clusterers.SimpleKMeans;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * Created by root on 10/27/16.
 */
public class KMeansClusterer {

    IndexReader reader;
    SimpleKMeans clusterer;
    public KMeansClusterer(){
        try {
            StandardAnalyzer analyzer = new StandardAnalyzer();
            reader = DirectoryReader.open(new SimpleFSDirectory(Paths.get(Constants.MainIndexLocation + "/astronomyIndex")));
            clusterer = new SimpleKMeans();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void cluster() throws IOException{
        ArrayList<weka.core.Attribute> attributes = TrainingDataHelper.getAttributes();
        attributes.remove(2);
        try {
            Instances dataset = new Instances("dataset", attributes, 0);
            for (int i = 0; i < reader.maxDoc(); i++) {
                Instance inst = new DenseInstance(2);
                inst.setDataset(dataset);
                Document d = reader.document(i);
                String title = d.get("title");
                String text = TrainingDataHelper.removeStopWords(d.get("text"));
                text = text.replace("{"," ").replace("["," ").replace("]"," ").replace("}", " ");
                inst.setValue(0,title);
                inst.setValue(1,text);
                dataset.add(inst);
            }
            StringToWordVector filter = new StringToWordVector();
            //     filter.setTokenizer(tokenizer);
            filter.setInputFormat(dataset);
            //      filter.setWordsToKeep(1000000);
            //     filter.setDoNotOperateOnPerClassBasis(true);
            filter.setLowerCaseTokens(true);

            Instances outputInstances = Filter.useFilter(dataset,filter);
            clusterer.setSeed(10);

            //important parameter to set: preserver order, number of cluster.
            clusterer.setPreserveInstancesOrder(true);
            clusterer.setNumClusters(5);
            clusterer.buildClusterer(outputInstances);
            System.out.println("Clusters - "+clusterer.getNumClusters());
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            reader.close();
        }
    }
}
