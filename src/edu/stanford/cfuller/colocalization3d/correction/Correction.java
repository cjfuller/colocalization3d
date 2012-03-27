/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (c) 2012 Colin J. Fuller
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the Software), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED AS IS, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 * ***** END LICENSE BLOCK ***** */

package edu.stanford.cfuller.colocalization3d.correction;

import org.apache.commons.math.analysis.function.Power;
import org.apache.commons.math.analysis.function.Sqrt;
import org.apache.commons.math.linear.ArrayRealVector;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.RealVector;

import edu.stanford.cfuller.imageanalysistools.fitting.ImageObject;

import java.io.*;
import java.util.List;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

public class Correction implements java.io.Serializable {
	
	private RealMatrix correctionX;
    private RealMatrix correctionY;
    private RealMatrix correctionZ;
    private RealMatrix positionsForCorrection;
    private RealVector distanceCutoffs;
    private int referenceChannel;
    private int correctionChannel;
    
    protected static final String CORRECTION_ELEMENT = "correction";
    protected static final String CORRECTION_POINT_ELEMENT = "point";
    protected static final String N_POINTS_ATTR = "n";
    protected static final String REF_CHANNEL_ATTR = "reference_channel";
    protected static final String CORR_CHANNEL_ATTR = "correction_channel";
    protected static final String X_POS_ATTR = "x_position";
    protected static final String Y_POS_ATTR = "y_position";
    protected static final String Z_POS_ATTR = "z_position";
    protected static final String LOG_NAME = "edu.stanford.cfuller.colocalization3d";
    protected static final String X_PARAM_ELEMENT = "x_dimension_parameters";
    protected static final String Y_PARAM_ELEMENT = "y_dimension_parameters";
    protected static final String Z_PARAM_ELEMENT = "z_dimension_parameters";
    protected static final String BINARY_DATA_ELEMENT = "serialized_form";
    protected static final String ENCODING_ATTR = "encoding";
    protected static final String ENCODING_NAME = "hexBinary";
    
    protected static final int numberOfCorrectionParameters = 6;
    
	private static final long serialVersionUID = 3L;
    
    private double tre;


    /**
     * Construct a new Correction from a set of locations and corrections at those points.
     *
     * For each of the matrix parameters, the row index is over objects.
     *
     * @param cX                a RealMatrix containing the parameters describing the interpolating function centered at each object used for correction in the x dimension.
     * @param cY                a RealMatrix containing the parameters describing the interpolating function centered at each object used for correction in the y dimension.
     * @param cZ                a RealMatrix containing the parameters describing the interpolating function centered at each object used for correction in the z dimension.
     * @param distanceCutoffs   a RealVector containing the distance to the farthest point used to generate each interpolating function.
     * @param imageObjects		the ImageObjects used for correction.
     * @param referenceChannel	the referenceChannel relative to which the other channel was corrected.
     * @param correctionChannel the channel being corrected.
     */
    public Correction(RealMatrix cX, RealMatrix cY, RealMatrix cZ, RealVector distanceCutoffs, List<ImageObject> imageObjects, int referenceChannel, int correctionChannel) {

        this.correctionX = cX;
        this.correctionY = cY;
        this.correctionZ = cZ;
        
        this.referenceChannel = referenceChannel;
        this.correctionChannel = correctionChannel;

        this.positionsForCorrection = new Array2DRowRealMatrix(imageObjects.size(), 3);
        
        for (int i =0; i < this.positionsForCorrection.getRowDimension(); i++) {
 
        	this.positionsForCorrection.setRowVector(i, imageObjects.get(i).getPositionForChannel(referenceChannel));
        	
        }
        
        this.distanceCutoffs = distanceCutoffs;
    }


    /**
     * Gets the RealMatrix of parameters describing the correction in the X-dimension.
     *
     * @return      a RealMatrix containing the parameters describing the interpolating function centered at each object used for correction in the x dimension.
     */
    public RealMatrix getCorrectionX() {
        return correctionX;
    }

    /**
     * Gets the RealMatrix of parameters describing the correction in the Y-dimension.
     *
     * @return      a RealMatrix containing the parameters describing the interpolating function centered at each object used for correction in the y dimension.
     */
    public RealMatrix getCorrectionY() {
        return correctionY;
    }

