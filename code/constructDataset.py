import sys
import xml.etree.ElementTree as ET
import operator

def main():
	postLink = sys.argv[1]
	posts = sys.argv[2]

	postMap ={}
	postAtt = {}
	tree  = ET.parse(posts)
	root = tree.getroot()
	count =0
	ftrainX = open("datasetX.txt","w")
	ftrainY = open("datasetY.txt","w")

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
			info.append(body)
			dataset[postId] = info
		else:
			postType = "Answer"
		
		postMap[postId] = postType


	tree = ET.parse(postLink)
	root = tree.getroot()

	questionMap = {}
	stats = {}
	print root.tag
	for child in root:
		attbs = child.attrib
		rel = int(attbs["LinkTypeId"])
		relation=""
		if(rel==1):
			relation = "Linked"
		elif (rel==3):
			relation = "Duplicate"
		post1 = attbs["PostId"]
		post2 = attbs["RelatedPostId"]
		if(post1 in postMap and post2 in postMap):
			if(postMap[post1] == "Question" and postMap[post2]=="Question"):
				ftrainX.write(str(dataset[post1]))
				ftrainX.write("\n")
				ftrainY.write(str(dataset[post2]))
				ftrainY.write("\n")

	ftrainX.close()
	ftrainY.close()
	
		

main()