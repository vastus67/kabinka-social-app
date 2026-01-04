package app.kabinka.coreui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

sealed class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Home : BottomNavItem(
        "home",
        "Home",
        Icons.Filled.Home,
        Icons.Outlined.Home
    )

    object Search : BottomNavItem(
        "search",
        "Search",
        Icons.Filled.Search,
        Icons.Outlined.Search
    )

    object Compose : BottomNavItem(
        "compose",
        "Compose",
        Icons.Filled.Add,
        Icons.Outlined.Add
    )

    object Notifications : BottomNavItem(
        "notifications",
        "Notifications",
        Icons.Filled.Notifications,
        Icons.Outlined.Notifications
    )

    object Profile : BottomNavItem(
        "profile",
        "Profile",
        Icons.Filled.Person,
        Icons.Outlined.Person
    )
}

@Composable
fun KabinkaBottomNav(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Search,
        BottomNavItem.Compose,
        BottomNavItem.Notifications,
        BottomNavItem.Profile
    )

    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        items.forEach { item ->
            val selected = currentRoute.startsWith(item.route)

            if (item is BottomNavItem.Compose) {
                Spacer(modifier = Modifier.weight(0.1f))

                FloatingActionButton(
                    onClick = { onNavigate(item.route) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = item.selectedIcon,
                        contentDescription = item.label
                    )
                }

                Spacer(modifier = Modifier.weight(0.1f))
            } else {
                NavigationBarItem(
                    selected = selected,
                    onClick = { onNavigate(item.route) },
                    icon = {
                        Icon(
                            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.label
                        )
                    },
                    label = {
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}
