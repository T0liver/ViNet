package hu.toliver.vinet.scan

enum class TractorDisplayMode {
    DayScreen,
    Dim,
    ScreenOff,
    ;

    fun next(): TractorDisplayMode =
        when (this) {
            DayScreen -> Dim
            Dim -> ScreenOff
            ScreenOff -> DayScreen
        }

    val label: String
        get() =
            when (this) {
                DayScreen -> "Day screen"
                Dim -> "Dim"
                ScreenOff -> "Screen off"
            }
}
