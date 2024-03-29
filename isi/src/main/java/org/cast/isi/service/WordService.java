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
package org.cast.isi.service;

import java.util.List;

import net.databinder.hib.Databinder;
import net.databinder.models.hib.HibernateListModel;
import net.databinder.models.hib.HibernateObjectModel;

import org.apache.wicket.injection.web.InjectorHolder;
import org.apache.wicket.model.IModel;
import org.cast.cwm.data.User;
import org.cast.cwm.service.ICwmService;
import org.cast.cwm.service.IEventService;
import org.cast.isi.data.WordCard;
import org.cast.isi.data.builder.WordCardsQuery;

import com.google.inject.Inject;

/**
 * Methods to interface with the database representations of WordCards, WordConnections, etc.
 * @author bgoldowsky
 *
 */
public class WordService  {
	
	private static WordService INSTANCE = new WordService();
	
	@Inject
	private ICwmService cwmService;

	@Inject
	private IEventService eventService;

	public WordService () {
		InjectorHolder.getInjector().inject(this);
	}
	
	public static WordService get() { return INSTANCE; }

	public IModel<WordCard> getWordCard (Long id) {
		return new HibernateObjectModel<WordCard>(WordCard.class, id);
	}
	
	public IModel<WordCard> getWordCard (String word, User user) {		
		return new HibernateObjectModel<WordCard>(new WordCardsQuery().setWord(word).setUser(user));
	}
	
	public IModel<List<WordCard>> listWordCards(User user) {
		return new HibernateListModel<WordCard>(new WordCardsQuery().setUser(user));
	}
	
	public IModel<WordCard> getWordCardCreate (String word, User user, boolean inGlossary) {
		IModel<WordCard> cardModel = getWordCard(word, user);
		if (cardModel.getObject() != null)
			return cardModel;
		WordCard wc = new WordCard(word, user);
		wc.setGlossaryWord(inGlossary);
		Databinder.getHibernateSession().save(wc);
		cwmService.flushChanges();
		eventService.saveEvent("wordcard:create", word, "glossary");
		cardModel.setObject(wc);
		return cardModel;
	}
}
