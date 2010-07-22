package kieker.tools.traceAnalysis.systemModel.repository;

import java.util.concurrent.atomic.AtomicInteger;

/*
 * ==================LICENCE=========================
 * Copyright 2006-2010 Kieker Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ==================================================
 */

/**
 *
 * @author Andre van Hoorn
 */
public abstract class AbstractSystemSubRepository {
    public final static int ROOT_ELEMENT_ID = 0;

    private final AtomicInteger nextId = new AtomicInteger(ROOT_ELEMENT_ID+1);

    private final SystemModelRepository systemFactory;

    private AbstractSystemSubRepository(){
        this.systemFactory = null;
    }

    public AbstractSystemSubRepository(SystemModelRepository systemFactory){
        this.systemFactory = systemFactory;
    }

    protected final int getAndIncrementNextId() {
        return this.nextId.getAndIncrement();
    }

    protected final SystemModelRepository getSystemFactory() {
        return systemFactory;
    }
}
