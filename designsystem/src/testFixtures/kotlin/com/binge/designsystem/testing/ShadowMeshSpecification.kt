package com.binge.designsystem.testing

import android.graphics.MeshSpecification
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements

/**
 * Lets `android.graphics.MeshSpecification` be built under Robolectric, which ships no native for it.
 *
 * From Compose UI 1.13, `MeshGradientPainter` renders through a renderer that calls
 * `MeshSpecification.make` on API 34+, so composing `HeroBackdropMeshWash` throws without this. The
 * shadow returns a non-zero handle, which is all `make` checks. Drawing then takes the software fallback,
 * and nothing asserts the wash's pixels: the screenshot baselines own its look. Register it in a module's
 * `robolectric.properties` with `shadows=com.binge.designsystem.testing.ShadowMeshSpecification`.
 */
@Implements(value = MeshSpecification::class, minSdk = MESH_MIN_SDK, isInAndroidSdk = false)
// Robolectric instantiates a shadow reflectively, so it must be a class with a public constructor.
@Suppress("UtilityClassWithPublicConstructor")
class ShadowMeshSpecification {
    companion object {
        @JvmStatic
        @Implementation
        @Suppress("UnusedParameter", "LongParameterList")
        fun nativeMake(
            attributes: Array<MeshSpecification.Attribute>,
            vertexStride: Int,
            varyings: Array<MeshSpecification.Varying>,
            vertexShader: String,
            fragmentShader: String,
        ): Long = FAKE_NATIVE_HANDLE

        @JvmStatic
        @Implementation
        @Suppress("UnusedParameter", "LongParameterList")
        fun nativeMakeWithCS(
            attributes: Array<MeshSpecification.Attribute>,
            vertexStride: Int,
            varyings: Array<MeshSpecification.Varying>,
            vertexShader: String,
            fragmentShader: String,
            colorSpace: Long,
        ): Long = FAKE_NATIVE_HANDLE

        @JvmStatic
        @Implementation
        @Suppress("UnusedParameter", "LongParameterList")
        fun nativeMakeWithAlpha(
            attributes: Array<MeshSpecification.Attribute>,
            vertexStride: Int,
            varyings: Array<MeshSpecification.Varying>,
            vertexShader: String,
            fragmentShader: String,
            colorSpace: Long,
            alphaType: Int,
        ): Long = FAKE_NATIVE_HANDLE
    }
}

/** `MeshSpecification` and `Mesh` are API 34 (`UPSIDE_DOWN_CAKE`). */
private const val MESH_MIN_SDK = 34

/** Any non-zero handle: `MeshSpecification.make` throws on zero, and nothing reads the handle back. */
private const val FAKE_NATIVE_HANDLE = 1L
