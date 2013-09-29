import java.awt.Color;
import java.util.ArrayList;

import uchicago.src.reflector.RangePropertyDescriptor;
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

	private ArrayList agentList;

	// Default Values
	private static final int NUMAGENTS = 1;
	private static final int WORLDXSIZE = 10;
	private static final int WORLDYSIZE = 10;
	private static final int GRASSGROWTRATE = 10;
	private static final int GRASSENERGY = 1;
	//private static final int INITIAL_ENERGY = 100;
	private static final int BIRTH_TRESHOLD = 25;

	private int numAgents = NUMAGENTS;
	private int worldXSize = WORLDXSIZE;
	private int worldYSize = WORLDYSIZE;
	private int grassGrowthRate = GRASSGROWTRATE;
	private int birthThreshold = BIRTH_TRESHOLD;

	public static void main(String[] args) {

		System.out.println("Rabbit skeleton");
		SimInit init = new SimInit();
		RabbitsGrassSimulationModel model = new RabbitsGrassSimulationModel();
		init.loadModel(model, "", false);

	}

	public void setup() {
		System.out.println("Running setup");
		rgsSpace = null;
		agentList = new ArrayList();

		schedule = new Schedule(1);

		if (displaySurf != null) {
			displaySurf.dispose();
		}
		displaySurf = null;

		displaySurf = new DisplaySurface(this, "Carry Drop Model Window 1");

		registerDisplaySurface("Carry Drop Model Window 1", displaySurf);

	}

	public void begin() {
		buildModel();
		buildSchedule();
		buildDisplay();

		displaySurf.display();

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

		class CarryDropStep extends BasicAction {
			public void execute() {
				
				try {Thread.sleep(500); // sleep a tenth of a second
				} catch (Exception ex) {
				// ignore this exception
				} 
				
				rgsSpace.spreadGrass(grassGrowthRate);
				
				
				SimUtilities.shuffle(agentList);
				for (int i = 0; i < agentList.size(); i++) {
					RabbitsGrassSimulationAgent rgsa = (RabbitsGrassSimulationAgent) agentList
							.get(i);
					rgsa.step();
					
					
					if (rgsa.getEnergy() > birthThreshold) {
						rgsa.setEnergy(rgsa.getEnergy() - birthThreshold);
						addNewAgent();
						//System.out.println("Repoduction of agent #"+(i+1));
					}
					
					rgsa.report();
				}

				// Remove dead agents
				
				
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

		schedule.scheduleActionBeginning(0, new CarryDropStep());

		class CarryDropCountLiving extends BasicAction {
			public void execute() {
				countLivingAgents();
			}
		}

		schedule.scheduleActionAtInterval(10, new CarryDropCountLiving());
	}

	public void buildDisplay() {
		System.out.println("Running BuildDisplay");

		ColorMap map = new ColorMap();

		for (int i = 1; i < 32; i++) {
			/* Green color map (light green to dark green)*/
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
	}

	public String[] getInitParam() {
		
		
		/* Sliders */
		RangePropertyDescriptor pdNumAgents = new RangePropertyDescriptor("NumAgents", 0, 100, 20);
		descriptors.put("NumAgents", pdNumAgents);
		
		RangePropertyDescriptor pdWorldXSize = new RangePropertyDescriptor("WorldXSize", 0, 50, 10);
		descriptors.put("WorldXSize", pdWorldXSize);
		
		RangePropertyDescriptor pdWorldYSize = new RangePropertyDescriptor("WorldYSize", 0, 50, 10);
		descriptors.put("WorldYSize", pdWorldYSize);
		
		RangePropertyDescriptor pdGrassGrowthRate = new RangePropertyDescriptor("GrassGrowthRate", 0, 50, 10);
		descriptors.put("GrassGrowthRate", pdGrassGrowthRate);
		
		RangePropertyDescriptor pdBirthThreshold = new RangePropertyDescriptor("BirthThreshold", 0, 50, 10);
		descriptors.put("BirthThreshold", pdBirthThreshold);
		
		
		String[] initParams = { "NumAgents", "WorldXSize", "WorldYSize",
				"GrassGrowthRate", "BirthThreshold" };
		return initParams;
	}

	public String getName() {
		return "Rabbits";
	}

	private void addNewAgent() {
		RabbitsGrassSimulationAgent a = new RabbitsGrassSimulationAgent(birthThreshold);
		agentList.add(a);
		rgsSpace.addAgent(a);
		System.out.println("A new agent with "+a.getEnergy()+"is born!");
		a.report();
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
		numAgents = na;
	}

	public int getWorldXSize() {
		return worldXSize;
	}

	public void setWorldXSize(int wxs) {
		if(wxs < 1){
			wxs = WORLDXSIZE;
		}
		worldXSize = wxs;
	}

	public int getWorldYSize() {
		return worldYSize;
	}

	public void setWorldYSize(int wys) {
		
		if(wys < 1){
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
	
	public int getBirthThreshold() {
		return birthThreshold;
	}

	public void setBirthThreshold(int i) {
		birthThreshold = i;
	}

}
