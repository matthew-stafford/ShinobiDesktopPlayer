import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class VideoFrameCanvas extends Canvas {

	public enum status {
		Loading,
		NoVideo,
		NoPlaybackVideo
	}
	
	public status _status = status.Loading;

	public void paint(Graphics g) {
		 Graphics2D g2;
         g2 = (Graphics2D) g;
         g2.setColor(Color.BLACK);
         g2.fillRect(0, 0, getWidth(), getHeight());         
         g2.setColor(Color.WHITE);
         
         Font myFont = new Font("Serif", Font.ITALIC | Font.BOLD, 24);
         
         if (_status == status.Loading) {
        	 drawCenteredString(g2, "LOADING...", new Rectangle(0,0, getWidth(),getHeight()), myFont);
         } else if (_status == status.NoVideo) {
        	 drawCenteredString(g2, "NO VIDEO",new Rectangle(0,0, getWidth(),getHeight()), myFont);        	 
         } else if (_status == status.NoPlaybackVideo) {
        	 drawCenteredString(g2, "NO PLAYBACK VIDEO FILE FOUND",new Rectangle(0,0, getWidth(),getHeight()), myFont);        	 
         }
         
     }
	
	/**
	 * Draw a String centered in the middle of a Rectangle.
	 *
	 * @param g The Graphics instance.
	 * @param text The String to draw.
	 * @param rect The Rectangle to center the text in.
	 */
	public void drawCenteredString(Graphics2D g, String text, Rectangle rect, Font font) {
	    // Get the FontMetrics
	    FontMetrics metrics = g.getFontMetrics(font);
	    // Determine the X coordinate for the text
	    int x = rect.x + (rect.width - metrics.stringWidth(text)) / 2;
	    // Determine the Y coordinate for the text (note we add the ascent, as in java 2d 0 is top of the screen)
	    int y = rect.y + ((rect.height - metrics.getHeight()) / 2) + metrics.getAscent();
	    // Set the font
	    g.setFont(font);
	    // Draw the String
	    g.drawString(text, x, y);
	}
}