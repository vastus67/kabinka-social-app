package app.kabinka.frontend.screens

import android.app.Activity
import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kabinka.social.R
import app.kabinka.social.api.session.AccountSessionManager
import app.kabinka.social.fragments.onboarding.InstanceCatalogSignupFragment
import app.kabinka.social.fragments.onboarding.InstanceChooserLoginFragment
import app.kabinka.social.fragments.onboarding.InstanceRulesFragment
import app.kabinka.social.model.Instance
import me.grishka.appkit.Nav
import me.grishka.appkit.api.Callback
import me.grishka.appkit.api.ErrorResponse
import org.parceler.Parcels

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToLoginChooser: () -> Unit = {}
) {
    val context = LocalContext.current
    val activity = context as Activity
    var isLoading by remember { mutableStateOf(false) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF6BCFED), // Sky blue
                        Color(0xFF9FDEAF)  // Light green
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(60.dp))
            
            // Logo/Title area
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Kabinka",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Social networking that's not for sale",
                    fontSize = 16.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Main action buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Join mastodon.social button
                Button(
                    onClick = {
                        isLoading = true
                        val domain = "mastodon.social"
                        
                        Log.d("LoginScreen", "Joining mastodon.social - loading instance info")
                        
                        val progressDialog = ProgressDialog(activity)
                        progressDialog.setCancelable(false)
                        progressDialog.setMessage(activity.getString(R.string.loading_instance))
                        progressDialog.show()
                        
                        AccountSessionManager.loadInstanceInfo(domain, object : Callback<Instance> {
                            override fun onSuccess(result: Instance) {
                                Log.d("LoginScreen", "Instance loaded: ${result.title}")
                                progressDialog.dismiss()
                                isLoading = false
                                
                                if (!result.areRegistrationsOpen()) {
                                    Toast.makeText(
                                        context,
                                        R.string.instance_signup_closed,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    return
                                }
                                
                                // Navigate to InstanceRulesFragment for signup flow
                                val args = Bundle()
                                args.putParcelable("instance", Parcels.wrap(result))
                                Nav.go(activity, InstanceRulesFragment::class.java, args)
                            }
                            
                            override fun onError(error: ErrorResponse) {
                                Log.e("LoginScreen", "Failed to load instance: ${error}")
                                progressDialog.dismiss()
                                isLoading = false
                                error.showToast(activity)
                            }
                        })
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF97316),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Text(
                            text = "Join mastodon.social",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                
                // Pick another server button
                OutlinedButton(
                    onClick = {
                        Log.d("LoginScreen", "Navigating to InstanceCatalogSignupFragment")
                        val args = Bundle()
                        args.putBoolean("signup", true)
                        args.putString("defaultServer", "mastodon.social")
                        Nav.go(activity, InstanceCatalogSignupFragment::class.java, args)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 2.dp,
                        color = Color(0xFFF97316)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Pick another server",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                // OR divider
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = Color.White.copy(alpha = 0.3f),
                        thickness = 1.dp
                    )
                    Text(
                        text = "OR",
                        modifier = Modifier.padding(horizontal = 16.dp),
                        fontSize = 14.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = Color.White.copy(alpha = 0.3f),
                        thickness = 1.dp
                    )
                }
                
                // Learn more and Log in buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TextButton(
                        onClick = {
                            // TODO: Show learn more bottom sheet
                            Toast.makeText(context, "Learn more coming soon", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text(
                            text = "Learn more",
                            fontSize = 16.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    TextButton(
                        onClick = {
                            Log.d("LoginScreen", "Navigating to login chooser")
                            onNavigateToLoginChooser()
                        }
                    ) {
                        Text(
                            text = "Log in",
                            fontSize = 16.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
