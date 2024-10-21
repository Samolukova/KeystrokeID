package org.example;

import org.datavec.api.io.filters.BalancedPathFilter;
import org.datavec.api.io.labels.ParentPathLabelGenerator;
import org.datavec.api.split.FileSplit;
import org.datavec.api.split.InputSplit;
import org.datavec.image.loader.NativeImageLoader;
import org.datavec.image.recordreader.ImageRecordReader;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.*;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerMinMaxScaler;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Random;

public class KeystrokeCNN {
    private static final int HEIGHT = 3;
    private static final int WIDTH = 11;
    private static final int CHANNELS = 1;
    private static final int NUM_CLASSES = 5;
    private static final int BATCH_SIZE = 4;
    private static final int EPOCHS = 10;
    private static final String TRAIN_DATA_PATH = "data/trainData";
    private static final String TEST_DATA_PATH = "data/testData";
    private static final String MODEL_PATH = "model_withoutDropoutAll.zip";

    public static void start() throws IOException {

        File trainData = new File(TRAIN_DATA_PATH);
        File testData = new File(TEST_DATA_PATH);


        ParentPathLabelGenerator labelMaker = new ParentPathLabelGenerator();
        ImageRecordReader trainRecordReader = new ImageRecordReader(HEIGHT, WIDTH, CHANNELS, labelMaker);
        ImageRecordReader testRecordReader = new ImageRecordReader(HEIGHT, WIDTH, CHANNELS, labelMaker);

        //разделения файлов данных на обучающие и тестовые наборы
        FileSplit trainSplit = new FileSplit(trainData, NativeImageLoader.ALLOWED_FORMATS, new Random());
        FileSplit testSplit = new FileSplit(testData, NativeImageLoader.ALLOWED_FORMATS, new Random());

        trainRecordReader.initialize(trainSplit);
        testRecordReader.initialize(testSplit);


        DataSetIterator trainIterator = new RecordReaderDataSetIterator(trainRecordReader, BATCH_SIZE, 1, NUM_CLASSES);
        DataSetIterator testIterator = new RecordReaderDataSetIterator(testRecordReader, BATCH_SIZE, 1, NUM_CLASSES);

        // нормализация
        DataNormalization scaler = new NormalizerMinMaxScaler();
        scaler.fit(trainIterator);
        trainIterator.setPreProcessor(scaler);
        testIterator.setPreProcessor(scaler);


        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(123)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .updater(new Adam(1e-3))
                .weightInit(WeightInit.XAVIER)
                .list()
                .layer(new ConvolutionLayer.Builder(2, 2)
                        .nIn(CHANNELS)
                        .stride(1, 1)
                        .padding(1, 1)
                        .nOut(32)
                        .activation(Activation.RELU)
                        .build())
                .layer(new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
                        .kernelSize(2, 2) // Changed kernel size to 2x2
                        .stride(1, 1)
                        .build())
                .layer(new ConvolutionLayer.Builder(1, 1)
                        .stride(1, 1)
                        .nOut(64) // Increased number of filters
                        .activation(Activation.RELU)
                        .build())
                .layer(new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
                        .kernelSize(2, 2)
                        .stride(1, 1)
                        .build())
                .layer(new ConvolutionLayer.Builder(1, 1)
                        .stride(1, 1)
                        .nOut(64) // Increased number of filters
                        .activation(Activation.RELU)
                        .build())
                .layer(new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
                        .kernelSize(2, 2)
                        .stride(1, 1)
                        .build())
                .layer(new DenseLayer.Builder().nOut(128).activation(Activation.RELU).build())

                .layer(new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .nOut(NUM_CLASSES)
                        .activation(Activation.SOFTMAX)
                        .build())
                .setInputType(InputType.convolutional(HEIGHT, WIDTH, CHANNELS))
                .build();


        MultiLayerNetwork model = new MultiLayerNetwork(conf);
        model.init();
        model.setListeners(new ScoreIterationListener(10));

        System.out.println("Обучение модели...");
        for (int i = 0; i < EPOCHS; i++) {
            model.fit(trainIterator);
        }

        System.out.println("Оценка модели...");
        Evaluation eval = new Evaluation(NUM_CLASSES);
        while (testIterator.hasNext()) {
            DataSet next = testIterator.next();
            INDArray output = model.output(next.getFeatures(), false);
            eval.eval(next.getLabels(), output);
        }
        System.out.println(eval.stats());


        System.out.println("Saving model...");
        ModelSerializer.writeModel(model, MODEL_PATH, true);
    }
}
