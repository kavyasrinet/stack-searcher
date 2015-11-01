package edu.cmu.lti.ranking;

import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;

import net.sf.javaml.*;
import net.sf.javaml.classification.Classifier;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.params.ModifiableSolrParams;

public class QuestionRanker
{

	Classifier C;
	 HashMap<Long, ArrayList<Double>> userInfo = new HashMap<Long, ArrayList<Double>>();
	 HashMap<String, ArrayList<Double>> userNameInfo = new HashMap<String, ArrayList<Double>>();
	 
	public QuestionRanker() throws MalformedURLException, SolrServerException{
		SolrServer solr = new CommonsHttpSolrServer("http://localhost:8983/solr/travelusers/");
			SolrQuery solr_query = new SolrQuery("*:*");
			solr_query.setRows(21187);  

			QueryResponse response =  solr.query(solr_query);		
    	    ArrayList<SolrDocument> s = response.getResults();
    	   
    	    for(SolrDocument doc: s){
    	    	ArrayList<Double> info = new ArrayList<Double>();
    	    	
    	    	long Id = ((ArrayList<Long>) doc.getFieldValue("Id")).get(0);
    	    	String name = ((ArrayList<String>) doc.getFieldValue("DisplayName")).get(0);
    	    	if(doc.containsKey("Reputation"))
    	    		info.add((double)((ArrayList<Long>) doc.getFieldValue("Reputation")).get(0));
    	    	else
    	    		info.add(null);
    	    	if(doc.containsKey("Views"))
    	    		info.add((double)((ArrayList<Long>) doc.getFieldValue("Views")).get(0));
    	    	else
    	    		info.add(null);
    	    	if(doc.containsKey("UpVotes"))
    	    		info.add((double)((ArrayList<Long>) doc.getFieldValue("UpVotes")).get(0));
    	    	else
    	    		info.add(null);
    	    	if(doc.containsKey("DownVotes"))
    	    		info.add((double)((ArrayList<Long>) doc.getFieldValue("DownVotes")).get(0));
    	    	else
    	    		info.add(null);
    	    	userInfo.put(Id, info);
    	    	userNameInfo.put(name, info);
    	    }
		    
		
	}

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
		 * 7. User's reputation
		 * 8. User's #views
		 * 9. User's Upvotes
		 * 10. User's Downvotes
		 */
		ArrayList<Double> feats = new ArrayList<Double>();
		ArrayList<Double> nones = new ArrayList<Double>();
		nones.add(null);
		nones.add(null);
		nones.add(null);
		nones.add(null);
		if(((ArrayList<Long>) doc.getFieldValue("PostTypeId")).get(0)==1){
			
			
			if(doc.containsKey("Score"))
				feats.add((double)((ArrayList<Long>) doc.getFieldValue("Score")).get(0));
			else
				feats.add(null);
			if(doc.containsKey("ViewCount"))
				feats.add((double)((ArrayList<Long>) doc.getFieldValue("ViewCount")).get(0));
			else
				feats.add(null);
			if(doc.containsKey("AnswerCount"))
				feats.add((double)((ArrayList<Long>) doc.getFieldValue("AnswerCount")).get(0));
			else
				feats.add(null);
			if(doc.containsKey("CommentCount"))
				feats.add((double)((ArrayList<Long>) doc.getFieldValue("CommentCount")).get(0));
			else
				feats.add(null);
			if(doc.containsKey("FavoriteCount"))
				feats.add((double)((ArrayList<Long>)doc.getFieldValue("FavoriteCount")).get(0));
			else
				feats.add(null);
			
			if(doc.getFieldValue("AcceptedAnswerId")!=null)
				feats.add(1.0);
			else
				feats.add(0.0);
			if(doc.containsKey("OwnerUserId")){
				long Id = ((ArrayList<Long>) doc.getFieldValue("OwnerUserId")).get(0);
				feats.addAll(userInfo.get(Id));
			}
			else{
				if(doc.containsKey("OwnerDisplayName")){
					 String userName = ((ArrayList<String>) doc.getFieldValue("OwnerDisplayName")).get(0);
					 if(userNameInfo.containsKey(userName))
						 feats.addAll(userNameInfo.get(userName));
					 else
						 feats.addAll(nones); 
				}
				else
					feats.addAll(nones);
				
			}				
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
