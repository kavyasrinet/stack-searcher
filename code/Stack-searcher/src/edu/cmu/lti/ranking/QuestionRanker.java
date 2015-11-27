package edu.cmu.lti.ranking;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;





import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.params.ModifiableSolrParams;

import edu.cmu.lti.custom.GenerateQuery;
import weka.classifiers.Classifier;
import weka.classifiers.functions.Logistic;
import weka.classifiers.functions.SMO;
import weka.classifiers.functions.supportVector.Kernel;
import weka.classifiers.functions.supportVector.RBFKernel;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import edu.cmu.lti.pipeline.QuestionRetreivalBaseline;
import edu.stanford.nlp.util.ArrayUtils;

public class QuestionRanker
{
	SolrServer solr;
	Logistic l;
	 HashMap<Long, ArrayList<Double>> userInfo = new HashMap<Long, ArrayList<Double>>();
	 HashMap<String, ArrayList<Double>> userNameInfo = new HashMap<String, ArrayList<Double>>();

	public SolrDocument get_solr_doc( String post_id) throws SolrServerException
	{
		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set("qt", "/select");
		params.set("q", "Id:"+post_id);
		params.set("rows", "1");
		QueryResponse response = this.solr.query(params);
		return response.getResults().get(0);		
	}
	 
	public QuestionRanker(SolrServer solr) throws MalformedURLException, SolrServerException{
		this.solr = solr;
		
		SolrServer usersolr = new CommonsHttpSolrServer("http://localhost:8983/solr/travelusers/");
			SolrQuery solr_query = new SolrQuery("*:*");
			solr_query.setRows(21187);  

			QueryResponse response =  usersolr.query(solr_query);		
    	    ArrayList<SolrDocument> s = response.getResults();
    	   
    	    for(SolrDocument doc: s){
    	    	ArrayList<Double> info = new ArrayList<Double>();
    	    	
    	    	long Id = ((ArrayList<Long>) doc.getFieldValue("Id")).get(0);
    	    	String name = ((ArrayList<String>) doc.getFieldValue("DisplayName")).get(0);
    	    	if(doc.containsKey("Reputation"))
    	    		info.add((double)((ArrayList<Long>) doc.getFieldValue("Reputation")).get(0));
    	    	else
    	    		info.add(null);
    	    	if(doc.containsKey("Views"))
    	    		info.add((double)((ArrayList<Long>) doc.getFieldValue("Views")).get(0));
    	    	else
    	    		info.add(null);
    	    	if(doc.containsKey("UpVotes"))
    	    		info.add((double)((ArrayList<Long>) doc.getFieldValue("UpVotes")).get(0));
    	    	else
    	    		info.add(null);
//    	    	if(doc.containsKey("DownVotes"))
//    	    		info.add((double)((ArrayList<Long>) doc.getFieldValue("DownVotes")).get(0));
//    	    	else
    	    		info.add(null);
    	    	userInfo.put(Id, info);
    	    	userNameInfo.put(name, info);
    	    }
	}
	
