package com.crack.gastoslistener.parsers

object ParserRegistry {
    private val parsers: List<NotificationParser> = listOf(
        GooglePayParser(),
        SantanderParser(),
        RevolutParser(),
        TradeRepublicParser()
        // Añade aquí más bancos/fintech: BBVA, CaixaBank, N26, Wise, etc.
        // Cada uno con su applicationId de paquete real (sácalo con
        // `adb shell pm list packages | grep -i banco` o similar).
    )

    private val byPackage: Map<String, NotificationParser> =
        parsers.flatMap { p -> p.packageNames.map { pkg -> pkg to p } }.toMap()

    fun forPackage(packageName: String): NotificationParser? = byPackage[packageName]

    fun allPackageNames(): Set<String> = byPackage.keys
}
