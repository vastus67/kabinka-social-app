package app.kabinka.social.di

import app.kabinka.social.KabinkaSocialApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SocialModule {
    
    // KabinkaSocialApi is already annotated with @Singleton and @Inject
    // so we don't need to provide it explicitly - Hilt will create it automatically
}
