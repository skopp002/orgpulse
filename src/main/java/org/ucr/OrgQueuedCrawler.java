package org.ucr;

import com.google.common.base.Stopwatch;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.io.FileUtils;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class OrgQueuedCrawler {
        private ConcurrentHashMap<String,Integer> links;
        private ConcurrentHashMap<String, String> articles;
        private String seedfile;
        private int pages;
        private String docStoreLocation;
        //private List<List<String>> articles;

        public OrgQueuedCrawler(String seedfile, int numPages, String outputdir) {
            this.seedfile=seedfile;
            pages=numPages;
            docStoreLocation=outputdir;
            links = new ConcurrentHashMap<String, Integer>();
            articles = new ConcurrentHashMap<String, String>();
        }

        public void put(String url, int depth) throws IOException{
             links.putIfAbsent(url,depth);
        }

        public void put() throws IOException{
            File file = new File(seedfile);
            List lines = FileUtils.readLines(file, "UTF-8");
            for (Object line :lines){
               put(line.toString(),0);
            }
            try {
                Set<Map.Entry<String,Integer>> entrySet=links.entrySet();
                for(Map.Entry<String,Integer> entry:entrySet) {
                    Document document = Jsoup.connect(entry.getKey()).get();
                   // Elements linksOnPage = document.select("a[href^=*.org]");
                    Elements linksOnPage = document.select("a[href]");
                    Integer depth = entry.getValue() + 1;
                    for (Element page : linksOnPage) {
                        System.out.println("Fetching..."+ page.toString());
                        links.putIfAbsent(page.attr("abs:href"), depth);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        public void process()throws IOException{
            Set<Map.Entry<String,Integer>> entrySet=links.entrySet();
            for(Map.Entry<String,Integer> entry:entrySet){
            try {
                Document document = Jsoup.connect(entry.getKey()).get();
                Elements articleLinks = document.select("h2 a[href]");
                for (Element article : articleLinks) {
                    //Only retrieve the titles that have non profit
                    //if (article.text().matches("^.*?(org).*$")) {
                        System.out.println("Processing....." + article.attr("abs:href"));
                        articles.putIfAbsent(article.attr("abs:href"),article.text());
                    //}
                }
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
            }
        }


       public void persist(String filename) {
           FileWriter writer;
           Set<Map.Entry<String, String>> entrySet = articles.entrySet();
           for (Map.Entry<String, String> entry : entrySet) {
               try {
                   writer = new FileWriter(filename);
                   try {
                       String temp = "- Title: " + entry.getKey();
                       System.out.println("Persisting" + temp);
                       //save to file
                       writer.write(temp);
                       writer.write(entry.getValue());
                   } catch (IOException e) {
                       System.err.println(e.getMessage());
                   }
                   writer.close();
               } catch (IOException e) {
                   System.err.println(e.getMessage());
               }
           }
       }


    public static void main(String[] args) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        ExecutorService executor = Executors.newFixedThreadPool(3);
        if (args.length != 3) {
            throw new IllegalArgumentException("Usage org.ucr.OrgQueuedCrawler <seedfile.txt> <numPages> <output dir>");
        }
        OrgQueuedCrawler s = new OrgQueuedCrawler(args[0], Integer.parseInt(args[1]), args[2]);
        Runnable producerTask = () -> {
            try {
                s.put();
            } catch (IOException e) {
                    e.printStackTrace();
                }
        };

        Runnable consumerTask = () -> {
            try {
                Thread.sleep(1000);
                s.process();
            } catch (IOException e){
                    e.printStackTrace();
            }
            catch (InterruptedException e){

            }
        };
        Runnable writerTask = () -> {
            try {
                Thread.sleep(5000);
                s.persist(args[2]);
            }
            catch (InterruptedException e){

            }
        };
        executor.execute(producerTask);
        executor.execute(consumerTask);
        executor.execute(writerTask);
        executor.shutdown();
        stopwatch.stop();
        long millis = stopwatch.elapsed(MILLISECONDS);
        System.out.println("Time taken for crawling " + stopwatch);
    }

}