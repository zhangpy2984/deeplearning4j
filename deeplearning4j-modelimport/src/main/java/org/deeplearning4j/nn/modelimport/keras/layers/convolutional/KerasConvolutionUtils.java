/*-
 *
 *  * Copyright 2017 Skymind,Inc.
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *        http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 */
package org.deeplearning4j.nn.modelimport.keras.layers.convolutional;

import org.deeplearning4j.nn.conf.ConvolutionMode;
import org.deeplearning4j.nn.modelimport.keras.config.KerasLayerConfiguration;
import org.deeplearning4j.nn.modelimport.keras.exceptions.InvalidKerasConfigurationException;
import org.deeplearning4j.nn.modelimport.keras.exceptions.UnsupportedKerasConfigurationException;
import org.deeplearning4j.nn.modelimport.keras.utils.KerasLayerUtils;
import org.nd4j.linalg.util.ArrayUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Utility functionality for Keras convolution layers.
 *
 * @author Max Pumperla
 */
public class KerasConvolutionUtils {

    /**
     * Get (convolution) stride from Keras layer configuration.
     *
     * @param layerConfig dictionary containing Keras layer configuration
     * @return
     * @throws InvalidKerasConfigurationException
     */
    public static int[] getStrideFromConfig(Map<String, Object> layerConfig, int dimension,
                                            KerasLayerConfiguration conf)
            throws InvalidKerasConfigurationException {
        Map<String, Object> innerConfig = KerasLayerUtils.getInnerLayerConfigFromConfig(layerConfig, conf);
        int[] strides = null;
        if (innerConfig.containsKey(conf.getLAYER_FIELD_CONVOLUTION_STRIDES()) && dimension == 2) {
            /* 2D Convolutional layers. */
            List<Integer> stridesList = (List<Integer>) innerConfig.get(conf.getLAYER_FIELD_CONVOLUTION_STRIDES());
            strides = ArrayUtil.toArray(stridesList);
        } else if (innerConfig.containsKey(conf.getLAYER_FIELD_SUBSAMPLE_LENGTH()) && dimension == 1) {
           /* 1D Convolutional layers. */
            int subsample_length = (int) innerConfig.get(conf.getLAYER_FIELD_SUBSAMPLE_LENGTH());
            strides = new int[]{subsample_length};
        } else if (innerConfig.containsKey(conf.getLAYER_FIELD_POOL_STRIDES())) {
            /* Pooling layers. */
            List<Integer> stridesList = (List<Integer>) innerConfig.get(conf.getLAYER_FIELD_POOL_STRIDES());
            strides = ArrayUtil.toArray(stridesList);
        } else
            throw new InvalidKerasConfigurationException("Could not determine layer stride: no "
                    + conf.getLAYER_FIELD_CONVOLUTION_STRIDES() + " or "
                    + conf.getLAYER_FIELD_POOL_STRIDES() + " field found");
        return strides;
    }

    /**
     * Get (convolution) kernel size from Keras layer configuration.
     *
     * @param layerConfig dictionary containing Keras layer configuration
     * @return
     * @throws InvalidKerasConfigurationException
     */
    public static int[] getKernelSizeFromConfig(Map<String, Object> layerConfig, int dimension,
                                                KerasLayerConfiguration conf, int kerasMajorVersion)
            throws InvalidKerasConfigurationException {
        Map<String, Object> innerConfig = KerasLayerUtils.getInnerLayerConfigFromConfig(layerConfig, conf);
        int[] kernelSize = null;
        if (kerasMajorVersion != 2) {
            if (innerConfig.containsKey(conf.getLAYER_FIELD_NB_ROW()) && dimension == 2
                    && innerConfig.containsKey(conf.getLAYER_FIELD_NB_COL())) {
            /* 2D Convolutional layers. */
                List<Integer> kernelSizeList = new ArrayList<Integer>();
                kernelSizeList.add((Integer) innerConfig.get(conf.getLAYER_FIELD_NB_ROW()));
                kernelSizeList.add((Integer) innerConfig.get(conf.getLAYER_FIELD_NB_COL()));
                kernelSize = ArrayUtil.toArray(kernelSizeList);
            } else if (innerConfig.containsKey(conf.getLAYER_FIELD_FILTER_LENGTH()) && dimension == 1) {
            /* 1D Convolutional layers. */
                int filter_length = (int) innerConfig.get(conf.getLAYER_FIELD_FILTER_LENGTH());
                kernelSize = new int[]{ filter_length };
            } else if (innerConfig.containsKey(conf.getLAYER_FIELD_POOL_SIZE())) {
            /* Pooling layers. */
                List<Integer> kernelSizeList = (List<Integer>) innerConfig.get(conf.getLAYER_FIELD_POOL_SIZE());
                kernelSize = ArrayUtil.toArray(kernelSizeList);
            } else {
                throw new InvalidKerasConfigurationException("Could not determine kernel size: no "
                        + conf.getLAYER_FIELD_NB_ROW() + ", "
                        + conf.getLAYER_FIELD_NB_COL() + ", or "
                        + conf.getLAYER_FIELD_FILTER_LENGTH() + ", or "
                        + conf.getLAYER_FIELD_POOL_SIZE() + " field found");
            }
        } else {
            /* 2D Convolutional layers. */
            if (innerConfig.containsKey(conf.getLAYER_FIELD_KERNEL_SIZE()) && dimension == 2) {
                List<Integer> kernelSizeList = (List<Integer>) innerConfig.get(conf.getLAYER_FIELD_KERNEL_SIZE());
                kernelSize = ArrayUtil.toArray(kernelSizeList);
            } else if (innerConfig.containsKey(conf.getLAYER_FIELD_FILTER_LENGTH()) && dimension == 1) {
            /* 1D Convolutional layers. */
                int filter_length = (int) innerConfig.get(conf.getLAYER_FIELD_FILTER_LENGTH());
                kernelSize = new int[]{ filter_length };
            } else if (innerConfig.containsKey(conf.getLAYER_FIELD_POOL_SIZE())) {
            /* Pooling layers. */
                List<Integer> kernelSizeList = (List<Integer>) innerConfig.get(conf.getLAYER_FIELD_POOL_SIZE());
                kernelSize = ArrayUtil.toArray(kernelSizeList);
            } else {
                throw new InvalidKerasConfigurationException("Could not determine kernel size: no "
                        + conf.getLAYER_FIELD_KERNEL_SIZE() + ", or "
                        + conf.getLAYER_FIELD_FILTER_LENGTH() + ", or "
                        + conf.getLAYER_FIELD_POOL_SIZE() + " field found");
            }
        }

        return kernelSize;
    }

