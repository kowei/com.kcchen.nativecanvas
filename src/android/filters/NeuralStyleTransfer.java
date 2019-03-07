package com.kcchen.nativecanvas.filters;

/**
 * Created by kowei on 2018/4/3.
 */

public class NeuralStyleTransfer {
    /**
     * Values suggested by
     * https://harishnarayanan.org/writing/artistic-style-transfer/
     *
     * Will likely change this, or make them parameters.
     */
    public static double content_weight = 0.025;
    public static double style_weight = 5.0;
    public static double total_variation_weight = 1.0;

    /**
     * Image conversion/size properties
     */
    public static final int HEIGHT = 224;
    public static final int WIDTH = 224;
    public static final int CHANNELS = 3;
    public static final int IMAGE_SIZE = HEIGHT*WIDTH;

    /**
     * Element-wise differences are squared, and then summed.
     * This is modelled after the content_loss method defined in
     * https://harishnarayanan.org/writing/artistic-style-transfer/
     *
     * @param a One tensor
     * @param b Another tensor
     * @return Sum of squared errors: scalar
     */
//    public static double sumOfSquaredErrors(INDArray a, INDArray b) {
//        INDArray diff = a.sub(b); // difference
//        INDArray squares = Transforms.pow(diff, 2); // element-wise squaring
//        return squares.sumNumber().doubleValue();
//    }

    /**
     * After passing in the content, style, and combination images,
     * compute the loss with respect to the content. Based off of:
     * https://harishnarayanan.org/writing/artistic-style-transfer/
     *
     * @param activations Intermediate layer activations from the three inputs
     * @return Weighted content loss component
     */
//    public static double content_loss(Map<String, INDArray> activations) {
//        INDArray block2_conv2_features = activations.get("block2_conv2");
//        INDArray content_features = block2_conv2_features.get(NDArrayIndex.point(0), NDArrayIndex.all(), NDArrayIndex.all(), NDArrayIndex.all());
//        INDArray combination_features = block2_conv2_features.get(NDArrayIndex.point(2), NDArrayIndex.all(), NDArrayIndex.all(), NDArrayIndex.all());
//        return content_weight * sumOfSquaredErrors(content_features, combination_features);
//    }

    /**
     * Equation (2) from the Gatys et all paper: https://arxiv.org/pdf/1508.06576.pdf
     * This is the derivative of the content loss w.r.t. the combo image features
     * within a specific layer of the CNN.
     *
     * @param originalFeatures Features at particular layer from the original content image
     * @param comboFeatures Features at same layer from current combo image
     * @return Derivatives of content loss w.r.t. combo features
     */
//    public static INDArray derivativeLossContentInLayer(INDArray originalFeatures, INDArray comboFeatures) {
//        // Create tensor of 0 and 1 indicating whether values in comboFeatures are positive or negative
//        INDArray mult = comboFeatures.dup();
//        BooleanIndexing.applyWhere(mult, Conditions.lessThan(0.0f), new Value(0.0f));
//        BooleanIndexing.applyWhere(mult, Conditions.greaterThan(0.0f), new Value(1.0f));
//        // Compute the F^l - P^l portion of equation (2), where F^l = comboFeatures and P^l = originalFeatures
//        INDArray diff = comboFeatures.sub(originalFeatures);
//        // This multiplication assures that the result is 0 when the value from F^l < 0, but is still F^l - P^l otherwise
//        return diff.mul(mult);
//    }

    /**
     * Computing the Gram matrix as described here:
     * https://harishnarayanan.org/writing/artistic-style-transfer/
     * Permuting dimensions is not needed because DL4J stores
     * the channel at the front rather than the end of the tensor.
     * Basically, each tensor is flattened into a vector so that
     * the dot product can be calculated.
     *
     * @param x Tensor to get Gram matrix of
     * @return Resulting Gram matrix
     */
//    public static INDArray gram_matrix(INDArray x) {
//        INDArray flattened = Nd4j.toFlattened(x);
//        // mmul is dot product/outer product
//        INDArray gram = flattened.mmul(flattened.dup().transpose()); // Is the dup necessary?
//        return gram;
//    }

    /**
     * This method is simply called style_loss in
     * https://harishnarayanan.org/writing/artistic-style-transfer/
     * but it takes inputs for intermediate activations from a particular
     * layer, hence my re-name. These values contribute to the total
     * style loss.
     *
     * @param style Activations from intermediate layer of CNN for style image input
     * @param combination Activations from intermediate layer of CNN for combination image input
     * @return Loss contribution from this comparison
     */
//    public static double style_loss_for_one_layer(INDArray style, INDArray combination) {
//        INDArray s = gram_matrix(style);
//        INDArray c = gram_matrix(combination);
//        return sumOfSquaredErrors(s,c) / (4.0 * (CHANNELS * CHANNELS) * (IMAGE_SIZE * IMAGE_SIZE));
//    }

