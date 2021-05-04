/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tarea2;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
/**
 *
 * @author emanuel
 */
public class ProcesadorC {

    /** Imagen actual que se ha cargado. */
    public BufferedImage imageActual;
    
    // Archivo que mantiene la imagen seleccionada.
    private File imagenSeleccionada;

    // Matriz para aplicar blur.
    public int[][]blur = {{0,0,1,0,0},
			      {0,1,1,1,0},
			      {1,1,1,1,1},
			      {0,1,1,1,0},
                              {0,0,1,0,0}};
    
    // Matriz para aplicar motion blur.
    public int[][]motionBlur = {{1,0,0,0,0},
				    {0,1,0,0,0},
				    {0,0,1,0,0},
				    {0,0,0,1,0},
				    {0,0,0,0,1}};
    
    // Matriz para aplicar FindEdges
    public int[][]edges = {{-1,-1,-1,-1,-1},
                               {-1,-1,-1,-1,-1},
                               {-1,-1,24,-1,-1},
                               {-1,-1,-1,-1,-1},
                               {-1,-1,-1,-1,-1}};
    
    // Matriz para aplicar Sharpen
    public int[][]sharpen = {{-1,-1,-1,-1,-1},
                                 {-1,-1,-1,-1,-1},
                                 {-1,-1,25,-1,-1},
                                 {-1,-1,-1,-1,-1},
                                 {-1,-1,-1,-1,-1}};
    
    // Matriz para aplicar emboss.
    public int[][]emboss1 = {{-1,-1,-1,-1, 0},
                                 {-1,-1,-1, 0, 1},
                                 {-1,-1, 0, 1, 1},
                                 {-1, 0, 1, 1, 1},
                                 { 0, 1, 1, 1, 1}};
    
    //Método que devuelve una imagen abierta desde archivo
    //Retorna un objeto BufferedImagen
    public BufferedImage abrirImagen(){
        //Creamos la variable que será devuelta (la creamos como null)
        BufferedImage bmp=null;
        //Creamos un nuevo cuadro de diálogo para seleccionar imagen
        JFileChooser selector=new JFileChooser();
        //Le damos un título
        selector.setDialogTitle("Seleccione una imagen");
        //Filtramos los tipos de archivos
        FileNameExtensionFilter filtroImagen = new FileNameExtensionFilter("JPG & GIF & BMP", "jpg", "gif", "bmp");
        selector.setFileFilter(filtroImagen);
        //Abrimos el cuadro de diálog
        int flag=selector.showOpenDialog(null);
        //Comprobamos que pulse en aceptar
        if(flag==JFileChooser.APPROVE_OPTION){
            try {
                //Devuelve el fichero seleccionado
                imagenSeleccionada=selector.getSelectedFile();
                //Asignamos a la variable bmp la imagen leida
                bmp = ImageIO.read(imagenSeleccionada);
            } catch (IOException e) {}
        }
        //Asignamos la imagen cargada a la propiedad imageActual
        imageActual=bmp;
        //Retornamos el valor
        return bmp;
    }
    
    /**
     * Verifica si ya se cargó una imagen al procesador de imágenes.
     * @return true si hay imagen cargada, false en otro caso.
     */
    public boolean tieneImagen(){
        return !(imageActual==null);
    }
    
