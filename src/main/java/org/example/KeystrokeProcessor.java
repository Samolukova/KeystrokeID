package org.example;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class KeystrokeProcessor {

    private static final int IMAGE_WIDTH = 11;
    private static final int IMAGE_HEIGHT = 3;
    private static final int MAX_PIXEL_VALUE = 255;
    private static final int MAX_DURATION = 300;
    private static final String[] bigrams = {"ов", "ст", "но", "ко", "ен", "ни", "во", "то", "ро", "ра", "на"};
    public void saveImage(BufferedImage image, String filename) {
        try {
            File outputfile = new File(filename);
            ImageIO.write(image, "png", outputfile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void createImage(Map<String, List<KeystrokeData>> keystrokeDataMap) {
        BufferedImage image = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_BYTE_GRAY);

        int col = 0;

        for (String bigram :bigrams) {
            List<KeystrokeData> keystrokeDataList = keystrokeDataMap.getOrDefault(bigram, new ArrayList<>());


            long sumFirstPressDuration = 0;
            long sumSecondPressDuration = 0;
            long sumReleaseToPressDuration = 0;
            int count = keystrokeDataList.size();

            for (KeystrokeData data : keystrokeDataList) {
                sumFirstPressDuration += data.getFirstKeyPressDuration();
                sumSecondPressDuration += data.getSecondKeyPressDuration();
                sumReleaseToPressDuration += data.getReleaseToPressDuration();
            }

            long avgFirstPressDuration = count > 0 ? sumFirstPressDuration / count : 0;
            long avgSecondPressDuration = count > 0 ? sumSecondPressDuration / count : 0;
            long avgReleaseToPressDuration = count > 0 ? sumReleaseToPressDuration / count : 0;

            int firstKeyPressPixel = normalizeToPixelValue(avgFirstPressDuration);
            int secondKeyPressPixel = normalizeToPixelValue(avgSecondPressDuration);
            int releaseToPressPixel = normalizeToPixelValue(avgReleaseToPressDuration);

            firstKeyPressPixel = Math.min(255, Math.max(0, firstKeyPressPixel));
            secondKeyPressPixel = Math.min(255, Math.max(0, secondKeyPressPixel));
            releaseToPressPixel = Math.min(255, Math.max(0, releaseToPressPixel));

            image.setRGB(col, 0, new Color(firstKeyPressPixel, firstKeyPressPixel, firstKeyPressPixel).getRGB());
            image.setRGB(col, 1, new Color(secondKeyPressPixel, secondKeyPressPixel, secondKeyPressPixel).getRGB());
            image.setRGB(col, 2, new Color(releaseToPressPixel, releaseToPressPixel, releaseToPressPixel).getRGB());

            col++;
        }

         BufferedImage bufferedImage= normalizeImageIntensity(image);
        String filename="res.png";
        saveImage(bufferedImage,filename);
    }

    private int normalizeToPixelValue(long duration) {
        int pixelValue = (int) (duration * MAX_PIXEL_VALUE / MAX_DURATION);
        return Math.min(pixelValue, MAX_PIXEL_VALUE);
    }

    private BufferedImage normalizeImageIntensity(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage normalizedImage = new BufferedImage(width, height, image.getType());

        int minPixel = 255;
        int maxPixel = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = new Color(image.getRGB(x, y)).getRed();
                if (pixel < minPixel) minPixel = pixel;
                if (pixel > maxPixel) maxPixel = pixel;
            }
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = new Color(image.getRGB(x, y)).getRed();
                int normalizedPixel = 255 * (pixel - minPixel) / (maxPixel - minPixel);
                normalizedImage.setRGB(x, y, new Color(normalizedPixel, normalizedPixel, normalizedPixel).getRGB());
            }
        }

        return normalizedImage;
    }
}