	public ArrayList<Double> extract_features(SolrDocument doc) {
		/*
		 * Features are:
		 * 1. Score
		 * 2. ViewCount
		 * 3. AnswerCount
		 * 4. Comment Count
		 * 5. Favorite Count
		 * 6. AcceptedAnswerId - binary
<<<<<<< HEAD
		 * 7. Solr score 
		 * 8. User's reputation
		 * 9. User's #views
		 * 10. User's Upvotes
		 * 11. User's Downvotes
=======
		 * 7. 
		 * 7. User's reputation
		 * 8. User's #views
		 * 9. User's Upvotes
		 * 10. User's Downvotes
>>>>>>> f54ea4b... added word mover's distance as feature
		 */
		ArrayList<Double> feats = new ArrayList<Double>();
		ArrayList<Double> nones = new ArrayList<Double>();
		nones.add(null);
		nones.add(null);
		nones.add(null);
		nones.add(null);
		HashMap<String, String> post_fields = new HashMap<String, String>();
		for (String field: doc.getFieldNames())
		{
			if(field.contains("score"))
				post_fields.put(field,(doc.getFieldValue(field)).toString());
			else if((!field.equals("id")) && (!field.equals("_version_")))
				post_fields.put(field,((ArrayList)doc.getFieldValue(field)).get(0).toString());
		}
		
		if(post_fields.get("PostTypeId").equals("1")){
			
			if(doc.containsKey("Score"))
				feats.add(Double.parseDouble(post_fields.get("Score")));
			else
				feats.add(null);
			
			if(doc.containsKey("ViewCount"))
				feats.add(Double.parseDouble(post_fields.get("ViewCount")));
			else
				feats.add(null);
			if(doc.containsKey("AnswerCount"))
				feats.add(Double.parseDouble(post_fields.get("AnswerCount")));
			else
				feats.add(null);
			if(doc.containsKey("CommentCount"))
				feats.add(Double.parseDouble(post_fields.get("CommentCount")));
			else
				feats.add(null);
//			if(doc.containsKey("FavoriteCount"))
//				feats.add(Double.parseDouble(post_fields.get("FavoriteCount")));
//			else
				feats.add(null);
			
			if(doc.getFieldValue("AcceptedAnswerId")!=null)
				feats.add(1.0);
			else
				feats.add(0.0);

			if(doc.getFieldValue("score")!=null)
				feats.add(Double.parseDouble(post_fields.get("score")));
			else
				feats.add(0.0);
			
			if(doc.getFieldValue("wmd_score_1")!=null)
				feats.add(Double.parseDouble(post_fields.get("wmd_score_1")));
			else
				feats.add(0.0);			
			
			if(doc.getFieldValue("wmd_score_2")!=null)
				feats.add(Double.parseDouble(post_fields.get("wmd_score_2")));
			else
				feats.add(0.0);
		
			if(doc.containsKey("OwnerUserId")){
				long Id = Long.parseLong(post_fields.get("OwnerUserId"));
				feats.addAll(userInfo.get(Id));
			}
			else{
				if(doc.containsKey("OwnerDisplayName")){
					 String userName = ((ArrayList<String>) doc.getFieldValue("OwnerDisplayName")).get(0);
					 if(userNameInfo.containsKey(userName))
						 feats.addAll(userNameInfo.get(userName));
					 else
						 feats.addAll(nones); 
				}
				else
					feats.addAll(nones);
			}
									
		}
		return feats;
	}
	public ArrayList<Double> extract_features_pairwise(SolrDocument query, SolrDocument result)
	{
		/*
		 * Features are:
		 * SDM score for (query,doc)
		 * 1. unigram
		 * 2. bigram (exact match) 
		 * 3. bigram (proximity score)
		 * 4. trigram (exact match)
		 * 5. trigram (proximity score)
		 */
		// Compute semantic dependency model score
		String query_content = "";
		String document_content = "";
		if(query.containsKey("Title"))
			query_content += query.getFieldValue("Title");
		if(query.containsKey("Body"))
			query_content += " " + query.getFieldValue("Body");
		if(result.containsKey("Title"))
			document_content += result.getFieldValue("Title");
		if(result.containsKey("Body"))
			document_content += " " + result.getFieldValue("Body");
		ArrayList<Double> feats_sdm = sdm_score(query_content,document_content);
		return feats_sdm;
	}
	

	public Double overlap_countO(Set<String> src, ArrayList<String> tar) {
		Double overlap_score = 0.0;
		HashMap<String,Double> tarCount = new HashMap<String,Double>();
		for (String s: tar) {
			if (tarCount.get(s) != null) {
				tarCount.put(s, tarCount.get(s)+1.0);
			} else {
				tarCount.put(s, 1.0);
			}
		}
		for (String s: src) {
			if (tarCount.get(s) != null) {
				overlap_score += tarCount.get(s);
			} 
		}
		return overlap_score;
	}
	
