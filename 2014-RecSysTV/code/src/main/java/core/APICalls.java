package core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import model.Properties;
import model.Word;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.simple.*;
import org.json.simple.parser.JSONParser;

import com.eclipsesource.json.JsonObject;

public class APICalls {
	private static final String HOST 			= "https://api.boxfish.com";
	private static final String VERSION_HEADER 	= "application/vnd.com.boxfish-1+json";


	static void getAuthorization(){

		CloseableHttpClient http = HttpClients.createDefault();
		JSONParser parser = new JSONParser();

		// Get access token using client ID and secret.
		String url = HOST + "/oauth/token";
		HttpPost httpPost = new HttpPost(url);
		httpPost.addHeader("Accept", VERSION_HEADER);

		List<NameValuePair> params = new LinkedList<NameValuePair>();
		params.add(new BasicNameValuePair("client_id", Properties.getTestClientId()));
		params.add(new BasicNameValuePair("client_secret", Properties.getTestSecret()));
		params.add(new BasicNameValuePair("grant_type", "client_credentials"));
		String accessToken = null;


		// AUTH - ONLY NEEDS TO BE DONE ONCE
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(params));
			CloseableHttpResponse r = http.execute(httpPost);
			System.out.println("POST " + url + " => " + r.getStatusLine());

			HttpEntity e = r.getEntity();
			String response = EntityUtils.toString(e);
			JSONObject json = (JSONObject) parser.parse(response);
			System.out.println(json.toJSONString());

			// Get the access token.
			accessToken = (String) json.get("access_token");
		}
		catch (Exception e) {
			System.out.println("Failed to get access token with POST " + url + ": " + e.getMessage());
			System.exit(1);
		}

		System.out.println("the access token is: "+ accessToken);


	}

	// CALLS THE METRICS ENDPOINT WITH THE ANEW DATASET AS A QUERY
	// RETURNS THE WORD COUNTS AS IF IT WAS TEXT 
	static String getFeatures(String startTime, String endTime, List <Word> anew, String channel) throws ClientProtocolException, IOException, org.json.simple.parser.ParseException{


		List<String> dictionaryAnew 		= new ArrayList<String>();
		for (Word wi : anew) dictionaryAnew.add(anew.indexOf(wi), wi.getWord()); // create the index

		//System.out.println(dictionaryAnew);
		String query 	= "";
		String queryII 	= "";
		String queryIII = "";
		int i = 0;
		for(Word wi: anew){
			if (i <= 1000) query+=wi.getWord()+",";
			if ((i > 1000) && (i < 1500)) queryII+=wi.getWord()+",";
			if (i >= 1500) queryIII+=wi.getWord()+",";
			i++;
		}
		query		= query.substring(0, query.length()-1);
		queryII		= queryII.substring(0, queryII.length()-1);
		queryIII	= queryIII.substring(0, queryIII.length()-1);

		String sentence ="";
		String url ="";


		for (int y = 0 ; y<3; y++){
			// need to divide the query in 3 cause it's too long
			if(y==0) url = "/metrics/channel/"+query+"/"+channel+"/boxfish/"+startTime+"/"+endTime;
			if(y==1) url =  "/metrics/channel/"+queryII+"/"+channel+"/boxfish/"+startTime+"/"+endTime;
			if(y==2) url =  "/metrics/channel/"+queryIII+"/"+channel+"/boxfish/"+startTime+"/"+endTime;
			JSONArray json = doHttpRequest(url);

			for(Object jsonI : json){
				JsonObject eachLine = JsonObject.readFrom(jsonI.toString());
				int count 			= eachLine.get("count").asInt();
				String keyword 		= eachLine.get("keyword").asString();
				for(int x=0 ; x < count; x++) sentence+= keyword+" ";
			}
		}
		return sentence;

	}

	// GENERATES THE REQUEST TO THE BOXFISH API
	// RETURNS A JSON OBJECT
	static JSONArray  doHttpRequest(String url) throws ClientProtocolException, IOException, org.json.simple.parser.ParseException{

		CloseableHttpClient http 	= HttpClients.createDefault();
		JSONParser parser 			= new JSONParser();
		url 						= HOST + url;
		HttpPost httpPost 			= new HttpPost(url);
		httpPost.addHeader("Accept", VERSION_HEADER);
		HttpGet httpGet 			= new HttpGet(url);


		//delete when uploading!
		httpGet.addHeader("Authorization", "Bearer " + Properties.getToken());
		httpGet.addHeader("Accept", VERSION_HEADER);
		CloseableHttpResponse r 	= http.execute(httpGet);

		System.out.println(r.getStatusLine() +" // GET " + url + " => " + r.getStatusLine());

		HttpEntity e 				= r.getEntity();
		String response 			= EntityUtils.toString(e);
		JSONArray json 				= (JSONArray) parser.parse(response);

		return json;

	}
}

