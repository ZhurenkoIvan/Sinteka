package org.example;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
//Задача этого класса состоит в том, чтобы хранить и сортировать в TreeSet Sentence
//по наибольшему количеству похожих лемм
public class SentenceOrder implements Comparable<SentenceOrder>{
    private int lemmasCount;
    private Sentence sentence;

    @Override
    public int compareTo(SentenceOrder o) {
        if (o.getLemmasCount() != lemmasCount) {
            return (Integer.compare(o.getLemmasCount(), lemmasCount));
        } else {
            if (sentence.equals(o.getSentence())) {
                return 0;
            } else return -1;
        }
    }
}
