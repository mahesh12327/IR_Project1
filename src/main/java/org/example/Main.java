package org.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.*;

public class Main {
//    private static final String WEBSITE = "https://redirect.cs.umbc.edu/courses/graduate/676/term%20project/files/";

    public static void main(String[] args) {
        long startTime = System.nanoTime();
        // Input - Path to store Directory and files
        System.out.println("The input and output directories - " + args[0] + " ---- " + args[1]);
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
//                System.out.println("Directory: " + filename.getName());
                // and call the displayFiles function recursively to list files present in subdirectory
                getFilePaths(filename.listFiles());
            }
            // Printing the file name present in given path
            else {
                // Getting the file name
//                System.out.println("File: " + filename.getAbsolutePath());
                String[] filePathsSplit = filename.getAbsolutePath().split("/");
                String numberString = (filePathsSplit[filePathsSplit.length - 1]).replaceAll("[^0-9]", "");
                filePathsArr[Integer.parseInt(numberString) - 1] = filename.getAbsolutePath();
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

            for(int i=0; i<filePaths.length; i++) {
                enterIntoPages(filePaths[i], (i+1), outputPath, frequencySortByTokenTreeMap);
            }

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

    private static void enterIntoPages(String link, int count, String outputPath, Map<String, Integer> frequencySortByTokenTreeMap) {
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
                    if(isContentPresent) {
                        fileWriter.write((content[i]));
                        fileWriter.write("\r\n");
                        if(frequencySortByTokenTreeMap.containsKey(content[i]))
                            frequencySortByTokenTreeMap.put(content[i], (Integer) frequencySortByTokenTreeMap.get(content[i]) + 1);
                        else
                            frequencySortByTokenTreeMap.put(content[i],1);
                    }
                }
                fileWriter.close();
            }
        }catch (IOException exception) {
            System.out.println("Exception occurred ! " + exception);
        }
    }
}

//            /Users/mahesh/IdeaProjects/IR_Project1
//            /Users/mahesh/Downloads/files