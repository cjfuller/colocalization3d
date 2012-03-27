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
import edu.stanford.cfuller.imageanalysistools.filter.Filter;
import edu.stanford.cfuller.imageanalysistools.filter.ImageSubtractionFilter;
import edu.stanford.cfuller.imageanalysistools.fitting.FitParameters;
import edu.stanford.cfuller.imageanalysistools.fitting.ImageObject;
import edu.stanford.cfuller.imageanalysistools.image.Histogram;
import edu.stanford.cfuller.imageanalysistools.image.Image;
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate;
import edu.stanford.cfuller.imageanalysistools.image.ReadOnlyImage;
import edu.stanford.cfuller.imageanalysistools.parameters.ParameterDictionary;

public class Colocalization3DMain {
	
	public static final String LOGGER_NAME= "edu.stanford.cfuller.colocalization3d";
	
	static final int DEFAULT_MAX_THREADS = 4;
	static final int DEFAULT_THREAD_WAIT_MS = 50;
	
	/*
	Required parameters:
	*/
	
	static final String DIRNAME_PARAM = "dirname_set";
	static final String BASENAME_PARAM = "basefilename_set";
	static final String BORDER_PARAM = "im_border_size";
	static final String Z_BOX_SIZE_PARAM = "half_z_size";
	static final String DETERMINE_CORRECTION_PARAM = "determine_correction";
	static final String PIXELSIZE_PARAM = "pixelsize_nm";
	static final String SECTIONSIZE_PARAM = "z_sectionsize_nm";
	
	/*
	Optional parameters:
	*/
	
	static final String PRECOMPUTED_POS_PARAM = "precomputed_position_data";
	static final String THREAD_COUNT_PARAM = "max_threads";
	static final String DARK_IMAGE_PARAM = "darkfield_image"; //TODO: change this parameter's name
	static final String R2_PARAM = "residual_cutoff";
	static final String MAX_LEVEL_PARAM = "max_greylevel_cutoff";
	static final String DIST_CUTOFF_PARAM = "distance_cutoff";
	static final String ERROR_CUTOFF_PARAM = "fit_error_cutoff";
	
	/**
	 * Automatically filled parameters:
	 */
	
	static final String CAMERA_SIZE_PARAM = "camera_size";
	static final String NUM_PLANES_PARAM = "numplanes";
	
	
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
		
		if (this.parameters.hasKeyAndTrue(PRECOMPUTED_POS_PARAM) && (new File(FileUtils.getPositionDataFilename(this.parameters))).exists()) {
			try {
				return FileUtils.readPositionData(FileUtils.getPositionDataFilename(this.parameters));
			} catch (java.io.IOException e) {
				/*
					TODO Log something
				*/
			} catch (ClassNotFoundException e) {
				/*
					TODO Log something
				*/
			}
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
	
		Image theImage = FileUtils.loadImage(toLoad.getImageFilename());
		
		if (this.parameters.hasKey(DARK_IMAGE_PARAM)) {
			
			Image dark = FileUtils.loadImage(this.parameters.getValueForKey(DARK_IMAGE_PARAM));
			
			Filter isf = new ImageSubtractionFilter();
			
			isf.setReferenceImage(dark);
			isf.apply(theImage);
			
		}
		
		return theImage;
		
	}
	
	/**
	 * Loads a mask specified by an ImageAndMaskSet.
	 * 
	 * @param toLoad an {@link ImageAndMaskSet} specifying the mask that will be loaded.
	 * @return an {@link Image} read from the specified location.
	 */
	protected Image loadMaskFromSet(ImageAndMaskSet toLoad) {
		
		Image theMask = FileUtils.loadImage(toLoad.getMaskFilename());
		
		return theMask;
	}
	
