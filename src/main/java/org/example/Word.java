package org.example;

import lombok.Data;


import java.util.HashSet;
import java.util.Set;

@Data
//Задача этого класс - повысить читабельность кода
//Представляет собой слово из предложения. Содержит в себе все леммы этого слова.
public class Word {
    private Set<String> lemmas = new HashSet<>();

    public int getEqualLemmasInThisWord(Word word) {
        int equalLemmasCount = 0;
        for (String lemma1: lemmas) {
            for (String lemma2: word.getLemmas()) {
                if (lemma1.equals(lemma2)) {
                    equalLemmasCount++;
                }
            }
        }
        return equalLemmasCount;
    }
}
