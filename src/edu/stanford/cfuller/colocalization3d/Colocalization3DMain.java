/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (c) 2012 Colin J. Fuller
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 * ***** END LICENSE BLOCK ***** */
package edu.stanford.cfuller.colocalization3d;

import java.io.File;
import java.util.List;

import edu.stanford.cfuller.colocalization3d.correction.Correction;
import edu.stanford.cfuller.colocalization3d.correction.PositionCorrector;
import edu.stanford.cfuller.colocalization3d.fitting.DistributionFitter;
import edu.stanford.cfuller.colocalization3d.fitting.P3DFitter;
import edu.stanford.cfuller.imageanalysistools.fitting.ImageObject;
import edu.stanford.cfuller.imageanalysistools.image.Histogram;
import edu.stanford.cfuller.imageanalysistools.image.Image;
import edu.stanford.cfuller.imageanalysistools.image.ReadOnlyImage;
import edu.stanford.cfuller.imageanalysistools.parameters.ParameterDictionary;

public class Colocalization3DMain {
	
	static final int DEFAULT_MAX_THREADS = 4;
	static final int DEFAULT_THREAD_WAIT_MS = 50;
	
	/*
	Required parameters:
	*/
	
	static final String DIRNAME_PARAM = "dirname_set";
	static final String BASENAME_PARAM = "basefilename_set";
	
	/*
	Optional parameters:
	*/
	
	static final String PRECOMPUTED_POS_PARAM = "precomputed_position_data";
	static final String THREAD_COUNT_PARAM = "max_threads";
	
	//TODO: organize parameters
		
	protected ParameterDictionary parameters;
	
	private FitFailureStatistics failures;
	
	public Colocalization3DMain() {
		this.failures = new FitFailureStatistics();
	}
	
	/**
	 * Loads the position data from disk if this is requested in the specified parameters.  If this
	 * has not been requested, or if the position data file does not exist returns null.
	 * 
	 * @return A List<ImageObject> containing the image objects and their positions, or null if 
	 * this should be recalculated or if the file cannot be found.
	 */
	protected List<ImageObject> loadExistingPositionData() {
		
		/*
			TODO implementation 
		*/
		
		if (this.parameters.hasKeyAndTrue(PRECOMPUTED_POS_PARAM) && (new File(FileUtils.getPositionDataFilename(this.parameters))).exists()) {
			
			//load it
		} else {
			return null;
		}
		
		return null;
		
	}
	
	/**
	 * Loads an Image specified by an ImageAndMaskSet and corrects it with a dark current image
	 * specified in the parameters.
	 * 
	 * @param toLoad an {@link ImageAndMaskSet} specifying the image that will be loaded.
	 * @return an {@link Image} read from the specfied location.
	 */
	protected Image loadAndCorrectImageFromSet(ImageAndMaskSet toLoad) {
		/*
			TODO implementation
		*/
		
		return null;
	}
	
	/**
	 * Loads a mask specified by an ImageAndMaskSet.
	 * 
	 * @param toLoad an {@link ImageAndMaskSet } specifying the mask that will be loaded.
	 * @return an {@link Image } read from the specified location.
	 */
	protected Image loadMaskFromSet(ImageAndMaskSet toLoad) {
		
		/*
			TODO implementation
		*/
		
		return null;
	}
	
	private void checkAllRunningThreadsAndRemoveFinished(List<FittingThread> started, List<FittingThread> finished) {
		/*
			TODO implementation
		*/
	}
	
