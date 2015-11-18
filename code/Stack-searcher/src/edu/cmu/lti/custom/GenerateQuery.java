package edu.cmu.lti.custom;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;

/**
 * @author Kavya Srinet.
 */


public class GenerateQuery {
	MaxentTagger tagger;
	//bigram_best = 
	public GenerateQuery(){
		tagger = new MaxentTagger("taggers/english-left3words-distsim.tagger");
		//init_best_bigrams("dataset/all_posts.txt","dataset/train.txt");
	}
	
	//private static init_best_bigrams()
    public static void main(String[] args) throws URISyntaxException, IOException {
    	GenerateQuery e = new GenerateQuery();
    	MaxentTagger tagger = new MaxentTagger("taggers/english-left3words-distsim.tagger");
//    	e.getPOS("Costa Rica Puerto Rico");
//    	e.getPOS("Hello my name is Tada", new HashSet<String>(), tagger);
    //	e.getNGrams("Hello my name is Kavya",1);
    }
    
    //This function generates the query based on frequent bigrams / phrases
    public String getRequestUsingBigrams(String text, HashMap<String, Double> map){
    	ArrayList<String> terms = new ArrayList<String>();
    	ArrayList<String> bigrams = getNGrams(text,2);
    	String result = "";
    	for(int i=0;i<bigrams.size();i++){
    		String s = bigrams.get(i);
    		if(map.containsKey(s)){
    			if(!terms.contains(s)){
    				result = result + "\"" + s+"\""+" ";
    				terms.add(s);
    			}
    				
    		}
    		else{
//    			String[] parts = s.split("\\s+");
//    			int l = terms.size();
//    			if(terms.size()>0 && terms.get(l-1).equals(parts[0])){
//    				terms.remove(l-1);	
//    			}
//    			else{
//    				result=  result + " "+parts[0];
//    				terms.add(parts[0]);
//    			}
//    				
//    			result = result+" " +parts[1]+ " ";
//    			terms.add(parts[1]);
    		}
    	}
    	return result.trim();
    }


    public static String getKeywords(String s, HashSet<String> stopwords) throws IOException{

    //Remove standard stopwords 
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
  public static ArrayList<String> getNGrams(String text, int n){	  
	  text = (text).replaceAll("[^a-zA-Z0-9\\s\\']", " ");
	  ArrayList<String> ngrams = new ArrayList<String>();

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
			  ngrams.add(cand);
		  else
			  return ngrams;
		  
		  i++;
	  }
	  return ngrams;
  }
    
    public String getPOS(String title){

    	String tagged = this.tagger.tagString(title);
    	String out = "";
    	String[] parts = tagged.split("\\s+");
    	for(String s: parts){
    		String tag = s.split("_")[1];
    		if(tag.equals("NNP") || tag.equals("JJ"))
    			out = out+" "+s.split("_")[0];
    	}
    	return out;
    }
    
    //Appends tags to the title
    public String addTags(String title, String tagList){
    	String[] tags = tagList.trim().split("\\s+");
    	String s =title.toLowerCase().trim();
    	for(String tag: tags){
    		tag = tag.trim().toLowerCase();
    		s = s + " "+tag;
    	}
    	return s.trim();   	
    }
    
    //Appends the last line of the body
    public String appendBody(String title, String body){
    	//appends title and last sentence of body
    	String[] sentences = body.split(".");
    	title= title + " "+sentences[0];
    	return title+ " "+sentences[sentences.length-1];
    }    
}