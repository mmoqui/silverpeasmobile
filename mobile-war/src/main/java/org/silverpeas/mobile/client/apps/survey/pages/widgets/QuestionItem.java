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

package org.silverpeas.mobile.client.apps.survey.pages.widgets;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;
import org.silverpeas.mobile.client.resources.ApplicationMessages;
import org.silverpeas.mobile.shared.dto.survey.AnswerDTO;
import org.silverpeas.mobile.shared.dto.survey.QuestionDTO;

public class QuestionItem extends Composite {

  private QuestionDTO data;
  private static QuestionItemUiBinder uiBinder = GWT.create(QuestionItemUiBinder.class);
  @UiField
  HTML label;

  @UiField
  HTMLPanel field;

  @UiField
  HTMLPanel container;
  protected ApplicationMessages msg = null;

  interface QuestionItemUiBinder extends UiBinder<Widget, QuestionItem> {}

  public QuestionItem() {
    initWidget(uiBinder.createAndBindUi(this));
    msg = GWT.create(ApplicationMessages.class);
  }

  public void setData(QuestionDTO data) {
    this.data = data;
    label.setText(data.getLabel());

    if (data.getType().equalsIgnoreCase("open")) {
      field.add(new TextArea());
    } else if (data.getType().equalsIgnoreCase("radio")) {
      for (AnswerDTO answer : data.getAnswers()) {
        RadioButton r = new RadioButton(answer.getLabel(), answer.getLabel());
        field.add(r);
      }
    }  else if (data.getType().equalsIgnoreCase("checkbox")) {
      for (AnswerDTO answer : data.getAnswers()) {
        CheckBox cb = new CheckBox(answer.getLabel());
        field.add(cb);
      }
    }  else if (data.getType().equalsIgnoreCase("list")) {
      ListBox l = new ListBox();
      for (AnswerDTO answer : data.getAnswers()) {
        l.addItem(answer.getLabel());
      }
      field.add(l);
    }
  }
}