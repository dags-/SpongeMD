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

import impl.Init;
import impl.TestColor;
import impl.TestStyle;
import me.dags.text.MUSpec;
import org.junit.Assert;
import org.junit.Test;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;

public class ParseTests {

    static {
        Init.init();
    }

    @Test
    public void test0() {
        test("hello world", Text.of("hello world"));
    }

    @Test
    public void test1() {
        test("hello [world", Text.of("hello [world"));
    }

    @Test
    public void test2() {
        test("hello world]", Text.of("hello world]"));
    }

    @Test
    public void test3() {
        test("hello world(", Text.of("hello world("));
    }

    @Test
    public void test4() {
        test("hello [world](", Text.of("hello [world]("));
    }

    @Test
    public void test5() {
        test("hello [world](red)", Text.builder("hello ")
                .append(Text.builder("world").color(TestColor.RED).build())
                .build());
    }

    @Test
    public void test6() {
        test(
                "[hover me](some hover text)",
                Text.builder("hover me")
                        .onHover(TextActions.showText(Text.of("some hover text")))
                        .build()
        );
    }

    @Test
    public void test7() {
        test(
                "Plain text [hover me](some hover text)",
                Text.builder("Plain text ").append(Text.builder("hover me")
                        .onHover(TextActions.showText(Text.of("some hover text"))).build())
                        .build()
        );
    }

    @Test
    public void test8() {
        test(
                "Plain text [hover me](some hover text) and more plain text",
                Text.builder("Plain text ").append(Text.builder("hover me")
                        .onHover(TextActions.showText(Text.of("some hover text"))).build())
                        .append(Text.of(" and more plain text"))
                        .build()
        );
    }

    @Test
    public void test9() {
        test(
                "Outer1 [inner1 [inner2 [inner3](red,bold)](blue,italic)](green,[hover text](gold,underline)) outer2",
                Text.builder("Outer1 ")
                        .append(Text.builder("inner1 ")
                                .color(TestColor.GREEN)
                                .onHover(TextActions.showText(Text.builder("hover text")
                                        .color(TestColor.GOLD)
                                        .style(TestStyle.UNDERLINE)
                                        .build()))
                                .append(Text.builder("inner2 ")
                                        .color(TestColor.BLUE)
                                        .style(TestStyle.ITALIC)
                                        .append(Text.builder("inner3")
                                                .color(TestColor.RED)
                                                .style(TestStyle.BOLD)
                                                .build())
                                        .build())
                                .build())
                        .append(Text.of(" outer2"))
                        .build()
        );
    }

    private static void test(String string, Text expected) {
        Text result = MUSpec.global().render(string);
        Assert.assertEquals(expected, result);
    }
}
