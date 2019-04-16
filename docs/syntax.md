# Syntax

[Home](index.md)

# Syntax

The TextMU syntax is similar to Markdown links:
```
[the visible text](color,style,actions)
```

Nested syntax is valid:
```
[Some yellow text and [some green text](green)](yellow)
```

Syntax characters ` [ ] ( ) ` (which would not normally be visible in the rendered text) can be escaped via:
- character escaping by prefixing a backslash: `\[` produces `[`
- string escaping by enclosing in backticks: `` `[escaped string](bold)` `` produces `[escaped string](bold)`

Text actions are supported:

- RunCommand - a command prefixed by one forward slash  
`[Click to run command](/say hello world)`

- SuggestCommand - a command prefixed by two forward slashes  
`[Click to suggest command](//say hello world)`

- OpenURL - a valid URL  
`[Click to open google](https://google.com)`

- HoverText - any other string that does not match the above (can include the MU syntax)  
`[Mouse over me](this is a [hidden](italic) message!)`
