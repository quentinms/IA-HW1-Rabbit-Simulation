import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import javax.activation.DataSource;

import uchicago.src.reflector.RangePropertyDescriptor;
import uchicago.src.sim.analysis.OpenSequenceGraph;
import uchicago.src.sim.analysis.Sequence;
import uchicago.src.sim.engine.BasicAction;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.engine.SimModelImpl;
import uchicago.src.sim.gui.ColorMap;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.Object2DDisplay;
import uchicago.src.sim.gui.Value2DDisplay;
import uchicago.src.sim.util.SimUtilities;

/**
 * Class that implements the simulation model for the rabbits grass simulation.
 * This is the first class which needs to be setup in order to run Repast
 * simulation. It manages the entire RePast environment and the simulation.
 * 
 * @author
 */

public class RabbitsGrassSimulationModel extends SimModelImpl {

	private Schedule schedule;

	private RabbitsGrassSimulationSpace rgsSpace;

	private DisplaySurface displaySurf;

	private OpenSequenceGraph amountOfGrassInSpace;

	private OpenSequenceGraph amountOfAgentsInSpace;

	private ArrayList agentList;

	// Default Values
	private static final int NUMAGENTS = 1;
	private static final int WORLDXSIZE = 10;
	private static final int WORLDYSIZE = 10;
	private static final int GRASSGROWTRATE = 10;
	private static final int GRASS_ENERGY = 1;
	private static final int INITIAL_ENERGY = 10;
	private static final int BIRTH_TRESHOLD = 15;

	private int numAgents = NUMAGENTS;
	private int worldXSize = WORLDXSIZE;
	private int worldYSize = WORLDYSIZE;
	private int grassGrowthRate = GRASSGROWTRATE;
	private int birthThreshold = BIRTH_TRESHOLD;
	private int grassEnergy = GRASS_ENERGY;

	class grassInSpace implements DataSource, Sequence {

		public Object execute() {
			return new Double(getSValue());
		}

		public double getSValue() {
			System.out.println("Total grass in space = " + rgsSpace.getTotalGrass());
			return (double) rgsSpace.getTotalGrass();
		}

		
		/* Useless but required methods. */
		@Override
		public String getContentType() {
			return null;
		}

		@Override
		public InputStream getInputStream() throws IOException {
			return null;
		}

		@Override
		public String getName() {
			return null;
		}

		@Override
		public OutputStream getOutputStream() throws IOException {
			return null;
		}
	}

	class agentsInSpace implements DataSource, Sequence {

		public Object execute() {
			return new Double(getSValue());
		}

		@Override
		public double getSValue() {
			return (double) rgsSpace.getTotalAgents();
		}

		/* Useless but required methods. */
		@Override
		public String getContentType() {
			return null;
		}

		@Override
		public InputStream getInputStream() throws IOException {
			return null;
		}

		@Override
		public String getName() {
			return null;
		}

		@Override
		public OutputStream getOutputStream() throws IOException {
			return null;
		}

	}

	public static void main(String[] args) {

		System.out.println("Rabbit skeleton");
		SimInit init = new SimInit();
		RabbitsGrassSimulationModel model = new RabbitsGrassSimulationModel();
		init.loadModel(model, "", false);

	}

	public void setup() {
		// System.out.println("Running setup");
		rgsSpace = null;
		agentList = new ArrayList();

		schedule = new Schedule(1);

		// Tear down displays
		if (displaySurf != null) {
			displaySurf.dispose();
		}
		displaySurf = null;

		if (amountOfGrassInSpace != null) {
			amountOfGrassInSpace.dispose();
		}
		amountOfGrassInSpace = null;

		if (amountOfAgentsInSpace != null) {
			amountOfAgentsInSpace.dispose();
		}
		amountOfAgentsInSpace = null;

		// Create displays
		displaySurf = new DisplaySurface(this, "Carry Drop Model Window 1");
		amountOfGrassInSpace = new OpenSequenceGraph(
				"Amount of grass in space", this);
		amountOfAgentsInSpace = new OpenSequenceGraph(
				"Amount of rabbits in space", this);

		// Register displays
		registerDisplaySurface("Carry Drop Model Window 1", displaySurf);
		this.registerMediaProducer("PlotGrass", amountOfGrassInSpace);
		this.registerMediaProducer("PlotAgents", amountOfAgentsInSpace);

	}

