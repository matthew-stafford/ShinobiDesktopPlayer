import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

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

public class ShinobiMonitor {
	public String mid;
	public String name;
	public String stream;
	public String api_key;
	public String group_key;
	public String host;
	public Date start_date, end_date;// for searching videos (must be done when selecting day or takes too long :/)
	public boolean loadingVideos = false;
	
	public boolean recording = false;
	// key for videos will be YYYY-MM-DD
	public HashMap<String,ArrayList<ShinobiVideo>> videos = new HashMap<String,ArrayList<ShinobiVideo>>();
	
	@Override
	public String toString() {
		return "ShinobiMonitor [mid=" + mid + ", name=" + name + ", stream=" + stream + "]";
	}
	
	public void LoadVideos(Date start_date) {
		this.start_date = start_date;
		loadingVideos = true;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		
		if (sdf.format(start_date).equals(sdf.format(Calendar.getInstance().getTime()))) {
			// if day == today then clear since updated videos could be available for playback
			// videos.clear();
		} 
				
		Calendar cal = Calendar.getInstance();
		cal.setTime(start_date);
		cal.add(Calendar.DATE, 1);
		this.end_date = cal.getTime();
		
		// only load if not already in hashmap
		if (!videos.containsKey(sdf.format(start_date))) {
			get_videos();
		}
		loadingVideos = false;
		
	}

	public int getVideoFileIndex(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	
		Calendar cal = Calendar.getInstance();
		cal.setTimeZone(cal.getTimeZone());
		cal.setTime(date);
		
		if (videos.containsKey(sdf.format(date))) {
			for (int i = 0; i < videos.get(sdf.format(date)).size(); i++) {
				Calendar endTime = Calendar.getInstance();
				endTime.setTime(videos.get(sdf.format(date)).get(i).endTime);
				endTime.setTimeZone(endTime.getTimeZone());
				
				Calendar startTime = Calendar.getInstance();
				startTime.setTime(videos.get(sdf.format(date)).get(i).startTime);
				startTime.setTimeZone(startTime.getTimeZone());
				
				
				if (cal.after(startTime) && cal.before(endTime)) {
					return i;
				}			
			}		
		}
		return -1;
	}
	
	public String getVideoFilename(Date date, int index) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		if (index == -1) {
			return null;
		}
			
		System.out.println("Filename: "+videos.get(sdf.format(date)).get(index).filename);
			
		return videos.get(sdf.format(date)).get(index).filename;
	}
	
	// mpv uses seconds for seek pos
	public int getVideoFileSeekPos(Date date, int index) {
		if (index == -1) {
			return 0;
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		
		long seconds = (date.getTime() - videos.get(sdf.format(date)).get(index).startTime.getTime()) / 1000;
		
		System.out.println("File seek pos: "+seconds);
		return (int) seconds;
		
	}
		
	public void get_videos() {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
						
			String url = "/"+api_key+"/videos/"+group_key+"/"+mid+"?start="+sdf.format(start_date)+"T00:00:00&end="+sdf.format(end_date)+"T00:00:00";
			
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
			    
			    JsonElement jelement = new JsonParser().parse(json);
			    JsonObject jobject = jelement.getAsJsonObject();
			    JsonArray jarray = jobject.getAsJsonArray("videos");
			    for (JsonElement element : jarray) {
			    	JsonObject video_data = element.getAsJsonObject();

			    	String startTime = video_data.get("time").getAsString();
			    	String endTime = video_data.get("end").getAsString();
			    	
			    	String date = startTime.substring(0, startTime.indexOf("T"));
			    	
			    	System.out.println("Date="+date);
			    	
			    	String mid = video_data.get("mid").getAsString();
			    	
			    	ShinobiVideo sv = new ShinobiVideo();
			    	sv.startTime = sv.parseShinobiTime(startTime);
			    	sv.endTime = sv.parseShinobiTime(endTime);
			    	sv.filename = video_data.get("filename").getAsString();
			    	sv.href = video_data.get("href").getAsString();
			    	
			    	if (videos.get(date) == null) {
			    		videos.put(date, new ArrayList<ShinobiVideo>());
			    	}
			    	videos.get(date).add(sv);
			    	
			    }
			    
			    System.out.println("Finished loading videos for "+mid);

			    
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
		
	
	
}