    /**
     * Gets the RealMatrix of parameters describing the correction in the Z-dimension.
     *
     * @return      a RealMatrix containing the parameters describing the interpolating function centered at each object used for correction in the z dimension.
     */
    public RealMatrix getCorrectionZ() {
        return correctionZ;
    }

    /**
     * Gets the RealMatrix of positions used to construct the correction.
     *
     * @return      a RealMatrix containing the positions of the point used to generate each interpolating function.
     */
    public RealMatrix getPositionsForCorrection() {
        return positionsForCorrection;
    }

    /**
     * Gets the maximal distances for each interpolating function used to construct the correction.
     * 
     * @return      a RealVector containing the distance to the farthest point used to generate each interpolating function.
     */
    public RealVector getDistanceCutoffs() {
        return distanceCutoffs;
    }

    /**
     * Gets the index of the reference channel relative to which the correction was calculated.
     * @return	The index of the reference channel.
     */
    public int getReferenceChannelIndex() {
    	return this.referenceChannel;
    }
    
    /**
     * Gets the index of the channel that was being corrected.
     * @return	The index of the channel being corrected.
     */
    public int getCorrectionChannelIndex() {
    	return this.correctionChannel;
    }
    
    /**
     * Writes the Correction to disk in binary format so it can be stored and retrieved later.
     *
     * @param filename              The filename to which to write the Correction.
     * @throws java.io.IOException  if the Correction cannot be written to disk.
     */
    public void writeToDisk(String filename) throws java.io.IOException {

        File f = new File(filename);

        FileOutputStream fo = new FileOutputStream(f);

        PrintWriter p = new PrintWriter(fo);
        
        p.write(this.writeToXML());
        
        p.close();
        
    }
    
    
    protected String writeToXML() {
    	
    	StringWriter sw = new StringWriter();
    	
    	try {
    	
	    	XMLStreamWriter xsw = XMLOutputFactory.newFactory().createXMLStreamWriter(sw);
	    	
	    	xsw.writeStartDocument();
	    	xsw.writeCharacters("\n");
	    	xsw.writeStartElement("root");
	    	xsw.writeCharacters("\n");
	    	xsw.writeStartElement(CORRECTION_ELEMENT);
	    	xsw.writeAttribute(N_POINTS_ATTR, Integer.toString(this.distanceCutoffs.getDimension()));
	    	xsw.writeAttribute(REF_CHANNEL_ATTR, Integer.toString(this.referenceChannel));
	    	xsw.writeAttribute(CORR_CHANNEL_ATTR, Integer.toString(this.correctionChannel));
	    	
	    	xsw.writeCharacters("\n");
	    	
	    	for (int i = 0; i < this.distanceCutoffs.getDimension(); i++) {
	    		xsw.writeStartElement(CORRECTION_POINT_ELEMENT);
	    		
	    		xsw.writeAttribute(X_POS_ATTR, Double.toString(this.positionsForCorrection.getEntry(i,0)));
	    		xsw.writeAttribute(Y_POS_ATTR, Double.toString(this.positionsForCorrection.getEntry(i,1)));
	    		xsw.writeAttribute(Z_POS_ATTR, Double.toString(this.positionsForCorrection.getEntry(i,2)));
	
	    		xsw.writeCharacters("\n");
	    		
	    		
	    		String x_param_string = "";
	    		String y_param_string = "";
	    		String z_param_string = "";
	    		
	    		for (int j = 0; j < this.correctionX.getColumnDimension(); j++) {
	    			String commaString = "";
	    			if (j != 0) commaString = ", ";
	    			x_param_string += commaString + this.correctionX.getEntry(i,j);
	    			y_param_string += commaString + this.correctionY.getEntry(i,j);
	    			z_param_string += commaString + this.correctionZ.getEntry(i,j);
	    		}
	    		
	    		x_param_string = x_param_string.trim() + "\n";
	    		y_param_string = y_param_string.trim() + "\n";
	    		z_param_string = z_param_string.trim() + "\n";
	    		
	    		xsw.writeStartElement(X_PARAM_ELEMENT);
	    		
	    		xsw.writeCharacters(x_param_string);
	    			
	    		xsw.writeEndElement();
	    		
	    		xsw.writeCharacters("\n");
	    		
	    		xsw.writeStartElement(Y_PARAM_ELEMENT);
	    		
	    		xsw.writeCharacters(y_param_string);
	    		
	    		xsw.writeEndElement();
	    		
	    		xsw.writeCharacters("\n");
	    		
	    		xsw.writeStartElement(Z_PARAM_ELEMENT);
	    		
	    		xsw.writeCharacters(z_param_string);
	    		
	    		xsw.writeEndElement();
	    		
	    		xsw.writeCharacters("\n");

	    		xsw.writeEndElement();
	    		
	    		xsw.writeCharacters("\n");	
	    			
	    	}
	    	
	    	xsw.writeStartElement(BINARY_DATA_ELEMENT);
	    		    	
	    	xsw.writeAttribute(ENCODING_ATTR, ENCODING_NAME);
	    	
	    	ByteArrayOutputStream bytesOutput = new ByteArrayOutputStream();
	    	
	    	try {
	    		
	    		ObjectOutputStream oos = new ObjectOutputStream(bytesOutput);
	    	
	    		oos.writeObject(this);
	    		
	    	} catch (java.io.IOException e) {
	    		java.util.logging.Logger.getLogger(LOG_NAME).severe("Exception encountered while serializing correction: " + e.getMessage());

	    	}
	    	
    		HexBinaryAdapter adapter = new HexBinaryAdapter();
    		xsw.writeCharacters(adapter.marshal(bytesOutput.toByteArray()));
    	
	    	xsw.writeEndElement();
	    	
	    	xsw.writeCharacters("\n");
	    	
	    	xsw.writeEndElement();
	    	
	    	xsw.writeCharacters("\n");
	    	
	    	xsw.writeEndElement();
	    	
	    	xsw.writeCharacters("\n");
	    	
	    	xsw.writeEndDocument();
	    	
    	} catch (XMLStreamException e) {
    		
    		java.util.logging.Logger.getLogger(LOG_NAME).severe("Exception encountered while writing XML correction output: " + e.getMessage());
    		
    	}
    	
    	return sw.toString();
    	
    }

