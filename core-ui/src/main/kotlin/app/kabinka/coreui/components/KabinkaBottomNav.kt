package app.kabinka.coreui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import compose.icons.FeatherIcons
import compose.icons.feathericons.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
        FeatherIcons.Home,
        FeatherIcons.Home
    )

    object Search : BottomNavItem(
        "search",
        "Search",
        FeatherIcons.Search,
        FeatherIcons.Search
    )

    object Compose : BottomNavItem(
        "compose",
        "Compose",
        FeatherIcons.Edit,
        FeatherIcons.Edit
    )

    object Notifications : BottomNavItem(
        "notifications",
        "Notifications",
        FeatherIcons.Bell,
        FeatherIcons.Bell
    )

    object Profile : BottomNavItem(
        "profile",
        "Profile",
        FeatherIcons.User,
        FeatherIcons.User
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
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(3.dp)
                                    .background(
                                        if (selected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surface
                                    )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Icon(
                                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.label
                            )
                        }
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
                        indicatorColor = Color.Transparent,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}
