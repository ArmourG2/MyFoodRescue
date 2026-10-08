package com.lab.myfoodrescue.navigation

enum class Screen(val route: String) {
    LOGIN("login"),
    SIGN_UP("sign_up"),
    MAIN("main"),          // Holds the bottom nav: Home/Search/Booking/History/Profile

    ROLE_SELECTION("role_selection"),
    HOME("home"),
    SEARCH("search"),
    LISTING_DETAIL("listing_detail"),
    RESERVATION("reservation"),
    BOOKING("booking"),
    PICKUP_SCHEDULE("pickup_schedule"),
    STATUS("status"),
    HISTORY("history"),
    PROFILE("profile"),
    SETTINGS("settings"),
    CREATE_DONATION("create_donation")
}