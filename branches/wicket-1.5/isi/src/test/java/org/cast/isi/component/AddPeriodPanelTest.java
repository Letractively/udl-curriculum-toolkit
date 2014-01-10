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
package org.cast.isi.component;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;

import org.cast.cwm.data.Period;
import org.cast.cwm.service.ICwmSessionService;
import org.cast.cwm.service.ISiteService;
import org.cast.cwm.test.CwmWicketTester;
import org.cast.cwm.test.GuiceInjectedCwmTestApplication;
import org.cwm.db.service.IModelProvider;
import org.cwm.db.service.SimpleModelProvider;
import org.junit.Before;
import org.junit.Test;

public class AddPeriodPanelTest {
	
	private CwmWicketTester tester;
	private HashMap<Class<? extends Object>, Object> injectionMap;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Before
	public void setUp() {
		setupInjectedServices();
		tester = new CwmWicketTester(new GuiceInjectedCwmTestApplication(injectionMap));
	}
	
	private void setupInjectedServices() {
		injectionMap = new HashMap<Class<? extends Object>, Object>();
		ISiteService siteServiceMock = mock(ISiteService.class);
		when(siteServiceMock.newPeriod()).thenReturn(new Period());
		injectionMap.put(ISiteService.class, siteServiceMock);
		
		injectionMap.put(IModelProvider.class, new SimpleModelProvider());
		
		injectionMap.put(ICwmSessionService.class,  mock(ICwmSessionService.class));
	}

	@Test
	public void canRender() {
		tester.startComponentInPage(new AddPeriodPanel("panel"));
		tester.assertComponent("panel", AddPeriodPanel.class);
	}

}
