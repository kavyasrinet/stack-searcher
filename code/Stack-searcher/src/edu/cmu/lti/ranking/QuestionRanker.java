package edu.cmu.lti.ranking;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URISyntaxException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.javaml.*;
import net.sf.javaml.classification.evaluation.CrossValidation;
import net.sf.javaml.classification.evaluation.PerformanceMeasure;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.tools.weka.WekaClassifier;
import net.sf.javaml.*;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.params.ModifiableSolrParams;

import weka.classifiers.Classifier;
import weka.classifiers.functions.Logistic;
import weka.classifiers.functions.SMO;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import edu.cmu.lti.custom.GenerateQuery;
import edu.cmu.lti.pipeline.QuestionRetreivalBaseline;
import edu.stanford.nlp.util.ArrayUtils;

public class QuestionRanker
{
	SolrServer solr;
	Logistic l;
	 HashMap<Long, ArrayList<Double>> userInfo = new HashMap<Long, ArrayList<Double>>();
	 HashMap<String, ArrayList<Double>> userNameInfo = new HashMap<String, ArrayList<Double>>();

	public SolrDocument get_solr_doc( String post_id) throws SolrServerException
	{
		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set("qt", "/select");
		params.set("q", "Id:"+post_id);
		params.set("rows", "1");
		QueryResponse response = this.solr.query(params);
		return response.getResults().get(0);		
	}
	 
	public QuestionRanker(SolrServer solr) throws MalformedURLException, SolrServerException{
		this.solr = solr;
		SolrServer usersolr = new CommonsHttpSolrServer("http://localhost:8983/solr/travelusers/");
			SolrQuery solr_query = new SolrQuery("*:*");
			solr_query.setRows(21187);  

			QueryResponse response =  usersolr.query(solr_query);		
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

			if(doc.getFieldValue("score")!=null)
				feats.add((double)((ArrayList<Long>) doc.getFieldValue("score")).get(0));
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
	public ArrayList<Double> extract_features_pairwise(SolrDocument query, SolrDocument result)
	{
		ArrayList<Double> feats = new ArrayList<Double>();
		return feats;
	}
	
	public void train_model(String training_file) throws Exception
	{
		GenerateQuery generate_query = new GenerateQuery();
		HashMap<SolrDocument, ArrayList<SolrDocument>> training_data = QuestionRetreivalBaseline.querySolr(training_file, 100, solr, generate_query);
		HashMap<String, Set<String>> goldset = new HashMap<String, Set<String>>();
		for (String line : Files.readAllLines(Paths.get(training_file))) {
			String[] splits = line.trim().split("\t");
			Set<String> s = new HashSet<String>();
			for(int i=1;i<splits.length;i++)
				s.add(splits[i]);
			goldset.put(splits[0],s);
		}
		Dataset data = new DefaultDataset();
		Instances weka_data = null;

		for(Map.Entry<SolrDocument, ArrayList<SolrDocument>> entry : training_data.entrySet())
		{
			ArrayList<Double> query_feats = extract_features(entry.getKey());
			String query_id = String.valueOf(((ArrayList<Long>)  entry.getKey().getFieldValue("Id")).get(0));
			Instance i;

			for(SolrDocument result : entry.getValue())
			{
				String result_id = String.valueOf(((ArrayList<Long>)  result.getFieldValue("Id")).get(0));
				ArrayList<Double> result_feats = extract_features(result);
				result_feats.addAll(query_feats);
				result_feats.addAll(extract_features_pairwise(entry.getKey(), result));
				
				double[] feats = ArrayUtils.toPrimitive(result_feats.toArray(new Double[result_feats.size()]));
				i = new Instance(1.0, feats);
									
				
				if(weka_data == null)
				{
					FastVector atts = new FastVector();
					for(int feat_no = 0;feat_no < result_feats.size()-1;feat_no++)
						atts.addElement(new Attribute("attribute " + String.valueOf(feat_no)));
					FastVector classes = new FastVector();
					classes.addElement("1");
					classes.addElement("-1");
					atts.addElement(new Attribute("class",classes));
			        weka_data = new Instances("stack-searcher", atts, 25*goldset.size());
			        weka_data.setClassIndex(weka_data.numAttributes() - 1);
				}
				
				i.setDataset(weka_data);
				if(goldset.get(query_id).contains(result_id))
					i.setClassValue("1");					
				else
					i.setClassValue("-1");	
				weka_data.add(i);
			}
		}
		
		l = new Logistic();
		l.buildClassifier(weka_data);
	}
	
	public HashMap<SolrDocument, ArrayList<SolrDocument>>rerank(HashMap<SolrDocument, ArrayList<SolrDocument>> predicted_results) throws Exception
	{
		HashMap<SolrDocument, ArrayList<SolrDocument>> output = new HashMap<SolrDocument, ArrayList<SolrDocument>>();
		for(SolrDocument query: predicted_results.keySet())
		{
			ArrayList<Double> query_feats = extract_features(query);
			ArrayList<SolrDocument> results = predicted_results.get(query);
			HashMap<SolrDocument, Double> result_score = new HashMap<SolrDocument, Double>();
			for( SolrDocument result: results  )
			{
				ArrayList<Double> result_feats = extract_features(result);
				result_feats.addAll(query_feats);
				result_feats.addAll(extract_features_pairwise(query, result));
				double[] feats = ArrayUtils.toPrimitive(result_feats.toArray(new Double[result_feats.size()]));
				Instance i = new Instance(0, feats);
				double[] score = l.distributionForInstance(i);
				result_score.put(result, score[1]);
			}
				Collections.sort(results, new Comparator<SolrDocument>() {
				    @Override
				    public int compare(SolrDocument s1, SolrDocument s2) {
				        return Double.compare(result_score.get(s1), result_score.get(s2));
				    }
				});		
			output.put(query, results);
		}
		
		return output;	
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
