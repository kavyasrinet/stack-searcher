"""
Converts from the Stack exchange xml format given to the XML dormat needed by Solr
Usage Example:

python convert_xml.py dataset_sample/travel.stackexchange.com/Posts.xml ./Posts 
"""

import sys
import xml.etree.ElementTree as ET
import re

reload(sys) 
sys.setdefaultencoding('utf8')


old_xml_file = sys.argv[1]
new_xml_file = sys.argv[2]

f = open(new_xml_file,"w")
f.write("<add>\n")	
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

		s +="<field name = \"" + field +"\">" + att[field]  + "</field>\n"
	s+="</doc>\n"
	f.write(s.encode('utf-8'))

f.write("</add>\n")
f.close()
