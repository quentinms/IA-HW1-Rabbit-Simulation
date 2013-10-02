import java.awt.Color;

import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;
import uchicago.src.sim.space.Object2DGrid;

/**
 * Class that implements the simulation agent for the rabbits grass simulation.
 * 
 *  ( Y)
 *  ( . .)
 *  o(")(")
 */

public class RabbitsGrassSimulationAgent implements Drawable {

	private int x;
	private int y;
	private int energy;
	private static int IDNumber = 0;
	private int ID;
	
	private int grassEnergy; //represents how efficient the rabbit is at digesting grass.

	private RabbitsGrassSimulationSpace rgsSpace;
	

	public RabbitsGrassSimulationAgent(int initialEnergy, int grassEnergy) {
		x = -1;
		y = -1;
		energy = initialEnergy;
		IDNumber++;
		ID = IDNumber;
		this.grassEnergy = grassEnergy;
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
		System.out.println(getID() + " at (" + x + "," + y + ") has "
				+ getEnergy() + " energy");
	}

	public void step() {
		
			int direction = (int) (Math.random() * 4);
		
			int vX = 0;
			int vY = 0;
			
			int newX = x;
			int newY = y;
			
			Object2DGrid grid = rgsSpace.getCurrentAgentSpace();
			
			int count_move_tries = 0;
			
			do{
			direction = (direction + 1) % 4;
			switch (direction) {
				case 0: vX = 1; vY = 0; break;
				case 1: vX = 0; vY = 1; break;
				case 2: vX = -1; vY = 0; break;
				case 3: vX = 0; vY = -1; break;
			}
			
			newX = (x + vX + grid.getSizeX()) % grid.getSizeX();
			newY = (y + vY + grid.getSizeY()) % grid.getSizeY();
			 
			count_move_tries++;
			
			if(count_move_tries == 4){
				System.out.println("Rabbit "+getID()+" is blocked in ("+x+","+y+")");
			}
			
			}while(!tryMove(newX, newY) && count_move_tries < 4);
	
		
		//Eating
		energy = energy + (rgsSpace.eatGrassAt(x, y) * grassEnergy);
		
	
		energy = energy - 1;
	}
	
	private boolean tryMove(int newX, int newY) {
		return rgsSpace.moveAgentAt(x, y, newX, newY);
	}

}