    /**
     * Reads a stored correction from disk.
     * 
     * @param filename                  The name of the file containing the Correction that was previously written to disk.
     * @return                          The Correction contained in the file.
     * @throws java.io.IOException      if the Correction cannot be successfully read.
     * @throws ClassNotFoundException   if the file does not contain a Correction.
     */
    public static Correction readFromDisk(String filename) throws java.io.IOException, ClassNotFoundException {


        File f = new File(filename);

        
        FileReader fr = new FileReader(f);
        
        XMLStreamReader xsr = null;
        String encBinData = null;

        try {
        	xsr = XMLInputFactory.newFactory().createXMLStreamReader(fr);
        
    
        
	        while(xsr.hasNext()) {
	        	
	        	int event = xsr.next();
	        	
	        	if (event != XMLStreamReader.START_ELEMENT) continue; 
	        	
	        	if (xsr.hasName() && xsr.getLocalName() == BINARY_DATA_ELEMENT) {
	        		
	        		encBinData = xsr.getElementText();
	        		
	        		break;
	        	
	        	}
	        	
	        }
        } catch (XMLStreamException e) {
        	java.util.logging.Logger.getLogger(LOG_NAME).severe("Exception encountered while reading XML correction: " + e.getMessage());        
    	}
        byte[] binData = (new HexBinaryAdapter()).unmarshal(encBinData);
        
        ObjectInputStream oi = new ObjectInputStream(new ByteArrayInputStream(binData));

        Object o = oi.readObject();

        return (Correction) o;

        
    }
 
    

    /**
     * Gets the target registration error associated with the correction.
     *
     * This must be externally calculated and stored using {@link #setTre(double)}.
     *
     * @return      the target registration error.
     */
    public double getTre() {
        return tre;
    }

    /**
     * Sets the target registration error associated with the correction to the specified value.
     * @param tre   the target registration error.
     */
    public void setTre(double tre) {
        this.tre = tre;
    }
    
