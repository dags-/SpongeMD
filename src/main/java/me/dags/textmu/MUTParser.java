package me.dags.textmu;

import java.util.LinkedList;
import java.util.List;

/**
 * @author dags <dags@dags.me>
 */
class MUTParser {

    private final MarkupSpec spec;
    private final String in;

    private boolean blockEscaped = false;
    private boolean charEscaped = false;
    private char next = (char) -1;
    private int pos = -1;

    MUTParser(MarkupSpec spec, String in) {
        this.spec = spec;
        this.in = in;
    }

    MarkupTemplate parse() {
        return new MarkupTemplate(spec, in, parse(false));
    }

    private boolean next() {
        if (pos + 1 < in.length()) {
            next = in.charAt(++pos);
            if (blockEscaped) {
                blockEscaped = next != '`';
                return next();
            }
            if (charEscaped) {
                charEscaped = false;
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

    private List<MarkupTemplate.Component> parse(boolean inArg) {
        List<MarkupTemplate.Component> list = new LinkedList<>();

        int start = pos + 1;
        while (next()) {
            if (next == '{') {
                addPlain(list, start, pos);
                addArg(list);
                start = pos + 1;
                continue;
            }

            if (inArg && (next == '}' || next == ':')) {
                addPlain(list, start, pos);
                return list;
            }
        }

        addPlain(list, start, in.length());

        return list;
    }

    private void addArg(List<MarkupTemplate.Component> list) {
        int start = pos + 1;

        String key = null;
        String separator = "";
        List<MarkupTemplate.Component> template = null;

        while (next()) {
            if (next == ':') {
                key = in.substring(start, pos);
                template = parse(true);
                if (next == '}') {
                    break;
                }
                start = pos + 1;
            }

            if (next == '}') {
                if (template == null) {
                    key = in.substring(start, pos);
                } else {
                    separator = in.substring(start, pos);
                }
                break;
            }
        }

        if (key == null) {
            list.add(plain(start, pos));
            return;
        }

        if (template == null) {
            list.add(new MarkupTemplate.Arg(key));
            return;
        }

        list.add(new MarkupTemplate.Templ(key, separator, template));
    }

    private void addPlain(List<MarkupTemplate.Component> list, int start, int end) {
        if (end > start) {
            list.add(plain(start, end));
        }
    }

    private MarkupTemplate.Component plain(int start, int end) {
        String plain = in.substring(start, end);
        return new MarkupTemplate.Plain(plain);
    }
}
