package edu.cmu.lti.ranking;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

import net.sf.javaml.*;
import net.sf.javaml.classification.Classifier;

import org.apache.solr.common.SolrDocument;

public class QuestionRanker
{

	Classifier C;

	public HashMap<String, ArrayList<SolrDocument>>load_training_data(String training_file)
	{	
		return null;	
	}
	
	public double[] extract_features(SolrDocument s)
	{
		return null;
	}
	
	public void train_model(String training_file)
	{

	}
	
	public HashMap<String, ArrayList<SolrDocument>>rank()
	{
		return null;
	}
	

}
