import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.tika.Tika;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.TagRatioParser;
import org.apache.tika.parser.ner.NamedEntityParser;
import org.apache.tika.sax.ToXMLContentHandler;
import org.json.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public class SWEETExtractor {
	static JSONObject idMap = new JSONObject();
	private static TagRatioParser parser = new TagRatioParser();
	private static Tika tika = null;
	private static String outputDir = "/home/siddharth/Desktop/SWEETMeasure";
	private int count = 0;
	
	
	
	public static void main(String args[]) {
		initializeMap();
		try {
			tika = new Tika(new TikaConfig("/home/siddharth/Desktop/tika-config.xml"));
		} catch (TikaException | IOException | SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Initialized Map");
		for(File file : new File(args[0]).listFiles()) {
			if(idMap.containsKey(file.getName())) {
				System.out.println(file.getAbsolutePath());
				extractSWEET(file);
			}
		}
	}
	
	public static void extractSWEET(File file) {
		Metadata metadata = new Metadata();
		ContentHandler contentHandler = new ToXMLContentHandler();
		ParseContext context = new ParseContext();
		try {
			parser.parse(new FileInputStream(file), contentHandler, metadata, context);
			extractSWEET(contentHandler.toString(), metadata, file.getName());
			
		} catch (IOException | SAXException | TikaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
private static void extractSWEET(String content, Metadata metadata, String fileName) {
		
		System.setProperty(NamedEntityParser.SYS_PROP_NER_IMPL, "org.apache.tika.parser.ner.regex.RegexNERecogniser");
		try {
//			System.out.println("String content:"+content);
			tika.parse(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)), metadata);

				Set<String> measurements = new HashSet<>(Arrays.asList(metadata.getValues("NER_SWEET_CONCEPT")));
				JSONArray array = null;
				if(!measurements.isEmpty()) {
					array = new JSONArray();
					for(String m : measurements) 
						array.put(m);
				}
				
				if(array !=null){
					JSONObject obj = new JSONObject();
					obj.put("id", idMap.get(fileName));
					obj.put("sweet_concepts", array);
					PrintWriter output = new PrintWriter(outputDir+"/"+fileName);
					output.println("["+obj.toJSONString()+"]");
					output.close();
					
				}
			
			
			System.clearProperty(NamedEntityParser.SYS_PROP_NER_IMPL);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void initializeMap() {
		System.out.println("--");
		JSONParser parser = new JSONParser();
		try {
			idMap = (JSONObject) parser.parse(FileUtils.readFileToString(new File("URL_MAP.txt")));
		} catch (ParseException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
