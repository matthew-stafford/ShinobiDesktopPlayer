import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ShinobiVideo implements Comparable<ShinobiVideo> {
	public Date endTime,startTime;
	public String timeZone;
	public String href;
	public String filename;
	
	
	public Date parseShinobiTime(String shinobiTime) {
		// parse date from 2019-20-01T13:00:00+7:00 to an actual date/time
		String date = shinobiTime.substring(0, shinobiTime.indexOf("T"));
		String time="";
		if (shinobiTime.contains("+") || shinobiTime.contains("-")) {
			// has timezone
			if (shinobiTime.contains("+")) {
				time = shinobiTime.substring(shinobiTime.indexOf("T")+1,shinobiTime.indexOf("+"));
				timeZone = shinobiTime.substring(shinobiTime.indexOf("+"));
			} else if (shinobiTime.contains("-")) {
				time = shinobiTime.substring(shinobiTime.indexOf("T")+1, shinobiTime.indexOf("-"));	
				timeZone= shinobiTime.substring(shinobiTime.indexOf("-"));
			}
		} else {
			time = shinobiTime.substring(shinobiTime.indexOf("T")+1,shinobiTime.length());
			timeZone="";
		}
		
		String timezone = shinobiTime.substring(shinobiTime.indexOf("+"), shinobiTime.length());
		
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date returnDate = null;
		try {
			 returnDate = dateFormat.parse(date +" "+time);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		if (returnDate == null) {
			System.out.println("WTF DATE IS NULL BRUH");
			System.out.println("date="+date+" time="+time+" tz="+timeZone);
		}
		
		return returnDate;
		
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((href == null) ? 0 : href.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ShinobiVideo other = (ShinobiVideo) obj;
		if (href == null) {
			if (other.href != null)
				return false;
		} else if (!href.equals(other.href))
			return false;
		return true;
	}


	@Override
	public int compareTo(ShinobiVideo sv) {
		return sv.startTime.compareTo(startTime);
	}
}