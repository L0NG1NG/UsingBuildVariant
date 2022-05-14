使用 Build Variant
=================

目的
----

把项目中可以共有(不变)的和易变(要变)的东西分开来，分别放在源代码集(main)和变体的代码集(productFlavors )。这样的话，在变体的目录下就能进行相对应的定制操作，最后在打包或者编译就可以选择不同的资源集。

步骤
-----

在项目app目录下的build.gralde文件中定义我们的变体

    android {
        defaultConfig {...}
        ...
        flavorDimensions "api"
        productFlavors {
            minApi21 {
                dimension "api"
                minSdk 21
            }
            minApi23 {
                dimension "api"
                minSdk 23
            }
        }
    }



参考
----

* Android官网[配置build变体](https://developer.android.com/studio/build/build-variants)

* 相关博客：[Product flavour/Flavour Dimensions in Android ](https://proandroiddev.com/product-flavour-flavour-dimensions-in-android-how-to-customize-your-app-395c17b0ff9b)

* 如何去[定义不同变种的类](https://stackoverflow.com/questions/23698863/build-flavors-for-different-version-of-same-class)

