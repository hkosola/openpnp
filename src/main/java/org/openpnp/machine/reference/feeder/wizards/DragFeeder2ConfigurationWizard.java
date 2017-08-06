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

package org.openpnp.machine.reference.feeder.wizards;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.openpnp.gui.MainFrame;
import org.openpnp.gui.components.CameraView;
import org.openpnp.gui.components.ComponentDecorators;
import org.openpnp.gui.components.LocationButtonsPanel;
import org.openpnp.gui.support.BufferedImageIconConverter;
import org.openpnp.gui.support.DoubleConverter;
import org.openpnp.gui.support.IntegerConverter;
import org.openpnp.gui.support.LengthConverter;
import org.openpnp.gui.support.MessageBoxes;
import org.openpnp.gui.support.MutableLocationProxy;
import org.openpnp.gui.support.PercentConverter;
import org.openpnp.machine.reference.feeder.DragFeeder2;
import org.openpnp.model.Configuration;
import org.openpnp.spi.Camera;
import org.openpnp.util.UiUtils;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;

@SuppressWarnings("serial")
public class DragFeeder2ConfigurationWizard
        extends AbstractReferenceFeederConfigurationWizard {
    private final DragFeeder2 feeder;

    private JTextField textFieldFeedStartX;
    private JTextField textFieldFeedStartY;
    private JTextField textFieldFeedStartZ;
    private JTextField textFieldFeedRate;
    private JLabel lblActuatorId;
    private JTextField textFieldActuatorId;
    private JPanel panelGeneral;
    private JPanel panelLocations;
    private LocationButtonsPanel locationButtonsPanelFeedStart;

    public DragFeeder2ConfigurationWizard(DragFeeder2 feeder) {
        super(feeder);
        this.feeder = feeder;

        JPanel panelFields = new JPanel();
        panelFields.setLayout(new BoxLayout(panelFields, BoxLayout.Y_AXIS));

        panelGeneral = new JPanel();
        panelGeneral.setBorder(new TitledBorder(null, "General Settings", TitledBorder.LEADING,
                TitledBorder.TOP, null, null));

        panelFields.add(panelGeneral);
        panelGeneral.setLayout(new FormLayout(
                new ColumnSpec[] {FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC,
                        FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC,},
                new RowSpec[] {FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
                        FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,}));

        JLabel lblFeedRate = new JLabel("Feed Speed %");
        panelGeneral.add(lblFeedRate, "2, 2");

        textFieldFeedRate = new JTextField();
        panelGeneral.add(textFieldFeedRate, "4, 2");
        textFieldFeedRate.setColumns(5);

        lblActuatorId = new JLabel("Actuator Name");
        panelGeneral.add(lblActuatorId, "2, 4, right, default");

        textFieldActuatorId = new JTextField();
        panelGeneral.add(textFieldActuatorId, "4, 4");
        textFieldActuatorId.setColumns(5);

        panelLocations = new JPanel();
        panelFields.add(panelLocations);
        panelLocations.setBorder(new TitledBorder(null, "Locations", TitledBorder.LEADING,
                TitledBorder.TOP, null, null));
        panelLocations.setLayout(new FormLayout(new ColumnSpec[] {
        		FormSpecs.RELATED_GAP_COLSPEC,
        		FormSpecs.DEFAULT_COLSPEC,
        		FormSpecs.RELATED_GAP_COLSPEC,
        		ColumnSpec.decode("max(30dlu;default)"),
        		FormSpecs.RELATED_GAP_COLSPEC,
        		FormSpecs.DEFAULT_COLSPEC,
        		FormSpecs.RELATED_GAP_COLSPEC,
        		FormSpecs.DEFAULT_COLSPEC,
        		FormSpecs.RELATED_GAP_COLSPEC,
        		ColumnSpec.decode("left:default:grow"),},
        	new RowSpec[] {
        		FormSpecs.RELATED_GAP_ROWSPEC,
        		FormSpecs.DEFAULT_ROWSPEC,
        		FormSpecs.RELATED_GAP_ROWSPEC,
        		FormSpecs.DEFAULT_ROWSPEC,
        		FormSpecs.RELATED_GAP_ROWSPEC,
        		FormSpecs.DEFAULT_ROWSPEC,
        		FormSpecs.RELATED_GAP_ROWSPEC,
        		FormSpecs.DEFAULT_ROWSPEC,
        		FormSpecs.RELATED_GAP_ROWSPEC,
        		FormSpecs.DEFAULT_ROWSPEC,
        		FormSpecs.RELATED_GAP_ROWSPEC,
        		FormSpecs.DEFAULT_ROWSPEC,
        		FormSpecs.RELATED_GAP_ROWSPEC,
        		FormSpecs.DEFAULT_ROWSPEC,}));

        JLabel lblX = new JLabel("X");
        panelLocations.add(lblX, "4, 4");

        JLabel lblY = new JLabel("Y");
        panelLocations.add(lblY, "6, 4");

        JLabel lblZ = new JLabel("Z");
        panelLocations.add(lblZ, "8, 4");

        JLabel lblFeedStartLocation = new JLabel("Feed Start Location");
        panelLocations.add(lblFeedStartLocation, "2, 6, right, default");

        textFieldFeedStartX = new JTextField();
        panelLocations.add(textFieldFeedStartX, "4, 6");
        textFieldFeedStartX.setColumns(8);

        textFieldFeedStartY = new JTextField();
        panelLocations.add(textFieldFeedStartY, "6, 6");
        textFieldFeedStartY.setColumns(8);

        textFieldFeedStartZ = new JTextField();
        panelLocations.add(textFieldFeedStartZ, "8, 6");
        textFieldFeedStartZ.setColumns(8);

        locationButtonsPanelFeedStart = new LocationButtonsPanel(textFieldFeedStartX,
                textFieldFeedStartY, textFieldFeedStartZ, null);
        panelLocations.add(locationButtonsPanelFeedStart, "10, 6");

        JLabel lblFeedEndLocation = new JLabel("Feed Direction");
        panelLocations.add(lblFeedEndLocation, "2, 8, right, default");
        
        cbDirection = new JComboBox();
        cbDirection.setModel(new DefaultComboBoxModel(new String[] {"+X", "+Y", "-X", "-Y"}));
        cbDirection.setSelectedIndex(0);
        panelLocations.add(cbDirection, "4, 8, fill, default");
        
        lblNewLabel = new JLabel("Feed Distance");
        lblNewLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        panelLocations.add(lblNewLabel, "2, 10, right, default");
        
        tfFeedDistance = new JTextField();
        tfFeedDistance.setText("4");
        panelLocations.add(tfFeedDistance, "4, 10");
        tfFeedDistance.setColumns(10);
        
        lblNewLabel_2 = new JLabel("mm");
        panelLocations.add(lblNewLabel_2, "6, 10, default, center");
        
        lblBackoffDistance = new JLabel("Backoff Distance");
        panelLocations.add(lblBackoffDistance, "2, 12, right, default");
        
        backoffDistTf = new JTextField();
        panelLocations.add(backoffDistTf, "4, 12");
        backoffDistTf.setColumns(10);
        
        label = new JLabel("mm");
        panelLocations.add(label, "6, 12");
        
        lblNewLabel_1 = new JLabel("Component Pitch");
        panelLocations.add(lblNewLabel_1, "2, 14, right, default");
        
        tfComponentPitch = new JTextField();
        panelLocations.add(tfComponentPitch, "4, 14");
        tfComponentPitch.setColumns(10);
        
        label_1 = new JLabel("mm");
        panelLocations.add(label_1, "6, 14");

        contentPanel.add(panelFields);
    }

    @Override
    public void createBindings() {
        super.createBindings();
        LengthConverter lengthConverter = new LengthConverter();
        IntegerConverter intConverter = new IntegerConverter();
        DoubleConverter doubleConverter =
                new DoubleConverter(Configuration.get().getLengthDisplayFormat());
        BufferedImageIconConverter imageConverter = new BufferedImageIconConverter();
        PercentConverter percentConverter = new PercentConverter();

        addWrappedBinding(feeder, "feedSpeed", textFieldFeedRate, "text", percentConverter);
        addWrappedBinding(feeder, "actuatorName", textFieldActuatorId, "text");

        MutableLocationProxy feedStartLocation = new MutableLocationProxy();
        bind(UpdateStrategy.READ_WRITE, feeder, "feedStartLocation", feedStartLocation, "location");
        addWrappedBinding(feedStartLocation, "lengthX", textFieldFeedStartX, "text", lengthConverter);
        addWrappedBinding(feedStartLocation, "lengthY", textFieldFeedStartY, "text", lengthConverter);
        addWrappedBinding(feedStartLocation, "lengthZ", textFieldFeedStartZ, "text", lengthConverter);

        addWrappedBinding(feeder, "backoffDistance_mm", backoffDistTf, "text", doubleConverter);
        addWrappedBinding(feeder, "feedDistance_mm", tfFeedDistance, "text", doubleConverter);
        addWrappedBinding(feeder, "feedDirection", cbDirection, "selectedItem");
        addWrappedBinding(feeder, "componentPitch_mm", tfComponentPitch, "text", intConverter);
        
        

        ComponentDecorators.decorateWithAutoSelectAndLengthConversion(textFieldFeedRate);
        ComponentDecorators.decorateWithAutoSelect(textFieldActuatorId);
        ComponentDecorators.decorateWithAutoSelectAndLengthConversion(textFieldFeedStartX);
        ComponentDecorators.decorateWithAutoSelectAndLengthConversion(textFieldFeedStartY);
        ComponentDecorators.decorateWithAutoSelectAndLengthConversion(textFieldFeedStartZ);
        ComponentDecorators.decorateWithAutoSelectAndLengthConversion(backoffDistTf);
        ComponentDecorators.decorateWithAutoSelectAndLengthConversion(tfFeedDistance);
        ComponentDecorators.decorateWithAutoSelect(tfComponentPitch);

        bind(UpdateStrategy.READ, feeder, "actuatorName", locationButtonsPanelFeedStart, "actuatorName");
    }


    private JLabel lblBackoffDistance;
    private JTextField backoffDistTf;
    private JLabel lblNewLabel;
    private JComboBox cbDirection;
    private JTextField tfFeedDistance;
    private JLabel lblNewLabel_1;
    private JTextField tfComponentPitch;
    private JLabel lblNewLabel_2;
    private JLabel label;
    private JLabel label_1;
}
