/*
 * Copyright (C) 2011 Jason von Nieda <jason@vonnieda.org>
 * 
 * This file is part of OpenPnP.
 * 
 * OpenPnP is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * OpenPnP is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with OpenPnP. If not, see
 * <http://www.gnu.org/licenses/>.
 * 
 * For more information about OpenPnP visit http://openpnp.org
 */

package org.openpnp.machine.reference.feeder;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.Action;

import org.openpnp.ConfigurationListener;
import org.openpnp.gui.support.Wizard;
import org.openpnp.machine.reference.ReferenceFeeder;
import org.openpnp.machine.reference.feeder.wizards.DragFeeder2ConfigurationWizard;
import org.openpnp.model.Configuration;
import org.openpnp.model.Length;
import org.openpnp.model.LengthUnit;
import org.openpnp.model.Location;
import org.openpnp.model.Rectangle;
import org.openpnp.spi.Actuator;
import org.openpnp.spi.Camera;
import org.openpnp.spi.Head;
import org.openpnp.spi.Nozzle;
import org.openpnp.spi.PropertySheetHolder;
import org.openpnp.spi.VisionProvider;
import org.openpnp.util.Utils2D;
import org.pmw.tinylog.Logger;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.core.Persist;

/**

 */
public class DragFeeder2 extends ReferenceFeeder {


    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    @Element
    protected Location feedStartLocation = new Location(LengthUnit.Millimeters);
    @Element
    protected Location feedEndLocation = new Location(LengthUnit.Millimeters);
    @Element(required = false)
    protected double feedSpeed = 1.0;
    @Attribute(required = false)
    protected String actuatorName;
    @Element(required = false)
    protected double backoffDistance_mm = 0.15;    
    @Element(required = false)
    protected double feedDistance_mm = 4.2;
    @Element(required = false)
    protected String feedDirection = "-Y";
    @Element(required = false)
    protected int componentPitch_mm = 4; 
    
    protected Location pickLocation;

    protected Location visionOffset;

    @Override
    public Location getPickLocation() throws Exception {
        if (pickLocation == null) {
            pickLocation = location;
        }
        return pickLocation;
    }

    
    private int pickPoint = 0;
    
    
    @Override
    public void feed(Nozzle nozzle) throws Exception {
        Logger.debug("feed({})", nozzle);

        if (actuatorName == null) {
            throw new Exception("No actuator name set.");
        }


        Head head = nozzle.getHead();

        /*
         * TODO: We can optimize the feed process: If we are already higher than the Z we will move
         * to to index plus the height of the tape, we don't need to Safe Z first. There is also
         * probably no reason to Safe Z after extracting the pin since if the tool was going to hit
         * it would have already hit.
         */

        Actuator actuator = head.getActuatorByName(actuatorName);

        if (actuator == null) {
            throw new Exception(String.format("No Actuator found with name %s on feed Head %s",
                    actuatorName, head.getName()));
        }

        
        // calculate do we need feed operation
        pickPoint -= this.componentPitch_mm;
        
        
       int dX = 0;
       int dY = 0;
       if(this.feedDirection.equals("+X")) { dX =  1; }
       if(this.feedDirection.equals("-X")) { dX = -1; }
       if(this.feedDirection.equals("+Y")) { dY =  1; }
       if(this.feedDirection.equals("-Y")) { dY = -1; }

    	        
       if(pickPoint>=0) {
    	   // calculate new picklocation (component pitch is less than hole pitch)
           Location lp = new Location(LengthUnit.Millimeters, pickPoint * dX, pickPoint * dY, 0,0);
           pickLocation = this.location.add(lp);
    	   return; // no need for drag-operation
       }

       // calculate feed moves
       
       Location lf = new Location(LengthUnit.Millimeters,  
    		   						dX * this.feedDistance_mm,
    		   						dY * this.feedDistance_mm,
    		   						0,0);
       Location lb = new Location(LengthUnit.Millimeters, 
    		   					  -dX * this.backoffDistance_mm,
    		   					  -dY * this.backoffDistance_mm,
    		   					  0,0);
       
        head.moveToSafeZ();

        // set rotation for pickup, 
        // plenty of time to rotate nozzle when we are traveling to the drag position
        
        Location feedStartLocation = this.feedStartLocation.derive(null, null, null, this.location.getRotation());
        Location feedEndLocation = feedStartLocation.add(lf);
        
        // in practice, the feed distance must be multiple of hole pitch (4mm)
        pickPoint += (int)(feedDistance_mm + 0.5);
        Location lp = new Location(LengthUnit.Millimeters, pickPoint * dX, pickPoint * dY, 0,0);
        pickLocation = this.location.add(lp);

        // Move the actuator to the feed start location.
        actuator.moveTo(feedStartLocation.derive(null, null, Double.NaN, null));

        // extend the pin
        actuator.actuate(true);

        // insert the pin
        actuator.moveTo(feedStartLocation);

        // drag the tape
        actuator.moveTo(feedEndLocation, feedSpeed * actuator.getHead().getMachine().getSpeed());
        
        // backoff to release tension from the pin
        if (backoffDistance_mm != 0) {
            Location backoffLocation = feedEndLocation.add(lb);
            actuator.moveTo(backoffLocation, feedSpeed * actuator.getHead().getMachine().getSpeed());
        }
        
        head.moveToSafeZ();

        // retract the pin
        actuator.actuate(false);

        Logger.debug("Modified pickLocation {}", pickLocation);
    }


    @Override
    public String toString() {
        return String.format("ReferenceTapeFeeder id %s", id);
    }

    public Location getFeedStartLocation() {
        return feedStartLocation;
    }

    public void setFeedStartLocation(Location feedStartLocation) {
        this.feedStartLocation = feedStartLocation;
    }

    public Double getFeedSpeed() {
        return feedSpeed;
    }

    public void setFeedSpeed(Double feedSpeed) {
        this.feedSpeed = feedSpeed;
    }

    public String getActuatorName() {
        return actuatorName;
    }

    public void setActuatorName(String actuatorName) {
        String oldValue = this.actuatorName;
        this.actuatorName = actuatorName;
        propertyChangeSupport.firePropertyChange("actuatorName", oldValue, actuatorName);
    }

    public double getBackoffDistanceMm() {
        return backoffDistance_mm;
    }

    public void setBackoffDistanceMm(double backoffDistance_mm) {
        this.backoffDistance_mm = backoffDistance_mm;
    }

    public double getFeedDistanceMm() { 
    	return feedDistance_mm;
    }
    public void setFeedDistanceMm(double feedDistance_mm) {
    	this.feedDistance_mm = feedDistance_mm;
    }

    public String getFeedDirection() {
    	return feedDirection;
    }
    public void setFeedDirection(String direction) {
    	this.feedDirection = direction;
    }
    
    public int getComponentPitchMm() {
    	return componentPitch_mm;
    }
    public void setComponentPitchMm(int pitch_mm) {
    	this.componentPitch_mm = pitch_mm;
    }
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
    }

    @Override
    public Wizard getConfigurationWizard() {
        return new DragFeeder2ConfigurationWizard(this);
    }

    @Override
    public String getPropertySheetHolderTitle() {
        return getClass().getSimpleName() + " " + getName();
    }

    @Override
    public PropertySheetHolder[] getChildPropertySheetHolders() {
        return null;
    }

    @Override
    public Action[] getPropertySheetHolderActions() {
        return null;
    }

}
