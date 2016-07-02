import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.tika.Tika;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.TagRatioParser;
import org.apache.tika.parser.geo.topic.GeoParser;
import org.apache.tika.parser.ner.NamedEntityParser;
import org.apache.tika.sax.ToXMLContentHandler;
import org.json.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public class ExtractMeasureAndLocation {
	private static TagRatioParser parser = new TagRatioParser();
	private static GeoParser geoParser = new GeoParser();
	private static Tika tika ;
	private static String outputDir;
	private static HttpClient httpclient = HttpClients.createDefault();
	private static HttpPost httppost = new HttpPost("http://localhost:8080/a");
	private static JSONParser jsonParser = new JSONParser();
	private static JSONObject jsonObj = null;
	static boolean not_skip = true;
	
	
	public static void main(String args[]) throws TikaException, IOException, SAXException {
		tika = new Tika (new TikaConfig(args[1]));
		outputDir = args[2];
		processFile(new File(args[0]));
	}
	
	private static void processFile(File f) {
		File files [] = f.listFiles();
		for(File file : files) {
			if(file.isDirectory())
				processFile(file);
			else {
				extractContent(file);
			}
		}
	}
	
	private static void extractContent (File file) {
//		if(not_skip) {
//			if(file.getName().equals("2AAE3F8F5232EA85F79DB84733C99D9AA3114AE1687AD10B60C2C4FF6D842E72"))
//				not_skip = false;
//			return;
//		}
		System.out.println(file.getAbsolutePath());
		Metadata metadata = new Metadata();
		ContentHandler contentHandler = new ToXMLContentHandler();
		ParseContext context = new ParseContext();
		try {
			parser.parse(new FileInputStream(file), contentHandler, metadata, context);
			JSONObject obj = extractDetails(contentHandler.toString(), metadata, file.getName());
			String spaced_content = contentHandler.toString().replace("\n", " ");
			geoParser.parse(new ByteArrayInputStream(spaced_content.getBytes(StandardCharsets.UTF_8)), contentHandler, metadata);
			printLocations(metadata, file.getName(), obj);
			
		} catch (IOException | SAXException | TikaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void printLocations(Metadata metadata, String fileName, JSONObject obj) {
		
		if(metadata.get("Geographic_NAME")!=null) {
			if(obj ==null)
				obj = new JSONObject();
			obj.put("geographic_name",metadata.get("Geographic_NAME"));
			obj.put("geographic_latitude", metadata.get("Geographic_LATITUDE"));
			obj.put("geographic_longitude", metadata.get("Geographic_LONGITUDE"));
		}
		
		if(metadata.get("Optional_NAME1")!=null) {
			if(obj ==null)
				obj = new JSONObject();
			obj.put("optional_name1",metadata.get("Optional_NAME1"));
			obj.put("optional_latitude1", metadata.get("Optional_LATITUDE1"));
			obj.put("optional_longitude1", metadata.get("Optional_LONGITUDE1"));
		}
		
		if(metadata.get("Optional_NAME2")!=null) {
			if(obj ==null)
				obj = new JSONObject();
			obj.put("optional_name2",metadata.get("Optional_NAME2"));
			obj.put("optional_latitude2", metadata.get("Optional_LATITUDE2"));
			obj.put("optional_longitude2", metadata.get("Optional_LONGITUDE2"));
		}
		
		PrintWriter output;
		try {
			output = new PrintWriter(outputDir+"/"+fileName);
			if(obj !=null)
				output.println(obj.toJSONString());
			else
				output.print("");
			output.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static JSONArray getMeasureFromMetaData(String unitType, Metadata metadata) {
		Set<String> measurements = new HashSet<>(Arrays.asList(metadata.getValues("NER_R_"+unitType+"_MEASURE")));
		JSONArray array = null;
		if(!measurements.isEmpty()) {
			array = new JSONArray();
			for(String m : measurements) 
				array.put(m);
		}
		return array;
	}
	
	private static JSONObject extractDetails(String content, Metadata metadata, String fileName) {
		
		System.setProperty(NamedEntityParser.SYS_PROP_NER_IMPL, "org.apache.tika.parser.ner.regex.RegexNERecogniser");
		try {
			tika.parse(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)), metadata);
			JSONArray temp_obj = getMeasureFromMetaData("TEMP", metadata);
			JSONArray time_obj = getMeasureFromMetaData("TIME", metadata);
			JSONArray mass_obj = getMeasureFromMetaData("MASS", metadata);
			JSONArray length_obj = getMeasureFromMetaData("LENGTH", metadata);
			
			JSONObject obj = new JSONObject();
			if(temp_obj != null)
				obj.put("temperature_measurements", temp_obj);
			if(time_obj != null)
				obj.put("time_measurements", time_obj);
			if(mass_obj != null)
				obj.put("mass_measurements", mass_obj);
			if(length_obj != null)
				obj.put("length_measurements", length_obj);		
			PrintWriter output = new PrintWriter(outputDir+"/"+fileName);
			output.println(obj.toJSONString());
			output.close();
			System.clearProperty(NamedEntityParser.SYS_PROP_NER_IMPL);
			return obj;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
