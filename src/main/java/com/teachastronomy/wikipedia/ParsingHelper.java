package com.teachastronomy.wikipedia;

import com.teachastronomy.Logger;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileInputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;


/**
 * Created by sridh on 10/5/2016.
 */
public class ParsingHelper {



    public static void parseDump(File f) {


        try {
                 DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                Calendar cal = Calendar.getInstance();
          //  System.out.println(dateFormat.format(cal.getTime()));
                System.out.println("Parsing process about to start");
                Logger.writeToLog("Parsing process about to start. Time "+ cal.getTime());
                SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
                saxParserFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING,false);
                SAXParser parser = saxParserFactory.newSAXParser();


                BZip2CompressorInputStream stream = new BZip2CompressorInputStream(new FileInputStream(f));

                WikiPageHandler handler = new WikiPageHandler();
                parser.parse(stream,handler);
            Logger.writeToLog("Parsing process completed at "+ cal.getTime());
            Logger.writeToLog("*************************************************");
         }
        catch (Exception e){
            System.err.println(e.getMessage());
            e.printStackTrace();
         }

    }
}
