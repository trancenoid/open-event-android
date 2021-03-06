package org.fossasia.openevent.general.di

import android.arch.persistence.room.Room
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.jasminb.jsonapi.retrofit.JSONAPIConverterFactory
import okhttp3.Authenticator
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.fossasia.openevent.general.BuildConfig
import org.fossasia.openevent.general.OpenEventDatabase
import org.fossasia.openevent.general.about.AboutEventViewModel
import org.fossasia.openevent.general.attendees.*
import org.fossasia.openevent.general.attendees.forms.CustomForm
import org.fossasia.openevent.general.auth.*
import org.fossasia.openevent.general.data.Network
import org.fossasia.openevent.general.data.Preference
import org.fossasia.openevent.general.event.*
import org.fossasia.openevent.general.event.topic.EventTopic
import org.fossasia.openevent.general.event.topic.EventTopicApi
import org.fossasia.openevent.general.event.topic.SimilarEventsViewModel
import org.fossasia.openevent.general.favorite.FavouriteEventsViewModel
import org.fossasia.openevent.general.order.*
import org.fossasia.openevent.general.paypal.Paypal
import org.fossasia.openevent.general.paypal.PaypalApi
import org.fossasia.openevent.general.search.SearchLocationViewModel
import org.fossasia.openevent.general.search.SearchTimeViewModel
import org.fossasia.openevent.general.search.SearchViewModel
import org.fossasia.openevent.general.settings.SettingsFragmentViewModel
import org.fossasia.openevent.general.social.SocialLink
import org.fossasia.openevent.general.social.SocialLinkApi
import org.fossasia.openevent.general.social.SocialLinksService
import org.fossasia.openevent.general.social.SocialLinksViewModel
import org.fossasia.openevent.general.ticket.*
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module.applicationContext
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.jackson.JacksonConverterFactory
import java.util.concurrent.TimeUnit

val commonModule = applicationContext {
    bean { Preference() }
    bean { Network() }
}

val apiModule = applicationContext {
    bean {
        val retrofit: Retrofit = get()
        retrofit.create(EventApi::class.java)
    }
    bean {
        val retrofit: Retrofit = get()
        retrofit.create(AuthApi::class.java)
    }
    bean {
        val retrofit: Retrofit = get()
        retrofit.create(TicketApi::class.java)
    }
    bean {
        val retrofit: Retrofit = get()
        retrofit.create(SocialLinkApi::class.java)
    }
    bean {
        val retrofit: Retrofit = get()
        retrofit.create(EventTopicApi::class.java)
    }
    bean {
        val retrofit: Retrofit = get()
        retrofit.create(AttendeeApi::class.java)
    }
    bean {
        val retrofit: Retrofit = get()
        retrofit.create(OrderApi::class.java)
    }
    bean {
        val retrofit: Retrofit = get()
        retrofit.create(PaypalApi::class.java)
    }

    factory { AuthHolder(get()) }
    factory { AuthService(get(), get(), get()) }

    factory { EventService(get(), get(), get(), get()) }
    factory { TicketService(get(), get()) }
    factory { SocialLinksService(get(), get()) }
    factory { AttendeeService(get(), get(), get()) }
    factory { OrderService(get(), get(), get()) }
}

val viewModelModule = applicationContext {
    viewModel { LoginFragmentViewModel(get(), get()) }
    viewModel { EventsViewModel(get(), get()) }
    viewModel { ProfileFragmentViewModel(get()) }
    viewModel { SignUpFragmentViewModel(get(), get()) }
    viewModel { EventDetailsViewModel(get()) }
    viewModel { SearchViewModel(get(), get(), get()) }
    viewModel { AttendeeViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { SearchLocationViewModel(get()) }
    viewModel { SearchTimeViewModel(get()) }
    viewModel { TicketsViewModel(get(), get()) }
    viewModel { AboutEventViewModel(get()) }
    viewModel { SocialLinksViewModel(get()) }
    viewModel { FavouriteEventsViewModel(get()) }
    viewModel { SettingsFragmentViewModel(get()) }
    viewModel { SimilarEventsViewModel(get()) }
    viewModel { OrderCompletedViewModel(get()) }
    viewModel { OrdersUnderUserVM(get(), get(), get()) }
    viewModel { OrderDetailsViewModel(get(), get()) }
    viewModel { EditProfileViewModel(get(), get()) }
}

val networkModule = applicationContext {

    bean {
        val objectMapper = jacksonObjectMapper()
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        objectMapper
    }

    bean { RequestAuthenticator(get()) as Authenticator }

    bean {
        val connectTimeout = 15 // 15s
        val readTimeout = 15 // 15s

        OkHttpClient().newBuilder()
                .connectTimeout(connectTimeout.toLong(), TimeUnit.SECONDS)
                .readTimeout(readTimeout.toLong(), TimeUnit.SECONDS)
                .addInterceptor(
                        HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
                )
                .authenticator(get())
                .build()
    }

    bean {
        val baseUrl = BuildConfig.DEFAULT_BASE_URL
        val objectMapper: ObjectMapper = get()

        Retrofit.Builder()
                .client(get())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(JSONAPIConverterFactory(objectMapper, Event::class.java, User::class.java, SignUp::class.java, Ticket::class.java, SocialLink::class.java, EventId::class.java, EventTopic::class.java, Attendee::class.java, TicketId::class.java, Order::class.java, AttendeeId::class.java, Charge::class.java, Paypal::class.java, ConfirmOrder::class.java, CustomForm::class.java))
                .addConverterFactory(JacksonConverterFactory.create(objectMapper))
                .baseUrl(baseUrl)
                .build()
    }
}

val databaseModule = applicationContext {

    bean {
        Room.databaseBuilder(androidApplication(),
                OpenEventDatabase::class.java, "open_event_database")
                .fallbackToDestructiveMigration()
                .build()
    }

    factory {
        val database: OpenEventDatabase = get()
        database.eventDao()
    }

    factory {
        val database: OpenEventDatabase = get()
        database.userDao()
    }

    factory {
        val database: OpenEventDatabase = get()
        database.ticketDao()
    }

    factory {
        val database: OpenEventDatabase = get()
        database.socialLinksDao()
    }

    factory {
        val database: OpenEventDatabase = get()
        database.attendeeDao()
    }

    factory {
        val database: OpenEventDatabase = get()
        database.eventTopicsDao()
    }

    factory {
        val database: OpenEventDatabase = get()
        database.orderDao()
    }
}
