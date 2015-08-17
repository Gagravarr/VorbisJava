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
package org.gagravarr.ogg;

import java.io.IOException;
import java.io.InputStream;

/**
 * Utilities for reading (and hopefully later writing)
 *  a stream of arbitrary bits, in big endian encoding, eg 
 *  "give me the next 3 bits" or "give me bits to the 
 *  byte boundary"
 */
public class BitsReader {
    private InputStream input;
    
    private int tmp = 0;
    private int remaining = 0;
    
    public BitsReader(InputStream input) {
        this.input = input;
    }
    
    public int read(int numBits) throws IOException {
        int res = 0;
        while (numBits > 0 && tmp != -1) {
            if (remaining == 0) {
                tmp = input.read();
                remaining = 8;
            }
            int toNibble = Math.min(remaining, numBits);
            int toLeave = (remaining-toNibble);
            int leaveMask = (1<<toLeave)-1;

            res = res << toNibble;
            res += (tmp>>toLeave);
            tmp = tmp & leaveMask;

            remaining -= toNibble;
            numBits -= toNibble;
        }
        if (tmp == -1) return -1;
        return res;
    }
    
    /**
     * Reads the number to the next byte boundary,
     *  or -1 if already there.
     */
    public int readToByteBoundary() throws IOException {
        if (remaining == 0) return -1;
        return read(remaining);
    }
}
