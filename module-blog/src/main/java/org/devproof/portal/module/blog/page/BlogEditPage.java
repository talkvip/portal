/*
 * Copyright 2009 Carsten Hufe devproof.org
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
package org.devproof.portal.module.blog.page;

import java.util.List;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.validator.StringValidator;
import org.devproof.portal.core.module.common.component.richtext.RichTextArea;
import org.devproof.portal.core.module.right.entity.RightEntity;
import org.devproof.portal.core.module.right.panel.RightGridPanel;
import org.devproof.portal.core.module.tag.component.TagField;
import org.devproof.portal.core.module.tag.service.TagService;
import org.devproof.portal.module.blog.entity.BlogEntity;
import org.devproof.portal.module.blog.entity.BlogTagEntity;
import org.devproof.portal.module.blog.service.BlogService;

/**
 * @author Carsten Hufe
 */
public class BlogEditPage extends BlogBasePage {

	private static final long serialVersionUID = 1L;
	@SpringBean(name = "blogService")
	private BlogService blogService;
	@SpringBean(name = "blogTagService")
	private TagService<BlogTagEntity> blogTagService;

	public BlogEditPage(final BlogEntity blog) {
		super(new PageParameters());
		final RightGridPanel rightGrid = new RightGridPanel("viewright", "blog.view", new ListModel<RightEntity>(blog.getAllRights()));
		IModel<List<BlogTagEntity>> blogListModel = new PropertyModel<List<BlogTagEntity>>(blog, "tags");
		final TagField<BlogTagEntity> tagField = new TagField<BlogTagEntity>("tags", blogListModel, blogTagService);

		final Form<BlogEntity> form = new Form<BlogEntity>("form", new CompoundPropertyModel<BlogEntity>(blog)) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit() {
				BlogEditPage.this.setVisible(false);
				final BlogEntity blog = getModelObject();
				blogService.save(blog);
				setRedirect(false);
				setResponsePage(BlogPage.class, new PageParameters("id=" + blog.getId()));
				info(getString("msg.saved"));
			}
		};
		form.setOutputMarkupId(true);
		form.add(tagField);
		form.add(rightGrid);
		add(form);

		FormComponent<String> fc;

		fc = new RequiredTextField<String>("headline");
		fc.add(StringValidator.minimumLength(3));
		form.add(fc);

		fc = new RichTextArea("content");
		fc.add(StringValidator.minimumLength(3));
		form.add(fc);
	}
}
