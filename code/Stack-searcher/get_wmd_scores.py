from urllib2 import *
import sys
import pickle
from sklearn.metrics import pairwise
import collections
import numpy as np
from emd import emd

mem ={}

def get_doc(id1):
	url = 'http://localhost:8983/solr/travelstackexchange/select?q=Id%3A'+id1+'&wt=python'
	connection = urlopen(url)
	response = eval(connection.read())
	mem[id1] = response['response']['docs'][0]
	return response['response']['docs'][0]

def get_score(s1, s2, model):
	s1 =re.sub('[^a-zA-Z\s]+', ' ', s1)
	s2 =re.sub('[^a-zA-Z\s]+', ' ', s2)
	set1 = [word  for word in set( s1.strip().lower().split() + s1.strip().split()) if word in model ]
	set2 = [word  for word in set(s2.strip().lower().split() + s2.strip().split()) if word in model]

	c1 = collections.Counter(s1.strip().split() + s1.lower().strip().split())
	c2 = collections.Counter( s2.strip().split() + s2.lower().strip().split())

	w1 = [c1[word]*1.00 for word in set1]
	w2 = [c2[word]*1.00 for word in set2]

	w1 = np.array(w1)
	w2 = np.array(w2)
	w1/=sum(w1)
	w2/=sum(w2)

	v1 = [model[word]*1.00 for word in set1]
	v2 = [model[word]*1.00 for word in set2]

	return emd(v1,v2,X_weights = w1, Y_weights = w2)

def get_wmd(query,result, word_2_vec_model):
	query_doc = get_doc(query)
	result_doc = get_doc(result)

	title_score = get_score(query_doc['Title'],result_doc['Title'], word_2_vec_model)
	body_score = get_score(query_doc['Body'],result_doc['Body'], word_2_vec_model)
	return title_score, body_score


input_file  = sys.argv[1]
word_2_vec_model = pickle.load(open(sys.argv[2]))

output_file = open(sys.argv[3],'w')

input_questions = []

for line in open(input_file).readlines():
	splits = line.strip().split()
	query = splits[0]
	results = splits[1:]	
	input_questions.append((query, results))
	s =''
	for result in results:
		scores = get_wmd(query, result, word_2_vec_model)
		s+='\t'+str(scores[0])+","+str(scores[1])
		
	output_file.write(s.strip() +"\n")
