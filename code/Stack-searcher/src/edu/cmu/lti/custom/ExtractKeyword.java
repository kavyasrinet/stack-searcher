package edu.cmu.lti.custom;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
/**
 * @author Kavya Srinet.
 */

public class ExtractKeyword {
    
    public static void main(String[] args) throws URISyntaxException, IOException {
    	ExtractKeyword e = new ExtractKeyword();
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
    
}