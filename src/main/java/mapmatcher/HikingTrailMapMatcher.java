package mapmatcher;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Chargeur;
import fr.ign.cogit.geoxygene.matching.hmmm.HMMMapMatcher;

public class HikingTrailMapMatcher extends HMMMapMatcher {
	/**
	 * @param gpsPop
	 * @param networkPop
	 * @param sigmaZ
	 * @param selection
	 * @param beta
	 * @param distanceLimit
	 */
	public HikingTrailMapMatcher(IFeatureCollection<? extends IFeature> gpsPop,
			IFeatureCollection<? extends IFeature> networkPop, double sigmaZ, double selection, double beta,
			double distanceLimit) {
		super(gpsPop, networkPop, sigmaZ, selection, beta, distanceLimit);
	}

	@Override
	protected void importNetwork(IFeatureCollection<? extends IFeature> network) {
		double tolerance = 0.1;
		Chargeur.importAsEdges(network, this.getNetworkMap(), null, null, null, null, null, tolerance);
	}
}
