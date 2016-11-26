package com.teachastronomy;

import com.teachastronomy.wikipedia.ParsingHelper;
import org.sweble.wikitext.engine.EngineException;
import org.sweble.wikitext.engine.PageId;
import org.sweble.wikitext.engine.PageTitle;
import org.sweble.wikitext.engine.WtEngineImpl;
import org.sweble.wikitext.engine.config.WikiConfig;
import org.sweble.wikitext.engine.nodes.EngProcessedPage;
import org.sweble.wikitext.engine.utils.DefaultConfigEnWp;
import org.sweble.wikitext.parser.parser.LinkTargetException;

import java.io.File;



public class Main {


    public static void main(String[] args) {

        ParsingHelper.parseDump(new File(Constants.DumpFileLocation));
//        try {
//            ArrayList<String> articles = new ArrayList<>();
//            XSSFWorkbook book = new XSSFWorkbook(new File("/home/sridhar/Desktop/Test.xlsx"));
//            XSSFSheet sheet = book.getSheetAt(0);
//            int i=1;
//            while(articles.size()<=1000)
//            {
//                XSSFRow row = sheet.getRow(i);
//                String title = row.getCell(0).getStringCellValue();
//                i++;
//                if(row.getCell(1)!=null && row.getCell(1).getNumericCellValue()==1) {
//                    continue;
//                }
//
//                articles.add(title);
//            }
//
//            i=0;
//            CloseableHttpClient client = HttpClients.createDefault();
//            for(String article:articles){
//                FileOutputStream fos;
//                CloseableHttpResponse response;
//                try {
//                    if(i%50==0)
//                        Thread.sleep(1000);
//                    if(i<772) {
//                        i++;
//                        continue;
//                    }
//                article = article.replaceAll(" ","_");
//                HttpGet getter = new HttpGet("https://en.wikipedia.org/wiki/"+article+"?action=raw");
//                response = client.execute(getter);
//                HttpEntity entity1 = response.getEntity();
//                String text = convertStreamToString(entity1.getContent());
//
//
//                    fos = new FileOutputStream("/home/sridhar/Desktop/TrainingData/New/" + article + ".txt");
//                    fos.write(text.getBytes());
//                    fos.close();
//                    response.close();
//                    i++;
//                }
//                catch (Exception e){
//                    e.printStackTrace();
//                    continue;
//                }
//
//
//            }
//            client.close();
//        }
//        catch (Exception e){
//            e.printStackTrace();
//        }



    }

    static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
    public static String convertWikiText(String title, String wikiText, int maxLineLength) throws LinkTargetException,EngineException {
        // Set-up a simple wiki configuration
        WikiConfig config = DefaultConfigEnWp.generate();
        // Instantiate a compiler for wiki pages
        WtEngineImpl engine = new WtEngineImpl(config);
        // Retrieve a page
        PageTitle pageTitle = PageTitle.make(config, title);
        PageId pageId = new PageId(pageTitle, -1);
        // Compile the retrieved page
        EngProcessedPage cp = engine.postprocess(pageId, wikiText, null);
        TextConverter p = new TextConverter(config, maxLineLength);
        return (String)p.go(cp.getPage());
    }





}

