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

package edu.stanford.cfuller.colocalization3d.fitting;

import edu.stanford.cfuller.imageanalysistools.fitting.ImageObject;
import edu.stanford.cfuller.imageanalysistools.fitting.NelderMeadMinimizer;
import edu.stanford.cfuller.imageanalysistools.fitting.ObjectiveFunction;
import edu.stanford.cfuller.imageanalysistools.meta.parameters.ParameterDictionary;

import org.apache.commons.math3.exception.ConvergenceException;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import java.util.List;

/**
* A DistributionFitter that fits scalar observations to a P3D probability distibution.
* @author Colin J. Fuller
*/
public class P3DFitter extends DistributionFitter {
	
	/**
	 * Required parameters
	 */
	
	static final String MARKER_CH_PARAM = "marker_channel_index";
	
	static final String CORR_CH_PARAM = "channel_to_correct";
	
	/**
	 * Optional parameters
	 */
	
	static final String ROBUST_P3D_FIT_PARAM = "robust_p3d_fit_cutoff";
	
	public P3DFitter(ParameterDictionary p) {
		super(p);
	}
	
	/**
	 * Fits the distances between the two channels of a set of objects to a p3d distribution.
	 * 
	 * @param objects the ImageObjects whose distances will be fit
	 * @param diffs a RealVector containing the scalar distances between the channels of the ImageObjects, in the same order.
	 * 
	 * @return a RealVector containing the parameters for the distribution fit: first the mean parameter, second the standard deviation parameter
	 */
	public RealVector fit(List<ImageObject> objects, RealVector diffs) {
		
		P3dObjectiveFunction of = new P3dObjectiveFunction();
		
		of.setR(diffs);
		
		final double tol = 1e-12;
		
		NelderMeadMinimizer nmm = new NelderMeadMinimizer(tol);
		
		double initialMean = diffs.getL1Norm()/diffs.getDimension();
		
		double initialWidth = diffs.mapSubtract(initialMean).map(new org.apache.commons.math3.analysis.function.Power(2)).getL1Norm()/diffs.getDimension();
		
		initialWidth = Math.sqrt(initialWidth);
		
		RealVector startingPoint = new ArrayRealVector(2,0.0);
		
		startingPoint.setEntry(0, initialMean);
		startingPoint.setEntry(1, initialWidth);
		
		RealVector parametersMin = null;
		
		if (this.parameters.hasKey(ROBUST_P3D_FIT_PARAM)) {
			
			double cutoff = this.parameters.getDoubleValueForKey(ROBUST_P3D_FIT_PARAM);
			
			of.setMinProb(cutoff);
			
		}			
		
		return nmm.optimize(of, startingPoint);
		
	}
	
	/**
     * Implements the P3D distribution as an ObjectiveFunction suitable for one of the optimizers in
     * {@link edu.stanford.cfuller.imageanalysistools.fitting}.
     */
    private static class P3dObjectiveFunction implements ObjectiveFunction {


        private RealVector r;
        private double s;
        private double minProb;
        private boolean useMinProb;
        private boolean shouldFitS;

        public P3dObjectiveFunction() {
            this.r = null;
            this.shouldFitS = true;
        }

        public void setMinProb(double minProb) {
            this.minProb = -1.0*Math.log(minProb);
            this.useMinProb = true;
        }

        public double evaluate(RealVector point) {

            double m = point.getEntry(0);
            double s = point.getEntry(1);

            if (!this.shouldFitS) {
            	s= this.s;
            }

            if (m < 0 || s < 0) {return Double.MAX_VALUE;}

            RealVector negLogPVector = new ArrayRealVector(r.getDimension(), 0.0);

            for (int i = 0; i < r.getDimension(); i++) {
                double tempNegLogP = -1.0*Math.log(p3d(r.getEntry(i), m, s));
                if (this.useMinProb && tempNegLogP > this.minProb) { tempNegLogP = this.minProb;}
                negLogPVector.setEntry(i,tempNegLogP);
            }

            //trimmed mean:

            double trimPercentage = 0.0;

            double[] sortedNegLogP = negLogPVector.toArray();

            java.util.Arrays.sort(sortedNegLogP);

            double negLogL = 0;

            int trimmedLength = (int) Math.floor((1-trimPercentage)*sortedNegLogP.length);

            for (int i =0; i < trimmedLength; i++) {

                negLogL += sortedNegLogP[i];

            }

            return negLogL;

        }

        private double p3d(double r, double m, double s) {

            return ( (Math.exp(-1.0*Math.pow(m-r, 2)/(2*s*s)) - Math.exp(-1.0*Math.pow(m+r, 2)/(2*s*s)))*(Math.sqrt(2.0/Math.PI)*r/(2*m*s)));

        }

        public void setR(RealVector r) {
            this.r = r;
        }

        public void setS(double s) {
            this.s = s;
            this.shouldFitS = false;
        }

    }

 }