package org.geworkbenchweb.genspace.ui.component;

import org.vaadin.artur.icepush.ICEPush;

import com.vaadin.ui.CustomComponent;

public abstract class SocialPanel extends CustomComponent{
		
	public abstract String getPanelTitle();
	public abstract void updatePanel();
	public abstract void attachPusher();
}