import ij.*;
import ij.plugin.*;
import ij.process.*;
import ij.gui.*;
import ij.io.SaveDialog;
import java.io.*;
import java.awt.Rectangle;
import ij.gui.Plot;

// Define el plugin implementando la interfaz PlugIn
public class PosicionMaximosIntensidad implements PlugIn {

    // Método principal que ejecuta el plugin
    public void run(String arg) {

        // Obtiene la imagen activa en ImageJ
        ImagePlus imp = IJ.getImage();
        // Obtiene la ROI (región de interés) seleccionada
        Roi roi = imp.getRoi();

        // Verifica si no hay ROI seleccionada
        if (roi == null) {
            // Muestra un mensaje de error si no hay ROI
            IJ.error("Debe seleccionar una ROI.");
            return;
        }

        // Obtiene el rectángulo delimitador de la ROI
        Rectangle bounds = roi.getBounds();

        // Ancho de la ROI en píxeles
        int width = bounds.width;
        // Alto de la ROI en píxeles
        int height = bounds.height;
        // Número total de planos (slices) en el stack
        int slices = imp.getStackSize();

        // =========================
        // 1. Planos de interés
        // =========================

        // Inicializa el máximo global con el menor valor posible
        double globalMax = -Double.MAX_VALUE;
        // Coordenada X del píxel con máxima intensidad global
        int xStar = -1;
        // Coordenada Y del píxel con máxima intensidad global
        int yStar = -1;

        // Itera sobre cada slice del stack
        for (int z = 1; z <= slices; z++) {
            // Activa el slice z en la imagen
            imp.setSlice(z);
            // Obtiene el procesador de imagen del slice actual
            ImageProcessor ip = imp.getProcessor();

            // Itera sobre cada fila de la ROI
            for (int y = 0; y < height; y++) {
                // Itera sobre cada columna de la ROI
                for (int x = 0; x < width; x++) {

                    // Obtiene el valor del píxel en coordenadas absolutas
                    double val = ip.getPixelValue(bounds.x + x, bounds.y + y);

                    // Comprueba si el valor actual supera el máximo global
                    if (val > globalMax) {
                        // Actualiza el máximo global
                        globalMax = val;
                        // Guarda la coordenada X del nuevo máximo
                        xStar = x;
                        // Guarda la coordenada Y del nuevo máximo
                        yStar = y;
                    }
                }
            }
        }

        // Imprime en el log la fila del máximo global encontrado
        IJ.log("Plano Y* = " + yStar);
        // Imprime en el log la columna del máximo global encontrado
        IJ.log("Plano X* = " + xStar);

        // =========================
        // Maximos de intensidad en X para cada Slice
        // =========================

        // Abre diálogo para guardar el perfil del plano Y*
        SaveDialog sdY = new SaveDialog("Guardar perfil Y*", "y_plane_profile", ".csv");
        // Verifica que el usuario haya elegido un nombre de archivo
        if (sdY.getFileName() != null) {

            try {
                // Crea el archivo CSV en la ruta seleccionada
                BufferedWriter writer = new BufferedWriter(
                    new FileWriter(sdY.getDirectory() + sdY.getFileName())
                );

                // Escribe la cabecera del CSV
                writer.write("z,x_max,intensity_max\n");
                // Array para almacenar los índices de slice (eje Z)
                double[] zVals = new double[slices];
                // Array para almacenar las posiciones X del máximo por slice
                double[] xVals = new double[slices];

                // Itera sobre cada slice del stack
                for (int z = 1; z <= slices; z++) {

                    // Activa el slice z
                    imp.setSlice(z);
                    // Obtiene el procesador del slice actual
                    ImageProcessor ip = imp.getProcessor();

                    // Inicializa el máximo local con el menor valor posible
                    double maxVal = -Double.MAX_VALUE;
                    // Inicializa la posición X del máximo local
                    int xMax = -1;

                    // Recorre todos los píxeles en la fila Y* de la ROI
                    for (int x = 0; x < width; x++) {

                        // Obtiene el valor del píxel en la fila Y* del slice actual
                        double val = ip.getPixelValue(bounds.x + x, bounds.y + yStar);

                        // Comprueba si supera el máximo local
                        if (val > maxVal) {
                            // Actualiza el máximo local
                            maxVal = val;
                            // Guarda la posición X del nuevo máximo local
                            xMax = x;
                        }
                    }
                    // Almacena el índice del slice (base 0) para la gráfica
                    zVals[z-1] = z-1;
                    // Almacena la posición X del máximo para la gráfica
                    xVals[z-1] = xMax;
                    // Escribe la fila de datos en el CSV
                    writer.write((z-1) + "," + xMax + "," + maxVal + "\n");
                }

                // Cierra el archivo CSV
                writer.close();
                // Crea la gráfica de posición X vs slice en el plano Y*
                Plot plotY = new Plot("Perfil Y*", "Slice (z)", "X(pixel)", zVals, xVals);
                // Muestra la gráfica en pantalla
                plotY.show();

            } catch (IOException e) {
                // Muestra un error si falla la escritura del archivo
                IJ.error(e.getMessage());
            }
        }

        // =========================
        // Posición de máximos de intensidad en Y para cada Z (se desarrollan los mismos pasos que para el plano Y*, pero ahora para el plano X*)
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

        // Muestra mensaje de confirmación al finalizar el plugin
        IJ.showMessage("Procesamiento completo.");
    }
}
