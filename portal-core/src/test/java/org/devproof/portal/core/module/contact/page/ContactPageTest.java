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
package org.devproof.portal.core.module.contact.page;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.List;

import junit.framework.TestCase;

import org.apache.wicket.PageParameters;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.devproof.portal.core.module.email.bean.EmailPlaceholderBean;
import org.devproof.portal.core.module.email.entity.EmailTemplateEntity;
import org.devproof.portal.core.module.email.service.EmailService;
import org.devproof.portal.test.PortalTestUtil;

/**
 * @author Carsten Hufe
 */
public class ContactPageTest extends TestCase {
	private WicketTester tester;

	@Override
	public void setUp() throws Exception {
		tester = PortalTestUtil.createWicketTesterWithSpringAndDatabase();
		PortalTestUtil.loginDefaultAdminUser(tester);
	}

	@Override
	protected void tearDown() throws Exception {
		PortalTestUtil.destroy(tester);
	}

	public void testRenderDefaultPage() {
		callContactPage();
	}
	
	public void testSendContactForm() throws Exception {
		EmailServiceMock emailServiceMock = createEmailServiceMock();
		callContactPage();
		setEmailServiceMock(emailServiceMock);
		submitContactForm();
		assertEmail(emailServiceMock);
	}

	private void assertEmail(EmailServiceMock emailServiceMock) {
		EmailPlaceholderBean emailPlaceholderBean = emailServiceMock.getEmailPlaceholderBean();
		assertEquals("Max Power", emailPlaceholderBean.getContactFullname());
		assertEquals("max.power@no.domain", emailPlaceholderBean.getContactEmail());
		assertEquals("testing content more then 30 letters 1234567890", emailPlaceholderBean.getContent());
	}

	private void submitContactForm() throws Exception {
		String captchaChallengeCode = getCaptchaChallengeCode();
		FormTester form = tester.newFormTester("form");
		form.setValue("fullname", "Max Power");
		form.setValue("email", "max.power@no.domain");
		form.setValue("content", "testing content more then 30 letters 1234567890");
		form.setValue("trCaptcha2:captchacode", captchaChallengeCode);
		form.submit("sendButton");
	}
	
	private String getCaptchaChallengeCode() {
		ContactPage lastRenderedPage = (ContactPage)tester.getLastRenderedPage();
		return lastRenderedPage.getCaptchaChallengeCode();
	}

	private void callContactPage() {
		tester.startPage(ContactPage.class, new PageParameters("0=admin"));
		tester.assertRenderedPage(ContactPage.class);
	}
	

	private void setEmailServiceMock(EmailService emailServiceMock) throws Exception {
		ContactPage lastRenderedPage = (ContactPage)tester.getLastRenderedPage();
		Field emailServiceField = ContactPage.class.getDeclaredField("emailService");
		emailServiceField.setAccessible(true);
		emailServiceField.set(lastRenderedPage, emailServiceMock);
	}
	
	private EmailServiceMock createEmailServiceMock() {
		return new EmailServiceMock();
	}
	
	private static class EmailServiceMock implements EmailService, Serializable {
		private static final long serialVersionUID = 1L;
		private EmailPlaceholderBean emailPlaceholderBean;
		
		public EmailPlaceholderBean getEmailPlaceholderBean() {
			return emailPlaceholderBean;
		}

		@Override
		public EmailTemplateEntity newEmailTemplateEntity() {
			return null;
		}

		@Override
		public void sendEmail(EmailTemplateEntity template,
				EmailPlaceholderBean placeholder) {
			this.emailPlaceholderBean = placeholder;
		}

		@Override
		public void sendEmail(Integer templateId,
				EmailPlaceholderBean placeholder) {
			this.emailPlaceholderBean = placeholder;
		}

		@Override
		public void delete(EmailTemplateEntity entity) {
			
		}

		@Override
		public List<EmailTemplateEntity> findAll() {
			return null;
		}

		@Override
		public EmailTemplateEntity findById(Integer id) {
			return null;
		}

		@Override
		public void save(EmailTemplateEntity entity) {
			
		}
	}
}
