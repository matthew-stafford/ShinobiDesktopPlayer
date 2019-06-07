import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.TreeMap;

import javax.swing.JOptionPane;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ShinobiAPI {

	private String api_key;
	private String host;
	private String group_key;
	private TreeMap<String, ShinobiMonitor> monitors = new TreeMap<String, ShinobiMonitor>();
	
	public ShinobiAPI(String api_key, String host, String group_key) {
		this.api_key = api_key;
		this.host = host;
		this.group_key = group_key;
	}

	public boolean checkAPIValidAndGetMonitors() {
		try {
			String url = "/"+api_key+"/monitor/"+group_key;
			
			CloseableHttpClient httpclient = HttpClients.createDefault();
			
			System.out.println("Visiting "+host+url);
			HttpGet httpGet = new HttpGet(host+url);
			
			CloseableHttpResponse response = httpclient.execute(httpGet);
			try {
			    System.out.println(response.getStatusLine());
			    
			    HttpEntity entity = response.getEntity();
			    BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()));
			    String json = "", line = null;
			    while ((line = reader.readLine()) != null) {
			    	json = json+line;
			    }
			    EntityUtils.consume(entity);

			    try {
				    JsonElement jelement = new JsonParser().parse(json);
				    JsonObject  jobject = jelement.getAsJsonObject();
				    jobject = jobject.getAsJsonObject();
				    String auth = jobject.get("msg").getAsString();
				    if (auth.equalsIgnoreCase("Not Authorized")) {
				    	return false;
				    }
			    } catch (Exception e) {
			    	// Should throw an illegal state exception if api key is correct
			    }
			    			    
			    // api key works, retrieve monitor data
			    JsonElement jelement = new JsonParser().parse(json);
			    JsonArray jarray = jelement.getAsJsonArray();
			    for (JsonElement element : jarray) {
			    	JsonObject monitor_data = element.getAsJsonObject();
			    	ShinobiMonitor monitor = new ShinobiMonitor();
			    	monitor.mid = monitor_data.get("mid").getAsString();
			    	monitor.name = monitor_data.get("name").getAsString();
			    	if (monitor_data.get("streams").getAsJsonArray().size() > 0) {
				    	monitor.stream = host+""+monitor_data.get("streams").getAsJsonArray().get(0).getAsString();
			    	}
			    	
			    	if (monitor_data.get("status") != null) {
				    	if (monitor_data.get("status").getAsString().equalsIgnoreCase("recording")) {
				    		monitor.recording = true;
				    	}
			    	}
			    	monitor.api_key = api_key;
			    	monitor.group_key = group_key;
			    	monitor.host = host;
			    	monitors.put(monitor.mid, monitor);
			    }
			    
			    return true;			    
			    
			} finally {
			    response.close();
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("EMSG="+e.getLocalizedMessage());
			if (e.getLocalizedMessage().equals("No route to host (Host unreachable)")) {
            	JOptionPane.showMessageDialog(null, "Could not establish connection to host. Check server is running and that your host string is correct.\n\nError: No route to host (Host unreachable)", "Error", JOptionPane.ERROR_MESSAGE);	
			}
		}
				
		return false;
	}
	
	public void getVideoData(Date date) {
		// clear prev video data
		for (ShinobiMonitor monitor:monitors.values()) {
			if (monitor.recording) {				
				monitor.LoadVideos(date);
			}
		}
	}
	
	public TreeMap<String, ShinobiMonitor> getMonitors() {
		return this.monitors;
	}
	
}