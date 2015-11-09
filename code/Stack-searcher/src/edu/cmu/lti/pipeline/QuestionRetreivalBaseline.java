package edu.cmu.lti.pipeline;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.params.ModifiableSolrParams;

import edu.cmu.lti.evaluation.Evaluate;
import edu.cmu.lti.custom.GenerateQuery;
import edu.cmu.lti.search.RetrievalResult;
import edu.cmu.lti.custom.*;
import edu.cmu.lti.ranking.*;


public class QuestionRetreivalBaseline {
	
	public static HashSet<String> stopwords = new HashSet<String>();
	
	public static void main(String[] args) throws Exception {
   
				
    	GenerateQuery generate_query = new GenerateQuery();

		SolrServer solr = new CommonsHttpSolrServer("http://localhost:8983/solr/travelstackexchange/");

    	QuestionRanker ranker = new QuestionRanker(solr);
    	ranker.train_model( "dataset_sample/train.txt");


		QuestionRetreivalBaseline qrb = new QuestionRetreivalBaseline();
    	BufferedReader reader = new BufferedReader(new FileReader(new File("dataset_sample/stopwords.txt")));
    	String line ="";
    	while((line=reader.readLine())!=null){
    		line = line.trim();
    		stopwords.add(line);
    	}
    	
    	
    	String query_file = "dataset_sample/val.txt";

    	
    	HashMap<SolrDocument, ArrayList<SolrDocument>> docs = qrb.querySolr(query_file,100, solr, generate_query);
    	
    	docs = ranker.rerank(docs);
    	
    	System.out.println("Evaluating\n");
    	HashMap<String, ArrayList<String>> predicted_results = retreivedIds(docs);
    	Evaluate evaluator = new Evaluate(query_file);
    	
    	System.out.println("mAP Score = " + evaluator.getMapScore(predicted_results));
    	System.out.println("mrr Score = " + evaluator.getMrrScore(predicted_results));
       	System.out.println("Recall Score = " + evaluator.getRecall(predicted_results));
    	System.out.println("P@1 Score = " + evaluator.getPAtK(predicted_results,1));
    	System.out.println("P@5 Score = " + evaluator.getPAtK(predicted_results,5));
    }
    
	public static HashMap<String, ArrayList<String>> retreivedIds(HashMap<SolrDocument, ArrayList<SolrDocument>> docs){
		HashMap<String, ArrayList<String>> predicted_results = new HashMap<String, ArrayList<String>>(); 
		for(SolrDocument key : docs.keySet()){
    		ArrayList<String> ids = new ArrayList<String>();    		
    		ArrayList<SolrDocument> sd = docs.get(key);
    		for(SolrDocument doc : sd){
    			ArrayList<Long> id = (ArrayList<Long>)  doc.getFieldValue("Id");
    			ids.add(id.get(0).toString());
    		}
			ArrayList<Long> id = (ArrayList<Long>)  key.getFieldValue("Id");
    		predicted_results.put(id.get(0).toString(), ids);
    	}
		return predicted_results;
	}
	
	public static HashMap<String, String>get_post(String postid,  SolrServer solr) throws SolrServerException
	{	
		ModifiableSolrParams params = new ModifiableSolrParams();
	    params.set("qt", "/select");
		params.set("q", "Id:"+postid);
		QueryResponse response = solr.query(params);
		
		SolrDocument sd  =  response.getResults().get(0);
		
		HashMap<String, String> result = new HashMap<String, String>();
		for (String field: sd.getFieldNames())
		{
			if((!field.equals("id")) && (!field.equals("_version_")))
				result.put(field,((ArrayList)sd.getFieldValue(field)).get(0).toString());
		}
		return result;
	}
    
	private static  String generateQuery(String postId,SolrServer solr, GenerateQuery generate_query) throws IOException, SolrServerException
    {
    	String query = "";
    	HashMap<String, String> postAttb  = get_post(postId, solr);

      	String question_id = postId;
      	
      	String title = postAttb.get("Title");
        String body = postAttb.get("Body");
        String tags = postAttb.get("Tags");

        query = title;
        //Use the top k bigrams containing map for the following
        /*
         * Initialize TfidfTerms and call the function to get top k bigrams
         * 
         */

       
//        final HashMap<String,ArrayList<String>> doc_attributes = TfidfTerms.doc_attributes;
//        HashMap<String,Double> map = TfidfTerms.top_terms(2, 5, postId);
//        for (String s:map.keySet()) {
//        	System.out.println(s);
//        }
//        ArrayList<String> res = doc_attributes.get(postId);
//        query = gq.getRequestUsingBigrams(res.get(0)+" "+res.get(1), map);

//        query = title;
//        for (String s:map.keySet()) {
//        	query = query + " " + s;
//        }
        
//        System.out.println(res.get(0));
//        System.out.println(res.get(1));
      //    query = e.getRequestUsingBigrams(title+" "+body, map);
      //   query = e.getKeywords(title, stopwords);
       // query = title+ " "+e.getPOS(title+ " "+body, stopwords);
       // query = e.addTags(title, tags);

     //  query = e.appendBody(title, body);

        //Use the top k bigrams containing map for the folowing
      //    query = generate_query.getRequestUsingBigrams(title+" "+body, map);
      //   query = generate_query.getKeywords(title, stopwords);
       // query = title+ " "+generate_query.getPOS(title+ " "+body, stopwords);
       // query = generate_query.addTags(title, tags);
        //generate_query.appendBody(title, body)
       query = title + " " + tags; 
        return query.replaceAll("[^A-Za-z0-9 ']", " ").trim();
    }
    