	public void begin() {
		buildModel();
		buildSchedule();
		buildDisplay();

		displaySurf.display();
		amountOfGrassInSpace.display();
		amountOfAgentsInSpace.display();
	}

	public void buildModel() {
		System.out.println("Running BuildModel");
		rgsSpace = new RabbitsGrassSimulationSpace(worldXSize, worldYSize);
		rgsSpace.spreadGrass(grassGrowthRate);

		for (int i = 0; i < numAgents; i++) {
			addNewAgent();
		}

		for (int i = 0; i < agentList.size(); i++) {
			RabbitsGrassSimulationAgent rgsa = (RabbitsGrassSimulationAgent) agentList
					.get(i);
			rgsa.report();
		}

	}

	public void buildSchedule() {
		System.out.println("Running BuildSchedule");

		class RabbitsGrassSimulationStep extends BasicAction {
			public void execute() {

				/*try {
					Thread.sleep(500); // sleep a tenth of a second
				} catch (Exception ex) {
					// ignore this exception
				}*/

				System.out
						.println("*********************************************************");

				rgsSpace.spreadGrass(grassGrowthRate);

				SimUtilities.shuffle(agentList);
				for (int i = 0; i < agentList.size(); i++) {
					RabbitsGrassSimulationAgent rgsa = (RabbitsGrassSimulationAgent) agentList
							.get(i);
					rgsa.step();

					rgsa.report();
				}

				/* Check Repro */

				for (int i = 0; i < agentList.size(); i++) {
					RabbitsGrassSimulationAgent rgsa = (RabbitsGrassSimulationAgent) agentList
							.get(i);

					if (rgsa.getEnergy() > birthThreshold) {

						boolean reproductionSuccessfull = addNewAgent();

						rgsa.setEnergy(rgsa.getEnergy() - INITIAL_ENERGY);
						System.out.println("Reproduction of agent #"
								+ (rgsa.getID()) + ". It now has "
								+ rgsa.getEnergy());

					}

				}

				// Remove dead rabbits :'(

				for (int i = (agentList.size() - 1); i >= 0; i--) {
					RabbitsGrassSimulationAgent rgsa = (RabbitsGrassSimulationAgent) agentList
							.get(i);
					if (rgsa.getEnergy() <= 0) {
						rgsSpace.removeAgentAt(rgsa.getX(), rgsa.getY());
						agentList.remove(i);
					}
				}

				displaySurf.updateDisplay();
			}
		}

		schedule.scheduleActionBeginning(0, new RabbitsGrassSimulationStep());

		class CarryDropCountLiving extends BasicAction {
			public void execute() {
				countLivingAgents();
			}
		}

		schedule.scheduleActionAtInterval(10, new CarryDropCountLiving());

		class RabbitsGrassSimulationUpdateGrassInSpace extends BasicAction {
			public void execute() {
				amountOfGrassInSpace.step();
			}
		}

		schedule.scheduleActionAtInterval(10,
				new RabbitsGrassSimulationUpdateGrassInSpace());

		class RabbitsGrassSimulationUpdateAgentsInSpace extends BasicAction {
			public void execute() {
				amountOfAgentsInSpace.step();
			}
		}

		schedule.scheduleActionAtInterval(10,
				new RabbitsGrassSimulationUpdateAgentsInSpace());
	}

	public void buildDisplay() {
		System.out.println("Running BuildDisplay");

		ColorMap map = new ColorMap();

		for (int i = 1; i < 32; i++) {
			/* Green color map (light green to dark green) */
			map.mapColor(i, new Color(0, 255 - (int) (i * 4 + 127), 0));
		}
		map.mapColor(0, Color.white);

		Value2DDisplay displayGrass = new Value2DDisplay(
				rgsSpace.getCurrentGrassSpace(), map);

		Object2DDisplay displayRabbits = new Object2DDisplay(
				rgsSpace.getCurrentAgentSpace());
		displayRabbits.setObjectList(agentList);

		displaySurf.addDisplayableProbeable(displayGrass, "Grass");
		displaySurf.addDisplayableProbeable(displayRabbits, "Rabbits");

		amountOfGrassInSpace.addSequence("Grass in space", new grassInSpace());
		amountOfAgentsInSpace.addSequence("Agents in space",
				new agentsInSpace());
	}

