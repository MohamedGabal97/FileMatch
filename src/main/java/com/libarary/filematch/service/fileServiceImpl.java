package com.libarary.filematch.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class fileServiceImpl implements IFileService {

    @Value("${file.a.path}")
    private String fileAPath;

    @Value("${directory.path}")
    private String directoryPath;

    private List<String> extractAlphabeticWordsFromFile(String filePath) {

        List<String> alphabeticWords = new ArrayList<>();
        Pattern pattern = Pattern.compile("[a-zA-Z]+");

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))){
            String line;
            while ((line = br.readLine()) != null){
                String[] words = line.split("\\s+");
                for (String word : words) {
                    Matcher matcher = pattern.matcher(word);
                    if (matcher.matches()){
                        alphabeticWords.add(word.toLowerCase());
                    }
                }
            }
        }catch (IOException e) {
            log.error("Error: " + e.getMessage());
            e.printStackTrace();
        }
        log.info("end Extracted alphabetic words from file... ");
        log.info("number of alphabetic words: {}\n", alphabeticWords.size());
        return alphabeticWords;
    }


    private double calculateSimilarityScore(Map<String, Integer> frequencyA, Map<String, Integer> frequencyFile, int lengthOfWordsInFileA) {

        // Calculate common words based on the minimum frequency
        int commonWordCount = 0;
        for (Map.Entry<String, Integer> entry : frequencyA.entrySet()) {
            String word = entry.getKey();
            int countA = entry.getValue();
            int countB = frequencyFile.getOrDefault(word, 0);
            commonWordCount += Math.min(countA, countB);
        }

        log.info("Common words between file (A and another) is: {}\n", commonWordCount);

        return (double) commonWordCount / lengthOfWordsInFileA * 100;
    }


    private Map<String, Integer> calculateFrequencyFile(List<String> wordsInFile) {

        Map<String, Integer> frequency = new HashMap<>();
        for (String word : wordsInFile) {
            frequency.put(word, frequency.getOrDefault(word, 0) + 1);
        }
        return frequency;
    }

    @Override
    public Map<String, Double> compareFiles() {
        log.info("\n");
        log.info("**********************************************");
        log.info("Start compareFiles");
        log.info("Extracting alphabetic words from file A");
        List<String> wordsFromFileA = extractAlphabeticWordsFromFile(fileAPath);

        if (wordsFromFileA.isEmpty()){
            log.info("file A is empty");
            return Collections.emptyMap();
        }
        log.info("Calculating Frequencies for file A");
        Map<String, Integer> frequencyFileA = calculateFrequencyFile(wordsFromFileA);

        Map<String, Double> score = new HashMap<>();
        try {
            log.info("Reading All files in directory {}", directoryPath);
            Files.list(Paths.get(directoryPath)).forEach(filePath->{

                log.info("Reading file {}", filePath.getFileName());
                log.info("*** Extracting alphabetic words from file {}", filePath.getFileName());
                List<String> wordsFromFile = extractAlphabeticWordsFromFile(filePath.toString());

                log.info("*** Calculating Frequencies for file {}\n", filePath.getFileName());
                Map<String, Integer> frequencyFile = calculateFrequencyFile(wordsFromFile);

                log.info("*** Calculating similarity score between two file (A and {})!",filePath.getFileName());
                double scoreFile = calculateSimilarityScore(frequencyFileA, frequencyFile, wordsFromFileA.size());

                score.put(String.valueOf(filePath.getFileName()), scoreFile);
                log.info("finish with file {}", filePath.getFileName());
                log.info("#####################\n");
        });
        }catch (IOException e){
            log.error("Error: " + e.getMessage());
            e.printStackTrace();
        }
        log.info("end compareFiles");
        log.info("**********************************************");
        return score;
    }

}
