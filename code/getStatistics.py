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
			postType = "Question"
		else:
			postType = "Answer"
		postMap[postId] = postType
		viewCount =0
		
		if(typeId==1):
			viewCount = attbs["ViewCount"]
		if(viewCount!=0):
			postAtt[postId] = viewCount


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
			if((postMap[post1], postMap[post2]) in questionMap):
				questionMap[(postMap[post1], postMap[post2])] +=1
			else:
				questionMap[(postMap[post1], postMap[post2])] =1

			if((postMap[post1], postMap[post2], relation) in questionMap):
				questionMap[(postMap[post1], postMap[post2], relation)] +=1
			else:
				questionMap[(postMap[post1], postMap[post2], relation)] =1
		if(post2 not in stats ):
			stats[str(post2)] = 1
		else:
			stats[str(post2)] += 1


	sorted_stats = sorted(stats.items(), key=lambda x: x[1], reverse=True)
	print "The format of the following data is :"
	print "Related_Post_id Number_of_links View_count"
	for a in sorted_stats[:15]:
		if(postAtt[a[0]]!=0):
			print a[0],a[1],postAtt[a[0]]

	total = len(postMap)


	# print "Total number of posts is : "+str(total)
	# print "The format is : PostType1 , PostType2 , Relation between posts, count "
	# print "The statistics in numbers :"
	# for key in questionMap:
	# 	print(key, questionMap[key])

	# print "The format is : PostType1 , PostType2 , Relation between posts, percentage  "
	# print "The statistics in percentage: "
	# for key in questionMap:
	# 	print(key,(questionMap[key]*1.0)/ total)



	
		

main()