    /**
     * The overall style loss calculation shown in
     * https://harishnarayanan.org/writing/artistic-style-transfer/
     * for every relevant intermediate layer of the CNN.
     *
     * @param activations Intermediate activations of all CNN layers
     * @return weighted style loss component
     */
//    public static double style_loss(Map<String, INDArray> activations) {
//        String[] feature_layers = new String[] {
//                "block1_conv2", "block2_conv2",
//                "block3_conv3", "block4_conv3",
//                "block5_conv3"};
//        double loss = 0.0;
//        for(String layer_name : feature_layers) {
//            INDArray layer_features = activations.get(layer_name);
//            INDArray style_features = layer_features.get(NDArrayIndex.point(1), NDArrayIndex.all(), NDArrayIndex.all(), NDArrayIndex.all());
//            INDArray combination_features = layer_features.get(NDArrayIndex.point(2), NDArrayIndex.all(), NDArrayIndex.all(), NDArrayIndex.all());
//            double sl = style_loss_for_one_layer(style_features, combination_features);
//            loss += (style_weight / feature_layers.length) * sl;
//        }
//        return loss;
//    }

    /**
     * Equation (6) from the Gatys et all paper: https://arxiv.org/pdf/1508.06576.pdf
     * This is the derivative of the style error for a single layer w.r.t. the
     * combo image features at that layer.
     *
     * @param styleFeatures Intermediate activations of one layer for style input
     * @param comboFeatures Intermediate activations of one layer for combo image input
     * @return Derivative of style error matrix for the layer w.r.t. combo image
     */
//    public static INDArray derivativeLossStyleInLayer(INDArray styleFeatures, INDArray comboFeatures) {
//        // Create tensor of 0 and 1 indicating whether values in comboFeatures are positive or negative
//        INDArray mult = comboFeatures.dup();
//        BooleanIndexing.applyWhere(mult, Conditions.lessThan(0.0f), new Value(0.0f));
//        BooleanIndexing.applyWhere(mult, Conditions.greaterThan(0.0f), new Value(1.0f));
//
//        double styleWeight = 1.0 / ((CHANNELS * CHANNELS) * (IMAGE_SIZE * IMAGE_SIZE));
//        // Corresponds to A^l in equation (6)
//        INDArray a = gram_matrix(styleFeatures); // Should this actually be the content image?
//        // Corresponds to G^l in equation (6)
//        INDArray g = gram_matrix(comboFeatures);
//        // G^l - A^l
//        INDArray diff = g.sub(a);
//        // (F^l)^T * (G^l - A^l)
//        // ERROR HERE! PROBLEM WITH TRANSPOSE? IS THIS THE RIGHT TYPE OF MULTIPLICATION?
//        INDArray product = comboFeatures.transpose().mmul(diff);
//        // (1/(N^2 * M^2)) * ((F^l)^T * (G^l - A^l))
//        INDArray posResult = product.mul(styleWeight);
//        // This multiplication assures that the result is 0 when the value from F^l < 0, but is still (1/(N^2 * M^2)) * ((F^l)^T * (G^l - A^l)) otherwise
//        return posResult.mul(mult);
//    }

    /**
     * Returns weighted total variation loss that smooths the error across the image.
     * Based on https://harishnarayanan.org/writing/artistic-style-transfer/ again.
     * I'm not sure what the point of leaving out certain edges is in the tensor bounds,
     * but I expect the point is to reduce the variance between directly adjacent pixels.
     *
     * @param combination Combination image
     * @return Weighted total variation loss across combo image
     */
//    public static double total_variation_loss(INDArray combination) {
//        INDArray sliceA1 = combination.get(NDArrayIndex.all(), NDArrayIndex.all(), NDArrayIndex.interval(0, HEIGHT-1), NDArrayIndex.interval(0, WIDTH-1));
//        INDArray sliceA2 = combination.get(NDArrayIndex.all(), NDArrayIndex.all(), NDArrayIndex.interval(1, HEIGHT), NDArrayIndex.interval(0, WIDTH-1));
//        INDArray diffA = sliceA1.sub(sliceA2);
//        INDArray a = Transforms.pow(diffA, 2); // element-wise squaring
//
//        INDArray sliceB1 = combination.get(NDArrayIndex.all(), NDArrayIndex.all(), NDArrayIndex.interval(0, HEIGHT-1), NDArrayIndex.interval(0, WIDTH-1));
//        INDArray sliceB2 = combination.get(NDArrayIndex.all(), NDArrayIndex.all(), NDArrayIndex.interval(0, HEIGHT-1), NDArrayIndex.interval(1, WIDTH));
//        INDArray diffB = sliceB1.sub(sliceB2);
//        INDArray b = Transforms.pow(diffB, 2); // element-wise squaring
//
//        INDArray add = a.add(b);
//        INDArray pow = Transforms.pow(add, 1.25);
//        return total_variation_weight * pow.sumNumber().doubleValue();
//    }

