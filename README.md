Stack Searcher
=========

Problem Definition
---
Retrieving and predicting relevant questions given an input question

Challenge and Motivation
---
* Stack exchange questions are often **long and complex**, thus hard to search for similar ones
  * Dynamic keywords **selection and expansion** for question retrieval 
    * new search results quality estimation algorithm
  * Utilize question title, description, category,  date, previous answer votes, and other metadata to improve search results  
* Users could describe same symptom in **different words**
  * new relevant model based on word embedding  

Dataset
----
Stack Exchange Data Dump
https://archive.org/details/stackexchange

Size: ~25GB

Full dataset:  metal.lti:/usr2/diwang/stack/stackexchange

Sample dataset: https://github.com/Digo/stack-searcher/blob/master/dataset_sample/

Next Steps
---
* [x] Data collection reader
* [x] Find questions that marked as duplicated or answers contains a link for another question
* [x] Extract and format data tuples (input question, [relevant questions]) 
* [x] Split data tuples into train, validate, test datasets
* [x] Search evaluation MAP, MRR, Recall@10 
* [ ] Develop and evaluate new dynamic keywords selection and expansion methods
  * [ ] Build feedback loop between keyword generation and MAP/Recall@N  
* [ ] Develop and evaluate new relevant function based on word embedding
  * [ ] Learn about document distance in vector space e.g Word Mover's Distance
