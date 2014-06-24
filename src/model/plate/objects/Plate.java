package model.plate.objects;

import java.awt.Graphics;
import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;

import model.plate.IWellCmd;
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
     * User-given nickname for this plate.
     */
    private String name;

    /**
     * How the plate should order its wells on creation (by row, by column, alphanumeric, etc.)
     */
    private String orderingType;

    /**
     * Dispatcher used to talk to individual wells, should not be serialized to JSON.
     */
    private transient WellDispatcher dispatcher;
	
	/**
	 * Easy constructor that sets the proper parameters.
     * @param name - name to refer to this plate by
	 * @param platePos - location of the top left corner of the plate (in millimeters)
	 * @param plateSpecs - object containing all necessary specifications of the physical plate
     * @param orderingType - type of ordering to make the wells with
	 */
	public Plate(String name, Point2D platePos, PlateSpecifications plateSpecs, String orderingType){
        this.name = name;
		this.TLcorner = platePos;
		this.plateSpecs = plateSpecs;
        this.orderingType = orderingType;
        this.dispatcher = new WellDispatcher();
        addAllWells(dispatcher, orderingType);
	}

    /**
     * Zero arg constructor called when a plate is made via JSON. All variables will be set, but well dispatcher
     * still needs to be set up.
     */
    public Plate() {
        addAllWells(new WellDispatcher(), orderingType);
    }


	
	/**
	 * Called by the main model, draws the plate and its wells on the appropriate view.
	 * @param g - - graphics to draw on
	 * @param sF - scalefactor, how much to stretch this box based on its bounding frame
	 */
	public void paint(final Graphics g, final double sF){
        this.drawPlate(g, sF);
        //tell dispatcher to get all wells to paint
        dispatcher.notifyAll(
                new IWellCmd(){
                    public void apply(Well context, WellDispatcher disp){
                        context.paint(g, sF);
                    }
                }
        );
	}

    /**
     * Get well's location based on input identifer
     * @param identifier String identifying the well
     * @return absolute location of the well
     */
    public Point2D getWellLocation(final String identifier) {

        //wrap the result in an arraylist to allow anonymous inner classes
        final ArrayList<Point2D> returnPoint = new ArrayList<Point2D>();

        //set a default value to return if nothing is found
        returnPoint.add(new Point2D.Double(-1,-1));
        dispatcher.notifyAll(
            new IWellCmd(){
                public void apply(Well context, WellDispatcher disp){
                    if (context.getIdentifier().equals(identifier)) {
                        //if we find a match, overwrite the default value
                        returnPoint.set(0, context.getAbsoluteLocation());
                    }
                }
            }
        );
        //if no well matches the number, tell the user and return a bad location
        if (returnPoint.get(0).equals(new Point2D.Double(-1,-1))){
            System.out.println("Could not find the well with specified number.");
        }
        return returnPoint.get(0);
    }
	
	/**
	 * Draw the bounding box for plate object (physical dimensions, defined by user), as well as its given name
	 * @param g - graphics to draw on
	 * @param sF - scalefactor, how much to stretch this box based on its bounding frame
	 */
	private void drawPlate(Graphics g, double sF){
        //draw the plate border
		g.drawRect((int)Math.round(TLcorner.getX()*sF), (int)Math.round(TLcorner.getY()*sF), 
				   (int)Math.round(plateSpecs.getBorderDimensions().getX()*sF), (int)Math.round(plateSpecs.getBorderDimensions().getY()*sF));
        //write the plate's name, based on the input scale factor for the window
        g.setFont(g.getFont().deriveFont((float) (sF*10)));
        g.drawString(name, (int)Math.round(TLcorner.getX()*sF), (int)Math.round((TLcorner.getY())*sF));
        //TODO: if the plate has an alphanumeric ordering, draw that

	}
	
	/**
	 * Creates all wells on this plate and adds them to the plate's dispatcher.
	 */
	private void addAllWells(WellDispatcher disp, String orderingType){
		switch (orderingType){
            case "ALPHANUMERIC": {
                for (int i = 0; i < plateSpecs.getNumRows(); i++){
                    for (int j = 0; j < plateSpecs.getNumCols(); j++){
                        char rowLetter = (char) (i + 65); //label rows, starting at A
                        String colNumber = Integer.toString(j + 1);
                        addWell(disp, j, i, rowLetter + colNumber);
                    }
                }
            }
			case "ROW": {
                int wellIndex = 1;
				for (int i = 0; i < plateSpecs.getNumRows(); i++){
					for (int j = 0; j < plateSpecs.getNumCols(); j++){
						addWell(disp, j, i, Integer.toString(wellIndex));
						wellIndex++;
					}
				}
				break;
			}
			case "COLUMN": {
                int wellIndex = 1;
				for (int i = 0; i < plateSpecs.getNumCols(); i++){
					for (int j = 0; j < plateSpecs.getNumRows(); j++){
						addWell(disp, i, j, Integer.toString(wellIndex));
						wellIndex++;
					}
				}
				break;
			}
			default: System.out.println("Did not recognize numbering order, did not add wells.");
		}
	}
	
	/**
	 * Helper function for addAllWells, adds the specified well to the grid location.
	 * @param disp - dispatcher to add well with
	 * @param i - row the well is in
	 * @param j - column the well is in
     * @param identifier - how to identify this well
	 */
	private void addWell(WellDispatcher disp, int i, int j, String identifier){
		Point2D unroundedLocation = new Point2D.Double(
				plateSpecs.getWellCorner().getX() + i*plateSpecs.getWellSpacing(),
				plateSpecs.getWellCorner().getY() + j*plateSpecs.getWellSpacing());
		disp.addObserver(new Well(this, unroundedLocation, plateSpecs.getWellDiameter(), identifier));
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

    /**
     * @return this plate's name
     */
    public String getName() { return name; }
}

