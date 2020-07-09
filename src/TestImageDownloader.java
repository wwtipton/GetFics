import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
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
           URL url =new URL("https://i.ibb.co/3k5G8nB/sadfasf.jpg");
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