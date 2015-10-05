Stack Searcher
=========

Problem Definition
---
Retrieving and predicting relevant questions given an input question

Challenge and Motivation
---
* Stack exchange questions are often **long and complex**, thus hard to search for similar ones
  * Dynamic **selection and expansion** of keywords for question retrieval 
    * new search results quality estimation algorithm
  * Utilize question title, description, category,  date, previous answer votes, and other metadata to improve search results  
* Users could describe same symptom in **different words**
  * new relevant mode based on word embedding  

Dataset
----
Stack Exchange Data Dump
https://archive.org/details/stackexchange

Size: ~25GB

Location: 

Full dataset:  metal.lti:/usr2/diwang/stack/stackexchange

Sample dataset: https://github.com/Digo/stack-searcher/blob/master/dataset_sample/

Next Steps
---
* Data collection reader
* Find questions that marked as duplicated or answers contains a link for another question
* Extract and format data tuples (input question, [relevant questions]) 
* Split data tuples into train, validate, test datasets
* Search evaluation MAP, MRR, Recall@10 
