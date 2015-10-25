package edu.cmu.lti.custom;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
/**
 * @author Kavya Srinet.
 */

public class GenerateQuery {
    
    public static void main(String[] args) throws URISyntaxException, IOException {
    	GenerateQuery e = new GenerateQuery();
    //	String s = e.getKeywords("What happens with checked luggage with an airport change?");
    
    }
    public String getKeywords(String s, HashSet<String> stopwords) throws IOException{
    	s = s.toLowerCase();
    	
    	String[] parts = s.split(" ");
    	String updated = "";
    	for(String p : parts){
    		if(!stopwords.contains(p))
    			updated = updated + " " + p;    		
    	}
    	updated = updated.trim();
    	
    	return updated;
    }
    
    public String getNER(String title, HashSet<String> stopwords){
    	return "";
    }
    
    public String addTags(String title, ArrayList<String> tags){
    	String s =title.toLowerCase().trim();
    	for(String tag: tags){
    		tag = tag.trim().toLowerCase();
    		s = s + " "+tag;
    	}
    	return s;
    	
    }
    
    public String appendBody(String title, String body){
    	return title+ " "+body;
    }
    
    
    
}