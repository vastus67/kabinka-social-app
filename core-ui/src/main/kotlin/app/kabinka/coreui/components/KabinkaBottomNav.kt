package app.kabinka.coreui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Home
import compose.icons.fontawesomeicons.solid.Search
import compose.icons.fontawesomeicons.solid.Edit
import compose.icons.fontawesomeicons.solid.Bell
import compose.icons.fontawesomeicons.solid.User
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
        FontAwesomeIcons.Solid.Home,
        FontAwesomeIcons.Solid.Home
    )

    object Search : BottomNavItem(
        "search",
        "Search",
        FontAwesomeIcons.Solid.Search,
        FontAwesomeIcons.Solid.Search
    )

    object Compose : BottomNavItem(
        "compose",
        "Compose",
        FontAwesomeIcons.Solid.Edit,
        FontAwesomeIcons.Solid.Edit
    )

    object Notifications : BottomNavItem(
        "notifications",
        "Notifications",
        FontAwesomeIcons.Solid.Bell,
        FontAwesomeIcons.Solid.Bell
    )

    object Profile : BottomNavItem(
        "profile",
        "Profile",
        FontAwesomeIcons.Solid.User,
        FontAwesomeIcons.Solid.User
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

    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        Column {
            // Selection indicators at the top edge
            Row(modifier = Modifier.fillMaxWidth()) {
                items.forEach { item ->
                    val selected = currentRoute.startsWith(item.route)
                    
                    if (item is BottomNavItem.Compose) {
                        Spacer(modifier = Modifier.width(56.dp))
                    } else {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(3.dp)
                                .background(
                                    if (selected) MaterialTheme.colorScheme.primary
                                    else Color.Transparent
                                )
                        )
                    }
                }
            }
            
            NavigationBar(
                containerColor = Color.Transparent,
                tonalElevation = 0.dp
            ) {
                items.forEach { item ->
                    val selected = currentRoute.startsWith(item.route)

                    if (item is BottomNavItem.Compose) {
                        FloatingActionButton(
                            onClick = { onNavigate(item.route) },
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            shape = CircleShape,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(
                                imageVector = item.selectedIcon,
                                contentDescription = item.label,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    } else {
                        NavigationBarItem(
                            selected = selected,
                            onClick = { onNavigate(item.route) },
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = item.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1,
                                    softWrap = false
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
    }
}
