package net.haesleinhuepf.clij.demo;

import clearcl.ClearCLImage;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.plugin.Duplicator;
import net.haesleinhuepf.clij.CLIJ;
import net.haesleinhuepf.clij.kernels.Kernels;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.integer.UnsignedShortType;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * This example shows how to take input images, process them through
 * by two OpenCL kernels and show the result
 * <p>
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * February 2018
 */
public class PipelineCLDemo {

    public static void main(String... args) throws IOException {
        // Initialize ImageJ and example images
        new ImageJ();
        ImagePlus inputImp = IJ.openImage("src/main/resources/flybrain.tif");

        RandomAccessibleInterval<UnsignedShortType> input = ImageJFunctions.wrap(inputImp);

        RandomAccessibleInterval<UnsignedShortType> output = ImageJFunctions.wrap(new Duplicator().run(inputImp));

        ImageJFunctions.show(input);

        // Startup OpenCL device, convert images to ClearCL format
        CLIJ clij = CLIJ.getInstance();

        ClearCLImage inputCLImage = clij.convert(input, ClearCLImage.class);
        ClearCLImage outputCLImage = clij.convert(output, ClearCLImage.class);

        // ---------------------------------------------------------------
        // Example step 1: Downsampling

        Map<String, Object> lParameterMap = new HashMap<>();
        lParameterMap.put("src", inputCLImage);
        lParameterMap.put("dst", outputCLImage);
        lParameterMap.put("factor_x", 0.5f);
        lParameterMap.put("factor_y", 0.5f);
        lParameterMap.put("factor_z", 1.f);

        clij.execute(Kernels.class, "downsampling.cl", "downsample_3d_nearest", lParameterMap);

        // Convert/copy and show intermediate result
        RandomAccessibleInterval intermediateResult = clij.convert(outputCLImage, RandomAccessibleInterval.class);

        ImageJFunctions.show(intermediateResult);

        // ---------------------------------------------------------------
        // Example Step 2: Bluring
        HashMap<String, Object> lBlurParameterMap = new HashMap<>();
        lBlurParameterMap.put("Nx", 3);
        lBlurParameterMap.put("Ny", 3);
        lBlurParameterMap.put("Nz", 3);
        lBlurParameterMap.put("sx", 2.0f);
        lBlurParameterMap.put("sy", 2.0f);
        lBlurParameterMap.put("sz", 2.0f);
        // we reuse memory in the GPU by taking the result from the former
        // step as input here and the input from the former step as
        // output:
        lBlurParameterMap.put("src", outputCLImage);
        lBlurParameterMap.put("dst", inputCLImage);

        clij.execute(Kernels.class, "blur.cl", "gaussian_blur_image3d", lBlurParameterMap);

        // Convert and show final result
        RandomAccessibleInterval result = clij.convert(inputCLImage, RandomAccessibleInterval.class);

        ImageJFunctions.show(result);


    }
}
