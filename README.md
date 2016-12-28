[![Build Status](https://travis-ci.org/karlicoss/checker-fenum-android-demo.svg?branch=master)](https://travis-ci.org/karlicoss/checker-fenum-android-demo)

This is a demo of using [The Checker Framework](http://types.cs.washington.edu/checker-framework/), specifically,
[fake Enum checker](http://types.cs.washington.edu/checker-framework/current/checker-framework-manual.html#fenum-checker) in Android project.

Why use it? Imagine you're developing a mail application (I do at the moment). You're using a database
and storing all kinds of entities in it: messages, folders, labels, threads, etc. Each of them, of course, has an identifier which is a 
primary key of type `long`. So if you are passing around these ids in your code, they are of same type and you could accidentally pass, say,
account id instead of folder id in your code which is likely to result in crash, or even worse, database corruption. There are several ways
of dealing with this problems:
 
1. Rely on variables names and IDE completion. Android Studio/Intellij are great and pretty smart, and it's unlikely they would
suggest you to pass local variable named `accountId` in the function expecting parameter `folderId`. However, there is still a possibility
of mistaking.

2. Do not expose `long` identifiers, instead, use wrapper classes, e.g.

    ```
    abstract class FolderId {
        public final long id();
    }
    
    abstract class AccountId {
        public final long id();
    }
    ```
While this is the safest way, this is quite annoying since it increases memory footprint and puts unnecessary pressure on garbage collector.
3. Remember what people who are [frightened of Enums](https://www.youtube.com/watch?v=Hzs6OBcvNQE) do? They use 
[@IntDef/@StringDef annotations](https://developer.android.com/studio/write/annotations.html#enum-annotations). The problem with them is
`IntDef`/`StringDef` only work for predefined **finite** sets of values (you have to specify them in your IntDef declaration).
Actually, to me, this restriction doesn't make much sense, but that's the way it is in Android, so I can't do much about it. Here's where 
the Fenum checker helps you.

With Fenum checker you can essentially get type synonyms which Java lacks and do the following
(see [MainActivity.java example](fenum-android-demo/src/main/java/com/github/karlicoss/fenum_android_demo/MainActivity.java) for a full example):

1. Define your own annotation (see [FolderId.java](checker-annotations/src/main/java/com/github/karlicoss/checker_example_annotations/FolderId.java))
    
    ```
    @Documented
    @Retention(RetentionPolicy.SOURCE)
    @Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
    @SubtypeOf(FenumTop.class)
    public @interface FolderId {}
    ```
    
2. Use it on your provider methods. Note that we suppress the warning here since we've got to 'cast' raw long from `Cursor` to a FolderId:
    
    ```
    @SuppressWarnings("fenum:return.type.incompatible")
    @FolderId
    long getFolderId(@MessageId long messageId) {
        Cursor result = null; // SQL query filtering message by id and projecting folderId goes here
        return result.getLong(0);
    }
    ```

3. Annotate consumers with the annotation as well:

    ```
    long getMessagesCount(@FolderId long folderId) { /* whatever */ }
    ```
Now, if you try passing a raw unannotated `long` into `getMessagesCount` or a `long` with different annotation, e.g. `@MessageId`, this
 will result in a compilation error. Yay, disaster averted!
 
4. You can even annotate objects in collections:
 
    ```
    long getTotalMessagesCount(Collection</*@FolderId*/ Long> ids) { /* whatever */ }
    ```
However, we can't write `Collection<@FolderId Long> ids` directly, since in Android we can't annotations on types. Fenum checker 
has a workaround for it and is able to parse the type annotation from comments like in the snippet above. If anyone knows how to trick the compiler
to accept annotations on types here though, please tell me!

The Checker is quite smart and is able to infer local variables and iteration variables type annotations, so your code wouldn't bloat.

# Integrating Fenum checker and running
1. Define your annotations in a separate subproject (here it is the [checker-annotations](checker-annotations) subproject). This is necessary since
[as the section 7.3 of the manual says](http://types.cs.washington.edu/checker-framework/current/checker-framework-manual.html#fenum-running),
the annotations must be compiled *prior* to running the checker, which runs as a part of project's compilation.
2. Copy the Checker configuration ([build.gradle, starting from line 25](fenum-android-demo/build.gradle)]) in your build.gradle. My config
is based on the [official guide](http://types.cs.washington.edu/checker-framework/current/checker-framework-manual.html#android-gradle), 
however, the biggest difference is that it creates separate task for checking each variant (e.g. `checkFenumsDebug`, `checkFenumsRelease`),
so it wouldn't mess with your production code.
3. Don't forget to update the `annotations` variable in build.gradle and specify your custom annotation names there. 
4. Use your annotations in your Android app and run the checker! `./gradlew checkFenumsDebug`

I tried to keep my commits nice, clean and incremental, so if the configuration is unclear, try [looking at commits separately](https://github.com/karlicoss/checker-fenum-android-demo/commits/master).
Or raise an issue, I'll try to help!


# Requirements
* JDK 8, because of `ElementType.TYPE_USE` and `ElementType.TYPE_PARAMETER` on Fenum annotations

# Bugs
* For now, it doesn't seem to work with Jack (see https://github.com/karlicoss/checker-fenum-android-demo/issues/8)

# License

    Copyright 2016 Dmitrii Gerasimov.
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
       http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
