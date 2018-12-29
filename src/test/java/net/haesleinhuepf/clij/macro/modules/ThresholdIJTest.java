package net.haesleinhuepf.clij.macro.modules;

import clearcl.ClearCLBuffer;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import net.haesleinhuepf.clij.CLIJ;
import net.haesleinhuepf.clij.kernels.Kernels;
import net.haesleinhuepf.clij.macro.AbstractMacroPluginTest;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class ThresholdIJTest extends AbstractMacroPluginTest {
    @Test
    public void testIfIdentialWithImageJ(){
        new ImageJ();

        CLIJ clij = CLIJ.getInstance();
        ImagePlus testImage = IJ.openImage("src/test/resources/blobs.gif");
        Double threshold = 127.0;

        ClearCLBuffer bufferIn = clij.convert(testImage, ClearCLBuffer.class);
        ClearCLBuffer bufferOutCL = clij.createCLBuffer(bufferIn);
        ClearCLBuffer bufferOutIJ = clij.createCLBuffer(bufferIn);

        Object[] argsCL = {bufferIn, bufferOutCL, threshold};
        makeThresholdIJ(clij, argsCL).executeCL();

        Object[] argsIJ = {bufferIn, bufferOutIJ, threshold};
        makeThresholdIJ(clij, argsIJ).executeIJ();

        //clij.show(bufferOutCL, "cl " + bufferOutCL);
        //clij.show(bufferOutIJ, "ij");
        //new WaitForUserDialog("wait").show();

        ClearCLBuffer bufferOutCLBinary = clij.createCLBuffer(bufferIn);
        ClearCLBuffer bufferOutIJBinary = clij.createCLBuffer(bufferIn);

        Kernels.threshold(clij, bufferOutCL, bufferOutCLBinary, 1f);
        Kernels.threshold(clij, bufferOutIJ, bufferOutIJBinary, 1f);

        assertTrue(clBuffersEqual(clij, bufferOutIJBinary, bufferOutCLBinary));
        bufferIn.close();
        bufferOutCL.close();
        bufferOutIJ.close();
        bufferOutCLBinary.close();
        bufferOutIJBinary.close();
    }

    private ThresholdIJ makeThresholdIJ(CLIJ clij, Object[] args) {
        ThresholdIJ thresholdIJ = new ThresholdIJ();
        thresholdIJ.setClij(clij);
        thresholdIJ.setArgs(args);
        return thresholdIJ;
    }
}