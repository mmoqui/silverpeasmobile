/*
 * Copyright (C) 2000 - 2018 Silverpeas
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

package org.silverpeas.mobile.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.MetaElement;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import org.silverpeas.mobile.client.apps.agenda.AgendaApp;
import org.silverpeas.mobile.client.apps.blog.BlogApp;
import org.silverpeas.mobile.client.apps.documents.DocumentsApp;
import org.silverpeas.mobile.client.apps.favorites.FavoritesApp;
import org.silverpeas.mobile.client.apps.hyperlink.HyperLinkApp;
import org.silverpeas.mobile.client.apps.media.MediaApp;
import org.silverpeas.mobile.client.apps.navigation.NavigationApp;
import org.silverpeas.mobile.client.apps.navigation.events.pages.HomePageLoadedEvent;
import org.silverpeas.mobile.client.apps.news.NewsApp;
import org.silverpeas.mobile.client.apps.tasks.TasksApp;
import org.silverpeas.mobile.client.apps.webpage.WebPageApp;
import org.silverpeas.mobile.client.apps.workflow.WorkflowApp;
import org.silverpeas.mobile.client.common.AuthentificationManager;
import org.silverpeas.mobile.client.common.ErrorManager;
import org.silverpeas.mobile.client.common.EventBus;
import org.silverpeas.mobile.client.common.Notification;
import org.silverpeas.mobile.client.common.ServicesLocator;
import org.silverpeas.mobile.client.common.ShortCutRouter;
import org.silverpeas.mobile.client.common.app.App;
import org.silverpeas.mobile.client.common.event.ExceptionEvent;
import org.silverpeas.mobile.client.common.mobil.MobilUtils;
import org.silverpeas.mobile.client.common.mobil.Orientation;
import org.silverpeas.mobile.client.common.navigation.PageHistory;
import org.silverpeas.mobile.client.common.network.AsyncCallbackOnlineOnly;
import org.silverpeas.mobile.client.common.network.AsyncCallbackOnlineOrOffline;
import org.silverpeas.mobile.client.common.storage.LocalStorageHelper;
import org.silverpeas.mobile.client.components.base.Page;
import org.silverpeas.mobile.client.components.base.events.window.OrientationChangeEvent;
import org.silverpeas.mobile.client.pages.connexion.ConnexionPage;
import org.silverpeas.mobile.client.pages.main.HomePage;
import org.silverpeas.mobile.client.pages.search.SearchResultPage;
import org.silverpeas.mobile.client.resources.ApplicationMessages;
import org.silverpeas.mobile.shared.dto.DetailUserDTO;
import org.silverpeas.mobile.shared.dto.FullUserDTO;
import org.silverpeas.mobile.shared.dto.HomePageDTO;
import org.silverpeas.mobile.shared.dto.configuration.Config;
import org.silverpeas.mobile.shared.dto.search.ResultDTO;

import java.util.ArrayList;
import java.util.List;

public class SpMobil implements EntryPoint {

  private static Page mainPage = null;
  private static DetailUserDTO user;

  private static String viewport, bodyClass, bodyId;
  private static ApplicationMessages msg;
  private static String shortcutAppId;
  private static String shortcutContentType;
  private static String shortcutContentId;
  private static SpMobil instance = null;
  private static Orientation orientation = null;
  private static List<App> apps = new ArrayList<App>();

  public static DetailUserDTO getUser() {
    return user;
  }

  public static void setUser(final DetailUserDTO user) {
    SpMobil.user = user;
  }

  /**
   * Init. spmobile.
   */
  public void onModuleLoad() {
    instance = this;
    shortcutAppId = Window.Location.getParameter("shortcutAppId");
    shortcutContentType = Window.Location.getParameter("shortcutContentType");
    shortcutContentId = Window.Location.getParameter("shortcutContentId");
    msg = GWT.create(ApplicationMessages.class);
    EventBus.getInstance().addHandler(ExceptionEvent.TYPE, new ErrorManager());
    loadIds(null);

    NodeList<Element> tags = Document.get().getElementsByTagName("meta");
    for (int i = 0; i < tags.getLength(); i++) {
      MetaElement metaTag = ((MetaElement) tags.getItem(i));
      if (metaTag.getName().equals("viewport")) {
        viewport = metaTag.getContent();
      }
    }

    orientation = MobilUtils.getOrientation();
    Window.addResizeHandler(new ResizeHandler() {
      @Override
      public void onResize(final ResizeEvent resizeEvent) {

        if (!MobilUtils.getOrientation().equals(orientation)) {
          orientation = MobilUtils.getOrientation();
          EventBus.getInstance().fireEvent(new OrientationChangeEvent(orientation));
        }

      }
    });

    // Instanciate apps
    apps.add(new DocumentsApp());
    apps.add(new MediaApp());
    apps.add(new NewsApp());
    apps.add(new NavigationApp());
    apps.add(new TasksApp());
    apps.add(new FavoritesApp());
    apps.add(new WebPageApp());
    apps.add(new BlogApp());
    apps.add(new WorkflowApp());
    apps.add(new HyperLinkApp());
    apps.add(new AgendaApp());
  }

  public static Page getMainPage() {
    if (mainPage == null) {
      mainPage = new Page();
    }
    return mainPage;
  }

  public static String getUserToken() {
    if (getUser() != null) {
      return getUser().getToken();
    } else {
      return null;
    }
  }

  public static SpMobil getInstance() {
    return instance;
  }

  /**
   * Auto login.
   * @param user
   * @param password
   */
  private void login(final FullUserDTO user, final String password, final Command attempt) {
    Notification.activityStart();
    if (user != null) {
      ServicesLocator.getServiceNavigation().getUserToken(new AsyncCallback<String>() {
        @Override
        public void onFailure(final Throwable throwable) {
          displayLoginPage();
        }

        @Override
        public void onSuccess(final String token) {
          if (token.equals(SpMobil.getUserToken())) {
            displayMainPage();
          } else {
            AuthentificationManager.getInstance()
                .authenticateOnSilverpeas(user.getLogin(), password, user.getDomainId(), new
                    Command() {
                  @Override
                  public void execute() {
                    if (attempt == null) {
                      displayMainPage();
                    } else {
                      attempt.execute();
                    }
                  }
                });
          }
        }
      });
    } else {
      displayLoginPage();
    }
  }

  public static void displayMainPage() {

    if (!Window.Location.getHref().contains("?locale=") &&
        !user.getLanguage().equalsIgnoreCase("fr")) {
      Window.Location.replace(Window.Location.getHref() + "?locale=" + user.getLanguage());
    }
    getMainPage().setUser(user);
    RootPanel.get().clear();
    RootPanel.get().add(getMainPage());
    PageHistory.getInstance().goTo(new HomePage());

    if (shortcutAppId != null && shortcutContentType != null && shortcutContentId != null) {
      ShortCutRouter.route(user, shortcutAppId, shortcutContentType, shortcutContentId);
    } else {
      final String key = "MainHomePage_";
      AsyncCallbackOnlineOrOffline action =
          new AsyncCallbackOnlineOrOffline<HomePageDTO>(getOfflineAction(key)) {

            @Override
            public void attempt() {
              ServicesLocator.getServiceNavigation().getHomePageData(null, this);
            }

            @Override
            public void onSuccess(HomePageDTO result) {
              super.onSuccess(result);
              // send event to main home page
              EventBus.getInstance().fireEvent(new HomePageLoadedEvent(result));
              LocalStorageHelper.store(key, HomePageDTO.class, result);
            }
          };
      action.attempt();
    }
  }

  private static Command getOfflineAction(final String key) {
    Command offlineAction = new Command() {

      public void execute() {
        HomePageDTO result = LocalStorageHelper.load(key, HomePageDTO.class);
        if (result == null) {
          result = new HomePageDTO();
        }
        // send event to main home page
        EventBus.getInstance().fireEvent(new HomePageLoadedEvent(result));
      }
    };
    return offlineAction;
  }

  /**
   * Load ids in SQL Web Storage.
   */
  public void loadIds(Command attempt) {
    FullUserDTO user = AuthentificationManager.getInstance().loadUser();
    if (user != null) {
      String password = AuthentificationManager.getInstance().decryptPassword(user.getPassword());
      if (password != null) {
        login(user, password, attempt);
      }
    } else {
      tabletGesture(false);
      displayLoginPage();
    }
  }

  private void tabletGesture(boolean connected) {
    if (MobilUtils.isTablet()) {
      if (connected) {
        ServicesLocator.getServiceNavigation().setTabletMode(new AsyncCallback<Boolean>() {
          @Override
          public void onFailure(final Throwable throwable) {
            Notification.activityStop();
          }

          @Override
          public void onSuccess(final Boolean desktopMode) {
            Notification.activityStop();
            if (desktopMode) {
              String url = Window.Location.getHref();
              url = url.substring(0, url.indexOf("silverpeas") + "silverpeas".length());
              Window.Location.replace(url);
            }
          }
        });
      } else {
        ServicesLocator.getServiceConnection().setTabletMode(new AsyncCallback<Boolean>() {
          @Override
          public void onFailure(final Throwable throwable) {
            Notification.activityStop();
          }

          @Override
          public void onSuccess(final Boolean desktopMode) {
            Notification.activityStop();
            if (desktopMode) {
              String url = Window.Location.getHref();
              url = url.substring(0, url.indexOf("silverpeas") + "silverpeas".length());
              Window.Location.replace(url);
            }
          }
        });
      }
    }
  }

  private void displayLoginPage() {
    AuthentificationManager.getInstance().clearUserStorage();
    ConnexionPage connexionPage = new ConnexionPage();
    RootPanel.get().clear();
    RootPanel.get().add(connexionPage);
  }

  public static void search(final String query) {
    Notification.activityStart();
    AsyncCallbackOnlineOnly action = new AsyncCallbackOnlineOnly<List<ResultDTO>>() {
      @Override
      public void attempt() {
        ServicesLocator.getServiceSearch().search(query, this);
      }

      @Override
      public void onSuccess(final List<ResultDTO> results) {
        super.onSuccess(results);
        getMainPage().resetSearchField();
        getMainPage().closeMenu();
        SearchResultPage page = new SearchResultPage();
        page.setPageTitle(msg.results());
        page.setResults(results);
        page.show();
        Notification.activityStop();
      }
    };
    action.attempt();
  }

  public static void showFullScreen(final Widget content, final boolean zoomable, String bodyClass,
      String bodyId) {
    PageHistory.getInstance().gotoToFullScreen("viewer");
    RootPanel.get().clear();
    RootPanel.get().add(content);

    if (zoomable) {
      NodeList<Element> tags = Document.get().getElementsByTagName("meta");
      for (int i = 0; i < tags.getLength(); i++) {
        MetaElement metaTag = ((MetaElement) tags.getItem(i));
        if (metaTag.getName().equals("viewport")) {
          metaTag.setContent("");
        }
      }
    }
    SpMobil.bodyClass = Document.get().getBody().getClassName();
    SpMobil.bodyId = Document.get().getBody().getId();
    Document.get().getBody().setClassName(bodyClass);
    Document.get().getBody().setId(bodyId);
    Document.get().getBody().getStyle().setPaddingTop(0, Style.Unit.PX);
  }

  public static void restoreMainPage() {
    RootPanel.get().clear();
    RootPanel.get().add(SpMobil.getMainPage());

    Document.get().getBody().setId(bodyId);
    Document.get().getBody().setClassName(bodyClass);
    Document.get().getBody().getStyle().clearPaddingTop();

    NodeList<Element> tags = Document.get().getElementsByTagName("meta");
    for (int i = 0; i < tags.getLength(); i++) {
      MetaElement metaTag = ((MetaElement) tags.getItem(i));
      if (metaTag.getName().equals("viewport")) {
        metaTag.setContent(viewport);
      }
    }

  }

  public static void destroyMainPage() {
    mainPage = null;
  }

  public static Config getConfiguration() {
    Config conf = LocalStorageHelper.load("config", Config.class);
    if (conf == null) {
      conf = Config.getDefaultConfig();
    }
    return conf;
  }
}
