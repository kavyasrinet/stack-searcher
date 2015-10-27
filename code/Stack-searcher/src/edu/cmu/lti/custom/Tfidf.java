package edu.cmu.lti.custom;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
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

class Tfidf {
//	TF(t) = (Number of times term t appears in a document) / (Total number of terms in the document).
//	IDF(t) = log_e(Total number of documents / Number of documents with term t in it).

	public static void main(String[] args) throws MalformedURLException, SolrServerException {
		SolrServer solr = new CommonsHttpSolrServer("http://128.2.100.173:7574/solr/travelstackexchange/");
	}
	public static Double tfidf(SolrServer solr, String documentID) throws SolrServerException {
		String[] token_list = uniqueTokenList(solr, documentID);
	    HashMap<String,Double> tf_map = get_tf_map(solr, token_list);
	    HashMap<String,Double> idf_map = get_idf_map(solr,token_list);
	    
		return 1.0;
	}
	
	public static String[] uniqueTokenList(SolrServer solr, String documentID) throws SolrServerException {
		String query = String.format("id:%s", documentID);
		SolrQuery q = new SolrQuery(query);
		q.setRows(1);  
		
		ArrayList<SolrDocument> doc =  solr.query(q).getResults();
		SolrDocument sd = doc.get(0);
		ArrayList title = (ArrayList<Long>)  sd.getFieldValue("Title");
		String title_content = (String) title.get(0);
		ArrayList body = (ArrayList<Long>) sd.getFieldValue("Body");
		String body_content = (String) body.get(0);
		// @TODO clear punctuation and stop-words? 
		
		String[] token_list = (title_content + " " + body_content).split("\\s+");
		return token_list;
	}
	
	public static HashMap<String,Double> get_tf_map(SolrServer solr, String[] token_list) {
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
	
	public static HashMap<String,Double> get_idf_map(SolrServer solr, String[] token_list) throws SolrServerException {
		SolrQuery q = new SolrQuery("*:*");
	    q.setRows(0);  // don't actually request any data; just want numDocs
	    long total_docs =  solr.query(q).getResults().getNumFound();
		
	    HashMap<String,Double> idf_map = new HashMap<String,Double>();
	    for (String token : token_list) {
			idf_map.put(token,get_idf(solr, total_docs, token));
		}
	    return idf_map;
	}

	public static double get_idf(SolrServer solr, long total_docs, String term) throws SolrServerException {
		// q:antactica 50 found online, 124 found in query
		// 42f6d0eb-8295-4ca9-9a9a-fab3a66f37b4
		// http://localhost:8983/solr/tss/select/?q=Antarctica&wt=json&indent=on
		// http://localhost:8983/solr/tss/select/?&q=Antarctica&wt=json&start=0&rows=0
		
		SolrQuery q = new SolrQuery(term);
		q.setRows(0);  // don't actually request any data; just want numDocs
	    long docs_with_term =  solr.query(q).getResults().getNumFound();
	    
	    double term_idf = Math.log(total_docs / docs_with_term);
	    return term_idf;
	}
}
