/*
 * Copyright (C) 2000 - 2019 Silverpeas
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.mobile.client.apps.survey;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.silverpeas.mobile.client.apps.navigation.events.app.external.AbstractNavigationEvent;
import org.silverpeas.mobile.client.apps.navigation.events.app.external.NavigationAppInstanceChangedEvent;
import org.silverpeas.mobile.client.apps.navigation.events.app.external.NavigationEventHandler;
import org.silverpeas.mobile.client.apps.navigation.events.app.external.NavigationShowContentEvent;
import org.silverpeas.mobile.client.apps.survey.events.app.AbstractSurveyAppEvent;
import org.silverpeas.mobile.client.apps.survey.events.app.SurveyAppEventHandler;
import org.silverpeas.mobile.client.apps.survey.events.app.SurveyLoadEvent;
import org.silverpeas.mobile.client.apps.survey.events.app.SurveysLoadEvent;
import org.silverpeas.mobile.client.apps.survey.events.pages.SurveyLoadedEvent;
import org.silverpeas.mobile.client.apps.survey.events.pages.SurveysLoadedEvent;
import org.silverpeas.mobile.client.apps.survey.pages.SurveysPage;
import org.silverpeas.mobile.client.apps.survey.resources.SurveyMessages;
import org.silverpeas.mobile.client.common.EventBus;
import org.silverpeas.mobile.client.common.ServicesLocator;
import org.silverpeas.mobile.client.common.app.App;
import org.silverpeas.mobile.client.common.event.ErrorEvent;
import org.silverpeas.mobile.client.common.network.AsyncCallbackOnlineOrOffline;
import org.silverpeas.mobile.client.common.storage.LocalStorageHelper;
import org.silverpeas.mobile.shared.dto.navigation.ApplicationInstanceDTO;
import org.silverpeas.mobile.shared.dto.navigation.Apps;
import org.silverpeas.mobile.shared.dto.survey.SurveyDTO;
import org.silverpeas.mobile.shared.dto.survey.SurveyDetailDTO;

import java.util.ArrayList;
import java.util.List;

public class SurveyApp extends App implements SurveyAppEventHandler, NavigationEventHandler {

  private SurveyMessages msg;
  private ApplicationInstanceDTO instance;

  public SurveyApp(){
    super();
    msg = GWT.create(SurveyMessages.class);
    EventBus.getInstance().addHandler(AbstractSurveyAppEvent.TYPE, this);
    EventBus.getInstance().addHandler(AbstractNavigationEvent.TYPE, this);
  }

  public void start(){
    // always start
  }

  @Override
  public void stop() {
    // nevers stop
  }

  @Override
  public void appInstanceChanged(final NavigationAppInstanceChangedEvent event) {
    if (event.getInstance().getType().equals(Apps.survey.name())) {
      this.instance = event.getInstance();
      SurveysPage page = new SurveysPage();
      page.setPageTitle(event.getInstance().getLabel());
      page.show();
    }
  }

  @Override
  public void showContent(final NavigationShowContentEvent event) {
    if (event.getContent().getType().equals("Component") && event.getContent().getInstanceId().startsWith(Apps.survey.name())) {
      super.showContent(event);
    } else {
      //TODO
    }
  }

  @Override
  public void loadSurveys(final SurveysLoadEvent event) {
    //TODO

    final String key = "surveys_" + instance.getId();
    Command offlineAction = new Command() {
      @Override
      public void execute() {
        List<SurveyDTO> result = LocalStorageHelper.load(key, List.class);
        if (result == null) {
          result = new ArrayList<SurveyDTO>();
        }
        EventBus.getInstance().fireEvent(new SurveysLoadedEvent(result));
      }
    };

    AsyncCallbackOnlineOrOffline action = new AsyncCallbackOnlineOrOffline<List<SurveyDTO>>(offlineAction) {
      @Override
      public void attempt() {
        ServicesLocator.getServiceSurvey().getSurveys(instance.getId(), this);
      }

      @Override
      public void onSuccess(List<SurveyDTO> result) {
        super.onSuccess(result);
        LocalStorageHelper.store(key, List.class, result);
        EventBus.getInstance().fireEvent(new SurveysLoadedEvent(result));
      }
    };
    action.attempt();

  }

  @Override
  public void loadSurvey(final SurveyLoadEvent event) {
      ServicesLocator.getServiceSurvey().getSurvey(event.getId(), new AsyncCallback<SurveyDetailDTO>() {
        @Override
        public void onFailure(final Throwable throwable) {
          EventBus.getInstance().fireEvent(new ErrorEvent(throwable));
        }

        @Override
        public void onSuccess(final SurveyDetailDTO surveyDetailDTO) {
          EventBus.getInstance().fireEvent(new SurveyLoadedEvent(surveyDetailDTO));
        }
      });
  }
}