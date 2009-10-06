package org.mmisw.ontmd.gwt.client.portal.wizard;


import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.Widget;

/**
 * Base class for the wizard-like pages.
 * 
 * TODO complete implementation
 * 
 * @author Carlos Rueda
 */
public abstract class WizardPageBase  {
	
	protected final WizardBase wizard;
	
	private final DecoratorPanel decPanel = new DecoratorPanel();
	private final FlexTable flexTable = new FlexTable();

	
	private final HorizontalPanel container = new HorizontalPanel();
	
	private final HorizontalPanel buttonsPanel = new HorizontalPanel();
	
	protected final PushButton backButton = new PushButton("< Back", new ClickListener() {
		public void onClick(Widget sender) {
			if ( preCheckBackClicked() ) {
				wizard.pageBack(WizardPageBase.this);
			}
		}
	});
	
	protected final PushButton nextButton = new PushButton("Next >", new ClickListener() {
		public void onClick(Widget sender) {
			if ( preCheckNextClicked() ) {
				wizard.pageNext(WizardPageBase.this);
			}
		}
	});
	
	protected final PushButton finishButton = new PushButton("Finish", new ClickListener() {
		public void onClick(Widget sender) {
			if ( preCheckFinishClicked() ) {
				wizard.finish(WizardPageBase.this);
			}
		}
	});
	
	protected HTML statusHtml = new HTML("");
	
	private boolean includeBack;
	private boolean includeNext;
	private boolean includeFinish;
	
	protected WizardPageBase(WizardBase wizard,
			boolean includeBack, boolean includeNext
	) {
		this(wizard, includeBack, includeNext, false);
	}

	protected WizardPageBase(WizardBase wizard,
			boolean includeBack, boolean includeNext, boolean includeFinish
	) {
		this.wizard = wizard;
		this.includeBack = includeBack;
		this.includeNext = includeNext;
		this.includeFinish = includeFinish;
		
		statusHtml.setHeight("50px");
		
		decPanel.setWidget(flexTable); // container);
		
		buttonsPanel.setSpacing(6);
		
		flexTable.setWidth("650px");
//		flexTable.setBorderWidth(1);
		int row = 0;

		_prepareButtons();
		
		container.setWidth("650px");
		
	    flexTable.setWidget(row, 0, container);
		flexTable.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_MIDDLE
		);
	    row++;

	    flexTable.setWidget(row, 0, statusHtml);
		flexTable.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);
	    row++;

		flexTable.setWidget(row, 0, buttonsPanel);
		flexTable.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		row++;
	}
	
	protected WizardBase getWizard() {
		return wizard;
	}
	
	public Widget getWidget() {
		HorizontalPanel w = new HorizontalPanel();
		w.setSpacing(6);
		w.add(decPanel);
		return w;
	}
	
	/**
	 * A subclass calls this to define the contents of the page.
	 * @param contents
	 */
	protected void addContents(Widget contents) {
		container.clear();
		container.add(contents);
	}

	/**
	 * If this method returns true, then wizard.pageBack(WizardPageBase.this) is called upon the
	 * click of the Back button.
	 * @return true in this base class.
	 */
	protected boolean preCheckBackClicked() {
		return true;
	}
	
	/**
	 * If this method returns true, then wizard.pageNext(WizardPageBase.this) is called upon the
	 * click of the Next button.
	 * @return true in this base class.
	 */
	protected boolean preCheckNextClicked() {
		return true;
	}
	
	/**
	 * If this method returns true, then wizard.finish(WizardPageBase.this) is called upon the
	 * click of the Finish button.
	 * @return true in this base class.
	 */
	protected boolean preCheckFinishClicked() {
		return true;
	}
	
	private void _prepareButtons() {
		backButton.setEnabled(includeBack);
		nextButton.setEnabled(includeNext);
		finishButton.setEnabled(includeFinish);
		
		buttonsPanel.add(backButton);
//		backButton.setTitle("TODO");
		
		buttonsPanel.add(nextButton);
//		nextButton.setTitle("TODO");
		
		buttonsPanel.add(finishButton);
//		finishButton.setTitle("TODO");
	}
	
	protected void enable(boolean enabled) {
		backButton.setEnabled(includeBack && enabled);
		nextButton.setEnabled(includeNext && enabled);
		finishButton.setEnabled(includeFinish && enabled);
	}

}
