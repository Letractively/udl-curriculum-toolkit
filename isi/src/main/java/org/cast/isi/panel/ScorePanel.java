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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.databinder.models.hib.HibernateObjectModel;

import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.cast.cwm.data.Response;
import org.cast.isi.data.ISIPrompt;
import org.cast.isi.data.PromptType;

public abstract class ScorePanel extends Panel {

	private static final long serialVersionUID = 1L;

	private List<IModel<Response>> responseModels;

	public ScorePanel(String id, List<IModel<Response>> models) {
		super(id);
		this.responseModels = models;
		setOutputMarkupId(true);
	}
	
	public boolean isVisible() {
		return !(getResponses().isEmpty());
	}

	@Override
	public void onDetach() {
		for (IModel<Response> responseModel: responseModels) {
			responseModel.detach();
		}
		super.onDetach();
	}
	
	protected static List< IModel<Response>> getResponses(ISortableDataProvider<Response> responseProvider) {
		List< IModel<Response>> responses = new ArrayList< IModel<Response>>();
		for (Iterator<? extends Response> it = responseProvider.iterator(0, Integer.MAX_VALUE); it.hasNext(); ) {
			responses.add(new HibernateObjectModel<Response>(it.next()));
		}
		return responses;
	}

	protected boolean isMarkedCorrect() {
		Integer score = getScore();
		return (score != null) && (score > 0);
	}

	protected boolean isMarkedIncorrect() {
		Integer score = getScore();
		return (score != null) && (score < 1);
	}

	protected boolean isUnmarked() {
		Integer score = getScore();
		return (score == null);
	}

	protected Integer getScore() {
		List<Response> responses = getResponses();
		if (responses.isEmpty())
			return null;
		Response response = responses.get(0);
		ISIPrompt prompt = (ISIPrompt) response.getPrompt();
		PromptType type = prompt.getType();
		if (type.equals(PromptType.SINGLE_SELECT))
			return response.getResponseData().getScore();
		else
			return response.getScore();
	}

	protected List<Response> getResponses() {
		List<Response> result = new ArrayList<Response>();
		for (IModel<Response> model:  responseModels) {
			result.add(model.getObject());
		}
		return result;
	}

}