	private void checkAllRunningThreadsAndRemoveFinished(List<FittingThread> started, List<FittingThread> finished) {
		
		List<FittingThread> toMove = new java.util.ArrayList<FittingThread>();
		
		for (FittingThread ft : started) {
			try {
				ft.join(DEFAULT_THREAD_WAIT_MS);
			} catch (InterruptedException e) {
				/*
					TODO log something
				*/
			}
			
			if (! ft.isAlive()) {
				toMove.add(ft);
			}
		}
		
		started.removeAll(toMove);
		finished.addAll(toMove);
		
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
		
			ImageObject obj = new edu.stanford.cfuller.imageanalysistools.fitting.GaussianImageObject(i, new ReadOnlyImage(im), new ReadOnlyImage(mask), this.parameters);
		
			obj.setImageID(iams.getImageFilename());
		
			FittingThread nextThread = new FittingThread(obj, this.parameters);
			
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
	
	/**
	 * Checks an image object's fitted parameters for a variety of criteria like fit error, camera saturation, etc.  See the documentation in the implementation for specific checks.
	 * @param toCheck	the ImageObject whose parameters will be checked
	 * @return true if all the checks succeeded, false otherwise.
	 */
	protected boolean fitParametersOk(ImageObject toCheck) {
		
		/**
		 * Things to check:
		 * 1.  finished fitting
		 * 2.  R^2 value (replace this eventually with a better metric since I'm not least squares fitting...)
		 * 3.  too close to image edge in x, y, or z
		 * 4.  objects that are at or very near to camera saturation
		 * 5.  separation between channel is reasonable (i.e. on a scale that is within the known size of the complex of interest) -- occasionally different objects or schmutz are fit together and give wacky results
		 * 6.  theoretical fitting error
		 */
		
		//finished fitting
		if (!toCheck.finishedFitting()) return false;
		
		//R2 value
		
		if (! checkR2Ok(toCheck)) return false;
		
		//image edge proximity
		
		if (! checkEdgesOk(toCheck)) return false;
		
		//saturation
		
		if (! checkSaturationOk(toCheck)) return false;
		
		//reasonable separation
		
		if (! checkSeparationOk(toCheck)) return false;
		
		//fitting error
		
		if (! checkFittingErrorOk(toCheck)) return false;
		
		return true;
	}
	
	private boolean checkR2Ok(ImageObject obj) {

		if (! this.parameters.hasKey(R2_PARAM)) {return true;}

		double R2Cutoff = this.parameters.getDoubleValueForKey(R2_PARAM);
		
		int c = 0;
		
		for (double r2 : obj.getFitR2ByChannel()) {
			if (r2 < R2Cutoff) {
				/*
					TODO log something
				*/
				
				/*
					TODO increment fit failure
				*/
				
				return false;
			}
			++c;
		}
		return true;
	}
		
	private boolean checkEdgesOk(ImageObject obj) {
		//image edges
		double eps = 0.1; // a little wiggle room
		double cameraSizeX = this.parameters.getDoubleValueForKey(CAMERA_SIZE_PARAM);
		double cameraSizeY = cameraSizeX;
		double numplanes = this.parameters.getDoubleValueForKey(NUM_PLANES_PARAM);
		double imageBorderSize = this.parameters.getDoubleValueForKey(BORDER_PARAM);
		double halfZSize = this.parameters.getDoubleValueForKey(Z_BOX_SIZE_PARAM);
		
		if (!this.parameters.getBooleanValueForKey(DETERMINE_CORRECTION_PARAM)) {
			imageBorderSize *= 4;  //this ensures that the correction covers the area of the objects of interest without too many problematic edge effects;
		}
		
		for (FitParameters fp : obj.getFitParametersByChannel()) {
			double pos_x = fp.getPosition(ImageCoordinate.X);
			double pos_y = fp.getPosition(ImageCoordinate.Y);
			double pos_z = fp.getPosition(ImageCoordinate.Z);
		
			if (pos_x - eps > cameraSizeX - imageBorderSize ||
				pos_x + eps <= imageBorderSize ||
				pos_y - eps > cameraSizeY - imageBorderSize ||
				pos_y + eps <= imageBorderSize ||
				pos_z - eps > numplanes - halfZSize ||
				pos_z + eps <= halfZSize) {
					/*
						TODO log something
					*/
					
					/*
						TODO increment fit failures
					*/
					return false;
				}
		
		}
		
		
		
		return true;
	}
	
	private boolean checkSaturationOk(ImageObject obj) {
		
		if (this.parameters.hasKey(MAX_LEVEL_PARAM)) {
			obj.boxImages();
			
			double cutoff = this.parameters.getDoubleValueForKey(MAX_LEVEL_PARAM);
			
			for (ImageCoordinate ic : obj.getParent()) {
				if (obj.getParent().getValue(ic) > cutoff) {
					/*
						TODO log something
					*/
					/*
						TODO increment fit failures
					*/
					obj.unboxImages();
					return false;
				}
			}
			
			obj.unboxImages();
		}
		
		return true;
	}
		
	private boolean checkSeparationOk(ImageObject obj) {
		
		if (! this.parameters.hasKey(DIST_CUTOFF_PARAM)) {return true;}
		
		int numberOfChannels = obj.getFitParametersByChannel().size();
		
		double xy_pixelsize_2 = this.parameters.getDoubleValueForKey(PIXELSIZE_PARAM);
		
		xy_pixelsize_2 *= xy_pixelsize_2;
		
		double z_sectionsize_2 = this.parameters.getDoubleValueForKey(SECTIONSIZE_PARAM);
		
		z_sectionsize_2 *= z_sectionsize_2;
		
		for (int i = 0; i < numberOfChannels; i++) {
			for (int j = 0; j < numberOfChannels; j++) {
				if (i == j) continue;
				
				FitParameters fp1 = obj.getFitParametersByChannel().get(i);
				FitParameters fp2 = obj.getFitParametersByChannel().get(j);
				
				double ijdist = xy_pixelsize_2 * Math.pow(fp1.getPosition(ImageCoordinate.X) - fp2.getPosition(ImageCoordinate.X),2) + xy_pixelsize_2 * Math.pow(fp1.getPosition(ImageCoordinate.Y) - fp2.getPosition(ImageCoordinate.Y),2) + z_sectionsize_2 * Math.pow(fp1.getPosition(ImageCoordinate.Z) - fp2.getPosition(ImageCoordinate.Z),2);
			
				if (ijdist > this.parameters.getDoubleValueForKey(DIST_CUTOFF_PARAM)) {
				
					/*
						TODO log something
					*/
					
					/*
						TODO increment fit failures
					*/
					
					return false;
				}
			
			}
		}
		
		return true;
	}
		
	private boolean checkFittingErrorOk(ImageObject obj) {
		
		if (! this.parameters.hasKey(ERROR_CUTOFF_PARAM)) {return true;}
		
		double totalError = 0;
		
		for (double d : obj.getFitErrorByChannel()) {
			totalError += d*d;
		}
		
		totalError = Math.sqrt(totalError);
		
		if (totalError > this.parameters.getDoubleValueForKey(ERROR_CUTOFF_PARAM) || Double.isNaN(totalError)) {
			
			/*
				TODO log something
			*/
			
			/*
				TODO increment fit failures
			*/
			
			return false;
			
		}
		
		
		return true;
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
		
		df.fit(imageObjects, null);
		
		//output plots and information
		
		/*
			TODO output
		*/
		
	}

	public static void main(String[] args) {
		
		System.out.println("Hello, world!");
		
		Initializer in = new Initializer();
		
		in.initializeParameters(args);
		
		(new Colocalization3DMain()).go(in);
						
	}
	
	protected static class FittingThread extends Thread {
		
		ParameterDictionary p;
		ImageObject toFit;
		
		public FittingThread(ImageObject toFit, ParameterDictionary p) {
			this.toFit = toFit;
			this.p = p;
		}
		
		public ImageObject getFitObject() {
			
			return this.toFit;
			
		}
		
		@Override
		public void run() {
			try {
				this.toFit.fitPosition(this.p);
			} catch (IllegalArgumentException e) {
                e.printStackTrace();
                java.util.logging.Logger.getLogger(LOGGER_NAME).warning("exception while fitting in image: " + this.toFit.getImageID() + ".  Skipping and continuing.");
            }

		}
		
	}
	
}

