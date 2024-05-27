package fr.vocaltech.location.navigation

sealed class Screens(val route: String) {
    object Home:
            Screens("home_route")

    object List:
            Screens("list_route")

    object Analytics:
            Screens("analytics_route")

    object Profile:
            Screens("profile_route")
}