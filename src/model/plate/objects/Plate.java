package model.plate.objects;

import java.awt.Graphics;
import java.awt.geom.Point2D;
import java.io.Serializable;
import model.plate.WellDispatcher;

/**
 * Class representing which plates are present in the system, as well as where they are.
 * @author Christian
 *
 */
public class Plate implements Serializable {
	
	/**
	 * Auto generated serial ID to be able to save the Plate.
	 */
	private static final long serialVersionUID = -5180528423168711163L;

	/**
	 * Where to position the bottom left corner of the plate.
	 */
	private Point2D TLcorner;
	
	/**
	 * Wrapper class that encompasses all necessary specifications of the physical plate.
	 */
	private PlateSpecifications plateSpecs;
	
	/**
	 * Enum that specifies how to label wells with numbers.
	 */
	private String orderingType;
	
	/**
	 * Easy constructor that sets the proper parameters.
	 * @param platePos - location of the top left corner of the plate (in millimeters)
	 * @param plateSpecs - object containing all necessary specifications of the physical plate
	 */
	public Plate(Point2D platePos, PlateSpecifications plateSpecs, String orderingType){
		TLcorner = platePos;
		this.plateSpecs = plateSpecs;
		this.orderingType = orderingType;
	}
	
	/**
	 * Called by the main model, draws the plate on the appropriate view.
	 * @param g
	 * @param sF - scalefactor, how much to stretch this box based on its bounding frame
	 */
	public void paint(Graphics g, double sF){
		this.drawBorder(g, sF);
	}
	
	/**
	 * Draw the bounding box for plate object (physical dimensions, defined by user)
	 * @param g - graphics to draw on
	 * @param sF - scalefactor, how much to stretch this box based on its bounding frame
	 */
	public void drawBorder(Graphics g, double sF){
		g.drawRect((int)Math.round(TLcorner.getX()*sF), (int)Math.round(TLcorner.getY()*sF), 
				   (int)Math.round(plateSpecs.getBorderDimensions().getX()*sF), (int)Math.round(plateSpecs.getBorderDimensions().getY()*sF));
	}
	
	/**
	 * Called by the model, adds all wells to the main dispatcher.
	 * @return total number of wells after the plate is added
	 */
	public int addAllWells(WellDispatcher disp, int startingNumber){
		int wellIndex = startingNumber;
		switch (orderingType){
			case "ROW": {
				for (int i = 0; i < plateSpecs.getNumRows(); i++){
					for (int j = 0; j < plateSpecs.getNumCols(); j++){
						addWell(disp, j, i, wellIndex);
						wellIndex++;
					}
				}
				break;
			}
			case "COLUMN": {
				for (int i = 0; i < plateSpecs.getNumCols(); i++){
					for (int j = 0; j < plateSpecs.getNumRows(); j++){
						addWell(disp, i, j, wellIndex);
						wellIndex++;
					}
				}
				break;
			}
			default: System.out.println("Did not recognize numbering order, did not add wells.");
		}
		
		return wellIndex;
	}
	
	/**
	 * Helper function for addAllWells, adds the specified well to the grid location.
	 * @param disp - dispatcher to add well with
	 * @param i - row the well is in
	 * @param j - column the well is in
	 */
	private void addWell(WellDispatcher disp, int i, int j, int currentIndex){
		Point2D unroundedLocation = new Point2D.Double(
				plateSpecs.getWellCorner().getX() + i*plateSpecs.getWellSpacing(),
				plateSpecs.getWellCorner().getY() + j*plateSpecs.getWellSpacing());
		disp.addObserver(new Well(this, unroundedLocation, plateSpecs.getWellDiameter(), currentIndex));
	}
	
	/**
	 * @return top left corner of the plate, in cm
	 */
	public Point2D getTLcorner(){
		return TLcorner;
	}
	
	/**
	 * @return the maximum volume per well, as defined by plate specifications
	 */
	public double getMaxWellVolume(){
		return plateSpecs.getWellVolume();
	}
}

