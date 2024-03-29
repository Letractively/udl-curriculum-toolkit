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
package org.cast.isi.data;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.cast.cwm.data.Prompt;
import org.cast.cwm.data.User;
import org.cast.isi.ISIXmlSection;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
@Getter
@Setter
@ToString
@Table(name="prompt")
public class ISIPrompt extends Prompt implements Comparable<ISIPrompt> {

	private static final long serialVersionUID = 1L;
	protected static final Logger log = LoggerFactory.getLogger(ISIPrompt.class);

	@Index(name="prompt_type_idx")
	@Enumerated(EnumType.STRING)
	private PromptType type;
	
	@Index(name="prompt_location_idx")
	@ManyToOne(optional=true)
	private ContentElement contentElement;
	
	@Index(name="prompt_targetuser_idx")
	@ManyToOne(optional=true)
	private User targetUser;
	
	@Index(name="prompt_identifier_idx")
	private String identifier; // Generic identifier (e.g. a glossary word)

	@Index(name="prompt_collection_idx")
	private String collectionName; // grouping of prompts for MyModels, ResponseCollections (or whatever it is named)

	protected ISIPrompt() {
		super();
	}
	
	public ISIPrompt(PromptType type) {
		this();
		this.type = type;
	}
	
	public boolean isDelayFeedback() {
		ISIXmlSection section = getSection();
		return (section != null) && section.isDelayFeedback();
	}
	
	public ISIXmlSection getSection() {
		ContentLoc contentLoc = getContentLoc();
		return (contentLoc == null) ? null: contentLoc.getSection();
	}
	
	public ContentLoc getContentLoc() {
		return contentElement.getContentLocObject();
	}
	
	// Order by the order of the respective ContentElements.  If none, fall back to ID order.
	// Fallback could fail, and throw an exception, if objects are transient.
	public int compareTo(ISIPrompt o) {
		if (this.equals(o))
			return 0;
		if (contentElement == null && o.getContentElement() == null) {
			if (this.isTransient() || o.isTransient())
				throw new IllegalArgumentException("Can't compare transient prompts");
			return getId().compareTo(o.getId());
		}
		if (contentElement == null)
			return -1;
		
		// added these two if statements to sort PAGE_NOTES prompts after other
		// response area prompts on the page
		if (contentElement.getXmlId() == null)
			return 1;
		if (o.contentElement.getXmlId() == null)
			return -1;
		return contentElement.compareTo(o.getContentElement());
	}

}
