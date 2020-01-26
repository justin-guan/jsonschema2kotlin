private object Versions {
    const val moshiVersion = "1.9.2"

    const val junitVersion = "5.6.0"
    const val kluentVersion = "1.59"
}

object Dependencies {
    const val moshiKotlin = "com.squareup.moshi:moshi-kotlin:${Versions.moshiVersion}"
    const val moshiAdapters = "com.squareup.moshi:moshi-adapters:${Versions.moshiVersion}"
}

object TestDependencies {
    const val junit = "org.junit.jupiter:junit-jupiter:${Versions.junitVersion}"
    const val kluent = "org.amshove.kluent:kluent:${Versions.kluentVersion}"
}
