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
package org.devproof.portal.core.module.common.locator;

import org.devproof.portal.core.config.Locator;
import org.devproof.portal.core.config.ModuleConfiguration;
import org.devproof.portal.core.config.PageConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Locates the pages of all modules
 *
 * @author Carsten Hufe
 */
@Locator("pageLocator")
public class PageLocatorImpl implements PageLocator {
    private ApplicationContext context;

    @Override
    public Collection<PageConfiguration> getPageConfigurations() {
        Map<String, ModuleConfiguration> beans = context.getBeansOfType(ModuleConfiguration.class);
        List<PageConfiguration> back = new ArrayList<PageConfiguration>();
        for (ModuleConfiguration module : beans.values()) {
            back.addAll(module.getPages());
            for (PageConfiguration page : module.getPages()) {
                page.setModule(module);
            }
        }
        return back;
    }

    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) {
        context = applicationContext;
    }
}
