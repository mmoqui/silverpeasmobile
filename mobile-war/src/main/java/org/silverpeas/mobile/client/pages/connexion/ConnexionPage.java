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

package org.silverpeas.mobile.client.pages.connexion;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import org.silverpeas.mobile.client.SpMobil;
import org.silverpeas.mobile.client.common.AuthentificationManager;
import org.silverpeas.mobile.client.common.EventBus;
import org.silverpeas.mobile.client.common.Notification;
import org.silverpeas.mobile.client.common.ServicesLocator;
import org.silverpeas.mobile.client.common.event.ErrorEvent;
import org.silverpeas.mobile.client.common.network.AsyncCallbackOnlineOrOffline;
import org.silverpeas.mobile.client.common.network.OfflineHelper;
import org.silverpeas.mobile.client.common.resources.ResourcesManager;
import org.silverpeas.mobile.client.common.storage.LocalStorageHelper;
import org.silverpeas.mobile.client.components.base.PageContent;
import org.silverpeas.mobile.client.resources.ApplicationMessages;
import org.silverpeas.mobile.shared.dto.DetailUserDTO;
import org.silverpeas.mobile.shared.dto.DomainDTO;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class ConnexionPage extends PageContent {

  private static ConnexionPageUiBinder uiBinder = GWT.create(ConnexionPageUiBinder.class);

  @UiField(provided = true)
  protected ApplicationMessages msg = null;
  @UiField
  Anchor go;
  @UiField
  TextBox loginField;
  @UiField
  PasswordTextBox passwordField;
  @UiField
  ListBox domains;
  @UiField
  FormPanel form;

  interface ConnexionPageUiBinder extends UiBinder<Widget, ConnexionPage> {}

  public ConnexionPage() {
    msg = GWT.create(ApplicationMessages.class);
    loadDomains();
    initWidget(uiBinder.createAndBindUi(this));
    loginField.getElement().setId("Login");
    loginField.getElement().setAttribute("autocapitalize", "off");
    loginField.getElement().setAttribute("autocorrect", "off");
    loginField.getElement().setAttribute("spellcheck", "off");

    passwordField.getElement().setId("Password");
    passwordField.getElement().setAttribute("autocapitalize", "off");
    passwordField.getElement().setAttribute("autocorrect", "off");
    passwordField.getElement().setAttribute("spellcheck", "off");
    domains.getElement().setId("DomainId");
    form.getElement().setId("formLogin");
    form.setAction("/silverpeas/AuthenticationServlet");
    form.setMethod("POST");
    form.getElement().setAttribute("target", "auth");

    Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
      @Override
      public void execute() {
        DOM.getElementById("page-login-title")
            .setInnerHTML(ResourcesManager.getLabel("login.title"));
      }
    });
  }

  /**
   * Gestion du clique sur le bouton go.
   * @param e
   */
  @UiHandler("go")
  void connexion(ClickEvent e) {
    loginField.setFocus(false);
    passwordField.setFocus(false);
    domains.setFocus(false);

    String login = loginField.getText();
    String password = passwordField.getText();
    login(login, password, domains.getValue(domains.getSelectedIndex()));


  }

  /**
   * Transition de la demande de connexion au serveur.
   * @param login
   * @param password
   * @param domainId
   */
  private void login(final String login, final String password, final String domainId) {
    Notification.activityStart();
    if (login.isEmpty()) {
      loginField.getElement().getStyle().setBackgroundColor("#ec9c01");
    } else {
      loginField.getElement().getStyle().clearBackgroundColor();
    }
    if (password.isEmpty()) {
      passwordField.getElement().getStyle().setBackgroundColor("#ec9c01");
    } else {
      passwordField.getElement().getStyle().clearBackgroundColor();
    }

    if (!login.isEmpty() && !password.isEmpty()) {

      ServicesLocator.getServiceConnection()
          .login(login, password, domainId, new AsyncCallback<DetailUserDTO>() {
            @Override
            public void onFailure(Throwable t) {
              Notification.activityStop();
              if (OfflineHelper.needToGoOffine(t)) {
                Notification.alert(msg.needToBeOnline());
              } else {
                EventBus.getInstance().fireEvent(new ErrorEvent(t));
              }
            }

            @Override
            public void onSuccess(DetailUserDTO user) {
              LocalStorageHelper.clear(); // clear offline data
              AuthentificationManager.getInstance().storeUser(user, loginField.getText(), password,
                  domains.getValue(domains.getSelectedIndex()));
              AuthentificationManager.getInstance()
                  .authenticateOnSilverpeas(loginField.getText(), passwordField.getText(),
                      domains.getSelectedValue(), new Command() {
                        @Override
                        public void execute() {
                          SpMobil.displayMainPage();
                        }
                      });
            }
          });
    }
  }

  /**
   * Récupération de la liste des domaines.
   */
  private void loadDomains() {
    Command offlineAction = new Command() {

      @Override
      public void execute() {
        List<DomainDTO> result = loadInLocalStorage();
        displayDomains(result);
      }
    };

    AsyncCallbackOnlineOrOffline action =
        new AsyncCallbackOnlineOrOffline<List<DomainDTO>>(offlineAction) {
          @Override
          public void attempt() {
            ServicesLocator.getServiceConnection().getDomains(this);
          }

          @Override
          public void onSuccess(List<DomainDTO> result) {
            super.onSuccess(result);
            storeInLocalStorage(result);
            displayDomains(result);
          }
        };
    action.attempt();
  }

  private void displayDomains(final List<DomainDTO> result) {
    Iterator<DomainDTO> iDomains = result.iterator();
    while (iDomains.hasNext()) {
      DomainDTO domain = iDomains.next();
      domains.addItem(domain.getName(), domain.getId());
    }
    String defaultDomainId = ResourcesManager.getParam("defaultDomainId");
    for (int i = 0; i < domains.getItemCount(); i++) {
      if (domains.getValue(i).equals(defaultDomainId)) {
        domains.setSelectedIndex(i);
        break;
      }
    }
    if (domains.getItemCount() == 1) {
      domains.setVisible(false);
    }
  }

  private List<DomainDTO> loadInLocalStorage() {
    Storage storage = Storage.getLocalStorageIfSupported();
    List<DomainDTO> result = LocalStorageHelper.load("domains", List.class);
    if (result == null) {
      result = new ArrayList<DomainDTO>();
    }
    return result;
  }

  private void storeInLocalStorage(final List<DomainDTO> result) {
    LocalStorageHelper.store("domains", List.class, result);
  }

}
