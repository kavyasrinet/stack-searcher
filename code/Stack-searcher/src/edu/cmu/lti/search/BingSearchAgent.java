package edu.cmu.lti.search;

//import edu.cmu.lti.neal.dexp.MultiMap;
//import edu.cmu.lti.neal.dexp.WebSearchCache;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class BingSearchAgent  {
	private WebSearchCache retrievalCache;
	private MultiMap<String, Result> cacheBing;
	private String AccountKey;
	private String cachePath = "CACHE" + File.separator ;
	private int passagenumber = 0;
	private boolean VERBOSE = false;
	public void setVerbose( boolean flag ){
		VERBOSE = flag;
	}
	private int resultSetSize;

	public void initialize( String accountKey )  {
		this.AccountKey = accountKey;

		this.retrievalCache = new WebSearchCache( cachePath );
		cacheBing = (MultiMap<String, Result>) retrievalCache.loadCache( this.getClass().getSimpleName() );
		if (cacheBing == null)
			cacheBing = new MultiMap<String, Result>();
		resultSetSize = 10;
	}
	
	public void setResultSetSize( int size ){
		resultSetSize = size;
	}
	
	 public List<RetrievalResult> retrieveDocuments(String qid, String question) throws URISyntaxException{
     List<RetrievalResult> documents = new ArrayList<RetrievalResult>(0);
     List<Result> resultL = new ArrayList<Result>();
     String requestURL = BingSearch.buildRequest( question, resultSetSize );
     if ( VERBOSE ) System.out.println("Bing Search : " + question);
     getResults(resultL, question, requestURL, this.getClass().getSimpleName() );
     // remove characters that are not supported by UIMA
     for (Result result : resultL) {
         String s = result.getAnswer();
         StringBuilder sb = new StringBuilder();
         for (char c : s.toCharArray()) {
             if (c > 0x1f) {
                 sb.append(c);
             } else {
                 sb.append(' ');
             }
         }

         documents.add( new RetrievalResult(this.getClass().getSimpleName() +"-"+result.getDocID(),
                 result.getScore(),
                 result.getQuery(),
                 sb.toString() ,
                 result.getURL() ));
     }


     return documents;
 }

	public List<RetrievalResult> retrieveDocuments(String qid, String question,
			List<String> keyTerms, List<String> keyPhrases) throws URISyntaxException {

		List<RetrievalResult> documents = new ArrayList<RetrievalResult>(0);
		if (keyTerms.size() == 0)
			return documents;

		List<Result> resultL = new ArrayList<Result>();

		// TODO: Leonid Boytsov, what does the following comment mean?
		/*
		 * Building the request. Option 1 : To use the entire web as the corpus
		 * -Trec option used to remove noise from Trec pages, research papers
		 * containing the answer 49 results being retrieved
		 */

		// Sending the request to the Web Search Service and get the response.
		// 1. Executing the direct question as the query
		String requestURL = BingSearch.buildRequest( question, resultSetSize );
		if ( VERBOSE ) System.out.println("Bing Search : " + question);
		getResults(resultL, question, requestURL, this.getClass().getSimpleName() );

		// remove characters that are not supported by UIMA
		for (Result result : resultL) {
			String s = result.getAnswer();
			StringBuilder sb = new StringBuilder();
			for (char c : s.toCharArray()) {
				if (c > 0x1f) {
					sb.append(c);
				} else {
					sb.append(' ');
				}
			}

			documents.add( new RetrievalResult(this.getClass().getSimpleName() +"-"+result.getDocID(),
					result.getScore(),
					result.getQuery(),
					sb.toString() ,
					result.getURL() ));
		}


		return documents;
	}

	private void getResults(List<Result> resultL,
			String question,
			String requestURL,
			String sourceID) {

		// Don't clear results here!!!
		//resultL.clear();
		Document doc;

		if (cacheBing.contains(requestURL)) {
			if ( VERBOSE ) System.out.println("Bing Cache Entry Found");
			resultL.addAll(cacheBing.get(requestURL));
		} else {
			if ( VERBOSE ) System.out.println("Not in Bing Local cache");
			try {
				doc = BingSearch.getResponse(requestURL, this.AccountKey);
				if (doc != null) {
					try {
						List<Result> tmpResult = BingSearch.processResponse(doc, question);
						resultL.addAll(tmpResult);
						
						
						for (Result result : tmpResult) {
							cacheBing.add(requestURL, result);
						}

						if (tmpResult.size() > 0) {
							retrievalCache.saveCache(cacheBing, this.getClass().getSimpleName() );
						}
					} catch (XPathExpressionException e) {
						e.printStackTrace();
					}
				}
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}
}
