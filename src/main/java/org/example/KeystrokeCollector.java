package org.example;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;

public class KeystrokeCollector extends JFrame {

    private static final String[] BIGRAMS = {"ов", "ст", "но", "ко", "ен", "ни", "во", "то", "ро", "ра", "на"};
    private static final int SAMPLES_PER_BIGRAM = 3;
    private static final long MAX_KEY_PRESS_DURATION = 3000;
    private final Map<String, List<KeystrokeData>> keystrokeDataMap = new HashMap<>();
    private StringBuilder currentText = new StringBuilder();
    private long firstKeyPressTime = 0;
    private long firstKeyReleaseTime = 0;

    public KeystrokeCollector() {
        for (String bigram : BIGRAMS) {
            keystrokeDataMap.put(bigram, new ArrayList<>());
        }
        setupUI();
    }

    private void setupUI() {
        JTextArea textArea = new JTextArea();
        textArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPressed(e);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                handleKeyReleased(e);
            }
        });

        this.add(new JScrollPane(textArea));
        this.setSize(500, 500);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
    }

    private void handleKeyPressed(KeyEvent e) {
        char keyChar = e.getKeyChar();
        if (Character.isLetterOrDigit(keyChar)) {
            long currentTime = System.currentTimeMillis();
            if (firstKeyPressTime == 0) {
                firstKeyPressTime = currentTime;
                currentText.append(keyChar);
            } else {
                long secondKeyPressTime = currentTime;
                currentText.append(keyChar);
                if (currentText.length() >= 2) {
                    String bigram = currentText.substring(currentText.length() - 2);
                    if (keystrokeDataMap.containsKey(bigram) && keystrokeDataMap.get(bigram).size() < SAMPLES_PER_BIGRAM) {
                        long firstPressDuration = firstKeyReleaseTime - firstKeyPressTime;
                        long secondPressDuration = secondKeyPressTime - firstKeyReleaseTime;
                        long releaseToPressDuration = secondKeyPressTime - firstKeyReleaseTime;
                        if (firstPressDuration < MAX_KEY_PRESS_DURATION && secondPressDuration < MAX_KEY_PRESS_DURATION) {
                            keystrokeDataMap.get(bigram).add(new KeystrokeData(bigram, firstPressDuration, secondPressDuration, releaseToPressDuration));
                        if (keystrokeDataMap.get(bigram).size() == SAMPLES_PER_BIGRAM) {
                            System.out.println("Собранная биграмма: " + bigram );
                        }
                    }else{System.out.println("Возможно произошло залипание клавиши");}
                    }
                    firstKeyPressTime = secondKeyPressTime;
                    firstKeyReleaseTime = 0;
                    if (isDataCollectionComplete()) {
                        System.out.println("Все данные собраны.");
                        processAndClose();
                    }
                }
            }
        }
    }

    private void handleKeyReleased(KeyEvent e) {
        long currentTime = System.currentTimeMillis();
        if (firstKeyReleaseTime == 0) {
            firstKeyReleaseTime = currentTime;
        }
    }

    private boolean isDataCollectionComplete() {
        return keystrokeDataMap.values().stream().allMatch(list -> list.size() == SAMPLES_PER_BIGRAM);
    }

    private void processAndClose() {
        KeystrokeProcessor processor = new KeystrokeProcessor();
        processor.createImage(keystrokeDataMap);
        this.dispose();
    }

    public static void run() {
        SwingUtilities.invokeLater(() -> {
            KeystrokeCollector collector = new KeystrokeCollector();
            collector.setVisible(true);
        });
    }
}
