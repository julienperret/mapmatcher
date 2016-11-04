[![Build Status](https://travis-ci.org/julienperret/mapmatcher.svg?branch=master)](https://travis-ci.org/julienperret/mapmatcher)
# Overview

A simple **map matching** library using the *Hidden-Markov Model Map Matching algorithm* (HMM Map Matching) from Paul Newson and John Krumm, "Hidden Markov Map Matching Through Noise and Sparseness", 17th ACM SIGSPATIAL International Conference on Advances in Geographic Information Systems (ACM SIGSPATIAL GIS 2009), November 4-6, Seattle, WA, pp. 336-343. ([PDF](http://research.microsoft.com/en-us/um/people/jckrumm/Publications%202009/map%20matching%20ACM%20GIS%20camera%20ready.pdf), [slides](http://research.microsoft.com/en-us/um/people/jckrumm/Publications%202009/Hidden%20Markov%20Map%20Matching%20Through%20Noise%20and%20Sparseness%20-%20ACM%20SIGSPATIAL%202009-final.pptx), [shared data](http://research.microsoft.com/en-us/um/people/jckrumm/MapMatchingData/data.htm)).

It is designed to be used from [R](https://www.r-project.org/) and uses the [GeOxygene](https://github.com/IGNF/geoxygene) implementation of the algorithm.

# Usage
## Installation
You will need to have [git](https://github.com/git/git) and [maven](https://github.com/apache/maven) installed to proceed.
~~~~
git clone git@github.com:julienperret/mapmatcher.git
cd mapmatcher
mvn install
~~~~
If that fails, it might be because of insecure SSL certificate. Try the following:
~~~~
mvn install -Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true
~~~~
this should produce a file named *mapmatcher-0.0.1-SNAPSHOT-jar-with-dependencies.jar* in the *target* directory.
You can then include this library in *R* by placing the library in the *classpath*.
~~~~
export CLASSPATH=./target/mapmatcher-0.0.1-SNAPSHOT-jar-with-dependencies.jar
~~~~

Good, now you can use the library from *R*. In order to do that, let's create a simple function:
~~~~
matchHikingTrail<-function(
  gpsFile,
  networkFile,
  outFile=tempfile(),
  sigmaZ=10.0,
  selection=50.0,
  beta=6.0,
  distanceLimit=2000.0)
{
  command=paste("java",
                "mapmatcher.MatchHikingTrail",
                gpsFile,
                networkFile,
                outFile,
                sigmaZ=10.0,
                selection=50.0,
                beta=6.0,
                distanceLimit)
  system(command)
  read.table(outFile,header=TRUE)
}
~~~~

An example call of the function with existing shapefiles:
~~~~
matchHikingTrail("/my/existing/gps_file.shp","/my/existing/roadnetwork_file.shp","/output/matched_gps_file.csv")
~~~~

