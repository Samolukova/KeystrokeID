package org.example;

import org.datavec.image.loader.NativeImageLoader;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;
import org.nd4j.linalg.factory.Nd4j;

import java.io.File;

public class KeystrokePrediction {
    private static final int HEIGHT = 3;
    private static final int WIDTH = 11;
    private static final int CHANNELS = 1;
    private static final String MODEL_PATH = "model_withoutDropoutAll.zip";
    private static final String IMAGE_PATH = "daria_1.png";
    private static final String[] NAMES = {"daria","marina", "olga","sasha","tanya"};
  //   private static final String[] NAMES = {"test","try"};
    public static void start() {
        try {
            File modelFile = new File(MODEL_PATH);
            MultiLayerNetwork model = ModelSerializer.restoreMultiLayerNetwork(modelFile);

            File imageFile = new File(IMAGE_PATH);
            NativeImageLoader loader = new NativeImageLoader(HEIGHT, WIDTH, CHANNELS);
            INDArray image = loader.asMatrix(imageFile);
            ImagePreProcessingScaler scaler = new ImagePreProcessingScaler(0, 1);
            scaler.transform(image);

            INDArray output = model.output(image);

            System.out.println("Предсказание: ");
            for (int i = 0; i < output.columns(); i++) {
                System.out.println(NAMES[i] + ": " + output.getFloat(i));
            }

            int predictedClass = Nd4j.argMax(output, 1).getInt(0);
            System.out.println("Наиболее вероятный класс: " + NAMES[predictedClass]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
