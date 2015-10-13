package edu.cmu.lti.pipeline;

import java.awt.List;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import java.io.File;

import org.apache.lucene.queryparser.flexible.core.util.StringUtils;

import edu.cmu.lti.search.BingSearchAgent;
import edu.cmu.lti.search.RetrievalResult;
/**
 * @author Kavya Srinet.
 */
public class QuestionRetreivalBaseline {
    private static final int ArrayList = 0;
    private static final int String = 0;
    public static void main(String[] args) throws URISyntaxException, IOException {
    	QuestionRetreivalBaseline qrb = new QuestionRetreivalBaseline();
    	HashMap<String, ArrayList<String>> map = qrb.crawlBing(10);
    }
    
    /*
     * This function gets the first 100 lines of the Posts.xml file and 
     * crawls the web using BingSearchAPI and returns back a hashmap that contains the PostId
     * as the key and a list of related PostIds as per Bing
     */
    public HashMap<String, ArrayList<String>> crawlBing(int resultSetSize) throws IOException, URISyntaxException{
       
       BufferedReader reader = new BufferedReader(new FileReader(new File("dataset_sample/question_queries.txt")));

        String line = null;
        String accountKey = "74mM12fgn5KdVok+J7bKHkPybKZjBHh8asx+91JkwdI";
        BingSearchAgent bsa = new BingSearchAgent();
        bsa.initialize(accountKey);
        bsa.setResultSetSize(resultSetSize);
        String qid ="";
        String question="";
        String ln2 = null;
        int j=0;
        
        HashMap<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();
        while((line=reader.readLine())!=null && j<=100){
        	
        	ArrayList<RetrievalResult> results = new ArrayList<>();
          String[] parts = line.split("\t");
          	qid = parts[0];
            String title = parts[1].trim();
            title = title.replaceAll("\\&", ""); 
            String body = parts[2].trim();
            results.addAll(bsa.retrieveDocuments(qid, "site:travel.stackexchange.com "+title));
            ArrayList<String> list = new ArrayList<String>();
            for(int i=0;i<results.size();i++){
            	RetrievalResult r = results.get(i);
            	String url = r.getUrl();
            	String[] p = url.split("/");
            	if(p[3].equals("questions"))
            		list.add(p[4]);  	
            	else
            		continue;
            }
            if(!map.containsKey(qid)){
            	map.put(qid, list);
            }
            j++;
        }
        
        
          return map;

        
  
    }
}
