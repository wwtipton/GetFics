import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;
 
public class TestImageDownloader
{      
    public static void main(String[] args )
    {
        BufferedImage image =null;
        try{
 
          //  URL url =new URL("https://homepages.cae.wisc.edu/~ece533/images/sails.png");
        //   URL url =new URL("https://ic.pics.livejournal.com/teddyradiator/28273561/70960/70960_original.png");
            // read the url
           URL url =new URL("https://i.imgur.com/BDENSz9.jpg");
       	HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    	conn.setReadTimeout(5000);

    	System.out.println("Request URL ... " + url);
    	System.out.println("Connection ... " + conn);

    	boolean redirect = false;
    	
    	// normally, 3xx is redirect
    	int status = conn.getResponseCode();
    	if (status != HttpURLConnection.HTTP_OK) {
    		if (status == HttpURLConnection.HTTP_MOVED_TEMP
    			|| status == HttpURLConnection.HTTP_MOVED_PERM
    				|| status == HttpURLConnection.HTTP_SEE_OTHER)
    		redirect = true;
    	}

    	System.out.println("Response Code ... " + status);

    	if (redirect) {

    		// get redirect url from "location" header field
    		String newUrl = conn.getHeaderField("Location");

    		// get the cookie if need, for login
    		String cookies = conn.getHeaderField("Set-Cookie");

    		// open the new connnection again
    		conn = (HttpURLConnection) new URL(newUrl).openConnection();
    		conn.setRequestProperty("Cookie", cookies);

    		System.out.println("Redirect to URL : " + newUrl);
    		System.out.println("Current value of url : " + url);

    	}
            image = ImageIO.read(url);
 
            //for png
            Date dt = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");
            String fileName = "Test_" + sdf.format(dt) + ".png";
            ImageIO.write(image, "png",new File(fileName));
 
            // for jpg
           // ImageIO.write(image, "jpg",new File("/tmp/have_a_question.jpg"));
 
        }catch(IOException e){
            e.printStackTrace();
        }
    }}