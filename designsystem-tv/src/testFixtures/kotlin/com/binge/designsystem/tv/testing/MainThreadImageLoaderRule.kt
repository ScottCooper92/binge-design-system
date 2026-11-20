package com.binge.designsystem.tv.testing

import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.intercept.Interceptor
import coil3.request.ErrorResult
import coil3.request.ImageResult
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Stops Coil doing anything at all for the duration of a Compose test: no network, and no state
 * written from a background thread.
 */
class MainThreadImageLoaderRule : TestRule {
    override fun apply(base: Statement, description: Description): Statement =
        object : Statement() {
            override fun evaluate() {
                SingletonImageLoader.setUnsafe { context ->
                    ImageLoader
                        .Builder(context)
                        .components { add(NoImagesInterceptor) }
                        .interceptorCoroutineContext(EmptyCoroutineContext)
                        .fetcherCoroutineContext(EmptyCoroutineContext)
                        .decoderCoroutineContext(EmptyCoroutineContext)
                        .build()
                }
                try {
                    base.evaluate()
                } finally {
                    SingletonImageLoader.reset()
                }
            }
        }
}

/**
 * Fails every request before a fetcher can run. The painter takes its error branch, which is what it
 * would have done anyway — a test has no network and no image to decode — only without the round trip.
 */
private object NoImagesInterceptor : Interceptor {
    override suspend fun intercept(chain: Interceptor.Chain): ImageResult =
        ErrorResult(
            image = null,
            request = chain.request,
            throwable = UnsupportedOperationException("Images are not loaded in tests (MainThreadImageLoaderRule)"),
        )
}
