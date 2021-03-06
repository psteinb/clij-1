package net.haesleinhuepf.clij.demo;

import clearcl.ClearCLImage;
import ij.IJ;
import ij.ImagePlus;
import net.haesleinhuepf.clij.CLIJ;
import net.haesleinhuepf.clij.kernels.Kernels;

/**
 * LocalThresholdDemo
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 06 2018
 */
public class LocalThresholdDemo {
    public static void main(String... args) {
        ImagePlus imp = IJ.openImage("src/main/resources/droso_crop.tif");

        CLIJ clij = CLIJ.getInstance();

        // conversion
        ClearCLImage input = clij.convert(imp, ClearCLImage.class);
        ClearCLImage output = clij.createCLImage(input);
        ClearCLImage temp = clij.createCLImage(input);

        // blur
        Kernels.blurFast(clij, input, temp, 2, 2, 2);

        // local threshold
        Kernels.localThreshold(clij, input, output, temp);

        Kernels.erodeSphere(clij, output, temp);
        Kernels.erodeSphere(clij, temp, output);

        // show results
        clij.show(input, "original");
        clij.show(output, "mask");

        input.close();
        output.close();
        temp.close();
    }
}