	/**
	 * Fits all the image objects in a single image specified by a supplied mask and image set.
	 * <p>
	 * Does not check for whether the fitting was successful.
	 * 
	 * @param iams a {@link ImageAndMaskSet } specifying the image whose objects will be fit.
	 * @return a List<ImageObject> containing an ImageObject for each object in the image that has been fit.
	 */
	protected List<ImageObject> fitObjectsInSingleImage(ImageAndMaskSet iams) {
		
		Image im = this.loadAndCorrectImageFromSet(iams);
		
		Image mask = this.loadMaskFromSet(iams);
		
		Histogram h = new Histogram(mask);
		
		int maxRegionId = h.getMaxValue();
		
		List<FittingThread> startedThreads = new java.util.ArrayList<FittingThread>();
		List<FittingThread> finishedThreads = new java.util.ArrayList<FittingThread>();
		
		int maxThreadCount = Colocalization3DMain.DEFAULT_MAX_THREADS;
		
		if (this.parameters.hasKey(THREAD_COUNT_PARAM)) {
			maxThreadCount = this.parameters.getIntValueForKey(THREAD_COUNT_PARAM);
		}
		
		for (int i = 1; i < maxRegionId; i++) {
		
			ImageObject obj = new edu.stanford.cfuller.imageanalysistools.fitting.GaussianImageObjectWithCovariance(i, new ReadOnlyImage(im), new ReadOnlyImage(mask), this.parameters);
		
			obj.setImageID(iams.getImageFilename());
		
			FittingThread nextThread = new FittingThread(); //TODO: change constructor
			
			try {
				while(startedThreads.size() >= maxThreadCount) {
					Thread.sleep(Colocalization3DMain.DEFAULT_THREAD_WAIT_MS);
					checkAllRunningThreadsAndRemoveFinished(startedThreads, finishedThreads);
				}
			} catch (InterruptedException e) {
				/*
					TODO log something
				*/
			}
			
			nextThread.start();
			
			startedThreads.add(nextThread);
			
		}
		
		while(startedThreads.size() > 0) {
			try {
				Thread.sleep(Colocalization3DMain.DEFAULT_THREAD_WAIT_MS);
			} catch (InterruptedException e) {
				/*
					TODO log something
				*/
			}
			checkAllRunningThreadsAndRemoveFinished(startedThreads, finishedThreads);
		}
		
		List<ImageObject> output = new java.util.ArrayList<ImageObject>();
		
		for (FittingThread ft : finishedThreads) {
			output.add(ft.getFitObject());
		}
		
		return output;
				
	}
	
	protected boolean fitParametersOk(ImageObject toCheck) {
		/*
			TODO implementation
		*/
		
		return false;
	}
		
	public void go(Initializer in) {
		//initialize parameters
		
		this.parameters = in.initializeParameters();
		
		//load precomputed position data if needed
		
		List<ImageObject> imageObjects = this.loadExistingPositionData();
		
		//otherwise, do the fitting:
		
		if (imageObjects == null) {
			
			imageObjects = new java.util.ArrayList<ImageObject>();
					
			List<ImageAndMaskSet> allFilesToProcess = FileUtils.listFilesToProcess(this.parameters);
			
			for (ImageAndMaskSet iams : allFilesToProcess) {
				
				List<ImageObject> fittedObjects = this.fitObjectsInSingleImage(iams);
				
				for (ImageObject iobj : fittedObjects) {
					if (this.fitParametersOk(iobj)) {
						imageObjects.add(iobj);
					}
				}
				
			}
			
		}
		
		//write the objects and their positions to disk
		
		FileUtils.writeFittedImageObjectsToDisk(imageObjects, this.parameters);
		
		//get a correction, either by making one or reading from disk
		
		PositionCorrector pc = new PositionCorrector(this.parameters);
		
		Correction c = pc.getCorrection(imageObjects);
		
		//apply the correction, removing objects that cannot be corrected
		
		pc.applyCorrection(c, imageObjects);
						
		//fit the distribution of separations
		
		DistributionFitter df = new P3DFitter(this.parameters);
		
		df.fit(imageObjects);
		
		//output plots and information
		
	}

	public static void main(String[] args) {
		
		System.out.println("Hello, world!");
		
		Initializer in = new Initializer();
		
		in.initializeParameters(args);
		
		(new Colocalization3DMain()).go(in);
						
	}
	
	protected static class FittingThread extends Thread {
		/*
			TODO implementation
		*/
		
		public ImageObject getFitObject() {
			/*
				TODO implementation
			*/
			return null;
			
		}
		
	}
	
}

