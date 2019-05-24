import { AppRoutingModule } from './app-routing.module';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { NgModule } from '@angular/core';

import { adapterFactory } from 'angular-calendar/date-adapters/date-fns';
import { CalendarModule, DateAdapter } from 'angular-calendar';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { OwlDateTimeModule, OwlNativeDateTimeModule } from 'ng-pick-datetime';


import { AppComponent } from './app.component';
import {
    CalendarComponent,
    BirthdayComponent,
    CourseComponent,
    HolidayComponent,
    MeetingComponent,
    NavigationComponent,
    TrainerComponent,
} from 'src/app/components';

import { httpInterceptorProviders } from './http-interceptors';
import { SessionStorageService } from './services/session-storage-service';
import { SimpleHttpInterceptor } from './http-interceptors/simple-http-interceptor';
import {HolidayClient} from 'src/app/rest/holiday-client';

@NgModule({
    declarations: [
        AppComponent,
        BirthdayComponent,
        CalendarComponent,
        CourseComponent,
        HolidayComponent,
        MeetingComponent,
        NavigationComponent,
        TrainerComponent,
    ],
    imports: [
        AppRoutingModule,
        BrowserAnimationsModule,
        BrowserModule,
        CalendarModule.forRoot({
            provide: DateAdapter,
            useFactory: adapterFactory,
        }),
        NgbModule,
        FormsModule,
        HttpClientModule,
        OwlDateTimeModule,
        OwlNativeDateTimeModule,
    ],
    providers: [
        httpInterceptorProviders,
        SessionStorageService,
        SimpleHttpInterceptor,
        HolidayClient,
    ],
    bootstrap: [AppComponent],
})
export class AppModule {}
