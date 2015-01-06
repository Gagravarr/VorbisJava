/*
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
 */
package org.gagravarr.opus;

import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

/**
 * Parent class of Opus read and write tests
 */
public abstract class AbstractOpusTest extends TestCase {
    /** Get a test file created with libopus 0.9.x */
    protected InputStream getTest09File() throws IOException {
        return this.getClass().getResourceAsStream("/testOPUS_09.opus");
    }
    /** Get a test file created with libopus 1.1.x */
    protected InputStream getTest11File() throws IOException {
        return this.getClass().getResourceAsStream("/testOPUS_11.opus");
    }

    protected OpusFile of;

    @Override
    protected void tearDown() throws IOException {
        if (of != null) {
            of.close();
        }
    }
}
