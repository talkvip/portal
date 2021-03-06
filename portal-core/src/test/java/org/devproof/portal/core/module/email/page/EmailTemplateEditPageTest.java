/*
 * Copyright 2009-2011 Carsten Hufe devproof.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.devproof.portal.core.module.email.page;

import org.apache.wicket.model.Model;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.devproof.portal.core.module.email.entity.EmailTemplate;
import org.devproof.portal.test.MockContextLoader;
import org.devproof.portal.test.PortalTestUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.servlet.ServletContext;

/**
 * @author Carsten Hufe
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = MockContextLoader.class,
        locations = {"classpath:/org/devproof/portal/core/test-datasource.xml"})
public class EmailTemplateEditPageTest {
    @Autowired
    private ServletContext servletContext;
    private WicketTester tester;

    @Before
    public void setUp() throws Exception {
        tester = PortalTestUtil.createWicketTester(servletContext);
        PortalTestUtil.loginDefaultAdminUser(tester);
    }

    @After
    public void tearDown() throws Exception {
        PortalTestUtil.destroy(tester);
    }

    @Test
    public void testRenderDefaultPage() {
        tester.startPage(createEmailTemplateEditPage());
        tester.assertRenderedPage(EmailTemplateEditPage.class);
    }

    @Test
    public void testSaveEmailTemplate() {
        callEmailTemplateEditPage();
        submitEmailTemplateForm();
        assertEmailTemplateEditPage();
    }

    private void assertEmailTemplateEditPage() {
        String expectedMsgs[] = PortalTestUtil.getMessage("msg.saved", createEmailTemplateEditPage());
        tester.assertRenderedPage(EmailTemplatePage.class);
        tester.assertNoErrorMessage();
        tester.assertInfoMessages(expectedMsgs);
        tester.startPage(EmailTemplatePage.class);
        tester.assertContains("test subject");
    }

    private void callEmailTemplateEditPage() {
        tester.startPage(createEmailTemplateEditPage());
        tester.assertRenderedPage(EmailTemplateEditPage.class);
    }

    private void submitEmailTemplateForm() {
        FormTester form = tester.newFormTester("form");
        form.setValue("subject", "test subject");
        form.setValue("content", "testing content");
        form.submit();
    }

    private EmailTemplateEditPage createEmailTemplateEditPage() {
        return new EmailTemplateEditPage(Model.of(new EmailTemplate()));
    }
}
