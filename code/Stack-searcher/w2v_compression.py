"""
Compress Google Word2Vec Model for travel dataset.

To read in Posts corpus:
  question_text()

Try to break content into 'real' (multi-word) tokens by POS tag (~3.5 hrs, 16GB+):
Currently inaccurate (b/c tagger behavior w/ capitalized words)
  smart_tokenize()

Naive tokenization:
  dumb_tokenize()

Merge uppercase & lowercase corpus. Model case-sensitive:
  concat_cases()

Compute top-n similar words for each token in corpus (~2 hrs):
  cosine_similarity(n)

Extract just top 1 similar token for corpus 
Return in txt to be read into Java pipeline
  java_convert_json()
"""


import string,json,re
import sys,logging
import nltk,nltk.data
import gensim.models as gm

reload(sys) 
sys.setdefaultencoding('utf8')
logging.basicConfig(format='%(asctime)s : %(levelname)s : %(message)s', level=logging.INFO)


def question_text(filename="question_queries.txt"):
  f = open(filename,'r')
  raw = f.read()
  f.close() 

  clean = []
  clean_lower = []
  curr = ""
  ignore = False
  for x in raw:
    if x == "<":
      curr = re.sub('[^a-zA-Z\'\s]+', ' ', curr)
      clean.append(curr)
      clean_lower.append(curr.lower())
      ignore = True
    elif x == ">":
      curr = ""
      ignore = False
    elif not ignore:
      curr += x
  f = open('question_queries_clean.txt','w')
  f.write(str(clean))
  f.close() 
  f = open('question_queries_clean_lower.txt','w')
  f.write(str(clean_lower))
  f.close() 
  
  return


def smart_tokenize(filename='question_queries_clean.txt',tokens=[],s=""):
  f = open(filename,'r')
  raw = f.read()
  f.close() 
  all_text = eval(raw)
  f = open(('token%s.txt'%s),'w')
    
  for line in all_text:
    tokens = line.split()
    tag_tokens = nltk.pos_tag(tokens)
    token_set = set()
    curr_token = ""
    for (tok,tag) in tag_tokens:
      if "NNP" in tag:
        if curr_token == "":
          curr_token = tok
        else:
          curr_token += "_" + tok
      else:
        try:
          f.write(tok+"\n")
        except:
            raise Exception ("Encoding error. Check token: %s" % tok)
        token_set.add(tok)
        if (curr_token != "") and (curr_token != tok):
          token_set.add(curr_token)
          try:
            f.write(curr_token+"\n")
          except:
            raise Exception ("Encoding error. Check token: %s" % tok)
          curr_token = ""
  if (curr_token != ""):
    token_set.add(curr_token)
    try:
      f.write(curr_token+"\n")
    except:
      print(curr_token)  
  f.close() 
  return token_set

def dumb_tokenize(filename='question_queries_clean.txt',tokens=[],s=""):
  f = open(filename,'r')
  raw = f.read()
  f.close() 
  all_text = eval(raw)
  f = open(('dumbtoken%s.txt'%s),'w')
  for line in all_text:
    tokens = line.split()
    for tok in tokens:
      try:
        f.write(tok+"\n")
      except:
          raise Exception ("Encoding error. Check token: %s" % tok)
  f.close() 
  return 

def concat_cases():
  with open("dumbtoken.txt") as f:
    content = f.readlines()
  with open("dumbtoken-lower.txt") as f:
    content_lower = f.readlines()
  input_content = set(content + content_lower)
  content = set([x.rstrip() for x in input_content])
  return content
  


def cosine_similarity(s=[],n=15):
  model = gm.Word2Vec.load_word2vec_format("GoogleNews-vectors-negative300.bin", binary=True)
  s = concat_cases()
  word2vec_map = dict()
  oov = []
  for word in s:
    # cat = []
    # term = []
    try:
      sim = model.most_similar(word,topn=n)
      # Split similar words by relation to query
      #
      # for (sim_word,score) in sim:
      #   w,sw = word.lower(),sim_word.lower()
      #   if (w in sw) or (sw in w):
      #     term.append((sim_word,score))
      #   else:
      #     cat.append((sim_word,score))
      # word2vec_map[word] = {"cat":cat, "term":term,"all":sim}
      word2vec_map[word] = sim
    except:
      oov.append(word)
  with open('w2v_travel_dumb_single.txt', 'w') as f:
    json.dump(word2vec_map, f, ensure_ascii=False)
  with open('oov.txt', 'w') as f:
    for x in oov:
      f.write(x+"\n")  
  return None

def java_convert_json():
  with open('w2v_java.txt', 'w') as jf:
    with open('w2v_travel_dumb.txt', 'r') as f:
      d = json.loads(f.read())
    for k in d:
      jf.write(k +" "+ d[k][0][0] +"\n")
  return
  