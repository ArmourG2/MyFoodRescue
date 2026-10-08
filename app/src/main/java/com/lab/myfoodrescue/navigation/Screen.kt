package com.lab.myfoodrescue.navigation

enum class Screen(val route: String) {
    LOGIN("login"),
    SIGN_UP("sign_up"),
    MAIN("main"),          // Recipient shell: bottom nav Home/Search/Reserved/Profile
    COURIER("courier"),    // Courier shell: bottom nav Pickup/History/Profile
    DONOR("donor"),        // Donor shell: bottom nav Home/Donate/History/Profile

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