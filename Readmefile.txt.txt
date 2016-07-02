Effort Distribution and Contribution to Assignment :
There was 10-15% contrbution to this entire assignment by Suhas Suresh (suhassur@usc.edu). Rest all the work is done by Siddharth Bhayani (sbhayani@usc.edu) and Nimesh Jain (nimeshja@usc.edu). 
Suhas's work is of no input in this assignment.

1. Grobid Parser
a) Start the grobid service using below command
cd $HOME/src/grobid/grobid-service
mvn -Dmaven.test.skip=true jetty:run-war
b) Start the Tika app with input as publication file.

The above two steps are integrated in a python file which iterates through all the publications in a directory fetches the Author name and retrieves  the 20 related publication of that Author using Google Scholar API.

2. Similarity
Clone the tika-similarity repo and execute k-means,  jaccard, cosine and edit distance clustering techniques on the metadata fetched and indexed by solr.

3. Solr indexing and GeoParsing
Start Lucene server
Start a solr server on 8983
Start a solr server for indexed data on any port apart from 8983 say 8985, create a core, build schema and index data
Start tika server
Start Django Server which pulls data from 8985 and stores in 8983

4. SWEETExtractor.java
Args[0] - directory from which map(url to file mapping) needs to be initialized.

5. TagRatioParser.java
This is a parser written in java. One needs to create it's object and call parse method to use it. 

5. ExtractMeasureAndLocation
Args[0] - input directory containing the files
Args[1] - path to tika-config.xml
Args[2] - output directory

6. D3 Visualizations
We have generated 5 different d3 visualizations. 
All the visualizations are in the Graph folder with self explanatory file names.
There is a sample data.json is a subset of the actual  data that can be visualized from this graph. This json file also describes the structure of json file required for these visualizations.
