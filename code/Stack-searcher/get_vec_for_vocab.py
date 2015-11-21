import gensim.models as gm
import logging
import sys
import pickle

logging.basicConfig(format='%(asctime)s : %(levelname)s : %(message)s', level=logging.INFO)
model = gm.Word2Vec.load_word2vec_format("GoogleNews-vectors-negative300.bin", binary=True)

vocab = set([word.strip() for word in open(sys.argv[1]).readlines()])

output_pickle_file = sys.argv[2]

vecs = {}

for word in vocab:
	if word in model:
		vecs[word] = model[word]
print len(vecs)
pickle.dump(vecs,open(output_pickle_file,'w'))