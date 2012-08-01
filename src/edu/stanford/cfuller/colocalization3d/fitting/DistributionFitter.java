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
import edu.stanford.cfuller.imageanalysistools.meta.parameters.ParameterDictionary;

import org.apache.commons.math3.linear.RealVector;

/**
* Fits a set of scalar observations to some probability distribution.  
* Subclasses handle particular distributions.
* @author Colin J. Fuller
*/
public abstract class DistributionFitter {
	
	ParameterDictionary parameters;
	
	/**
	* Constructs a new DistributionFitter
	* @param p a ParameterDictionary containing any parameters required for the fitting (specific parameters are specified by subclasses).
	*/
	public DistributionFitter(ParameterDictionary p) {
		this.parameters = p;
	}
	
	/**
	 * Fits the distances between the two channels of a set of objects to a distribution.
	 * 
	 * @param objects the ImageObjects whose distances will be fit
	 * @param diffs a RealVector containing the scalar distances between the channels of the ImageObjects, in the same order.
	 * 
	 * @return a RealVector containing the parameters for the distribution fit; the order and number will depend on the distribution being fit.
	 */
	public abstract RealVector fit(java.util.List<ImageObject> objects, RealVector diffs);
	
}
