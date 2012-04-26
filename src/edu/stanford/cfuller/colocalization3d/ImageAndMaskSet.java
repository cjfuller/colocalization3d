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

/**
* An object that keeps track of an Image and an associated Mask.
* @author Colin J. Fuller
*/
public class ImageAndMaskSet {

	String imageName;
	String maskName;

	/**
	* Constructs a new ImageAndMaskSet with the specified filenames.
	* @param image The absolute filename of the image.
	* @param mask The absolute filename of the mask.
	*/
	public ImageAndMaskSet(String image, String mask) {
		this.imageName = image;
		this.maskName = mask;
	}

	/**
	 * Gets the filename of the image associated with this image and mask set.
	 * 
	 * @return a string containing the absolute filename of the image.
	 */
	public String getImageFilename() {
		
		return this.imageName;
		
	}
	
	/**
	 * Gets the filename of the mask associated with this image and mask set.
	 * 
	 * @return a string containing the absolute filename of the mask.
	 */
	public String getMaskFilename() {
		
		return this.maskName;
		
	}
	
}
