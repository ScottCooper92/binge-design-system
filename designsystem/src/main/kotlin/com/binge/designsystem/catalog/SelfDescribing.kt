package com.binge.designsystem.catalog

/**
 * Marks a samples file whose samples say what they show in their own copy: a button labelled with
 * the state it is in, a field whose text names its state. The catalog app shows no description card
 * over them, since the sample already says it; the KDoc stays, for the IDE and the app's search. Put
 * it on the file, `@file:SelfDescribing`, above `package`.
 */
@Target(AnnotationTarget.FILE)
@Retention(AnnotationRetention.SOURCE)
annotation class SelfDescribing
