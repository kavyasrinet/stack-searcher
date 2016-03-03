package edu.cmu.lti.evaluation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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
	public Evaluate(String query_file) throws IOException
	{

    	for (String line : Files.readAllLines(Paths.get(query_file))) {
    		String[] splits = line.trim().split("\t");
    		HashSet<String> linked_qids = new HashSet<String>();
    		for(int i=1;i<splits.length;i++)
    			linked_qids.add(splits[i]);
    		goldSet.put(splits[0],linked_qids);
    	}
	}
	
/*
 * This function computes the MAP score over the predicted result for every query.	
 */
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

	/*
	 * This function computes Precision at K for the given query for every result document.
	 */
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
					if ((++total) > k)
						break;
					
					if(goldSet.get(questionID).contains(resultID))
						correct++;
				}
			}
			
			map = map + correct/k;
		}
		return map/total_questions;		
	}
/*
 * This function computes Mean reciprocal rank.	
 */
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

	/*
	 * This function computes recall.
	 */
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

	/*
	 * This function computes recall.
	 */
	public void doErrorAnalysis(HashMap<String,ArrayList<String>> predicted_results) throws IOException
	{
		File output = new File("errorAnalysis.txt");
		BufferedWriter writer;
		writer = new BufferedWriter(new FileWriter(output, true));
		
		int total_questions = predicted_results.size();
		for(Entry<String,ArrayList<String>> e  :predicted_results.entrySet())
		{
			String questionID = e.getKey();
			writer.write(questionID);
			writer.write("\n");
			HashSet<String> allResults = goldSet.get(questionID);
			for(String resultID: allResults)
				if (! e.getValue().contains(resultID)) {
					writer.write(resultID);
					writer.write("\n");
				}
			writer.write("\n");
		}
		writer.close();
		return;		
	}
}
