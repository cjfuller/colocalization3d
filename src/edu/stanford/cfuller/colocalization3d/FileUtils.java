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

import java.util.List;

import edu.stanford.cfuller.imageanalysistools.fitting.ImageObject;
import edu.stanford.cfuller.imageanalysistools.image.Image;
import edu.stanford.cfuller.imageanalysistools.parameters.ParameterDictionary;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

public class FileUtils {
	
	/**
	 * Required parameters
	 */
	
	static final String DIRNAME_PARAM = "dirname_set";
	static final String BASENAME_PARAM = "basename_set";
	static final String MASK_DIR_PARAM = "mask_relative_dirname";
	static final String MASK_EXT_PARAM = "mask_extra_extension";
	static final String DATA_DIR_PARAM = "data_directory";
	
	
	
	static final String postion_xml_extension = "_position_data.xml";

	
	/**
	 * Gets the full set of directory names for processing from a parameter dictionary.
	 * @param p 	The {@link ParameterDictionary} specifying the set of directory names.
	 * @return 		A String array containing the directory names.
	 */
	public static String[] getSetOfDirnames(ParameterDictionary p) {
		return p.getValueForKey(DIRNAME_PARAM).split(",");
	}
	
	/**
	 * Gets the full set of base filenames for processing from a parameter dictionary.
	 * @param p The {@link ParameterDictionary} specifying the set of base filenames.
	 * @return A String array containing the base filenames.
	 */
	public static String[] getSetOfBaseFilenames(ParameterDictionary p) {
		return p.getValueForKey(BASENAME_PARAM).split(",");
	}
	
	/**
	 * Gets the filename to which / from which image object positions will be written / read from a parameter dictionary.
	 * @param p The {@link ParameterDictionary} specifying the filename for the positions.
	 * @return A String specifying the absolute path to the position file.
	 */
	public static String getPositionDataFilename(ParameterDictionary p) {
		String dir = p.getValueForKey(DATA_DIR_PARAM);
		String filename = p.getValueForKey(BASENAME_PARAM).split(",")[0];
		return (dir + File.separator + filename + position_xml_extension);
	}
	
	
	/**
	* Reads fitted position data stored in ImageObjects from the file specified by the supplied name.
	* @param filename	a String specifying the full path to the file containing the position data.
	* @return a List<ImageObject> containing the ImageObjects (and their position fit data).
	* @throws IOException              If the objects cannot be read from disk.
	* @throws ClassNotFoundException   If the file does not contain data for ImageObjects in the correct format.
	*/
	public static Vector<ImageObject> readPositionData(String filename) throws IOException, ClassNotFoundException {

		String filename = FileUtils.getPositionDataFilename(p);

		File f = new File(filename);

		FileReader fr = new FileReader(f);

		XMLStreamReader xsr = null;
		String encBinData = null;

		Vector<ImageObject> output = new Vector<ImageObject>();

		HexBinaryAdapter hba = new HexBinaryAdapter();

		try {
			xsr = XMLInputFactory.newFactory().createXMLStreamReader(fr);

			while(xsr.hasNext()) {

				int event = xsr.next();

				if (event != XMLStreamReader.START_ELEMENT) continue; 

				if (xsr.hasName() && xsr.getLocalName() == ImageObject.SERIAL_ELEMENT) {

					encBinData = xsr.getElementText();

					byte[] binData = hba.unmarshal(encBinData);

					ObjectInputStream oi = new ObjectInputStream(new ByteArrayInputStream(binData));

					output.add((ImageObject) oi.readObject());

				}

			}
		} catch (XMLStreamException e) {
			java.util.logging.Logger.getLogger(FileUtils.class.getName()).severe("Exception encountered while reading XML ImageObject data: " + e.getMessage());        
		}

		return output;

	}
	
	/**
	 * Loads an image from the specified filename.
	 * @param filename A string containing the filename from which to load the image.
	 * @return an Image loaded from the file.
	 */
	public static Image loadImage(String filename) {
		try {
			return (new edu.stanford.cfuller.imageanalysistools.image.io.ImageReader()).read(filename);
		} catch (java.io.IOException e) {
			java.util.logging.Logger.getLogger(Colocalization3DMain.LOGGER_NAME).severe("Exception encountered while reading image from " + filename + ": " + e.getMessage());
		}
		return null;
	}
	
	/**
	 * Lists all the image files to be processed and matches each one with a mask identifying the objects in that image.
	 * @param p The {@link ParameterDictionary} specifying which files will be processed.
	 * @return A {@link ImageAndMaskSet List<ImageAndMaskSet>} containing each filename paired with its mask.
	 */
	public static List<ImageAndMaskSet> listFilesToProcess(ParameterDictionary p) {
		
		String[] dirnames = FileUtils.getSetOfDirnames(p);
		String[] basenames = FileUtils.getSetOfBaseFilenames(p);
		
		List<ImageAndMaskSet> toReturn = new java.util.ArrayList<ImageAndMaskSet>();
				
		for (String dir : dirnames) {
			
			String maskdirname = dir + File.separator + p.getValueForKey(MASK_DIR_PARAM);
			
				
			for (File f : (new File(dir)).listFiles()) {
				for (String base : basenames) {
					if (f.getName().matches(".*" + base + ".*")) {
						String image = f.getAbsolutePath();
						String mask = maskdirname + File.separator + f.getName() + p.getValueForKey(MASK_EXT_PARAM);
						ImageAndMaskSet current = new ImageAndMaskSet(image, mask);
						toReturn.add(current);
					}
				}
			}
		}


		return toReturn;
		
	}
	
	/**
	 * Writes position data from ImageObjects that have been fitted to disk.
	 * 
	 * @param objects The list of ImageObjects that have been fitted and are ready to write.
	 * @param p A {@link ParameterDictionary } specifying the location to which the position data will be written.
	 * @throws IOException      If the objects cannot be written.
	 */
	public static void writeFittedImageObjectsToDisk(java.util.List<ImageObject> objects, ParameterDictionary p) {
		
		String filename = FileUtils.getPositionDataFilename(p);
		
        File f = new File(filename);
        
        StringWriter sw = new StringWriter();

		try {

			XMLStreamWriter xsw = XMLOutputFactory.newFactory().createXMLStreamWriter(sw);
		
			xsw.writeStartDocument();
			xsw.writeStartElement("root");
			xsw.writeCharacters("\n");
			
			for (ImageObject i : imageObjects) {
				i.writeToXML(xsw);
			}
			
			xsw.writeEndElement(); //root
			
			xsw.writeEndDocument();
			
		} catch (XMLStreamException e) {
    		
    		java.util.logging.Logger.getLogger(FileUtils.class.getName()).severe("Exception encountered while writing XML ImageObject data output: " + e.getMessage());
		
		}

        PrintWriter pw = new PrintWriter(new FileWriter(f));
        
        pw.print(sw.toString());

        pw.close();
	}
	
	
}