    /**
     * Applies an existing correction to a single x-y position in the Image plane.
     *
     * @param x     The x-position at which to apply the correction.
     * @param y     The y-position at which to apply the correction.
     * @return      A RealVector containing 3 elements-- the magnitude of the correction in the x, y, and z dimensions, in that order.
     */
    public RealVector correctPosition(double x, double y) throws UnableToCorrectException {
        
        RealVector corrections = new ArrayRealVector(3, 0.0);

        RealVector distsToCentroids = this.getPositionsForCorrection().getColumnVector(0).mapSubtract(x).mapToSelf(new Power(2));
        distsToCentroids = distsToCentroids.add(this.getPositionsForCorrection().getColumnVector(1).mapSubtract(y).mapToSelf(new Power(2)));
        distsToCentroids.mapToSelf(new Sqrt());

        RealVector distRatio = distsToCentroids.ebeDivide(this.getDistanceCutoffs());

        RealVector distRatioBin = new ArrayRealVector(distRatio.getDimension(), 0.0);

        for (int i =0; i < distRatio.getDimension(); i++) {
            if (distRatio.getEntry(i) <= 1) distRatioBin.setEntry(i, 1.0);
        }

        RealVector weights = distRatio.map(new Power(2.0)).mapMultiplyToSelf(-3).mapAddToSelf(1).add(distRatio.map(new Power(3.0)).mapMultiplyToSelf(2));

        weights = weights.ebeMultiply(distRatioBin);

        double sumWeights = 0;

        int countWeights = 0;

        for (int i =0; i < weights.getDimension(); i++) {
            if (weights.getEntry(i) > 0) {
                sumWeights += weights.getEntry(i);
                countWeights++;
            }
        }
        
        if (countWeights == 0) { // this means there were no points in the correction dataset near the position being corrected.
        	throw (new UnableToCorrectException("Incomplete coverage in correction dataset at (x,y) = (" + x + ", " + y + ")."));
        }

        RealMatrix cX = new Array2DRowRealMatrix(countWeights, this.getCorrectionX().getColumnDimension());
        RealMatrix cY = new Array2DRowRealMatrix(countWeights, this.getCorrectionX().getColumnDimension());
        RealMatrix cZ = new Array2DRowRealMatrix(countWeights, this.getCorrectionX().getColumnDimension());

        RealVector xVec = new ArrayRealVector(countWeights, 0.0);
        RealVector yVec = new ArrayRealVector(countWeights, 0.0);

        RealVector keptWeights = new ArrayRealVector(countWeights, 0.0);

        int keptCounter = 0;

        for (int i =0; i < weights.getDimension(); i++) {
            if (weights.getEntry(i) > 0) {

                cX.setRowVector(keptCounter, this.getCorrectionX().getRowVector(i));
                cY.setRowVector(keptCounter, this.getCorrectionY().getRowVector(i));
                cZ.setRowVector(keptCounter, this.getCorrectionZ().getRowVector(i));

                xVec.setEntry(keptCounter, x - this.getPositionsForCorrection().getEntry(i, 0));
                yVec.setEntry(keptCounter, y - this.getPositionsForCorrection().getEntry(i, 1));

                keptWeights.setEntry(keptCounter, weights.getEntry(i));

                keptCounter++;
            }
        }


        double xCorr = 0;
        double yCorr = 0;
        double zCorr = 0;


        RealMatrix allCorrectionParameters = new Array2DRowRealMatrix(countWeights, numberOfCorrectionParameters);

        RealVector ones = new ArrayRealVector(countWeights, 1.0);

        allCorrectionParameters.setColumnVector(0, ones);
        allCorrectionParameters.setColumnVector(1, xVec);
        allCorrectionParameters.setColumnVector(2, yVec);
        allCorrectionParameters.setColumnVector(3, xVec.map(new Power(2)));
        allCorrectionParameters.setColumnVector(4, yVec.map(new Power(2)));
        allCorrectionParameters.setColumnVector(5, xVec.ebeMultiply(yVec));


        for (int i =0; i < countWeights; i++) {

            xCorr += allCorrectionParameters.getRowVector(i).dotProduct(cX.getRowVector(i))*keptWeights.getEntry(i);
            yCorr += allCorrectionParameters.getRowVector(i).dotProduct(cY.getRowVector(i))*keptWeights.getEntry(i);
            zCorr += allCorrectionParameters.getRowVector(i).dotProduct(cZ.getRowVector(i))*keptWeights.getEntry(i);

        }


        xCorr/= sumWeights;
        yCorr/= sumWeights;
        zCorr/= sumWeights;

        corrections.setEntry(0, xCorr);
        corrections.setEntry(1, yCorr);
        corrections.setEntry(2, zCorr);

        return corrections;
    }
	
	
}