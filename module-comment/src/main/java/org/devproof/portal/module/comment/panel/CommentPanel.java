/*
 * Copyright 2009-2010 Carsten Hufe devproof.org
 * 
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 *   
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.devproof.portal.module.comment.panel;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.markup.html.captcha.CaptchaImageResource;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.validator.AbstractValidator;
import org.apache.wicket.validation.validator.EmailAddressValidator;
import org.apache.wicket.validation.validator.StringValidator;
import org.devproof.portal.core.app.PortalSession;
import org.devproof.portal.core.module.common.dataprovider.QueryDataProvider;
import org.devproof.portal.core.module.common.panel.MetaInfoPanel;
import org.devproof.portal.core.module.common.util.PortalUtil;
import org.devproof.portal.module.comment.CommentConstants;
import org.devproof.portal.module.comment.config.CommentConfiguration;
import org.devproof.portal.module.comment.entity.CommentEntity;
import org.devproof.portal.module.comment.query.CommentQuery;
import org.devproof.portal.module.comment.service.CommentService;

/**
 * @author Carsten Hufe
 */
public class CommentPanel extends Panel {

	private static final long serialVersionUID = 1L;

	// @SpringBean(name = "configurationService")
	// private ConfigurationService configurationService;
	@SpringBean(name = "commentDataProvider")
	private QueryDataProvider<CommentEntity> commentDataProvider;
	@SpringBean(name = "commentService")
	private CommentService commentService;
	private CommentQuery query;
	private Boolean captchaEnabled;
	private String captchaChallengeCode;
	private CaptchaImageResource captchaImageResource;

	public CommentPanel(String id, CommentConfiguration configuration) {
		super(id);
		setCaptchaEnabled();
		setCaptchaChallengeCode();
		setCaptchaImageResource();
		add(CSSPackageResource.getHeaderContribution(CommentConstants.class, "css/comment.css"));
		query = new CommentQuery();
		query.setModuleName(configuration.getModuleName());
		query.setModuleContentId(configuration.getModuleContentId());
		commentDataProvider.setQueryObject(query);
		add(new WebMarkupContainer("noCommentsHint") {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return commentDataProvider.size() == 0;
			}

		});
		// Repeater
		add(new CommentDataView("listComment"));
		// Form
		final CommentEntity comment = new CommentEntity();
		comment.setModuleName(configuration.getModuleName());
		comment.setModuleContentId(configuration.getModuleContentId());
		final StatelessForm<CommentEntity> form = new StatelessForm<CommentEntity>("form",
				new CompoundPropertyModel<CommentEntity>(comment));
		TextField<String> guestNameField = new RequiredTextField<String>("guestName");
		guestNameField.add(StringValidator.lengthBetween(3, 50));
		form.add(guestNameField);
		TextField<String> guestEmailField = new RequiredTextField<String>("guestEmail");
		guestEmailField.add(StringValidator.maximumLength(50));
		guestEmailField.add(EmailAddressValidator.getInstance());
		form.add(guestEmailField);
		TextArea<String> commentField = new TextArea<String>("comment");
		commentField.add(StringValidator.lengthBetween(10, 3000));
		form.add(commentField);
		form.add(new AjaxButton("addCommentButton") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				String commentStr = comment.getComment();
				commentStr = StringEscapeUtils.escapeHtml(commentStr).replace("\n", "<br />");
				comment.setComment(commentStr);
				comment.setIpAddress(((PortalSession) getSession()).getIpAddress());
				form.setVisible(false);
				commentService.save(comment);
				target.addComponent(CommentPanel.this);
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				target.addComponent(CommentPanel.this);
			}

		});
		form.add(createCaptchaImageContainer());
		form.add(createCaptchaFieldContainer());
		add(form);
		setOutputMarkupId(true);
	}

	private WebMarkupContainer createCaptchaFieldContainer() {
		WebMarkupContainer trCaptcha2 = new WebMarkupContainer("trCaptcha2");
		trCaptcha2.add(createCaptchaCodeField());
		trCaptcha2.setVisible(captchaEnabled);
		return trCaptcha2;
	}

	private WebMarkupContainer createCaptchaImageContainer() {
		WebMarkupContainer trCaptcha1 = new WebMarkupContainer("trCaptcha1");
		trCaptcha1.add(createCaptchaImage());
		trCaptcha1.setVisible(captchaEnabled);
		return trCaptcha1;
	}

	private FormComponent<String> createCaptchaCodeField() {
		FormComponent<String> fc = new TextField<String>("captchacode", Model.of(""));
		if (captchaEnabled) {
			fc.add(createCaptchaValidator());
		}
		fc.setRequired(captchaEnabled);
		return fc;
	}

	private Image createCaptchaImage() {
		return new Image("captchacodeimage", captchaImageResource);
	}

	private void setCaptchaImageResource() {
		captchaImageResource = new CaptchaImageResource(captchaChallengeCode);
	}

	private void setCaptchaChallengeCode() {
		captchaChallengeCode = PortalUtil.randomString(6, 8);
	}

	private AbstractValidator<String> createCaptchaValidator() {
		return new AbstractValidator<String>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onValidate(IValidatable<String> ivalidatable) {
				if (!captchaChallengeCode.equalsIgnoreCase(ivalidatable.getValue())) {
					captchaImageResource.invalidate();
					error(ivalidatable);
				}
			}

			@Override
			protected String resourceKey() {
				return "wrong.captchacode";
			}
		};
	}

	private void setCaptchaEnabled() {
		captchaEnabled = Boolean.FALSE;// configurationService.findAsBoolean(UserConstants.CONF_REGISTRATION_CAPTCHA);
	}

	private class CommentDataView extends DataView<CommentEntity> {
		private static final long serialVersionUID = 1L;

		public CommentDataView(String id) {
			super(id, commentDataProvider);
			// TODO in config
			// setItemsPerPage(10);
		}

		@Override
		protected void populateItem(Item<CommentEntity> item) {
			item.setOutputMarkupId(true);
			item.add(new CommentView("commentView", item));
		}
	}

	public class CommentView extends Fragment {

		private static final long serialVersionUID = 1L;

		private CommentEntity comment;

		public CommentView(String id, Item<CommentEntity> item) {
			super(id, "commentView", CommentPanel.this);
			comment = item.getModelObject();
			add(new MetaInfoPanel("metaInfo", comment));
			Label commentLabel = new Label("comment", comment.getComment());
			commentLabel.setEscapeModelStrings(false);
			add(commentLabel);
		}
	}
}