    public static HashMap<String,ArrayList<String>> rerank_results(HashMap<String,ArrayList<String>> predicted_results) throws IOException, InterruptedException
    {   		
    	
    	PrintWriter writer = new PrintWriter("input_ids.txt", "UTF-8");
    	for(Entry<String,ArrayList<String>> e  :predicted_results.entrySet())
		{
    		String qid = e.getKey();
	    	writer.println(qid);
	    	for(String result : e.getValue()) {
	    		writer.println(result);
	    	}
	    	writer.println("###");
		}
    	writer.close();
    	Process p = Runtime.getRuntime().exec("python rerank.py");
    	p.waitFor();
    	
    	HashMap<String,ArrayList<String>> reranked_ids = new HashMap<String,ArrayList<String>>();
    	boolean new_query = true;
    	String current_qid = "";
    	for (String line : Files.readAllLines(Paths.get("reranked_ids.txt"))) {
    		String id = line.trim();
    		if(id.equals("###"))
    		{
    			new_query = true;
    		}
    		else if(new_query)
    		{
    			reranked_ids.put(id, new ArrayList<String>());
    			current_qid = id;
    			new_query = false;
    		}
    		else
    			reranked_ids.get(current_qid).add(id);
    	}
    	return reranked_ids;
    }
   
    

    /*
     * This function gets the first 100 lines of the Posts.xml file and 
     * crawls the web using BingSearchAPI and returns back a hashmap that contains the PostId
     * as the key and a list of related PostIds as per Bing
     */
    public static HashMap<SolrDocument, ArrayList<SolrDocument>> querySolr(String query_file, int resultSetSize, SolrServer solr, GenerateQuery generate_query) throws IOException, URISyntaxException, SolrServerException{
        
       BufferedReader reader = new BufferedReader(new FileReader(new File(query_file)));

        String line = null;
        String qid ;

        int j=0;
        
        HashMap<SolrDocument, ArrayList<SolrDocument>> map = new HashMap<SolrDocument, ArrayList<SolrDocument>>();
      
        while((line=reader.readLine())!=null){
        		j++;
        		qid = line.split("\t")[0];
    			ModifiableSolrParams params = new ModifiableSolrParams();
    			params.set("qt", "/select");
    			params.set("q", "Id:"+qid);
    			params.set("rows", "1");
    			QueryResponse response = solr.query(params);
    			SolrDocument qid_solrdoc =  response.getResults().get(0);		
    			
        		ArrayList<RetrievalResult> results = new ArrayList<>();
	        	
	        	String query = "PostTypeId=1 " + generateQuery(qid, solr, generate_query);
	            ArrayList<SolrDocument> list = new ArrayList<SolrDocument>();
	    	    params = new ModifiableSolrParams();
	    	    params.set("qt", "/select");
	    	    params.set("fl", "*, score");

	    	    String solr_query ="";
	    	    for(String query_split : query.split(" "))
	    	    	solr_query +=  "+" + query_split;
	    	    
	    	    params.set("q", solr_query);
	    	    
	    	    params.set("rows", String.valueOf(resultSetSize));

	    	    try {


	    	    response = solr.query(params);
	    	    ArrayList<SolrDocument> s = response.getResults();
	    	    
	    	    for(int i=1;i<s.size();i++)
	    	    {	
	    	    	SolrDocument sd = s.get(i);
	    	    	ArrayList<Long> id = (ArrayList<Long>)  sd.getFieldValue("Id");
	    	    	ArrayList<Long> posttype = (ArrayList<Long>) sd.getFieldValue("PostTypeId");
	    	    	if(posttype.get(0) == 1)
	    	    		list.add(s.get(i));//id.get(0).toString());	
	    	    }
            	map.put(qid_solrdoc, list);

	        	System.out.println(j);
	    	    } catch (Exception e) {
	    	    	System.out.println("Query failed in solr.");
	    	    	System.out.println(String.format("Skipped query: %s", solr_query));		
	    	    }
        }        
    	reader.close();
    	return map;
    }
}
