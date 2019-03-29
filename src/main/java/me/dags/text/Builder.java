package me.dags.text;

import org.spongepowered.api.text.Text;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

class Builder {

    public static final Builder EMPTY = new Builder();

    private final StringBuilder pre = new StringBuilder();
    private final List<Property> properties = new LinkedList<>();
    private final List<Builder> children = new LinkedList<>();

    public boolean isEmpty() {
        return pre.length() == 0 && children.size() == 0;
    }

    public Builder text(char c) {
        if (children.isEmpty()) {
            pre.append(c);
        } else {
            Builder post = new Builder();
            post.text(c);
            children.add(post);
            return post;
        }
        return this;
    }

    public Builder text(String s) {
        if (!s.isEmpty()) {
            if (children.isEmpty()) {
                pre.append(s);
            } else {
                Builder post = new Builder();
                post.text(s);
                return child(new Builder().text(s));
            }
        }
        return this;
    }

    public Builder property(Property p) {
        if (p != Property.NONE) {
            properties.add(p);
        }
        return this;
    }

    public Builder child(Builder child) {
        children.add(child);
        return this;
    }

    public Text.Builder build() {
        Text.Builder builder;
        Iterator<Builder> iterator = children.iterator();

        if (pre.length() > 0) {
            builder = Text.builder(pre.toString());
        } else if (iterator.hasNext()) {
            builder = iterator.next().build();
        } else {
            return Text.EMPTY.toBuilder();
        }

        while (iterator.hasNext()) {
            builder.append(iterator.next().build().build());
        }

        for (Property property : properties) {
            property.apply(builder);
        }

        return builder;
    }
}