    /**
     * Get convolution border mode from Keras layer configuration.
     *
     * @param layerConfig dictionary containing Keras layer configuration
     * @return
     * @throws InvalidKerasConfigurationException
     */
    public static ConvolutionMode getConvolutionModeFromConfig(Map<String, Object> layerConfig,
                                                        KerasLayerConfiguration conf)
            throws InvalidKerasConfigurationException, UnsupportedKerasConfigurationException {
        Map<String, Object> innerConfig = KerasLayerUtils.getInnerLayerConfigFromConfig(layerConfig, conf);
        if (!innerConfig.containsKey(conf.getLAYER_FIELD_BORDER_MODE()))
            throw new InvalidKerasConfigurationException("Could not determine convolution border mode: no "
                    + conf.getLAYER_FIELD_BORDER_MODE() + " field found");
        String borderMode = (String) innerConfig.get(conf.getLAYER_FIELD_BORDER_MODE());
        ConvolutionMode convolutionMode = null;
        if (borderMode.equals(conf.getLAYER_BORDER_MODE_SAME())) {
            /* Keras relies upon the Theano and TensorFlow border mode definitions and operations:
             * TH: http://deeplearning.net/software/theano/library/tensor/nnet/conv.html#theano.tensor.nnet.conv.conv2d
             * TF: https://www.tensorflow.org/api_docs/python/nn/convolution#conv2d
             */
            convolutionMode = ConvolutionMode.Same;

        } else if (borderMode.equals(conf.getLAYER_BORDER_MODE_VALID()) ||
                borderMode.equals(conf.getLAYER_BORDER_MODE_FULL())) {
            convolutionMode = ConvolutionMode.Truncate;

        } else {
            throw new UnsupportedKerasConfigurationException("Unsupported convolution border mode: " + borderMode);
        }
        return convolutionMode;
    }

    /**
     * Get (convolution) padding from Keras layer configuration.
     *
     * @param layerConfig dictionary containing Keras layer configuration
     * @return
     * @throws InvalidKerasConfigurationException
     */
    public static int[] getPaddingFromBorderModeConfig(Map<String, Object> layerConfig, int dimension,
                                                KerasLayerConfiguration conf, int kerasMajorVersion)
            throws InvalidKerasConfigurationException, UnsupportedKerasConfigurationException {
        Map<String, Object> innerConfig = KerasLayerUtils.getInnerLayerConfigFromConfig(layerConfig, conf);
        int[] padding = null;
        if (!innerConfig.containsKey(conf.getLAYER_FIELD_BORDER_MODE()))
            throw new InvalidKerasConfigurationException("Could not determine convolution border mode: no "
                    + conf.getLAYER_FIELD_BORDER_MODE() + " field found");
        String borderMode = (String) innerConfig.get(conf.getLAYER_FIELD_BORDER_MODE());
        if (borderMode == conf.getLAYER_FIELD_BORDER_MODE()) {
            padding = getKernelSizeFromConfig(layerConfig, dimension, conf, kerasMajorVersion);
            for (int i = 0; i < padding.length; i++)
                padding[i]--;
        }
        return padding;
    }
}
