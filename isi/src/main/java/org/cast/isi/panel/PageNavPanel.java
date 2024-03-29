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
package org.cast.isi.panel;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.cast.cwm.data.Role;
import org.cast.cwm.data.User;
import org.cast.cwm.data.models.UserModel;
import org.cast.cwm.xml.XmlSection;
import org.cast.cwm.xml.XmlSectionModel;
import org.cast.isi.ISIApplication;
import org.cast.isi.ISISession;
import org.cast.isi.ISIXmlSection;
import org.cast.isi.component.PageNumberLink;
import org.cast.isi.service.ISectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

/**
 * This nav panel adds the previous/next buttons, the completion checkmark, the section title
 * and adds the panel with the page number icons
 */
public class PageNavPanel extends AbstractNavBar<XmlSection> {

	private static final long serialVersionUID = 1L;
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(PageNavPanel.class);

	@Inject
	private ISectionService sectionService;

	public PageNavPanel(String id, IModel<XmlSection> mSection) {
		super(id, mSection);

		UserModel mUser = new UserModel(ISISession.get().getUser());
		IModel<User> mTargetUser = ISISession.get().getTargetUserModel();
		boolean teacher = mUser.getObject().getRole().equals(Role.TEACHER);

		ISIXmlSection currentPage = (ISIXmlSection) mSection.getObject(); // FIXME: XmlSectionModel should be genericized to fix this and similar hacks
		ISIXmlSection currentSection = currentPage.getSectionAncestor(); // May be itself
		
		add(new PageLinkPanel("pageLinkPanel", new XmlSectionModel(currentSection), mSection));

		// Previous & Next Links
		int currentPageNum = ISIApplication.get().getStudentContent().getLabelIndex(ISIXmlSection.SectionType.PAGE, currentPage);
		add(new PageNumberLink("previousPage", currentPageNum-1));
		add(new PageNumberLink("nextPage", currentPageNum+1));

		add(new Label("sectionTitle", currentSection.getTitle()));		

		// If this is a student then add the checkbox for a student.  If this is a teacher then add the checkbox for the teacher
		if (!teacher) {
		// Section Complete Toggle Icon
			SectionCompleteToggleLink toggle = new StudentSectionCompleteToggleImageLink("sectionCompleteToggle", new XmlSectionModel(currentSection)) {
				private static final long serialVersionUID = 1L;
			};
			add(toggle);
		} else {
			// teacher is finished reviewing this section
			TeacherSectionCompleteToggleImageLink toggle = new TeacherSectionCompleteToggleImageLink("sectionCompleteToggle", new XmlSectionModel(currentSection)) {
				private static final long serialVersionUID = 1L;
			};
			// Teacher can only mark as reviewed if the student has marked this as completed
			// and there are no outstanding messages from the student.
			toggle.setEnabled(sectionService.sectionReviewable(mTargetUser.getObject(), currentSection));
			add(toggle);

		}
	}
}