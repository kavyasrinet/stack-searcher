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

import edu.cmu.lti.evaluation.Evaluate;
import edu.cmu.lti.search.BingSearchAgent;
import edu.cmu.lti.custom.ExtractKeyword;
import edu.cmu.lti.search.RetrievalResult;
/**
 * @author Kavya Srinet.
 */
public class QuestionRetreivalBaseline {
	static HashSet<String> stopwords = new HashSet<String>();
    public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException {
    	QuestionRetreivalBaseline qrb = new QuestionRetreivalBaseline();
    	Evaluate evaluator = new Evaluate();
    	BufferedReader reader = new BufferedReader(new FileReader(new File("dataset_sample/stopwords.txt")));
    	String line ="";
    	while((line=reader.readLine())!=null){
    		line = line.trim();
    		stopwords.add(line);
    	}
    	HashMap<String, ArrayList<String>> predicted_results = qrb.crawlBing(10);
    	predicted_results = rerank_results(predicted_results);
    	
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
        title = title.replaceAll("\\&", ""); 
        title = e.getKeywords(title, stopwords);
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
    public HashMap<String, ArrayList<String>> crawlBing(int resultSetSize) throws IOException, URISyntaxException{
       
       BufferedReader reader = new BufferedReader(new FileReader(new File("dataset_sample/question_queries.txt")));

        String line = null;
        String accountKey = "5B9+TEUKn+w9SoNRoZVYgVh64sgRqRrrvB1dDxSYvg0=";
        BingSearchAgent bsa = new BingSearchAgent();
        bsa.initialize(accountKey);
        bsa.setResultSetSize(resultSetSize);
        String qid ;

        int j=0;
        
        HashMap<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();
      
        while((line=reader.readLine())!=null && (j++<50)){
        	if(j%10 == 0)
        	{
	        	qid = line.split("\t")[0];
	        	if(!map.containsKey(qid))
	        	{
		        	ArrayList<RetrievalResult> results = new ArrayList<>();
		        	String query = generateQuery(line);
		        	
		            results.addAll(bsa.retrieveDocuments(qid, "site:travel.stackexchange.com "+query));
		            
		            ArrayList<String> list = getRelatedQuestions(results);
		            
		            
		            
	            	map.put(qid, list);
		            }
	        	System.out.println(j);
        	}

        }        
    	reader.close();
    	return map;
    }

	private ArrayList<String> getRelatedQuestions(ArrayList<RetrievalResult> results)
	{
        ArrayList<String> list = new ArrayList<String>();
        
        for(int i=1;i<results.size();i++){
        	RetrievalResult r = results.get(i);
        	String url = r.getUrl();        
    		String[] p = url.split("/");
    		if(p.length > 4 && p[3].equals("questions") && p[4].matches("[0-9]+") )
        		list.add(p[4]);  	
        }
        return list;
	}

}