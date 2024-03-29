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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.util.tester.TagTester;
import org.cast.cwm.components.Icon;
import org.junit.Test;

public class TeacherSectionCompleteToggleImageLinkTest extends
		SectionCompleteToggleImageLinkTestCase {

	@Test
	public void canRender() {
		startWicket();
		wicketTester.assertComponent("panel:component", TeacherSectionCompleteToggleImageLink.class);
	}
	
	@Test
	public void hasCorrectImageWhenUnreviewed() {
		sectionStatus.setCompleted(true);
		sectionStatus.setReviewed(false);
		startWicket();
		Icon image = (Icon) wicketTester.getComponentFromLastRenderedPage("panel:component:doneImg");
		assertThat((String) image.getmImagePath().getObject(), equalTo("img/icons/check_notdone.png"));
	}

	@Test
	public void imageHasCorrectAltTagWhenUnreviewed() {
		sectionStatus.setCompleted(true);
		sectionStatus.setReviewed(false);
		startWicket();
		TagTester imageTag = wicketTester.getTagByWicketId("doneImg");
		assertTrue("Should have alt=\"Not Finished\"", imageTag.getAttributeIs("alt", "Not Finished"));
	}

	@Test
	public void imageHasCorrectTitleTagWhenUnreviewed() {
		sectionStatus.setCompleted(true);
		sectionStatus.setReviewed(false);
		startWicket();
		TagTester imageTag = wicketTester.getTagByWicketId("doneImg");
		assertTrue("Should have title=\"Not Finished\"", imageTag.getAttributeIs("title", "Not Finished"));
	}

	@Test
	public void hasCorrectImageWhenReviewed() {
		sectionStatus.setCompleted(true);
		sectionStatus.setReviewed(true);
		startWicket();
		TagTester imageTag = wicketTester.getTagByWicketId("doneImg");
		assertThat(imageTag.getAttribute("src"), equalTo("../img/icons/check_done.png"));
	}

	@Test
	public void imageHasCorrectAltTagWhenReviewed() {
		sectionStatus.setCompleted(true);
		sectionStatus.setReviewed(true);
		startWicket();
		TagTester imageTag = wicketTester.getTagByWicketId("doneImg");
		assertTrue("Should have alt=\"Finished\"", imageTag.getAttributeIs("alt", "Finished"));
	}

	@Test
	public void imageHasCorrectTitleTagWhenReviewed() {
		sectionStatus.setCompleted(true);
		sectionStatus.setReviewed(true);
		startWicket();
		TagTester imageTag = wicketTester.getTagByWicketId("doneImg");
		assertTrue("Should have title=\"Finished\"", imageTag.getAttributeIs("title", "Finished"));
	}

	@Test
	public void clickingComponentMarksUnreviewedSectionAsReviewed() {
		sectionStatus.setCompleted(true);
		sectionStatus.setReviewed(false);
		startWicket();
		wicketTester.clickLink("panel:component");
		verify(sectionService).setReviewed(eq(student), eq(sectionContentLoc), eq(true));
	}

	@Test
	public void clickingComponentMarksReviewedSectionAsUnreviewed() {
		sectionStatus.setCompleted(true);
		sectionStatus.setReviewed(true);
		startWicket();
		wicketTester.clickLink("panel:component");
		verify(sectionService).setReviewed(eq(student), eq(sectionContentLoc), eq(false));
	}

	@Test
	public void clickingComponentMarksUnreviewedSummativeSectionAsLocked() {
		sectionStatus.setCompleted(true);
		sectionStatus.setReviewed(false);
		when(pageXmlSection.isLockResponse()).thenReturn(true);
		startWicket();
		wicketTester.clickLink("panel:component");
		verify(pageXmlSection).isLockResponse();
		verify(sectionService).setLocked(eq(student), eq(sectionContentLoc), eq(true));
	}

	@Test
	public void clickingComponentMarksReviewedSummativeSectionAsUnlocked() {
		sectionStatus.setCompleted(true);
		sectionStatus.setReviewed(true);
		when(pageXmlSection.isLockResponse()).thenReturn(true);
		startWicket();
		wicketTester.clickLink("panel:component");
		verify(pageXmlSection).isLockResponse();
		verify(sectionService).setLocked(eq(student), eq(sectionContentLoc), eq(false));
	}

	@Test
	public void clickingComponentDoesNotNotAffectLockOnUnreviewedNonSummativeSection() {
		sectionStatus.setCompleted(true);
		sectionStatus.setReviewed(false);
		when(pageXmlSection.isLockResponse()).thenReturn(false);
		startWicket();
		wicketTester.clickLink("panel:component");
		verify(sectionService).setReviewed(eq(student), eq(sectionContentLoc), eq(true));
		verify(pageXmlSection).isLockResponse();
		verify(sectionService, never()).setLocked(eq(student), eq(sectionContentLoc), anyBoolean());
	}

	@Test
	public void clickingComponentDoesNotAffectLockOnReviewedNonSummativeSection() {
		sectionStatus.setCompleted(true);
		sectionStatus.setReviewed(true);
		when(pageXmlSection.isLockResponse()).thenReturn(false);
		startWicket();
		wicketTester.clickLink("panel:component");
		verify(sectionService).setReviewed(eq(student), eq(sectionContentLoc), eq(false));
		verify(pageXmlSection).isLockResponse();
		verify(sectionService, never()).setLocked(eq(student), eq(sectionContentLoc), anyBoolean());
	}

	@Override
	protected Component newTestComponent() {
		return new TeacherSectionCompleteToggleImageLink("component", pageSectionXmlModel, studentModel);
	}

}
