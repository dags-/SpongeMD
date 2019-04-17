# Quickstart

[Home](index.md)

### Dependency Management

#### Gradle
```gradle
repositories {
    maven { url "https://jitpack.io" }
}

dependencies {
    compile "com.github.dags-:TextMU:$VERSION"
}
```

#### Maven
```maven
<repositories>
  <repository>
    <id>jitpack</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>

<dependencies>
  <dependency>
    <groupId>com.github.dags-</groupId>
    <artifactId>TextMU</artifactId>
    <version>$VERSION</version>
  </dependency>
</dependencies>
```

### Rendering

#### Basic text rendering
```java
Text text = MUSpec.global().render("Hello [World](blue)");
Sponge.getServer().getBroadcastChannel().send(text);
```

_Renders the text "Hello World", where "World" will be colored blue_


#### Permission-based text rendering
```java
Text text = MUSpec.global().render(player, "Hello [World!](green)");
Sponge.getServer().getBroadcastChannel().send(text);
```
_Renders the text "Hello World!", where "World!" will be colored green if the given player has the permission `textmu.color.green`_

### Writing

```java
Text text = Text.builder("Hello ")
                .append(Text.builder("World!?").color(TextColors.RED).build())
                .onClick(TextActions.runCommand("say hi"))
                .build();
                
String string = MUSpec.global().write(text);

System.out.println(string);
```
_Writes the formatted text to the TextMU string: `Hello [World!?](red,/say hi)`_
