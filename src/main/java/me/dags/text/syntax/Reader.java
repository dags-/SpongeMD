package me.dags.text.syntax;

import me.dags.template.CharReader;

import java.io.IOException;

public class Reader {

    private final CharReader reader;
    private char buffered = CharReader.EOF;

    public Reader(CharReader reader) {
        this.reader = reader;
    }

    public char character() {
        if (buffered != CharReader.EOF) {
            char c = buffered;
            buffered = CharReader.EOF;
            return c;
        } else {
            return reader.character();
        }
    }

    public boolean next() throws IOException {
        return buffered != CharReader.EOF || reader.next();
    }

    public char peek() {
        if (buffered == CharReader.EOF) {
            buffered = reader.character();
        }
        return buffered;
    }
}
