/*
 * Copyright 2011 CAST, Inc.
 *
 * This file is part of the UDL Curriculum Toolkit:
 * see <http://code.google.com/p/udl-curriculum-toolkit>.
 *
 * The UDL Curriculum Toolkit is free software: you can redistribute and/or
 * modify it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * The UDL Curriculum Toolkit is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cast.isi.page;

import java.util.List;

import lombok.Getter;

import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.cast.cwm.components.ClassAttributeModifier;
import org.cast.cwm.data.Prompt;
import org.cast.cwm.xml.XmlSectionModel;
import org.cast.cwm.xml.transform.FilterElements;
import org.cast.isi.ISIApplication;
import org.cast.isi.ISISession;
import org.cast.isi.ISIXmlComponent;
import org.cast.isi.ISIXmlSection;
import org.cast.isi.ResponseViewerFactory;
import org.cast.isi.data.ContentLoc;
import org.cast.isi.data.ISIPrompt;
import org.cast.isi.data.ISIResponse;
import org.cast.isi.data.PromptType;
import org.cast.isi.data.ScoreCounts;
import org.cast.isi.panel.PeriodResponseListPanel;
import org.cast.isi.panel.ResponseCollectionSummary;
import org.cast.isi.service.IFeatureService;
import org.cast.isi.service.IISIResponseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.inject.Inject;

/**
 * Given a prompt id and period, list all the user responses
 * 
 * @author lynnmccormack
 *
 */
@AuthorizeInstantiation("TEACHER")
public class PeriodResponsePage extends ISIBasePage implements IHeaderContributor {

	private long promptId;
	protected String pageTitleEnd = null;

	@Getter protected boolean showNames = false;
	
	@Inject
	private IISIResponseService responseService;

	@Inject
	private IFeatureService featureService;

	protected static final Logger log = LoggerFactory.getLogger(PeriodResponsePage.class);

	private static final ResponseViewerFactory factory = new ResponseViewerFactory();

	public PeriodResponsePage(final PageParameters parameters) {
		super(parameters);

		pageTitleEnd = (new StringResourceModel("Compare.pageTitle", this, null, "Compare").getString());
		setPageTitle(pageTitleEnd);
		add(new Label("pageTitle", new PropertyModel<String>(this, "pageTitle")));
		add(new Label("periodName", ISISession.get().getCurrentPeriodModel().getObject().getName()));

		add(ISIApplication.get().getToolbar("tht", this));
		
		add(new NameDisplayToggleLink("hideNamesLink").add(new Label("hideNamesText", new AbstractReadOnlyModel<String>() {
			private static final long serialVersionUID = 1L;
			@Override
			public String getObject() {
				if (showNames) {
					return new ResourceModel("Compare.buttonTitle.hideNames").getObject();
				} else {
					return new ResourceModel("Compare.buttonTitle.showNames").getObject();
				}
			}			
		})).setOutputMarkupId(true));

		// prompt id is sent in via parameters, get the prompt for this id
		if (parameters.containsKey("promptId")) {
			promptId = (parameters.getLong("promptId"));
		} 

		// get the prompt for this id
		IModel<Prompt> mPrompt = responseService.getPromptById(promptId);
		ISIPrompt prompt = (ISIPrompt) mPrompt.getObject();

		// Add the crumb trail, link and icon link to the page where this response is located
		String crumbTrail = prompt.getContentElement().getContentLocObject().getSection().getCrumbTrailAsString(1, 1);
		add(new Label("crumbTrail", crumbTrail));
		// TODO: should target link to main window, not in popup
		BookmarkablePageLink<ISIStandardPage> link = new SectionLinkFactory().linkToPage("titleLink", prompt.getContentElement().getContentLocObject().getSection());
		link.add(new Label("title", prompt.getContentElement().getContentLocObject().getSection().getTitle()));
		link.add(new ClassAttributeModifier("sectionLink"));
		add(link);
		add(ISIApplication.get().iconFor(prompt.getContentElement().getContentLocObject().getSection().getSectionAncestor()));		
		add(makeSummary("promptResponseSummary", mPrompt));
		
		// Add the text associated with Prompt
		add(factory.makeQuestionTextComponent("question", prompt));
		
		addDetailsPanel(mPrompt);
	}

