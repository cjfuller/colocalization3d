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

import edu.stanford.cfuller.imageanalysistools.parameters.ParameterDictionary;


public class FileUtils {
	
	/**
	 * Gets the full set of directory names for processing from a parameter dictionary.
	 * @param p 	The {@link ParameterDictionary} specifying the set of directory names.
	 * @return 		A String array containing the directory names.
	 */
	public static String[] getSetOfDirnames(ParameterDictionary p) {
		//TODO: implementation
		return null;
	}
	
	/**
	 * Gets the full set of base filenames for processing from a parameter dictionary.
	 * @param p The {@link ParameterDictionary} specifying the set of base filenames.
	 * @return A String array containing the base filenames.
	 */
	public static String[] getSetOfBaseFilenames(ParameterDictionary p) {
		//TODO: implementation
		return null;
	}
	
	/**
	 * Gets the filename to which / from which image object positions will be written / read from a parameter dictionary.
	 * @param p The {@link ParameterDictionary} specifying the filename for the positions.
	 * @return A String specifying the absolute path to the position file.
	 */
	public static String getPositionDataFilename(ParameterDictionary p) {
		//TODO: implementation
		return null;
	}
	
	/**
	 * Lists all the image files to be processed and matches each one with a mask identifying the objects in that image.
	 * @param p The {@link ParameterDictionary} specifying which files will be processed.
	 * @return A {@link ImageAndMaskSet java.util.List<ImageAndMaskSet>} containing each filename paired with its mask.
	 */
	public static java.util.List<ImageAndMaskSet> listFilesToProcess(ParameterDictionary p) {
		//TODO: implemenation
		return null;
	}
	
}
