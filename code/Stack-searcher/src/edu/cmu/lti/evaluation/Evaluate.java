package edu.cmu.lti.evaluation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import edu.cmu.lti.search.Result;

public class Evaluate
{
	HashMap<String, HashSet<String>> goldSet = new HashMap<String,HashSet<String>>();
	public Evaluate() throws IOException
	{
		BufferedReader queryReader = new BufferedReader(new FileReader(new File("dataset_sample/question_queries.txt")));
		BufferedReader resultReader = new BufferedReader(new FileReader(new File("dataset_sample/question_results.txt")));
		
		String query;
		String result;
		while(( query = queryReader.readLine())!= null && ( result = resultReader.readLine())!= null)
		{
			query = query.split("\t")[0];
			result = result.split("\t")[0];
			if(!goldSet.containsKey(query))		
				goldSet.put(query, new HashSet<String>());
			goldSet.get(query).add(result);
		}
	}
	
	
	public float getMapScore(HashMap<String,ArrayList<String>> predicted_results)
	{
		int total_questions = predicted_results.size();
		float map = 0;
		for(Entry<String,ArrayList<String>> e  :predicted_results.entrySet())
		{
			String questionID = e.getKey();
			float precision = 0;  
			float correct = 0;
			float total  = 0;
			for(String resultID: e.getValue())
			{
				if(resultID.matches("[0-9]+")) 	
				{
					total++;
					
					if(goldSet.get(questionID).contains(resultID))
					{
						correct++;
						precision = precision + correct/total;
					}
				}
			}
			if(precision > 0)
				map = map + precision/correct;
		}
		return map/total_questions;		
	}
	
	public float getPAtK(HashMap<String,ArrayList<String>> predicted_results,int k)
	{
		int total_questions = predicted_results.size();
		float map = 0;
		for(Entry<String,ArrayList<String>> e  :predicted_results.entrySet())
		{
			String questionID = e.getKey();
			float precision = 0;  
			float correct = 0;
			float total  = 0;
			for(String resultID: e.getValue())
			{
				if(resultID.matches("[0-9]+")) 	
				{
					if ((total++) > k)
						break;
					
					if(goldSet.get(questionID).contains(resultID))
						correct++;
				}
			}
			
			map = map + correct/k;
		}
		return map/total_questions;		
	}
	
	public float getMrrScore(HashMap<String,ArrayList<String>> predicted_results)
	{
		int total_questions = predicted_results.size();
		float mrr = 0;
		for(Entry<String,ArrayList<String>> e  :predicted_results.entrySet())
		{
			String questionID = e.getKey();
		
			float total  = 0;
			for(String resultID: e.getValue())
			{
				if(resultID.matches("[0-9]+")) 	
				{
					total++;
					
					if(goldSet.get(questionID).contains(resultID))
					{

						mrr = mrr + 1/total;
						break;
					}
				}
			}
		}
		return mrr/total_questions;		
	}
	
	public float getRecall(HashMap<String,ArrayList<String>> predicted_results)
	{
		int total_questions = predicted_results.size();
		float recall = 0;
		for(Entry<String,ArrayList<String>> e  :predicted_results.entrySet())
		{
			String questionID = e.getKey();
		
			float relevant  = 0;
			for(String resultID: e.getValue())
				if(resultID.matches("[0-9]+") && goldSet.get(questionID).contains(resultID))
						relevant++;
			recall = recall + relevant/goldSet.get(questionID).size();
			
		}
		return recall/total_questions;		
	}
}