	// Currently written exclusively for bigram.trigram ie n=2,3
	public Double overlap_countU(Set<String> src, ArrayList<Set<String>> tar, int n) {
		Double overlap_score = 0.0;
		String q1 = "";
		String q2 = "";
		String q3 = "";
		for (String b: src) {
			String[] ngram = b.split("\\s+");
			q1 = ngram[0];
			q2 = ngram[1];
			if (n > 2) {
				q3 = ngram[2];
			}
			for (Set<String> win: tar) {
				if (win.contains(q1) & win.contains(q2) & (n < 3 | win.contains(q3))) {
					overlap_score += 1.0;
				}
			}
		}
		return overlap_score;
	}
	
	public ArrayList<Double> sdm_score(String query_raw, String document_raw) {
		// default weights
//		Double unigramWeight = 0.1; 
//		Double bigramOWeight = 0.2;
//		Double bigramUWeight = 0.1; 
//		Double trigramOWeight = 0.8;
//		Double trigramUWeight = 0.1;
		int w = 8; // default window for unordered computation. 
		
		ArrayList<Double> feats_sdm = new ArrayList<Double>();
		
		String query = (query_raw).replaceAll("[^a-zA-Z0-9\\s\\']", " ");
		String document = (document_raw).replaceAll("[^a-zA-Z0-9\\s\\']", " ");
		
		// compute similarity for unigrams
		List<String> ugQ = Arrays.asList((query).split("\\s+"));
		ArrayList<String> unigramsQ = new ArrayList<String>();
		for (String u: ugQ) {
			unigramsQ.add(u);
		}
		if (unigramsQ.size() == 0) {
			feats_sdm.add(null);
		} else {
			Set<String> uniqueUnigramsQ = new HashSet<String>(unigramsQ); 
			List<String> ugD = Arrays.asList((document).split("\\s+"));
			ArrayList<String> unigramsD = new ArrayList<String>();
			for (String u: ugD) {
				unigramsD.add(u);
			}
			Double unigramScore = overlap_countO(uniqueUnigramsQ,unigramsD);
			feats_sdm.add(unigramScore);
		}
		// compute similarity for bigrams
		ArrayList<String> bigramsQ = GenerateQuery.getNGrams(query, 2);
		if (bigramsQ == null) {
			// null for bigram ordered & unordered
			feats_sdm.add(null);
			feats_sdm.add(null);
		} else {
			// exact 'ordered' match
			Set<String> uniqueBigramsQ = new HashSet<String>(bigramsQ);
			ArrayList<String> bigramsD = GenerateQuery.getNGrams(document, 2);
			Double bigramOScore = overlap_countO(uniqueBigramsQ,bigramsD);
			feats_sdm.add(bigramOScore);
			
			// 'unordered' match within window (default w = 8)
			ArrayList<String> windowDBi = GenerateQuery.getNGrams(document, w);
			ArrayList<Set<String>> windowSetDBi = new ArrayList<Set<String>>();
			for (String window: windowDBi) {
				String[] windowTerms = window.split("\\s+");
				Set<String> currSet = new HashSet<String>(Arrays.asList(windowTerms)); 
				windowSetDBi.add(currSet);
			}
			Double bigramUScore = overlap_countU(uniqueBigramsQ,windowSetDBi,2);
			feats_sdm.add(bigramUScore);
		}
		// compute similarity for trigrams
		ArrayList<String> trigramsQ = GenerateQuery.getNGrams(query, 3);
		if (trigramsQ == null) {
			// null for trigram ordered & unordered
			feats_sdm.add(null);
			feats_sdm.add(null);
		} else {
			// exact 'ordered' match
			Set<String> uniqueTrigramsQ = new HashSet<String>(trigramsQ);
			ArrayList<String> TrigramsD = GenerateQuery.getNGrams(document, 3);
			Double trigramOScore = overlap_countO(uniqueTrigramsQ,TrigramsD);
			feats_sdm.add(trigramOScore);
			
			// 'unordered' match within window (default w = 8)
			ArrayList<String> windowDTri = GenerateQuery.getNGrams(document, w);
			ArrayList<Set<String>> windowSetDTri = new ArrayList<Set<String>>();
			for (String window: windowDTri) {
				String[] windowTerms = window.split("\\s+");
				Set<String> currSet = new HashSet<String>(Arrays.asList(windowTerms)); 
				windowSetDTri.add(currSet);
			}
			Double trigramUScore = overlap_countU(uniqueTrigramsQ,windowSetDTri,3);
			feats_sdm.add(trigramUScore);
		}
		return feats_sdm;
		
	}
	
