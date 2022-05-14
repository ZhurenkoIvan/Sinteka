package org.example;

import com.github.demidko.aot.WordformMeaning;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

//Создает из строки(предложения) объект Sentence, который хранит все леммы слов в предложении
public class Lemmatizator {

    public static Sentence getAllLemmas(String line) {
        Sentence sentence = new Sentence();
        String[] array = line.split(" ");
        int lemmasCount = 0;
        for (String text : array) {
            try {
                List<WordformMeaning> meanings = WordformMeaning.lookupForMeanings(text);
                Word word = new Word();
                AtomicBoolean hasPretext = new AtomicBoolean(false);
                meanings.forEach(wordFormMeaning-> {
                    if (wordFormMeaning.getPartOfSpeech().toString().equals("Pretext")) {
                        hasPretext.set(true);
                    }
                    word.getLemmas().add(wordFormMeaning.getLemma().toString());
                });
                if (hasPretext.get()) {
                    continue;
                }
                sentence.getWords().add(word);
                lemmasCount += word.getLemmas().size();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        sentence.setLemmasCount(lemmasCount);
        return sentence;
    }
}
