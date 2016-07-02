import os
import json

path = "/home/nimesh/599/TestSet/jpeg/"

data_parent = {}
data = {}
for file in os.listdir(path):
	try:
		jsonOutput = os.popen("java -Dtika.config=tika-config.xml -classpath tika-app/target/tika-app-1.12.jar org.apache.tika.cli.TikaCLI -m " + path + file).read()

		for line in jsonOutput.splitlines():
			if line is not "":
				line = line.strip()
				line = line.split(':', 1)
				data[line[0]] = line[1]
				#print data

				
		data_parent[file] = data
		data = {}
	except Exception, e:
		pass

with open('jpegextraCredit'+'.json', 'w') as outfile:
  	json.dump(data_parent, outfile)	