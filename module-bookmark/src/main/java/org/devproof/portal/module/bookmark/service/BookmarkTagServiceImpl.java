/*
 * Copyright 2009-2010 Carsten Hufe devproof.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.devproof.portal.module.bookmark.service;

import org.devproof.portal.core.module.tag.service.AbstractTagServiceImpl;
import org.devproof.portal.module.bookmark.repository.BookmarkTagRepository;
import org.devproof.portal.module.bookmark.entity.BookmarkTag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Carsten Hufe
 */
@Service("bookmarkTagService")
public class BookmarkTagServiceImpl extends AbstractTagServiceImpl<BookmarkTag> implements BookmarkTagService {
    @Override
    public String getRelatedTagRight() {
        return "bookmark.view";
    }

    @Autowired
    public void setBookmarkTagRepository(BookmarkTagRepository bookmarkTagRepository) {
        setTagRepository(bookmarkTagRepository);
    }
}
