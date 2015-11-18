"""
Get vocabulary fo all the posts.

python convert_xml.py dataset_sample/travel.stackexchange.com/Posts.xml
"""

import sys
import xml.etree.ElementTree as ET
import re

reload(sys) 
sys.setdefaultencoding('utf8')


old_xml_file = sys.argv[1]

vocab_file = sys.argv[2]

all_words = set()
tree  = ET.parse(old_xml_file)
root = tree.getroot()
chars_to_remove = ['<', '>']
for child in root:
	s = "<doc>"
	att = child.attrib
	for field in child.attrib:
		att[field] = re.sub('&', "and", att[field])
		if field =="Body" or field =="AboutMe":

			if "Possible Duplicate" in att[field]:
				i = att[field].find("</blockquote>")
				att[field] = att[field][i+ len("</blockquote>") :]

			att[field] = re.sub('<[^>]*>','',str(att[field]))

		else :
			att[field] = re.sub('<|>',' ',str(att[field]))

		if field == 'Title' or field == 'Body':
			s = re.sub('[^a-zA-Z\s]+', ' ', att[field])
			for word in set(s.lower().strip().split()):
				all_words.add(word)

print len(all_words)
with open(vocab_file,'w') as f:
	for word in sorted(all_words):
		if word:
			f.write(word+"\n")