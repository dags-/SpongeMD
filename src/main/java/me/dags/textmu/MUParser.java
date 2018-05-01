package me.dags.textmu;

import org.spongepowered.api.text.Text;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author dags <dags@dags.me>
 */
final class MUParser {

    private final MarkupSpec spec;
    private final Buffer buffer;
    private final String in;

    private boolean blockEscaped = false;
    private boolean charEscaped = false;
    private char next = (char) -1;
    private int pos = -1;

    MUParser(MarkupSpec spec, String in) {
        this.buffer = new Buffer(in.length());
        this.spec = spec;
        this.in = in;
    }

    Text parse() {
        return nextText(true).build();
    }

    /**
     * Increment the character position until it is not on whitespace
     */
    private void skipSpace() {
        while (next == ' ' && next()) {

        }
    }

    /**
     * Checks and increments the character position in the input string
     * Reads escaped blocks '`...`' and escaped chars '\\.' in one go (appending the block/char to the buffer)
     */
    private boolean next() {
        if (pos + 1 < in.length()) {
            next = in.charAt(++pos);
            if (blockEscaped) {
                blockEscaped = next != '`';
                if (blockEscaped) {
                    buffer.append(next);
                }
                return next();
            }
            if (charEscaped) {
                charEscaped = false;
                buffer.append(next);
                return next();
            }
            if (next == '`') {
                blockEscaped = true;
                return next();
            }
            if (next == '\\') {
                charEscaped = true;
                return next();
            }
            return true;
        }
        return false;
    }

    /**
     * Reads in the next text section, splitting off an parsing any markup syntax before appending it
     * If is called as the root the input string will be read to the end, otherwise the method terminates
     * at the end of a markup statement on the close brace ')'
     */
    @Nonnull
    private Text.Builder nextText(boolean root) {
        buffer.reset();
        Text.Builder builder = null;

        while (next()) {
            // '[' indicates start of markup syntax
            if (next == '[') {
                // append any plain-text currently in the buffer
                builder = appendPlainText(builder);
                int start = pos;

                // parse markup syntax `[..](...)`, returns null if the syntax is invalid/incomplete
                Text.Builder next = nextStatement();
                if (next != null) {
                    // valid syntax so append
                    builder = append(builder, next);
                    continue;
                }

                // invalid syntax, reset position to read as plain text starting with the '[' char
                pos = start;
                buffer.reset();
                buffer.append('[');
                continue;
            }

            // indicates end of a syntax
            if (!root && next == ')') {
                break;
            }

            // append char as plain-text
            buffer.append(next);
        }

        // append any trailing plain-text
        builder = appendPlainText(builder);

        // builder may still be null at this point
        return builder == null ? Text.builder() : builder;
    }

    /**
     * Parses the markup statement `[..](...)`
     * Starts at pos 1 in the syntax. ie the char immediately after the open bracket '['
     * Ends on the close brace ')'
     * returns null if syntax is incorrect/incomplete
     */
    private Text.Builder nextStatement() {
        // parse params `[..]`
        List<MUParam> params = nextParams();
        if (!next()) {
            // unexpected end of string
            return null;
        }

        if (next != '(') {
            // invalid syntax
            return null;
        }

        // parse content `(...)`
        Text.Builder builder = nextText(false);
        if (next != ')') {
            // invalid syntax
            return null;
        }

        // apply params to valid content
        for (MUParam param : params) {
            if (spec == null || param.test(spec)) {
                param.apply(builder);
            }
        }

        return builder;
    }

    /**
     * Parses the list of parameters between the brackets `[..]` in the statement
     */
    private List<MUParam> nextParams() {
        List<MUParam> params = Collections.emptyList();

        while (next()) {
            // end of params
            if (next == ']') {
                break;
            }

            buffer.append(next);
            MUParam param = nextParam();
            if (param != MUParam.EMPTY) {
                if (params.isEmpty()) {
                    // lazy initialize the params list
                    params = new LinkedList<>();
                }
                params.add(param);
            }

            // end of params
            if (next == ']') {
                break;
            }
        }

        return params;
    }

    /**
     * Parse a single parameter
     */
    private MUParam nextParam() {
        // ignore leading whitespace
        skipSpace();

        // special case where param is hover text
        if (next == '[') {
            Text.Builder hover = nextStatement();
            if (hover == null) {
                return MUParam.EMPTY;
            }
            return MUParam.of(hover.build());
        }

        // read param chars into the buffer
        while (next()) {
            if (next == ',' || next == ']') {
                break;
            }
            buffer.append(next);
        }

        // parse param from the buffer
        return MUParam.of(buffer.drain());
    }

    /**
     * Adds any plain-text stored in the buffer to the current Text.Builder
     */
    private Text.Builder appendPlainText(Text.Builder builder) {
        if (buffer.length() > 0) {
            String plain = buffer.drain();
            if (builder == null) {
                builder = Text.builder(plain);
            } else {
                builder.append(Text.of(plain));
            }
        }
        return builder;
    }

    /**
     * Adds the 'other' Builder to the main one, lazily creating the main Builder if null
     */
    private Text.Builder append(Text.Builder builder, @Nonnull Text.Builder other) {
        if (builder == null) {
            // lazy create builder
            builder = Text.builder();
        }
        return builder.append(other.build());
    }

    private static class Buffer {

        private char[] buffer;
        private int carrot = 0;

        private Buffer(int size) {
            buffer = new char[size + 2];
        }

        /**
         * The length of the current buffered content
         */
        private int length() {
            return carrot;
        }

        /**
         * Resets the carrot position
         * Contents are overwritten as subsequent appends are called rather than creating new char array
         */
        private void reset() {
            carrot = 0;
        }

        /**
         * Append a char to buffer
         */
        private Buffer append(char c) {
            ensureCapacity(carrot + 1);
            buffer[carrot++] = c;
            return this;
        }

        /**
         * Retrieve the string stored in the buffer and reset the carrot to the start
         */
        private String drain() {
            int end = carrot;
            carrot = 0;
            return new String(buffer, 0, end);
        }

        // expand the buffer array as necessary

        /**
         * Expand the buffer array as necessary
         */
        private void ensureCapacity(int size) {
            if (size > buffer.length) {
                buffer = Arrays.copyOf(buffer, size * 2 + 2);
            }
        }
    }
}
