import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileCacheImageInputStream;

import org.brotli.dec.BrotliInputStream;


 
public class TestImageDownloader2
{      
	
	static CookieManager cookieManager = new CookieManager();
	static HttpClient client = null;
	static{
		CookieHandler.setDefault(cookieManager);
		cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
		
	   client = HttpClient.newBuilder()
		        .followRedirects(Redirect.NORMAL)
		        .connectTimeout(Duration.ofSeconds(120))
		        .cookieHandler(CookieHandler.getDefault())
		        .build();
	}

    public static void main(String[] args )
    {
     
    	//HttpRequest.Builder builder = getRequestBuilder("https://homepages.cae.wisc.edu/~ece533/images/sails.png");
   // 412 error 	HttpRequest.Builder builder = getRequestBuilder("https://ic.pics.livejournal.com/teddyradiator/28273561/70960/70960_original.png");
          // read the url
   //      HttpRequest.Builder builder = getRequestBuilder("https://i.ibb.co/3k5G8nB/sadfasf.jpg");

	    HttpRequest.Builder builder = getRequestBuilder("https://i.imgur.com/BDENSz9.jpg");

	    HttpRequest request = builder.build();
	    
	    File dir = new File("E:\\CodingProjects\\GetFics");
	    
		try {
			HttpResponse<InputStream> response = client.send(request, BodyHandlers.ofInputStream());
			System.out.println("Status code:\t" + response.statusCode());
			InputStream is = decompress(response);
			FileCacheImageInputStream iis = new FileCacheImageInputStream(is, dir);
			BufferedImage pic = ImageIO.read(iis);
			if (null == pic) {
				throw new Exception("Picture diddn't download!!!"); //$NON-NLS-1$
			} else {
		            Date dt = new Date();
		            SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");
		            String fileName = "Test_" + sdf.format(dt) + ".png";
		            ImageIO.write(pic, "png",new File(fileName));
		 

			}
		} catch (Exception e) {
	            e.printStackTrace();
	        }

    }
    
	/**
	 * @param url
	 * @return
	 */
	static HttpRequest.Builder getRequestBuilder(String url) {
		HttpRequest.Builder builder = HttpRequest.newBuilder()
			   	.uri(URI.create(url))
			   	.timeout(Duration.ofSeconds(120))
			   	.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:84.0) Gecko/20100101 Firefox/84.0") //$NON-NLS-1$
			   	.setHeader("upgrade-insecure-requests", "1") //$NON-NLS-1$ //$NON-NLS-2$
			   	.setHeader("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8") //Firefox //$NON-NLS-1$ //$NON-NLS-2$
			   	.setHeader("accept-language", "en-US,en;q=0.5") //$NON-NLS-1$ //$NON-NLS-2$
			   	.setHeader("accept-encoding", "gzip, deflate, br")   //$NON-NLS-1$ //$NON-NLS-2$
			   	.GET();
		return builder;
	}
	
	static InputStream decompress(HttpResponse<InputStream> response) throws IOException {
		String encoding = response.headers().firstValue("Content-Encoding").orElse(""); //$NON-NLS-1$ //$NON-NLS-2$
	    InputStream is = null;
	    System.out.println("Encoding:\t" + encoding);
	    if (encoding.equals("gzip")) { //$NON-NLS-1$
	    	System.out.println("gzip compressed"); //$NON-NLS-1$
	      is = new GZIPInputStream(response.body());
	      }
	    else if (encoding.equals("br")) { //$NON-NLS-1$
	    	System.out.println("br compressed"); //$NON-NLS-1$
		      is = new BrotliInputStream(response.body());	    	
	    }
	    else if (encoding.equals("deflate")) {
	    	System.out.println("deflate compressed"); //$NON-NLS-1$
		      is = new InflaterInputStream(response.body());	    	
	    }
	    else {
	    	System.out.println("not compressed"); //$NON-NLS-1$
	      is = response.body();
	    }
		return is;
	}
    }