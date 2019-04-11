# Permissions

[Home](index.md)

### Built-in Permissions

| Permission | Description |
| :- | :- |
| `textmu.color.<color>`     | Enables rendering of the given text color |
| `textmu.style.<style>`     | Enables rendering of the given text style |
| `textmu.action.hover`      | Enables rendering of hover text           |
| `textmu.action.url`        | Enables the 'open url' text action        |
| `textmu.action.command`    | Enables the 'run command' text action     |
| `textmu.action.suggestion` | Enables the 'suggest command' text action |


### Custom Permissions

Developers can define their own permission nodes to be used by the MUSpec instance:

1. Create a new `MUPerms` instance by calling `MUPerms.of(base_node)`.The `base_node` string
essentially replaces the word "textmu" from the permissions in the above table.

2. Create a new `MUSpec` instance, using the `MUPerms` from step 1 by calling `MUSpec.create(muperms)`.

3. Use the `MUSpec` instance created in step 2 to perform the text rendering
