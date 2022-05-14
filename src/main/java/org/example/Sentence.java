package org.example;

import lombok.Data;

import java.util.*;

@Data
//Задача этого класса - повысить читабельность кода
//Sentence = одна строка из файла input. Хранит в себе список Word
//SortedSentences - отсортированное по количеству общих лемм у двух предложений дерево
public class Sentence {
    private int id;
    private int lemmasCount;
    private List<Word> words = new ArrayList<>();
    private TreeSet<SentenceOrder> sortedSentences = new TreeSet<>();


    public void getSentenceRank(Sentence sentence) {
        int equalLemmasInSentencesCount = 0;
        for (Word word1: words) {
            for (Word word2: sentence.getWords()) {
                equalLemmasInSentencesCount += word1.getEqualLemmasInThisWord(word2);
            }
        }
        if (equalLemmasInSentencesCount > 0) {
            SentenceOrder sentenceOrder = new SentenceOrder(equalLemmasInSentencesCount, sentence);
            sortedSentences.add(sentenceOrder);
        }
    }

    public static void sortByRelevance(Sentence sentence) {
        if (sentence.sortedSentences.isEmpty()) {
            return;
        }
        SentenceOrder firstRank = sentence.getSortedSentences().first();
        Sentence firstSentence = firstRank.getSentence();
        if (firstSentence.getSortedSentences().isEmpty()) {
            firstSentence.getSortedSentences().add(new SentenceOrder(sentence.getLemmasCount() - firstRank.getLemmasCount(), sentence));
        } else {
            if (firstRank.getSentence().getSortedSentences().first().getLemmasCount() <= sentence.getLemmasCount() - firstRank.getLemmasCount()) {
                sentence.getSortedSentences().remove(firstRank);
                sortByRelevance(sentence);
            } else {
                Sentence currentSentence = firstRank.getSentence().getSortedSentences().first().getSentence();
                SentenceOrder sentenceOrder = currentSentence.getSortedSentences().first();
                currentSentence.getSortedSentences().remove(sentenceOrder);
                sortByRelevance(currentSentence);
            }
        }
    }
}
