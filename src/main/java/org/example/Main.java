package org.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.*;

public class Main {
    private static String[] stopWordsArray = new String[600];
    //    private static final String WEBSITE = "https://redirect.cs.umbc.edu/courses/graduate/676/term%20project/files/";
    public static void main(String[] args) {
        long startTime = System.nanoTime();
        // Input - Path to store Directory and files
        System.out.println("The input and output directories - " + args[0] + " ---- " + args[1]);
        try {
            FileReader fr = new FileReader("/Users/mahesh/IdeaProjects/IR_Project1/StopWords");
            Scanner sc = new Scanner(fr);
            int i=0;
            while (sc.hasNextLine()) {
                String stopWord = (sc.nextLine()).replaceAll("[^a-zA-Z]", "");
                stopWordsArray[i] = stopWord;
                i++;
            }
        }catch (IOException error) {
            System.out.println("IOException occured ---> " + error);
        }

        if(!(args[0].isEmpty()) && !(args[1].isEmpty())) {
            // Method to scrape the html files
            scanItems(args[0], args[1]);
        }
        long endTime = System.nanoTime();
        long duration = (endTime - startTime);
        System.out.println("duration ---> " + duration + "ns");

    }

    public static void writeTokenSort(Map<String, Integer> frequencySortByTokenTreeMap, FileWriter tokenFileWriter) {
        try{
            for (Map.Entry entry : frequencySortByTokenTreeMap.entrySet())
            {
                tokenFileWriter.write("Token: " + entry.getKey() + "; Frequency: " + entry.getValue());
                tokenFileWriter.write("\r\n");
            }
        }catch (IOException exception) {
            System.out.println("Exception occurred ! " + exception);
        }
    }

    public static LinkedHashMap<String, Integer> sortByFrequency(Map<String, Integer> frequencySortByTokenTreeMap) {
        Map<String, Integer> unSortedMap = frequencySortByTokenTreeMap;
        LinkedHashMap<String, Integer> reverseSortedMap = new LinkedHashMap<>();
        unSortedMap.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEachOrdered(x -> reverseSortedMap.put(x.getKey(), x.getValue()));
        return reverseSortedMap;
    }

    public static void writeFrequencySort(Map<String, Integer> reverseSortedMap, FileWriter frequencyFileWriter) {
        try{
            for (Map.Entry entry : reverseSortedMap.entrySet())
            {
                frequencyFileWriter.write("Token: " + entry.getKey() + "; Frequency: " + entry.getValue());
                frequencyFileWriter.write("\r\n");
            }
        }catch (IOException exception) {
            System.out.println("Exception occurred ! " + exception);
        }

    }

    public static String[] getFilePaths(File[] files) {
        String s = (String.valueOf(files.length));
        String[] filePathsArr = new String[Integer.parseInt(s)];
        for (File filename : files) {
            // If a subdirectory is found, print the name of the subdirectory
            if (filename.isDirectory()) {
                System.out.println("Directory: " + filename.getName());
                // and call the displayFiles function recursively to list files present in subdirectory
                getFilePaths(filename.listFiles());
            }
            // Printing the file name present in given path
            else {
                // Getting the file name
//                System.out.println("File: " + filename.getAbsolutePath());
                String[] filePathsSplit = filename.getAbsolutePath().split("/");
//                System.out.println(filePathsSplit[filePathsSplit.length - 1] + " --- " + filename.getAbsolutePath());
                if(filePathsSplit[filePathsSplit.length - 1] != ".DS_Store") {
                    String numberString = (filePathsSplit[filePathsSplit.length - 1]).replaceAll("[^0-9]", "");
                    filePathsArr[Integer.parseInt(numberString) - 1] = filename.getAbsolutePath();
                }
            }
        }
        return filePathsArr;
    }

    private static void scanItems(String inputPath, String outputPath) {
        try {
            File[] files = new File(inputPath).listFiles();
            // Method to get all the file paths
            String[] filePaths = getFilePaths(files);
            FileWriter tokenFileWriter = new FileWriter( outputPath + "/FrequenciesSortedByToken.txt");
            FileWriter frequencyFileWriter = new FileWriter( outputPath + "/FrequenciesSortedByFrequency.txt");
            Map<String, Integer> frequencySortByTokenTreeMap = new TreeMap<String, Integer>();
            long startTime = System.nanoTime();
//            filePaths.length
            String[][] crawlArray = new String[filePaths.length + 1][50000];
            for(int i=0; i<filePaths.length; i++) {
                String[][] ca = enterIntoPages(filePaths[i], (i), outputPath, frequencySortByTokenTreeMap, crawlArray);
                crawlArray = ca;
                System.out.println("$$$$$$$$$$$$$$$$" + ca[i][0]);
            }
            System.out.println(crawlArray[502][0]);

//            System.out.println(crawlArray);
            final File parentDir = new File(outputPath + "/crawl");
            parentDir.mkdir();
            for (int i = 0; i < filePaths.length; i++) {
                FileWriter fileWriter = new FileWriter(new File(parentDir, i+1 + ".txt"));
                for (int j = 0; j < 50000; j++) {
                    if(crawlArray[i][j] != null) {
//                        System.out.println(crawlArray[i][j] + "---" + i + "-----" + j);
                        boolean preprocessingCheck = doPreprocessing(crawlArray[i][j]);
                        if(!preprocessingCheck) {
                            boolean countOne = false;
                            for (Map.Entry entry : frequencySortByTokenTreeMap.entrySet())
                            {
//                            tokenFileWriter.write("Token: " + entry.getKey() + "; Frequency: " + entry.getValue());
                                if(crawlArray[i][j].equals(entry.getKey()) && ((int) (entry.getValue()) == 1)) {
//                                System.out.println("entered count one");
                                    countOne = true;
                                }
                            }
                            if(!countOne) {
                                fileWriter.write((crawlArray[i][j]));
                                fileWriter.write("\r\n");
                            }
                        }
                    }
                }
                System.out.println("");
            }
//            for(int i=0; i<filePaths.length; i++) {
//                enterIntoPages1(filePaths[i], (i+1), outputPath, frequencySortByTokenTreeMap);
//            }

            long endTime = System.nanoTime();
            long duration = (endTime - startTime);
            System.out.println("duration 1 ---> " + duration + "ns");
            // Text write based on tokens sort
            writeTokenSort(frequencySortByTokenTreeMap, tokenFileWriter);
            // Method to sort based on frequencies
            LinkedHashMap<String, Integer> reverseSortedMap = sortByFrequency(frequencySortByTokenTreeMap);
            // Text write based on frequencies sort
            writeFrequencySort(reverseSortedMap, frequencyFileWriter);
            tokenFileWriter.close();
            frequencyFileWriter.close();
        }catch (IOException exception) {
            System.out.println("Exception occurred ! " + exception);
        }
    }
///Users/mahesh/IdeaProjects/IR_Project1/StopWords
    private static boolean doPreprocessing(String word) {
        if(word.length() == 1)
            return true;
        for(int i=0; i<stopWordsArray.length; i++) {
            if(word.equals(stopWordsArray[i])){
                return true;
            }
        }

        return false;
    }

