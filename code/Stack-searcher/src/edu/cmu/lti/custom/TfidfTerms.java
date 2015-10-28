package edu.cmu.lti.custom;


import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
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
import edu.cmu.lti.pipeline.*;;


class TfidfTerms {
//	TF(t,d) = (Number of times term t appears in a document d) / (Total number of terms in d).
//	IDF(t) = log_e(Total number of documents / Number of documents with term t in it).
	QuestionRetreivalBaseline qrb = new QuestionRetreivalBaseline();
	static HashSet<String> stopwords = QuestionRetreivalBaseline.stopwords;
	static HashMap<String,ArrayList<String>> doc_attributes = new HashMap<String,ArrayList<String>>();
	static SolrServer solr;
	void setup() {
	    try {
	    	solr = new CommonsHttpSolrServer("http://http://128.237.181.230:8983/solr/travelstackexchange/");
	    } catch (MalformedURLException ex) {
	        throw new RuntimeException(ex);
	    }
	}
	
	public static void main(String[] args) throws SolrServerException, IOException {
//	    long startTime = System.nanoTime();
//	    List<String> topen;
//	    String[] uuid = {"333adf4d-ba25-4bfa-a76f-103803d6ab91",
//	                     "8396bb88-ca29-4fa5-ac63-a69a73014f83",
//	                     "2a7931e1-ccf5-4c86-926a-48d4862f4ab0",
//	                     "4f61715e-fa33-4963-9e85-ac9fce681127",
//	                     "5ef3ecc8-55e0-4d5f-9076-f3b295526a88",
//	                     "f6cfa2e5-5ea8-4f35-9d5b-d0063b34a3e5",
//	                     "250894af-41c8-489a-b101-beeb10f08bb2",
//	                     "798c737d-5b79-4af1-944d-199e8b09fec9",
//	                     "bbf9141b-c025-4b42-a09f-b93a57048584",
//	                     "f36f845b-7a0e-4ed5-88be-bacba31edc49"};
//	    for(String id: uuid) {
//	      System.out.println(id);
//	      topen = top_terms(5, id);
//	      for(String token: topen) {
////	        System.out.println(token);
//	      }
//	    }
//	    long endTime = System.nanoTime();
//	    System.out.println((endTime - startTime)/1000000000);
		HashMap<String,Double> map = top_terms(2, 10, "5ef3ecc8-55e0-4d5f-9076-f3b295526a88");
		//get title and body in map
		//map.get(docId) -> title and body
		GenerateQuery gq = new GenerateQuery();
		//1st argument is title +" " + body
		ArrayList<String> res = doc_attributes.get("5ef3ecc8-55e0-4d5f-9076-f3b295526a88");
		String newQuery = gq.getRequestUsingBigrams(res.get(0)+" "+res.get(1), map);
		System.out.println(newQuery);
	}
	
	private static HashMap<String, Double> top_terms(int n_gram, int top_k, String documentID) throws SolrServerException, IOException {
		HashMap<String,Double> tfidf = tfidf_map(documentID,n_gram);
		Set<String> set = tfidf.keySet();
	    List<String> keys = new ArrayList<String>(set);

	    Collections.sort(keys, new Comparator<String>() {
	        @Override
	        public int compare(String s1, String s2) {
	            return Double.compare(tfidf.get(s2), tfidf.get(s1)); //reverse order
	        }
	    });
	    List<String> top_k_tokens = keys.subList(0, top_k);
	    HashMap<String,Double> top_k_terms = new HashMap<String,Double>();
	    for(String token: top_k_tokens) {
	    	top_k_terms.put(token,tfidf.get(token));
	    }
		return top_k_terms;
	}
	
	private static HashMap<String,Double>  tfidf_map(String documentID, int n_gram) throws SolrServerException, IOException {
		String[] token_list = uniqueTokenList(documentID, n_gram);
	    HashMap<String,Double> tf_map = get_tf_map(token_list);
	    HashMap<String,Double> idf_map = get_idf_map(token_list);
	    HashMap<String,Double> tfidf_map = new HashMap<String,Double>();
	    for(String token: token_list) {
	    	tfidf_map.put(token,Math.log(1+tf_map.get(token)) * idf_map.get(token));
	    }
	    return tfidf_map;
	}
	
	private static String[]  uniqueTokenList(String documentID, int n_gram) throws SolrServerException, IOException {
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
		ArrayList<String> attr = new ArrayList<String>();
		attr.add(title_content);
		attr.add(body_content);
		doc_attributes.put(documentID, attr);
		String full_content = title_content + " " + body_content;
		String content = (full_content).replaceAll("[^a-zA-Z0-9\\s\\']", " ");
		String content_noStopwords = GenerateQuery.getKeywords(content, stopwords);
		String[] token_list = null;
		if (n_gram == 1) {
	    	token_list = (content).split("\\s+");
	    } else {
	    	ArrayList<String> ngram_list = GenerateQuery.getNGrams(content_noStopwords, n_gram);
	    	token_list = new String[ngram_list.size()]; 
	    	ngram_list.toArray(token_list);
	    }
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
		String query = String.format("\"%s\"", term);
		SolrQuery q = new SolrQuery(query);
		q.setRows(0);  // don't actually request any data; just want numDocs for query term
	    long docs_with_term =  solr.query(q).getResults().getNumFound();
//	    System.out.println(query);
//		System.out.println(docs_with_term);
	    double term_idf = Math.log((total_docs+1) / (docs_with_term+1));
	    return term_idf;
	}
}
