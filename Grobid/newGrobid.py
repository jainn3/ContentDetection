import os
import json

path = "/home/nimesh/599/ClassifiedData/pdf"
count = 0
f = open('myfile','w')

for file in os.listdir(path):
	if count < 14799:
		count = count +1
		continue
	try:

		jsonFromGrobid = os.popen("java -classpath $HOME/src/grobidparser-resources/:tika.jar org.apache.tika.cli.TikaCLI --config=$HOME/src/grobidparser-resources/tika-config.xml -J /home/nimesh/599/ClassifiedData/pdf/" + file).read()
		print count
		f.write(str(count) + '\n')
		jsonOutput = json.loads(jsonFromGrobid)[0]
		count = count + 1
		
		if "Author" in jsonOutput:
			print jsonOutput["Author"]
			printstrn = os.popen("python scholar.py -c 20 --author " + jsonOutput["Author"]).read()	
			
			print "raw output"
			print printstrn
				
			data_parent = {}
			data = {}
			cnt = 0
			for line in printstrn.splitlines():
				if line is not "":
					line = line.strip()
					line = line.split(" ", 1)
					data[line[0]] = line[1]
				else :
					json_data = json.dumps(data)
					data_parent[str(cnt)] = data
					cnt = cnt + 1
					data = {}

			print "outside for"
			print data_parent
			data_parent["count"] = str(cnt)
			data_parent["Author"] = jsonOutput["Author"]

			if "grobid:header_FullAffiliations" in jsonOutput:
				data_parent["Affiliation"] = jsonOutput["grobid:header_FullAffiliations"] 
				#print data_parent
			with open(file + '-grobid-' +'.json', 'w') as outfile:
	   				json.dump(data_parent, outfile)
	   				
		else:
			print "No Author"
	
	except Exception, e:
				print "Exception"
				pass		
f.close()		


