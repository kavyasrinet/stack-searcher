package edu.cmu.lti.ranking;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import net.sf.javaml.*;
import net.sf.javaml.classification.Classifier;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.params.ModifiableSolrParams;

public class QuestionRanker
{
	SolrServer solr;
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

	public HashMap<String, ArrayList<SolrDocument>>load_training_data(String training_file) throws IOException, SolrServerException
	{	
		HashMap<String, ArrayList<SolrDocument>> training_data = new HashMap<String, ArrayList<SolrDocument>>();
		
		for (String line : Files.readAllLines(Paths.get(training_file))) {
			String [] splits = line.split("\t");
			String q_id = splits[0];
			ArrayList<SolrDocument> solr_doclist = new ArrayList<SolrDocument>();
			for(int j=1;j<splits.length;j++)
			{
				ModifiableSolrParams params = new ModifiableSolrParams();
				params.set("qt", "/select");
				params.set("q", "Id:"+splits[j]);
				params.set("rows", "1");
				QueryResponse response = this.solr.query(params);
				solr_doclist.add(response.getResults().get(0));
			}
			training_data.put(q_id, solr_doclist);
		}
		return training_data;
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
	
	public ArrayList<ArrayList<Double>> getAllFeatures(HashMap<String, ArrayList<SolrDocument>> mapResults){
		ArrayList<ArrayList<Double>> features = new ArrayList<ArrayList<Double>>();
		for(String id: mapResults.keySet()){
			for(SolrDocument doc: mapResults.get(id)){
				features.add(extract_features(doc));
			}
		}
		return features;
	}
	

}
