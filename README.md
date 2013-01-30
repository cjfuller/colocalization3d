## Colocalization3D

Java implementation of three-dimensional high-resolution colocalization.

Requires the [ImageAnalysisTools library](http://cjfuller.github.com/imageanalysistools).

## Running

The program is run from the command-line with an XML or ruby file (formatted according to the parameter file specifications for the ImageAnalysisTools project) containing parameters for the analysis:

    java -jar Colocalization3D.jar /path/to/parameters.xml

A sample parameters file, including annotations for the recommended/required parameters, and what they do is located in the [downloads](https://github.com/downloads/cjfuller/colocalization3d/colocalization3D_sample_annotated_parameters.rb).

## Building from source

Building from source can be done with Apache ant (http://ant.apache.org/).  To build, in the top-level directory containing this file and build.xml, run:

    ant dist

The build expects to have the ImageAnalysisTools standalone jar in the folder named by the ant property IAT_libdir.  (This defaults to a subdirectory of the top-level directory called lib.)  This will create the executable jar file in the subdirectory dist, and it will copy required libraries to a subdirectory of dist.


## License

Colocalization3D is distributed under the MIT/X11 license.  See the file named LICENSE for the full license text.