    /**
     * Método que regresa la imagen actual a su estado orginal.
     */
    public void returnOriginal(){
        if (imageActual!=null) {
            try {
                imageActual=ImageIO.read(imagenSeleccionada);
            } catch (IOException ex) {
                Logger.getLogger(ProcesadorC.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /**
     * Método que aplica un filtro de convolución dada una matriz.
     * @param m matriz con la que se aplican los filtros.
     * @param factor factor de contribución en la suma de pixeles.
     * @param bias constante para sumar a los colores RGB.
     * @return una imagen con el filtro aplicado, respecto a la matriz.
     */
    public BufferedImage convolution(int[][] m, double factor,double bias) {
        returnOriginal();
        // Tamaño del arreglo de vecindad y su radio.
        int tam = m.length, rad = tam/2;
        // Auxuliares para los colores RGB.
        double R = 0, G = 0, B = 0;
        // Los colores en los pixeles se mantienen en un arreglo de colores para no perderlos.
        Color[][] neighbours = new Color[imageActual.getWidth()][imageActual.getHeight()];
        for (int x = 0; x < imageActual.getWidth(); x++) {
            for (int y = 0; y < imageActual.getHeight(); y++)
                neighbours[x][y] = new Color(imageActual.getRGB(x, y));
        }
        // Se calculan los colores nuevos a partir de los vecinos.
        for (int x = 0; x < imageActual.getWidth(); x++) {
            int xi=(x<rad)?rad-x:0, 
		xf=((imageActual.getWidth()-x)<=rad)?rad+imageActual.getWidth()-x:tam;
            for(int y=0;y< imageActual.getHeight();y++){
                // Inicializamos nuestros colores RGB
                R=0;
                G=0;
                B=0;
                int yi=(y<rad)?rad-y:0,
                        yf=((imageActual.getHeight()-y) <=rad)?rad+imageActual.getHeight()-y:tam;
                for (int i=xi,px=x-rad;i<xf;i++) {
                    for (int j = yi, py = y - rad; j < yf; j++) {
                        double val = m[i][j];
                        R+=(neighbours[px+i][py+j].getRed() * val);
                        G+=(neighbours[px+i][py+j].getGreen() * val);
                        B+=(neighbours[px+i][py+j].getBlue() * val);
                    }
                }
                R = R * factor + bias;
                G = G * factor + bias;
                B = B * factor + bias;
                imageActual.setRGB(x, y, new Color(normalize((int)R), normalize((int)G), normalize((int)B)).getRGB());
            }
        }
        return imageActual;
    }
    
    /**
     * Filtro Blur con una matriz de 5x5.
     * @return Devuelve una imagen con el filtro Blur aplicado.
     */
    public BufferedImage blur() {
        return convolution(blur, 1.0 / 13.0, 0);
    }
    
    /**
     * Filtro MotionBlur con una matriz de 5x5.
     * @return Devuelve una imagen con el filtro MotionBlur aplicado.
     */
    public BufferedImage motionBlur() {
        return convolution(motionBlur, 1.0 / 9.0 , 0);
    }
    
    /**
     * Filtro que encuentra los bordes con una matriz de 5x5.
     * @return Devuelve una imagen con los bordes filtrados.
     */
    public BufferedImage findEdges() {
        return convolution(edges, 1, 0);
    }
    
    /**
     * Filtro de bordes sobre una imagen con una matriz de 5x5.
     * @return Devuelve una imagen con los bordes remarcados.
     */
    public BufferedImage sharpen() {
        return convolution(sharpen, 1, 0);
    }

    /**
     * Filtro Emboss con una matriz de 5x5.
     * @param mode Es el tipo de Emboss que se aplica, <0 es horizonatl, 0 es a 45 grados y >0 es vertical.
     * @return Devuelve una imagen con el filtro aplicado.
     */
    public BufferedImage emboss(int mode) {
            return convolution(emboss1, 1, 128);
    }
    
    /**
     * Filtro de convolucion mediana de imagen con una matriz de 5x5.
     * @return Devuelve una imagen con el filtro aplicado.
     */
    public BufferedImage mediana() {
        returnOriginal();        
        //Se definen las variables auxiliares.
        int rad = 3/2, r[], g[], b[];
        // Se guardan los colores originales de los pixeles.
        Color[][] original = new Color[imageActual.getWidth()][imageActual.getHeight()];
        for (int x = 0; x < imageActual.getWidth(); x++) {
            for (int y = 0; y < imageActual.getHeight(); y++) original[x][y] = new Color(imageActual.getRGB(x, y));
        } // Se recorre la matriz para calcular los nuevos colores.
        for (int x = 0; x < imageActual.getWidth(); x++) {
            // Se definen los limites horizontales de la matriz segun la posicion del pixel actual.
            int xi = (x < rad)? rad - x : 0, xf = ((imageActual.getWidth() - x) <= rad)? rad + imageActual.getWidth() - x : 3;
            for (int y = 0; y < imageActual.getHeight(); y++){
                // Se definen los limites verticales de la matriz segun la posicion del pixel actual.
                int yi = (y < rad)? rad - y : 0, yf = ((imageActual.getHeight() - y) <= rad)? rad + imageActual.getHeight() - y : 3;
                // Se definen los arreglos auxiliares de color para calcular la mediana de cada componente.
                r = new int[(xf - xi) * (yf - yi)]; g = new int[r.length]; b = new int[r.length];
                for (int i = 0, px = x - rad; (i + xi) < xf; i++) {
                    for (int j = 0, py = y - rad; (j + yi) < yf; j++) {
                        r[j + (yf - yi) * i] = original[px + i + xi][py + j + yi].getRed();
                        g[j + (yf - yi) * i] = original[px + i + xi][py + j + yi].getGreen();
                        b[j + (yf - yi) * i] = original[px + i + xi][py + j + yi].getBlue();
                    }
                }
                imageActual.setRGB(x, y, new Color(mediana(r), mediana(g), mediana(b)).getRGB());
            }
        } return imageActual;
    }
    
    private static int mediana(int ... x) {
        int xn = x.length, m = -1;
        for (int i = 0, j = 0; i <= (xn / 2); i++, j = i) {
            for (int k = i + 1; k < xn; k++) { if(x[j] > x[k]) j = k; }
            m = x[j]; x[j] = x[i]; x[i] = m;
        } if((xn % 2) == 0) return (m + x[xn / 2 - 1]) / 2;
        return m;
    }
    
    private int normalize(int n) { 
	return (n < 0)? 0 : ((n < 256)? n : 255);
    }
}
