package edu.cmu.lti.custom;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;
/**
 * @author Kavya Srinet.
 */

public class GenerateQuery {
    
    public static void main(String[] args) throws URISyntaxException, IOException {
    	GenerateQuery e = new GenerateQuery();
    	MaxentTagger tagger = new MaxentTagger("taggers/english-left3words-distsim.tagger");
//    	e.getPOS("Hello my name is Kavya", new HashSet<String>(), tagger);
//    	e.getPOS("Hello my name is Tada", new HashSet<String>(), tagger);
    	e.getNGrams("Hello my name is Kavya",1);
    
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
    
  //given n , this function returns back an arrayList of n-grams.
  //  The value of n>= 2
  public ArrayList<String> getNGrams(String text, int n){	  
	  ArrayList<String> bigrams = new ArrayList<String>();
	  String[] unigrams = text.split("\\s+");
	  int len = unigrams.length;
	  int i=0;
	  boolean flag = true;
	  while(i<len-1){
		  
		  String cand = unigrams[i];
		  for(int j=1;j<n;j++){
			  if(i+j < len)
				  cand  = cand+ " "+unigrams[i+j];
			  else{
				  flag = false;
				  break;
			  }
		  }
		  if(flag==true)
			  bigrams.add(cand);
		  else
			  return bigrams;
		  
		  i++;
	  }
	  return bigrams;
  }
    
    public String getPOS(String title, HashSet<String> stopwords, MaxentTagger tagger){
    	
    	
       	String tagged = tagger.tagString(title);
    	String out = "";
    	String[] parts = tagged.split("\\s+");
    	for(String s: parts){
    		String tag = s.split("_")[1];
    		if(tag.equals("NNP") || tag.equals("JJ"))
    			out = out+" "+s.split("_")[0];
    	}
    	return out;
    }
    
    public String addTags(String title, String tagList){
    	String[] tags = tagList.trim().split("\\s+");
    	String s =title.toLowerCase().trim();
    	for(String tag: tags){
    		tag = tag.trim().toLowerCase();
    		s = s + " "+tag;
    	}
    	return s.trim();   	
    }
    
    public String appendBody(String title, String body){
    	//appends title and last sentence of body
    	String[] sentences = body.split(".");
    	
    	return title+ " "+sentences[sentences.length-1];
    }
    
    
    
}