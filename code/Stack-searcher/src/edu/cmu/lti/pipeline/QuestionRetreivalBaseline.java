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

import edu.cmu.lti.custom.ExtractKeyword;
import edu.cmu.lti.search.RetrievalResult;
/**
 * @author Kavya Srinet.
 */
public class QuestionRetreivalBaseline {
	static HashSet<String> stopwords = new HashSet<String>();
    public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException, SolrServerException {
    	QuestionRetreivalBaseline qrb = new QuestionRetreivalBaseline();
    	Evaluate evaluator = new Evaluate();
    	BufferedReader reader = new BufferedReader(new FileReader(new File("dataset_sample/stopwords.txt")));
    	String line ="";
    	while((line=reader.readLine())!=null){
    		line = line.trim();
    		stopwords.add(line);
    	}
    	HashMap<String, ArrayList<String>> predicted_results = qrb.querySolr(10);
    	//predicted_results = rerank_results(predicted_results);
    	
    	System.out.println("mAP Score = " + evaluator.getMapScore(predicted_results));
    	System.out.println("mrr Score = " + evaluator.getMrrScore(predicted_results));
       	System.out.println("Recall Score = " + evaluator.getRecall(predicted_results));
    	System.out.println("P@1 Score = " + evaluator.getPAtK(predicted_results,1));
    	System.out.println("P@5 Score = " + evaluator.getPAtK(predicted_results,5));
    }
    
    private  String generateQuery(String line) throws IOException
    {
    	ExtractKeyword e = new ExtractKeyword();
    	String[] parts = line.split("\t");
      	
        String title = parts[1].trim();
        title = title.replaceAll("[^A-Za-z0-9 ]", ""); 
        //title = e.getKeywords(title, stopwords);
        String body = parts[2].trim();
        return title;
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
    public HashMap<String, ArrayList<String>> querySolr(int resultSetSize) throws IOException, URISyntaxException, SolrServerException{
        SolrServer solr = new CommonsHttpSolrServer("http://128.237.164.54:8983/solr/travelstack/");
	    

       BufferedReader reader = new BufferedReader(new FileReader(new File("dataset_sample/question_queries.txt")));

        String line = null;
        String qid ;

        int j=0;
        
        HashMap<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();
      
        while((line=reader.readLine())!=null && (j++<5000)){
        	if(j%10 == 0)
        	{
	        	qid = line.split("\t")[0];
	        	if(!map.containsKey(qid))
	        	{
		        	ArrayList<RetrievalResult> results = new ArrayList<>();
		        	String query = generateQuery(line);
		            ArrayList<String> list = new ArrayList<String>();
		    	    ModifiableSolrParams params = new ModifiableSolrParams();
		    	    params.set("qt", "/select");
		    	    String solr_query ="";
		    	    for(String query_split : query.split(" "))
		    	    	solr_query +=  "+" + query_split;
		    	    params.set("q", solr_query);
		    	    params.set("rows", resultSetSize);
		    		
		    	    
		    	    QueryResponse response = solr.query(params);
		    	    ArrayList<SolrDocument> s = response.getResults();
		    	    for(SolrDocument sd: response.getResults())
		    	    {
		    	    	ArrayList<Long> id = (ArrayList<Long>)  sd.getFieldValue("Id");
		    	    	ArrayList<Long> posttype = (ArrayList<Long>) sd.getFieldValue("PostTypeId");
		    	    	if(posttype.get(0) == 1)
		    	    		list.add(id.get(0).toString());	
		    	    }
	            	map.put(qid, list);
		            }
	        	System.out.println(j);
        	}

        }        
    	reader.close();
    	return map;
    }
}