package edu.cmu.lti.ranking;

import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import net.sf.javaml.*;
import net.sf.javaml.classification.Classifier;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.params.ModifiableSolrParams;

import edu.cmu.lti.custom.GenerateQuery;

public class QuestionRanker
{
	SolrServer solr;
	Classifier C;

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
	
	public ArrayList<Double> extract_features(SolrDocument doc, SolrDocument q)
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
			
			// Compute semantic dependency model score
			String query_content = "";
			String document_content = "";
			if(q.containsKey("Title"))
				query_content += doc.getFieldValue("Title");
			if(q.containsKey("Body"))
				query_content += " " + doc.getFieldValue("Body");
			if(doc.containsKey("Title"))
				document_content += doc.getFieldValue("Title");
			if(doc.containsKey("Body"))
				document_content += " " + doc.getFieldValue("Body");
			feats.add(sdm_score(query_content,document_content));
		}
		return feats;
	}
	
	public Double overlap_countO(Set<String> src, ArrayList<String> tar) {
		Double overlap_score = 0.0;
		HashMap<String,Double> tarCount = new HashMap<String,Double>();
		for (String s: tar) {
			if (tarCount.get(s) != null) {
				tarCount.put(s, tarCount.get(s)+1.0);
			} else {
				tarCount.put(s, 1.0);
			}
		}
		for (String s: src) {
			if (tarCount.get(s) != null) {
				overlap_score += tarCount.get(s);
			} 
		}
		return overlap_score;
	}
	
	public Double overlap_countU(Set<String> src, ArrayList<Set<String>> tar) {
		Double overlap_score = 0.0;
		for (String b: src) {
			String[] bigram = b.split("\\s+");
			String q1 = bigram[0];
			String q2 = bigram[1];
			for (Set<String> win: tar) {
				if (win.contains(q1) & win.contains(q2)) {
					overlap_score += 1.0;
				}
			}
		}
		return overlap_score;
	}
	
	public Double sdm_score(String query_raw, String document_raw) {
		Double unigramsScore;
		Double bigramsOScore;
		Double bigramsUScore;
		int w = 8; // window for unordered computation. 
		
		String query = (query_raw).replaceAll("[^a-zA-Z0-9\\s\\']", " ");
		String document = (document_raw).replaceAll("[^a-zA-Z0-9\\s\\']", " ");
		
		// compute similarity for unigrams
		ArrayList<String> unigramsQ = (ArrayList<String>) Arrays.asList((query).split("\\s+"));
		if (unigramsQ == null) {
			return 0.0;
		} else {
			Set<String> uniqueUnigramsQ = new HashSet<String>(unigramsQ); 
			ArrayList<String> unigramsD = (ArrayList<String>) Arrays.asList((document).split("\\s+"));
			unigramsScore = overlap_countO(uniqueUnigramsQ,unigramsD);
		}
		// compute similarity for bigrams
		ArrayList<String> bigramsQ = GenerateQuery.getNGrams(query, 2);
		if (bigramsQ == null) {
			return 0.0;
		} else {
			// exact 'ordered' match
			Set<String> uniqueBigramsQ = new HashSet<String>(bigramsQ);
			ArrayList<String> bigramsD = GenerateQuery.getNGrams(document, 2);
			bigramsOScore = overlap_countO(uniqueBigramsQ,bigramsD);
			
			// 'unordered' match within window (default w = 8)
			ArrayList<String> windowD = GenerateQuery.getNGrams(document, w);
			ArrayList<Set<String>> windowSetD = new ArrayList<Set<String>>();
			for (String window: windowD) {
				String[] windowTerms = window.split("\\s+");
				Set<String> currSet = new HashSet<String>(Arrays.asList(windowTerms)); 
				windowSetD.add(currSet);
			}
			bigramsUScore = overlap_countU(uniqueBigramsQ,windowSetD);
		}
		return (unigramsScore + bigramsOScore + bigramsUScore);
		
	}
	
	public void train_model(String training_file)
	{

	}
	
	public HashMap<String, ArrayList<SolrDocument>>rank()
	{
		return null;
	}
	
	public ArrayList<ArrayList<Double>> getFeaturesFromPosts(HashMap<String, ArrayList<SolrDocument>> mapResults, SolrDocument query){
		ArrayList<ArrayList<Double>> features = new ArrayList<ArrayList<Double>>();
		for(String id: mapResults.keySet()){
			for(SolrDocument doc: mapResults.get(id)){
				features.add(extract_features(doc,query));
			}
		}
		return features;
	}
	

}
