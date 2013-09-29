import java.awt.Color;
import java.awt.Image;
import java.awt.Toolkit;

import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;
import uchicago.src.sim.space.Object2DGrid;

/**
 * Class that implements the simulation agent for the rabbits grass simulation.
 * 
 * @author
 */

public class RabbitsGrassSimulationAgent implements Drawable {

	private int x;
	private int y;
	private int vX;
	private int vY;
	private int energy;
	private static int IDNumber = 0;
	private int ID;

	private RabbitsGrassSimulationSpace rgsSpace;
	
	private static final int GRASS_ENERGY  = 2;

	public RabbitsGrassSimulationAgent(int initialEnergy) {
		x = -1;
		y = -1;
		energy = initialEnergy;
		//setVxVy();
		IDNumber++;
		ID = IDNumber;
	}

	private void setDirection() {
		vX = 0;
		vY = 0;
		int direction = (int) (Math.random() * 4);
		
		switch (direction) {
		case 0: vX = 1; vY = 0; break;
		case 1: vX = 0; vY = 1; break;
		case 2: vX = -1; vY = 0; break;
		case 3: vX = 0; vY = -1; break;

		default:
			break;
		}
		
	}

	public void draw(SimGraphics G) {

		
		G.drawFastCircle(Color.PINK);

	}

	public int getX() {
		return x;
	}

	public void setRabbitGrassSimulatorSpace(RabbitsGrassSimulationSpace cds) {
		rgsSpace = cds;
	}

	public int getY() {
		return y;
	}

	public void setXY(int newX, int newY) {
		x = newX;
		y = newY;
	}

	public String getID() {
		return "R-" + ID;
	}

	public int getEnergy() {
		return energy;
	}
	
	public void setEnergy(int nrj) {
		energy = nrj;
	}

	
	public void report() {
		System.out.println(getID() + " at " + x + ", " + y + " has "
				+ getEnergy() + " energy");
	}

	public void step() {
		
		
		//TODO direction
		setDirection();
		int newX = x + vX;
		int newY = y + vY;

		Object2DGrid grid = rgsSpace.getCurrentAgentSpace();
		newX = (newX + grid.getSizeX()) % grid.getSizeX();
		newY = (newY + grid.getSizeY()) % grid.getSizeY();

		if (tryMove(newX, newY)) {
			energy = energy + (rgsSpace.eatGrassAt(x, y)*GRASS_ENERGY);
		} 
		
		
		energy = energy - 1;
	}
	
	private boolean tryMove(int newX, int newY) {
		return rgsSpace.moveAgentAt(x, y, newX, newY);
	}

}