    /**
     * Computes complete loss function for the neural style transfer
     * optimization problem by adding up all of the components.
     *
     * @param activations Activations from the content, style, and combination images (TODO: cache those that don't change)
     * @param combination Combined image (so far)
     * @return Loss value
     */
//    public static double neuralStyleLoss(Map<String, INDArray> activations, INDArray combination) {
//        double loss = 0;
//        loss += content_loss(activations);
//        loss += style_loss(activations);
//        loss += total_variation_loss(combination);
//        return loss;
//    }

//    public static void main(String[] args) throws IOException {
//        ZooModel zooModel = new VGG16();
//        ComputationGraph vgg16 = (ComputationGraph) zooModel.initPretrained(PretrainedType.IMAGENET);
//
//        NativeImageLoader loader = new NativeImageLoader(HEIGHT, WIDTH, CHANNELS);
//        DataNormalization scaler = new VGG16ImagePreProcessor();
//
//        String contentFile = "data/imagematch/cat.jpg";
//        INDArray content = loader.asMatrix(new File(contentFile));
//        scaler.transform(content);
//
//        String styleFile = "data/imagematch/supercreepypersonimage.jpg";
//        INDArray style = loader.asMatrix(new File(styleFile));
//        scaler.transform(style);
//
//        // Starting combination image is pure white noise
//        int totalEntries = CHANNELS*HEIGHT*WIDTH;
//        int[] upper = new int[totalEntries];
//        Arrays.fill(upper, 256);
//        INDArray combination = Nd4j.create(ArrayUtil.doubleArrayFromIntegerArray(RandomNumbers.randomIntArray(upper)), new int[] {1, CHANNELS, HEIGHT, WIDTH});
//        scaler.transform(combination);
//
//        int iterations = 10; // TODO: Change to a parameter
//        double learningRate = 0.01; // TODO: Change to parameter
//
//        // TODO: Generalize: should not need to get this list manually
//        String[] lowerLayers = new String[] {
//                "input_1", // Need this?
//                "block1_conv1",
//                "block1_conv2",
//                "block1_pool", // Need this?
//                "block2_conv1",
//                "block2_conv2"
//        };
//
//        for(int itr = 0; itr < iterations; itr++) {
//            System.out.println("Iteration: " + itr);
//
//            // Stacking the inputs works for now, but is inefficient in the long run.
//            // TODO: Don't recalculate activations that won't change.
//            INDArray input = Nd4j.concat(0, content, style, combination);
//            // Activate the network
//            INDArray[] result = vgg16.output(input);
//            // Get the intermediate activations
//            Map<String, INDArray> activations = vgg16.feedForward();
//            // Calculate overall loss
//            // TODO: This isn't being used ... why not?
//            double loss = neuralStyleLoss(activations, combination);
//            // One iteration of passing the gradient back
//            Layer block2_conv2 = vgg16.getLayer("block2_conv2");
//            int[] shape = activations.get("block2_conv2").shape(); // Shape of whole stack for three inputs
//            shape[0] = 1; // Get the shape for a single image input
//            INDArray dLdANext = Nd4j.zeros(shape);	//Shape: size of activations of block2_conv2 for one image
//            int startLayer = block2_conv2.getIndex();
//            for(int i = startLayer; i >= 0; i++) {
//                System.out.println("Layer: " + lowerLayers[i]);
//                // This code below doesn't actually use the result of the loss function, just the derivatives. Is this ok?
//                INDArray layerFeatures = activations.get(lowerLayers[i]);
//                // Some duplicated code here ... fix later
//                INDArray content_features = layerFeatures.get(NDArrayIndex.point(0), NDArrayIndex.all(), NDArrayIndex.all(), NDArrayIndex.all());
//                INDArray style_features = layerFeatures.get(NDArrayIndex.point(1), NDArrayIndex.all(), NDArrayIndex.all(), NDArrayIndex.all());
//                INDArray combination_features = layerFeatures.get(NDArrayIndex.point(2), NDArrayIndex.all(), NDArrayIndex.all(), NDArrayIndex.all());
//
//                // Equation (6) from the Gatys et all paper: https://arxiv.org/pdf/1508.06576.pdf
//                INDArray dLstyle_currLayer = derivativeLossStyleInLayer(style_features, combination_features);
//                // Equation (2) from the Gatys et all paper: https://arxiv.org/pdf/1508.06576.pdf
//                INDArray dLcontent_currLayer = derivativeLossContentInLayer(content_features, combination_features);
//
//                INDArray dLdAOut = dLdANext.add(dLcontent_currLayer).add(dLstyle_currLayer);
//
//                Layer l = vgg16.getLayer(i);
//                dLdANext = l.backpropGradient(dLdAOut).getSecond();
//            }
//            //Once loop ends: dLdANext == dL/dCombination
//
//            // Update combination image
//            combination.subi(dLdANext.mul(learningRate)); // Maybe replace with an Updater later?
//        }
//
//        // Undo color mean subtraction
//        scaler.revertFeatures(combination);
//        // Show final image afterward
//        BufferedImage output = GraphicsUtil.imageFromINDArray(combination);
//        DrawingPanel panel = GraphicsUtil.drawImage(output, "Combined Image", WIDTH, HEIGHT);
//        MiscUtil.waitForReadStringAndEnterKeyPress();
//    }
}
