package org.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        try {
            String inputPath = args[0];
            String outputPath = args[0].replace("input.txt", "output.txt");
            List<String> input = Files.readAllLines(Paths.get(inputPath));
            Files.deleteIfExists(Paths.get(outputPath));
            Files.createFile(Paths.get(outputPath));
            int firstArraySize = Integer.parseInt(input.get(0));
            List<String> output = new ArrayList<>();
            List<Sentence> firstArray = new ArrayList<>();
            List<Sentence> secondArray = new ArrayList<>();
            boolean[] usedInFirstArray = new boolean[firstArraySize];
            boolean[] usedInSecondArray = new boolean[input.size() - firstArraySize - 2];
            Map<ResultString, String> resultMap = new HashMap<>();
            for (int i = 1; i <= firstArraySize; i++) {
                addSentenceToArray(input, firstArray, i);
            }
            for (int i = firstArraySize + 2; i < input.size(); i++) {
                addSentenceToArray(input, secondArray, i);
            }
            addRanksToSentences(firstArray, secondArray);
            optimizeSentencesByRelevance(firstArray);
            fillResultMap(resultMap, firstArray, input, usedInFirstArray, usedInSecondArray);
            resultMap.forEach((resultString, s) -> {
                String line = resultString.TEXT + ":" + s;
                output.add(line);
            });
            Files.write(Paths.get(outputPath), output);
        } catch (IOException e) {
            System.out.println("Файл не найден");
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Не задан путь к файлу input.txt");
        }
    }

    //Переделывает строку из обычного String в Sentence, который содержит в себе Words,
    //который содержат в себе все леммы конкретного слова
    private static void addSentenceToArray(List<String> input, List<Sentence> array, int i) {
        Sentence sentence = Lemmatizator.getAllLemmas(input.get(i));
        sentence.setId(i);
        array.add(sentence);
    }

    //Метод ищет во втором массиве предложения, которые содержат одну или больше лемм с
    //предложением из первого массива. Если такие есть, то в добавляет их в sortedSentences
    //и сортирует по количеству общих лемм.
    private static void addRanksToSentences(List<Sentence> firstArray, List<Sentence> secondArray) {
        for (Sentence sentence1 : firstArray) {
            for (Sentence sentence2 : secondArray) {
                sentence1.getSentenceRank(sentence2);
            }
        }
    }
    //Если случается так, что два предложения из первого массива имеют наиболее похожим
    //Одно и то же предложение из второго массива, то высчитывается, какое предложение
    //наиболее похоже. Более похожее оставляет ссылку на это предложение, а менее похожее
    //теперь будет ссылаться на второй предложением из массива sortedSentences. Если второе
    //предложение уже тоже ссылается на какое-то предложение из первого массива - метод повторяется.
    //И так пока не найдет предложение без ссылки, либо пока не станет пустым.
    private static void optimizeSentencesByRelevance(List<Sentence> firstArray) {
        for (Sentence sentence : firstArray) {
            Sentence.sortByRelevance(sentence);
        }
    }

    //Заполняю конечную мапу для ее последующей записи в output.txt
    private static void fillResultMap(Map<ResultString, String> resultMap, List<Sentence> firstArray, List<String> input,
                                      boolean[] usedInFirstArray, boolean[] usedInSecondArray) {
        int firstArrayDifference = 1;
        int secondArrayDifference = firstArray.size() + 2;
        //Если есть пара хотя бы с одной леммой
        fillWithPairByRank(firstArray, resultMap, input, usedInFirstArray, usedInSecondArray);
        //Если остались свободные пары без лемм
        if (usedInFirstArray.length > usedInSecondArray.length) {
            fillWithPair(usedInSecondArray, secondArrayDifference,
                        usedInFirstArray, firstArrayDifference,
                        input, resultMap);
        } else {
            fillWithPair(usedInFirstArray, firstArrayDifference,
                    usedInSecondArray, secondArrayDifference,
                    input, resultMap);
        }
    }
    //Сначала забираю из массива все предложения, которые имеют пару с похожими леммами
    private static void fillWithPairByRank(List<Sentence> firstArray, Map<ResultString, String> resultMap, List<String> input,
                                           boolean[] usedInFirstArray, boolean[] usedInSecondArray) {
        for (Sentence sentence : firstArray) {
            if (!sentence.getSortedSentences().isEmpty()) {
                int firstSentenceIdInInput = sentence.getId();
                int secondSentenceIdInInput = sentence.getSortedSentences().first().getSentence().getId();
                int firstSentenceIdInFirstArray = firstSentenceIdInInput - 1;
                int secondSentenceIdInSecondArray = secondSentenceIdInInput - firstArray.size() - 2;
                ResultString resultString = new ResultString(firstSentenceIdInFirstArray, input.get(firstSentenceIdInInput));
                resultMap.put(resultString, input.get(secondSentenceIdInInput));
                usedInFirstArray[firstSentenceIdInFirstArray] = true;
                usedInSecondArray[secondSentenceIdInSecondArray] = true;
            }
        }
    }

    //Потом заполняю парами, у которых нет общих лемм в случайном порядке (эти предложения могут быть совсем не похожи)
    private static void fillWithPair(boolean[] usedInSmallArray, int smallArrayDifference,
                                     boolean[] usedInLargeArray, int largeArrayDifference,
                                     List<String> input, Map<ResultString, String> resultMap) {
        for (int i = 0; i < usedInSmallArray.length; i++) {
            if (!usedInSmallArray[i]) {
                int j = 0;
                while (usedInLargeArray[j]) {
                    j++;
                }
                ResultString resultString = new ResultString(i, input.get(i + smallArrayDifference));
                resultMap.put(resultString, input.get(j + largeArrayDifference));
                usedInSmallArray[i] = true;
                usedInLargeArray[j] = true;
            }
        }
        //Пар не осталось - заполняю вопросительными знаками
        fillWithQuestionMarks(usedInLargeArray, input, resultMap, largeArrayDifference);
    }

    //Если остались предложения совсем без пары, то проставляю вопросы
    private static void fillWithQuestionMarks(boolean[] usedInArray, List<String> input, Map<ResultString, String> resultMap, int arrayDifference) {
        for (int i = 0; i < usedInArray.length; i++) {
            if (!usedInArray[i]) {
                resultMap.put(new ResultString(i, input.get(i + arrayDifference)), "?");
            }
        }
    }
}