	public void train_model(String training_file) throws Exception
	{
		GenerateQuery generate_query = new GenerateQuery();
		HashMap<SolrDocument, ArrayList<SolrDocument>> training_data = QuestionRetreivalBaseline.querySolr(training_file, 100, solr, generate_query);
		add_wmd_scores(training_data,"output_scores.txt","input_ids.txt");
		HashMap<String, Set<String>> goldset = new HashMap<String, Set<String>>();
		for (String line : Files.readAllLines(Paths.get(training_file))) {
			String[] splits = line.trim().split("\t");
			Set<String> s = new HashSet<String>();
			for(int i=1;i<splits.length;i++)
				s.add(splits[i]);
			goldset.put(splits[0],s);
		}
		Instances weka_data = null;
		for(Map.Entry<SolrDocument, ArrayList<SolrDocument>> entry : training_data.entrySet())
		{
			ArrayList<Double> query_feats = extract_features(entry.getKey());
			String query_id = String.valueOf(((ArrayList<Long>)  entry.getKey().getFieldValue("Id")).get(0));
			
			ArrayList<double[]> pos_data = new ArrayList<double[]>();
			ArrayList<double[]> neg_data = new ArrayList<double[]>();
			for(SolrDocument result : entry.getValue())
			{
				String result_id = String.valueOf(((ArrayList<Long>)  result.getFieldValue("Id")).get(0));
				ArrayList<Double> result_feats = extract_features(result);
				result_feats.addAll(query_feats);
				result_feats.addAll(extract_features_pairwise(entry.getKey(), result));
				
				double[] feats = ArrayUtils.toPrimitive(result_feats.toArray(new Double[result_feats.size()]));
				if(goldset.get(query_id).contains(result_id))
					pos_data.add(feats);
				else
					neg_data.add(feats);
				if(weka_data == null)
				{
					FastVector atts = new FastVector();
					for(int feat_no = 0;feat_no < feats.length-1;feat_no++)
						atts.addElement(new Attribute("attribute " + String.valueOf(feat_no)));
					FastVector classes = new FastVector();
					classes.addElement("1");
					classes.addElement("-1");
					atts.addElement(new Attribute("class",classes));
					weka_data = new Instances("stack-searcher", atts, 25*goldset.size());			        
			        weka_data.setClassIndex(weka_data.numAttributes()-1);
				}
			}
			
			for(int p=0;p<pos_data.size();p++)
			{
				double[] pos = pos_data.get(p); 
				for(int q=0;q<neg_data.size();q++)
				{
					double[] neg = neg_data.get(q);
					Instance neg_instance = new Instance(neg.length);
					Instance pos_instance = new Instance(neg.length);
					pos_instance.setDataset(weka_data);
					neg_instance.setDataset(weka_data);
					for(int r=0;r<neg.length;r++)
					{
						neg_instance.setValue(r, neg[r] - pos[r]);
						pos_instance.setValue(r, pos[r] - neg[r]);						
					}
					pos_instance.setClassValue("1");
					neg_instance.setClassValue("-1");
					weka_data.add(pos_instance);
					weka_data.add(neg_instance);
				}
			}		
		}
		/*	
		CSVSaver c = new CSVSaver();
		c.setInstances(weka_data);
		c.setDestination(new File("ranker_training_data.csv"));
		c.writeBatch();
		
		System.out.println("Done saving");
		*/
		l = new Logistic();
		//l.setBuildLogisticModels(true);
		l.buildClassifier(weka_data);
	}
	
