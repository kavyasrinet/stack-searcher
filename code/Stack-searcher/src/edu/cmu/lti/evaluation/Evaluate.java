package edu.cmu.lti.evaluation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import edu.cmu.lti.search.Result;

public class Evaluate
{
	HashMap<String, HashSet<String>> goldSet = new HashMap<String,HashSet<String>>();
	Evaluate() throws IOException
	{
		BufferedReader queryReader = new BufferedReader(new FileReader(new File("dataset_sample/question_queries.txt")));
		BufferedReader resultReader = new BufferedReader(new FileReader(new File("dataset_sample/question_results.txt")));
		
		String query;
		String result;
		while(( query = queryReader.readLine().split("\t")[0])!= null && ( result = resultReader.readLine().split("\t")[0])!= null)
		{
			if(!goldSet.containsKey(query))		
				goldSet.put(query, new HashSet<String>());
			goldSet.get(query).add(result);
		}
	}
	
	
	float getMapScore(Map<String,List<String>> predicted_results)
	{
		int total_questions = predicted_results.size();
		float map = 0;
		for(Entry<String,List<String>> e  :predicted_results.entrySet())
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
			map = map + precision/total;
		}
		return map/total_questions;		
	}
}
