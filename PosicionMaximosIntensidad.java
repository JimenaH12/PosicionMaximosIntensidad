import ij.*;
import ij.plugin.*;
import ij.process.*;
import ij.gui.*;
import ij.io.SaveDialog;
import java.io.*;
import java.awt.Rectangle;
import ij.gui.Plot;

public class PosicionMaximosIntensidad implements PlugIn {

    public void run(String arg) {

        ImagePlus imp = IJ.getImage();
        Roi roi = imp.getRoi();

        if (roi == null) {
            IJ.error("Debe seleccionar una ROI.");
            return;
        }

        Rectangle bounds = roi.getBounds();

        int width = bounds.width;
        int height = bounds.height;
        int slices = imp.getStackSize();

        // =========================
        // 1. Planos de interés
        // =========================
        double globalMax = -Double.MAX_VALUE;
        int xStar = -1;
        int yStar = -1;

        for (int z = 1; z <= slices; z++) {
            imp.setSlice(z);
            ImageProcessor ip = imp.getProcessor();

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {

                    double val = ip.getPixelValue(bounds.x + x, bounds.y + y);

                    if (val > globalMax) {
                        globalMax = val;
                        xStar = x;
                        yStar = y;
                    }
                }
            }
        }

        IJ.log("Plano Y* = " + yStar);
        IJ.log("Plano X* = " + xStar);

        // =========================
        // Maximos de intensidad en X para cada Slice
        // =========================
        SaveDialog sdY = new SaveDialog("Guardar perfil Y*", "y_plane_profile", ".csv");
        if (sdY.getFileName() != null) {

            try {
                BufferedWriter writer = new BufferedWriter(
                    new FileWriter(sdY.getDirectory() + sdY.getFileName())
                );

                writer.write("z,x_max,intensity_max\n");
		double[] zVals = new double[slices];
	        double[] xVals = new double[slices];
                for (int z = 1; z <= slices; z++) {

                    imp.setSlice(z);
                    ImageProcessor ip = imp.getProcessor();

                    double maxVal = -Double.MAX_VALUE;
                    int xMax = -1;

                    for (int x = 0; x < width; x++) {

                        double val = ip.getPixelValue(bounds.x + x, bounds.y + yStar);

                        if (val > maxVal) {
                            maxVal = val;
                            xMax = x;
                        }
                    }
		    // guardar datos para graficar
    		    zVals[z-1] = z-1;
                    xVals[z-1] = xMax;
                    writer.write((z-1) + "," + xMax + "," + maxVal + "\n");
		    
                }

                writer.close();
		Plot plotY = new Plot("Perfil Y*", "Slice (z)", "X(pixel)", zVals, xVals);
		plotY.show();

            } catch (IOException e) {
                IJ.error(e.getMessage());
            }
        }

        // =========================
        // Posición de máximos de intensidad en Y para cada Z
        // =========================
        SaveDialog sdX = new SaveDialog("Guardar perfil X*", "x_plane_profile", ".csv");
        if (sdX.getFileName() != null) {

            try {
                BufferedWriter writer = new BufferedWriter(
                    new FileWriter(sdX.getDirectory() + sdX.getFileName())
                );

                writer.write("z,y_max,intensity_max\n");
		double[] yVals = new double[slices];
                double[] zVals = new double[slices];
                for (int z = 1; z <= slices; z++) {

                    imp.setSlice(z);
                    ImageProcessor ip = imp.getProcessor();

                    double maxVal = -Double.MAX_VALUE;
                    int yMax = -1;

                    for (int y = 0; y < height; y++) {

                        double val = ip.getPixelValue(bounds.x + xStar, bounds.y + y);

                        if (val > maxVal) {
                            maxVal = val;
                            yMax = y;
                        }
                    }
		    // guardar datos para graficar
                    zVals[z-1] = z-1;
                    yVals[z-1] = yMax;
                    writer.write((z-1) + "," + yMax + "," + maxVal + "\n");
                }

                writer.close();
		Plot plotX = new Plot("Perfil X*", "Slice (z)", "Y(pixel)", zVals, yVals);
		plotX.show();
            } catch (IOException e) {
                IJ.error(e.getMessage());
            }
        }

        IJ.showMessage("Procesamiento completo.");
    }
}
