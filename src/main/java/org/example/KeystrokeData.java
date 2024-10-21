package org.example;

public class KeystrokeData {
    private final String bigram;
    private final long firstKeyPressDuration;
    private final long secondKeyPressDuration;
    private final long releaseToPressDuration;

    public KeystrokeData(String bigram, long firstKeyPressDuration, long secondKeyPressDuration, long releaseToPressDuration) {
        this.bigram = bigram;
        this.firstKeyPressDuration = firstKeyPressDuration;
        this.secondKeyPressDuration = secondKeyPressDuration;
        this.releaseToPressDuration = releaseToPressDuration;
    }

    public String getBigram() {
        return bigram;
    }

    public long getFirstKeyPressDuration() {
        return firstKeyPressDuration;
    }

    public long getSecondKeyPressDuration() {
        return secondKeyPressDuration;
    }

    public long getReleaseToPressDuration() {
        return releaseToPressDuration;
    }
}