	private Component makeSummary(String id, IModel<Prompt> mPrompt) {
		if (featureService.isCompareScoreSummaryOn() && validAnswers(mPrompt))
			return new ResponseCollectionSummary(id, getScoreCounts(mPrompt));
		else return new EmptyPanel(id);
	}
	
	/**
	 * Determine if this is a single select with a correct answer.  If
	 * there are no correct answers, don't display the summary.
	 * 
	 * @param mPrompt
	 * @return
	 */
	private boolean validAnswers(IModel<Prompt> mPrompt) {
		ISIPrompt prompt = ((ISIPrompt)mPrompt.getObject()); 
		if (prompt.getType().equals(PromptType.SINGLE_SELECT)) {
			ContentLoc promptLoc = prompt.getContentLoc();
			String xmlId = prompt.getContentElement().getXmlId();
			Element xmlElement = promptLoc.getSection().getElement().getOwnerDocument().getElementById(xmlId);
			NodeList items = xmlElement.getElementsByTagName("item");
			 for(int i=0; i < items.getLength(); i++){
				 Node node = (Node) items.item(i);
				 Element itemElement =  (Element) node;
				 if (itemElement.getAttributeNS(null, "correct").equals("true")) {
					 return true; // correct answer found
				 }
			 }	
			 return false; // no correct answer found
		}
		return true; // not a single select
	}

	private ScoreCounts getScoreCounts(IModel<Prompt> mPrompt) {
		IModel<List<ISIResponse>> responses =  responseService.getPeriodResponsesForPrompt(mPrompt, ISISession.get().getCurrentPeriodModel());
		return ScoreCounts.forResponses("students", responses);
	}

	protected void addDetailsPanel(IModel<Prompt> mPrompt) {		
		ISIPrompt prompt = ((ISIPrompt)mPrompt.getObject()); 
		if (prompt.getType().equals(PromptType.SINGLE_SELECT)) {
			ContentLoc location = prompt.getContentElement().getContentLocObject();
			ISIXmlSection section = location.getSection();
			String xmlId = prompt.getContentElement().getXmlId();
			ISIXmlComponent xml = new ISIXmlComponent("details", new XmlSectionModel(section), "compare-responses");
			xml.setTransformParameter(FilterElements.XPATH, String.format("//dtb:responsegroup[@id='%s']", xmlId));
			xml.setOutputMarkupId(true);
			add (xml);
		} else {
			add (new PeriodResponseListPanel("details", mPrompt));
		}
	}

	protected WebMarkupContainer getDetailsPanel() {
		return (WebMarkupContainer) get("details");
	}
	
	@Override
	public String getPageName() {
		return null;
	}

	@Override
	public String getPageType() {
		return "compare";
	}

	@Override
	public String getPageViewDetail() {
		return String.valueOf(promptId);
	}

	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		renderThemeCSS(response, "css/window.css");
		renderThemeCSS(response, "css/window_print.css", "print");
	}
	

	private class NameDisplayToggleLink extends AjaxFallbackLink<Object> {
		private static final long serialVersionUID = 1L;

		public NameDisplayToggleLink(String id) {
			super(id);
		}

		@Override
		public void onClick(AjaxRequestTarget target) {
			showNames = !showNames;
			WebMarkupContainer details = getDetailsPanel();
			if (details instanceof PeriodResponseListPanel)
				((PeriodResponseListPanel) details).setShowNames(showNames);
			target.addComponent(getDetailsPanel());
			target.addComponent(this);;			
		}				
	}


}