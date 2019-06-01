import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.TreeSet;

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
	public Date todaysPreviousVideoGetTimestamp = null; // if null, get all, else get from this variable to current time
	public boolean loadingVideos = false;
	
	public boolean recording = false;
	// key for videos will be YYYY-MM-DD
	public TreeMap<String, ArrayList<ShinobiVideo>> videoPlaylist = new TreeMap<String, ArrayList<ShinobiVideo>>();
		
	@Override
	public String toString() {
		return "ShinobiMonitor [mid=" + mid + ", name=" + name + ", stream=" + stream + "]";
	}
	
	/**
	 * Generate an ArrayList of URL's for use as a playlist
	 * @return ArrayList<String> containing URL's to be used
	 */
	public ArrayList<String> generatePlaylist() {
		ArrayList<String> playlist = new ArrayList<String>();
		
		for (ArrayList<ShinobiVideo> videoList : videoPlaylist.values()) {
			for (ShinobiVideo sv : videoList) {
				playlist.add(sv.href);
			}
		}
		
		return playlist;
	}
	
	/**
	 * Loads video data from API
	 * @param date - start date for getting video data. Time will be reset to 00:00:00 to get data for the day
	 */
	public void LoadVideos(Date date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
		SimpleDateFormat shinobiFormat = new SimpleDateFormat("yyyy-MM-dd'''T'''HH:mm:ss");
		
		// if today
		if (dateFormat.format(date).equals(dateFormat.format(Calendar.getInstance().getTime()))) {
			if (todaysPreviousVideoGetTimestamp == null) {
				// get all videos for today, and save time last retrieved
				Date endDate = Calendar.getInstance().getTime(); // now
				
				Calendar cal = Calendar.getInstance();
				cal.set(Calendar.YEAR, cal.get(Calendar.YEAR));
				cal.set(Calendar.MONTH, cal.get(Calendar.MONTH));
				cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH));
				cal.set(Calendar.HOUR_OF_DAY, 0);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
				
				Date startDate = cal.getTime();
				
				GetVideos(startDate,endDate);
				
				// record timestamp
				todaysPreviousVideoGetTimestamp = endDate;
			} else {
				// get all videos since last update 
				Date endDate = Calendar.getInstance().getTime(); // now
				Date startDate = todaysPreviousVideoGetTimestamp; // previous timestamp for today
				
				GetVideos(startDate,endDate);
				
				// record timestamp
				todaysPreviousVideoGetTimestamp = endDate;
			}
		} else {
			// videos not today
			
			// if not in videoPlaylist, load
			if (!videoPlaylist.containsKey(dateFormat.format(date))) {
				
				Calendar cal = Calendar.getInstance();
				// set calendar date to requested date
				cal.setTime(date);
				// reset time to 00:00:00
				cal.set(Calendar.HOUR_OF_DAY, 0);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);				
				Date startDate = cal.getTime();				
				
				// reset time to requested date
				cal.setTime(date);
				// add a day
				cal.add(Calendar.DATE, 1);
				// reset time to 00:00:00
				cal.set(Calendar.HOUR_OF_DAY, 0);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);				
				
				Date endDate = cal.getTime();
				
				GetVideos(startDate,endDate);
			}
		}	
	}

	public int getVideoFileIndex(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	
		Calendar cal = Calendar.getInstance();
		cal.setTimeZone(cal.getTimeZone());
		cal.setTime(date);
		
		if (videoPlaylist.containsKey(sdf.format(date))) {
			for (int i = 0; i < videoPlaylist.get(sdf.format(date)).size(); i++) {
				Calendar endTime = Calendar.getInstance();
				endTime.setTime(videoPlaylist.get(sdf.format(date)).get(i).endTime);
				endTime.setTimeZone(endTime.getTimeZone());
				
				Calendar startTime = Calendar.getInstance();
				startTime.setTime(videoPlaylist.get(sdf.format(date)).get(i).startTime);
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
			
		System.out.println("Filename: "+videoPlaylist.get(sdf.format(date)).get(index).filename);
			
		return videoPlaylist.get(sdf.format(date)).get(index).filename;
	}
	
	// mpv uses seconds for seek pos
	public int getVideoFileSeekPos(Date date, int index) {
		if (index == -1) {
			return 0;
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		
		long seconds = (date.getTime() - videoPlaylist.get(sdf.format(date)).get(index).startTime.getTime()) / 1000;
		
		System.out.println("File seek pos: "+seconds);
		return (int) seconds;
		
	}
	
	/**
	 * Load videos from Shinobi API
	 * 
	 * if (date is not today) AND videos have already been loaded for that date, do nothing for that date
	 * if (date is not today) AND (video does not contain any videos for the date) then get all videos for the date
	 * if (date is today) AND (previous get video timestamp is null) then get all videos for today
	 * if (date is today) AND (previous get video timestamp is not null) then get all videos for today since latest timestamp
	 * if (date is before today) AND (day before date videos is empty) get videos for day before date (so seeking before 00 will work)
	 * if (date is before today) AND (day after date videos is empty) get videos for day after date (so seeking after 23:59:59 will work)
	 * 
	 * @param date
	 */
	public void GetVideosAsync(Date date) {
		
	}
		
	public void GetVideos(Date startDate, Date endDate) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			SimpleDateFormat shinobiFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
						
			String url = "/"+api_key+"/videos/"+group_key+"/"+mid+"?start="+shinobiFormat.format(startDate)+"&end="+shinobiFormat.format(endDate);
			
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
			    	
			    	ShinobiVideo sv = new ShinobiVideo();
			    	sv.startTime = sv.parseShinobiTime(startTime);
			    	sv.endTime = sv.parseShinobiTime(endTime);
			    	sv.filename = video_data.get("filename").getAsString();
			    	sv.href = video_data.get("href").getAsString();
			    	
			    	if (videoPlaylist.get(date) == null) {
			    		videoPlaylist.put(date, new ArrayList<ShinobiVideo>());
			    	}
			    	videoPlaylist.get(date).add(sv);
			    	
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