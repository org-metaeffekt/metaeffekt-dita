# Markdown Test

This test content was taken from https://github.com/jelovirt/dita-ot-markdown and
is used under Apache License 2.0.

Paragraph ~~Text~~ *test* and **list**:

-   hyphen
-   list
-   item


*   asterix
    *   list
*   item
    *   nested

1.  ordered
1.  list
1.  item
    1.  nested
    1.  list

## Codeblock {.section}

Code example on `for` loop:

    for i in items:
        println(i)

Fenced block:

```{.scala #foreach-example}
items.foreach(println)
```
\<xxxxx>_\<xxxxx>_\<xxxxx>

Tables
------

| First Header | Second Header | Third Header |
| :--- | :--- | -------------------------------: |
| First row    |      Data     | Very long data entry |
| Second row         |    **Cell**   |    x      *Cell* |
[simple_table]

|              | Grouping                    ||
| First Header | Second Header | Third Header |
| ------------ | :-----------: | -----------: |
| Content      | *Long Cell*                 ||
| Content      | **Cell**      | Cell         |
| New section  | More          | Data         |
[Prototype table][reference_table]

Links
-----

*   [Markdown](md_markdown.md)
*   [External](http://www.example.com/test.html)

Keys
----

This is [an example][key-a] reference-style link.

This is [key-a] reference-style link.

This is [an example][key-b] reference-style link.

This is [key-b] reference-style link.

This is [an example][key-missing] reference-style link.

This is [key-missing] reference-style link.

[key-a]: md_markdown.md
[key-b]: md_markdown.md "Markdown"