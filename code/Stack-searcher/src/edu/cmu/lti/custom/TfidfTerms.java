package edu.cmu.lti.custom;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import javax.swing.text.Document;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.ModifiableSolrParams;

class TfidfTerms {
//	TF(t,d) = (Number of times term t appears in a document d) / (Total number of terms in d).
//	IDF(t) = log_e(Total number of documents / Number of documents with term t in it).
	static SolrServer solr;
	void setup() {
	    try {
	    	solr = new CommonsHttpSolrServer("http://http://128.237.181.230:8983/solr/travelstackexchange/");
	    } catch (MalformedURLException ex) {
	        throw new RuntimeException(ex);
	    }
	}
	
	public static void main(String[] args) throws MalformedURLException, SolrServerException {
//		SolrServer solr = new CommonsHttpSolrServer("http://localhost:8983/solr/gettingstarted_shard1_replica1/");
		String[] a = uniqueTokenList("8cac10e7-c420-444c-9323-0e748476238b");
		System.out.println(1);
		
		
		
		List<String> topen = top_terms(5, "8cac10e7-c420-444c-9323-0e748476238b");
		for(String token: topen) {
			System.out.println(token);
		}
	}
	
	private static List<String> top_terms(int n, String documentID) throws SolrServerException, MalformedURLException {
		HashMap<String,Double> tfidf = tfidf_map(documentID);
		Set<String> set = tfidf.keySet();
	    List<String> keys = new ArrayList<String>(set);

	    Collections.sort(keys, new Comparator<String>() {
	        @Override
	        public int compare(String s1, String s2) {
	            return Double.compare(tfidf.get(s2), tfidf.get(s1)); //reverse order
	        }
	    });
	    return keys.subList(0, n);
	}
	
	private static HashMap<String,Double>  tfidf_map(String documentID) throws SolrServerException, MalformedURLException {
		String[] token_list = uniqueTokenList(documentID);
	    HashMap<String,Double> tf_map = get_tf_map(token_list);
	    HashMap<String,Double> idf_map = get_idf_map(token_list);
	    HashMap<String,Double> tfidf_map = new HashMap<String,Double>();
	    for(String token: token_list) {
	    	tfidf_map.put(token,tf_map.get(token) * idf_map.get(token));
	    }
	    return tfidf_map;
	}
	
	private static String[] uniqueTokenList(String documentID) throws SolrServerException, MalformedURLException {
		solr = new CommonsHttpSolrServer("http://128.237.181.230:8983/solr/travelstackexchange/");
		String query = String.format("id:%s", documentID);
		SolrQuery q = new SolrQuery(query);
		q.setRows(1);  //one result; documentID should be unique.
	    ArrayList<SolrDocument> doc =  solr.query(q).getResults();
	    SolrDocument sd = doc.get(0);
	    ArrayList<String> title = (ArrayList<String>) sd.getFieldValue("Title");
	    String title_content = title.get(0);
	    ArrayList<String> body = (ArrayList<String>) sd.getFieldValue("Body");
		String body_content = body.get(0);
		// @TODO clear punctuation and stop-words? 
		
		String[] token_list = (title_content + " " + body_content).split("\\s+");
		return token_list;
	}
	
	private static HashMap<String,Double> get_tf_map(String[] token_list) {
		double total_words = token_list.length;
		HashMap<String,Double> tf_count = new HashMap<String,Double>();
		HashMap<String,Double> tf_hashmap = new HashMap<String,Double>();
		for(String token: token_list) {
			if (tf_count.get(token) != null) {
				tf_count.put(token, tf_count.get(token)+1.0);
			} else {
				tf_count.put(token, 1.0);
			}
	    }
		for (String token : tf_count.keySet()) {
			Double tf = tf_count.get(token) / total_words;
			tf_hashmap.put(token, tf);
		}		   
		return tf_hashmap;
	}
	
	private static HashMap<String,Double> get_idf_map(String[] token_list) throws SolrServerException {
		SolrQuery q = new SolrQuery("*:*");
	    q.setRows(0);  // don't actually request any data; just want numDocs
	    long total_docs =  solr.query(q).getResults().getNumFound();
		
	    HashMap<String,Double> idf_map = new HashMap<String,Double>();
	    for (String token : token_list) {
			idf_map.put(token, get_idf(total_docs, token));
		}
	    return idf_map;
	}

	private static double get_idf(long total_docs, String term) throws SolrServerException {
		// http://localhost:8983/solr/tss/select/?&q=Antarctica&wt=json&start=0&rows=0&indent=on
		SolrQuery q = new SolrQuery(term);
		q.setRows(0);  // don't actually request any data; just want numDocs for query term
	    long docs_with_term =  solr.query(q).getResults().getNumFound();
	    
//	    System.out.println(docs_with_term);
//	    System.out.println(term); // fails for terms with punctuation ie "October?"
	    double term_idf = Math.log(total_docs / docs_with_term);
	    return term_idf;
	}
}
