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
package org.devproof.portal.core.module.contact;

import org.apache.wicket.request.resource.PackageResourceReference;

/**
 * @author Carsten Hufe
 */
public interface ContactConstants {

    String CONF_CONTACTFORM_EMAIL = "spring.emailService.findAll.subject.id.contactformemail";
    PackageResourceReference REF_CONTACT_CSS = new PackageResourceReference(ContactConstants.class, "css/contact.css");
    String CONTACT_RIGHT = "contact";
}
