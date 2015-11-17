import string
import nltk
import gensim.models as gm
import logging


from xml.dom import minidom
def post_content(file="posts_solr_format"):
  exclude = set(string.punctuation)
  exclude.remove("'")
  content = ""
  xmldoc = minidom.parse(file+'.xml')
  itemlist = xmldoc.getElementsByTagName('field')
  i = 0
  for s in itemlist:
      print(i)
      i+=1
      if (s.attributes['name'].value in {'Tags','Title','Body'}):
        # tags title body
        try:
          content += " " + s.childNodes[0].nodeValue
        except: # not all posts have body content
          pass
  clean_content = ''.join(ch for ch in content if ch not in exclude)
  return clean_content


def smart_tokenize(s):
  tokens = s.split()
  tag_tokens = nltk.pos_tag(tokens)
  token_set = set()
  curr_token = ""
  for (tok,tag) in tag_tokens:
    if tag == "NNP":
      if curr_token == "":
        curr_token = tok
      else:
        curr_token += "_" + tok
    else:
      token_set.add(tok)
      if curr_token != "":
        token_set.add(curr_token)
        # print(curr_token)
  return token_set

def cosine_similarity(s):
  model = gm.Word2Vec.load_word2vec_format("GoogleNews-vectors-negative300.bin", binary=True)
  word2vec_map = dict()
  for word in s:
    cat = []
    term = []
    sim = model.most_similar(word)
    for (sim_word,score) in sim:
      w,sw = word.lower(),sim_word.lower()
      if (w in sw) or (sw in w):
        term.append((sim_word,score))
      else:
        cat.append((sim_word,score))
    word2vec_map[word] = {"cat":cat, "term":term}
  return word2vec_map

    # remove if toooo similar ie cat -> Cat 
    # EDGE CASE
    #httpwwwtourmongoliacomindexphpoptioncomcontentandampviewarticleandampid2753Alistofmongolianembassiesaconsulatesabroadandampcatid52andampItemid82andamplangen
    # I, I'll, motorists' haven't we'll
    # 'tourist' 'invitation' keep quotes since has different connotation 
    # Start of sentence: caps: 'OR' words capped for effect
    # 大东 大东


import json
def main():
  logging.basicConfig(format='%(asctime)s : %(levelname)s : %(message)s', level=logging.INFO)
  print("Creating w2v dictionary")
  c = post_content()
  print("Tokenize text")
  s = smart_tokenize(c)
  print("Compute similarity")
  m = cosine_similarity(s)
  print("Writing dictionary to file")
  with open('w2v_travel2.txt', 'w') as f:
    json.dump(data, f, ensure_ascii=False)
  return
