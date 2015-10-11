import sys
import xml.etree.ElementTree as ET
import operator

def process_body(body):
	body = " ".join(body.split("\n"))
	if "Possible Duplicate" in body:
		i = body.find("</blockquote>")
		body = body[i+ len("</blockquote>") :]
	return body

def main():
	postLink = sys.argv[1]
	posts = sys.argv[2]

	postMap ={}
	postAtt = {}
	tree  = ET.parse(posts)
	root = tree.getroot()
	count =0
	ftrainX = open(sys.argv[3],"w")
	ftrainY = open(sys.argv[4],"w")

	dataset ={}
	for child in root:
		count = count +1
		attbs = child.attrib
		postId = attbs["Id"]
		typeId = int(attbs["PostTypeId"])
		postType =""

		if(typeId==1):
			info = []
			postType = "Question"
			body = attbs["Body"]
			title = attbs["Title"]
			Id = postId

			info.append(Id)
			info.append(title)
			info.append(process_body(body))
			dataset[postId] = info
		else:
			postType = "Answer"
		
		postMap[postId] = postType

	tree = ET.parse(postLink)
	root = tree.getroot()

	seen_links= set()
	links = {}	
	for child in root:
		attbs = child.attrib
		post1 = attbs["PostId"]
		post2 = attbs["RelatedPostId"]
		if (post1,post2) not in seen_links and post1 in postMap and post2 in postMap :
				seen_links.add((post1,post2))	
				if post1 not in links:
					links[post1] = [post2]
				else:
					links[post1].append(post2)

				if postMap[post1] == "Question" and postMap[post2]=="Question":
					ftrainX.write("\t".join(dataset[post1]).encode('utf-8') + "\n")
					ftrainY.write("\t".join(dataset[post2]).encode('utf-8') + "\n")
	ftrainX.close()
	ftrainY.close()		
main()