package cc.cxsj.nju.gobang.ai;

import cc.cxsj.nju.gobang.Main;
import org.apache.log4j.Logger;

public class RobotAIFactory {
	
	private static final Logger LOG = Logger.getLogger(Main.class);
	
	public static RobotAI produceRobotAIof(RobotAIModel model) {
		switch (model) {
			case RobotOmega:
                LOG.info("Produce Robot Omega");
                return new RobotO();
			case RobotAlpha:
				LOG.info("Produce one Robot Alpha");
				return new RobotI();
			case RobotBeta:
				LOG.info("Produce on Robot Beta");
				return new RobotII();
			case RobotGamma:
            case RobotLambda:
			default:
				LOG.info("Robot Factory can not produce this model Robot!");
				System.exit(0);
			}
		return null;
	}
}
