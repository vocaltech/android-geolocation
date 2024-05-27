package fr.vocaltech.location.navigation

import fr.vocaltech.location.R

sealed class BottomNavItem(
    var title: String,
    var icon: Int,
    val route: String
) {
    object Home:
            BottomNavItem(
                "Home",
                R.drawable.baseline_home_24,
                Screens.Home.route
            )

    object List:
            BottomNavItem(
                "Positions",
                R.drawable.baseline_list_24,
                Screens.List.route
            )

    object Analytics:
            BottomNavItem(
                "Tracks",
                R.drawable.baseline_access_time_24,
                Screens.Analytics.route
            )

    object Profile:
            BottomNavItem(
                "Profile",
                R.drawable.baseline_person_24,
                Screens.Profile.route
            )

}