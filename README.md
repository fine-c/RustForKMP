# Rust For KMP

该项目包含使用 [UniFFI](https://github.com/mozilla/uniffi-rs.git) 的 Kotlin 多平台绑定生成。

目前仅支持Android、JVM。

# 如何使用

- [Rust](https://rustup.rs/) 用于构建Rust库。
- [cross](https://github.com/cross-rs/cross.git) 用于交叉编译。
建议使用此[命令](https://github.com/cross-rs/cross?tab=readme-ov-file#installation)安装，
而非遵循[入门](https://github.com/cross-rs/cross/blob/main/docs/getting-started.md)中的
[安装 Cross ](https://github.com/cross-rs/cross/blob/main/docs/getting-started.md#installing-cross-1)步骤。
- [Android Studio](https://developer.android.google.cn/studio)

构建插件来自 [uniffi-kotlin-multiplatform-bindings](https://gitlab.com/trixnity/uniffi-kotlin-multiplatform-bindings) 。我对其进行了修改，使其支持使用cross进行交叉编译。

# 实现了什么

一个使用Rust进行简单计算的程序。由于返回类型的原因，结果并不准确。参考此[文档](https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-multiplatform-create-first-app.html#run-your-application)即可在Android、Windows上运行。
