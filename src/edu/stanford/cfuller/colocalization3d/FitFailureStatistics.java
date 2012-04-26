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

package edu.stanford.cfuller.colocalization3d;

import org.apache.commons.math3.linear.RealVector;

/**
* Keeps track of the reasons for throwing out ImageObjects being fit and provides
* a means to print out a string representation.
* 
* @author Colin J. Fuller
*/
public class FitFailureStatistics {
	
	public final static int R2_FAIL = 0;
	public final static int EDGE_FAIL = 1;
	public final static int SAT_FAIL = 2;
	public final static int SEP_FAIL = 3;
	public final static int ERR_FAIL = 4;
	
	final int n_reasons = 5;
	
	RealVector failCounts;
	
	/**
	* Creates a new FitFailureStatistics object with all counts initialized to zero.
	*/
	public FitFailureStatistics() {
		this.failCounts = new org.apache.commons.math3.linear.ArrayRealVector(n_reasons, 0.0);
	}
	
	/**
	* Increments the failure counter for a specific reason.
	* @param reason an int specifying the reason for the failure.  This should be one of the declared static constants.
	*/
	public void addFailure(int reason) {
		this.failCounts.setEntry(reason, this.failCounts.getEntry(reason)+1);
	}
	
	/**
	* Gets the failure counter for a specific reason.
	* @param reason an int specifying the reason for the failure.  This should be one of the declared static constants.
	* @return the number of failures for the specified reason.
	*/
	public int getFailureCount(int reason) {
		return (int) this.failCounts.getEntry(reason);
	}
	
	/**
	* Gets a formatted string representation of the failures and the reasons.
	* @return a String containing all the failure reasons and counts, one reason/count per line.
	*/
	public String toString() {
		String result = "Objects on which fitting failed due to:\n";
		result += "Edge proximity: " + this.getFailureCount(EDGE_FAIL) + "\n";
		result += "Brightness: " + this.getFailureCount(SAT_FAIL) + "\n";
		result += "R^2 value: " + this.getFailureCount(R2_FAIL) + "\n";
		result += "Fitting error: " + this.getFailureCount(ERR_FAIL) + "\n";
		result += "Channel separation: " + this.getFailureCount(SEP_FAIL) + "\n";
		return result;
	}
 	
}
