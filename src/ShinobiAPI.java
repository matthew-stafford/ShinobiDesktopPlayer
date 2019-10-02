import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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

	private ShinobiSite site;
	
	public ShinobiAPI(ShinobiSite site) {
		this.site = site;
	}

	public TreeMap<String, ShinobiMonitor> checkAPIValidAndGetMonitors() {
		System.out.println("Loading monitors");
		try {
			TreeMap<String, ShinobiMonitor> monitors = new TreeMap<String, ShinobiMonitor>();
			
			
			String url = site.getBaseURL()+"/"+site.apiKey+"/monitor/"+site.groupKey;
			
			CloseableHttpClient httpclient = HttpClients.createDefault();
			
			System.out.println("Visiting "+url);
			HttpGet httpGet = new HttpGet(url);
			
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
				    	return null;
				    }
			    } catch (Exception e) {
			    	// Should throw an illegal state exception if api key is correct
			    }
			    			    
			    // api key works, retrieve monitor data
			    JsonElement jelement = new JsonParser().parse(json);
			    JsonArray jarray = jelement.getAsJsonArray();
			    for (JsonElement element : jarray) {
			    	JsonObject monitor_data = element.getAsJsonObject();
			    	ShinobiMonitor monitor = new ShinobiMonitor(site);
			    	monitor.mid = monitor_data.get("mid").getAsString();
			    	monitor.name = monitor_data.get("name").getAsString();
			    	if (monitor_data.get("streams").getAsJsonArray().size() > 0) {
				    	monitor.stream = site.getBaseURL()+""+monitor_data.get("streams").getAsJsonArray().get(0).getAsString();
			    	}
			    	
			    	if (monitor_data.get("status") != null) {
				    	if (monitor_data.get("status").getAsString().equalsIgnoreCase("recording")) {
				    		monitor.recording = true;
				    	}
			    	}
			    	monitors.put(monitor.mid, monitor);
			    }
			    
			    return monitors;			    
			    
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
				
		return null;
	}
	
	public void getMotionEventData(String monitorId, Date date, int limit) {
		if (monitorId == null || monitorId.trim().length() < 1) {
			return;
		}
		if (date == null ) {
			return;
		}
		
		// create calendar instance to create start/end dates from the date provided
		Calendar cal = Calendar.getInstance();
		// set date to the date provided
		cal.setTime(date);
		
		// clear the hours,minutes,seconds for start date and end date
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		
		// create start date
		Date startDate = cal.getTime();
		
		// add 1 day to calendar to get end date
		cal.add(Calendar.DATE, 1);
		Date endDate= cal.getTime();
		
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			SimpleDateFormat key = new SimpleDateFormat("yyMMdd");
			
			String url = site.getBaseURL()+"/"+site.apiKey+"/events/"+site.groupKey+"/"+monitorId+"?start="+sdf.format(startDate)+"&end="+sdf.format(endDate)+"&limit="+limit;
			
			CloseableHttpClient httpclient = HttpClients.createDefault();
			
			System.out.println("Visiting "+url);
			HttpGet httpGet = new HttpGet(url);
			
			
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
			    			    
			    // api key works, retrieve monitor data
			    JsonElement jelement = new JsonParser().parse(json);
			    JsonArray jarray = jelement.getAsJsonArray();
			    for (JsonElement element : jarray) {
			    	JsonObject monitor_data = element.getAsJsonObject();

			    	MotionEvent me = new MotionEvent();
			    	me.timeInMilliseconds = monitor_data.get("time").getAsLong();			    	
			    	Date d = new Date(me.timeInMilliseconds);			    	
			    	site.getMonitors().get(monitorId).motionDays.add(key.format(d));
			    	
			    	if (monitor_data.get("details").getAsJsonObject().isJsonObject()) {
			    		me.confidence = monitor_data.get("details").getAsJsonObject().get("confidence").getAsByte();
			    	}
			    	if (site.getMonitors().get(monitorId).motionEvents.get(key.format(d)) == null) {
			    		site.getMonitors().get(monitorId).motionEvents.put(key.format(d), new ArrayList<MotionEvent>());
			    	}
			    	
			    	site.getMonitors().get(monitorId).motionEvents.get(key.format(d)).add(me);		    	
			    	System.out.println("Monitor event added to motion events");
			    }			    	    
			    
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
	}
	
	public void getVideoData(Date date) {
		// clear prev video data
		for (ShinobiMonitor monitor:site.getMonitors().values()) {
			if (monitor.recording) {				
				monitor.LoadVideos(date);
			}
		}
	}
	
	
}