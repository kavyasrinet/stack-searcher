"""
Converts from the Stack exchange xml format given to the XML dormat needed by Solr
Usage Example:

python convert_xml.py dataset_sample/travel.stackexchange.com/Posts.xml ./Posts 
"""

import sys
import xml.etree.ElementTree as ET
import re


def main():
	reload(sys) 
	sys.setdefaultencoding('utf8')
	
	TAG_RE = re.compile(r'<[^>]+>')
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
			if (field =="Body"):
				att[field] = re.sub('<[^>]*>','',str(att[field]))
			elif (field =="Tags"):
				att[field] = re.sub('<|>',' ',str(att[field]))
			s +="<field name = \"" + field +"\">" + att[field]  + "</field>\n"
		s+="</doc>\n"
		f.write(s.encode('utf-8'))
	f.write("</add>\n")
	f.close()
main()