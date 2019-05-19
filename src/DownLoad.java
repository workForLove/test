import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class DownLoad {
    static String url = null, bookUrl = null, dir = null, bookName = null, deleteWord1 = "", deleteWord2 = "";
    static int outTime = 0, stepChap=0;
    static FileWriter fileWriter;

    public static void main(String[] args) throws IOException {
        //导入配置文件信息
        Properties prop = new Properties();
        String path = Thread.currentThread().getContextClassLoader().getResource("para.properties").getPath();
        FileInputStream fileInputStream = new FileInputStream(path);
        prop.load(fileInputStream);
        try {
            url = prop.getProperty("url");
            bookUrl = prop.getProperty("bookUrl");
            dir = prop.getProperty("dir");
            outTime = Integer.parseInt(prop.getProperty("outTime"));
            stepChap=Integer.parseInt(prop.getProperty("stepChap"));
            deleteWord1 = prop.getProperty("deleteWord1");
            deleteWord2 = prop.getProperty("deleteWord2");
        } catch (Exception e) {
            System.out.println("配置文件有误");
        }


        //获取目录信息及章节信息
        Document parse = Jsoup.parse(new URL(url + bookUrl), outTime);
        Elements dd = parse.getElementsByTag("dd");
        //获取书名
        if (bookName == null || bookName.equals("")) {
            bookName = parse.getElementsByTag("h1").get(0).text();
        }
        List<Map> textArrayList = new ArrayList();
        for (Element element : dd) {
            HashMap<String, String> map = new HashMap<>();
            Element a = element.getElementsByTag("a").get(0);
            String attr = a.attr("href");
            String chapterName = a.text();
            String chapterUrl = url + bookUrl + a.attr("href");
            map.put("chapterUrl", chapterUrl);
            map.put("chapterName", chapterName);
            textArrayList.add(map);
        }

        //创建存储文件夹
        File file1 = new File(dir);
        if (!file1.exists()) file1.mkdirs();
        File file = new File(dir + "//" + bookName+".txt");
        file.createNewFile();
        fileWriter = new FileWriter(file, true);
        //循环下载
        for (int i = 0+stepChap; i < textArrayList.size(); i++) {
            try {
                Down(textArrayList.get(i));
            }catch (Exception e){
                System.out.println(e);
                i--;
            }
        }
        fileWriter.close();

    }

    public static void Down(Map map) throws IOException {
        String chapterUrl = (String) map.get("chapterUrl");
        String chapterName = (String) map.get("chapterName");
        Document parse = Jsoup.parse(new URL(chapterUrl), outTime);
        Element content = parse.getElementById("content");
        String chapterContext = content.html();
        chapterContext = chapterContext.replaceAll("&nbsp;", " ").replaceAll("<br>", "").replaceAll(deleteWord1, "").replaceAll(deleteWord2, "");

        String chapter=chapterName+"\r\n\r\n"+chapterContext+"\r\n\r\n\r\n\r\n\r\n";
        fileWriter.write(chapter);
        System.out.println("已下载："+chapterName);
        fileWriter.flush();
    }
}