	public String[] getInitParam() {

		/* Sliders */
		RangePropertyDescriptor pdNumAgents = new RangePropertyDescriptor(
				"NumAgents", 0, 100, 20);
		descriptors.put("NumAgents", pdNumAgents);

		RangePropertyDescriptor pdWorldXSize = new RangePropertyDescriptor(
				"WorldXSize", 0, 50, 10);
		descriptors.put("WorldXSize", pdWorldXSize);

		RangePropertyDescriptor pdWorldYSize = new RangePropertyDescriptor(
				"WorldYSize", 0, 50, 10);
		descriptors.put("WorldYSize", pdWorldYSize);

		RangePropertyDescriptor pdGrassGrowthRate = new RangePropertyDescriptor(
				"GrassGrowthRate", 0, 50, 10);
		descriptors.put("GrassGrowthRate", pdGrassGrowthRate);

		RangePropertyDescriptor pdBirthThreshold = new RangePropertyDescriptor(
				"BirthThreshold", 0, 50, 10);
		descriptors.put("BirthThreshold", pdBirthThreshold);

		RangePropertyDescriptor pdGrassEnergy = new RangePropertyDescriptor(
				"GrassEnergy", 0, 20, 5);
		descriptors.put("GrassEnergy", pdGrassEnergy);

		String[] initParams = { "NumAgents", "WorldXSize", "WorldYSize",
				"GrassGrowthRate", "BirthThreshold", "GrassEnergy" };
		return initParams;
	}

	public String getName() {
		return "Rabbits";
	}

	private boolean addNewAgent() {

		if (agentList.size() < worldXSize * worldYSize) {
			RabbitsGrassSimulationAgent a = new RabbitsGrassSimulationAgent(
					INITIAL_ENERGY, grassEnergy);
			agentList.add(a);
			rgsSpace.addAgent(a);
			System.out.println("A new agent with " + a.getEnergy()
					+ " energy is born!");
			a.report();
			return true;
		} else {
			System.out.println("Too many rabbits already");
			return false;
		}
	}

	private int countLivingAgents() {
		int livingAgents = 0;
		for (int i = 0; i < agentList.size(); i++) {
			RabbitsGrassSimulationAgent rgsa = (RabbitsGrassSimulationAgent) agentList
					.get(i);
			if (rgsa.getEnergy() > 0)
				livingAgents++;
		}
		System.out.println("Number of living agents is: " + livingAgents);

		return livingAgents;
	}

	public Schedule getSchedule() {
		return schedule;
	}

	public int getNumAgents() {
		return numAgents;
	}

	public void setNumAgents(int na) {
		if (na > worldXSize * worldYSize) {
			na = worldXSize * worldYSize;
		}
		numAgents = na;
	}

	public int getWorldXSize() {
		return worldXSize;
	}

	public void setWorldXSize(int wxs) {
		if (wxs < 1) {
			wxs = WORLDXSIZE;
		}
		worldXSize = wxs;
	}

	public int getWorldYSize() {
		return worldYSize;
	}

	public void setWorldYSize(int wys) {

		if (wys < 1) {
			wys = WORLDXSIZE;
		}

		worldYSize = wys;
	}

	public int getGrassGrowthRate() {
		return grassGrowthRate;
	}

	public void setGrassGrowthRate(int i) {
		grassGrowthRate = i;
	}

	public int getGrassEnergy() {
		return grassEnergy;
	}

	public void setGrassEnergy(int i) {
		grassEnergy = i;
	}

	public int getBirthThreshold() {
		return birthThreshold;
	}

	public void setBirthThreshold(int i) {
		birthThreshold = i;
	}

}
