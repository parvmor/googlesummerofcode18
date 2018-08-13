/*
 * =============================================================================
 * Simplified BSD License, see http://www.opensource.org/licenses/
 * -----------------------------------------------------------------------------
 * Copyright (c) 2008-2009, Marco Terzer, Zurich, Switzerland
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Swiss Federal Institute of Technology Zurich
 *       nor the names of its contributors may be used to endorse or promote
 *       products derived from this software without specific prior written
 *       permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * =============================================================================
 */

package tarjanUF;

import java.util.BitSet;
import java.util.concurrent.atomic.AtomicLongArray;

public class ConcurrentBitSet {
    // We use an array of long. So number of bit per unit is 64.
    private static final int BITS_PER_UNIT = 64;

    private volatile AtomicLongArray units;

    // Some constructors.
    public ConcurrentBitSet() {
        this(BITS_PER_UNIT);
    }

    public ConcurrentBitSet(int bitCapacity) {
        units = new AtomicLongArray(1 + (bitCapacity - 1) / BITS_PER_UNIT);
    }

    public ConcurrentBitSet(BitSet bitSet) {
        this(bitSet.length());
        for (int bit = bitSet.nextSetBit(0); bit >= 0; bit = bitSet.nextSetBit(bit + 1)) {
            this.set(bit);
        }
    }

    public ConcurrentBitSet(AtomicLongArray array) {
        units = array;
    }

    // set changes the value at `bit` in the bitset to be `value`.
    public void set(int bit, boolean value) {
        if (value) {
            // set changes the value at `bit` to be 1.
            set(bit);
        } else {
            // clear changes the value at `bit` to be 0.
            clear(bit);
        }
    }

    // set changes the value at `bit` to be 1.
    public void set(int bit) {
        // The unit to which this bit belongs.
        final int unit = bit / BITS_PER_UNIT;
        // The index of the bit in the unit.
        final int index = bit % BITS_PER_UNIT;
        // A bitmask of the bit in the unit.
        final long mask = 1L << index;

        // Run until the `unit`s value is it's old bitwised or with mask
        long old = units.get(unit);
        while (!units.compareAndSet(unit, old, old | mask)) {
            old = units.get(unit);
        }
    }

    // compareAndSet atomically sets the value of `bit` to be `update` given
    // earlier value is `expect`. Returns false if fails to update.
    public boolean compareAndSet(int bit, boolean expect, boolean update) {
        // The unit to which this bit belongs.
        final int unit = bit / BITS_PER_UNIT;
        // The index of the bit in the unit.
        final int index = bit % BITS_PER_UNIT;
        // A bitmask of the bit in the unit.
        final long mask = 1L << index;

        long old = units.get(unit);
        // If we need to make `bit` to be 1 then we should do a bitwise or
        // else we should do a bitwise and with the negation of mask.
        long upd = (update) ? (old | mask) : (old & ~mask);
        // This is used to check if curent value is same as the old one.
        boolean cur = 0L != (old & mask);
        // Interate until new value at `bit` is `update` given that update operation
        // is successful.
        while (cur == expect && !units.compareAndSet(unit, old, upd)) {
            old = units.get(unit);
            upd = (update) ? (old | mask) : (old & ~mask);
            cur = 0L != (old & mask);
        }
        return cur == expect;
    }

    // clear changes the value at `bit` to be 1.
    public void clear(int bit) {
        // The unit to which this bit belongs.
        final int unit = bit / BITS_PER_UNIT;
        // The index of the bit in the unit.
        final int index = bit % BITS_PER_UNIT;
        // A bitmask of the bit in the unit.
        final long mask = 1L << index;

        // Iterate until the new value is cleared.
        long old = units.get(unit);
        while (!units.compareAndSet(unit, old, old & ~mask)) {
            old = units.get(unit);
        }
    }

    // clear is used to make the bitset to be 0s.
    public void clear() {
        for (int i = 0; i < units.length(); i++) {
            units.set(i, 0);
        }
    }

    // get returns the value at `bit`.
    public boolean get(int bit) {
        // The unit to which this bit belongs.
        final int unit = bit / BITS_PER_UNIT;
        // The index of the bit in the unit.
        final int index = bit % BITS_PER_UNIT;
        // A bitmask of the bit in the unit.
        final long mask = 1L << index;

        return 0 != (units.get(unit) & mask);
    }

    // and changes the value of this bitset to be
    // this `bitwise and` with
    public void and(ConcurrentBitSet with) {
        if (this == with) {
            return;
        }

        assert units.length() == with.units.length();
        final int len = units.length();
        for (int i = 0; i < len; i++) {
            long old = units.get(i);
            while (!units.compareAndSet(i, old, old & with.units.get(i))) {
                old = units.get(i);
            }
        }
    }

    // or changes the value of this bitset to be
    // this `bitwise or` with
    public void or(ConcurrentBitSet with) {
        if (this == with) {
            return;
        }

        assert units.length() == with.units.length();
        final int len = units.length();
        for (int i = 0; i < len; i++) {
            long old = units.get(i);
            while (!units.compareAndSet(i, old, old | with.units.get(i))) {
                old = units.get(i);
            }
        }
    }

    // getOr returns a new instance with value being bitwise or of A and B.
    // As static makes no changes to any instance.
    public static ConcurrentBitSet getOr(ConcurrentBitSet A, ConcurrentBitSet B) {
        assert A.units.length() == B.units.length();
        final int len = A.units.length();
        final AtomicLongArray C = new AtomicLongArray(len);

        for (int i = 0; i < len; i++) {
            C.set(i, A.units.get(i) | B.units.get(i));
        }

        return new ConcurrentBitSet(C);
    }

    // getAnd returns a new instance with value being bitwise and of A and B.
    // As static makes no changes to any instance.
    public static ConcurrentBitSet getAnd(ConcurrentBitSet A, ConcurrentBitSet B) {
        assert A.units.length() == B.units.length();
        final int len = A.units.length();
        final AtomicLongArray C = new AtomicLongArray(len);

        for (int i = 0; i < len; i++) {
            C.set(i, A.units.get(i) & B.units.get(i));
        }

        return new ConcurrentBitSet(C);
    }

    // equals checks if the two bitsets A and B are equal.
    public static boolean equals(ConcurrentBitSet A, ConcurrentBitSet B) {
        assert A.units.length() == B.units.length();
        final int len = A.units.length();

        for (int i = 0; i < len; i++) {
            if (A.units.get(i) != B.units.get(i)) {
                return false;
            }
        }

        return true;
    }

    // isEmpty checks if the bitset is all zeros or not.
    public boolean isEmpty() {
        final int len = this.units.length();

        for (int i = 0; i < len; i++) {
            if (this.units.get(i) != 0L) {
                return false;
            }
        }
        return true;
    }
}