	private void add_wmd_scores(
			HashMap<SolrDocument, ArrayList<SolrDocument>> training_data, String output_file, String input_file) throws IOException, InterruptedException
	{
		
		PrintWriter writer = new PrintWriter(input_file, "UTF-8");
		for(Entry<SolrDocument, ArrayList<SolrDocument>> e  :training_data.entrySet())
		{
    		String qid = ((ArrayList)e.getKey().getFieldValue("Id")).get(0).toString();
	    	writer.print(qid+"\t");
	    	for(SolrDocument result : e.getValue()) {
	    		writer.print(((ArrayList)result.getFieldValue("Id")).get(0).toString()+"\t");
	    	}
	    	writer.println();
		}
    	writer.close();
    	
		
		ArrayList<SolrDocument> queries = new ArrayList<SolrDocument>(); 
		Map<String, SolrDocument> querymap = new HashMap<String, SolrDocument>();
		
		for(SolrDocument query : training_data.keySet())
		{
			
			querymap.put(((ArrayList)query.getFieldValue("Id")).get(0).toString(), query);
		}
    	Process p = Runtime.getRuntime().exec("python get_wmd_scores.py input_ids.txt ./dataset_sample/w2v_model.pk output_scores.txt");
    	p.waitFor();
		for (String line : Files.readAllLines(Paths.get(input_file))) {
			queries.add(querymap.get(line.trim().split("\t")[0]));
		}
		
    	int i=0;
    	for (String line : Files.readAllLines(Paths.get(output_file))) {
    		
    		String[] scores = line.trim().split("\t");
    		ArrayList<SolrDocument> results = training_data.get(queries.get(i));
    		for(int j=0;j<results.size();j++)
    		{
    			String[] result_scores = scores[j].split(",");
    			results.get(j).addField("wmd_score_1", result_scores[0]);
    			results.get(j).addField("wmd_score_2", result_scores[1]);
    		}
    		training_data.put(queries.get(i),results);
    		i++;
    	}		
	}

	public HashMap<SolrDocument, ArrayList<SolrDocument>>rerank(HashMap<SolrDocument, ArrayList<SolrDocument>> predicted_results) throws Exception
	{
		add_wmd_scores(predicted_results,"output_scores_val.txt","input_ids_val.txt");
		HashMap<SolrDocument, ArrayList<SolrDocument>> output = new HashMap<SolrDocument, ArrayList<SolrDocument>>();
		for(SolrDocument query: predicted_results.keySet())
		{
			ArrayList<Double> query_feats = extract_features(query);
			ArrayList<SolrDocument> results = predicted_results.get(query);
			HashMap<SolrDocument, Double> result_score = new HashMap<SolrDocument, Double>();
			for( SolrDocument result: results  )
			{
				ArrayList<Double> result_feats = extract_features(result);
				result_feats.addAll(query_feats);
				result_feats.addAll(extract_features_pairwise(query, result));
				double[] feats = ArrayUtils.toPrimitive(result_feats.toArray(new Double[result_feats.size()]));
				Instance i = new Instance(feats.length);
				for(int attIndex = 0; attIndex < feats.length; attIndex++)
				{
					i.setValue(attIndex, feats[attIndex]);
				}

				double[] score = l.distributionForInstance(i);

				result_score.put(result, score[1] );
			}
				Collections.sort(results, new Comparator<SolrDocument>() {
				    @Override
				    public int compare(SolrDocument s1, SolrDocument s2) {
				        return Double.compare(result_score.get(s1), result_score.get(s2));
				    }
				});		
			output.put(query, results);
		}
		System.out.println(l);
		return output;	
	}
	
	public ArrayList<ArrayList<Double>> getFeaturesFromPosts(HashMap<String, ArrayList<SolrDocument>> mapResults, SolrDocument query){
		ArrayList<ArrayList<Double>> features = new ArrayList<ArrayList<Double>>();
		for(String id: mapResults.keySet()){
			for(SolrDocument doc: mapResults.get(id)){
				features.add(extract_features(doc));
			}
		}
		return features;
	}
}
