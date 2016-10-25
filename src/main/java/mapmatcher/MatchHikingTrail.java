package mapmatcher;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.api.spatial.geomprim.IPoint;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Arc;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.Population;
import fr.ign.cogit.geoxygene.matching.hmmm.HMMMapMatcher;
import fr.ign.cogit.geoxygene.matching.hmmm.HMMMapMatcher.Node;
import fr.ign.cogit.geoxygene.schema.schemaConceptuelISOJeu.AttributeType;
import fr.ign.cogit.geoxygene.schema.schemaConceptuelISOJeu.FeatureType;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;
import fr.ign.cogit.geoxygene.spatial.geomprim.GM_Point;
import fr.ign.cogit.geoxygene.util.algo.JtsAlgorithms;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;

public class MatchHikingTrail {

	public static void main(String[] args) throws IOException {
		Logger logger = Logger.getLogger(HMMMapMatcher.class.getName());
		logger.setLevel(Level.FATAL);
		String gpsFile = args[0];// "/home/julien/test_mapmatching/20140716_3_test.shp"
		String networkFile = args[1];// "/home/julien/test_mapmatching/sentier_test.shp"
		String outFile = args[2];
		double sigmaZ = (args.length < 4) ? 10.0 : Double.parseDouble(args[3]);// 10.0
		double selection = (args.length < 5) ? 50.0 : Double.parseDouble(args[4]);// 50.0
		double beta = (args.length < 6) ? 6.0 : Double.parseDouble(args[5]);// 6.0
		double distanceLimit = (args.length < 7) ? 2000.0 : Double.parseDouble(args[6]);// 2000.0
		System.out.println("start");
		IPopulation<IFeature> gpsPop = ShapefileReader.read(gpsFile, "traces", null, true);
		IPopulation<IFeature> networkPop = ShapefileReader.read(networkFile, "sentiers", null, true);
		System.out.println("loaded");
		HikingTrailMapMatcher mapMatcher = new HikingTrailMapMatcher(gpsPop, networkPop, sigmaZ, selection, beta, distanceLimit);
		System.out.println("preprocess " + gpsPop.size());
		// mapMatcher.preprocessPoints();
		System.out.println("Map Matching start with " + gpsPop.size());
		Node result = mapMatcher.computeTransitions();
		System.out.println("Map Matching finished");
		// Création du type géométrique
		FeatureType ftLines = new FeatureType();
		ftLines.setTypeName("Traces");
		ftLines.setNomClasse("DefaultFeature");
		ftLines.setGeometryType(GM_LineString.class);
		Population<DefaultFeature> popTraces = new Population<DefaultFeature>(ftLines, false); // $NON-NLS-1$
		popTraces.setClasse(DefaultFeature.class);
		for (int i = 0; i < gpsPop.size() - 1; i++) {
			IFeature p1 = gpsPop.get(i);
			IFeature p2 = gpsPop.get(i + 1);
			popTraces.nouvelElement(new GM_LineString(Arrays.asList(p1.getGeom().centroid(), p2.getGeom().centroid())));
		}
		// RECALAGE DES POINTS GPS SUR LE RESEAU
		System.out.println("Traces wrote");
		// Création du type géométrique
		FeatureType ftPoints = new FeatureType();
		ftPoints.setGeometryType(GM_Point.class);
		ftPoints.addFeatureAttribute(new AttributeType("id", "int"));
		Population<DefaultFeature> popMatchedPoints = new Population<DefaultFeature>("Points Recales"); //$NON-NLS-1$
		popMatchedPoints.setFeatureType(ftPoints);
		popMatchedPoints.setClasse(DefaultFeature.class);

		FeatureType ftVector = new FeatureType();
		ftVector.setTypeName("Vector");
//		ftLines.setNomClasse("Arc");
		ftVector.setGeometryType(GM_LineString.class);
//		ftVector.addFeatureAttribute(new AttributeType("id", "int"));
		ftVector.addFeatureAttribute(new AttributeType("Poids", "double"));
		Population<Arc> popMatchedVectors = new Population<Arc>("Vectors"); //$NON-NLS-1$
		popMatchedVectors.setClasse(Arc.class);
		popMatchedVectors.setFeatureType(ftVector);
		List<Double> lengths = new ArrayList<>();
		for (int i = 0; i < gpsPop.size(); i++) {
			GM_Point p = (GM_Point) gpsPop.get(i).getGeom();
			ILineString l = result.getStates().get(i).getGeometrie();
			DefaultFeature projectedPoint = popMatchedPoints.nouvelElement();
			IPoint projection = JtsAlgorithms.getClosestPoint(p.getPosition(), l).toGM_Point();
			projectedPoint.setGeom(projection);
			projectedPoint.setId(i);
			gpsPop.get(i).setId(i);
			ILineString line = new GM_LineString(p.getPosition(), projection.getPosition());
			Arc edge = popMatchedVectors.nouvelElement(line);
			edge.setId(i);
			double length = line.length();
			edge.setPoids(length);
			lengths.add(length);
		}
		File file = new File(outFile);
		// if file doesnt exists, then create it
		if (!file.exists()) {
			file.createNewFile();
		}
		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write("id x y\n");
		for (IFeature f : popMatchedPoints) {
			IPoint p = (IPoint) f.getGeom();
			bw.write(f.getId() + " " + p.getPosition().getX() + " " + p.getPosition().getY() + "\n");
		}
		bw.close();
		fw.close();
    ShapefileWriter.write(popMatchedVectors, gpsFile.substring(0, gpsFile.lastIndexOf("."))+"_vectors.shp");

		System.out.println("length");
		for (Double d : lengths) System.out.println("" + d);
	}
}