    private static String[][] enterIntoPages(String link, int count, String outputPath, Map<String, Integer> frequencySortByTokenTreeMap, String[][] crawlArray) {
        try {
            File input = new File(link);
            Document doc = Jsoup.parse(input, "UTF-8", "");
            Elements elements = doc.select("body");
            final File parentDir = new File(outputPath + "/crawl");
            parentDir.mkdir();
            System.out.println("page" + count);
            int wordsCount = 0;
            for(Element element : elements) {
                String bodyText = element.text();
//                FileWriter fileWriter = new FileWriter(new File(parentDir, count + ".txt"));
                bodyText = bodyText.replaceAll("[\\\t|\\\n|\\\r]", " ");
                String[] content = bodyText.split(" ");
                System.out.println("---------content.length------------" +  content.length);
                for(int i=0; i<content.length; i++) {
                    content[i] = content[i].replaceAll("[^a-zA-Z]", "");
                    content[i] = (content[i]).toLowerCase();
                    boolean isContentPresent = ((content[i] != null) && (content[i] != "") && (content[i] != " ") && (!content[i].isEmpty()) && (!content[i].isBlank()));
                    boolean preprocessingCheck = doPreprocessing(content[i]);
                    if(!preprocessingCheck && isContentPresent) {
//                        fileWriter.write((content[i]));
//                        fileWriter.write("\r\n");
                        System.out.println(count + " - " + wordsCount + " - " + content[i]);
                        if(content[i] != null){
                            crawlArray[count][wordsCount] = content[i];
                            wordsCount++;
                        }
                        if(frequencySortByTokenTreeMap.containsKey(content[i]))
                            frequencySortByTokenTreeMap.put(content[i], (Integer) frequencySortByTokenTreeMap.get(content[i]) + 1);
                        else
                            frequencySortByTokenTreeMap.put(content[i],1);
                    }
                }
//                fileWriter.close();
            }
        }catch (IOException exception) {
            System.out.println("Exception occurred ! " + exception);
        }
        return crawlArray;
    }

    private static void enterIntoPages1(String link, int count, String outputPath, Map<String, Integer> frequencySortByTokenTreeMap) {
        try {
            File input = new File(link);
            Document doc = Jsoup.parse(input, "UTF-8", "");
            Elements elements = doc.select("body");
            final File parentDir = new File(outputPath + "/crawl");
            parentDir.mkdir();
            System.out.println("page " + count);
            for(Element element : elements) {
                String bodyText = element.text();
                FileWriter fileWriter = new FileWriter(new File(parentDir, count + ".txt"));
                bodyText = bodyText.replaceAll("[\\\t|\\\n|\\\r]", " ");
                String[] content = bodyText.split(" ");
                for(int i=0; i<content.length; i++) {
                    content[i] = content[i].replaceAll("[^a-zA-Z]", "");
                    content[i] = (content[i]).toLowerCase();
                    boolean isContentPresent = ((content[i] != null) && (content[i] != "") && (content[i] != " ") && (!content[i].isEmpty()) && (!content[i].isBlank()));
                    boolean preprocessingCheck = doPreprocessing(content[i]);
                    if(!preprocessingCheck && isContentPresent) {
                        boolean countOne = false;
                        for (Map.Entry entry : frequencySortByTokenTreeMap.entrySet())
                        {
//                            tokenFileWriter.write("Token: " + entry.getKey() + "; Frequency: " + entry.getValue());
                            if(content[i].equals(entry.getKey()) && ((int) (entry.getValue()) == 1)) {
//                                System.out.println("entered count one");
                                countOne = true;
                            }
                        }
                        if(!countOne) {
                            fileWriter.write((content[i]));
                            fileWriter.write("\r\n");
                        }
                    }
                }
                fileWriter.close();
            }
        }catch (IOException exception) {
            System.out.println("Exception occurred ! " + exception);
        }
    }
}