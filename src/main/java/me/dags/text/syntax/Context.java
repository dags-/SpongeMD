/*
 * MIT License
 *
 * Copyright (c) 2019 dags
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package me.dags.text.syntax;

import me.dags.template.CharReader;

public class Context {

    final Builder root;
    Builder builder;

    Context(Builder root) {
        this.root = root;
        this.builder = root;
    }

    void accept(char c) {
        builder = builder.text(c);
    }

    void accept(String string) {
        builder = builder.text(string);
    }

    void append(Builder child, char pre) {
        if (child.isValid()) {
            builder.child(child);
        } else {
            builder.text(pre);
            if (child.isPlain()) {
                builder.text(child.getPlain());
            } else {
                builder.child(child);
            }
            if (child.failChar() != CharReader.EOF) {
                accept(child.failChar());
            }
        }
    }
}
