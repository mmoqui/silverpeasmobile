package com.silverpeas.mobile.client.apps.documents;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.gwtmobile.persistence.client.Collection;
import com.gwtmobile.persistence.client.Entity;
import com.gwtmobile.persistence.client.Persistence;
import com.gwtmobile.persistence.client.ScalarCallback;
import com.silverpeas.mobile.client.apps.documents.events.controller.AbstractDocumentsControllerEvent;
import com.silverpeas.mobile.client.apps.documents.events.controller.DocumentsControllerEventHandler;
import com.silverpeas.mobile.client.apps.documents.events.controller.DocumentsLoadSettingsEvent;
import com.silverpeas.mobile.client.apps.documents.events.controller.DocumentsLoadTopicsEvent;
import com.silverpeas.mobile.client.apps.documents.events.controller.DocumentsSaveSettingsEvent;
import com.silverpeas.mobile.client.apps.documents.events.pages.DocumentsLoadedSettingsEvent;
import com.silverpeas.mobile.client.apps.documents.events.pages.NewInstanceLoadedEvent;
import com.silverpeas.mobile.client.apps.documents.events.pages.navigation.TopicsLoadedEvent;
import com.silverpeas.mobile.client.apps.documents.persistances.DocumentsSettings;
import com.silverpeas.mobile.client.apps.navigation.events.app.AbstractNavigationEvent;
import com.silverpeas.mobile.client.apps.navigation.events.app.NavigationAppInstanceChangedEvent;
import com.silverpeas.mobile.client.apps.navigation.events.app.NavigationEventHandler;
import com.silverpeas.mobile.client.common.Database;
import com.silverpeas.mobile.client.common.EventBus;
import com.silverpeas.mobile.client.common.app.Controller;
import com.silverpeas.mobile.shared.dto.documents.TopicDTO;

public class DocumentsController implements Controller, DocumentsControllerEventHandler, NavigationEventHandler{

	public DocumentsController() {
		super();
		EventBus.getInstance().addHandler(AbstractDocumentsControllerEvent.TYPE, this);
		EventBus.getInstance().addHandler(AbstractNavigationEvent.TYPE, this);
	}
	
	@Override
	public void stop() {
		EventBus.getInstance().removeHandler(AbstractDocumentsControllerEvent.TYPE, this);
		EventBus.getInstance().removeHandler(AbstractNavigationEvent.TYPE, this);		
	}

	@Override
	public void appInstanceChanged(NavigationAppInstanceChangedEvent event) {
		EventBus.getInstance().fireEvent(new NewInstanceLoadedEvent(event.getInstance()));		
	}

	@Override
	public void loadSettings(DocumentsLoadSettingsEvent event) {
		Database.open();		
		final Entity<DocumentsSettings> settingsEntity = GWT.create(DocumentsSettings.class);
		final Collection<DocumentsSettings> settings = settingsEntity.all().limit(1);			
		settings.one(new ScalarCallback<DocumentsSettings>() {
			public void onSuccess(final DocumentsSettings settings) {				
				EventBus.getInstance().fireEvent(new DocumentsLoadedSettingsEvent(settings));
			}
		});		
	}
	
	/**
	 * Store settings.
	 */
	@Override
	public void saveSettings(final DocumentsSaveSettingsEvent event) {
		Database.open();		
		final Entity<DocumentsSettings> settingsEntity = GWT.create(DocumentsSettings.class);
		final Collection<DocumentsSettings> settings = settingsEntity.all();		
		Persistence.schemaSync(new com.gwtmobile.persistence.client.Callback() {
			@Override
			public void onSuccess() {
				settings.destroyAll(new com.gwtmobile.persistence.client.Callback() {
					public void onSuccess() {						
						Persistence.flush();
						final Entity<DocumentsSettings> settingsEntity = GWT.create(DocumentsSettings.class);				
						Persistence.schemaSync(new com.gwtmobile.persistence.client.Callback() {			
							public void onSuccess() {
								final DocumentsSettings settings = settingsEntity.newInstance();
								settings.setSelectedInstanceId(event.getInstance().getId());
								settings.setSelectedInstanceLabel(event.getInstance().getLabel());
								if (event.getTopic() != null) {
									settings.setSelectedTopicId(event.getTopic().getId());
									settings.setSelectedTopicLabel(event.getTopic().getName());
								}
								Persistence.flush();
							}
						});				
					}
				});				
			}		
		});
	}

	@Override
	public void loadTopics(DocumentsLoadTopicsEvent event) {		
		
		//TODO : call remote service
		List<TopicDTO> topics = new ArrayList<TopicDTO>();
		
		for (int i = 0; i < 5; i++) {
			TopicDTO topic = new TopicDTO();
			topic.setId("1");
			topic.setName("test"+i);
			topics.add(topic);
		}				
		
		EventBus.getInstance().fireEvent(new TopicsLoadedEvent(topics));
		
	}
}