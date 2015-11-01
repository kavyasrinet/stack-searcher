package edu.cmu.lti.ranking;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

import net.sf.javaml.*;
import net.sf.javaml.classification.Classifier;

import org.apache.solr.common.SolrDocument;

public class QuestionRanker
{

	Classifier C;

	public HashMap<String, ArrayList<SolrDocument>>load_training_data(String training_file)
	{	
		return null;	
	}
	
	public ArrayList<Double> extract_features(SolrDocument doc)
	{
		/*
		 * Features are:
		 * 1. Score
		 * 2. ViewCount
		 * 3. AnswerCount
		 * 4. Favorite Count
		 * 5. Comment Count
		 * 6. AcceptedAnswerId - binary
		 */
		ArrayList<Double> feats = new ArrayList<Double>();
		//ArrayList<Long> postType = (ArrayList<Long>) doc.getFieldValue("PostTypeId");
		if(((ArrayList<Long>) doc.getFieldValue("PostTypeId")).get(0)==1){
			if(doc.containsKey("Score"))
				feats.add((double)((ArrayList<Long>) doc.getFieldValue("Score")).get(0));
			else
				feats.add(0.0);
			if(doc.containsKey("ViewCount"))
				feats.add((double)((ArrayList<Long>) doc.getFieldValue("ViewCount")).get(0));
			else
				feats.add(0.0);
			if(doc.containsKey("AnswerCount"))
				feats.add((double)((ArrayList<Long>) doc.getFieldValue("AnswerCount")).get(0));
			else
				feats.add(0.0);
			if(doc.containsKey("CommentCount"))
				feats.add((double)((ArrayList<Long>) doc.getFieldValue("CommentCount")).get(0));
			else
				feats.add(0.0);
			if(doc.containsKey("FavoriteCount"))
				feats.add((double)((ArrayList<Long>)doc.getFieldValue("FavoriteCount")).get(0));
			else
				feats.add(0.0);
			
			
			if(doc.getFieldValue("AcceptedAnswerId")!=null)
				feats.add(1.0);
			else
				feats.add(0.0);
		}
		return feats;
	}
	
	public void train_model(String training_file)
	{

	}
	
	public HashMap<String, ArrayList<SolrDocument>>rank()
	{
		return null;
	}
	
	public ArrayList<ArrayList<Double>> getFeaturesFromPosts(HashMap<String, ArrayList<SolrDocument>> mapResults){
		ArrayList<ArrayList<Double>> features = new ArrayList<ArrayList<Double>>();
		for(String id: mapResults.keySet()){
			for(SolrDocument doc: mapResults.get(id)){
				features.add(extract_features(doc));
			}
		}
		return features;
	}
	

}
