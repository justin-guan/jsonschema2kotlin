private object Versions {
    const val moshiVersion = "1.9.2"

    const val junitVersion = "5.6.0"
    const val mockkVersion = "1.9"
}

object Dependencies {
    const val moshiKotlin = "com.squareup.moshi:moshi-kotlin:${Versions.moshiVersion}"
    const val moshiAdapters = "com.squareup.moshi:moshi-adapters:${Versions.moshiVersion}"
}

object TestDependencies {
    const val junit = "org.junit.jupiter:junit-jupiter:${Versions.junitVersion}"
    const val hamcrest = "org.hamcrest:hamcrest-all:1.3"
    const val mockk = "io.mockk:mockk:${Versions.mockkVersion}"
}
